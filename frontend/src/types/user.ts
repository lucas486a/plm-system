export interface RoleDTO {
  id?: number
  name: string
  description?: string
  version?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}

export interface UserDTO {
  id?: number
  username: string
  email: string
  fullName?: string
  isActive?: boolean
  version?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
  roles?: RoleDTO[]
}

export interface CreateUserRequest {
  username: string
  email: string
  fullName?: string
  password: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  username: string
  email: string
  fullName?: string
}
