import { Outlet } from "react-router-dom"
import Navbar from "./Navbar"

export default function RootLayout() {
  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main>
        <Outlet />
      </main>
    </div>
  )
}
