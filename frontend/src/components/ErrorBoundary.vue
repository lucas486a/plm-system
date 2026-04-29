<template>
  <div v-if="hasError" class="error-boundary">
    <a-result
      status="error"
      :title="errorTitle"
      :sub-title="errorMessage"
    >
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleRetry">
            <template #icon><ReloadOutlined /></template>
            Try Again
          </a-button>
          <a-button @click="handleGoHome">
            <template #icon><HomeOutlined /></template>
            Go Home
          </a-button>
        </a-space>
      </template>

      <template v-if="showDetails" #default>
        <div class="error-details">
          <a-collapse>
            <a-collapse-panel key="1" header="Error Details">
              <pre class="error-stack">{{ errorStack }}</pre>
            </a-collapse-panel>
          </a-collapse>
        </div>
      </template>
    </a-result>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'
import { ReloadOutlined, HomeOutlined } from '@ant-design/icons-vue'

interface Props {
  showDetails?: boolean
}

withDefaults(defineProps<Props>(), {
  showDetails: false,
})

const router = useRouter()

const hasError = ref(false)
const errorTitle = ref('Something went wrong')
const errorMessage = ref('An unexpected error occurred. Please try again.')
const errorStack = ref('')

onErrorCaptured((err: Error) => {
  hasError.value = true
  errorTitle.value = err.name || 'Application Error'
  errorMessage.value = err.message || 'An unexpected error occurred.'
  errorStack.value = err.stack || ''
  return false // Prevent error from propagating
})

const handleRetry = () => {
  hasError.value = false
  errorTitle.value = 'Something went wrong'
  errorMessage.value = 'An unexpected error occurred. Please try again.'
  errorStack.value = ''
}

const handleGoHome = () => {
  hasError.value = false
  router.push('/')
}
</script>

<style scoped>
.error-boundary {
  padding: 48px 32px;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-details {
  max-width: 600px;
  margin: 0 auto;
}

.error-stack {
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-word;
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}
</style>
