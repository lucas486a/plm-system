import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ecoApi } from '@/api/ecoApi'
import type { ECODTO, ECOApprovalDTO } from '@/types/eco'
import type { ComponentDraftDTO } from '@/types/bom'
import type { PaginatedResponse, PageParams } from '@/types'
import { message } from 'ant-design-vue'

export const useEcoStore = defineStore('eco', () => {
  const ecos = ref<ECODTO[]>([])
  const currentEco = ref<ECODTO | null>(null)
  const approvals = ref<ECOApprovalDTO[]>([])
  const componentDrafts = ref<ComponentDraftDTO[]>([])
  const loading = ref(false)
  const total = ref(0)
  const totalPages = ref(0)

  async function fetchEcos(params?: PageParams) {
    loading.value = true
    try {
      const response = await ecoApi.getAll(params)
      const data = response.data as PaginatedResponse<ECODTO>
      ecos.value = data.content
      total.value = data.totalElements
      totalPages.value = data.totalPages
    } catch (error) {
      message.error('Failed to fetch ECOs')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function fetchEcoById(id: number) {
    loading.value = true
    try {
      const response = await ecoApi.getById(id)
      currentEco.value = response.data
      return response.data
    } catch (error) {
      message.error('Failed to fetch ECO details')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function createEco(data: ECODTO) {
    loading.value = true
    try {
      const response = await ecoApi.create(data)
      message.success('ECO created successfully')
      return response.data
    } catch (error) {
      message.error('Failed to create ECO')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function updateEco(id: number, data: ECODTO) {
    loading.value = true
    try {
      const response = await ecoApi.update(id, data)
      message.success('ECO updated successfully')
      return response.data
    } catch (error) {
      message.error('Failed to update ECO')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function deleteEco(id: number) {
    loading.value = true
    try {
      await ecoApi.delete(id)
      message.success('ECO deleted successfully')
    } catch (error) {
      message.error('Failed to delete ECO')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function convertFromEcr(ecrId: number) {
    loading.value = true
    try {
      const response = await ecoApi.convertFromECR(ecrId)
      message.success('ECR converted to ECO successfully')
      return response.data
    } catch (error) {
      message.error('Failed to convert ECR to ECO')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function submitEco(id: number) {
    loading.value = true
    try {
      const response = await ecoApi.submit(id)
      currentEco.value = response.data
      message.success('ECO submitted')
      return response.data
    } catch (error) {
      message.error('Failed to submit ECO')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function approveEco(id: number, approverId: number, stage: string, comments?: string) {
    loading.value = true
    try {
      const response = await ecoApi.approve(id, approverId, stage, comments)
      approvals.value.push(response.data)
      message.success('ECO approved')
      return response.data
    } catch (error) {
      message.error('Failed to approve ECO')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function rejectEco(id: number, approverId: number, stage: string, comments?: string) {
    loading.value = true
    try {
      const response = await ecoApi.reject(id, approverId, stage, comments)
      approvals.value.push(response.data)
      message.success('ECO rejected')
      return response.data
    } catch (error) {
      message.error('Failed to reject ECO')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function applyEco(id: number) {
    loading.value = true
    try {
      const response = await ecoApi.apply(id)
      currentEco.value = response.data
      message.success('ECO applied successfully')
      return response.data
    } catch (error) {
      message.error('Failed to apply ECO')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function closeEco(id: number) {
    loading.value = true
    try {
      const response = await ecoApi.close(id)
      currentEco.value = response.data
      message.success('ECO closed')
      return response.data
    } catch (error) {
      message.error('Failed to close ECO')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function fetchComponentDrafts(id: number) {
    try {
      const response = await ecoApi.getComponentDrafts(id)
      componentDrafts.value = response.data
      return response.data
    } catch (error) {
      message.error('Failed to fetch component drafts')
      throw error
    }
  }

  async function addComponentDraft(ecoId: number, data: ComponentDraftDTO) {
    try {
      const response = await ecoApi.addComponentDraft(ecoId, data)
      componentDrafts.value.push(response.data)
      message.success('Component draft added')
      return response.data
    } catch (error) {
      message.error('Failed to add component draft')
      throw error
    }
  }

  return {
    ecos,
    currentEco,
    approvals,
    componentDrafts,
    loading,
    total,
    totalPages,
    fetchEcos,
    fetchEcoById,
    createEco,
    updateEco,
    deleteEco,
    convertFromEcr,
    submitEco,
    approveEco,
    rejectEco,
    applyEco,
    closeEco,
    fetchComponentDrafts,
    addComponentDraft,
  }
})
