import { useState } from "react"
import { Outlet, useNavigate } from "react-router-dom"
import Navbar from "./Navbar"
import { useApprovedUnnotified } from "@/features/prescriptions/api/useApprovedUnnotified"
import { useMarkNotified } from "@/features/prescriptions/api/useMarkNotified"
import { useAuthStore } from "@/shared/stores/authStore"
import type { components } from "@/shared/types/api/catalog"

type PrescriptionResponseDTO = components["schemas"]["PrescriptionResponseDTO"]

function ApprovalNotificationModal() {
  const { data: approvals } = useApprovedUnnotified()
  const markNotified = useMarkNotified()
  const navigate = useNavigate()
  const [dismissed, setDismissed] = useState(false)

  const visible = !dismissed && !!approvals && approvals.length > 0

  const handleDismiss = () => {
    approvals?.forEach((rx: PrescriptionResponseDTO) => {
      if (rx.id != null) markNotified.mutate(rx.id)
    })
    setDismissed(true)
  }

  const handleGoToCart = () => {
    handleDismiss()
    navigate("/cart")
  }

  const handleViewPrescriptions = () => {
    handleDismiss()
    navigate("/prescriptions")
  }

  if (!visible) return null

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Prescription Approved"
        className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6 space-y-4"
      >
        <div className="flex items-start gap-3">
          <span className="text-3xl">✅</span>
          <div>
            <h2 className="font-bold text-slate-800 text-lg">Prescription Approved!</h2>
            <p className="text-sm text-slate-500 mt-0.5">
              {approvals!.length === 1
                ? "Your prescription has been approved by our pharmacist."
                : `${approvals!.length} of your prescriptions have been approved.`}
            </p>
          </div>
        </div>

        <div className="space-y-2">
          {approvals!.map((rx: PrescriptionResponseDTO) => (
            <div key={rx.id} className="bg-green-50 border border-green-200 rounded-lg px-3 py-2 text-sm text-green-800">
              {rx.fileName ?? `Prescription #${rx.id}`}
            </div>
          ))}
        </div>

        <p className="text-sm text-slate-600">
          You can now use your approved prescription to complete your order.
        </p>

        <div className="flex gap-2 pt-1">
          <button
            onClick={handleGoToCart}
            className="flex-1 py-2.5 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 transition-colors"
          >
            Go to Cart
          </button>
          <button
            onClick={handleViewPrescriptions}
            className="flex-1 py-2.5 border text-sm font-medium rounded-lg hover:bg-slate-50 transition-colors"
          >
            View Prescriptions
          </button>
        </div>
        <button
          onClick={handleDismiss}
          className="w-full text-xs text-slate-400 hover:text-slate-600 transition-colors"
        >
          Dismiss
        </button>
      </div>
    </div>
  )
}

export default function RootLayout() {
  const accessToken = useAuthStore((s) => s.accessToken)

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main>
        <Outlet />
      </main>
      {accessToken && <ApprovalNotificationModal />}
    </div>
  )
}
