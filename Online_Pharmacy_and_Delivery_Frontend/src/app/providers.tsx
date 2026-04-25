import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import { ReactQueryDevtools } from "@tanstack/react-query-devtools"
import { RouterProvider } from "react-router-dom"
import { router } from "./router"
import { useBootAuth } from "@/features/auth/hooks/useBootAuth"

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
})

function AppShell() {
  useBootAuth()
  return <RouterProvider router={router} />
}

export function AppProviders() {
  return (
    <QueryClientProvider client={queryClient}>
      <AppShell />
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  )
}