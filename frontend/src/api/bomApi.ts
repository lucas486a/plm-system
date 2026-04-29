import api from './index'
import type {
  BOMDTO,
  BOMItemDTO,
  BOMExplodeNode,
  BOMComparisonResult,
  BOMSnapshot,
} from '@/types/bom'
import type { PaginatedResponse, PageParams } from '@/types'

export const bomApi = {
  // BOM CRUD
  getAll(params?: PageParams) {
    return api.get<PaginatedResponse<BOMDTO>>('/boms', { params })
  },

  getById(id: number) {
    return api.get<BOMDTO>(`/boms/${id}`)
  },

  create(data: BOMDTO) {
    return api.post<BOMDTO>('/boms', data)
  },

  update(id: number, data: BOMDTO) {
    return api.put<BOMDTO>(`/boms/${id}`, data)
  },

  delete(id: number) {
    return api.delete<void>(`/boms/${id}`)
  },

  // BOM Items
  getItems(bomId: number) {
    return api.get<BOMItemDTO[]>(`/boms/${bomId}/items`)
  },

  addItem(bomId: number, data: BOMItemDTO) {
    return api.post<BOMItemDTO>(`/boms/${bomId}/items`, data)
  },

  updateItem(bomId: number, itemId: number, data: BOMItemDTO) {
    return api.put<BOMItemDTO>(`/boms/${bomId}/items/${itemId}`, data)
  },

  removeItem(bomId: number, itemId: number) {
    return api.delete<void>(`/boms/${bomId}/items/${itemId}`)
  },

  // BOM Operations
  explode(id: number) {
    return api.get<BOMExplodeNode[]>(`/boms/${id}/explode`)
  },

  whereUsed(partId: number) {
    return api.get<BOMDTO[]>(`/parts/${partId}/where-used`)
  },

  copy(id: number, newName: string) {
    return api.post<BOMDTO>(`/boms/${id}/copy`, null, { params: { newName } })
  },

  compare(bom1: number, bom2: number) {
    return api.get<BOMComparisonResult>('/boms/compare', { params: { bom1, bom2 } })
  },

  // BOM Export/Import
  exportCsv(id: number) {
    return api.get<string>(`/boms/${id}/export`, { params: { format: 'csv' } })
  },

  importCsv(csvData: string, assemblyId: number, bomName: string) {
    return api.post<BOMDTO>('/boms/import', csvData, {
      params: { assemblyId, bomName },
    })
  },

  // BOM Snapshots
  getSnapshots(id: number) {
    return api.get<BOMSnapshot[]>(`/boms/${id}/snapshots`)
  },

  createSnapshot(id: number, label?: string) {
    return api.post<BOMSnapshot>(`/boms/${id}/snapshots`, null, {
      params: { label },
    })
  },

  // BOM Cost
  calculateCost(id: number) {
    return api.get<number>(`/boms/${id}/cost`)
  },
}
