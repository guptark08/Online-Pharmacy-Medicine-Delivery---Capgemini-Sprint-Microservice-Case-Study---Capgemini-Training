import { Link } from "react-router-dom"
import { useDashboard } from "../api/useDashboard"

function StatCard({
  label,
  value,
  sub,
  color = "text-slate-800",
}: {
  label: string
  value: string | number | undefined
  sub?: string
  color?: string
}) {
  return (
    <div className="bg-white rounded-xl border p-5 space-y-1">
      <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{label}</p>
      <p className={`text-3xl font-bold ${color}`}>{value ?? "—"}</p>
      {sub && <p className="text-xs text-slate-400">{sub}</p>}
    </div>
  )
}

const STATUS_COLOR: Record<string, string> = {
  DELIVERED:          "bg-green-100 text-green-800",
  PAID:               "bg-blue-100 text-blue-800",
  PACKED:             "bg-purple-100 text-purple-800",
  OUT_FOR_DELIVERY:   "bg-indigo-100 text-indigo-800",
  PAYMENT_PENDING:    "bg-yellow-100 text-yellow-800",
  CUSTOMER_CANCELLED: "bg-red-100 text-red-800",
  ADMIN_CANCELLED:    "bg-red-100 text-red-800",
}

export default function AdminDashboardPage() {
  const { data, isLoading } = useDashboard()

  if (isLoading) {
    return (
      <div className="p-6 space-y-6">
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="h-28 bg-slate-200 rounded-xl animate-pulse" />
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="p-6 space-y-6">
      <h1 className="text-2xl font-bold text-slate-800">Dashboard</h1>

      {/* Revenue row */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard
          label="Total Revenue"
          value={data?.totalRevenue != null ? `₹${data.totalRevenue.toLocaleString()}` : undefined}
          color="text-green-700"
        />
        <StatCard
          label="Revenue This Month"
          value={data?.revenueThisMonth != null ? `₹${data.revenueThisMonth.toLocaleString()}` : undefined}
        />
        <StatCard
          label="Revenue Today"
          value={data?.revenueToday != null ? `₹${data.revenueToday.toLocaleString()}` : undefined}
        />
      </div>

      {/* Orders row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-6 gap-4">
        <StatCard label="Total Orders"     value={data?.totalOrders} />
        <StatCard label="Pending Payment"  value={data?.pendingPaymentOrders} color="text-yellow-600" />
        <StatCard label="Packed"           value={data?.packedOrders}          color="text-purple-700" />
        <StatCard label="Out for Delivery" value={data?.outForDeliveryOrders}  color="text-indigo-700" />
        <StatCard label="Delivered"        value={data?.deliveredOrders}       color="text-green-700" />
        <StatCard label="Cancelled"        value={data?.cancelledOrders}       color="text-red-600" />
      </div>

      {/* Prescriptions + Inventory row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <StatCard label="Pending Rx"   value={data?.pendingPrescriptions}    color="text-amber-600" />
        <StatCard label="Approved Rx"  value={data?.approvedPrescriptions}   color="text-green-700" />
        <StatCard label="Active Meds"  value={data?.activeMedicines} />
        <StatCard label="Low Stock"    value={data?.lowStockCount}           color="text-red-600" />
      </div>

      {/* Recent Orders + Alerts row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* Recent Orders */}
        <div className="bg-white rounded-xl border p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-slate-800">Recent Orders</h2>
            <Link to="/admin/orders" className="text-xs text-green-600 hover:underline">View all →</Link>
          </div>
          {(data?.recentOrders?.length ?? 0) === 0 ? (
            <p className="text-sm text-slate-400">No recent orders.</p>
          ) : (
            <div className="space-y-2">
              {data?.recentOrders?.map((order) => (
                <div key={order.orderId} className="flex items-center justify-between text-sm py-2 border-b last:border-0">
                  <div className="min-w-0">
                    <p className="font-medium text-slate-700">#{order.orderId}</p>
                    <p className="text-xs text-slate-400 truncate">{order.userEmail}</p>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${STATUS_COLOR[order.status ?? ""] ?? "bg-slate-100 text-slate-700"}`}>
                      {order.status}
                    </span>
                    <span className="font-semibold text-slate-800">
                      ₹{order.totalAmount?.toFixed(0)}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Alerts column */}
        <div className="space-y-4">
          {/* Low Stock */}
          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold text-slate-800 mb-3">
              ⚠ Low Stock Alerts
              {(data?.lowStockCount ?? 0) > 0 && (
                <span className="ml-2 text-xs bg-red-100 text-red-700 px-2 py-0.5 rounded-full">
                  {data?.lowStockCount}
                </span>
              )}
            </h2>
            {(data?.lowStockAlerts?.length ?? 0) === 0 ? (
              <p className="text-sm text-slate-400">No low stock alerts.</p>
            ) : (
              <div className="space-y-1.5">
                {data?.lowStockAlerts?.slice(0, 5).map((alert) => (
                  <div key={alert.medicineId} className="flex justify-between text-sm">
                    <span className="text-slate-700 truncate">{alert.medicineName}</span>
                    <span className="font-semibold text-red-600 ml-2 shrink-0">{alert.currentStock} left</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Expiry Alerts */}
          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold text-slate-800 mb-3">
              📅 Expiry Alerts
              {(data?.expiringThisMonthCount ?? 0) > 0 && (
                <span className="ml-2 text-xs bg-amber-100 text-amber-700 px-2 py-0.5 rounded-full">
                  {data?.expiringThisMonthCount}
                </span>
              )}
            </h2>
            {(data?.expiryAlerts?.length ?? 0) === 0 ? (
              <p className="text-sm text-slate-400">No expiry alerts.</p>
            ) : (
              <div className="space-y-1.5">
                {data?.expiryAlerts?.slice(0, 5).map((alert) => (
                  <div key={alert.medicineId} className="flex justify-between text-sm">
                    <span className="text-slate-700 truncate">{alert.medicineName}</span>
                    <span className="text-amber-600 ml-2 shrink-0">{alert.expiryDate}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
