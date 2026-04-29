import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ecrApi } from '@/api/ecrApi'
import type { ECRDTO } from '@/types/ecr'
import type { PartDTO } from '@/types/part'
import type { PaginatedResponse, PageParams } from '@/types'
import { message } from 'ant-design-vue'

export const useEcrStore = defineStore('ecr', () => {
  const ecrs = ref<ECRDTO[]>([])
  const currentEcr = ref<ECRDTO | null>(null)
  const affectedParts = ref<PartDTO[]>([])
  const loading = ref(false)
  const total = ref(0)
  const totalPages = ref(0)

  async function fetchEcrs(params?: PageParams) {
    loading.value = true
    try {
      const response = await ecrApi.getAll(params)
      const data = response.data as PaginatedResponse<ECRDTO>
      ecrs.value = data.content
      total.value = data.totalElements
      totalPages.value = data.totalPages
    } catch (error) {
      message.error('Failed to fetch ECRs')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function fetchEcrById(id: number) {
    loading.value = true
    try {
      const response = await ecrApi.getById(id)
      currentEcr.value = response.data
      return response.data
    } catch (error) {
      message.error('Failed to fetch ECR details')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function createEcr(data: ECRDTO) {
    loading.value = true
    try {
      const response = await ecrApi.create(data)
      message.success('ECR created successfully')
      return response.data
    } catch (error) {
      message.error('Failed to create ECR')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function updateEcr(id: number, data: ECRDTO) {
    loading.value = true
    try {
      const response = await ecrApi.update(id, data)
      message.success('ECR updated successfully')
      return response.data
    } catch (error) {
      message.error('Failed to update ECR')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function deleteEcr(id: number) {
    loading.value = true
    try {
      await ecrApi.delete(id)
      message.success('ECR deleted successfully')
    } catch (error) {
      message.error('Failed to delete ECR')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function submitEcr(id: number) {
    loading.value = true
    try {
      const response = await ecrApi.submit(id)
      currentEcr.value = response.data
      message.success('ECR submitted for evaluation')
      return response.data
    } catch (error) {
      message.error('Failed to submit ECR')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function evaluateEcr(id: number) {
    loading.value = true
    try {
      const response = await ecrApi.evaluate(id)
      currentEcr.value = response.data
      message.success('ECR evaluated')
      return response.data
    } catch (error) {
      message.error('Failed to evaluate ECR')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function approveEcr(id: number) {
    loading.value = true
    try {
      const response = await ecrApi.approve(id)
      currentEcr.value = response.data
      message.success('ECR approved')
      return response.data
    } catch (error) {
      message.error('Failed to approve ECR')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function rejectEcr(id: number) {
    loading.value = true
    try {
      const response = await ecrApi.reject(id)
      currentEcr.value = response.data
      message.success('ECR rejected')
      return response.data
    } catch (error) {
      message.error('Failed to reject ECR')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function assignEcr(id: number, userId: number) {
    loading.value = true
    try {
      const response = await ecrApi.assign(id, userId)
      currentEcr.value = response.data
      message.success('ECR assigned successfully')
      return response.data
    } catch (error) {
      message.error('Failed to assign ECR')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function fetchAffectedParts(id: number) {
    try {
      const response = await ecrApi.getAffectedParts(id)
      affectedParts.value = response.data
      return response.data
    } catch (error) {
      message.error('Failed to fetch affected parts')
      throw error
    }
  }

  async function addAffectedPart(ecrId: number, partId: number) {
    try {
      const response = await ecrApi.addAffectedPart(ecrId, partId)
      affectedParts.value.push(response.data)
      message.success('Part added to affected list')
      return response.data
    } catch (error) {
      message.error('Failed to add affected part')
      throw error
    }
  }

  async function removeAffectedPart(ecrId: number, partId: number) {
    try {
      await ecrApi.removeAffectedPart(ecrId, partId)
      affectedParts.value = affectedParts.value.filter((p) => p.id !== partId)
      message.success('Part removed from affected list')
    } catch (error) {
      message.error('Failed to remove affected part')
      throw error
    }
  }

  return {
    ecrs,
    currentEcr,
    affectedParts,
    loading,
    total,
    totalPages,
    fetchEcrs,
    fetchEcrById,
    createEcr,
    updateEcr,
    deleteEcr,
    submitEcr,
    evaluateEcr,
    approveEcr,
    rejectEcr,
    assignEcr,
    fetchAffectedParts,
    addAffectedPart,
    removeAffectedPart,
  }
})
