import { useMutation } from "@tanstack/react-query"
import { useNavigate } from "react-router-dom"
import { api } from "@/shared/api/client"
import { useAuthStore } from "@/shared/stores/authStore"
import { useQueryClient } from "@tanstack/react-query"

export function useLogout() {
  const clear = useAuthStore((s) => s.clear)
  const queryClient = useQueryClient()
  const navigate = useNavigate()

  const handleLogout = () => {
    clear()
    queryClient.clear()
    navigate("/login", { replace: true })
  }

  return useMutation({
    mutationFn: async (): Promise<void> => {
      try {
        await api.post("/api/auth/logout")
      } catch {
        // Server logout failure is non-blocking — always clear client state.
      }
    },
    onSuccess: handleLogout,
    onError: handleLogout,
  })
}