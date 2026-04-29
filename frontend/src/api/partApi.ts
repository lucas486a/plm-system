import api from './index'
import type { PartDTO, PartRevisionDTO } from '@/types/part'
import type { PaginatedResponse, PageParams } from '@/types'

export const partApi = {
  // Part CRUD
  getAll(params?: PageParams) {
    return api.get<PaginatedResponse<PartDTO>>('/parts', { params })
  },

  getById(id: number) {
    return api.get<PartDTO>(`/parts/${id}`)
  },

  getByNumber(partNumber: string) {
    return api.get<PartDTO>(`/parts/number/${partNumber}`)
  },

  create(data: PartDTO) {
    return api.post<PartDTO>('/parts', data)
  },

  update(id: number, data: PartDTO) {
    return api.put<PartDTO>(`/parts/${id}`, data)
  },

  delete(id: number) {
    return api.delete<void>(`/parts/${id}`)
  },

  search(query: string, params?: PageParams) {
    return api.get<PaginatedResponse<PartDTO>>('/parts/search', {
      params: { query, ...params },
    })
  },

  // Part Revisions
  getRevisions(partId: number, params?: PageParams) {
    return api.get<PaginatedResponse<PartRevisionDTO>>(`/parts/${partId}/revisions`, { params })
  },

  createRevision(partId: number, data: PartRevisionDTO) {
    return api.post<PartRevisionDTO>(`/parts/${partId}/revisions`, data)
  },
}
