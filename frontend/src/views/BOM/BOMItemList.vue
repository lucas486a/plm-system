<template>
  <div class="bom-item-list">
    <div class="toolbar">
      <a-button type="primary" @click="$emit('add')">
        <template #icon><PlusOutlined /></template>
        Add Item
      </a-button>
      <a-button @click="$emit('refresh')">
        <template #icon><ReloadOutlined /></template>
        Refresh
      </a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="items"
      :loading="loading"
      :pagination="{ pageSize: 10, showSizeChanger: true, showTotal: (total: number) => `Total ${total} items` }"
      row-key="id"
      :scroll="{ x: 'max-content' }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'actions'">
          <a-space>
            <a-button type="link" size="small" @click="$emit('edit', record)">
              <template #icon><EditOutlined /></template>
            </a-button>
            <a-popconfirm
              title="Are you sure you want to remove this item?"
              @confirm="$emit('delete', record)"
            >
              <a-button type="link" size="small" danger>
                <template #icon><DeleteOutlined /></template>
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { PlusOutlined, ReloadOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import type { TableColumnType } from 'ant-design-vue'
import type { BOMItemDTO } from '@/types/bom'

interface Props {
  bomId: number
  items: BOMItemDTO[]
  loading?: boolean
}

defineProps<Props>()

defineEmits<{
  (e: 'add'): void
  (e: 'edit', item: BOMItemDTO): void
  (e: 'delete', item: BOMItemDTO): void
  (e: 'refresh'): void
}>()

const columns: TableColumnType[] = [
  {
    title: 'Find Number',
    dataIndex: 'findNumber',
    key: 'findNumber',
    sorter: true,
    width: 100,
  },
  {
    title: 'Part Revision',
    dataIndex: 'partRevisionRevision',
    key: 'partRevisionRevision',
  },
  {
    title: 'Quantity',
    dataIndex: 'quantity',
    key: 'quantity',
    sorter: true,
    width: 100,
  },
  {
    title: 'Designator',
    dataIndex: 'designator',
    key: 'designator',
  },
  {
    title: 'Mounted',
    dataIndex: 'isMounted',
    key: 'isMounted',
    width: 80,
    customRender: ({ text }: { text: boolean }) => text ? 'Yes' : 'No',
  },
  {
    title: 'Scrap Factor',
    dataIndex: 'scrapFactor',
    key: 'scrapFactor',
    width: 100,
  },
  {
    title: 'Comment',
    dataIndex: 'comment',
    key: 'comment',
    ellipsis: true,
  },
  {
    title: 'Actions',
    key: 'actions',
    width: 100,
    fixed: 'right',
  },
]
</script>

<style scoped>
.bom-item-list {
  width: 100%;
}

.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
</style>
