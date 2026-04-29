import { defineStore } from 'pinia'
import { ref } from 'vue'
import { bomApi } from '@/api/bomApi'
import type { BOMDTO, BOMItemDTO, BOMExplodeNode } from '@/types/bom'
import type { PaginatedResponse, PageParams } from '@/types'

export const useBomStore = defineStore('bom', () => {
  // BOM list state
  const boms = ref<BOMDTO[]>([])
  const totalBoms = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Current BOM detail state
  const currentBom = ref<BOMDTO | null>(null)
  const bomItems = ref<BOMItemDTO[]>([])
  const bomTree = ref<BOMExplodeNode[]>([])

  // Where-used state
  const whereUsedBoms = ref<BOMDTO[]>([])

  // Pagination
  const currentPage = ref(0)
  const pageSize = ref(10)

  // Fetch all BOMs with pagination
  async function fetchBoms(params?: PageParams): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await bomApi.getAll({
        page: params?.page ?? currentPage.value,
        size: params?.size ?? pageSize.value,
        sort: params?.sort,
      })
      const data = response.data as PaginatedResponse<BOMDTO>
      boms.value = data.content
      totalBoms.value = data.totalElements
      currentPage.value = data.number
      pageSize.value = data.size
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch BOMs'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Fetch single BOM by ID
  async function fetchBomById(id: number): Promise<BOMDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await bomApi.getById(id)
      currentBom.value = response.data
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch BOM'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Create new BOM
  async function createBom(data: BOMDTO): Promise<BOMDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await bomApi.create(data)
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to create BOM'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Update existing BOM
  async function updateBom(id: number, data: BOMDTO): Promise<BOMDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await bomApi.update(id, data)
      if (currentBom.value?.id === id) {
        currentBom.value = response.data
      }
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to update BOM'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Delete BOM
  async function deleteBom(id: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      await bomApi.delete(id)
      if (currentBom.value?.id === id) {
        currentBom.value = null
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to delete BOM'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Fetch BOM items
  async function fetchBomItems(bomId: number): Promise<BOMItemDTO[]> {
    loading.value = true
    error.value = null

    try {
      const response = await bomApi.getItems(bomId)
      bomItems.value = response.data
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch BOM items'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Add BOM item
  async function addBomItem(bomId: number, data: BOMItemDTO): Promise<BOMItemDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await bomApi.addItem(bomId, data)
      bomItems.value.push(response.data)
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to add BOM item'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Update BOM item
  async function updateBomItem(bomId: number, itemId: number, data: BOMItemDTO): Promise<BOMItemDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await bomApi.updateItem(bomId, itemId, data)
      const index = bomItems.value.findIndex((item) => item.id === itemId)
      if (index !== -1) {
        bomItems.value[index] = response.data
      }
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to update BOM item'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Remove BOM item
  async function removeBomItem(bomId: number, itemId: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      await bomApi.removeItem(bomId, itemId)
      bomItems.value = bomItems.value.filter((item) => item.id !== itemId)
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to remove BOM item'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Fetch BOM tree (explode)
  async function fetchBomTree(bomId: number): Promise<BOMExplodeNode[]> {
    loading.value = true
    error.value = null

    try {
      const response = await bomApi.explode(bomId)
      bomTree.value = response.data
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch BOM tree'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Fetch where-used BOMs for a part
  async function fetchWhereUsed(partId: number): Promise<BOMDTO[]> {
    loading.value = true
    error.value = null

    try {
      const response = await bomApi.whereUsed(partId)
      whereUsedBoms.value = response.data
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch where-used data'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Copy BOM
  async function copyBom(id: number, newName: string): Promise<BOMDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await bomApi.copy(id, newName)
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to copy BOM'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Clear error
  function clearError(): void {
    error.value = null
  }

  // Reset state
  function resetState(): void {
    boms.value = []
    totalBoms.value = 0
    currentBom.value = null
    bomItems.value = []
    bomTree.value = []
    whereUsedBoms.value = []
    error.value = null
  }

  return {
    // State
    boms,
    totalBoms,
    loading,
    error,
    currentBom,
    bomItems,
    bomTree,
    whereUsedBoms,
    currentPage,
    pageSize,

    // Actions
    fetchBoms,
    fetchBomById,
    createBom,
    updateBom,
    deleteBom,
    fetchBomItems,
    addBomItem,
    updateBomItem,
    removeBomItem,
    fetchBomTree,
    fetchWhereUsed,
    copyBom,
    clearError,
    resetState,
  }
})
