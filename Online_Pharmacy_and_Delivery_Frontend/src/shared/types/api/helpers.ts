import type { paths as AuthPaths } from "./auth"
import type { paths as CatalogPaths } from "./catalog"
import type { paths as OrderPaths } from "./order"
import type { paths as AdminPaths } from "./admin"

// Example: extract request/response for a specific endpoint

// Usage: type LoginRequest = AuthBody<"/api/auth/login", "post">

export type AuthBody<P extends keyof AuthPaths, M extends keyof AuthPaths[P]> =
  AuthPaths[P][M] extends { 
        requestBody: { 
            content: { "application/json": infer B } } } ? B : never

export type AuthResponse<P extends keyof AuthPaths, M extends keyof AuthPaths[P]> =
  AuthPaths[P][M] extends { 
        responses: { 200: { 
            content: { "application/json": infer R } } } } ? R : never

export type CatalogBody<P extends keyof CatalogPaths, M extends keyof CatalogPaths[P]> =
    CatalogPaths[P][M] extends { 
        requestBody: { 
            content: { "application/json": infer B } } } ? B : never

export type CatalogResponse<P extends keyof CatalogPaths, M extends keyof CatalogPaths[P]> =
    CatalogPaths[P][M] extends { 
        responses: { 200: { 
            content: { "application/json": infer R } } } } ? R : never

export type OrderBody<P extends keyof OrderPaths, M extends keyof OrderPaths[P]> =
    OrderPaths[P][M] extends { 
        requestBody: { 
            content: { "application/json": infer B } } } ? B : never

export type OrderResponse<P extends keyof OrderPaths, M extends keyof OrderPaths[P]> =
    OrderPaths[P][M] extends { 
        responses: { 200: { 
            content: { "application/json": infer R } } } } ? R : never

export type AdminBody<P extends keyof AdminPaths, M extends keyof AdminPaths[P]> =
    AdminPaths[P][M] extends { 
        requestBody: { 
            content: { "application/json": infer B } } } ? B : never

export type AdminResponse<P extends keyof AdminPaths, M extends keyof AdminPaths[P]> =
    AdminPaths[P][M] extends { 
        responses: { 200: { 
            content: { "application/json": infer R } } } } ? R : never