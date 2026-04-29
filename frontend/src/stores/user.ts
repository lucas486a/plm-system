import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '@/api/userApi'
import type { UserDTO, RoleDTO, CreateUserRequest } from '@/types/user'
import type { PaginatedResponse, PageParams } from '@/types'

export const useUserStore = defineStore('user', () => {
  // State
  const users = ref<UserDTO[]>([])
  const currentUser = ref<UserDTO | null>(null)
  const userRoles = ref<RoleDTO[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const pagination = ref({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  // Actions
  async function fetchUsers(params?: PageParams): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await userApi.getAll({
        page: (params?.page ?? 1) - 1, // Backend uses 0-based indexing
        size: params?.size ?? 10,
        sort: params?.sort,
      })
      const data: PaginatedResponse<UserDTO> = response.data
      users.value = data.content
      pagination.value = {
        current: data.number + 1,
        pageSize: data.size,
        total: data.totalElements,
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch users'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchUserById(id: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await userApi.getById(id)
      currentUser.value = response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch user'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createUser(data: CreateUserRequest): Promise<UserDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await userApi.create(data)
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to create user'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateUser(id: number, data: UserDTO): Promise<UserDTO> {
    loading.value = true
    error.value = null

    try {
      const response = await userApi.update(id, data)
      if (currentUser.value?.id === id) {
        currentUser.value = response.data
      }
      return response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to update user'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deleteUser(id: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      await userApi.delete(id)
      users.value = users.value.filter((u) => u.id !== id)
      if (currentUser.value?.id === id) {
        currentUser.value = null
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to delete user'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchUserRoles(userId: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await userApi.getRoles(userId)
      userRoles.value = response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch user roles'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function assignRole(userId: number, roleId: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await userApi.assignRole(userId, roleId)
      userRoles.value = response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to assign role'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function removeRole(userId: number, roleId: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      await userApi.removeRole(userId, roleId)
      userRoles.value = userRoles.value.filter((r) => r.id !== roleId)
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to remove role'
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
    users,
    currentUser,
    userRoles,
    loading,
    error,
    pagination,
    // Actions
    fetchUsers,
    fetchUserById,
    createUser,
    updateUser,
    deleteUser,
    fetchUserRoles,
    assignRole,
    removeRole,
    clearError,
  }
})
