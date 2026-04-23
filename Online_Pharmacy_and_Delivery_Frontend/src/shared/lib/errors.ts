import { AxiosError } from "axios"

/**
 * Extract a user-friendly error message from any error.
 * Priority:
 *   1. Backend-provided message from ApiResponse.message (e.g., "Email already in use")
 *   2. Axios's own message (e.g., "Network Error")
 *   3. Generic fallback
 */
export function getErrorMessage(error: unknown): string {
  if (error instanceof AxiosError) {
    const backendMessage = error.response?.data?.message
    if (typeof backendMessage === "string" && backendMessage.trim().length > 0) {
      return backendMessage
    }
    if (error.message) return error.message
  }

  if (error instanceof Error) return error.message
  return "Something went wrong. Please try again."
}