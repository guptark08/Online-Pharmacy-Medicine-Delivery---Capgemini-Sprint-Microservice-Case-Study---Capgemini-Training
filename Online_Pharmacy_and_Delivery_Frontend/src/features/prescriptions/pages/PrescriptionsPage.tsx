import { useRef } from "react"
import { useMyPrescriptions } from "../api/useMyPrescriptions"
import { useUploadPrescription } from "../api/useUploadPrescription"
import type { components } from "@/shared/types/api/catalog"

type PrescriptionResponseDTO = components["schemas"]["PrescriptionResponseDTO"]

const STATUS_COLOR: Record<string, string> = {
  PENDING:  "bg-yellow-100 text-yellow-800",
  APPROVED: "bg-green-100 text-green-800",
  REJECTED: "bg-red-100 text-red-800",
}

const STATUS_ICON: Record<string, string> = {
  PENDING:  "⏳",
  APPROVED: "✅",
  REJECTED: "❌",
}

export default function PrescriptionsPage() {
  const { data: prescriptions, isLoading } = useMyPrescriptions()
  const upload = useUploadPrescription()
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    upload.mutate(file)
    e.target.value = ""
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-8 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-800">My Prescriptions</h1>
        <button
          onClick={() => fileInputRef.current?.click()}
          disabled={upload.isPending}
          className="px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 disabled:opacity-50 transition-colors"
        >
          {upload.isPending ? "Uploading…" : "+ Upload Rx"}
        </button>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*,.pdf"
          onChange={handleFileChange}
          className="hidden"
        />
      </div>

      {/* Upload result feedback */}
      {upload.isSuccess && (
        <div className="bg-green-50 border border-green-200 rounded-xl p-4 text-sm text-green-700">
          Prescription uploaded successfully. A pharmacist will review it shortly.
        </div>
      )}
      {upload.isError && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-sm text-red-700">
          Upload failed. Please try again with a valid image or PDF.
        </div>
      )}

      {/* Info banner */}
      <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 text-sm text-blue-700">
        <p className="font-semibold mb-1">How it works</p>
        <ol className="list-decimal list-inside space-y-0.5 text-blue-600">
          <li>Upload a clear photo or PDF of your prescription.</li>
          <li>A pharmacist reviews it — usually within a few hours.</li>
          <li>Once <span className="font-semibold text-green-700">Approved</span>, you can link it at checkout for Rx medicines.</li>
        </ol>
      </div>

      {/* Loading */}
      {isLoading && (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="h-20 bg-slate-200 rounded-xl animate-pulse" />
          ))}
        </div>
      )}

      {/* Empty state */}
      {!isLoading && (prescriptions?.length ?? 0) === 0 && (
        <div className="flex flex-col items-center py-20 gap-3 text-slate-400">
          <span className="text-5xl">📋</span>
          <p className="text-sm">No prescriptions yet.</p>
          <p className="text-xs text-center max-w-xs">
            Upload your doctor's prescription to order medicines that require one.
          </p>
        </div>
      )}

      {/* List */}
      <div className="space-y-3">
        {prescriptions?.map((rx: PrescriptionResponseDTO) => (
          <div key={rx.id} className="bg-white border rounded-xl p-4 space-y-2">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <p className="font-semibold text-slate-800">
                    {rx.fileName ? rx.fileName : `Prescription #${rx.id}`}
                  </p>
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${STATUS_COLOR[rx.status ?? ""] ?? "bg-slate-100 text-slate-700"}`}>
                    {STATUS_ICON[rx.status ?? ""]} {rx.status}
                  </span>
                </div>
                {rx.uploadedAt && (
                  <p className="text-xs text-slate-400 mt-0.5">
                    Uploaded {new Date(rx.uploadedAt).toLocaleDateString()}
                  </p>
                )}
                {rx.fileType && (
                  <p className="text-xs text-slate-400">{rx.fileType}</p>
                )}
              </div>
            </div>

            {rx.status === "REJECTED" && rx.reviewNotes && (
              <div className="bg-red-50 border border-red-200 rounded-lg px-3 py-2 text-xs text-red-700">
                <span className="font-medium">Rejection reason:</span> {rx.reviewNotes}
              </div>
            )}

            {rx.status === "APPROVED" && (
              <div className="bg-green-50 rounded-lg px-3 py-2 text-xs text-green-700">
                This prescription is approved and can be used at checkout.
                {rx.reviewedAt && (
                  <span className="text-green-600 ml-1">
                    · Reviewed {new Date(rx.reviewedAt).toLocaleDateString()}
                  </span>
                )}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
