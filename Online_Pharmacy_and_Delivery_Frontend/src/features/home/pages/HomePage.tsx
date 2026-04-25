
import { useLogout } from "@/features/auth/api/useLogout"
import { useAuthStore } from "@/shared/stores/authStore"
import { Button } from "@/shared/ui/button"

export default function HomePage() {
  // Pull the user from the global auth stat
  const user = useAuthStore((s) => s.user)
  
  // Grab the logout mutation
  const { mutate: logout, isPending } = useLogout()

  return (
    <div className="flex min-h-screen flex-col items-center justify-center p-4">
      <div className="w-full max-w-sm space-y-6 text-center">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">
            Hi, {user?.username}
          </h1>
          <p className="text-gray-500">
            Role: <span className="font-medium text-foreground">{user?.role}</span>
          </p>
        </div>

        <Button 
          variant="secondary" 
          className="w-full" 
          onClick={() => logout()} 
          disabled={isPending}
        >
          {isPending ? "Logging out..." : "Log out"}
        </Button>
      </div>
    </div>
  )
}