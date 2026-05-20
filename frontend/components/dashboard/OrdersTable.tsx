import React, { useState } from "react"

type OrderItem = {
  sku: string
  name: string
  qty: number
  price: number
}

type Order = {
  id: string
  customer: { name: string; email: string }
  items: OrderItem[]
  total: number
  status: string
  createdAt: string
}

export default function OrdersTable({ orders, loading }: { orders?: Order[]; loading?: boolean }) {
  const [page, setPage] = useState(1)
  const pageSize = 8
  const start = (page - 1) * pageSize
  const pageData = (orders || []).slice(start, start + pageSize)
  const total = orders?.length ?? 0

  return (
    <div>
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-slate-500">
            <tr>
              <th className="py-2">Order</th>
              <th>Customer</th>
              <th>Items</th>
              <th>Total</th>
              <th>Status</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            {loading
              ? Array.from({ length: Math.min(pageSize, (orders || []).length || pageSize) }).map((_, idx) => (
                  <tr key={idx} className="border-t">
                    <td className="py-3"><div className="h-4 bg-slate-200 rounded w-20 animate-pulse" /></td>
                    <td><div className="h-4 bg-slate-200 rounded w-32 animate-pulse" /></td>
                    <td><div className="h-4 bg-slate-200 rounded w-6 animate-pulse" /></td>
                    <td><div className="h-4 bg-slate-200 rounded w-24 animate-pulse" /></td>
                    <td><div className="h-4 bg-slate-200 rounded w-16 animate-pulse" /></td>
                    <td className="text-xs text-slate-500"><div className="h-4 bg-slate-200 rounded w-20 animate-pulse" /></td>
                  </tr>
                ))
              : pageData.map((o) => (
                  <tr key={o.id} className="border-t">
                    <td className="py-3">{o.id}</td>
                    <td>{o.customer.name}</td>
                    <td>{o.items.reduce((s, it) => s + it.qty, 0)}</td>
                    <td>{o.total.toLocaleString()} đ</td>
                    <td>
                      <span className={`px-2 py-1 rounded-full text-xs ${o.status === 'COMPLETED' ? 'bg-green-100 text-green-700' : o.status==='PENDING' ? 'bg-yellow-100 text-yellow-700' : 'bg-slate-100 text-slate-700'}`}>
                        {o.status}
                      </span>
                    </td>
                    <td className="text-xs text-slate-500">{o.createdAt}</td>
                  </tr>
                ))}
          </tbody>
        </table>
      </div>

      <div className="flex items-center justify-between mt-3 text-sm text-slate-500">
        <div>Showing {total === 0 ? 0 : start + 1}-{Math.min(start + pageSize, total)} of {total}</div>
        <div className="flex gap-2">
          <button disabled={page===1} onClick={()=>setPage(p=>Math.max(1,p-1))} className="px-2 py-1 rounded bg-slate-100">Prev</button>
          <button disabled={start+pageSize>=total} onClick={()=>setPage(p=>p+1)} className="px-2 py-1 rounded bg-slate-100">Next</button>
        </div>
      </div>
    </div>
  )
}
