import api from './index'
import type { ECODTO, ECOApprovalDTO } from '@/types/eco'
import type { ComponentDraftDTO } from '@/types/bom'
import type { PaginatedResponse, PageParams } from '@/types'

export const ecoApi = {
  // ECO CRUD
  getAll(params?: PageParams) {
    return api.get<PaginatedResponse<ECODTO>>('/ecos', { params })
  },

  getById(id: number) {
    return api.get<ECODTO>(`/ecos/${id}`)
  },

  getByNumber(ecoNumber: string) {
    return api.get<ECODTO>(`/ecos/number/${ecoNumber}`)
  },

  create(data: ECODTO) {
    return api.post<ECODTO>('/ecos', data)
  },

  update(id: number, data: ECODTO) {
    return api.put<ECODTO>(`/ecos/${id}`, data)
  },

  delete(id: number) {
    return api.delete<void>(`/ecos/${id}`)
  },

  // ECR-to-ECO Conversion
  convertFromECR(ecrId: number) {
    return api.post<ECODTO>(`/ecrs/${ecrId}/convert-to-eco`)
  },

  // Workflow Operations
  submit(id: number) {
    return api.post<ECODTO>(`/ecos/${id}/submit`)
  },

  approve(id: number, approverId: number, stage: string, comments?: string) {
    return api.post<ECOApprovalDTO>(`/ecos/${id}/approve`, { approverId, stage, comments })
  },

  reject(id: number, approverId: number, stage: string, comments?: string) {
    return api.post<ECOApprovalDTO>(`/ecos/${id}/reject`, { approverId, stage, comments })
  },

  apply(id: number) {
    return api.post<ECODTO>(`/ecos/${id}/apply`)
  },

  close(id: number) {
    return api.post<ECODTO>(`/ecos/${id}/close`)
  },

  // Component Drafts
  getComponentDrafts(id: number) {
    return api.get<ComponentDraftDTO[]>(`/ecos/${id}/component-drafts`)
  },

  addComponentDraft(id: number, data: ComponentDraftDTO) {
    return api.post<ComponentDraftDTO>(`/ecos/${id}/component-drafts`, data)
  },
}
