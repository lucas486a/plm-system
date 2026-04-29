// Re-export all types
export * from './part'
export * from './document'
export * from './bom'
export * from './ecr'
export * from './eco'
export * from './user'
export * from './dashboard'
export * from './assembly'
export * from './auditLog'

// Common API response types
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface PageParams {
  page?: number
  size?: number
  sort?: string
}
