<template>
  <div class="part-revision-list">
    <div class="revision-header">
      <h4>Revision History</h4>
      <a-button type="primary" size="small" @click="$emit('create-revision')">
        <template #icon><PlusOutlined /></template>
        New Revision
      </a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="revisions"
      :loading="loading"
      :pagination="false"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'revision'">
          <a-tag :color="record.isLatestRevision ? 'green' : 'default'">
            {{ record.revision }}
          </a-tag>
          <a-tag v-if="record.isLatestRevision" color="blue" size="small">
            Latest
          </a-tag>
        </template>
        <template v-else-if="column.key === 'lifecycleState'">
          <StatusBadge :status="record.lifecycleState || 'draft'" />
        </template>
        <template v-else-if="column.key === 'releaseState'">
          <StatusBadge :status="record.releaseState || 'unreleased'" />
        </template>
        <template v-else-if="column.key === 'price'">
          <span v-if="record.price">
            {{ record.currency || 'USD' }} {{ record.price.toFixed(2) }}
          </span>
          <span v-else class="text-muted">N/A</span>
        </template>
        <template v-else-if="column.key === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { PlusOutlined } from '@ant-design/icons-vue'
import type { TableColumnType } from 'ant-design-vue'
import StatusBadge from '@/components/StatusBadge.vue'
import type { PartRevisionDTO } from '@/types/part'

interface Props {
  revisions: PartRevisionDTO[]
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
})

defineEmits<{
  (e: 'create-revision'): void
}>()

const columns: TableColumnType[] = [
  {
    title: 'Revision',
    dataIndex: 'revision',
    key: 'revision',
    width: 120,
  },
  {
    title: 'Description',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true,
  },
  {
    title: 'Lifecycle State',
    dataIndex: 'lifecycleState',
    key: 'lifecycleState',
    width: 130,
  },
  {
    title: 'Release State',
    dataIndex: 'releaseState',
    key: 'releaseState',
    width: 130,
  },
  {
    title: 'MPN',
    dataIndex: 'mpn',
    key: 'mpn',
    width: 120,
  },
  {
    title: 'Manufacturer',
    dataIndex: 'manufacturer',
    key: 'manufacturer',
    width: 120,
  },
  {
    title: 'Price',
    dataIndex: 'price',
    key: 'price',
    width: 100,
  },
  {
    title: 'Created At',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 120,
  },
]

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString()
}
</script>

<style scoped>
.part-revision-list {
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
  font-weight: 500;
}

.text-muted {
  color: rgba(0, 0, 0, 0.45);
}
</style>
