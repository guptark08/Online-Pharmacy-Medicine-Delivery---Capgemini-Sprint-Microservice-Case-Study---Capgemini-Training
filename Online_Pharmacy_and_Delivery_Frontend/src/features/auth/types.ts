// Auth store shape — the runtime model of the logged-in user.
// Fields here match the backend's AuthResponse that comes back from /verify-login-otp.

export type Role = "CUSTOMER" | "ADMIN"

export interface AuthUser {
  userId: number
  username: string
  email: string
  role: Role
}

export type AuthStatus =
  | "idle"            // initial boot, before we've checked localStorage
  | "authenticated"   // tokens valid, user loaded
  | "unauthenticated" // no tokens, or tokens cleared
  | "refreshing"      // actively exchanging refresh token

// Login flow state — drives the two-step login page
export type LoginStep =
  | { phase: "password" }
  | { phase: "otp"; email: string; maskedEmail: string }