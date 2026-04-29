import type { AuditLogDTO } from './auditLog'

export interface DashboardStatsDTO {
  totalParts: number
  totalDocuments: number
  openECRs: number
  openECOs: number
  recentActivities: AuditLogDTO[]
}
