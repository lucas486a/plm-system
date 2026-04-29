export interface DocumentDTO {
  id?: number
  documentNumber: string
  title: string
  documentType?: string
  description?: string
  version?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}

export interface DocumentRevisionDTO {
  id?: number
  documentId: number
  revision: string
  iteration?: number
  lifecycleState?: string
  description?: string
  filePath?: string
  fileName?: string
  fileSize?: number
  contentType?: string
  isLatestRevision?: boolean
  revisionLocked?: boolean
  partId?: number
  assemblyId?: number
  version?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}
