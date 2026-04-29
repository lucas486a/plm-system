import api from './index'
import type { DashboardStatsDTO } from '@/types/dashboard'

export const dashboardApi = {
  getStats() {
    return api.get<DashboardStatsDTO>('/dashboard/stats')
  },
}
