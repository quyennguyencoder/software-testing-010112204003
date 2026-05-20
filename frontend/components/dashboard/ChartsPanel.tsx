"use client"

import React from 'react'
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  CartesianGrid,
  Legend,
} from 'recharts'

type Props = {
  revenueSeries?: { ts: string; value: number }[]
  topProducts?: { name: string; value: number }[]
}

export default function ChartsPanel({ revenueSeries = [], topProducts = [] }: Props) {
  const revenueData = revenueSeries.map((p) => ({ date: p.ts, revenue: p.value }))
  const topData = topProducts.map((p) => ({ name: p.name, value: p.value }))

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div className="bg-white/90 dark:bg-slate-800 p-4 rounded-2xl shadow">
        <h3 className="text-sm font-medium mb-2">Doanh thu theo thời gian</h3>
        <div style={{ width: '100%', height: 220 }}>
          <ResponsiveContainer>
            <LineChart data={revenueData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              <YAxis tickFormatter={(v) => new Intl.NumberFormat('vi-VN').format(v)} />
              <Tooltip formatter={(v: number | undefined) => v !== undefined ? new Intl.NumberFormat('vi-VN').format(v) : ''} />
              <Line type="monotone" dataKey="revenue" stroke="#7c3aed" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="bg-white/90 dark:bg-slate-800 p-4 rounded-2xl shadow">
        <h3 className="text-sm font-medium mb-2">Top sản phẩm (doanh thu)</h3>
        <div style={{ width: '100%', height: 220 }}>
          <ResponsiveContainer>
            <BarChart data={topData} layout="vertical">
              <XAxis type="number" hide />
              <YAxis dataKey="name" type="category" width={140} />
              <Tooltip formatter={(v: number | undefined) => v !== undefined ? new Intl.NumberFormat('vi-VN').format(v) : ''} />
              <Bar dataKey="value" fill="#06b6d4" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}
