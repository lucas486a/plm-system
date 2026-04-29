import { ref, computed, type Ref, type ComputedRef } from 'vue'

/**
 * Loading state management utility
 * Provides composable loading states for async operations
 */

export interface LoadingState {
  /** Whether any loading operation is active */
  isLoading: ComputedRef<boolean>
  /** Number of active loading operations */
  activeCount: ComputedRef<boolean>
  /** Start a loading operation */
  start: () => void
  /** Stop a loading operation */
  stop: () => void
  /** Wrap an async function with loading state */
  wrap: <T>(fn: () => Promise<T>) => Promise<T>
}

/**
 * Create a loading state manager
 * Supports multiple concurrent loading operations
 */
export function useLoading(initialState = false): LoadingState {
  const count = ref(initialState ? 1 : 0)

  const isLoading = computed(() => count.value > 0)
  const activeCount = computed(() => count.value > 0)

  const start = () => {
    count.value++
  }

  const stop = () => {
    if (count.value > 0) {
      count.value--
    }
  }

  const wrap = async <T>(fn: () => Promise<T>): Promise<T> => {
    start()
    try {
      return await fn()
    } finally {
      stop()
    }
  }

  return {
    isLoading,
    activeCount,
    start,
    stop,
    wrap,
  }
}

/**
 * Create a named loading state for specific operations
 * Useful for tracking multiple independent loading states
 */
export function useNamedLoading() {
  const states = ref<Map<string, Ref<boolean>>>(new Map())

  const isLoading = (name: string): boolean => {
    return states.value.get(name)?.value ?? false
  }

  const start = (name: string): void => {
    if (!states.value.has(name)) {
      states.value.set(name, ref(true))
    } else {
      states.value.get(name)!.value = true
    }
  }

  const stop = (name: string): void => {
    const state = states.value.get(name)
    if (state) {
      state.value = false
    }
  }

  const wrap = async <T>(name: string, fn: () => Promise<T>): Promise<T> => {
    start(name)
    try {
      return await fn()
    } finally {
      stop(name)
    }
  }

  const isAnyLoading = computed(() => {
    return Array.from(states.value.values()).some(state => state.value)
  })

  return {
    isLoading,
    start,
    stop,
    wrap,
    isAnyLoading,
  }
}

/**
 * Debounce loading state to prevent flickering
 * Only shows loading indicator after a delay
 */
export function useDebouncedLoading(delay = 200) {
  const isLoading = ref(false)
  const showLoading = ref(false)
  let timeoutId: ReturnType<typeof setTimeout> | null = null

  const start = () => {
    isLoading.value = true
    timeoutId = setTimeout(() => {
      showLoading.value = true
    }, delay)
  }

  const stop = () => {
    isLoading.value = false
    showLoading.value = false
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
    }
  }

  const wrap = async <T>(fn: () => Promise<T>): Promise<T> => {
    start()
    try {
      return await fn()
    } finally {
      stop()
    }
  }

  return {
    isLoading,
    showLoading,
    start,
    stop,
    wrap,
  }
}
