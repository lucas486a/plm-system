import { message, Modal } from 'ant-design-vue'
import type { AxiosError } from 'axios'

/**
 * Error handling utility
 * Provides centralized error handling for API errors
 */

export interface ApiErrorResponse {
  code: number
  message: string
  data?: unknown
  timestamp?: string
  path?: string
}

export interface ConflictErrorData {
  currentVersion: number
  submittedVersion: number
  entityType: string
  entityId: number | string
  message: string
}

export interface ValidationErrorData {
  field: string
  message: string
  rejectedValue?: unknown
}

/**
 * Extract error message from various error types
 */
export function getErrorMessage(error: unknown): string {
  if (typeof error === 'string') {
    return error
  }

  if (error instanceof Error) {
    return error.message
  }

  if (isAxiosError(error)) {
    const data = error.response?.data as ApiErrorResponse | undefined
    return data?.message || error.message || 'An unexpected error occurred'
  }

  return 'An unexpected error occurred'
}

/**
 * Check if error is an Axios error
 */
export function isAxiosError(error: unknown): error is AxiosError {
  return (
    error !== null &&
    typeof error === 'object' &&
    'isAxiosError' in error &&
    (error as AxiosError).isAxiosError === true
  )
}

/**
 * Check if error is a 409 Conflict error (optimistic locking)
 */
export function isConflictError(error: unknown): boolean {
  if (!isAxiosError(error)) return false
  return error.response?.status === 409
}

/**
 * Check if error is a validation error (400 Bad Request)
 */
export function isValidationError(error: unknown): boolean {
  if (!isAxiosError(error)) return false
  return error.response?.status === 400
}

/**
 * Check if error is a network error
 */
export function isNetworkError(error: unknown): boolean {
  if (!isAxiosError(error)) return false
  return !error.response && error.code === 'ERR_NETWORK'
}

/**
 * Extract conflict error data from 409 response
 */
export function getConflictData(error: unknown): ConflictErrorData | null {
  if (!isConflictError(error)) return null

  const data = (error as AxiosError).response?.data as Record<string, unknown> | undefined
  if (!data) return null

  return {
    currentVersion: data.currentVersion as number,
    submittedVersion: data.submittedVersion as number,
    entityType: data.entityType as string,
    entityId: data.entityId as number | string,
    message: data.message as string,
  }
}

/**
 * Extract validation errors from 400 response
 */
export function getValidationErrors(error: unknown): ValidationErrorData[] {
  if (!isValidationError(error)) return []

  const data = (error as AxiosError).response?.data as Record<string, unknown> | undefined
  if (!data) return []

  // Handle different validation error formats
  if (Array.isArray(data.errors)) {
    return data.errors as ValidationErrorData[]
  }

  if (Array.isArray(data)) {
    return data as ValidationErrorData[]
  }

  // Handle single validation error
  if (data.field && data.message) {
    return [data as unknown as ValidationErrorData]
  }

  return []
}

/**
 * Handle API error with appropriate user feedback
 * Returns the error message for further processing
 */
export function handleApiError(error: unknown, context?: string): string {
  const message = getErrorMessage(error)

  // Network errors
  if (isNetworkError(error)) {
    const networkMessage = 'Network error. Please check your connection and try again.'
    showErrorMessage(networkMessage, context)
    return networkMessage
  }

  // Conflict errors (409) - optimistic locking
  if (isConflictError(error)) {
    handleConflictError(error)
    return message
  }

  // Validation errors (400)
  if (isValidationError(error)) {
    handleValidationError(error)
    return message
  }

  // Generic errors
  showErrorMessage(message, context)
  return message
}

/**
 * Handle 409 Conflict error with user-friendly modal
 */
export function handleConflictError(error: unknown): void {
  const conflictData = getConflictData(error)

  if (conflictData) {
    Modal.warning({
      title: 'Conflict Detected',
      content: `This ${conflictData.entityType} has been modified by another user. ` +
        `The current version is ${conflictData.currentVersion}, but you submitted version ${conflictData.submittedVersion}. ` +
        'Please refresh the page and try again.',
      okText: 'Refresh',
      onOk: () => {
        window.location.reload()
      },
    })
  } else {
    Modal.warning({
      title: 'Conflict Detected',
      content: 'This record has been modified by another user. Please refresh the page and try again.',
      okText: 'Refresh',
      onOk: () => {
        window.location.reload()
      },
    })
  }
}

/**
 * Handle validation errors with field-level feedback
 */
export function handleValidationError(error: unknown): void {
  const validationErrors = getValidationErrors(error)

  if (validationErrors.length > 0) {
    const errorMessages = validationErrors
      .map(err => `${err.field}: ${err.message}`)
      .join('\n')

    Modal.error({
      title: 'Validation Error',
      content: `Please fix the following errors:\n${errorMessages}`,
      okText: 'OK',
    })
  } else {
    const errorMessage = getErrorMessage(error)
    message.error(errorMessage)
  }
}

/**
 * Show error message with optional context
 */
export function showErrorMessage(msg: string, context?: string): void {
  const fullMessage = context ? `${context}: ${msg}` : msg
  message.error(fullMessage)
}

/**
 * Show success message
 */
export function showSuccessMessage(msg: string): void {
  message.success(msg)
}

/**
 * Show warning message
 */
export function showWarningMessage(msg: string): void {
  message.warning(msg)
}

/**
 * Show info message
 */
export function showInfoMessage(msg: string): void {
  message.info(msg)
}

/**
 * Wrap async function with error handling
 * Returns the result or null if error occurred
 */
export async function withErrorHandling<T>(
  fn: () => Promise<T>,
  context?: string
): Promise<T | null> {
  try {
    return await fn()
  } catch (error) {
    handleApiError(error, context)
    return null
  }
}

/**
 * Wrap async function with error handling and loading state
 * Returns the result or null if error occurred
 */
export async function withLoadingAndError<T>(
  fn: () => Promise<T>,
  loadingRef: { value: boolean },
  context?: string
): Promise<T | null> {
  loadingRef.value = true
  try {
    return await fn()
  } catch (error) {
    handleApiError(error, context)
    return null
  } finally {
    loadingRef.value = false
  }
}
