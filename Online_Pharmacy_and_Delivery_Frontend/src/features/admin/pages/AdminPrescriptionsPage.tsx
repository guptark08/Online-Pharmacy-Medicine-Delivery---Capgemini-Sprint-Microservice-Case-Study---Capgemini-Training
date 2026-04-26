import { useState, useEffect } from "react"
import { useAdminPrescriptions, useAdminPendingPrescriptions } from "../api/useAdminPrescriptions"
import { useReviewPrescription } from "../api/useReviewPrescription"
import { api } from "@/shared/api/client"
import type { components } from "@/shared/types/api/admin"

type PrescriptionResponseDto = components["schemas"]["PrescriptionResponseDto"]

const STATUS_COLOR: Record<string, string> = {
  PENDING:  "bg-yellow-100 text-yellow-800",
  APPROVED: "bg-green-100 text-green-800",
  REJECTED: "bg-red-100 text-red-800",
}

const IMAGE_TYPES = ["PNG", "JPG", "JPEG", "GIF", "WEBP"]

function isImageType(fileType: string | null | undefined): boolean {
  return IMAGE_TYPES.includes((fileType ?? "").toUpperCase())
}

// Fetches a file via the authenticated Axios client and opens it in a new tab.
async function openFileAuthenticated(fileUrl: string) {
  const response = await api.get(fileUrl, { responseType: "blob" })
  const url = URL.createObjectURL(response.data)
  window.open(url, "_blank")
  // Keep URL alive briefly so the new tab can load it
  setTimeout(() => URL.revokeObjectURL(url), 60_000)
}

function ReviewModal({
  prescription,
  onClose,
}: {
  prescription: PrescriptionResponseDto
  onClose: () => void
}) {
  const [decision, setDecision] = useState<"APPROVED" | "REJECTED">("APPROVED")
  const [notes, setNotes]       = useState("")
  const [blobUrl, setBlobUrl]   = useState<string | null>(null)
  const [fileError, setFileError] = useState(false)
  const review = useReviewPrescription()

  // Fetch the prescription file with the admin JWT on mount
  useEffect(() => {
    if (!prescription.fileUrl) return
    let objectUrl: string | null = null

    api.get(prescription.fileUrl, { responseType: "blob" })
      .then((res) => {
        objectUrl = URL.createObjectURL(res.data)
        setBlobUrl(objectUrl)
      })
      .catch(() => setFileError(true))

    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [prescription.fileUrl])

  const handleSubmit = () => {
    if (!prescription.id) return
    review.mutate(
      { id: prescription.id, decision, rejectionReason: notes || undefined },
      { onSuccess: onClose }
    )
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4 overflow-y-auto">
      <div role="dialog" aria-modal="true" aria-label={`Review Prescription #${prescription.id}`}
        className="bg-white rounded-2xl shadow-xl w-full max-w-2xl space-y-4 p-6 my-4">

        <h2 className="text-lg font-bold text-slate-800">Review Prescription #{prescription.id}</h2>

        <div className="text-sm text-slate-600 space-y-1">
          <p><span className="font-medium">User:</span> {prescription.userEmail}</p>
          <p><span className="font-medium">Uploaded:</span>{" "}
            {prescription.uploadedAt ? new Date(prescription.uploadedAt).toLocaleDateString() : "—"}
          </p>
          {prescription.doctorName && (
            <p><span className="font-medium">Doctor:</span> {prescription.doctorName}</p>
          )}
        </div>

        {/* File preview — fetched via authenticated Axios */}
        {prescription.fileUrl && (
          <div className="border rounded-lg bg-slate-50 overflow-hidden">
            {fileError ? (
              <p className="text-xs text-slate-400 text-center py-6">File unavailable</p>
            ) : !blobUrl ? (
              <div className="h-24 flex items-center justify-center">
                <div className="w-5 h-5 border-2 border-green-600 border-t-transparent rounded-full animate-spin" />
              </div>
            ) : isImageType(prescription.fileType) ? (
              <div className="space-y-2 p-2">
                <img
                  src={blobUrl}
                  alt={`Prescription #${prescription.id}`}
                  className="max-h-80 mx-auto rounded-lg object-contain"
                />
                <div className="text-center">
                  <button
                    onClick={() => window.open(blobUrl, "_blank")}
                    className="text-xs text-green-600 underline"
                  >
                    Open full size ↗
                  </button>
                </div>
              </div>
            ) : (
              <div className="text-center py-8">
                <p className="text-sm text-slate-500 mb-3">📄 {prescription.fileType ?? "Document"}</p>
                <button
                  onClick={() => window.open(blobUrl, "_blank")}
                  className="text-sm text-green-600 underline hover:text-green-800"
                >
                  View / Download File ↗
                </button>
              </div>
            )}
          </div>
        )}

        {/* Decision buttons */}
        <div className="flex gap-2">
          {(["APPROVED", "REJECTED"] as const).map((d) => (
            <button
              key={d}
              onClick={() => setDecision(d)}
              className={`flex-1 py-2 rounded-lg border text-sm font-medium transition-colors ${
                decision === d
                  ? d === "APPROVED"
                    ? "bg-green-600 text-white border-green-600"
                    : "bg-red-600 text-white border-red-600"
                  : "hover:bg-slate-50 text-slate-700"
              }`}
            >
              {d === "APPROVED" ? "✓ Approve" : "✕ Reject"}
            </button>
          ))}
        </div>

        <textarea
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          placeholder={decision === "REJECTED" ? "Rejection reason (required)" : "Notes (optional)"}
          rows={3}
          className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-green-500 resize-none"
        />

        {review.isError && (
          <p className="text-xs text-red-600">Review failed. Please try again.</p>
        )}

        <div className="flex gap-2 pt-1">
          <button
            onClick={onClose}
            className="flex-1 py-2.5 border rounded-lg text-sm font-medium hover:bg-slate-50 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            disabled={review.isPending || (decision === "REJECTED" && !notes.trim())}
            className="flex-1 py-2.5 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {review.isPending ? "Submitting…" : "Submit Review"}
          </button>
        </div>
      </div>
    </div>
  )
}

export default function AdminPrescriptionsPage() {
  const [tab, setTab] = useState<"pending" | "all">("pending")
  const [reviewing, setReviewing] = useState<PrescriptionResponseDto | null>(null)

  const pendingQuery  = useAdminPendingPrescriptions()
  const allQuery      = useAdminPrescriptions()
  const activeQuery   = tab === "pending" ? pendingQuery : allQuery
  const prescriptions = activeQuery.data ?? []

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold text-slate-800">Prescriptions</h1>

      {/* Tabs */}
      <div className="flex gap-1 bg-slate-100 p-1 rounded-lg w-fit">
        {(["pending", "all"] as const).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-1.5 rounded-md text-sm font-medium transition-colors ${
              tab === t ? "bg-white shadow-sm text-slate-900" : "text-slate-500 hover:text-slate-900"
            }`}
          >
            {t === "pending"
              ? `Pending Review (${pendingQuery.data?.length ?? 0})`
              : `All (${allQuery.data?.length ?? 0})`}
          </button>
        ))}
      </div>

      {/* Loading */}
      {activeQuery.isLoading && (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-20 bg-slate-200 rounded-xl animate-pulse" />
          ))}
        </div>
      )}

      {/* Empty State */}
      {(activeQuery.isError || (prescriptions.length === 0 && !activeQuery.isLoading)) && (
        <div className="text-center py-16 text-slate-400 space-y-2">
          <p className="text-4xl mb-2">📋</p>
          <p>{tab === "pending" ? "No prescriptions pending review." : "No prescriptions found."}</p>
          {activeQuery.isError && (
            <p className="text-xs text-red-400">Failed to load prescriptions. Check backend services.</p>
          )}
        </div>
      )}

      <div className="space-y-3">
        {prescriptions.map((rx: PrescriptionResponseDto) => (
          <div key={rx.id} className="bg-white rounded-xl border p-4 flex items-start justify-between gap-4">
            <div className="space-y-1 min-w-0">
              <div className="flex items-center gap-2 flex-wrap">
                <p className="font-semibold text-slate-800">Rx #{rx.id}</p>
                <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${STATUS_COLOR[rx.status ?? ""] ?? "bg-slate-100 text-slate-700"}`}>
                  {rx.status}
                </span>
                {rx.fileType && (
                  <span className="text-[10px] px-2 py-0.5 rounded-full bg-slate-100 text-slate-600">
                    {rx.fileType}
                  </span>
                )}
              </div>
              <p className="text-sm text-slate-600">{rx.userEmail}</p>
              {rx.uploadedAt && (
                <p className="text-xs text-slate-400">
                  Uploaded {new Date(rx.uploadedAt).toLocaleDateString()}
                </p>
              )}
              {rx.rejectionReason && (
                <p className="text-xs text-red-600">Reason: {rx.rejectionReason}</p>
              )}
              {rx.fileUrl && (
                <button
                  onClick={() => openFileAuthenticated(rx.fileUrl!)}
                  className="text-xs text-green-600 underline hover:text-green-800"
                >
                  View File ↗
                </button>
              )}
            </div>

            {rx.status === "PENDING" && (
              <button
                onClick={() => setReviewing(rx)}
                className="shrink-0 px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 transition-colors"
              >
                Review
              </button>
            )}
          </div>
        ))}
      </div>

      {reviewing && (
        <ReviewModal
          prescription={reviewing}
          onClose={() => setReviewing(null)}
        />
      )}
    </div>
  )
}
