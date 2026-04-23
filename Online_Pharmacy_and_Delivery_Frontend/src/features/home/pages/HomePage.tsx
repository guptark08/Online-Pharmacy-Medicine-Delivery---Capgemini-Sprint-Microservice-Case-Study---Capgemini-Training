import { useAuthStore } from "@/shared/stores/authStore"

export default function HomePage() {
  const { user, status, clear } = useAuthStore()
  const setSession = useAuthStore((s) => s.setSession)

  return (
    <div className="p-8 space-y-4">
      <h1 className="text-3xl font-bold">Pharmacy</h1>
      <div>Status: {status}</div>
      <div>User: {user ? user.username : "(none)"}</div>

      <button
        className="border px-4 py-2"
        onClick={() => setSession({
          token: "fake-jwt",
          refreshToken: "fake-refresh",
          userId: 1,
          username: "alice",
          email: "alice@test.com",
          role: "CUSTOMER",
        })}
      >
        Fake Login
      </button>

      <button className="border px-4 py-2" onClick={clear}>
        Logout
      </button>
    </div>
  )
}