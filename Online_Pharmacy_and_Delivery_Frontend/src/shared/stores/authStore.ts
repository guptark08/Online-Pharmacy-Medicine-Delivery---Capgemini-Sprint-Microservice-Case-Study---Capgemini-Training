import { create } from "zustand"
import { persist, createJSONStorage } from "zustand/middleware"
import type { AuthUser, AuthStatus, Role } from "@/features/auth/types"

interface AuthResponsePayload {
  token?: string
  refreshToken?: string
  userId?: number
  name?: string
  username?: string
  email?: string
  role?: string
}

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  user: AuthUser | null
  status: AuthStatus

  setSession: (response: AuthResponsePayload) => void
  setTokens: (accessToken: string, refreshToken: string) => void
  setUser: (user: AuthUser) => void
  setStatus: (status: AuthStatus) => void
  hydrate: () => void
  clear: () => void
}

function normalizeRole(role: string | undefined): Role {
  if (!role) return "CUSTOMER"
  const cleaned = role.trim().toUpperCase().replace(/^ROLE_/, "")
  return cleaned === "ADMIN" ? "ADMIN" : "CUSTOMER"
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      status: "idle",

      setSession: (response) => {
        if (!response.token || !response.refreshToken || !response.userId) {
          console.error("setSession called with incomplete AuthResponse", response)
          return
        }
        set({
          accessToken: response.token,
          refreshToken: response.refreshToken,
          user: {
            userId: response.userId,
            name: response.name ?? "",
            username: response.username ?? "",
            email: response.email ?? "",
            role: normalizeRole(response.role),
          },
          status: "authenticated",
        })
      },

      setTokens: (accessToken, refreshToken) =>
        set({ accessToken, refreshToken, status: "authenticated" }),

      setUser: (user) => set({ user }),

      setStatus: (status) => set({ status }),

      // Flip out of "idle" based on what rehydrated from localStorage.
      // Called once on app boot.
      hydrate: () => {
        const { accessToken, user } = get()
        set({
          status: accessToken && user ? "authenticated" : "unauthenticated",
        })
      },

      clear: () =>
        set({
          accessToken: null,
          refreshToken: null,
          user: null,
          status: "unauthenticated",
        }),
    }),
    {
      name: "pharmacy-auth",
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
      }),
    }
  )
)