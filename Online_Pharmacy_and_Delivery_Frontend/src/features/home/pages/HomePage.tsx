import { Link, Navigate } from "react-router-dom"
import { useAuthStore } from "@/shared/stores/authStore"
import { useOrders } from "@/features/orders/api/useOrders"
import { useCart } from "@/features/cart/api/useCart"

const STATUS_COLOR: Record<string, string> = {
  DELIVERED:          "bg-green-100 text-green-800",
  PAID:               "bg-blue-100 text-blue-800",
  PACKED:             "bg-purple-100 text-purple-800",
  OUT_FOR_DELIVERY:   "bg-indigo-100 text-indigo-800",
  PAYMENT_PENDING:    "bg-yellow-100 text-yellow-800",
  CUSTOMER_CANCELLED: "bg-red-100 text-red-800",
  ADMIN_CANCELLED:    "bg-red-100 text-red-800",
  CHECKOUT_STARTED:   "bg-slate-100 text-slate-700",
}

export default function HomePage() {
  const user = useAuthStore((s) => s.user)

  // Redirect admin to their dashboard
  if (user?.role === "ADMIN") {
    return <Navigate to="/admin/dashboard" replace />
  }

  return <CustomerHome />
}

function CustomerHome() {
  const user = useAuthStore((s) => s.user)
  const { data: orders } = useOrders()
  const { data: cart }   = useCart()

  const recentOrders  = orders?.slice(0, 3) ?? []
  const cartItemCount = cart?.totalItems ?? 0

  const quickLinks = [
    {
      to:    "/catalog",
      icon:  "💊",
      label: "Browse Medicines",
      desc:  "Find prescription & OTC drugs",
      color: "bg-green-50 border-green-200 hover:bg-green-100",
    },
    {
      to:    "/cart",
      icon:  "🛒",
      label: "My Cart",
      desc:  cartItemCount > 0 ? `${cartItemCount} item${cartItemCount !== 1 ? "s" : ""} waiting` : "Your cart is empty",
      color: "bg-blue-50 border-blue-200 hover:bg-blue-100",
    },
    {
      to:    "/orders",
      icon:  "📦",
      label: "My Orders",
      desc:  orders ? `${orders.length} order${orders.length !== 1 ? "s" : ""} total` : "Track your deliveries",
      color: "bg-purple-50 border-purple-200 hover:bg-purple-100",
    },
    {
      to:    "/prescriptions",
      icon:  "📋",
      label: "Prescriptions",
      desc:  "Upload & manage your Rx",
      color: "bg-amber-50 border-amber-200 hover:bg-amber-100",
    },
  ]

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-8 space-y-8">

      {/* Hero greeting */}
      <div className="bg-gradient-to-br from-green-600 to-green-700 rounded-2xl p-6 text-white">
        <p className="text-green-200 text-sm font-medium">Welcome back,</p>
        <h1 className="text-3xl font-bold mt-0.5">{user?.username} 👋</h1>
        <p className="text-green-200 text-sm mt-2">
          Your health, delivered to your door.
        </p>
        <Link
          to="/catalog"
          className="inline-block mt-4 px-5 py-2.5 bg-white text-green-700 font-semibold rounded-xl text-sm hover:bg-green-50 transition-colors"
        >
          Browse Medicines →
        </Link>
      </div>

      {/* Quick links */}
      <div>
        <h2 className="text-lg font-bold text-slate-800 mb-3">Quick Access</h2>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {quickLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className={`flex flex-col p-4 rounded-xl border transition-colors ${link.color}`}
            >
              <span className="text-2xl mb-2">{link.icon}</span>
              <p className="font-semibold text-slate-800 text-sm">{link.label}</p>
              <p className="text-xs text-slate-500 mt-0.5 leading-tight">{link.desc}</p>
            </Link>
          ))}
        </div>
      </div>

      {/* Recent orders */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-lg font-bold text-slate-800">Recent Orders</h2>
          {(orders?.length ?? 0) > 3 && (
            <Link to="/orders" className="text-sm text-green-600 hover:underline">View all →</Link>
          )}
        </div>

        {recentOrders.length === 0 ? (
          <div className="bg-white border rounded-xl p-8 text-center space-y-2 text-slate-400">
            <p className="text-3xl">📦</p>
            <p className="text-sm">No orders yet.</p>
            <Link
              to="/catalog"
              className="inline-block mt-2 text-sm text-green-600 hover:underline"
            >
              Start shopping →
            </Link>
          </div>
        ) : (
          <div className="space-y-2">
            {recentOrders.map((order) => {
              const color = STATUS_COLOR[order.status ?? ""] ?? "bg-slate-100 text-slate-700"
              return (
                <Link
                  key={order.id}
                  to={`/orders/${order.id}`}
                  className="flex items-center justify-between bg-white border rounded-xl p-4 hover:shadow-md transition-all duration-150 hover:-translate-y-0.5"
                >
                  <div>
                    <p className="font-semibold text-slate-800">Order #{order.id}</p>
                    <p className="text-sm text-slate-500">
                      {order.items?.length ?? 0} item{(order.items?.length ?? 0) !== 1 ? "s" : ""}
                      {" · "}₹{(order.finalAmount ?? order.totalAmount ?? 0).toFixed(2)}
                    </p>
                  </div>
                  <span className={`text-xs px-3 py-1 rounded-full font-medium ${color}`}>
                    {order.status?.replace(/_/g, " ")}
                  </span>
                </Link>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
