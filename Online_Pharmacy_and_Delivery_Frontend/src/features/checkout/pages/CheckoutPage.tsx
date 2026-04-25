import { useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { useNavigate, Link } from "react-router-dom"
import { useCart } from "@/features/cart/api/useCart"
import { useAddresses } from "../api/useAddresses"
import { useMyPrescriptions } from "../api/useMyPrescriptions"
import { useCheckout } from "../api/useCheckout"
import { useInitiatePayment } from "../api/useInitiatePayment"
import { checkoutFormSchema, type CheckoutFormValues, type CheckoutFormInput } from "../schemas"

type Step = "address" | "slot" | "prescription" | "payment"

const STEPS_NO_RX: Step[]   = ["address", "slot", "payment"]
const STEPS_WITH_RX: Step[] = ["address", "slot", "prescription", "payment"]

const STEP_LABELS: Record<Step, string> = {
  address: "Address", slot: "Delivery", prescription: "Rx", payment: "Payment",
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
  const navigate = useNavigate()

  const { data: cart }          = useCart()
  const { data: addresses }     = useAddresses()
  const { data: prescriptions } = useMyPrescriptions()
  const checkout                = useCheckout()
  const initiatePayment         = useInitiatePayment()

  const steps = cart?.hasRxItems ? STEPS_WITH_RX : STEPS_NO_RX
  const [stepIdx, setStepIdx] = useState(0)
  const currentStep = steps[stepIdx]

  const { register, watch, setValue, trigger, handleSubmit, formState: { errors } } =
    useForm<CheckoutFormInput, any, CheckoutFormValues>({
      resolver: zodResolver(checkoutFormSchema),
      defaultValues: { addressMode: "existing", deliverySlot: "", paymentMethod: "" },
    })

  const addressMode    = watch("addressMode")
  const selectedAddrId = watch("addressId")
  const selectedSlot   = watch("deliverySlot")
  const selectedRxId   = watch("prescriptionId")
  const selectedMethod = watch("paymentMethod")

  const advance = async () => {
    const fieldMap: Record<Step, (keyof CheckoutFormValues)[]> = {
      address:      addressMode === "existing" ? ["addressId"] : ["newAddress"],
      slot:         ["deliverySlot"],
      prescription: [],
      payment:      ["paymentMethod"],
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
        ...(values.prescriptionId ? { prescriptionId: values.prescriptionId } : {}),
      }
      const order = await checkout.mutateAsync(checkoutPayload)
      if (!order.id) throw new Error("No order ID returned from server")
      await initiatePayment.mutateAsync({ orderId: order.id, method: values.paymentMethod })
      navigate(`/orders/${order.id}`, { state: { fromCheckout: true } })
    } catch {
      // Error displayed via mutation state below
    }
  }

  const isSubmitting           = checkout.isPending || initiatePayment.isPending
  const hasError               = !!(checkout.error || initiatePayment.error)
  const approvedPrescriptions  = prescriptions?.filter((p) => p.status === "APPROVED") ?? []

  return (
    <div className="max-w-xl mx-auto px-4 sm:px-6 py-8">

      {/* Step progress */}
      <nav className="flex items-center gap-1 mb-8 flex-wrap">
        {steps.map((step, idx) => (
          <div key={step} className="flex items-center gap-1">
            <div
              className={`flex items-center justify-center w-8 h-8 rounded-full text-sm font-semibold transition-colors ${
                idx < stepIdx
                  ? "bg-green-600 text-white"
                  : idx === stepIdx
                  ? "border-2 border-green-600 text-green-700"
                  : "border border-slate-200 text-slate-400"
              }`}
            >
              {idx < stepIdx ? "✓" : idx + 1}
            </div>
            <span className={`text-xs font-medium hidden sm:inline mr-1 ${idx === stepIdx ? "text-green-700" : "text-slate-400"}`}>
              {STEP_LABELS[step]}
            </span>
            {idx < steps.length - 1 && (
              <div className={`h-px w-4 ${idx < stepIdx ? "bg-green-400" : "bg-slate-200"}`} />
            )}
          </div>
        ))}
      </nav>

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

        {/* ── Prescription ─────────────────────────────────────────────────── */}
        {currentStep === "prescription" && (
          <div className="space-y-4">
            <h2 className="text-xl font-bold text-slate-800">Link Prescription</h2>
            <p className="text-sm text-slate-500">Your cart has Rx items. Attach an approved prescription.</p>
            {approvedPrescriptions.length === 0 ? (
              <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 text-sm text-amber-700 space-y-1">
                <p className="font-semibold">No approved prescriptions.</p>
                <p>Upload one and wait for pharmacist approval before checking out Rx items.</p>
              </div>
            ) : (
              <div className="space-y-2">
                {approvedPrescriptions.map((rx) => (
                  <div key={rx.id} onClick={() => setValue("prescriptionId", rx.id)}
                    className={`cursor-pointer rounded-xl border p-3 transition-colors ${selectedRxId === rx.id ? "border-green-500 bg-green-50" : "hover:bg-slate-50"}`}
                  >
                    <p className="text-sm font-medium">{rx.fileName ?? `Prescription #${rx.id}`}</p>
                    <p className="text-xs text-slate-500">
                      Approved{rx.uploadedAt ? ` · ${new Date(rx.uploadedAt).toLocaleDateString()}` : ""}
                    </p>
                  </div>
                ))}
              </div>
            )}
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
              className="px-5 py-2.5 border rounded-lg text-sm font-medium hover:bg-slate-50 transition-colors"
            >
              ← Back
            </button>
          ) : (
            <Link to="/cart" className="px-5 py-2.5 border rounded-lg text-sm font-medium hover:bg-slate-50 transition-colors">
              ← Cart
            </Link>
          )}

          {currentStep === "payment" ? (
            <button type="submit" disabled={isSubmitting}
              className="px-6 py-2.5 bg-green-600 text-white font-semibold rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {isSubmitting ? "Processing…" : "Place Order"}
            </button>
          ) : (
            <button type="button" onClick={advance}
              disabled={currentStep === "prescription" && approvedPrescriptions.length === 0}
              className="px-6 py-2.5 bg-green-600 text-white font-semibold rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              Continue →
            </button>
          )}
        </div>
      </form>
    </div>
  )
}
