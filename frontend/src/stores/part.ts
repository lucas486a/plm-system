import { defineStore } from 'pinia'
import { ref } from 'vue'
import { partApi } from '@/api/partApi'
import type { PartDTO, PartRevisionDTO } from '@/types/part'
import type { PaginatedResponse, PageParams } from '@/types'

export const usePartStore = defineStore('part', () => {
  // State
  const parts = ref<PartDTO[]>([])
  const currentPart = ref<PartDTO | null>(null)
  const revisions = ref<PartRevisionDTO[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const pagination = ref({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  // Actions
  async function fetchParts(params?: PageParams): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await partApi.getAll({
        page: (params?.page ?? 1) - 1, // Backend uses 0-based indexing
        size: params?.size ?? 10,
        sort: params?.sort,
      })
      const data: PaginatedResponse<PartDTO> = response.data
      parts.value = data.content
      pagination.value = {
        current: data.number + 1,
        pageSize: data.size,
        total: data.totalElements,
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch parts'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function searchParts(query: string, params?: PageParams): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await partApi.search(query, {
        page: (params?.page ?? 1) - 1,
        size: params?.size ?? 10,
        sort: params?.sort,
      })
      const data: PaginatedResponse<PartDTO> = response.data
      parts.value = data.content
      pagination.value = {
        current: data.number + 1,
        pageSize: data.size,
        total: data.totalElements,
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to search parts'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchPartById(id: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await partApi.getById(id)
      currentPart.value = response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch part'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createPart(data: PartDTO): Promise<PartDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await partApi.create(data)
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to create part'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updatePart(id: number, data: PartDTO): Promise<PartDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await partApi.update(id, data)
      if (currentPart.value?.id === id) {
        currentPart.value = response.data
      }
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to update part'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deletePart(id: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      await partApi.delete(id)
      parts.value = parts.value.filter((p) => p.id !== id)
      if (currentPart.value?.id === id) {
        currentPart.value = null
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to delete part'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchRevisions(partId: number, params?: PageParams): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await partApi.getRevisions(partId, {
        page: (params?.page ?? 1) - 1,
        size: params?.size ?? 10,
        sort: params?.sort,
      })
      const data: PaginatedResponse<PartRevisionDTO> = response.data
      revisions.value = data.content
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch revisions'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createRevision(partId: number, data: PartRevisionDTO): Promise<PartRevisionDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await partApi.createRevision(partId, data)
      revisions.value.unshift(response.data)
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to create revision'
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
    parts,
    currentPart,
    revisions,
    loading,
    error,
    pagination,
    // Actions
    fetchParts,
    searchParts,
    fetchPartById,
    createPart,
    updatePart,
    deletePart,
    fetchRevisions,
    createRevision,
    clearError,
  }
})
