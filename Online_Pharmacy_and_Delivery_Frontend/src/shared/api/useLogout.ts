import { useMutation } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { useAuthStore } from "@/shared/stores/authStore"
import { useQueryClient } from "@tanstack/react-query"

export function useLogout() {
  const clear = useAuthStore((s) => s.clear)
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (): Promise<void> => {
      try {
        await api.post("/api/auth/logout")
      } catch {
        // If logout fails on the server (network, 401, etc.), we still want to
        // clear the client state. A failed server logout is not a blocker.
      }
    },
    onSuccess: () => {
      clear()
      queryClient.clear()
    },
    onError: () => {
      clear()
      queryClient.clear()
    },
  })
}