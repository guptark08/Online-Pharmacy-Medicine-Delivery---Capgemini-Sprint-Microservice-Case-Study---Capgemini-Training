// The cart is a single server-side resource per user.
// One key is enough — no list / detail split needed.
export const cartKeys = {
  all: ["cart"] as const,
  detail: () => [...cartKeys.all, "detail"] as const,
}
