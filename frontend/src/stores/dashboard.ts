import { defineStore } from 'pinia'
import { ref } from 'vue'
import { dashboardApi } from '@/api/dashboardApi'
import type { DashboardStatsDTO } from '@/types/dashboard'

export const useDashboardStore = defineStore('dashboard', () => {
  // State
  const stats = ref<DashboardStatsDTO | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Actions
  async function fetchStats(): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const response = await dashboardApi.getStats()
      stats.value = response.data
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      error.value = axiosError.response?.data?.message || 'Failed to fetch dashboard stats'
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
    stats,
    loading,
    error,
    // Actions
    fetchStats,
    clearError,
  }
})
