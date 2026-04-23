import { create } from "zustand"
import { persist, createJSONStorage } from "zustand/middleware"
import type { AuthUser, AuthStatus, Role } from "@/features/auth/types"

// Shape of the backend's AuthResponse — what /verify-login-otp returns
interface AuthResponsePayload {
  token?: string
  refreshToken?: string
  userId?: number
  username?: string
  email?: string
  role?: string
}

// What the store exposes to components
interface AuthState {
  // state
  accessToken: string | null
  refreshToken: string | null
  user: AuthUser | null
  status: AuthStatus

  // actions
  setSession: (response: AuthResponsePayload) => void
  setTokens: (accessToken: string, refreshToken: string) => void
  setStatus: (status: AuthStatus) => void
  clear: () => void
}

// Role normalization — backend sometimes returns "ROLE_ADMIN", we want "ADMIN"
function normalizeRole(role: string | undefined): Role {
  if (!role) return "CUSTOMER"
  const cleaned = role.trim().toUpperCase().replace(/^ROLE_/, "")
  return cleaned === "ADMIN" ? "ADMIN" : "CUSTOMER"
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      // initial state
      accessToken: null,
      refreshToken: null,
      user: null,
      status: "idle",

      // actions
      setSession: (response) => {
        if (!response.token || !response.refreshToken || !response.userId) {
          // Defensive: if backend sends a malformed response, don't corrupt the store
          console.error("setSession called with incomplete AuthResponse", response)
          return
        }

        set({
          accessToken: response.token,
          refreshToken: response.refreshToken,
          user: {
            userId: response.userId,
            username: response.username ?? "",
            email: response.email ?? "",
            role: normalizeRole(response.role),
          },
          status: "authenticated",
        })
      },

      setTokens: (accessToken, refreshToken) =>
        set({ accessToken, refreshToken, status: "authenticated" }),

      setStatus: (status) => set({ status }),

      clear: () =>
        set({
          accessToken: null,
          refreshToken: null,
          user: null,
          status: "unauthenticated",
        }),
    }),
    {
      name: "pharmacy-auth",             // localStorage key
      storage: createJSONStorage(() => localStorage),
      // Only persist tokens + user. Status should always start as "idle" on reload,
      // then the app reconciles on boot (we'll wire that up later).
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
      }),
    }
  )
)