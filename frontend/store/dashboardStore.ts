"use client"

import { create } from "zustand"

type Filters = {
  start: string
  end: string
  search: string
  quick?: string
}

type State = {
  filters: Filters
  setFilters: (f: Partial<Filters>) => void
}

const defaultFilters: Filters = {
  start: new Date(Date.now() - 1000*60*60*24*7).toISOString().slice(0,10),
  end: new Date().toISOString().slice(0,10),
  search: "",
}

export const useDashboardStore = create<State>((set)=>({
  filters: defaultFilters,
  setFilters: (f)=> set(state => ({ filters: { ...state.filters, ...f } }))
}))
