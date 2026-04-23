import axios, { AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from "axios"
import { useAuthStore } from "@/shared/stores/authStore"

// ───────────────────────────────────────────────────────────
// AXIOS INSTANCE
// ───────────────────────────────────────────────────────────

export const api = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_URL,
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
})

// ───────────────────────────────────────────────────────────
// ENDPOINTS THAT DON'T GET THE TOKEN
// ───────────────────────────────────────────────────────────
// These endpoints are public (no Authorization header) or special (refresh).
// Attaching a stale token to a signup/login call is actively harmful.

const PUBLIC_ENDPOINTS = [
  "/api/auth/signup",
  "/api/auth/verify-password-then-send-otp",
  "/api/auth/verify-login-otp",
  "/api/auth/verify-email",
  "/api/auth/resend-verification",
  "/api/auth/forgot-password",
  "/api/auth/reset-password",
  "/api/auth/refresh",
]

function isPublicEndpoint(url: string | undefined): boolean {
  if (!url) return false
  return PUBLIC_ENDPOINTS.some((endpoint) => url.includes(endpoint))
}

// ───────────────────────────────────────────────────────────
// REQUEST INTERCEPTOR — attach the access token
// ───────────────────────────────────────────────────────────

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  if (isPublicEndpoint(config.url)) {
    return config
  }

  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ───────────────────────────────────────────────────────────
// REFRESH QUEUE — the hard part
// ───────────────────────────────────────────────────────────
// While a refresh is in flight, queue up any other 401s.
// When refresh resolves, replay them all with the new token.

let isRefreshing = false
type QueuedRequest = {
  resolve: (token: string) => void
  reject: (error: unknown) => void
}
let refreshQueue: QueuedRequest[] = []

function processQueue(error: unknown, token: string | null) {
  refreshQueue.forEach(({ resolve, reject }) => {
    if (error || !token) reject(error)
    else resolve(token)
  })
  refreshQueue = []
}

// ───────────────────────────────────────────────────────────
// REFRESH CALL — a raw axios call, NOT using `api`
// ───────────────────────────────────────────────────────────
// We use plain axios so this request skips our interceptors.
// Otherwise a 401 on /refresh itself could trigger another refresh → infinite loop.

async function performRefresh(refreshToken: string): Promise<{
  accessToken: string
  refreshToken: string
}> {
  const response = await axios.post(
    `${import.meta.env.VITE_GATEWAY_URL}/api/auth/refresh`,
    { refreshToken },
    { headers: { "Content-Type": "application/json" }, timeout: 15000 }
  )

  const data = response.data?.data
  if (!data?.token || !data?.refreshToken) {
    throw new Error("Refresh response malformed")
  }

  return { accessToken: data.token, refreshToken: data.refreshToken }
}

// ───────────────────────────────────────────────────────────
// RESPONSE INTERCEPTOR — the actual 401 handler
// ───────────────────────────────────────────────────────────

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined

    // No config or not a 401 → not our problem, reject as usual.
    if (!originalRequest || error.response?.status !== 401) {
      return Promise.reject(error)
    }

    // Never retry a refresh failure. Refresh failing = game over, log out.
    if (isPublicEndpoint(originalRequest.url)) {
      if (originalRequest.url?.includes("/api/auth/refresh")) {
        useAuthStore.getState().clear()
      }
      return Promise.reject(error)
    }

    // Already retried once. Something is very wrong. Log out to break any loop.
    if (originalRequest._retry) {
      useAuthStore.getState().clear()
      return Promise.reject(error)
    }

    // If a refresh is already in flight, queue this request and wait.
    if (isRefreshing) {
      return new Promise<AxiosRequestConfig>((resolve, reject) => {
        refreshQueue.push({
          resolve: (newToken: string) => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            resolve(originalRequest)
          },
          reject: (err) => reject(err),
        })
      }).then((config) => api(config))
    }

    // We're the first 401 during this expiry. Fire the refresh.
    originalRequest._retry = true
    isRefreshing = true
    useAuthStore.getState().setStatus("refreshing")

    const currentRefreshToken = useAuthStore.getState().refreshToken
    if (!currentRefreshToken) {
      // No refresh token means we were never logged in. Dead end.
      useAuthStore.getState().clear()
      processQueue(new Error("No refresh token"), null)
      isRefreshing = false
      return Promise.reject(error)
    }

    try {
      const { accessToken, refreshToken: newRefreshToken } = await performRefresh(currentRefreshToken)

      useAuthStore.getState().setTokens(accessToken, newRefreshToken)
      processQueue(null, accessToken)

      // Retry the original request with the new token.
      originalRequest.headers.Authorization = `Bearer ${accessToken}`
      return api(originalRequest)
    } catch (refreshError) {
      // Refresh failed for real. Clear the store, fail every queued request.
      useAuthStore.getState().clear()
      processQueue(refreshError, null)
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  }
)