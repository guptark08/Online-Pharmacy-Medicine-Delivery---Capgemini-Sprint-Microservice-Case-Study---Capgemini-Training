import { useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { useNavigate, Link, useLocation } from "react-router-dom"
import { useCart } from "@/features/cart/api/useCart"
import { useAddresses } from "../api/useAddresses"
import { useCheckout } from "../api/useCheckout"
import { useInitiatePayment } from "../api/useInitiatePayment"
import { checkoutFormSchema, type CheckoutFormValues, type CheckoutFormInput } from "../schemas"

const CART_RX_KEY = "pharmacy_cart_rx_prescription_id"

type Step = "address" | "slot" | "payment"

const STEPS: Step[] = ["address", "slot", "payment"]

const STEP_LABELS: Record<Step, string> = {
  address: "Address", slot: "Delivery", payment: "Payment",
}

const DELIVERY_SLOTS = [
  { label: "Morning (8AM – 12PM)",   value: "MORNING" },
  { label: "Afternoon (12PM – 4PM)", value: "AFTERNOON" },
  { label: "Evening (4PM – 8PM)",    value: "EVENING" },
]

const PAYMENT_METHODS = [
  { label: "UPI",              value: "UPI",        icon: "📱" },
  { label: "Card",             value: "CARD",       icon: "💳" },
  { label: "Net Banking",      value: "NETBANKING", icon: "🏦" },
  { label: "Cash on Delivery", value: "COD",        icon: "💵" },
]

const INPUT_CLS =
  "w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-green-500 bg-white"

export default function CheckoutPage() {
  const navigate  = useNavigate()
  const location  = useLocation()

  // Prescription ID comes from cart page (passed via navigation state) or localStorage
  const prescriptionId: number | null =
    (location.state as { prescriptionId?: number } | null)?.prescriptionId ??
    (() => {
      const raw = localStorage.getItem(CART_RX_KEY)
      const parsed = raw ? parseInt(raw, 10) : NaN
      return isNaN(parsed) ? null : parsed
    })()

  const { data: cart }      = useCart()
  const { data: addresses } = useAddresses()
  const checkout            = useCheckout()
  const initiatePayment     = useInitiatePayment()

  const [stepIdx, setStepIdx] = useState(0)
  const currentStep = STEPS[stepIdx]

  const { register, watch, setValue, trigger, handleSubmit, formState: { errors } } =
    useForm<CheckoutFormInput, any, CheckoutFormValues>({
      resolver: zodResolver(checkoutFormSchema),
      defaultValues: { addressMode: "existing", deliverySlot: "", paymentMethod: "" },
    })

  const addressMode    = watch("addressMode")
  const selectedAddrId = watch("addressId")
  const selectedSlot   = watch("deliverySlot")
  const selectedMethod = watch("paymentMethod")

  const advance = async () => {
    const fieldMap: Record<Step, (keyof CheckoutFormValues)[]> = {
      address: addressMode === "existing" ? ["addressId"] : ["newAddress"],
      slot:    ["deliverySlot"],
      payment: ["paymentMethod"],
    }
    const valid = await trigger(fieldMap[currentStep])
    if (valid) setStepIdx((i) => i + 1)
  }

  const onSubmit = async (values: CheckoutFormValues) => {
    try {
      const checkoutPayload = {
        deliverySlot: values.deliverySlot,
        ...(values.addressMode === "existing"
          ? { addressId: values.addressId }
          : { newAddress: values.newAddress }),
        ...(prescriptionId != null ? { prescriptionId } : {}),
      }
      const order = await checkout.mutateAsync(checkoutPayload)
      if (!order.id) throw new Error("No order ID returned from server")
      await initiatePayment.mutateAsync({ orderId: order.id, method: values.paymentMethod })
      // Clear the cart prescription after successful order
      localStorage.removeItem(CART_RX_KEY)
      navigate(`/orders/${order.id}`, { state: { fromCheckout: true } })
    } catch (_err) {
      // Errors surfaced via checkout.isError / initiatePayment.isError below
    }
  }

  const isSubmitting = checkout.isPending || initiatePayment.isPending
  const hasError     = !!(checkout.error || initiatePayment.error)

  return (
    <div className="min-h-screen bg-slate-50">

      {/* Page header */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-5">
          <div className="flex items-center gap-3">
            <Link to="/cart" className="text-slate-400 hover:text-slate-600 transition-colors">
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
              </svg>
            </Link>
            <div>
              <h1 className="text-xl font-extrabold text-slate-800">Checkout</h1>
              <p className="text-xs text-slate-500 mt-0.5">Complete your order in a few steps</p>
            </div>
          </div>
        </div>
      </div>

    <div className="max-w-xl mx-auto px-4 sm:px-6 py-6">

      {/* Step progress */}
      <nav className="flex items-center gap-1 mb-8">
        {STEPS.map((step, idx) => (
          <div key={step} className="flex items-center gap-1 flex-1">
            <div className="flex items-center gap-2 flex-1">
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-extrabold transition-colors shrink-0 ${
                  idx < stepIdx
                    ? "bg-green-600 text-white"
                    : idx === stepIdx
                    ? "bg-green-50 border-2 border-green-600 text-green-700"
                    : "bg-white border-2 border-slate-200 text-slate-400"
                }`}
              >
                {idx < stepIdx
                  ? <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}><path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" /></svg>
                  : idx + 1}
              </div>
              <span className={`text-xs font-semibold hidden sm:inline ${idx === stepIdx ? "text-green-700" : idx < stepIdx ? "text-slate-600" : "text-slate-400"}`}>
                {STEP_LABELS[step]}
              </span>
            </div>
            {idx < STEPS.length - 1 && (
              <div className={`h-0.5 flex-1 mx-1 rounded-full ${idx < stepIdx ? "bg-green-400" : "bg-slate-200"}`} />
            )}
          </div>
        ))}
      </nav>

      {/* Prescription banner (informational — already approved by this point) */}
      {cart?.hasRxItems && prescriptionId && (
        <div className="mb-6 bg-green-50 border border-green-200 rounded-xl p-3 flex items-center gap-2 text-xs text-green-700">
          <span>✅</span>
          <span>Approved prescription linked to this order (Rx #{prescriptionId})</span>
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">

        {/* ── Address ─────────────────────────────────────────────────────── */}
        {currentStep === "address" && (
          <div className="space-y-4">
            <h2 className="text-xl font-bold text-slate-800">Delivery Address</h2>
            <div className="flex gap-2">
              {(["existing", "new"] as const).map((mode) => (
                <button key={mode} type="button"
                  onClick={() => setValue("addressMode", mode)}
                  className={`flex-1 py-2 rounded-lg border text-sm font-medium transition-colors ${
                    addressMode === mode ? "bg-green-600 text-white border-green-600" : "hover:bg-slate-50 text-slate-700"
                  }`}
                >
                  {mode === "existing" ? "Saved address" : "New address"}
                </button>
              ))}
            </div>

            {addressMode === "existing" && (
              <div className="space-y-2">
                {(addresses?.length ?? 0) === 0 && (
                  <p className="text-sm text-slate-500">No saved addresses. Switch to "New address".</p>
                )}
                {addresses?.map((addr) => (
                  <div key={addr.id} onClick={() => setValue("addressId", addr.id)}
                    className={`cursor-pointer rounded-xl border p-3 transition-colors ${selectedAddrId === addr.id ? "border-green-500 bg-green-50" : "hover:bg-slate-50"}`}
                  >
                    <p className="text-sm font-medium">{addr.streetAddress}</p>
                    <p className="text-xs text-slate-500">{addr.city}, {addr.state} – {addr.pincode}</p>
                  </div>
                ))}
                {errors.addressId && <p className="text-xs text-red-600">{errors.addressId.message}</p>}
              </div>
            )}

            {addressMode === "new" && (
              <div className="space-y-3">
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">Street address</label>
                  <input {...register("newAddress.streetAddress")} placeholder="123 Main Street" className={INPUT_CLS} />
                  {errors.newAddress?.streetAddress && <p className="mt-1 text-xs text-red-600">{errors.newAddress.streetAddress.message}</p>}
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-slate-600 mb-1">City</label>
                    <input {...register("newAddress.city")} placeholder="Mumbai" className={INPUT_CLS} />
                    {errors.newAddress?.city && <p className="mt-1 text-xs text-red-600">{errors.newAddress.city.message}</p>}
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-slate-600 mb-1">State</label>
                    <input {...register("newAddress.state")} placeholder="Maharashtra" className={INPUT_CLS} />
                    {errors.newAddress?.state && <p className="mt-1 text-xs text-red-600">{errors.newAddress.state.message}</p>}
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">Pincode</label>
                  <input {...register("newAddress.pincode", { valueAsNumber: true })} placeholder="400001" inputMode="numeric" maxLength={6} className={INPUT_CLS} />
                  {errors.newAddress?.pincode && <p className="mt-1 text-xs text-red-600">{errors.newAddress.pincode.message}</p>}
                </div>
              </div>
            )}
          </div>
        )}

        {/* ── Delivery slot ────────────────────────────────────────────────── */}
        {currentStep === "slot" && (
          <div className="space-y-4">
            <h2 className="text-xl font-bold text-slate-800">Delivery Slot</h2>
            <div className="space-y-2">
              {DELIVERY_SLOTS.map((slot) => (
                <div key={slot.value} onClick={() => setValue("deliverySlot", slot.value)}
                  className={`cursor-pointer rounded-xl border p-3.5 transition-colors ${selectedSlot === slot.value ? "border-green-500 bg-green-50" : "hover:bg-slate-50"}`}
                >
                  <p className="text-sm font-medium">{slot.label}</p>
                </div>
              ))}
              {errors.deliverySlot && <p className="text-xs text-red-600">{errors.deliverySlot.message}</p>}
            </div>
          </div>
        )}

        {/* ── Payment ──────────────────────────────────────────────────────── */}
        {currentStep === "payment" && (
          <div className="space-y-4">
            <h2 className="text-xl font-bold text-slate-800">Payment</h2>
            <div className="space-y-2">
              {PAYMENT_METHODS.map((method) => (
                <div key={method.value} onClick={() => setValue("paymentMethod", method.value)}
                  className={`cursor-pointer rounded-xl border p-3.5 flex items-center gap-3 transition-colors ${selectedMethod === method.value ? "border-green-500 bg-green-50" : "hover:bg-slate-50"}`}
                >
                  <span className="text-xl">{method.icon}</span>
                  <span className="text-sm font-medium">{method.label}</span>
                </div>
              ))}
              {errors.paymentMethod && <p className="text-xs text-red-600">{errors.paymentMethod.message}</p>}
            </div>

            <div className="bg-slate-50 border rounded-xl p-4 text-sm space-y-1">
              <p className="font-semibold text-slate-700">Summary</p>
              <div className="flex justify-between text-slate-500">
                <span>{cart?.totalItems} item{(cart?.totalItems ?? 0) !== 1 ? "s" : ""}</span>
                <span className="font-semibold text-slate-800">₹{cart?.subtotal?.toFixed(2)}</span>
              </div>
            </div>

            {hasError && (
              <div className="bg-red-50 border border-red-200 rounded-xl p-3 text-sm text-red-700">
                Something went wrong. Please try again.
              </div>
            )}
          </div>
        )}

        {/* Navigation */}
        <div className="flex justify-between pt-2">
          {stepIdx > 0 ? (
            <button type="button" onClick={() => setStepIdx((i) => i - 1)}
              className="px-5 py-2.5 border rounded-xl text-sm font-semibold text-slate-700 hover:bg-slate-50 transition-colors"
            >
              ← Back
            </button>
          ) : (
            <Link to="/cart" className="px-5 py-2.5 border rounded-xl text-sm font-semibold text-slate-700 hover:bg-slate-50 transition-colors">
              ← Cart
            </Link>
          )}

          {currentStep === "payment" ? (
            <button type="submit" disabled={isSubmitting}
              className="px-6 py-2.5 bg-green-600 text-white font-bold rounded-xl hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-sm"
            >
              {isSubmitting ? "Processing…" : "Place Order"}
            </button>
          ) : (
            <button type="button" onClick={advance}
              className="px-6 py-2.5 bg-green-600 text-white font-bold rounded-xl hover:bg-green-700 transition-colors shadow-sm"
            >
              Continue →
            </button>
          )}
        </div>
      </form>
    </div>
    </div>
  )
}
