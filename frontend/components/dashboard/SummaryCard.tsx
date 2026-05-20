import React from "react"

type Props = {
  title: string
  value: string
  trend?: number
  spark?: number[]
}

export default function SummaryCard({ title, value, trend, spark }: Props) {
  const trendClass = trend && trend >= 0 ? "text-green-600" : "text-red-600"

  return (
    <div className="bg-white/90 dark:bg-slate-800 p-4 rounded-2xl shadow flex items-start justify-between">
      <div>
        <p className="text-sm text-slate-500">{title}</p>
        <p className="text-2xl font-semibold mt-1">{value}</p>
        {typeof trend === "number" && (
          <p className={`text-sm mt-1 ${trendClass}`}>{trend >= 0 ? `+${trend}%` : `${trend}%`}</p>
        )}
      </div>

      <div className="w-24 h-12">
        <svg viewBox="0 0 100 40" className="w-full h-full">
          <polyline
            fill="none"
            stroke="#7c3aed"
            strokeWidth={2}
            points={
              (spark || [10, 20, 15, 28, 22])
                .map((v, i) => `${(i * 100) / ((spark || []).length || 4)},${40 - v}`)
                .join(" ")
            }
          />
        </svg>
      </div>
    </div>
  )
}
