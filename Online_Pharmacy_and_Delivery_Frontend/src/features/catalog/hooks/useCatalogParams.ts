import { useCallback, useMemo } from "react"
import { useSearchParams } from "react-router-dom"
import type { MedicinesParams } from "../api/useMedicines"

// What the UI works with — 1-indexed pages, typed values
export interface CatalogUIParams {
  keyword: string
  categoryId: number | undefined
  requiresPrescription: boolean | undefined
  page: number   // 1-indexed (what the user sees)
  size: number
  sortBy: string
}

const DEFAULTS = {
  page: 1,
  size: 12,
  sortBy: "name",
}

export function useCatalogParams() {
  const [searchParams, setSearchParams] = useSearchParams()

  const params: CatalogUIParams = {
    keyword:              searchParams.get("keyword") ?? "",
    categoryId:           searchParams.has("categoryId") ? Number(searchParams.get("categoryId")) : undefined,
    requiresPrescription: searchParams.has("rx") ? searchParams.get("rx") === "true" : undefined,
    page:                 searchParams.has("page") ? Number(searchParams.get("page")) : DEFAULTS.page,
    size:                 searchParams.has("size") ? Number(searchParams.get("size")) : DEFAULTS.size,
    sortBy:               searchParams.get("sortBy") ?? DEFAULTS.sortBy,
  }

  // Every setter resets page to 1 except setPage itself
  const setKeyword = useCallback(
    (keyword: string) =>
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev)
        const prevKeyword = prev.get("keyword") ?? ""
        keyword ? next.set("keyword", keyword) : next.delete("keyword")
        // Only reset to page 1 when the keyword actually changes.
        // Without this guard, re-renders where setKeyword fires with the same
        // value would reset the page — breaking pagination.
        if (keyword !== prevKeyword) next.set("page", "1")
        return next
      }),
    [setSearchParams]
  )

  const setCategoryId = useCallback(
    (id: number | undefined) =>
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev)
        id != null ? next.set("categoryId", String(id)) : next.delete("categoryId")
        next.set("page", "1")
        return next
      }),
    [setSearchParams]
  )

  const setRequiresPrescription = useCallback(
    (rx: boolean | undefined) =>
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev)
        rx != null ? next.set("rx", String(rx)) : next.delete("rx")
        next.set("page", "1")
        return next
      }),
    [setSearchParams]
  )

  const setPage = useCallback(
    (page: number) =>
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev)
        next.set("page", String(page))
        return next
      }),
    [setSearchParams]
  )

  const setSortBy = useCallback(
    (sortBy: string) =>
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev)
        next.set("sortBy", sortBy)
        next.set("page", "1")
        return next
      }),
    [setSearchParams]
  )

  // Convert UI params → API params.
  // Key translation: UI uses 1-indexed pages, backend is 0-indexed.
  const apiParams: MedicinesParams = useMemo(
    () => ({
      keyword:              params.keyword || undefined,
      categoryId:           params.categoryId,
      requiresPrescription: params.requiresPrescription,
      page:                 params.page - 1,  // ← the only place this conversion lives
      size:                 params.size,
      sortBy:               params.sortBy,
    }),
    [params.keyword, params.categoryId, params.requiresPrescription, params.page, params.size, params.sortBy]
  )

  return { params, apiParams, setKeyword, setCategoryId, setRequiresPrescription, setPage, setSortBy }
}
