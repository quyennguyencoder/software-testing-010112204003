import { useEffect, useState } from 'react'
import { dashboardAPI, getAuthToken } from './api'

type OverviewState = {
  data: any | null
  loading: boolean
  error: any | null
}

export async function fetchOrdersRaw(params: Record<string, any> = {}) {
  const base = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api/v1'
  const qs = new URLSearchParams(params as Record<string, string>).toString()
  const token = getAuthToken()

  const headers: Record<string,string> = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`

  const url = `${base}/admin/dashboard/orders${qs ? `?${qs}` : ''}`

  try {
    const res = await fetch(url, { headers })
    if (!res.ok) {
      // fallback to recent-orders endpoint
      const fallback = await dashboardAPI.getRecentOrders(params.limit || 10)
      return fallback
    }
    const json = await res.json()
    return json
  } catch (err) {
    // fallback
    return dashboardAPI.getRecentOrders(params.limit || 10)
  }
}

export function useOverview() {
  const [state, setState] = useState<OverviewState>({ data: null, loading: true, error: null })

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setState({ data: null, loading: true, error: null })
      try {
        const res = await dashboardAPI.getOverview()
        if (!mounted) return
        setState({ data: res.data ?? null, loading: false, error: null })
      } catch (error) {
        if (!mounted) return
        setState({ data: null, loading: false, error })
      }
    })()
    return () => { mounted = false }
  }, [])

  return state
}

export function useOrders(params: { page?: number; size?: number; search?: string; status?: string } = {}) {
  const [data, setData] = useState<any | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<any | null>(null)

  useEffect(() => {
    let mounted = true
    setLoading(true)
    setError(null)

    ;(async () => {
      try {
        const res = await fetchOrdersRaw({ page: params.page ?? 1, size: params.size ?? 25, search: params.search ?? '', status: params.status ?? '' })
        if (!mounted) return
        // normalize both ApiResponse and direct JSON
        setData(res.data ?? res)
        setLoading(false)
      } catch (err) {
        if (!mounted) return
        setError(err)
        setLoading(false)
      }
    })()

    return () => { mounted = false }
  }, [params.page, params.size, params.search, params.status])

  return { data, loading, error }
}

export default {
  fetchOrdersRaw,
  useOverview,
  useOrders,
}
