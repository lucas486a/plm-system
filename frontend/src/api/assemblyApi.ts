import api from './index'
import type { AssemblyDTO } from '@/types/assembly'
import type { PaginatedResponse, PageParams } from '@/types'

export const assemblyApi = {
  getAll(params?: PageParams) {
    return api.get<PaginatedResponse<AssemblyDTO>>('/assemblies', { params })
  },

  getById(id: number) {
    return api.get<AssemblyDTO>(`/assemblies/${id}`)
  },

  create(data: AssemblyDTO) {
    return api.post<AssemblyDTO>('/assemblies', data)
  },

  update(id: number, data: AssemblyDTO) {
    return api.put<AssemblyDTO>(`/assemblies/${id}`, data)
  },

  delete(id: number) {
    return api.delete<void>(`/assemblies/${id}`)
  },
}
