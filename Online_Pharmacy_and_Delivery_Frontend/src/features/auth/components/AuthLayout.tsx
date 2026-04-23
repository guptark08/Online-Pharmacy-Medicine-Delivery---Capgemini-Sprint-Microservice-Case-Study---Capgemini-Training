import type { ReactNode } from "react"
import { Link } from "react-router-dom"

interface AuthLayoutProps {
  title: string
  subtitle?: string
  children: ReactNode
  /** Optional link shown beneath the card, e.g. "Don't have an account? Sign up" */
  footer?: ReactNode
}

export default function AuthLayout({ title, subtitle, children, footer }: AuthLayoutProps) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md space-y-6">
        <div className="text-center">
          <Link to="/" className="text-2xl font-bold">Pharmacy</Link>
        </div>

        <div className="rounded-lg border bg-white p-6 shadow-sm space-y-4">
          <div>
            <h1 className="text-2xl font-semibold">{title}</h1>
            {subtitle && <p className="text-sm text-slate-500 mt-1">{subtitle}</p>}
          </div>

          {children}
        </div>

        {footer && (
          <div className="text-center text-sm text-slate-600">{footer}</div>
        )}
      </div>
    </div>
  )
}