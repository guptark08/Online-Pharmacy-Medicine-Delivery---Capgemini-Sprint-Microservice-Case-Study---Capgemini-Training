import { useParams, Link, useLocation } from "react-router-dom"
import { useOrder } from "../api/useOrder"
import type { components } from "@/shared/types/api/order"

type OrderStatus = NonNullable<components["schemas"]["OrderResponse"]["status"]>

const STATUS_META: Partial<Record<OrderStatus, { label: string; color: string; icon: string }>> = {
  PAID:                { label: "Payment Confirmed",  color: "bg-blue-100 text-blue-800",   icon: "💳" },
  PAYMENT_PENDING:     { label: "Payment Pending",    color: "bg-yellow-100 text-yellow-800",icon: "⏳" },
  PAYMENT_FAILED:      { label: "Payment Failed",     color: "bg-red-100 text-red-800",     icon: "❌" },
  PACKED:              { label: "Being Packed",       color: "bg-purple-100 text-purple-800",icon: "📦" },
  OUT_FOR_DELIVERY:    { label: "Out for Delivery",   color: "bg-indigo-100 text-indigo-800",icon: "🚚" },
  DELIVERED:           { label: "Delivered",          color: "bg-green-100 text-green-800", icon: "✅" },
  CUSTOMER_CANCELLED:  { label: "Cancelled by You",   color: "bg-red-100 text-red-800",     icon: "✕" },
  ADMIN_CANCELLED:     { label: "Cancelled",          color: "bg-red-100 text-red-800",     icon: "✕" },
  CHECKOUT_STARTED:    { label: "Processing",         color: "bg-slate-100 text-slate-700", icon: "🔄" },
}

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const location = useLocation()
  const fromCheckout = (location.state as { fromCheckout?: boolean } | null)?.fromCheckout

  const { data: order, isLoading } = useOrder(Number(id))

  if (isLoading) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-8 animate-pulse space-y-4">
        <div className="h-20 bg-slate-200 rounded-xl" />
        <div className="h-48 bg-slate-200 rounded-xl" />
      </div>
    )
  }

  if (!order) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-20 text-center text-slate-500 space-y-3">
        <p className="text-4xl">📦</p>
        <p>Order not found.</p>
        <Link to="/orders" className="text-green-600 underline text-sm">View all orders</Link>
      </div>
    )
  }

  const meta = STATUS_META[order.status as OrderStatus] ?? {
    label: order.status ?? "Unknown",
    color: "bg-slate-100 text-slate-700",
    icon:  "📋",
  }

  return (
    <div className="max-w-2xl mx-auto px-4 sm:px-6 py-8 space-y-5">

      {/* Success banner (only shown when navigating from checkout) */}
      {fromCheckout && (
        <div className="bg-green-50 border border-green-200 rounded-xl p-4 flex items-start gap-3">
          <span className="text-2xl">🎉</span>
          <div>
            <p className="font-semibold text-green-800">Order placed successfully!</p>
            <p className="text-sm text-green-600 mt-0.5">
              We'll notify you when your order ships.
            </p>
          </div>
        </div>
      )}

      {/* Order header */}
      <div className="bg-white border rounded-xl p-5 space-y-4">
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="text-xs text-slate-500">Order #{order.id}</p>
            <p className="text-lg font-bold text-slate-800 mt-0.5">{meta.icon} {meta.label}</p>
          </div>
          <span className={`text-xs px-3 py-1 rounded-full font-medium shrink-0 ${meta.color}`}>
            {order.status}
          </span>
        </div>

        <div className="space-y-1 text-sm text-slate-600">
          {order.deliveryAddress && <p>📍 {order.deliveryAddress}</p>}
          {order.deliverySlot    && <p>🕐 {order.deliverySlot}</p>}
          {order.createdAt       && <p>📅 Placed on {new Date(order.createdAt).toLocaleDateString()}</p>}
        </div>

        {/* Items */}
        {(order.items?.length ?? 0) > 0 && (
          <div className="border-t pt-4 space-y-2">
            {order.items?.map((item, i) => (
              <div key={i} className="flex justify-between text-sm">
                <span className="text-slate-600">
                  {item.medicineName}
                  <span className="text-slate-400 ml-1">× {item.quantity}</span>
                </span>
                <span className="font-medium text-slate-800">₹{item.subtotal?.toFixed(2)}</span>
              </div>
            ))}
          </div>
        )}

        {/* Total */}
        <div className="border-t pt-3 flex justify-between font-bold text-slate-800">
          <span>Total</span>
          <span>₹{(order.finalAmount ?? order.totalAmount ?? 0).toFixed(2)}</span>
        </div>
      </div>

      {/* Actions */}
      <div className="flex gap-3">
        <Link
          to="/orders"
          className="flex-1 text-center py-2.5 border rounded-lg text-sm font-medium hover:bg-slate-50 transition-colors"
        >
          All Orders
        </Link>
        <Link
          to="/catalog"
          className="flex-1 text-center py-2.5 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 transition-colors"
        >
          Continue Shopping
        </Link>
      </div>
    </div>
  )
}
