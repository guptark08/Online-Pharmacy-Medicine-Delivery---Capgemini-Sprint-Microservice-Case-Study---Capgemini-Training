export const checkoutKeys = {
  all: ["checkout"] as const,
  addresses: () => [...checkoutKeys.all, "addresses"] as const,
}
