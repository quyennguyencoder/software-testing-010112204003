import React from "react"
import DashboardClient from "../../../components/dashboard/DashboardClient"

export const metadata = {
  title: 'Dashboard - Giỏ hàng'
}

export default function Page() {
  return (
    <main className="min-h-screen bg-slate-50 dark:bg-slate-900">
      <div className="max-w-7xl mx-auto">
        <DashboardClient />
      </div>
    </main>
  )
}
