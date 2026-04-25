import { z } from "zod"

// Zod 4 — coerce handles the "HTML inputs always return strings" problem cleanly
export const newAddressSchema = z.object({
  streetAddress: z.string().min(5, "Street address is too short"),
  city:          z.string().min(2, "City is required"),
  state:         z.string().min(2, "State is required"),
  pincode:       z.number().int().min(100000, "Must be 6 digits").max(999999, "Must be 6 digits"),
  isDefault:     z.boolean().default(false),
})

export const checkoutFormSchema = z
  .object({
    // "existing" = pick from saved addresses, "new" = fill in inline
    addressMode:    z.enum(["existing", "new"]),
    addressId:      z.number().optional(),
    newAddress:     newAddressSchema.optional(),
    deliverySlot:   z.string().min(1, "Select a delivery slot"),
    // Only required when cart.hasRxItems — the form step is skipped otherwise
    prescriptionId: z.number().optional(),
    paymentMethod:  z.string().min(1, "Select a payment method"),
  })
  .superRefine((data, ctx) => {
    if (data.addressMode === "existing" && !data.addressId) {
      ctx.addIssue({
        code: "custom",
        path: ["addressId"],
        message: "Select a delivery address",
      })
    }
    if (data.addressMode === "new" && !data.newAddress) {
      ctx.addIssue({
        code: "custom",
        path: ["newAddress"],
        message: "Fill in your address",
      })
    }
  })

export type CheckoutFormValues = z.infer<typeof checkoutFormSchema>
export type CheckoutFormInput = z.input<typeof checkoutFormSchema>

