<template>
  <div class="ecr-list">
    <PageHeader
      title="Engineering Change Requests"
      subtitle="Manage engineering change requests"
    >
      <template #actions>
        <a-button type="primary" @click="showCreateModal = true">
          <template #icon><PlusOutlined /></template>
          Create ECR
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
        <a-select-option value="SUBMITTED">Submitted</a-select-option>
        <a-select-option value="EVALUATED">Evaluated</a-select-option>
        <a-select-option value="APPROVED">Approved</a-select-option>
        <a-select-option value="REJECTED">Rejected</a-select-option>
      </a-select>
    </div>

    <DataTable
      :columns="columns"
      :data="ecrStore.ecrs"
      :loading="ecrStore.loading"
      :searchable="true"
      :exportable="false"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <StatusBadge :status="record.status" :status-map="ecrStatusMap" />
        </template>
        <template v-if="column.key === 'priority'">
          <a-tag :color="getPriorityColor(record.priority)">
            {{ record.priority }}
          </a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <a-space>
            <a-button type="link" size="small" @click="viewEcr(record.id)">
              View
            </a-button>
            <a-button
              v-if="record.status === 'DRAFT'"
              type="link"
              size="small"
              @click="editEcr(record)"
            >
              Edit
            </a-button>
            <a-popconfirm
              v-if="record.status === 'DRAFT'"
              title="Are you sure you want to delete this ECR?"
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

    <ECRForm
      :open="showCreateModal"
      :editing-ecr="editingEcr"
      @update:open="showCreateModal = $event"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useEcrStore } from '@/stores/ecr'
import DataTable from '@/components/DataTable.vue'
import PageHeader from '@/components/PageHeader.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import ECRForm from './ECRForm.vue'
import type { ECRDTO } from '@/types/ecr'
import type { TableColumnType } from 'ant-design-vue'

const router = useRouter()
const ecrStore = useEcrStore()

const showCreateModal = ref(false)
const editingEcr = ref<ECRDTO | null>(null)
const statusFilter = ref<string>('')
const currentPage = ref(0)
const pageSize = ref(10)

const ecrStatusMap: Record<string, { color: string; label: string }> = {
  draft: { color: 'default', label: 'Draft' },
  submitted: { color: 'processing', label: 'Submitted' },
  evaluated: { color: 'warning', label: 'Evaluated' },
  approved: { color: 'success', label: 'Approved' },
  rejected: { color: 'error', label: 'Rejected' },
}

const columns: TableColumnType[] = [
  {
    title: 'ECR Number',
    dataIndex: 'ecrNumber',
    key: 'ecrNumber',
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
      { text: 'Submitted', value: 'SUBMITTED' },
      { text: 'Evaluated', value: 'EVALUATED' },
      { text: 'Approved', value: 'APPROVED' },
      { text: 'Rejected', value: 'REJECTED' },
    ],
  },
  {
    title: 'Priority',
    dataIndex: 'priority',
    key: 'priority',
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

function getPriorityColor(priority: string | undefined): string {
  switch (priority?.toUpperCase()) {
    case 'HIGH':
      return 'red'
    case 'MEDIUM':
      return 'orange'
    case 'LOW':
      return 'green'
    default:
      return 'default'
  }
}

function viewEcr(id: number) {
  router.push(`/ecrs/${id}`)
}

function editEcr(ecr: ECRDTO) {
  editingEcr.value = ecr
  showCreateModal.value = true
}

async function handleDelete(id: number) {
  await ecrStore.deleteEcr(id)
  fetchEcrs()
}

function handleStatusFilter(value: string) {
  statusFilter.value = value
  currentPage.value = 0
  fetchEcrs()
}

function handleTableChange(pagination: Record<string, unknown>) {
  currentPage.value = ((pagination.current as number) || 1) - 1
  pageSize.value = (pagination.pageSize as number) || 10
  fetchEcrs()
}

function handleFormSuccess() {
  showCreateModal.value = false
  editingEcr.value = null
  fetchEcrs()
}

function fetchEcrs() {
  const params: Record<string, unknown> = {
    page: currentPage.value,
    size: pageSize.value,
  }
  if (statusFilter.value) {
    params.status = statusFilter.value
  }
  ecrStore.fetchEcrs(params as { page: number; size: number })
}

onMounted(() => {
  fetchEcrs()
})
</script>

<style scoped>
.ecr-list {
  padding: 0;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
