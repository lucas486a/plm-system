import api from './index'
import type { DocumentDTO, DocumentRevisionDTO } from '@/types/document'
import type { PaginatedResponse, PageParams } from '@/types'

export const documentApi = {
  // Document CRUD
  getAll(params?: PageParams) {
    return api.get<PaginatedResponse<DocumentDTO>>('/documents', { params })
  },

  getById(id: number) {
    return api.get<DocumentDTO>(`/documents/${id}`)
  },

  getByNumber(documentNumber: string) {
    return api.get<DocumentDTO>(`/documents/number/${documentNumber}`)
  },

  create(data: DocumentDTO) {
    return api.post<DocumentDTO>('/documents', data)
  },

  update(id: number, data: DocumentDTO) {
    return api.put<DocumentDTO>(`/documents/${id}`, data)
  },

  delete(id: number) {
    return api.delete<void>(`/documents/${id}`)
  },

  // Document Revisions
  getRevisions(documentId: number) {
    return api.get<DocumentRevisionDTO[]>(`/documents/${documentId}/revisions`)
  },

  createRevision(documentId: number, data: DocumentRevisionDTO) {
    return api.post<DocumentRevisionDTO>(`/documents/${documentId}/revisions`, data)
  },

  // File Operations
  uploadFile(documentId: number, revisionId: number, file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<DocumentRevisionDTO>(
      `/documents/${documentId}/revisions/${revisionId}/files`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    )
  },

  downloadFile(documentId: number, revisionId: number, fileName: string) {
    return api.get<Blob>(
      `/documents/${documentId}/revisions/${revisionId}/files/${fileName}`,
      { responseType: 'blob' }
    )
  },
}
