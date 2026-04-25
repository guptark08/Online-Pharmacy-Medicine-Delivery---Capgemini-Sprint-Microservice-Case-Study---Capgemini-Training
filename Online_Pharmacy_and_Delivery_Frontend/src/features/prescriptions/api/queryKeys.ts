export const prescriptionKeys = {
  all:    ["prescriptions"] as const,
  myList: () => ["prescriptions", "my"] as const,
}
