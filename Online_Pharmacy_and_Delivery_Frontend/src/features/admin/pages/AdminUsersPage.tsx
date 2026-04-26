import { useState } from "react"
import { useAdminUsers } from "../api/useAdminUsers"
import type { components } from "@/shared/types/api/auth"

type UserResponse = components["schemas"]["UserResponse"]

const ROLE_COLOR: Record<string, string> = {
  ADMIN:    "bg-purple-100 text-purple-800",
  CUSTOMER: "bg-blue-100 text-blue-800",
}

export default function AdminUsersPage() {
  const { data: users, isLoading } = useAdminUsers()
  const [search, setSearch] = useState("")

  const filtered = (users ?? []).filter((u) =>
    !search ||
    u.username?.toLowerCase().includes(search.toLowerCase()) ||
    u.email?.toLowerCase().includes(search.toLowerCase()) ||
    u.name?.toLowerCase().includes(search.toLowerCase())
  )

  if (isLoading) {
    return (
      <div className="p-6 space-y-3">
        {Array.from({ length: 5 }).map((_, i) => (
          <div key={i} className="h-14 bg-slate-200 rounded-xl animate-pulse" />
        ))}
      </div>
    )
  }

  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center justify-between gap-3 flex-wrap">
        <h1 className="text-2xl font-bold text-slate-800">Users</h1>
        <span className="text-sm text-slate-500">{users?.length ?? 0} total</span>
      </div>

      <input
        type="text"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        placeholder="Search by name, username or email…"
        aria-label="Search users"
        className="w-full max-w-sm px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-green-500 bg-white"
      />

      <div className="bg-white rounded-xl border overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 border-b">
            <tr>
              <th className="px-4 py-3 text-left font-semibold text-slate-600">User</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-600 hidden md:table-cell">Email</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-600 hidden sm:table-cell">Mobile</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-600">Role</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-600 hidden lg:table-cell">Verified</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-600">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {filtered.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-slate-400">
                  No users found.
                </td>
              </tr>
            )}
            {filtered.map((user: UserResponse) => (
              <tr key={user.id} className="hover:bg-slate-50 transition-colors">
                <td className="px-4 py-3">
                  <p className="font-medium text-slate-800">{user.name}</p>
                  <p className="text-xs text-slate-400">@{user.username}</p>
                </td>
                <td className="px-4 py-3 text-slate-600 hidden md:table-cell">{user.email}</td>
                <td className="px-4 py-3 text-slate-600 hidden sm:table-cell">{user.mobile ?? "—"}</td>
                <td className="px-4 py-3">
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${ROLE_COLOR[user.role ?? ""] ?? "bg-slate-100 text-slate-700"}`}>
                    {user.role}
                  </span>
                </td>
                <td className="px-4 py-3 hidden lg:table-cell">
                  {user.emailVerified ? (
                    <span className="text-green-600 text-xs">✓ Verified</span>
                  ) : (
                    <span className="text-amber-600 text-xs">Pending</span>
                  )}
                </td>
                <td className="px-4 py-3">
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${user.status ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"}`}>
                    {user.status ? "Active" : "Inactive"}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
