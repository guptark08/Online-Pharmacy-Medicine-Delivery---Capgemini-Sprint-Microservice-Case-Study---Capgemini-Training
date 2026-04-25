window.onload = function () {
  function resolveBearerToken() {
    try {
      var storageKeys = ["authorized", "swagger-ui.auth"];
      var rawAuthorized = null;
      for (var s = 0; s < storageKeys.length; s++) {
        rawAuthorized = window.localStorage.getItem(storageKeys[s]);
        if (rawAuthorized) {
          break;
        }
      }
      if (!rawAuthorized) {
        return null;
      }

      var authorized = JSON.parse(rawAuthorized);
      var candidates = ["BearerAuth", "Bearer Auth"];
      for (var i = 0; i < candidates.length; i++) {
        var entry = authorized[candidates[i]];
        if (entry && typeof entry.value === "string" && entry.value.trim().length > 0) {
          return entry.value.startsWith("Bearer ") ? entry.value : "Bearer " + entry.value;
        }
      }
    } catch (ignored) {
      return null;
    }
    return null;
  }

  function decodeJwtPayload(token) {
    try {
      if (!token) {
        return null;
      }

      var rawToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
      var parts = rawToken.split(".");
      if (parts.length < 2) {
        return null;
      }

      var payload = parts[1].replace(/-/g, "+").replace(/_/g, "/");
      while (payload.length % 4 !== 0) {
        payload += "=";
      }

      return JSON.parse(window.atob(payload));
    } catch (ignored) {
      return null;
    }
  }

  function resolveIdentity() {
    var token = resolveBearerToken();
    var payload = decodeJwtPayload(token);
    if (!payload) {
      return null;
    }

    var role = payload.role || payload.authorities || payload.scope || "UNKNOWN";
    if (Array.isArray(role)) {
      role = role.join(", ");
    }

    return {
      username: payload.sub || payload.username || "unknown",
      role: String(role).replace(/^ROLE_/, "")
    };
  }

  function ensureIdentityBanner() {
    var container = document.querySelector(".swagger-ui");
    if (!container) {
      return null;
    }

    var banner = document.getElementById("swagger-auth-identity");
    if (banner) {
      return banner;
    }

    banner = document.createElement("div");
    banner.id = "swagger-auth-identity";
    banner.style.margin = "16px 0";
    banner.style.padding = "12px 16px";
    banner.style.borderRadius = "8px";
    banner.style.border = "1px solid #d0d7de";
    banner.style.background = "#f6f8fa";
    banner.style.color = "#24292f";
    banner.style.fontFamily = "monospace";
    banner.style.fontSize = "14px";
    banner.style.display = "none";

    container.insertBefore(banner, container.firstChild);
    return banner;
  }

  function renderIdentityBanner() {
    var banner = ensureIdentityBanner();
    if (!banner) {
      return;
    }

    var identity = resolveIdentity();
    if (!identity) {
      banner.style.display = "none";
      banner.textContent = "";
      return;
    }

    banner.style.display = "block";
    banner.textContent = "Authenticated user: " + identity.username + " | Role: " + identity.role;
  }

  var IdentityBannerPlugin = function () {
    return {
      statePlugins: {
        auth: {
          wrapActions: {
            authorize: function (originalAction) {
              return function (payload) {
                var result = originalAction(payload);
                setTimeout(renderIdentityBanner, 50);
                return result;
              };
            },
            logout: function (originalAction) {
              return function (payload) {
                var result = originalAction(payload);
                setTimeout(renderIdentityBanner, 50);
                return result;
              };
            }
          }
        }
      }
    };
  };

  window.ui = SwaggerUIBundle({
    configUrl: "/v3/api-docs/swagger-config",
    dom_id: "#swagger-ui",
    deepLinking: true,
    presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
    plugins: [SwaggerUIBundle.plugins.DownloadUrl, IdentityBannerPlugin],
    layout: "StandaloneLayout",
    persistAuthorization: true,
    requestInterceptor: function (request) {
      var token = resolveBearerToken();
      if (token) {
        request.headers = request.headers || {};
        request.headers.Authorization = token;
      }
      return request;
    }
  });

  setTimeout(renderIdentityBanner, 100);
};
