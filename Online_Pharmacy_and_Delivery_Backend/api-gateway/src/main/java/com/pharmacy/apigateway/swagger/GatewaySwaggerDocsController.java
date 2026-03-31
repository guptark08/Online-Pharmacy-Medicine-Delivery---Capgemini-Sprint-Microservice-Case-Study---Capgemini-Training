package com.pharmacy.apigateway.swagger;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@RestController
public class GatewaySwaggerDocsController {

    private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD");

    private static final Map<String, String> SUPPORTED_SERVICE_IDS = buildSupportedServices();
    private static final Map<String, String> SERVICE_OPENAPI_URLS = buildServiceOpenApiUrls();

    private final WebClient webClient;
    private final GatewayOpenApiAccessPolicy accessPolicy;
    private final JwtRoleResolver jwtRoleResolver;
    private final ObjectMapper objectMapper;

    public GatewaySwaggerDocsController(
            GatewayOpenApiAccessPolicy accessPolicy,
            JwtRoleResolver jwtRoleResolver,
            ObjectMapper objectMapper) {
        this.webClient = WebClient.create();
        this.accessPolicy = accessPolicy;
        this.jwtRoleResolver = jwtRoleResolver;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/gateway-docs/{serviceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getGatewayFilteredDocs(
            @PathVariable String serviceKey,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            ServerWebExchange exchange) {

        String serviceId = resolveServiceId(serviceKey);
        if (serviceId == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson("Unknown service key: " + serviceKey)));
        }

        SwaggerUserContext userContext = jwtRoleResolver.resolveFromAuthorizationHeader(authorizationHeader);
        String downstreamOpenApiUrl = SERVICE_OPENAPI_URLS.get(serviceId);
        if (downstreamOpenApiUrl == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson("OpenAPI endpoint not configured for service: " + serviceId)));
        }

        return webClient
                .get()
                .uri(downstreamOpenApiUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(rawJson -> decorateAndRewriteOpenApi(rawJson, serviceId, userContext, exchange))
                .map(filteredJson -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(filteredJson))
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson("Unable to load OpenAPI docs for " + serviceId))));
    }

    private String decorateAndRewriteOpenApi(
            String rawJson,
            String serviceId,
            SwaggerUserContext userContext,
            ServerWebExchange exchange) {
        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(rawJson);

            forceGatewayServer(root, exchange);
            ensureBearerScheme(root);
            decoratePathOperations(root, serviceId, userContext);

            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException ex) {
            return rawJson;
        }
    }

    private void forceGatewayServer(ObjectNode root, ServerWebExchange exchange) {
        String scheme = exchange.getRequest().getURI().getScheme();
        String host = exchange.getRequest().getHeaders().getFirst(HttpHeaders.HOST);
        if (scheme == null || host == null || host.isBlank()) {
            return;
        }

        ArrayNode servers = objectMapper.createArrayNode();
        ObjectNode server = objectMapper.createObjectNode();
        server.put("url", scheme + "://" + host);
        server.put("description", "API Gateway");
        servers.add(server);
        root.set("servers", servers);
    }

    private void ensureBearerScheme(ObjectNode root) {
        ObjectNode components = root.with("components");
        ObjectNode securitySchemes = components.with("securitySchemes");

        JsonNode legacyScheme = securitySchemes.get("Bearer Auth");
        if (legacyScheme != null && !securitySchemes.has("BearerAuth")) {
            securitySchemes.set("BearerAuth", legacyScheme.deepCopy());
        }
        securitySchemes.remove("Bearer Auth");

        if (!securitySchemes.has("BearerAuth")) {
            ObjectNode bearerScheme = objectMapper.createObjectNode();
            bearerScheme.put("type", "http");
            bearerScheme.put("scheme", "bearer");
            bearerScheme.put("bearerFormat", "JWT");
            bearerScheme.put("description", "Paste JWT token. Example: Bearer eyJ...");
            securitySchemes.set("BearerAuth", bearerScheme);
        }

        normalizeSecurityRequirements(root.get("security"));
    }

    private void decoratePathOperations(ObjectNode root, String serviceId, SwaggerUserContext userContext) {
        JsonNode pathsNode = root.get("paths");
        if (!(pathsNode instanceof ObjectNode pathsObject)) {
            return;
        }

        pathsObject.fields().forEachRemaining(pathEntry -> {
            String path = pathEntry.getKey();
            JsonNode value = pathEntry.getValue();
            if (!(value instanceof ObjectNode operationGroup)) {
                return;
            }

            operationGroup.fields().forEachRemaining(methodEntry -> {
                String method = methodEntry.getKey();
                String normalizedMethod = method.toUpperCase(Locale.ROOT);
                if (!HTTP_METHODS.contains(normalizedMethod)) {
                    return;
                }

                ApiAccessLevel accessLevel = accessPolicy.resolveAccess(serviceId, path, normalizedMethod);
                JsonNode operationNode = methodEntry.getValue();
                if (operationNode instanceof ObjectNode operationObject) {
                    normalizeSecurityRequirements(operationObject.get("security"));
                    updateOperationSecurity(operationObject, accessLevel);
                    decorateOperationWithAccessHints(operationObject, accessLevel, userContext);
                }
            });
        });
    }

    private void updateOperationSecurity(ObjectNode operationObject, ApiAccessLevel accessLevel) {
        if (!accessLevel.requiresJwt()) {
            operationObject.remove("security");
            return;
        }

        ArrayNode securityArray = objectMapper.createArrayNode();
        ObjectNode securityRequirement = objectMapper.createObjectNode();
        securityRequirement.set("BearerAuth", objectMapper.createArrayNode());
        securityArray.add(securityRequirement);
        operationObject.set("security", securityArray);
    }

    private void decorateOperationWithAccessHints(
            ObjectNode operationObject,
            ApiAccessLevel accessLevel,
            SwaggerUserContext userContext) {

        operationObject.put("x-required-access-level", accessLevel.name());
        operationObject.put("x-token-authorized", accessLevel.isAllowedFor(userContext));

        String requiredAccess = switch (accessLevel) {
            case PUBLIC -> "PUBLIC";
            case AUTHENTICATED -> "AUTHENTICATED";
            case CUSTOMER -> "CUSTOMER";
            case ADMIN -> "ADMIN";
        };

        String roleHint = "Required access: " + requiredAccess + ".";

        String existingDescription = operationObject.has("description")
                ? operationObject.get("description").asText("")
                : "";

        String mergedDescription = existingDescription.isBlank()
                ? roleHint
                : existingDescription + "\n\n" + roleHint;

        operationObject.put("description", mergedDescription.trim());
    }

    private void normalizeSecurityRequirements(JsonNode securityNode) {
        if (!(securityNode instanceof ArrayNode securityArray)) {
            return;
        }

        for (JsonNode requirementNode : securityArray) {
            if (!(requirementNode instanceof ObjectNode requirementObject)) {
                continue;
            }

            JsonNode legacyRequirement = requirementObject.get("Bearer Auth");
            if (legacyRequirement != null && !requirementObject.has("BearerAuth")) {
                requirementObject.set("BearerAuth", legacyRequirement.deepCopy());
            }
            requirementObject.remove("Bearer Auth");
        }
    }

    private String resolveServiceId(String serviceKey) {
        if (serviceKey == null || serviceKey.isBlank()) {
            return null;
        }
        String normalizedKey = serviceKey.trim().toLowerCase(Locale.ROOT);
        return SUPPORTED_SERVICE_IDS.get(normalizedKey);
    }

    private static Map<String, String> buildSupportedServices() {
        Map<String, String> serviceMap = new LinkedHashMap<>();
        serviceMap.put("auth", "auth-service");
        serviceMap.put("auth-service", "auth-service");
        serviceMap.put("catalog", "catalog-service");
        serviceMap.put("catalog-service", "catalog-service");
        serviceMap.put("order", "order-service");
        serviceMap.put("order-service", "order-service");
        serviceMap.put("admin", "admin-service");
        serviceMap.put("admin-service", "admin-service");
        return serviceMap;
    }

    private static Map<String, String> buildServiceOpenApiUrls() {
        Map<String, String> urls = new LinkedHashMap<>();
        urls.put("auth-service", "http://auth-service:8081/api-docs");
        urls.put("catalog-service", "http://catalog-service:8082/api-docs");
        urls.put("order-service", "http://order-service:8083/api-docs");
        urls.put("admin-service", "http://admin-service:8084/api-docs");
        return urls;
    }

    private String errorJson(String message) {
        return "{\"success\":false,\"message\":\"" + message + "\"}";
    }
}
