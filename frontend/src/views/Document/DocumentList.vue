<template>
  <div class="document-list">
    <PageHeader
      title="Documents"
      subtitle="Manage product documents and files"
      :breadcrumbs="[
        { label: 'Home', path: '/' },
        { label: 'Documents' },
      ]"
    >
      <template #actions>
        <a-button type="primary" @click="showCreateModal">
          <template #icon><PlusOutlined /></template>
          Create Document
        </a-button>
      </template>
    </PageHeader>

    <DataTable
      :columns="columns"
      :data="documents"
      :loading="loading"
      :searchable="true"
      :exportable="true"
      :page-size="pagination.pageSize"
      @change="handleTableChange"
      @search="handleSearch"
      @export="handleExport"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'documentNumber'">
          <router-link :to="`/documents/${record.id}`" class="document-link">
            {{ record.documentNumber }}
          </router-link>
        </template>

        <template v-if="column.key === 'title'">
          <div class="document-title">
            <FileTextOutlined class="title-icon" />
            <span>{{ record.title }}</span>
          </div>
        </template>

        <template v-if="column.key === 'documentType'">
          <a-tag :color="getTypeColor(record.documentType)">
            {{ record.documentType || 'N/A' }}
          </a-tag>
        </template>

        <template v-if="column.key === 'version'">
          <span class="version">v{{ record.version || 1 }}</span>
        </template>

        <template v-if="column.key === 'createdAt'">
          <span class="date">
            {{ formatDate(record.createdAt) }}
          </span>
        </template>

        <template v-if="column.key === 'actions'">
          <a-space>
            <a-tooltip title="View Details">
              <a-button type="link" size="small" @click="viewDocument(record)">
                <template #icon><EyeOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip title="Edit">
              <a-button type="link" size="small" @click="editDocument(record)">
                <template #icon><EditOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-popconfirm
              title="Are you sure you want to delete this document?"
              ok-text="Yes"
              cancel-text="No"
              @confirm="deleteDocument(record)"
            >
              <a-tooltip title="Delete">
                <a-button type="link" size="small" danger>
                  <template #icon><DeleteOutlined /></template>
                </a-button>
              </a-tooltip>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <!-- Create/Edit Modal -->
    <DocumentForm
      v-model:open="formModalVisible"
      :document="selectedDocument"
      @success="handleFormSuccess"
      @cancel="handleFormCancel"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  PlusOutlined,
  FileTextOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { DocumentDTO } from '@/types/document'
import type { TableColumnType } from 'ant-design-vue'
import PageHeader from '@/components/PageHeader.vue'
import DataTable from '@/components/DataTable.vue'
import DocumentForm from './DocumentForm.vue'
import { useDocumentStore } from '@/stores/document'

const router = useRouter()
const documentStore = useDocumentStore()

const loading = ref(false)
const formModalVisible = ref(false)
const selectedDocument = ref<DocumentDTO | null>(null)
const searchValue = ref('')

const documents = ref<DocumentDTO[]>([])
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

const columns: TableColumnType[] = [
  {
    title: 'Document Number',
    key: 'documentNumber',
    dataIndex: 'documentNumber',
    width: 180,
    sorter: true,
  },
  {
    title: 'Title',
    key: 'title',
    dataIndex: 'title',
    ellipsis: true,
  },
  {
    title: 'Type',
    key: 'documentType',
    dataIndex: 'documentType',
    width: 140,
    filters: [
      { text: 'Specification', value: 'specification' },
      { text: 'Drawing', value: 'drawing' },
      { text: 'Report', value: 'report' },
      { text: 'Procedure', value: 'procedure' },
      { text: 'Manual', value: 'manual' },
      { text: 'Other', value: 'other' },
    ],
  },
  {
    title: 'Version',
    key: 'version',
    dataIndex: 'version',
    width: 100,
    align: 'center',
  },
  {
    title: 'Created',
    key: 'createdAt',
    dataIndex: 'createdAt',
    width: 180,
    sorter: true,
  },
  {
    title: 'Actions',
    key: 'actions',
    width: 150,
    fixed: 'right',
  },
]

const fetchDocuments = async () => {
  loading.value = true
  try {
    await documentStore.fetchDocuments({
      page: pagination.value.current,
      size: pagination.value.pageSize,
    })
    documents.value = documentStore.documents
    pagination.value = documentStore.pagination
  } catch (err) {
    message.error('Failed to fetch documents')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: Record<string, unknown>, _filters: Record<string, unknown>, _sorter: Record<string, unknown>) => {
  pagination.value.current = pag.current as number
  pagination.value.pageSize = pag.pageSize as number
  fetchDocuments()
}

const handleSearch = (value: string) => {
  searchValue.value = value
  // In a real app, you'd debounce this and search server-side
  if (value) {
    const search = value.toLowerCase()
    documents.value = documentStore.documents.filter((doc) =>
      doc.documentNumber.toLowerCase().includes(search) ||
      doc.title.toLowerCase().includes(search) ||
      (doc.description && doc.description.toLowerCase().includes(search))
    )
  } else {
    documents.value = documentStore.documents
  }
}

const handleExport = () => {
  // In a real app, you'd export to CSV/Excel
  message.info('Export functionality coming soon')
}

const showCreateModal = () => {
  selectedDocument.value = null
  formModalVisible.value = true
}

const viewDocument = (record: DocumentDTO) => {
  router.push(`/documents/${record.id}`)
}

const editDocument = (record: DocumentDTO) => {
  selectedDocument.value = { ...record }
  formModalVisible.value = true
}

const deleteDocument = async (record: DocumentDTO) => {
  if (!record.id) return

  try {
    await documentStore.deleteDocument(record.id)
    message.success('Document deleted successfully')
    fetchDocuments()
  } catch (err) {
    message.error('Failed to delete document')
  }
}

const handleFormSuccess = () => {
  fetchDocuments()
}

const handleFormCancel = () => {
  selectedDocument.value = null
}

const getTypeColor = (type?: string): string => {
  const colors: Record<string, string> = {
    specification: 'blue',
    drawing: 'green',
    report: 'orange',
    procedure: 'purple',
    manual: 'cyan',
    other: 'default',
  }
  return colors[type || 'other'] || 'default'
}

const formatDate = (date?: string): string => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

onMounted(() => {
  fetchDocuments()
})
</script>

<style scoped>
.document-list {
  width: 100%;
}

.document-link {
  color: #1890ff;
  font-weight: 500;
  text-decoration: none;
}

.document-link:hover {
  text-decoration: underline;
}

.document-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  color: #1890ff;
}

.version {
  font-weight: 500;
  color: rgba(0, 0, 0, 0.65);
}

.date {
  color: rgba(0, 0, 0, 0.45);
}
</style>
