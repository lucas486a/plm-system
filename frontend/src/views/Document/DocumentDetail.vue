<template>
  <div class="document-detail">
    <PageHeader
      :title="document?.title || 'Document Details'"
      :subtitle="document?.documentNumber"
      :breadcrumbs="[
        { label: 'Home', path: '/' },
        { label: 'Documents', path: '/documents' },
        { label: document?.documentNumber || 'Details' },
      ]"
    >
      <template #actions>
        <a-button @click="goBack">
          <template #icon><ArrowLeftOutlined /></template>
          Back
        </a-button>
        <a-button type="primary" @click="showEditModal">
          <template #icon><EditOutlined /></template>
          Edit
        </a-button>
      </template>
    </PageHeader>

    <LoadingSpinner v-if="loading" tip="Loading document..." />

    <template v-else-if="document">
      <a-tabs v-model:activeKey="activeTab" class="detail-tabs">
        <a-tab-pane key="info" tab="Basic Information">
          <div class="info-section">
            <a-descriptions bordered :column="{ xxl: 2, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }">
              <a-descriptions-item label="Document Number">
                <span class="document-number">{{ document.documentNumber }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="Title">
                {{ document.title }}
              </a-descriptions-item>
              <a-descriptions-item label="Type">
                <a-tag :color="getTypeColor(document.documentType)">
                  {{ document.documentType || 'N/A' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="Version">
                <span class="version">v{{ document.version || 1 }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="Description" :span="2">
                {{ document.description || 'No description provided' }}
              </a-descriptions-item>
              <a-descriptions-item label="Created By">
                {{ document.createdBy || 'System' }}
              </a-descriptions-item>
              <a-descriptions-item label="Created At">
                {{ formatDate(document.createdAt) }}
              </a-descriptions-item>
              <a-descriptions-item label="Updated By">
                {{ document.updatedBy || 'N/A' }}
              </a-descriptions-item>
              <a-descriptions-item label="Updated At">
                {{ formatDate(document.updatedAt) }}
              </a-descriptions-item>
            </a-descriptions>
          </div>
        </a-tab-pane>

        <a-tab-pane key="revisions" tab="Revision History">
          <DocumentRevisionList
            :document-id="document.id || 0"
            :revisions="revisions"
            :loading="revisionsLoading"
            @refresh="fetchRevisions"
          />
        </a-tab-pane>
      </a-tabs>
    </template>

    <EmptyState
      v-else
      title="Document Not Found"
      description="The requested document could not be found."
      icon="file"
      :show-action="true"
      action-text="Back to Documents"
      @action="goBack"
    />

    <!-- Edit Modal -->
    <DocumentForm
      v-model:open="editModalVisible"
      :document="document"
      @success="handleEditSuccess"
      @cancel="editModalVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeftOutlined, EditOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { DocumentDTO } from '@/types/document'
import PageHeader from '@/components/PageHeader.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import EmptyState from '@/components/EmptyState.vue'
import DocumentForm from './DocumentForm.vue'
import DocumentRevisionList from './DocumentRevisionList.vue'
import { useDocumentStore } from '@/stores/document'

const router = useRouter()
const route = useRoute()
const documentStore = useDocumentStore()

const loading = ref(false)
const revisionsLoading = ref(false)
const activeTab = ref('info')
const editModalVisible = ref(false)

const document = computed(() => documentStore.currentDocument)
const revisions = computed(() => documentStore.revisions)

const documentId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : null
})

const fetchDocument = async () => {
  if (!documentId.value) return

  loading.value = true
  try {
    await documentStore.fetchDocumentById(documentId.value)
  } catch (err) {
    message.error('Failed to fetch document')
  } finally {
    loading.value = false
  }
}

const fetchRevisions = async () => {
  if (!documentId.value) return

  revisionsLoading.value = true
  try {
    await documentStore.fetchRevisions(documentId.value)
  } catch (err) {
    message.error('Failed to fetch revisions')
  } finally {
    revisionsLoading.value = false
  }
}

const goBack = () => {
  router.push('/documents')
}

const showEditModal = () => {
  editModalVisible.value = true
}

const handleEditSuccess = (updatedDoc: DocumentDTO) => {
  documentStore.setCurrentDocument(updatedDoc)
  editModalVisible.value = false
  message.success('Document updated successfully')
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
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  fetchDocument()
  fetchRevisions()
})
</script>

<style scoped>
.document-detail {
  width: 100%;
}

.detail-tabs {
  margin-top: 24px;
}

.info-section {
  padding: 24px 0;
}

.document-number {
  font-weight: 600;
  color: #1890ff;
}

.version {
  font-weight: 500;
  color: rgba(0, 0, 0, 0.65);
}

:deep(.ant-descriptions-item-label) {
  font-weight: 500;
  color: rgba(0, 0, 0, 0.65);
}

:deep(.ant-descriptions-item-content) {
  color: rgba(0, 0, 0, 0.85);
}
</style>
