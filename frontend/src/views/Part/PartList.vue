<template>
  <div class="part-list">
    <PageHeader
      title="Parts"
      subtitle="Manage product parts and components"
      :breadcrumbs="[
        { label: 'Home', path: '/' },
        { label: 'Parts' },
      ]"
    >
      <template #actions>
        <a-button type="primary" @click="showCreateModal">
          <template #icon><PlusOutlined /></template>
          Create Part
        </a-button>
      </template>
    </PageHeader>

    <DataTable
      :columns="columns"
      :data="parts"
      :loading="loading"
      :searchable="true"
      :exportable="false"
      :page-size="pagination.pageSize"
      @search="handleSearch"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'partNumber'">
          <router-link :to="`/parts/${record.id}`">
            {{ record.partNumber }}
          </router-link>
        </template>
        <template v-else-if="column.key === 'partType'">
          <a-tag :color="getPartTypeColor(record.partType)">
            {{ record.partType || 'N/A' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>
        <template v-else-if="column.key === 'actions'">
          <a-space>
            <a-button type="link" size="small" @click="showEditModal(record)">
              <template #icon><EditOutlined /></template>
            </a-button>
            <a-popconfirm
              title="Are you sure you want to delete this part?"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">
                <template #icon><DeleteOutlined /></template>
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <PartForm
      v-model:open="formVisible"
      :part="selectedPart"
      :loading="formLoading"
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
} from '@ant-design/icons-vue'
import type { TableColumnType } from 'ant-design-vue'
import PageHeader from '@/components/PageHeader.vue'
import DataTable from '@/components/DataTable.vue'
import PartForm from './PartForm.vue'
import { usePartStore } from '@/stores/part'
import type { PartDTO } from '@/types/part'

const partStore = usePartStore()
const { parts, loading, pagination } = partStore

const formVisible = ref(false)
const formLoading = ref(false)
const selectedPart = ref<PartDTO | null>(null)
const searchQuery = ref('')

const columns: TableColumnType[] = [
  {
    title: 'Part Number',
    dataIndex: 'partNumber',
    key: 'partNumber',
    sorter: true,
    width: 150,
  },
  {
    title: 'Name',
    dataIndex: 'name',
    key: 'name',
    sorter: true,
    ellipsis: true,
  },
  {
    title: 'Description',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true,
  },
  {
    title: 'Type',
    dataIndex: 'partType',
    key: 'partType',
    width: 120,
    filters: [
      { text: 'Assembly', value: 'ASSEMBLY' },
      { text: 'Component', value: 'COMPONENT' },
      { text: 'Raw Material', value: 'RAW_MATERIAL' },
    ],
  },
  {
    title: 'Version',
    dataIndex: 'version',
    key: 'version',
    width: 80,
    align: 'center',
  },
  {
    title: 'Created At',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 150,
    sorter: true,
  },
  {
    title: 'Actions',
    key: 'actions',
    width: 100,
    align: 'center',
  },
]

const getPartTypeColor = (type?: string): string => {
  const colorMap: Record<string, string> = {
    ASSEMBLY: 'blue',
    COMPONENT: 'green',
    RAW_MATERIAL: 'orange',
  }
  return colorMap[type || ''] || 'default'
}

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString()
}

const fetchParts = async (page = 1) => {
  try {
    if (searchQuery.value) {
      await partStore.searchParts(searchQuery.value, {
        page,
        size: pagination.pageSize,
      })
    } else {
      await partStore.fetchParts({
        page,
        size: pagination.pageSize,
      })
    }
  } catch {
    message.error('Failed to fetch parts')
  }
}

const handleSearch = (value: string) => {
  searchQuery.value = value
  fetchParts(1)
}

const handleTableChange = (pag: Record<string, unknown>) => {
  fetchParts(pag.current as number)
}

const showCreateModal = () => {
  selectedPart.value = null
  formVisible.value = true
}

const showEditModal = (part: PartDTO) => {
  selectedPart.value = { ...part }
  formVisible.value = true
}

const handleFormSubmit = async (values: PartDTO) => {
  formLoading.value = true
  try {
    if (selectedPart.value?.id) {
      await partStore.updatePart(selectedPart.value.id, values)
      message.success('Part updated successfully')
    } else {
      await partStore.createPart(values)
      message.success('Part created successfully')
    }
    formVisible.value = false
    fetchParts(pagination.current)
  } catch {
    message.error('Failed to save part')
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await partStore.deletePart(id)
    message.success('Part deleted successfully')
    fetchParts(pagination.current)
  } catch {
    message.error('Failed to delete part')
  }
}

onMounted(() => {
  fetchParts()
})
</script>

<style scoped>
.part-list {
  width: 100%;
}
</style>
