import { useEffect, useRef } from "react"
import { useAuthStore } from "@/shared/stores/authStore"
import { useFetchCurrentUser } from "@/features/auth/api/useCurrentUser"

/**
 * Boot reconciliation:
 *   1. Zustand's persist middleware rehydrates tokens + user from localStorage.
 *   2. We flip status from "idle" → "authenticated" | "unauthenticated".
 *   3. If authenticated, fire /me in the background to refresh user data
 *      and detect stale tokens early. If /me 401s, the axios interceptor
 *      will refresh or clear. We don't block the UI on this.
 */
export function useBootAuth() {
  const hydrate = useAuthStore((s) => s.hydrate)
  const setUser = useAuthStore((s) => s.setUser)
  const { mutate: fetchMe } = useFetchCurrentUser()
  const hasRun = useRef(false)

  useEffect(() => {
    if (hasRun.current) return
    hasRun.current = true

    hydrate()

    const { accessToken } = useAuthStore.getState()
    if (accessToken) {
      fetchMe(undefined, {
        onSuccess: (user) => setUser(user),
      })
    }
  }, [hydrate, setUser, fetchMe])
}