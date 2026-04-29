<template>
  <div class="document-revision-list">
    <div class="revision-header">
      <h4>Revisions</h4>
      <a-button type="primary" size="small" @click="showAddRevision">
        <template #icon><PlusOutlined /></template>
        Add Revision
      </a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="revisions"
      :loading="loading"
      :pagination="false"
      size="small"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'revision'">
          <a-tag :color="record.isLatestRevision ? 'blue' : 'default'">
            {{ record.revision }}
          </a-tag>
          <a-tag v-if="record.isLatestRevision" color="green" size="small">
            Latest
          </a-tag>
        </template>

        <template v-if="column.key === 'lifecycleState'">
          <StatusBadge :status="record.lifecycleState || 'draft'" />
        </template>

        <template v-if="column.key === 'file'">
          <div v-if="record.fileName" class="file-info">
            <FileOutlined />
            <span class="file-name">{{ record.fileName }}</span>
            <span class="file-size">({{ formatFileSize(record.fileSize || 0) }})</span>
          </div>
          <span v-else class="no-file">No file attached</span>
        </template>

        <template v-if="column.key === 'actions'">
          <a-space>
            <a-tooltip title="Upload File">
              <a-button type="link" size="small" @click="handleUpload(record)">
                <template #icon><UploadOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip v-if="record.fileName" title="Download File">
              <a-button type="link" size="small" @click="handleDownload(record)">
                <template #icon><DownloadOutlined /></template>
              </a-button>
            </a-tooltip>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- Add Revision Modal -->
    <FormModal
      v-model:open="revisionModalVisible"
      title="Add New Revision"
      :confirm-loading="submitting"
      :rules="revisionRules"
      :initial-values="revisionForm"
      @ok="handleAddRevision"
    >
      <template #default="{ form }">
        <a-form-item label="Revision" name="revision">
          <a-input
            v-model:value="form.revision"
            placeholder="e.g., A, B, 1.0"
          />
        </a-form-item>
        <a-form-item label="Description" name="description">
          <a-textarea
            v-model:value="form.description"
            placeholder="Enter revision description"
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="Lifecycle State" name="lifecycleState">
          <a-select
            v-model:value="form.lifecycleState"
            placeholder="Select state"
          >
            <a-select-option value="draft">Draft</a-select-option>
            <a-select-option value="pending">Pending</a-select-option>
            <a-select-option value="approved">Approved</a-select-option>
            <a-select-option value="released">Released</a-select-option>
          </a-select>
        </a-form-item>
      </template>
    </FormModal>

    <!-- Upload Modal -->
    <a-modal
      v-model:open="uploadModalVisible"
      title="Upload File"
      :footer="null"
      width="500px"
    >
      <FileUpload
        v-if="uploadModalVisible"
        ref="fileUploadRef"
        :document-id="documentId"
        :revision-id="selectedRevisionId"
        @upload-success="handleUploadSuccess"
        @upload-error="handleUploadError"
      />
      <div class="upload-actions">
        <a-button @click="uploadModalVisible = false">Cancel</a-button>
        <a-button type="primary" :loading="uploading" @click="startUpload">
          Upload
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  PlusOutlined,
  FileOutlined,
  UploadOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { DocumentRevisionDTO } from '@/types/document'
import type { TableColumnType } from 'ant-design-vue'
import StatusBadge from '@/components/StatusBadge.vue'
import FormModal from '@/components/FormModal.vue'
import FileUpload from './FileUpload.vue'
import { useDocumentStore } from '@/stores/document'

interface Props {
  documentId: number
  revisions: DocumentRevisionDTO[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  (e: 'refresh'): void
}>()

const documentStore = useDocumentStore()

// Revision modal state
const revisionModalVisible = ref(false)
const submitting = ref(false)
const revisionForm = ref({
  revision: '',
  description: '',
  lifecycleState: 'draft',
})

const revisionRules = {
  revision: [{ required: true, message: 'Please enter revision number' }],
}

// Upload modal state
const uploadModalVisible = ref(false)
const uploading = ref(false)
const selectedRevisionId = ref(0)
const fileUploadRef = ref<InstanceType<typeof FileUpload> | null>(null)

const columns: TableColumnType[] = [
  {
    title: 'Revision',
    key: 'revision',
    dataIndex: 'revision',
    width: 150,
  },
  {
    title: 'Description',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true,
  },
  {
    title: 'State',
    key: 'lifecycleState',
    dataIndex: 'lifecycleState',
    width: 120,
  },
  {
    title: 'File',
    key: 'file',
    width: 250,
  },
  {
    title: 'Created',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 180,
    customRender: ({ text }: { text: string }) => {
      if (!text) return '-'
      return new Date(text).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    },
  },
  {
    title: 'Actions',
    key: 'actions',
    width: 100,
    fixed: 'right',
  },
]

const showAddRevision = () => {
  revisionForm.value = {
    revision: '',
    description: '',
    lifecycleState: 'draft',
  }
  revisionModalVisible.value = true
}

const handleAddRevision = async (values: Record<string, unknown>) => {
  submitting.value = true

  try {
    await documentStore.createRevision(props.documentId, {
      documentId: props.documentId,
      revision: values.revision as string,
      description: values.description as string,
      lifecycleState: values.lifecycleState as string,
    })

    message.success('Revision created successfully')
    revisionModalVisible.value = false
    emit('refresh')
  } catch (err) {
    message.error('Failed to create revision')
  } finally {
    submitting.value = false
  }
}

const handleUpload = (record: DocumentRevisionDTO) => {
  selectedRevisionId.value = record.id || 0
  uploadModalVisible.value = true
}

const startUpload = () => {
  fileUploadRef.value?.startUpload()
}

const handleUploadSuccess = () => {
  uploadModalVisible.value = false
  emit('refresh')
}

const handleUploadError = (error: string) => {
  message.error(error)
}

const handleDownload = async (record: DocumentRevisionDTO) => {
  if (!record.fileName) return

  try {
    await documentStore.downloadFile(
      props.documentId,
      record.id || 0,
      record.fileName
    )
    message.success('File downloaded successfully')
  } catch (err) {
    message.error('Failed to download file')
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style scoped>
.document-revision-list {
  width: 100%;
}

.revision-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.revision-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-name {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.no-file {
  color: rgba(0, 0, 0, 0.25);
  font-style: italic;
}

.upload-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
