<template>
  <div class="bom-list">
    <PageHeader
      title="Bill of Materials"
      subtitle="Manage BOMs for assemblies"
    >
      <template #actions>
        <a-button type="primary" @click="showCreateModal">
          <template #icon><PlusOutlined /></template>
          Create BOM
        </a-button>
      </template>
    </PageHeader>

    <DataTable
      :columns="columns"
      :data="bomStore.boms"
      :loading="bomStore.loading"
      :page-size="bomStore.pageSize"
      searchable
      exportable
      @change="handleTableChange"
      @search="handleSearch"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">
          <router-link :to="`/boms/${record.id}`">
            {{ record.name }}
          </router-link>
        </template>
        <template v-else-if="column.key === 'status'">
          <StatusBadge :status="record.status || 'draft'" />
        </template>
        <template v-else-if="column.key === 'actions'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">
              <template #icon><EditOutlined /></template>
            </a-button>
            <a-button type="link" size="small" @click="handleCopy(record)">
              <template #icon><CopyOutlined /></template>
            </a-button>
            <a-popconfirm
              title="Are you sure you want to delete this BOM?"
              @confirm="handleDelete(record)"
            >
              <a-button type="link" size="small" danger>
                <template #icon><DeleteOutlined /></template>
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <BOMForm
      v-model:open="formVisible"
      :bom="selectedBom"
      :is-edit="isEdit"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CopyOutlined,
} from '@ant-design/icons-vue'
import type { TableColumnType } from 'ant-design-vue'
import { useBomStore } from '@/stores/bom'
import type { BOMDTO } from '@/types/bom'
import PageHeader from '@/components/PageHeader.vue'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import BOMForm from './BOMForm.vue'

const bomStore = useBomStore()

const formVisible = ref(false)
const selectedBom = ref<BOMDTO | null>(null)
const isEdit = ref(false)

const columns: TableColumnType[] = [
  {
    title: 'Name',
    dataIndex: 'name',
    key: 'name',
    sorter: true,
  },
  {
    title: 'Assembly',
    dataIndex: 'assemblyPartNumber',
    key: 'assemblyPartNumber',
  },
  {
    title: 'Status',
    dataIndex: 'status',
    key: 'status',
    filters: [
      { text: 'Draft', value: 'draft' },
      { text: 'Active', value: 'active' },
      { text: 'Released', value: 'released' },
    ],
  },
  {
    title: 'Version',
    dataIndex: 'versionNumber',
    key: 'versionNumber',
    sorter: true,
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
    width: 120,
  },
]

onMounted(() => {
  loadBoms()
})

async function loadBoms(): Promise<void> {
  try {
    await bomStore.fetchBoms()
  } catch {
    message.error('Failed to load BOMs')
  }
}

function handleTableChange(pagination: Record<string, unknown>, _filters: Record<string, unknown>, _sorter: Record<string, unknown>): void {
  bomStore.fetchBoms({
    page: (pagination.current as number) - 1,
    size: pagination.pageSize as number,
  })
}

function handleSearch(_value: string): void {
  // Client-side search is handled by DataTable
}

function showCreateModal(): void {
  selectedBom.value = null
  isEdit.value = false
  formVisible.value = true
}

function handleEdit(record: BOMDTO): void {
  selectedBom.value = { ...record }
  isEdit.value = true
  formVisible.value = true
}

async function handleCopy(record: BOMDTO): Promise<void> {
  try {
    await bomStore.copyBom(record.id!, `${record.name} (Copy)`)
    message.success('BOM copied successfully')
    await loadBoms()
  } catch {
    message.error('Failed to copy BOM')
  }
}

async function handleDelete(record: BOMDTO): Promise<void> {
  try {
    await bomStore.deleteBom(record.id!)
    message.success('BOM deleted successfully')
    await loadBoms()
  } catch {
    message.error('Failed to delete BOM')
  }
}

async function handleFormSubmit(values: BOMDTO): Promise<void> {
  try {
    if (isEdit.value && selectedBom.value?.id) {
      await bomStore.updateBom(selectedBom.value.id, values)
      message.success('BOM updated successfully')
    } else {
      await bomStore.createBom(values)
      message.success('BOM created successfully')
    }
    formVisible.value = false
    await loadBoms()
  } catch {
    message.error(isEdit.value ? 'Failed to update BOM' : 'Failed to create BOM')
  }
}
</script>

<style scoped>
.bom-list {
  padding: 0;
}
</style>
