<template>
  <div class="file-upload">
    <a-upload-dragger
      name="file"
      :multiple="false"
      :before-upload="handleBeforeUpload"
      :show-upload-list="false"
      :disabled="uploading"
      @drop="handleDrop"
    >
      <div class="upload-content">
        <p class="upload-icon">
          <InboxOutlined v-if="!uploading" />
          <LoadingOutlined v-else spin />
        </p>
        <p class="upload-text">
          {{ uploading ? 'Uploading...' : 'Click or drag file to this area to upload' }}
        </p>
        <p class="upload-hint">
          Support for a single file upload. Maximum file size: 10MB.
        </p>
      </div>
    </a-upload-dragger>

    <div v-if="uploading" class="progress-container">
      <a-progress
        :percent="uploadProgress"
        :status="uploadStatus"
        :stroke-color="uploadStatus === 'exception' ? '#ff4d4f' : '#1890ff'"
      />
      <p class="progress-text">{{ progressText }}</p>
    </div>

    <div v-if="selectedFile && !uploading" class="file-info">
      <FileOutlined />
      <span class="file-name">{{ selectedFile.name }}</span>
      <span class="file-size">({{ formatFileSize(selectedFile.size) }})</span>
      <a-button type="link" size="small" @click="clearFile">
        <DeleteOutlined />
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { InboxOutlined, LoadingOutlined, FileOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

interface Props {
  documentId: number
  revisionId: number
  maxSize?: number // in bytes, default 10MB
}

const props = withDefaults(defineProps<Props>(), {
  maxSize: 10 * 1024 * 1024,
})

const emit = defineEmits<{
  (e: 'upload-success', file: File): void
  (e: 'upload-error', error: string): void
}>()

const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadStatus = ref<'active' | 'success' | 'exception'>('active')

const progressText = computed(() => {
  if (uploadStatus.value === 'exception') {
    return 'Upload failed'
  }
  if (uploadProgress.value >= 100) {
    return 'Processing...'
  }
  return `Uploading: ${uploadProgress.value}%`
})

const handleBeforeUpload = (file: File) => {
  // Validate file size
  if (file.size > props.maxSize) {
    message.error(`File size exceeds ${formatFileSize(props.maxSize)} limit`)
    return false
  }

  selectedFile.value = file
  return false // Prevent auto upload
}

const handleDrop = (e: DragEvent) => {
  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    const file = files[0]
    if (file.size > props.maxSize) {
      message.error(`File size exceeds ${formatFileSize(props.maxSize)} limit`)
      return
    }
    selectedFile.value = file
  }
}

const clearFile = () => {
  selectedFile.value = null
  uploadProgress.value = 0
  uploadStatus.value = 'active'
}

const startUpload = async () => {
  if (!selectedFile.value) {
    message.warning('Please select a file first')
    return
  }

  uploading.value = true
  uploadProgress.value = 0
  uploadStatus.value = 'active'

  try {
    // Simulate progress (in real app, use axios onUploadProgress)
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
      }
    }, 200)

    // Import store dynamically to avoid circular dependency
    const { useDocumentStore } = await import('@/stores/document')
    const documentStore = useDocumentStore()

    await documentStore.uploadFile(props.documentId, props.revisionId, selectedFile.value)

    clearInterval(progressInterval)
    uploadProgress.value = 100
    uploadStatus.value = 'success'

    message.success('File uploaded successfully')
    emit('upload-success', selectedFile.value)

    // Clear after success
    setTimeout(() => {
      clearFile()
    }, 1500)
  } catch (err: unknown) {
    uploadStatus.value = 'exception'
    const errorMessage = err instanceof Error ? err.message : 'Upload failed'
    message.error(errorMessage)
    emit('upload-error', errorMessage)
  } finally {
    uploading.value = false
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

defineExpose({
  startUpload,
  clearFile,
  selectedFile,
})
</script>

<style scoped>
.file-upload {
  width: 100%;
}

.upload-content {
  padding: 20px;
  text-align: center;
}

.upload-icon {
  font-size: 48px;
  color: #1890ff;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: rgba(0, 0, 0, 0.85);
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
}

.progress-container {
  margin-top: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

.progress-text {
  margin-top: 8px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.65);
  text-align: center;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 4px;
}

.file-name {
  flex: 1;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
}
</style>
