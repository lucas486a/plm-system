import api from './index'
import type { AuditLogDTO } from '@/types/auditLog'
import type { PaginatedResponse, PageParams } from '@/types'

export interface AuditLogSearchParams extends PageParams {
  entityType?: string
  action?: string
  userId?: number
  startTime?: string
  endTime?: string
}

export const auditLogApi = {
  getById(id: number) {
    return api.get<AuditLogDTO>(`/audit-logs/${id}`)
  },

  getEntityHistory(entityType: string, entityId: number) {
    return api.get<AuditLogDTO[]>(`/audit-logs/entity/${entityType}/${entityId}`)
  },

  getByEntityType(entityType: string, params?: PageParams) {
    return api.get<PaginatedResponse<AuditLogDTO>>(
      `/audit-logs/entity-type/${entityType}`,
      { params }
    )
  },

  getByUserId(userId: number, params?: PageParams) {
    return api.get<PaginatedResponse<AuditLogDTO>>(
      `/audit-logs/user/${userId}`,
      { params }
    )
  },

  getByAction(action: string, params?: PageParams) {
    return api.get<PaginatedResponse<AuditLogDTO>>(
      `/audit-logs/action/${action}`,
      { params }
    )
  },

  getByEntityTypeAndAction(entityType: string, action: string, params?: PageParams) {
    return api.get<PaginatedResponse<AuditLogDTO>>(
      `/audit-logs/entity-type/${entityType}/action/${action}`,
      { params }
    )
  },

  search(params: AuditLogSearchParams) {
    return api.get<PaginatedResponse<AuditLogDTO>>('/audit-logs/search', { params })
  },

  countByEntityType(entityType: string) {
    return api.get<number>(`/audit-logs/count/entity-type/${entityType}`)
  },

  countByEntityTypeAndEntityId(entityType: string, entityId: number) {
    return api.get<number>(
      `/audit-logs/count/entity-type/${entityType}/entity-id/${entityId}`
    )
  },
}
