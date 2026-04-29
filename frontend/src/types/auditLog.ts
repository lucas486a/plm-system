export interface AuditLogDTO {
  id: number
  entityType: string
  entityId: number
  action: string
  oldValue?: string
  newValue?: string
  userId?: number
  ipAddress?: string
  timestamp: string
}
