import { defineStore } from 'pinia'
import { ref } from 'vue'
import { auditLogApi } from '@/api/auditLogApi'
import type { AuditLogSearchParams } from '@/api/auditLogApi'
import type { AuditLogDTO } from '@/types/auditLog'
import type { PaginatedResponse } from '@/types'

export const useAuditLogStore = defineStore('auditLog', () => {
  // State
  const auditLogs = ref<AuditLogDTO[]>([])
  const currentAuditLog = ref<AuditLogDTO | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const pagination = ref({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  // Actions
  async function fetchAuditLogs(params?: AuditLogSearchParams): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await auditLogApi.search({
        page: (params?.page ?? 1) - 1, // Backend uses 0-based indexing
        size: params?.size ?? 10,
        sort: params?.sort,
        entityType: params?.entityType,
        action: params?.action,
        userId: params?.userId,
        startTime: params?.startTime,
        endTime: params?.endTime,
      })
      const data: PaginatedResponse<AuditLogDTO> = response.data
      auditLogs.value = data.content
      pagination.value = {
        current: data.number + 1,
        pageSize: data.size,
        total: data.totalElements,
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch audit logs'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchAuditLogById(id: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await auditLogApi.getById(id)
      currentAuditLog.value = response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch audit log'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchEntityHistory(entityType: string, entityId: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await auditLogApi.getEntityHistory(entityType, entityId)
      auditLogs.value = response.data
      pagination.value = {
        current: 1,
        pageSize: response.data.length,
        total: response.data.length,
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch entity history'
      throw err
    } finally {
      loading.value = false
    }
  }

  function clearError(): void {
    error.value = null
  }

  return {
    // State
    auditLogs,
    currentAuditLog,
    loading,
    error,
    pagination,
    // Actions
    fetchAuditLogs,
    fetchAuditLogById,
    fetchEntityHistory,
    clearError,
  }
})
