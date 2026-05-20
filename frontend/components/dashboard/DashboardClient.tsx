"use client"

import React from "react"
import { cn } from "../../lib/utils"
import FiltersPanel from "./FiltersPanel"
import SummaryCard from "./SummaryCard"
import OrdersTable from "./OrdersTable"
import ChartsPanel from "./ChartsPanel"
import { useDashboardStore } from "../../store/dashboardStore"
import { useOverview, useOrders } from "../../lib/dashboardApi"
import { sampleDashboard } from "../../lib/dashboardMock"

export default function DashboardClient() {
  const { filters, setFilters } = useDashboardStore()

  const overview = useOverview()
  const ordersHook = useOrders({ page: 1, size: 25, search: filters.search })

  const metrics = overview.loading || !overview.data ? sampleDashboard.metrics : [
    { id: 'orders', title: 'Tổng đơn', value: String(overview.data.totalOrders ?? 0), trend: 0, spark: (overview.data.sparklineRevenue || []).map((s:any)=>s.value) },
    { id: 'revenue', title: 'Doanh thu', value: `₫ ${Number(overview.data.totalRevenue ?? 0).toLocaleString()}`, trend: 0, spark: (overview.data.sparklineRevenue || []).map((s:any)=>s.value) },
    { id: 'aov', title: 'Giá trị TB giỏ hàng', value: `₫ ${Number(overview.data.averageOrderValue ?? 0).toLocaleString()}`, trend: 0, spark: (overview.data.sparklineRevenue || []).map((s:any)=>s.value) },
    { id: 'abandon', title: 'Tỉ lệ bỏ giỏ', value: `${overview.data.abandonedRate ?? 'N/A'}%`, trend: 0, spark: (overview.data.sparklineRevenue || []).map((s:any)=>s.value) }
  ]

  const orders = ordersHook.data?.content ?? ordersHook.data ?? sampleDashboard.orders

  return (
    <div className={cn("p-6 space-y-6")}>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Dashboard - Giỏ hàng</h1>
        <div className="text-sm text-slate-500">Range: {filters.start} → {filters.end}</div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {metrics.map((m) => (
          <SummaryCard key={m.id} title={m.title} value={m.value} trend={m.trend} spark={m.spark} />
        ))}
      </div>

      <div>
        <ChartsPanel revenueSeries={overview.data?.sparklineRevenue} topProducts={overview.data?.topProducts?.map((p:any)=>({ name: p.name, value: p.revenue }))} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-white/80 dark:bg-slate-800 p-4 rounded-2xl shadow">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-lg font-medium">Orders</h2>
              <div className="flex gap-2">
                <button className="px-3 py-1 rounded bg-slate-100 text-sm" onClick={() => setFilters({ ...filters, search: "" })}>Reset</button>
              </div>
            </div>
            <OrdersTable orders={orders} loading={ordersHook.loading} />
            {ordersHook.error && <div className="text-sm text-red-600 mt-2">Lỗi khi tải orders: {String(ordersHook.error)}</div>}
          </div>
        </div>

        <aside className="space-y-4">
          <div className="bg-white/80 dark:bg-slate-800 p-4 rounded-2xl shadow">
            <FiltersPanel />
          </div>
          <div className="bg-white/80 dark:bg-slate-800 p-4 rounded-2xl shadow">
            <h3 className="text-sm font-medium">Insights</h3>
            <ul className="mt-2 text-sm text-slate-600 space-y-2">
              <li>- Top product: {sampleDashboard.topProduct}</li>
              <li>- Abandon rate: {metrics.find(m=>m.id==='abandon')?.value}</li>
            </ul>
          </div>
        </aside>
      </div>
    </div>
  )
}
