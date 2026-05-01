import type { ReactNode } from "react"
import { Link } from "react-router-dom"

interface AuthLayoutProps {
  title: string
  subtitle?: string
  children: ReactNode
  footer?: ReactNode
}

const TRUST_POINTS = [
  {
    icon: "M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z",
    label: "100% Genuine Medicines",
    sub:   "Every product verified & quality-tested",
  },
  {
    icon: "M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z",
    label: "Fast Doorstep Delivery",
    sub:   "Get medicines delivered in 2–4 hours",
  },
  {
    icon: "M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z",
    label: "Safe & Secure",
    sub:   "Your data is always encrypted & protected",
  },
  {
    icon: "M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z",
    label: "Expert Pharmacist Support",
    sub:   "Available 24/7 to help with your needs",
  },
]

export default function AuthLayout({ title, subtitle, children, footer }: AuthLayoutProps) {
  return (
    <div className="min-h-screen flex">

      {/* ── Left panel — brand & trust ───────────────────────────── */}
      <div className="hidden lg:flex lg:w-[45%] flex-col justify-between bg-gradient-to-br from-green-700 via-green-600 to-emerald-500 px-12 py-10 relative overflow-hidden">
        {/* Decorative circles */}
        <div className="absolute -top-16 -right-16 w-64 h-64 rounded-full bg-white/5" />
        <div className="absolute bottom-24 -left-12 w-48 h-48 rounded-full bg-white/5" />
        <div className="absolute top-1/2 right-8 w-24 h-24 rounded-full bg-white/5" />

        {/* Logo */}
        <div>
          <Link to="/" className="inline-flex items-center gap-2.5">
            <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center backdrop-blur-sm">
              <svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9.75 3.104v5.714a2.25 2.25 0 01-.659 1.591L5 14.5M9.75 3.104c-.251.023-.501.05-.75.082m.75-.082a24.301 24.301 0 014.5 0m0 0v5.714c0 .597.237 1.17.659 1.591L19.8 15.3M14.25 3.104c.251.023.501.05.75.082M19.8 15.3l-1.57.393A9.065 9.065 0 0112 15a9.065 9.065 0 00-6.23-.693L5 14.5m14.8.8l1.402 1.402c1.232 1.232 1.232 3.23 0 4.462l-.678.678a3.158 3.158 0 01-4.462 0L12 17.672" />
              </svg>
            </div>
            <span className="text-2xl font-extrabold text-white tracking-tight">
              Pharma<span className="text-green-200">Care</span>
            </span>
          </Link>
        </div>

        {/* Tagline */}
        <div className="space-y-3">
          <p className="text-[11px] font-bold text-green-300 uppercase tracking-widest">Your trusted pharmacy</p>
          <h2 className="text-3xl font-extrabold text-white leading-tight">
            Your Health,<br />Our Priority
          </h2>
          <p className="text-green-100/80 text-sm leading-relaxed max-w-xs">
            Order genuine medicines, upload prescriptions, and get doorstep delivery — all from one trusted platform.
          </p>
        </div>

        {/* Trust points */}
        <div className="space-y-4">
          {TRUST_POINTS.map((point) => (
            <div key={point.label} className="flex items-start gap-3">
              <div className="w-8 h-8 rounded-lg bg-white/15 flex items-center justify-center shrink-0 mt-0.5">
                <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d={point.icon} />
                </svg>
              </div>
              <div>
                <p className="text-sm font-bold text-white leading-none">{point.label}</p>
                <p className="text-xs text-green-200/80 mt-0.5">{point.sub}</p>
              </div>
            </div>
          ))}
        </div>

        {/* Bottom note */}
        <p className="text-[11px] text-green-300/60">
          © {new Date().getFullYear()} PharmaCare · All rights reserved
        </p>
      </div>

      {/* ── Right panel — form ───────────────────────────────────── */}
      <div className="flex-1 flex flex-col justify-center items-center px-6 py-10 bg-slate-50">
        <div className="w-full max-w-[420px] space-y-7">

          {/* Mobile logo */}
          <div className="lg:hidden text-center">
            <Link to="/" className="inline-flex items-center gap-2">
              <div className="w-8 h-8 bg-green-600 rounded-lg flex items-center justify-center">
                <svg className="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9.75 3.104v5.714a2.25 2.25 0 01-.659 1.591L5 14.5M9.75 3.104c-.251.023-.501.05-.75.082m.75-.082a24.301 24.301 0 014.5 0m0 0v5.714c0 .597.237 1.17.659 1.591L19.8 15.3" />
                </svg>
              </div>
              <span className="text-xl font-extrabold text-green-700 tracking-tight">
                Pharma<span className="text-green-500">Care</span>
              </span>
            </Link>
          </div>

          {/* Card */}
          <div className="bg-white border rounded-2xl shadow-sm p-7 space-y-6">
            {/* Header */}
            <div>
              <h1 className="text-2xl font-extrabold text-slate-800">{title}</h1>
              {subtitle && <p className="text-sm text-slate-500 mt-1.5">{subtitle}</p>}
            </div>

            {/* Form content */}
            {children}
          </div>

          {/* Footer */}
          {footer && (
            <div className="text-center text-sm text-slate-500">{footer}</div>
          )}
        </div>
      </div>
    </div>
  )
}
