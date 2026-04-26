import { useState } from "react"
import { useParams, Link } from "react-router-dom"
import { useAdminOrderDetail } from "../api/useAdminOrderDetail"
import { useUpdateOrderStatus } from "../api/useUpdateOrderStatus"
import { useCancelAdminOrder } from "../api/useCancelAdminOrder"

const STATUS_COLOR: Record<string, string> = {
  CHECKOUT_STARTED:      "bg-slate-100 text-slate-700",
  PRESCRIPTION_PENDING:  "bg-amber-100 text-amber-800",
  PRESCRIPTION_APPROVED: "bg-teal-100 text-teal-800",
  PRESCRIPTION_REJECTED: "bg-rose-100 text-rose-800",
  PAYMENT_PENDING:       "bg-yellow-100 text-yellow-800",
  PAYMENT_FAILED:        "bg-red-100 text-red-800",
  PAID:                  "bg-blue-100 text-blue-800",
  PACKED:                "bg-purple-100 text-purple-800",
  OUT_FOR_DELIVERY:      "bg-indigo-100 text-indigo-800",
  DELIVERED:             "bg-green-100 text-green-800",
  CUSTOMER_CANCELLED:    "bg-red-100 text-red-800",
  ADMIN_CANCELLED:       "bg-red-100 text-red-800",
  RETURN_REQUESTED:      "bg-orange-100 text-orange-800",
  REFUND_INITIATED:      "bg-orange-100 text-orange-800",
  REFUND_COMPLETED:      "bg-green-100 text-green-800",
}

// Only valid next-state transitions (mirrors backend validateTransition)
const NEXT_STATUSES: Record<string, string[]> = {
  PRESCRIPTION_PENDING:  ["PRESCRIPTION_APPROVED", "PRESCRIPTION_REJECTED"],
  PRESCRIPTION_APPROVED: ["PAYMENT_PENDING"],
  PAYMENT_PENDING:       ["PAID", "PAYMENT_FAILED"],
  PAYMENT_FAILED:        ["PAID"],
  PAID:                  ["PACKED"],
  PACKED:                ["OUT_FOR_DELIVERY"],
  OUT_FOR_DELIVERY:      ["DELIVERED"],
  DELIVERED:             ["RETURN_REQUESTED"],
  RETURN_REQUESTED:      ["REFUND_INITIATED"],
  REFUND_INITIATED:      ["REFUND_COMPLETED"],
}

const TERMINAL_STATUSES = [
  "DELIVERED", "CUSTOMER_CANCELLED", "ADMIN_CANCELLED", "REFUND_COMPLETED",
]

export default function AdminOrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const orderId = Number(id)

  const { data: order, isLoading } = useAdminOrderDetail(orderId)
  const updateStatus = useUpdateOrderStatus()
  const cancelOrder  = useCancelAdminOrder()

  const [newStatus, setNewStatus] = useState("")
  const [adminNote, setAdminNote] = useState("")

  if (isLoading) {
    return (
      <div className="p-6 space-y-4 animate-pulse">
        <div className="h-8 bg-slate-200 rounded w-48" />
        <div className="h-40 bg-slate-200 rounded-xl" />
        <div className="h-60 bg-slate-200 rounded-xl" />
      </div>
    )
  }

  if (!order) {
    return (
      <div className="p-6 text-center text-slate-500 space-y-2">
        <p className="text-4xl">📦</p>
        <p>Order not found.</p>
        <Link to="/admin/orders" className="text-green-600 underline text-sm">Back to orders</Link>
      </div>
    )
  }

  const isTerminal = order.status != null && TERMINAL_STATUSES.includes(order.status)
  const validNextStatuses = NEXT_STATUSES[order.status ?? ""] ?? []
  const canCancel =
    !isTerminal &&
    order.status !== "REFUND_COMPLETED" &&
    !["CUSTOMER_CANCELLED", "ADMIN_CANCELLED"].includes(order.status ?? "")

  const handleUpdateStatus = () => {
    if (!newStatus) return
    updateStatus.mutate(
      { id: orderId, status: newStatus, adminNote },
      { onSuccess: () => { setNewStatus(""); setAdminNote("") } }
    )
  }

  return (
    <div className="p-6 space-y-5 max-w-3xl">
      <div className="flex items-center gap-3">
        <Link to="/admin/orders" className="text-sm text-slate-500 hover:text-slate-800">← Orders</Link>
        <h1 className="text-xl font-bold text-slate-800">Order #{order.id}</h1>
        <span className={`text-xs px-2.5 py-1 rounded-full font-medium ${STATUS_COLOR[order.status ?? ""] ?? "bg-slate-100 text-slate-700"}`}>
          {order.status?.replace(/_/g, " ")}
        </span>
      </div>

      {/* Customer + Delivery info */}
      <div className="bg-white rounded-xl border p-5 grid sm:grid-cols-2 gap-4 text-sm">
        <div className="space-y-1">
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Customer</p>
          <p className="text-slate-800 font-medium">{order.userName ?? "—"}</p>
          <p className="text-slate-500">{order.userEmail}</p>
        </div>
        <div className="space-y-1">
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Delivery</p>
          {order.deliveryAddress && <p className="text-slate-700">📍 {order.deliveryAddress}</p>}
          {order.deliverySlot && <p className="text-slate-700">🕐 {order.deliverySlot}</p>}
          {order.createdAt && (
            <p className="text-slate-500">📅 {new Date(order.createdAt).toLocaleDateString()}</p>
          )}
        </div>
        <div className="space-y-1">
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Payment</p>
          <p className="text-slate-700">{order.paymentMethod ?? "—"}</p>
          {order.paymentId && <p className="text-slate-400 text-xs">{order.paymentId}</p>}
        </div>
        {order.adminNote && (
          <div className="space-y-1">
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Admin Note</p>
            <p className="text-slate-700">{order.adminNote}</p>
          </div>
        )}
      </div>

      {/* Items */}
      <div className="bg-white rounded-xl border p-5">
        <h2 className="font-semibold text-slate-800 mb-3">Items ({order.items?.length ?? 0})</h2>
        <div className="space-y-2">
          {order.items?.map((item) => (
            <div key={item.id} className="flex justify-between text-sm py-2 border-b last:border-0">
              <span className="text-slate-700">
                {item.medicineName}
                {item.medicineStrength && (
                  <span className="text-slate-400 ml-1">{item.medicineStrength}</span>
                )}
                <span className="text-slate-400 ml-1">× {item.quantity}</span>
              </span>
              <span className="font-semibold text-slate-800">₹{item.totalPrice?.toFixed(2)}</span>
            </div>
          ))}
        </div>
        <div className="flex justify-between font-bold text-slate-800 pt-3 border-t mt-2">
          <span>Total</span>
          <span>₹{order.totalAmount?.toFixed(2)}</span>
        </div>
      </div>

      {/* Status update */}
      {(validNextStatuses.length > 0 || canCancel) && (
        <div className="bg-white rounded-xl border p-5 space-y-3">
          <h2 className="font-semibold text-slate-800">Update Status</h2>

          {validNextStatuses.length > 0 && (
            <>
              <div className="flex gap-2 flex-wrap">
                {validNextStatuses.map((s) => (
                  <button
                    key={s}
                    onClick={() => setNewStatus(s)}
                    className={`px-3 py-1.5 rounded-lg text-sm font-medium border transition-colors ${
                      newStatus === s
                        ? "bg-green-600 text-white border-green-600"
                        : "bg-white text-slate-700 hover:bg-slate-50"
                    }`}
                  >
                    → {s.replace(/_/g, " ")}
                  </button>
                ))}
              </div>
              <textarea
                value={adminNote}
                onChange={(e) => setAdminNote(e.target.value)}
                placeholder="Admin note (optional)"
                rows={2}
                className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-green-500 resize-none"
              />
              <button
                onClick={handleUpdateStatus}
                disabled={!newStatus || updateStatus.isPending}
                className="px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                {updateStatus.isPending ? "Updating…" : "Confirm Update"}
              </button>
            </>
          )}

          {canCancel && (
            <div className="pt-1 border-t">
              <button
                onClick={() => { if (order.id != null) cancelOrder.mutate({ id: order.id }) }}
                disabled={cancelOrder.isPending}
                className="px-4 py-2 bg-red-100 text-red-700 text-sm font-medium rounded-lg hover:bg-red-200 disabled:opacity-50 transition-colors"
              >
                {cancelOrder.isPending ? "Cancelling…" : "Cancel Order"}
              </button>
            </div>
          )}

          {(updateStatus.isError || cancelOrder.isError) && (
            <p className="text-xs text-red-600">Action failed. Please try again.</p>
          )}
        </div>
      )}
    </div>
  )
}
