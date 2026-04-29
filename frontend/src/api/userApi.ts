import api from './index'
import type {
  UserDTO,
  CreateUserRequest,
  LoginRequest,
  LoginResponse,
  RoleDTO,
} from '@/types/user'
import type { PaginatedResponse, PageParams } from '@/types'

export const userApi = {
  // Auth
  login(data: LoginRequest) {
    return api.post<LoginResponse>('/auth/login', data)
  },

  // User CRUD
  getAll(params?: PageParams) {
    return api.get<PaginatedResponse<UserDTO>>('/users', { params })
  },

  getById(id: number) {
    return api.get<UserDTO>(`/users/${id}`)
  },

  getByUsername(username: string) {
    return api.get<UserDTO>(`/users/username/${username}`)
  },

  create(data: CreateUserRequest) {
    return api.post<UserDTO>('/users', data)
  },

  update(id: number, data: UserDTO) {
    return api.put<UserDTO>(`/users/${id}`, data)
  },

  delete(id: number) {
    return api.delete<void>(`/users/${id}`)
  },

  // Role Management
  getRoles(id: number) {
    return api.get<RoleDTO[]>(`/users/${id}/roles`)
  },

  assignRole(id: number, roleId: number) {
    return api.post<RoleDTO[]>(`/users/${id}/roles`, null, { params: { roleId } })
  },

  removeRole(id: number, roleId: number) {
    return api.delete<void>(`/users/${id}/roles/${roleId}`)
  },
}
