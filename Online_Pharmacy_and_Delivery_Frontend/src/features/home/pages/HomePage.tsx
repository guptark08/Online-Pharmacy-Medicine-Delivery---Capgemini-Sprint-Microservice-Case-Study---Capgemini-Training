import { useState, useEffect } from "react"
import { Link, Navigate, useNavigate } from "react-router-dom"
import { useAuthStore } from "@/shared/stores/authStore"
import { useOrders } from "@/features/orders/api/useOrders"
import { useCart } from "@/features/cart/api/useCart"
import { useCategories } from "@/features/catalog/api/useCategories"
import { useMedicines } from "@/features/catalog/api/useMedicines"

const STATUS_CONFIG: Record<string, { label: string; color: string; bg: string }> = {
  DELIVERED:          { label: "Delivered",        color: "text-green-800",  bg: "bg-green-100" },
  PAID:               { label: "Paid",             color: "text-blue-800",   bg: "bg-blue-100" },
  PACKED:             { label: "Packed",           color: "text-purple-800", bg: "bg-purple-100" },
  OUT_FOR_DELIVERY:   { label: "Out for Delivery", color: "text-indigo-800", bg: "bg-indigo-100" },
  PAYMENT_PENDING:    { label: "Payment Pending",  color: "text-yellow-800", bg: "bg-yellow-100" },
  CUSTOMER_CANCELLED: { label: "Cancelled",        color: "text-red-800",    bg: "bg-red-100" },
  ADMIN_CANCELLED:    { label: "Cancelled",        color: "text-red-800",    bg: "bg-red-100" },
  CHECKOUT_STARTED:   { label: "Processing",       color: "text-slate-700",  bg: "bg-slate-100" },
}

// Palette cycles across real category tiles — no hardcoded names
const CATEGORY_PALETTE = [
  { color: "bg-orange-50",  text: "text-orange-600" },
  { color: "bg-yellow-50",  text: "text-yellow-600" },
  { color: "bg-blue-50",    text: "text-blue-600"   },
  { color: "bg-pink-50",    text: "text-pink-500"   },
  { color: "bg-teal-50",    text: "text-teal-600"   },
  { color: "bg-green-50",   text: "text-green-600"  },
  { color: "bg-purple-50",  text: "text-purple-600" },
  { color: "bg-red-50",     text: "text-red-500"    },
]

const CATEGORY_ICON = "M9.75 3.104v5.714a2.25 2.25 0 01-.659 1.591L5 14.5M9.75 3.104c-.251.023-.501.05-.75.082m.75-.082a24.301 24.301 0 014.5 0m0 0v5.714c0 .597.237 1.17.659 1.591L19.8 15.3M14.25 3.104c.251.023.501.05.75.082M19.8 15.3l-1.57.393A9.065 9.065 0 0112 15a9.065 9.065 0 00-6.23-.693L5 14.5m14.8.8l1.402 1.402c1.232 1.232 1.232 3.23 0 4.462l-.678.678a3.158 3.158 0 01-4.462 0L12 17.672"

export default function HomePage() {
  const user = useAuthStore((s) => s.user)
  if (user?.role === "ADMIN") return <Navigate to="/admin/dashboard" replace />
  return <CustomerHome />
}

function CustomerHome() {
  const user     = useAuthStore((s) => s.user)
  const navigate = useNavigate()

  const { data: orders }     = useOrders()
  const { data: cart }       = useCart()
  const { data: categories } = useCategories()
  const { data: medicines }  = useMedicines({ size: 1 })

  const [isLoaded, setIsLoaded]       = useState(false)
  const [searchQuery, setSearchQuery] = useState("")

  useEffect(() => {
    const id = requestAnimationFrame(() => setIsLoaded(true))
    return () => cancelAnimationFrame(id)
  }, [])

  const recentOrders   = orders?.slice(0, 3) ?? []
  const cartItemCount  = cart?.totalItems ?? 0
  const medicineCount  = medicines?.totalElements ?? null

  function handleHeroSearch(e: React.FormEvent) {
    e.preventDefault()
    const q = searchQuery.trim()
    navigate(q ? `/catalog?keyword=${encodeURIComponent(q)}` : "/catalog")
  }

  return (
    <div className="min-h-screen bg-slate-50">

      {/* ── Hero Banner ──────────────────────────────────────────── */}
      <section className="relative overflow-hidden bg-gradient-to-r from-green-700 via-green-600 to-emerald-500">
        <div className="absolute -right-20 -top-20 w-80 h-80 rounded-full bg-white/5" />
        <div className="absolute -right-8 top-16 w-48 h-48 rounded-full bg-white/5" />
        <div className="absolute left-1/3 -bottom-12 w-64 h-64 rounded-full bg-black/5" />

        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 py-10 sm:py-14 lg:py-16">
          <div className="grid lg:grid-cols-2 gap-8 items-center">

            {/* Left content */}
            <div className={`space-y-5 transition-all duration-700 ${isLoaded ? "opacity-100 translate-y-0" : "opacity-0 translate-y-4"}`}>
              <div className="inline-flex items-center gap-2 bg-white/15 backdrop-blur-sm text-white text-xs font-semibold px-3 py-1.5 rounded-full">
                <span className="w-1.5 h-1.5 rounded-full bg-green-300 animate-pulse" />
                Trusted by thousands — Delivering across India
              </div>

              <div>
                <h1 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-white leading-tight">
                  Your Health,
                  <br />
                  <span className="text-green-200">Delivered Fast</span>
                </h1>
                <p className="mt-3 text-green-100/90 text-sm sm:text-base max-w-md leading-relaxed">
                  Order genuine medicines, upload prescriptions, and get doorstep delivery — all in one place.
                </p>
              </div>

              {/* Hero search */}
              <form onSubmit={handleHeroSearch} className="flex max-w-md">
                <div className="flex w-full rounded-xl overflow-hidden shadow-xl">
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search medicines, vitamins..."
                    className="flex-1 px-4 py-3 text-sm text-slate-700 placeholder:text-slate-400 outline-none bg-white"
                  />
                  <button
                    type="submit"
                    className="px-5 bg-orange-500 hover:bg-orange-600 text-white font-semibold text-sm flex items-center gap-1.5 transition-colors"
                  >
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
                    </svg>
                    Search
                  </button>
                </div>
              </form>
            </div>

            {/* Right: stat cards */}
            <div className={`grid grid-cols-2 gap-3 transition-all duration-700 delay-200 ${isLoaded ? "opacity-100 translate-y-0" : "opacity-0 translate-y-4"}`}>
              {[
                {
                  to: "/cart",
                  label: "My Cart",
                  value: String(cartItemCount),
                  unit: "items",
                  icon: "M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 00-3 3h15.75m-12.75-3h11.218c1.121-2.3 2.1-4.684 2.924-7.138a60.114 60.114 0 00-16.536-1.84M7.5 14.25L5.106 5.272M6 20.25a.75.75 0 11-1.5 0 .75.75 0 011.5 0zm12.75 0a.75.75 0 11-1.5 0 .75.75 0 011.5 0z",
                },
                {
                  to: "/orders",
                  label: "Total Orders",
                  value: String(orders?.length ?? 0),
                  unit: "orders",
                  icon: "M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.129-.504 1.09-1.124a17.902 17.902 0 00-3.213-9.193 2.056 2.056 0 00-1.58-.86H14.25M16.5 18.75h-2.25m0-11.177v-.958c0-.568-.422-1.048-.987-1.106a48.554 48.554 0 00-10.026 0 1.106 1.106 0 00-.987 1.106v7.635m12-6.677v6.677m0 4.5v-4.5m0 0h-12",
                },
                {
                  to: "/prescriptions",
                  label: "Prescriptions",
                  value: "Manage",
                  unit: "Rx",
                  icon: "M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z",
                },
                {
                  to: "/catalog",
                  label: "Medicines",
                  value: medicineCount !== null ? String(medicineCount) : "—",
                  unit: "products",
                  icon: "M9.75 3.104v5.714a2.25 2.25 0 01-.659 1.591L5 14.5M9.75 3.104c-.251.023-.501.05-.75.082m.75-.082a24.301 24.301 0 014.5 0m0 0v5.714c0 .597.237 1.17.659 1.591L19.8 15.3M14.25 3.104c.251.023.501.05.75.082M19.8 15.3l-1.57.393A9.065 9.065 0 0112 15a9.065 9.065 0 00-6.23-.693L5 14.5m14.8.8l1.402 1.402c1.232 1.232 1.232 3.23 0 4.462l-.678.678a3.158 3.158 0 01-4.462 0L12 17.672",
                },
              ].map(({ to, label, value, unit, icon }) => (
                <Link
                  key={to}
                  to={to}
                  className="group bg-white/10 hover:bg-white/20 backdrop-blur-sm border border-white/20 rounded-2xl p-4 sm:p-5 transition-all duration-300"
                >
                  <div className="w-9 h-9 rounded-xl bg-white/20 flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                    <svg className="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                      <path strokeLinecap="round" strokeLinejoin="round" d={icon} />
                    </svg>
                  </div>
                  <p className="text-white/60 text-[10px] font-semibold uppercase tracking-wider">{label}</p>
                  <p className="text-white font-extrabold text-2xl sm:text-3xl mt-0.5 leading-none">
                    {value}
                    <span className="text-sm font-normal text-white/60 ml-1">{unit}</span>
                  </p>
                </Link>
              ))}
            </div>
          </div>
        </div>

        {/* Wave */}
        <div className="absolute bottom-0 left-0 right-0">
          <svg viewBox="0 0 1440 48" fill="none" className="w-full">
            <path d="M0 48L60 44C120 40 240 32 360 28C480 24 600 24 720 26.7C840 29.3 960 34.7 1080 36C1200 37.3 1320 34.7 1380 33.3L1440 32V48H0Z" fill="#F8FAFC" />
          </svg>
        </div>
      </section>

      {/* ── Trust badges ─────────────────────────────────────────── */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 -mt-1 mb-8">
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {[
            { icon: "M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z", label: "100% Genuine", sub: "Verified medicines only" },
            { icon: "M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z", label: "Fast Delivery", sub: "In 2–4 hours" },
            { icon: "M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z", label: "Secure Payment", sub: "256-bit encryption" },
            { icon: "M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z", label: "Expert Support", sub: "Pharmacists 24/7" },
          ].map((b, i) => (
            <div
              key={b.label}
              className={`flex items-center gap-3 bg-white rounded-xl border px-4 py-3.5 shadow-sm transition-all duration-500 ${isLoaded ? "opacity-100 translate-y-0" : "opacity-0 translate-y-2"}`}
              style={{ transitionDelay: `${200 + i * 80}ms` }}
            >
              <div className="w-9 h-9 rounded-xl bg-green-50 flex items-center justify-center shrink-0">
                <svg className="w-4 h-4 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d={b.icon} />
                </svg>
              </div>
              <div>
                <p className="text-xs font-bold text-slate-800 leading-none">{b.label}</p>
                <p className="text-[10px] text-slate-500 mt-0.5">{b.sub}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ── Promotional banners ──────────────────────────────────── */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 mb-10">
        <div className="grid sm:grid-cols-3 gap-4">
          <Link
            to="/prescriptions"
            className="group relative overflow-hidden rounded-2xl bg-gradient-to-br from-blue-600 to-blue-700 p-5 text-white hover:shadow-xl transition-all duration-300 hover:-translate-y-0.5"
          >
            <div className="absolute right-4 bottom-4 w-20 h-20 rounded-full bg-white/10 group-hover:scale-110 transition-transform" />
            <p className="text-[11px] font-semibold text-blue-200 uppercase tracking-wider mb-1">Upload & Save</p>
            <p className="font-extrabold text-lg leading-snug">Get medicines<br />with prescription</p>
            <p className="text-xs text-blue-200 mt-1">Pharmacist approval in minutes</p>
            <div className="mt-4 inline-flex items-center gap-1 text-xs font-semibold bg-white/15 px-3 py-1.5 rounded-full">
              Upload Now
              <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
              </svg>
            </div>
          </Link>

          <Link
            to="/catalog"
            className="group relative overflow-hidden rounded-2xl bg-gradient-to-br from-orange-500 to-orange-600 p-5 text-white hover:shadow-xl transition-all duration-300 hover:-translate-y-0.5"
          >
            <div className="absolute right-4 bottom-4 w-20 h-20 rounded-full bg-white/10 group-hover:scale-110 transition-transform" />
            <p className="text-[11px] font-semibold text-orange-200 uppercase tracking-wider mb-1">Shop Now</p>
            <p className="font-extrabold text-lg leading-snug">
              {medicineCount !== null ? `${medicineCount}` : "Browse"}<br />medicines available
            </p>
            <p className="text-xs text-orange-200 mt-1">OTC &amp; prescription drugs</p>
            <div className="mt-4 inline-flex items-center gap-1 text-xs font-semibold bg-white/15 px-3 py-1.5 rounded-full">
              Browse Catalog
              <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
              </svg>
            </div>
          </Link>

          <Link
            to="/orders"
            className="group relative overflow-hidden rounded-2xl bg-gradient-to-br from-green-600 to-emerald-700 p-5 text-white hover:shadow-xl transition-all duration-300 hover:-translate-y-0.5"
          >
            <div className="absolute right-4 bottom-4 w-20 h-20 rounded-full bg-white/10 group-hover:scale-110 transition-transform" />
            <p className="text-[11px] font-semibold text-green-200 uppercase tracking-wider mb-1">Track Orders</p>
            <p className="font-extrabold text-lg leading-snug">Real-time<br />delivery tracking</p>
            <p className="text-xs text-green-200 mt-1">Know exactly where your order is</p>
            <div className="mt-4 inline-flex items-center gap-1 text-xs font-semibold bg-white/15 px-3 py-1.5 rounded-full">
              View Orders
              <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
              </svg>
            </div>
          </Link>
        </div>
      </section>

      {/* ── Shop by Category (real data) ─────────────────────────── */}
      {categories && categories.length > 0 && (
        <section className="max-w-7xl mx-auto px-4 sm:px-6 mb-10">
          <div className="flex items-center justify-between mb-5">
            <div>
              <h2 className="text-lg font-extrabold text-slate-800">Shop by Category</h2>
              <p className="text-xs text-slate-500 mt-0.5">Find what you need quickly</p>
            </div>
            <Link to="/catalog" className="text-xs font-semibold text-green-600 hover:text-green-700 flex items-center gap-1 transition-colors">
              View all
              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
              </svg>
            </Link>
          </div>

          <div className="grid grid-cols-4 sm:grid-cols-8 gap-3">
            {categories.map((cat, i) => {
              const palette = CATEGORY_PALETTE[i % CATEGORY_PALETTE.length]
              return (
                <Link
                  key={cat.id}
                  to={`/catalog?categoryId=${cat.id}`}
                  className={`group flex flex-col items-center gap-2 p-3 rounded-2xl ${palette.color} hover:shadow-md transition-all duration-300 hover:-translate-y-0.5 ${isLoaded ? "opacity-100 scale-100" : "opacity-0 scale-95"}`}
                  style={{ transitionDelay: `${300 + i * 60}ms` }}
                >
                  <div className="w-10 h-10 rounded-xl flex items-center justify-center bg-white/70 group-hover:scale-110 transition-transform">
                    <svg className={`w-5 h-5 ${palette.text}`} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                      <path strokeLinecap="round" strokeLinejoin="round" d={CATEGORY_ICON} />
                    </svg>
                  </div>
                  <span className={`text-[10px] font-bold ${palette.text} text-center leading-tight`}>{cat.name}</span>
                </Link>
              )
            })}
          </div>
        </section>
      )}

      {/* ── Personalised welcome + cart CTA ──────────────────────── */}
      {user && (
        <section className="max-w-7xl mx-auto px-4 sm:px-6 mb-10">
          <div className="bg-white rounded-2xl border p-5 sm:p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-green-100 flex items-center justify-center text-2xl font-bold text-green-700 shrink-0">
                {(user.name?.trim() || user.username || "U")[0].toUpperCase()}
              </div>
              <div>
                <p className="text-sm text-slate-500">Welcome back,</p>
                <p className="font-extrabold text-slate-800 text-lg leading-tight">{user.name?.trim() || user.username}</p>
              </div>
            </div>
            <div className="flex flex-wrap gap-2">
              {cartItemCount > 0 && (
                <Link
                  to="/cart"
                  className="flex items-center gap-2 px-4 py-2.5 bg-orange-500 hover:bg-orange-600 text-white text-sm font-semibold rounded-xl transition-colors shadow-sm"
                >
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 00-3 3h15.75m-12.75-3h11.218c1.121-2.3 2.1-4.684 2.924-7.138a60.114 60.114 0 00-16.536-1.84M7.5 14.25L5.106 5.272M6 20.25a.75.75 0 11-1.5 0 .75.75 0 011.5 0zm12.75 0a.75.75 0 11-1.5 0 .75.75 0 011.5 0z" />
                  </svg>
                  Go to Cart ({cartItemCount})
                </Link>
              )}
              <Link
                to="/catalog"
                className="flex items-center gap-2 px-4 py-2.5 bg-green-600 hover:bg-green-700 text-white text-sm font-semibold rounded-xl transition-colors shadow-sm"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
                </svg>
                Browse Medicines
              </Link>
            </div>
          </div>
        </section>
      )}

      {/* ── Recent Orders ────────────────────────────────────────── */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 pb-16">
        <div className="flex items-center justify-between mb-5">
          <div>
            <h2 className="text-lg font-extrabold text-slate-800">Recent Orders</h2>
            <p className="text-xs text-slate-500 mt-0.5">Your latest deliveries</p>
          </div>
          {orders && orders.length > 3 && (
            <Link to="/orders" className="text-xs font-semibold text-green-600 hover:text-green-700 flex items-center gap-1 transition-colors">
              View all
              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
              </svg>
            </Link>
          )}
        </div>

        {recentOrders.length === 0 ? (
          <div className="bg-white border rounded-2xl p-10 text-center">
            <div className="w-16 h-16 rounded-2xl bg-slate-50 border flex items-center justify-center mx-auto mb-4">
              <svg className="w-8 h-8 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z" />
              </svg>
            </div>
            <p className="font-bold text-slate-700">No orders yet</p>
            <p className="text-sm text-slate-400 mt-1 mb-5">Start shopping to see your orders here</p>
            <Link
              to="/catalog"
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-green-600 hover:bg-green-700 text-white font-semibold rounded-xl text-sm transition-colors shadow-md"
            >
              Browse Medicines
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
              </svg>
            </Link>
          </div>
        ) : (
          <div className="bg-white border rounded-2xl overflow-hidden divide-y divide-slate-100">
            {recentOrders.map((order, i) => {
              const status = STATUS_CONFIG[order.status ?? ""] ?? { label: order.status ?? "", color: "text-slate-700", bg: "bg-slate-100" }
              return (
                <Link
                  key={order.id}
                  to={`/orders/${order.id}`}
                  className={`flex items-center justify-between px-5 py-4 hover:bg-slate-50 transition-all duration-300 ${isLoaded ? "opacity-100 translate-y-0" : "opacity-0 translate-y-2"}`}
                  style={{ transitionDelay: `${500 + i * 80}ms` }}
                >
                  <div className="flex items-center gap-4 min-w-0">
                    <div className="w-10 h-10 rounded-xl bg-green-50 border border-green-100 flex items-center justify-center shrink-0">
                      <svg className="w-5 h-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.129-.504 1.09-1.124a17.902 17.902 0 00-3.213-9.193 2.056 2.056 0 00-1.58-.86H14.25M16.5 18.75h-2.25m0-11.177v-.958c0-.568-.422-1.048-.987-1.106a48.554 48.554 0 00-10.026 0 1.106 1.106 0 00-.987 1.106v7.635m12-6.677v6.677m0 4.5v-4.5m0 0h-12" />
                      </svg>
                    </div>
                    <div className="min-w-0">
                      <p className="font-bold text-slate-800 text-sm">Order #{order.id}</p>
                      <p className="text-xs text-slate-500 mt-0.5">
                        {order.items?.length ?? 0} item{(order.items?.length ?? 0) !== 1 ? "s" : ""}
                        {" · "}₹{(order.finalAmount ?? order.totalAmount ?? 0).toFixed(2)}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 shrink-0">
                    <span className={`text-[11px] px-2.5 py-1 rounded-full font-bold ${status.bg} ${status.color}`}>
                      {status.label}
                    </span>
                    <svg className="w-4 h-4 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
                    </svg>
                  </div>
                </Link>
              )
            })}
          </div>
        )}
      </section>
    </div>
  )
}
