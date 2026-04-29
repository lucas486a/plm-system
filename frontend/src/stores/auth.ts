import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'
import type { UserDTO, ApiResponse } from '@/types'

interface LoginResponse {
  token: string
  user: UserDTO
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<UserDTO | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  async function login(username: string, password: string): Promise<boolean> {
    loading.value = true
    error.value = null

    try {
      const response = await api.post<ApiResponse<LoginResponse>>('/auth/login', {
        username,
        password,
      })

      const { token: newToken, user: userData } = response.data.data
      token.value = newToken
      user.value = userData
      localStorage.setItem('token', newToken)
      return true
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Login failed'
      return false
    } finally {
      loading.value = false
    }
  }

  function logout(): void {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  async function checkAuth(): Promise<boolean> {
    if (!token.value) {
      return false
    }

    try {
      const response = await api.get<ApiResponse<UserDTO>>('/auth/me')
      user.value = response.data.data
      return true
    } catch {
      logout()
      return false
    }
  }

  return {
    token,
    user,
    loading,
    error,
    isAuthenticated,
    login,
    logout,
    checkAuth,
  }
})
