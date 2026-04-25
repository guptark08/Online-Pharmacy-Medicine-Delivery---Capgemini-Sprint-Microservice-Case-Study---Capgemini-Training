export const adminKeys = {
  all: ["admin"] as const,
  dashboard: () => ["admin", "dashboard"] as const,
  orders: {
    all: () => ["admin", "orders"] as const,
    list: () => ["admin", "orders", "list"] as const,
    detail: (id: number) => ["admin", "orders", id] as const,
  },
  prescriptions: {
    all: () => ["admin", "prescriptions"] as const,
    list: () => ["admin", "prescriptions", "list"] as const,
    pending: () => ["admin", "prescriptions", "pending"] as const,
  },
  medicines: {
    all: () => ["admin", "medicines"] as const,
    list: () => ["admin", "medicines", "list"] as const,
  },
  categories: () => ["admin", "categories"] as const,
  reports: {
    sales: (params: { startDate: string; endDate: string }) =>
      ["admin", "reports", "sales", params] as const,
    inventory: () => ["admin", "reports", "inventory"] as const,
  },
}
