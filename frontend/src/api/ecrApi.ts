import api from './index'
import type { ECRDTO } from '@/types/ecr'
import type { PartDTO } from '@/types/part'
import type { PaginatedResponse, PageParams } from '@/types'

export const ecrApi = {
  // ECR CRUD
  getAll(params?: PageParams) {
    return api.get<PaginatedResponse<ECRDTO>>('/ecrs', { params })
  },

  getById(id: number) {
    return api.get<ECRDTO>(`/ecrs/${id}`)
  },

  getByNumber(ecrNumber: string) {
    return api.get<ECRDTO>(`/ecrs/number/${ecrNumber}`)
  },

  create(data: ECRDTO) {
    return api.post<ECRDTO>('/ecrs', data)
  },

  update(id: number, data: ECRDTO) {
    return api.put<ECRDTO>(`/ecrs/${id}`, data)
  },

  delete(id: number) {
    return api.delete<void>(`/ecrs/${id}`)
  },

  // Status Management
  submit(id: number) {
    return api.post<ECRDTO>(`/ecrs/${id}/submit`)
  },

  evaluate(id: number) {
    return api.post<ECRDTO>(`/ecrs/${id}/evaluate`)
  },

  approve(id: number) {
    return api.post<ECRDTO>(`/ecrs/${id}/approve`)
  },

  reject(id: number) {
    return api.post<ECRDTO>(`/ecrs/${id}/reject`)
  },

  // Assignment
  assign(id: number, userId: number) {
    return api.post<ECRDTO>(`/ecrs/${id}/assign`, null, { params: { userId } })
  },

  // Affected Parts
  getAffectedParts(id: number) {
    return api.get<PartDTO[]>(`/ecrs/${id}/affected-parts`)
  },

  addAffectedPart(id: number, partId: number) {
    return api.post<PartDTO>(`/ecrs/${id}/affected-parts`, null, { params: { partId } })
  },

  removeAffectedPart(id: number, partId: number) {
    return api.delete<void>(`/ecrs/${id}/affected-parts/${partId}`)
  },
}
