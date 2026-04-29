<template>
  <div class="audit-log-detail">
    <PageHeader
      :title="`Audit Log #${auditLogStore.currentAuditLog?.id || ''}`"
      subtitle="Audit log entry details"
      :breadcrumbs="[
        { label: 'Home', path: '/' },
        { label: 'Audit Logs', path: '/audit-logs' },
        { label: `#${route.params.id}` },
      ]"
    />

    <LoadingSpinner v-if="auditLogStore.loading && !auditLogStore.currentAuditLog" />

    <template v-else-if="auditLogStore.currentAuditLog">
      <a-descriptions bordered :column="{ xxl: 2, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }">
        <a-descriptions-item label="ID">
          {{ auditLogStore.currentAuditLog.id }}
        </a-descriptions-item>
        <a-descriptions-item label="Timestamp">
          {{ formatDate(auditLogStore.currentAuditLog.timestamp) }}
        </a-descriptions-item>
        <a-descriptions-item label="Entity Type">
          <a-tag :color="getEntityTypeColor(auditLogStore.currentAuditLog.entityType)">
            {{ auditLogStore.currentAuditLog.entityType }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Entity ID">
          {{ auditLogStore.currentAuditLog.entityId }}
        </a-descriptions-item>
        <a-descriptions-item label="Action">
          <a-tag :color="getActionColor(auditLogStore.currentAuditLog.action)">
            {{ auditLogStore.currentAuditLog.action }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="User ID">
          {{ auditLogStore.currentAuditLog.userId || 'N/A' }}
        </a-descriptions-item>
        <a-descriptions-item label="IP Address">
          {{ auditLogStore.currentAuditLog.ipAddress || 'N/A' }}
        </a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="old-value" tab="Old Value">
          <div class="value-container">
            <pre v-if="formattedOldValue">{{ formattedOldValue }}</pre>
            <EmptyState
              v-else
              title="No Old Value"
              description="No previous value was recorded for this change."
              icon="file"
            />
          </div>
        </a-tab-pane>
        <a-tab-pane key="new-value" tab="New Value">
          <div class="value-container">
            <pre v-if="formattedNewValue">{{ formattedNewValue }}</pre>
            <EmptyState
              v-else
              title="No New Value"
              description="No new value was recorded for this change."
              icon="file"
            />
          </div>
        </a-tab-pane>
        <a-tab-pane key="diff" tab="Changes">
          <div class="value-container">
            <a-table
              v-if="diffRows.length > 0"
              :columns="diffColumns"
              :data-source="diffRows"
              :pagination="false"
              size="small"
            />
            <EmptyState
              v-else
              title="No Changes"
              description="No field changes could be determined."
              icon="file"
            />
          </div>
        </a-tab-pane>
      </a-tabs>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import type { TableColumnType } from 'ant-design-vue'
import PageHeader from '@/components/PageHeader.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import EmptyState from '@/components/EmptyState.vue'
import { useAuditLogStore } from '@/stores/auditLog'

const route = useRoute()
const auditLogStore = useAuditLogStore()

const activeTab = ref('old-value')

const diffColumns: TableColumnType[] = [
  { title: 'Field', dataIndex: 'field', key: 'field', width: 200 },
  { title: 'Old Value', dataIndex: 'oldValue', key: 'oldValue' },
  { title: 'New Value', dataIndex: 'newValue', key: 'newValue' },
]

const formattedOldValue = computed(() => {
  const value = auditLogStore.currentAuditLog?.oldValue
  if (!value) return null
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
})

const formattedNewValue = computed(() => {
  const value = auditLogStore.currentAuditLog?.newValue
  if (!value) return null
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
})

interface DiffRow {
  field: string
  oldValue: string
  newValue: string
}

const diffRows = computed<DiffRow[]>(() => {
  const oldVal = auditLogStore.currentAuditLog?.oldValue
  const newVal = auditLogStore.currentAuditLog?.newValue

  if (!oldVal || !newVal) return []

  try {
    const oldObj = JSON.parse(oldVal)
    const newObj = JSON.parse(newVal)
    const allKeys = new Set([...Object.keys(oldObj), ...Object.keys(newObj)])

    return Array.from(allKeys)
      .filter((key) => JSON.stringify(oldObj[key]) !== JSON.stringify(newObj[key]))
      .map((key) => ({
        field: key,
        oldValue: formatValue(oldObj[key]),
        newValue: formatValue(newObj[key]),
      }))
  } catch {
    return []
  }
})

function formatValue(value: unknown): string {
  if (value === null || value === undefined) return 'N/A'
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleString()
}

function getEntityTypeColor(type: string): string {
  const colorMap: Record<string, string> = {
    PART: 'blue',
    DOCUMENT: 'green',
    BOM: 'orange',
    ECR: 'purple',
    ECO: 'cyan',
  }
  return colorMap[type] || 'default'
}

function getActionColor(action: string): string {
  const colorMap: Record<string, string> = {
    CREATE: 'green',
    UPDATE: 'blue',
    DELETE: 'red',
    STATUS_CHANGE: 'orange',
  }
  return colorMap[action] || 'default'
}

onMounted(async () => {
  const id = Number(route.params.id)
  if (id) {
    await auditLogStore.fetchAuditLogById(id)
  }
})
</script>

<style scoped>
.audit-log-detail {
  padding: 0;
}

.value-container {
  background: #fafafa;
  padding: 16px;
  border-radius: 4px;
  min-height: 200px;
}

.value-container pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
}
</style>
