import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { documentApi } from '@/api/documentApi'
import type { DocumentDTO, DocumentRevisionDTO } from '@/types/document'
import type { PageParams } from '@/types'

export const useDocumentStore = defineStore('document', () => {
  // State
  const documents = ref<DocumentDTO[]>([])
  const currentDocument = ref<DocumentDTO | null>(null)
  const revisions = ref<DocumentRevisionDTO[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const pagination = ref({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  // Computed
  const hasDocuments = computed(() => documents.value.length > 0)

  // Actions
  async function fetchDocuments(params?: PageParams): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await documentApi.getAll({
        page: (params?.page ?? pagination.value.current) - 1,
        size: params?.size ?? pagination.value.pageSize,
        sort: params?.sort,
      })

      documents.value = response.data.content
      pagination.value = {
        current: response.data.number + 1,
        pageSize: response.data.size,
        total: response.data.totalElements,
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch documents'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchDocumentById(id: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await documentApi.getById(id)
      currentDocument.value = response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch document'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createDocument(data: DocumentDTO): Promise<DocumentDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await documentApi.create(data)
      documents.value.unshift(response.data)
      pagination.value.total++
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to create document'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateDocument(id: number, data: DocumentDTO): Promise<DocumentDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await documentApi.update(id, data)
      const index = documents.value.findIndex((d) => d.id === id)
      if (index !== -1) {
        documents.value[index] = response.data
      }
      if (currentDocument.value?.id === id) {
        currentDocument.value = response.data
      }
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to update document'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deleteDocument(id: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      await documentApi.delete(id)
      documents.value = documents.value.filter((d) => d.id !== id)
      pagination.value.total--
      if (currentDocument.value?.id === id) {
        currentDocument.value = null
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to delete document'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchRevisions(documentId: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await documentApi.getRevisions(documentId)
      revisions.value = response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch revisions'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createRevision(documentId: number, data: DocumentRevisionDTO): Promise<DocumentRevisionDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await documentApi.createRevision(documentId, data)
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

  async function uploadFile(documentId: number, revisionId: number, file: File): Promise<DocumentRevisionDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await documentApi.uploadFile(documentId, revisionId, file)
      const index = revisions.value.findIndex((r) => r.id === revisionId)
      if (index !== -1) {
        revisions.value[index] = response.data
      }
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to upload file'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function downloadFile(documentId: number, revisionId: number, fileName: string): Promise<void> {
    error.value = null

    try {
      const response = await documentApi.downloadFile(documentId, revisionId, fileName)
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', fileName)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to download file'
      throw err
    }
  }

  function clearError(): void {
    error.value = null
  }

  function setCurrentDocument(document: DocumentDTO | null): void {
    currentDocument.value = document
  }

  return {
    // State
    documents,
    currentDocument,
    revisions,
    loading,
    error,
    pagination,

    // Computed
    hasDocuments,

    // Actions
    fetchDocuments,
    fetchDocumentById,
    createDocument,
    updateDocument,
    deleteDocument,
    fetchRevisions,
    createRevision,
    uploadFile,
    downloadFile,
    clearError,
    setCurrentDocument,
  }
})
