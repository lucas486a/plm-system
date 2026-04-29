export interface ECODTO {
  id?: number
  ecoNumber: string
  title: string
  description?: string
  status?: string
  type?: string
  ecrId?: number
  currentStage?: string
  processInstanceId?: string
  effectiveDate?: string
  appliedAt?: string
  version?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}

export interface ECOApprovalDTO {
  id?: number
  ecoId: number
  stage: string
  approverId: number
  decision: string
  comments?: string
  timestamp?: string
  version?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}
