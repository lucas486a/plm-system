<template>
  <div class="data-table">
    <div class="table-toolbar">
      <div class="toolbar-left">
        <slot name="toolbar-left" />
      </div>
      <div class="toolbar-right">
        <a-input-search
          v-if="searchable"
          v-model:value="searchValue"
          placeholder="Search..."
          style="width: 250px"
          @search="handleSearch"
        />
        <a-button
          v-if="exportable"
          @click="handleExport"
        >
          <template #icon><DownloadOutlined /></template>
          Export
        </a-button>
        <slot name="toolbar-right" />
      </div>
    </div>
    <a-table
      :columns="columns"
      :data-source="filteredData"
      :loading="loading"
      :pagination="paginationConfig"
      :row-selection="rowSelection"
      :scroll="{ x: scrollX }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <slot name="bodyCell" :column="column" :record="record" />
      </template>
      <template #headerCell="{ column }">
        <slot name="headerCell" :column="column" />
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { DownloadOutlined } from '@ant-design/icons-vue'
import type { TableColumnType } from 'ant-design-vue'

interface Props {
  columns: TableColumnType[]
  data: Record<string, unknown>[]
  loading?: boolean
  searchable?: boolean
  exportable?: boolean
  pageSize?: number
  scrollX?: number | string
  rowSelection?: Record<string, unknown> | null
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  searchable: true,
  exportable: true,
  pageSize: 10,
  scrollX: 'max-content',
  rowSelection: null,
})

const emit = defineEmits<{
  (e: 'change', pagination: Record<string, unknown>, filters: Record<string, unknown>, sorter: Record<string, unknown>): void
  (e: 'search', value: string): void
  (e: 'export'): void
}>()

const searchValue = ref('')
const currentPage = ref(1)
const currentPageSize = ref(props.pageSize)

const filteredData = computed(() => {
  if (!searchValue.value) return props.data
  const search = searchValue.value.toLowerCase()
  return props.data.filter((record) =>
    Object.values(record).some((value) =>
      String(value).toLowerCase().includes(search)
    )
  )
})

const paginationConfig = computed(() => ({
  current: currentPage.value,
  pageSize: currentPageSize.value,
  total: filteredData.value.length,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `Total ${total} items`,
}))

const handleTableChange = (
  pagination: Record<string, unknown>,
  filters: Record<string, unknown>,
  sorter: Record<string, unknown>
) => {
  currentPage.value = pagination.current as number
  currentPageSize.value = pagination.pageSize as number
  emit('change', pagination, filters, sorter)
}

const handleSearch = (value: string) => {
  searchValue.value = value
  currentPage.value = 1
  emit('search', value)
}

const handleExport = () => {
  emit('export')
}
</script>

<style scoped>
.data-table {
  width: 100%;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
