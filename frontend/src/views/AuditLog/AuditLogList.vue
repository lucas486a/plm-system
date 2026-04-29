<template>
  <div class="audit-log-list">
    <PageHeader
      title="Audit Logs"
      subtitle="View system audit trail and activity history"
      :breadcrumbs="[
        { label: 'Home', path: '/' },
        { label: 'Audit Logs' },
      ]"
    />

    <div class="filters-section">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-form-item label="Entity Type">
            <a-select
              v-model:value="filters.entityType"
              placeholder="All entity types"
              allow-clear
              @change="handleFilterChange"
            >
              <a-select-option value="PART">Part</a-select-option>
              <a-select-option value="DOCUMENT">Document</a-select-option>
              <a-select-option value="BOM">BOM</a-select-option>
              <a-select-option value="ECR">ECR</a-select-option>
              <a-select-option value="ECO">ECO</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item label="Action">
            <a-select
              v-model:value="filters.action"
              placeholder="All actions"
              allow-clear
              @change="handleFilterChange"
            >
              <a-select-option value="CREATE">Create</a-select-option>
              <a-select-option value="UPDATE">Update</a-select-option>
              <a-select-option value="DELETE">Delete</a-select-option>
              <a-select-option value="STATUS_CHANGE">Status Change</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item label="User ID">
            <a-input-number
              v-model:value="filters.userId"
              placeholder="User ID"
              style="width: 100%"
              @change="handleFilterChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item label="Time Range">
            <a-range-picker
              v-model:value="filters.timeRange"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
              @change="handleFilterChange"
            />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16" class="filter-actions">
        <a-col :span="24">
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              Search
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              Reset
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </div>

    <DataTable
      :columns="columns"
      :data="auditLogs"
      :loading="loading"
      :searchable="false"
      :exportable="false"
      :page-size="pagination.pageSize"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'entityType'">
          <a-tag :color="getEntityTypeColor(record.entityType)">
            {{ record.entityType }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-tag :color="getActionColor(record.action)">
            {{ record.action }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'timestamp'">
          {{ formatDate(record.timestamp) }}
        </template>
        <template v-else-if="column.key === 'actions'">
          <a-button type="link" size="small" @click="viewDetail(record.id)">
            View Details
          </a-button>
        </template>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import type { TableColumnType } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import DataTable from '@/components/DataTable.vue'
import { useAuditLogStore } from '@/stores/auditLog'

const router = useRouter()
const auditLogStore = useAuditLogStore()
const { auditLogs, loading, pagination } = auditLogStore

interface Filters {
  entityType: string | undefined
  action: string | undefined
  userId: number | undefined
  timeRange: [Dayjs, Dayjs] | undefined
}

const filters = reactive<Filters>({
  entityType: undefined,
  action: undefined,
  userId: undefined,
  timeRange: undefined,
})

const columns: TableColumnType[] = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: 'Entity Type',
    dataIndex: 'entityType',
    key: 'entityType',
    width: 120,
  },
  {
    title: 'Entity ID',
    dataIndex: 'entityId',
    key: 'entityId',
    width: 100,
  },
  {
    title: 'Action',
    dataIndex: 'action',
    key: 'action',
    width: 140,
  },
  {
    title: 'User ID',
    dataIndex: 'userId',
    key: 'userId',
    width: 100,
  },
  {
    title: 'IP Address',
    dataIndex: 'ipAddress',
    key: 'ipAddress',
    width: 140,
  },
  {
    title: 'Timestamp',
    dataIndex: 'timestamp',
    key: 'timestamp',
    width: 180,
    sorter: true,
  },
  {
    title: 'Actions',
    key: 'actions',
    width: 120,
    align: 'center',
  },
]

const getEntityTypeColor = (type: string): string => {
  const colorMap: Record<string, string> = {
    PART: 'blue',
    DOCUMENT: 'green',
    BOM: 'orange',
    ECR: 'purple',
    ECO: 'cyan',
  }
  return colorMap[type] || 'default'
}

const getActionColor = (action: string): string => {
  const colorMap: Record<string, string> = {
    CREATE: 'green',
    UPDATE: 'blue',
    DELETE: 'red',
    STATUS_CHANGE: 'orange',
  }
  return colorMap[action] || 'default'
}

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleString()
}

const buildSearchParams = (page = 1) => {
  const params: Record<string, unknown> = {
    page,
    size: pagination.pageSize,
  }

  if (filters.entityType) {
    params.entityType = filters.entityType
  }
  if (filters.action) {
    params.action = filters.action
  }
  if (filters.userId) {
    params.userId = filters.userId
  }
  if (filters.timeRange && filters.timeRange.length === 2) {
    params.startTime = filters.timeRange[0].toISOString()
    params.endTime = filters.timeRange[1].toISOString()
  }

  return params
}

const fetchLogs = async (page = 1) => {
  try {
    await auditLogStore.fetchAuditLogs(buildSearchParams(page))
  } catch {
    message.error('Failed to fetch audit logs')
  }
}

const handleSearch = () => {
  fetchLogs(1)
}

const handleReset = () => {
  filters.entityType = undefined
  filters.action = undefined
  filters.userId = undefined
  filters.timeRange = undefined
  fetchLogs(1)
}

const handleFilterChange = () => {
  // Filters are applied on search button click
}

const handleTableChange = (pag: Record<string, unknown>) => {
  fetchLogs(pag.current as number)
}

const viewDetail = (id: number) => {
  router.push(`/audit-logs/${id}`)
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.audit-log-list {
  width: 100%;
}

.filters-section {
  background: #fafafa;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.filter-actions {
  margin-top: 8px;
}
</style>
