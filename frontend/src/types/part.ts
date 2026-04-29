export interface PartDTO {
  id?: number
  partNumber: string
  name: string
  description?: string
  partType?: string
  defaultUnit?: string
  version?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}

export interface PartRevisionDTO {
  id?: number
  partId: number
  revision: string
  iteration?: number
  lifecycleState?: string
  releaseState?: string
  description?: string
  mpn?: string
  manufacturer?: string
  datasheet?: string
  price?: number
  currency?: string
  isLatestRevision?: boolean
  revisionNotes?: string
  releasedDate?: string
  releasedBy?: string
  version?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}
