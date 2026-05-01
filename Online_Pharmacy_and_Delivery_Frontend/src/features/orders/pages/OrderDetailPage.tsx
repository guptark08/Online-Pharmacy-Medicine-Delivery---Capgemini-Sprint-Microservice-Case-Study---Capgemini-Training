import { useState } from "react"
import { useParams, Link, useLocation } from "react-router-dom"
import { useOrder } from "../api/useOrder"
import { useCancelOrder } from "../api/useCancelOrder"
import { useReorder } from "../api/useReorder"
import { usePrescriptionById } from "@/features/prescriptions/api/usePrescriptionById"
import type { components } from "@/shared/types/api/order"

type OrderStatus = NonNullable<components["schemas"]["OrderResponse"]["status"]>

const STATUS_META: Partial<Record<OrderStatus, { label: string; color: string; bg: string; icon: string }>> = {
  PAID:               { label: "Payment Confirmed",  color: "text-blue-800",   bg: "bg-blue-50 border-blue-200",    icon: "M2.25 8.25h19.5M2.25 9h19.5m-16.5 5.25h6m-6 2.25h3m-3.75 3h15a2.25 2.25 0 002.25-2.25V6.75A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25v10.5A2.25 2.25 0 004.5 19.5z" },
  PAYMENT_PENDING:    { label: "Payment Pending",    color: "text-yellow-800", bg: "bg-yellow-50 border-yellow-200", icon: "M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" },
  PAYMENT_FAILED:     { label: "Payment Failed",     color: "text-red-800",    bg: "bg-red-50 border-red-200",      icon: "M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" },
  PACKED:             { label: "Being Packed",       color: "text-purple-800", bg: "bg-purple-50 border-purple-200",icon: "M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z" },
  OUT_FOR_DELIVERY:   { label: "Out for Delivery",   color: "text-indigo-800", bg: "bg-indigo-50 border-indigo-200",icon: "M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.129-.504 1.09-1.124a17.902 17.902 0 00-3.213-9.193 2.056 2.056 0 00-1.58-.86H14.25M16.5 18.75h-2.25m0-11.177v-.958c0-.568-.422-1.048-.987-1.106a48.554 48.554 0 00-10.026 0 1.106 1.106 0 00-.987 1.106v7.635m12-6.677v6.677m0 4.5v-4.5m0 0h-12" },
  DELIVERED:          { label: "Delivered",          color: "text-green-800",  bg: "bg-green-50 border-green-200",  icon: "M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" },
  CUSTOMER_CANCELLED: { label: "Cancelled by You",   color: "text-red-800",    bg: "bg-red-50 border-red-200",      icon: "M9.75 9.75l4.5 4.5m0-4.5l-4.5 4.5M21 12a9 9 0 11-18 0 9 9 0 0118 0z" },
  ADMIN_CANCELLED:    { label: "Cancelled",          color: "text-red-800",    bg: "bg-red-50 border-red-200",      icon: "M9.75 9.75l4.5 4.5m0-4.5l-4.5 4.5M21 12a9 9 0 11-18 0 9 9 0 0118 0z" },
  CHECKOUT_STARTED:   { label: "Processing",         color: "text-slate-700",  bg: "bg-slate-50 border-slate-200",  icon: "M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0l3.181 3.183a8.25 8.25 0 0013.803-3.7M4.031 9.865a8.25 8.25 0 0113.803-3.7l3.181 3.182m0-4.991v4.99" },
}

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const location = useLocation()
  const fromCheckout = (location.state as { fromCheckout?: boolean } | null)?.fromCheckout

  const { data: order, isLoading } = useOrder(Number(id))
  const cancelOrder = useCancelOrder()
  const reorder     = useReorder()
  const [showCancelConfirm, setShowCancelConfirm] = useState(false)

  const prescriptionId = order?.prescriptionId
  const { data: prescription } = usePrescriptionById(prescriptionId ?? null)

  const isCancellable = order?.status != null &&
    !["DELIVERED", "CUSTOMER_CANCELLED", "ADMIN_CANCELLED", "OUT_FOR_DELIVERY", "PACKED"].includes(order.status)
  const isDelivered = order?.status === "DELIVERED"

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-50">
        <div className="bg-white border-b h-16 animate-pulse" />
        <div className="max-w-2xl mx-auto px-4 py-6 space-y-4">
          <div className="h-32 bg-white border rounded-2xl animate-pulse" />
          <div className="h-48 bg-white border rounded-2xl animate-pulse" />
        </div>
      </div>
    )
  }

  if (!order) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="text-center space-y-3">
          <div className="w-16 h-16 rounded-2xl bg-white border flex items-center justify-center mx-auto">
            <svg className="w-8 h-8 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5" />
            </svg>
          </div>
          <p className="font-semibold text-slate-600">Order not found</p>
          <Link to="/orders" className="text-sm text-green-600 hover:text-green-700 font-medium">← Back to Orders</Link>
        </div>
      </div>
    )
  }

  const meta = STATUS_META[order.status as OrderStatus] ?? {
    label: order.status ?? "Unknown",
    color: "text-slate-700",
    bg:    "bg-slate-50 border-slate-200",
    icon:  "M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 002.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 00-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25zM6.75 12h.008v.008H6.75V12zm0 3h.008v.008H6.75V15zm0 3h.008v.008H6.75V18z",
  }

  return (
    <div className="min-h-screen bg-slate-50">

      {/* ── Page header ──────────────────────────────────────────── */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-5">
          <div className="flex items-center gap-3">
            <Link to="/orders" className="text-slate-400 hover:text-slate-600 transition-colors">
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
              </svg>
            </Link>
            <div>
              <h1 className="text-xl font-extrabold text-slate-800">Order #{order.id}</h1>
              <p className="text-xs text-slate-500 mt-0.5">
                {order.createdAt
                  ? `Placed on ${new Date(order.createdAt).toLocaleDateString("en-IN", { day: "numeric", month: "long", year: "numeric" })}`
                  : "Order details"}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-2xl mx-auto px-4 sm:px-6 py-6 space-y-4">

        {/* Success banner */}
        {fromCheckout && (
          <div className="bg-green-50 border border-green-200 rounded-2xl p-5 flex items-start gap-4">
            <div className="w-10 h-10 rounded-xl bg-green-100 flex items-center justify-center shrink-0">
              <svg className="w-5 h-5 text-green-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div>
              <p className="font-bold text-green-800">Order placed successfully!</p>
              <p className="text-sm text-green-600 mt-0.5">We'll notify you as your order progresses.</p>
            </div>
          </div>
        )}

        {/* Status card */}
        <div className={`border rounded-2xl p-5 ${meta.bg}`}>
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-white/70 flex items-center justify-center shrink-0">
              <svg className={`w-5 h-5 ${meta.color}`} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d={meta.icon} />
              </svg>
            </div>
            <div>
              <p className={`font-extrabold text-lg leading-none ${meta.color}`}>{meta.label}</p>
              <p className="text-xs text-slate-500 mt-1">Order #{order.id}</p>
            </div>
          </div>

          {(order.deliveryAddress || order.deliverySlot) && (
            <div className="mt-4 pt-4 border-t border-white/50 space-y-1.5">
              {order.deliveryAddress && (
                <div className="flex items-center gap-2 text-sm text-slate-600">
                  <svg className="w-4 h-4 text-slate-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15 10.5a3 3 0 11-6 0 3 3 0 016 0z" /><path strokeLinecap="round" strokeLinejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1115 0z" />
                  </svg>
                  {order.deliveryAddress}
                </div>
              )}
              {order.deliverySlot && (
                <div className="flex items-center gap-2 text-sm text-slate-600">
                  <svg className="w-4 h-4 text-slate-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  {order.deliverySlot}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Order items */}
        <div className="bg-white border rounded-2xl p-5 space-y-4">
          <h2 className="font-bold text-slate-800">Items</h2>
          {(order.items?.length ?? 0) > 0 ? (
            <div className="divide-y divide-slate-100">
              {order.items?.map((item, i) => (
                <div key={i} className="flex justify-between items-center py-3 first:pt-0 last:pb-0 text-sm">
                  <div>
                    <p className="font-semibold text-slate-800">{item.medicineName}</p>
                    <p className="text-xs text-slate-400 mt-0.5">Qty: {item.quantity}</p>
                  </div>
                  <span className="font-bold text-slate-800">₹{(item.totalPrice ?? item.subtotal)?.toFixed(2)}</span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-slate-400">No items recorded</p>
          )}

          <div className="border-t pt-4 flex justify-between font-extrabold text-slate-800">
            <span>Total</span>
            <span className="text-green-700">₹{(order.finalAmount ?? order.totalAmount ?? 0).toFixed(2)}</span>
          </div>

          {/* Prescription */}
          {prescriptionId && (
            <div className="border-t pt-4">
              <p className="text-xs font-bold text-slate-500 mb-2 uppercase tracking-wider">Prescription</p>
              {prescription ? (
                <div className={`flex items-center gap-2 rounded-xl px-3 py-2.5 text-xs border ${
                  prescription.status === "APPROVED" ? "bg-green-50 text-green-800 border-green-200" :
                  prescription.status === "REJECTED" ? "bg-red-50 text-red-800 border-red-200" :
                  "bg-yellow-50 text-yellow-800 border-yellow-200"
                }`}>
                  <svg className="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                  </svg>
                  <span className="truncate">{prescription.fileName ?? `Prescription #${prescriptionId}`}</span>
                  <span className="ml-auto font-bold shrink-0">{prescription.status}</span>
                </div>
              ) : (
                <div className="flex items-center gap-2 bg-slate-50 border rounded-xl px-3 py-2.5 text-xs text-slate-500">
                  <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 002.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 00-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25zM6.75 12h.008v.008H6.75V12zm0 3h.008v.008H6.75V15zm0 3h.008v.008H6.75V18z" />
                  </svg>
                  Prescription #{prescriptionId}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Cancel confirmation */}
        {showCancelConfirm && (
          <div className="bg-red-50 border border-red-200 rounded-2xl p-5 space-y-3">
            <p className="font-bold text-red-800">Cancel this order?</p>
            <p className="text-sm text-red-600">This action cannot be undone.</p>
            <div className="flex gap-2">
              <button
                onClick={() => {
                  if (order?.id != null) {
                    cancelOrder.mutate(
                      { id: order.id },
                      { onSuccess: () => setShowCancelConfirm(false) }
                    )
                  }
                }}
                disabled={cancelOrder.isPending}
                className="px-4 py-2 bg-red-600 text-white text-sm font-semibold rounded-xl hover:bg-red-700 disabled:opacity-50 transition-colors"
              >
                {cancelOrder.isPending ? "Cancelling…" : "Yes, Cancel"}
              </button>
              <button
                onClick={() => setShowCancelConfirm(false)}
                className="px-4 py-2 border text-slate-700 text-sm rounded-xl hover:bg-slate-50 transition-colors"
              >
                Keep Order
              </button>
            </div>
          </div>
        )}

        {/* Actions */}
        <div className="flex flex-wrap gap-3">
          <Link
            to="/orders"
            className="flex-1 min-w-[120px] text-center py-2.5 border rounded-xl text-sm font-semibold text-slate-700 hover:bg-slate-50 transition-colors"
          >
            All Orders
          </Link>
          {isDelivered && order?.id != null && (
            <button
              onClick={() => reorder.mutate(order.id!)}
              disabled={reorder.isPending}
              className="flex-1 min-w-[120px] py-2.5 border border-green-500 text-green-700 rounded-xl text-sm font-semibold hover:bg-green-50 disabled:opacity-50 transition-colors"
            >
              {reorder.isPending ? "Adding to cart…" : "Reorder"}
            </button>
          )}
          {isCancellable && !showCancelConfirm && (
            <button
              onClick={() => setShowCancelConfirm(true)}
              className="flex-1 min-w-[120px] py-2.5 bg-red-50 text-red-700 border border-red-200 rounded-xl text-sm font-semibold hover:bg-red-100 transition-colors"
            >
              Cancel Order
            </button>
          )}
          <Link
            to="/catalog"
            className="flex-1 min-w-[120px] text-center py-2.5 bg-green-600 hover:bg-green-700 text-white rounded-xl text-sm font-semibold transition-colors"
          >
            Continue Shopping
          </Link>
        </div>
      </div>
    </div>
  )
}
