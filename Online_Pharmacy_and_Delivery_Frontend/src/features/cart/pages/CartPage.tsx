import { useRef, useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { useCart } from "../api/useCart"
import { useAddToCart } from "../api/useAddToCart"
import { useUpdateCartItem } from "../api/useUpdateCartItem"
import { useRemoveCartItem } from "../api/useRemoveCartItem"
import { useClearCart } from "../api/useClearCart"
import { useUploadPrescription } from "@/features/prescriptions/api/useUploadPrescription"
import { usePrescriptionById } from "@/features/prescriptions/api/usePrescriptionById"
import type { components as orderComponents } from "@/shared/types/api/order"
import type { components as catalogComponents } from "@/shared/types/api/catalog"

type CartItemResponse        = orderComponents["schemas"]["CartItemResponse"]
type PrescriptionResponseDTO = catalogComponents["schemas"]["PrescriptionResponseDTO"]

const CART_RX_KEY        = "pharmacy_cart_rx_prescription_id"
const DISCARDED_CART_KEY = "pharmacy_discarded_cart"

function readStoredPrescriptionId(): number | null {
  const raw = localStorage.getItem(CART_RX_KEY)
  const n   = raw ? parseInt(raw, 10) : NaN
  return isNaN(n) ? null : n
}

function readDiscardedCart(): CartItemResponse[] {
  try {
    const raw = localStorage.getItem(DISCARDED_CART_KEY)
    return raw ? JSON.parse(raw) : []
  } catch { return [] }
}

// ─── Prescription section ──────────────────────────────────────────────────────

interface PrescriptionSectionProps {
  prescriptionId: number | null
  prescription: PrescriptionResponseDTO | undefined
  isLoading: boolean
  onUploaded: (id: number) => void
  onReupload: () => void
}

function PrescriptionSection({ prescriptionId, prescription, isLoading, onUploaded, onReupload }: PrescriptionSectionProps) {
  const upload       = useUploadPrescription()
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    upload.mutate(file, {
      onSuccess: (rx: PrescriptionResponseDTO) => {
        if (rx.id != null) { localStorage.setItem(CART_RX_KEY, String(rx.id)); onUploaded(rx.id) }
      },
    })
    e.target.value = ""
  }

  const triggerUpload = () => fileInputRef.current?.click()
  const status = prescription?.status

  return (
    <div className="bg-white border rounded-2xl p-5 space-y-4">
      <div className="flex items-center gap-3">
        <div className="w-9 h-9 rounded-xl bg-blue-50 border border-blue-100 flex items-center justify-center shrink-0">
          <svg className="w-4.5 h-4.5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
          </svg>
        </div>
        <div>
          <h2 className="font-bold text-slate-800 text-sm">Prescription Required</h2>
          <p className="text-xs text-slate-500 mt-0.5">Your cart contains Rx medicines. Upload a prescription to proceed.</p>
        </div>
      </div>

      <input ref={fileInputRef} type="file" accept="image/*,.pdf" onChange={handleFileChange} className="hidden" />

      {!prescriptionId && (
        <div className="space-y-2">
          {upload.isError && <p className="text-xs text-red-600">Upload failed. Please try a valid image or PDF (max 10 MB).</p>}
          <button
            onClick={triggerUpload}
            disabled={upload.isPending}
            className="w-full py-3 border-2 border-dashed border-slate-200 rounded-xl text-sm text-slate-500 hover:border-green-400 hover:text-green-600 disabled:opacity-50 transition-colors font-medium"
          >
            {upload.isPending ? "Uploading…" : "+ Upload Prescription (Image or PDF)"}
          </button>
        </div>
      )}

      {prescriptionId && (
        <div>
          {isLoading ? (
            <div className="h-12 bg-slate-100 rounded-xl animate-pulse" />
          ) : (
            <>
              {status === "PENDING" && (
                <div className="flex items-start gap-3 bg-yellow-50 border border-yellow-200 rounded-xl p-3">
                  <svg className="w-4 h-4 text-yellow-600 mt-0.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-bold text-yellow-800">Pending Review</p>
                    <p className="text-xs text-yellow-700 truncate mt-0.5">{prescription?.fileName ?? `Prescription #${prescriptionId}`}</p>
                    <p className="text-xs text-yellow-600 mt-1">You can still place the order now — it will be held until the pharmacist approves.</p>
                  </div>
                  <button onClick={() => { onReupload(); triggerUpload() }} disabled={upload.isPending} className="shrink-0 text-xs text-yellow-700 underline hover:text-yellow-900 disabled:opacity-50">Change</button>
                </div>
              )}
              {status === "APPROVED" && (
                <div className="flex items-start gap-3 bg-green-50 border border-green-200 rounded-xl p-3">
                  <svg className="w-4 h-4 text-green-600 mt-0.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-bold text-green-800">Prescription Approved</p>
                    <p className="text-xs text-green-700 truncate mt-0.5">{prescription?.fileName ?? `Prescription #${prescriptionId}`}</p>
                  </div>
                  <button onClick={() => { onReupload(); triggerUpload() }} disabled={upload.isPending} className="shrink-0 text-xs text-green-700 underline hover:text-green-900 disabled:opacity-50">Change</button>
                </div>
              )}
              {status === "REJECTED" && (
                <>
                  <div className="flex items-start gap-3 bg-red-50 border border-red-200 rounded-xl p-3 mb-2">
                    <svg className="w-4 h-4 text-red-600 mt-0.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M9.75 9.75l4.5 4.5m0-4.5l-4.5 4.5M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <div className="min-w-0">
                      <p className="text-sm font-bold text-red-800">Prescription Rejected</p>
                      <p className="text-xs text-red-700 truncate mt-0.5">{prescription?.fileName ?? `Prescription #${prescriptionId}`}</p>
                      {prescription?.reviewNotes && <p className="text-xs text-red-600 mt-1">Reason: {prescription.reviewNotes}</p>}
                    </div>
                  </div>
                  {upload.isError && <p className="text-xs text-red-600 mb-2">Upload failed. Please try again.</p>}
                  <button onClick={() => { onReupload(); triggerUpload() }} disabled={upload.isPending}
                    className="w-full py-2.5 border rounded-xl text-sm text-slate-600 hover:bg-slate-50 disabled:opacity-50 transition-colors font-medium">
                    {upload.isPending ? "Uploading…" : "Upload New Prescription"}
                  </button>
                </>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}

// ─── Discarded cart ────────────────────────────────────────────────────────────

function DiscardedCartSection() {
  const addToCart = useAddToCart()
  const [discarded, setDiscarded] = useState<CartItemResponse[]>(readDiscardedCart)

  if (discarded.length === 0) return null

  const clearDiscarded = () => { localStorage.removeItem(DISCARDED_CART_KEY); setDiscarded([]) }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 pb-8 space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-bold text-slate-700">Previous Cart</h2>
        <button onClick={clearDiscarded} className="text-xs text-slate-400 hover:text-red-500 transition-colors">Clear saved</button>
      </div>
      <div className="bg-white border rounded-2xl overflow-hidden divide-y divide-slate-100">
        {discarded.map((item, i) => (
          <div key={i} className="flex items-center gap-3 px-4 py-3 opacity-75">
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-slate-700 truncate">{item.medicineName}</p>
              <p className="text-xs text-slate-400">₹{item.unitPrice} · qty {item.quantity}</p>
              {item.requiresPrescription && (
                <span className="text-[10px] bg-amber-100 text-amber-700 px-1.5 py-0.5 rounded-full font-semibold">Rx required</span>
              )}
            </div>
            <button
              onClick={() => { if (item.medicineId && item.quantity) addToCart.mutate({ medicineId: item.medicineId, quantity: item.quantity }) }}
              disabled={addToCart.isPending}
              className="shrink-0 px-3 py-1.5 text-xs bg-green-600 text-white rounded-xl hover:bg-green-700 disabled:opacity-50 transition-colors font-semibold"
            >
              + Add
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}

// ─── Main ─────────────────────────────────────────────────────────────────────

export default function CartPage() {
  const navigate = useNavigate()
  const { data: cart, isLoading } = useCart()
  const update = useUpdateCartItem()
  const remove = useRemoveCartItem()
  const clear  = useClearCart()

  const [prescriptionId, setPrescriptionId] = useState<number | null>(readStoredPrescriptionId)
  const { data: cartPrescription, isLoading: rxLoading } = usePrescriptionById(cart?.hasRxItems ? prescriptionId : null)

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-50">
        <div className="bg-white border-b h-16 animate-pulse" />
        <div className="max-w-3xl mx-auto px-4 py-6 space-y-3">
          {Array.from({ length: 3 }).map((_, i) => <div key={i} className="h-24 bg-white border rounded-2xl animate-pulse" />)}
        </div>
      </div>
    )
  }

  const items    = cart?.items ?? []
  const subtotal = cart?.subtotal ?? 0

  if (items.length === 0) {
    return (
      <div className="min-h-screen bg-slate-50">
        <div className="bg-white border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 py-5">
            <h1 className="text-xl font-extrabold text-slate-800">My Cart</h1>
            <p className="text-xs text-slate-500 mt-0.5">Review and checkout your items</p>
          </div>
        </div>
        <div className="max-w-3xl mx-auto px-4 py-16 text-center space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-white border flex items-center justify-center mx-auto">
            <svg className="w-8 h-8 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 00-3 3h15.75m-12.75-3h11.218c1.121-2.3 2.1-4.684 2.924-7.138a60.114 60.114 0 00-16.536-1.84M7.5 14.25L5.106 5.272M6 20.25a.75.75 0 11-1.5 0 .75.75 0 011.5 0zm12.75 0a.75.75 0 11-1.5 0 .75.75 0 011.5 0z" />
            </svg>
          </div>
          <div>
            <p className="font-bold text-slate-600">Your cart is empty</p>
            <p className="text-sm text-slate-400 mt-1">Add medicines to your cart to get started</p>
          </div>
          <Link to="/catalog" className="inline-flex items-center gap-2 px-5 py-2.5 bg-green-600 hover:bg-green-700 text-white font-semibold rounded-xl text-sm transition-colors shadow-sm">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
            </svg>
            Browse Medicines
          </Link>
        </div>
        <DiscardedCartSection />
      </div>
    )
  }

  // Allow checkout when prescription is PENDING — backend creates a PRESCRIPTION_PENDING
  // order and clears the cart, so the customer is free to place additional orders.
  // Only block when no prescription has been uploaded yet, or it was REJECTED.
  const rxPending    = cart?.hasRxItems && prescriptionId != null && cartPrescription?.status === "PENDING"
  const rxApproved   = !cart?.hasRxItems || (prescriptionId != null && cartPrescription?.status !== "REJECTED")
  const belowMinimum = subtotal < 200
  const canCheckout  = rxApproved && !belowMinimum

  const handleClearAll = () => {
    if (items.length > 0) localStorage.setItem(DISCARDED_CART_KEY, JSON.stringify(items))
    localStorage.removeItem(CART_RX_KEY)
    setPrescriptionId(null)
    clear.mutate()
  }

  const handleProceed = () => {
    navigate("/checkout", {
      state: cart?.hasRxItems && prescriptionId ? { prescriptionId } : undefined,
    })
  }

  return (
    <div className="min-h-screen bg-slate-50">

      {/* ── Page header ──────────────────────────────────────────── */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-5">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-xl font-extrabold text-slate-800">My Cart</h1>
              <p className="text-xs text-slate-500 mt-0.5">{cart?.totalItems} item{(cart?.totalItems ?? 0) !== 1 ? "s" : ""} · ₹{subtotal.toFixed(2)}</p>
            </div>
            <button
              onClick={handleClearAll}
              disabled={clear.isPending}
              className="text-xs font-semibold text-slate-400 hover:text-red-500 transition-colors disabled:opacity-50"
            >
              {clear.isPending ? "Clearing…" : "Clear all"}
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-3xl mx-auto px-4 sm:px-6 py-6 space-y-4">

        {/* Items */}
        <div className="bg-white border rounded-2xl overflow-hidden divide-y divide-slate-100">
          {items.map((item: CartItemResponse) => (
            <CartItem
              key={item.id}
              item={item}
              onUpdate={(qty) => item.id && update.mutate({ itemId: item.id, quantity: qty })}
              onRemove={() => item.id && remove.mutate(item.id)}
            />
          ))}
        </div>

        {/* Prescription */}
        {cart?.hasRxItems && (
          <PrescriptionSection
            prescriptionId={prescriptionId}
            prescription={cartPrescription}
            isLoading={rxLoading}
            onUploaded={(id) => setPrescriptionId(id)}
            onReupload={() => { localStorage.removeItem(CART_RX_KEY); setPrescriptionId(null) }}
          />
        )}

        {/* Order summary */}
        <div className="bg-white border rounded-2xl p-5 space-y-3">
          <h2 className="font-bold text-slate-800">Order Summary</h2>
          <div className="flex justify-between text-sm text-slate-500">
            <span>Subtotal ({cart?.totalItems} item{(cart?.totalItems ?? 0) !== 1 ? "s" : ""})</span>
            <span>₹{subtotal.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-sm text-slate-500">
            <span>Delivery</span>
            <span className="text-green-600 font-semibold">Free</span>
          </div>
          <div className="border-t pt-3 flex justify-between font-extrabold text-slate-800">
            <span>Total</span>
            <span className="text-green-700">₹{subtotal.toFixed(2)}</span>
          </div>

          {belowMinimum && (
            <div className="bg-orange-50 border border-orange-200 rounded-xl p-3 text-xs text-orange-700 font-medium">
              Add ₹{(200 - subtotal).toFixed(2)} more to reach the minimum order of ₹200.
            </div>
          )}

          {rxPending && (
            <div className="bg-blue-50 border border-blue-200 rounded-xl p-3 flex items-start gap-2.5 text-xs text-blue-700">
              <svg className="w-4 h-4 shrink-0 mt-0.5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M11.25 11.25l.041-.02a.75.75 0 011.063.852l-.708 2.836a.75.75 0 001.063.853l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z" />
              </svg>
              <span>
                <span className="font-bold">Your order will be placed and held</span> until the pharmacist approves your prescription. Once approved, your order continues automatically — and your cart will be free for new orders right away.
              </span>
            </div>
          )}

          {cart?.hasRxItems && !rxApproved && (
            <div className="bg-amber-50 border border-amber-200 rounded-xl p-3 text-xs text-amber-700 font-medium">
              {prescriptionId
                ? "Your prescription was rejected. Please upload a new one to proceed."
                : "Upload a prescription above to proceed to checkout."}
            </div>
          )}

          <button
            onClick={handleProceed}
            disabled={!canCheckout}
            className={`w-full py-3 font-bold rounded-xl text-sm transition-colors ${
              canCheckout
                ? "bg-green-600 hover:bg-green-700 text-white shadow-sm"
                : "bg-slate-100 text-slate-400 cursor-not-allowed"
            }`}
          >
            {belowMinimum
              ? `Add ₹${(200 - subtotal).toFixed(0)} more (Min. ₹200)`
              : cart?.hasRxItems && !rxApproved
              ? "Upload Prescription to Continue"
              : rxPending
              ? "Place Order — Awaiting Rx Approval"
              : "Proceed to Checkout →"}
          </button>
        </div>
      </div>
    </div>
  )
}

// ─── Cart item row ─────────────────────────────────────────────────────────────

function CartItem({ item, onUpdate, onRemove }: { item: CartItemResponse; onUpdate: (qty: number) => void; onRemove: () => void }) {
  return (
    <div className="flex items-center gap-4 px-4 py-4">
      <div className="w-10 h-10 rounded-xl bg-green-50 border border-green-100 flex items-center justify-center shrink-0">
        <svg className="w-5 h-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9.75 3.104v5.714a2.25 2.25 0 01-.659 1.591L5 14.5M9.75 3.104c-.251.023-.501.05-.75.082m.75-.082a24.301 24.301 0 014.5 0m0 0v5.714c0 .597.237 1.17.659 1.591L19.8 15.3" />
        </svg>
      </div>

      <div className="flex-1 min-w-0">
        <p className="font-bold text-slate-800 text-sm truncate">{item.medicineName}</p>
        <p className="text-xs text-slate-500 mt-0.5">₹{item.unitPrice} each</p>
        {item.requiresPrescription && (
          <span className="inline-block mt-1 text-[10px] bg-amber-100 text-amber-700 px-2 py-0.5 rounded-full font-semibold">
            Rx required
          </span>
        )}
      </div>

      <div className="flex items-center gap-1.5 shrink-0">
        <button
          onClick={() => item.quantity && item.quantity > 1 && onUpdate(item.quantity - 1)}
          disabled={item.quantity === 1}
          className="w-7 h-7 rounded-lg border flex items-center justify-center text-slate-600 font-bold hover:bg-slate-50 disabled:opacity-30 transition-colors"
        >
          −
        </button>
        <span className="w-6 text-center font-bold text-sm text-slate-800">{item.quantity}</span>
        <button
          onClick={() => item.quantity && item.quantity < 10 && onUpdate(item.quantity + 1)}
          disabled={item.quantity === 10}
          className="w-7 h-7 rounded-lg border flex items-center justify-center text-slate-600 font-bold hover:bg-slate-50 disabled:opacity-30 transition-colors"
        >
          +
        </button>
      </div>

      <span className="w-20 text-right font-bold text-slate-800 text-sm shrink-0">
        ₹{item.lineTotal?.toFixed(2)}
      </span>

      <button
        onClick={onRemove}
        className="shrink-0 w-7 h-7 rounded-lg flex items-center justify-center text-slate-300 hover:text-red-500 hover:bg-red-50 transition-colors ml-1"
        aria-label="Remove"
      >
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
  )
}
