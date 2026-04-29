<template>
  <div class="where-used-view">
    <PageHeader
      title="Where Used"
      subtitle="Find all BOMs that use a specific part"
      :breadcrumbs="[
        { label: 'BOMs', path: '/boms' },
        { label: 'Where Used' },
      ]"
    />

    <div class="search-section">
      <a-form layout="inline" @finish="handleSearch">
        <a-form-item label="Part ID">
          <a-input-number
            v-model:value="partId"
            placeholder="Enter part ID"
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="bomStore.loading">
            <template #icon><SearchOutlined /></template>
            Search
          </a-button>
        </a-form-item>
      </a-form>
    </div>

    <a-divider />

    <a-spin :spinning="bomStore.loading">
      <a-empty v-if="!hasSearched" description="Enter a part ID to search" />
      <a-empty v-else-if="bomStore.whereUsedBoms.length === 0" description="No BOMs found using this part" />

      <a-table
        v-else
        :columns="columns"
        :data-source="bomStore.whereUsedBoms"
        :pagination="{ pageSize: 10, showSizeChanger: true, showTotal: (total: number) => `Total ${total} BOMs` }"
        row-key="id"
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
        </template>
      </a-table>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import type { TableColumnType } from 'ant-design-vue'
import { useBomStore } from '@/stores/bom'
import PageHeader from '@/components/PageHeader.vue'
import StatusBadge from '@/components/StatusBadge.vue'

const bomStore = useBomStore()

const partId = ref<number | undefined>(undefined)
const hasSearched = ref(false)

const columns: TableColumnType[] = [
  {
    title: 'BOM Name',
    dataIndex: 'name',
    key: 'name',
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
  },
  {
    title: 'Version',
    dataIndex: 'versionNumber',
    key: 'versionNumber',
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
  },
]

async function handleSearch(): Promise<void> {
  if (!partId.value) {
    message.warning('Please enter a part ID')
    return
  }

  hasSearched.value = true

  try {
    await bomStore.fetchWhereUsed(partId.value)
  } catch {
    message.error('Failed to fetch where-used data')
  }
}
</script>

<style scoped>
.where-used-view {
  padding: 0;
}

.search-section {
  margin-bottom: 16px;
}
</style>
