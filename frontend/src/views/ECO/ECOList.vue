<template>
  <div class="eco-list">
    <PageHeader
      title="Engineering Change Orders"
      subtitle="Manage engineering change orders"
    >
      <template #actions>
        <a-button type="primary" @click="showCreateModal = true">
          <template #icon><PlusOutlined /></template>
          Create ECO
        </a-button>
      </template>
    </PageHeader>

    <div class="filter-bar">
      <a-select
        v-model:value="statusFilter"
        placeholder="Filter by status"
        allow-clear
        style="width: 200px"
        @change="handleStatusFilter"
      >
        <a-select-option value="">All Statuses</a-select-option>
        <a-select-option value="DRAFT">Draft</a-select-option>
        <a-select-option value="IN_PROGRESS">In Progress</a-select-option>
        <a-select-option value="APPROVED">Approved</a-select-option>
        <a-select-option value="APPLIED">Applied</a-select-option>
        <a-select-option value="CLOSED">Closed</a-select-option>
      </a-select>
    </div>

    <DataTable
      :columns="columns"
      :data="ecoStore.ecos"
      :loading="ecoStore.loading"
      :searchable="true"
      :exportable="false"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <StatusBadge :status="record.status" :status-map="ecoStatusMap" />
        </template>
        <template v-if="column.key === 'type'">
          <a-tag>{{ record.type || 'Standard' }}</a-tag>
        </template>
        <template v-if="column.key === 'ecrNumber'">
          <router-link v-if="record.ecrId" :to="`/ecrs/${record.ecrId}`">
            View ECR
          </router-link>
          <span v-else>-</span>
        </template>
        <template v-if="column.key === 'actions'">
          <a-space>
            <a-button type="link" size="small" @click="viewEco(record.id)">
              View
            </a-button>
            <a-button
              v-if="record.status === 'DRAFT'"
              type="link"
              size="small"
              @click="editEco(record)"
            >
              Edit
            </a-button>
            <a-popconfirm
              v-if="record.status === 'DRAFT'"
              title="Are you sure you want to delete this ECO?"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" size="small" danger>
                Delete
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <ECOForm
      :open="showCreateModal"
      :editing-eco="editingEco"
      @update:open="showCreateModal = $event"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useEcoStore } from '@/stores/eco'
import DataTable from '@/components/DataTable.vue'
import PageHeader from '@/components/PageHeader.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import ECOForm from './ECOForm.vue'
import type { ECODTO } from '@/types/eco'
import type { TableColumnType } from 'ant-design-vue'

const router = useRouter()
const ecoStore = useEcoStore()

const showCreateModal = ref(false)
const editingEco = ref<ECODTO | null>(null)
const statusFilter = ref<string>('')
const currentPage = ref(0)
const pageSize = ref(10)

const ecoStatusMap: Record<string, { color: string; label: string }> = {
  draft: { color: 'default', label: 'Draft' },
  in_progress: { color: 'processing', label: 'In Progress' },
  approved: { color: 'success', label: 'Approved' },
  applied: { color: 'cyan', label: 'Applied' },
  closed: { color: 'purple', label: 'Closed' },
}

const columns: TableColumnType[] = [
  {
    title: 'ECO Number',
    dataIndex: 'ecoNumber',
    key: 'ecoNumber',
    sorter: true,
  },
  {
    title: 'Title',
    dataIndex: 'title',
    key: 'title',
    ellipsis: true,
  },
  {
    title: 'Status',
    dataIndex: 'status',
    key: 'status',
    filters: [
      { text: 'Draft', value: 'DRAFT' },
      { text: 'In Progress', value: 'IN_PROGRESS' },
      { text: 'Approved', value: 'APPROVED' },
      { text: 'Applied', value: 'APPLIED' },
      { text: 'Closed', value: 'CLOSED' },
    ],
  },
  {
    title: 'Type',
    dataIndex: 'type',
    key: 'type',
  },
  {
    title: 'Source ECR',
    key: 'ecrNumber',
  },
  {
    title: 'Current Stage',
    dataIndex: 'currentStage',
    key: 'currentStage',
  },
  {
    title: 'Created By',
    dataIndex: 'createdBy',
    key: 'createdBy',
  },
  {
    title: 'Created At',
    dataIndex: 'createdAt',
    key: 'createdAt',
    sorter: true,
  },
  {
    title: 'Actions',
    key: 'actions',
    width: 200,
  },
]

function viewEco(id: number) {
  router.push(`/ecos/${id}`)
}

function editEco(eco: ECODTO) {
  editingEco.value = eco
  showCreateModal.value = true
}

async function handleDelete(id: number) {
  await ecoStore.deleteEco(id)
  fetchEcos()
}

function handleStatusFilter(value: string) {
  statusFilter.value = value
  currentPage.value = 0
  fetchEcos()
}

function handleTableChange(pagination: Record<string, unknown>) {
  currentPage.value = ((pagination.current as number) || 1) - 1
  pageSize.value = (pagination.pageSize as number) || 10
  fetchEcos()
}

function handleFormSuccess() {
  showCreateModal.value = false
  editingEco.value = null
  fetchEcos()
}

function fetchEcos() {
  const params: Record<string, unknown> = {
    page: currentPage.value,
    size: pageSize.value,
  }
  if (statusFilter.value) {
    params.status = statusFilter.value
  }
  ecoStore.fetchEcos(params as { page: number; size: number })
}

onMounted(() => {
  fetchEcos()
})
</script>

<style scoped>
.eco-list {
  padding: 0;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
