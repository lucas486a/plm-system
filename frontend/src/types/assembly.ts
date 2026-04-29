export interface AssemblyDTO {
  id?: number
  partNumber: string
  name: string
  description?: string
  revision?: string
  lifecycleState?: string
  isLatestRevision?: boolean
  version?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}
