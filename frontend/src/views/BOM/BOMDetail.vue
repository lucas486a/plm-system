<template>
  <div class="bom-detail">
    <PageHeader
      :title="bomStore.currentBom?.name || 'BOM Details'"
      :subtitle="`Assembly: ${bomStore.currentBom?.assemblyPartNumber || 'N/A'}`"
      :breadcrumbs="[
        { label: 'BOMs', path: '/boms' },
        { label: bomStore.currentBom?.name || 'Details' },
      ]"
    >
      <template #actions>
        <a-button @click="handleEdit">
          <template #icon><EditOutlined /></template>
          Edit
        </a-button>
        <a-button @click="handleCopy">
          <template #icon><CopyOutlined /></template>
          Copy
        </a-button>
        <a-popconfirm
          title="Are you sure you want to delete this BOM?"
          @confirm="handleDelete"
        >
          <a-button danger>
            <template #icon><DeleteOutlined /></template>
            Delete
          </a-button>
        </a-popconfirm>
      </template>
    </PageHeader>

    <a-spin :spinning="bomStore.loading">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="info" tab="Basic Info">
          <a-descriptions bordered :column="{ xxl: 4, xl: 3, lg: 3, md: 2, sm: 1, xs: 1 }">
            <a-descriptions-item label="Name">{{ bomStore.currentBom?.name }}</a-descriptions-item>
            <a-descriptions-item label="Assembly">{{ bomStore.currentBom?.assemblyPartNumber }}</a-descriptions-item>
            <a-descriptions-item label="Status">
              <StatusBadge :status="bomStore.currentBom?.status || 'draft'" />
            </a-descriptions-item>
            <a-descriptions-item label="Version">{{ bomStore.currentBom?.versionNumber }}</a-descriptions-item>
            <a-descriptions-item label="Comments" :span="2">{{ bomStore.currentBom?.comments || '-' }}</a-descriptions-item>
            <a-descriptions-item label="Created By">{{ bomStore.currentBom?.createdBy }}</a-descriptions-item>
            <a-descriptions-item label="Created At">{{ formatDate(bomStore.currentBom?.createdAt) }}</a-descriptions-item>
            <a-descriptions-item label="Updated By">{{ bomStore.currentBom?.updatedBy }}</a-descriptions-item>
            <a-descriptions-item label="Updated At">{{ formatDate(bomStore.currentBom?.updatedAt) }}</a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>

        <a-tab-pane key="items" tab="BOM Items">
          <BOMItemList
            :bom-id="bomId"
            :items="bomStore.bomItems"
            :loading="bomStore.loading"
            @add="handleAddItem"
            @edit="handleEditItem"
            @delete="handleDeleteItem"
            @refresh="loadBomItems"
          />
        </a-tab-pane>

        <a-tab-pane key="tree" tab="Tree View">
          <BOMTreeView
            :bom-id="bomId"
            :tree-data="bomStore.bomTree"
            :loading="bomStore.loading"
            @refresh="loadBomTree"
          />
        </a-tab-pane>
      </a-tabs>
    </a-spin>

    <BOMForm
      v-model:open="bomFormVisible"
      :bom="bomStore.currentBom"
      is-edit
      @submit="handleBomFormSubmit"
    />

    <BOMItemForm
      v-model:open="itemFormVisible"
      :bom-id="bomId"
      :item="selectedItem"
      :is-edit="isEditItem"
      @submit="handleItemFormSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { EditOutlined, CopyOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { useBomStore } from '@/stores/bom'
import type { BOMDTO, BOMItemDTO } from '@/types/bom'
import PageHeader from '@/components/PageHeader.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import BOMForm from './BOMForm.vue'
import BOMItemList from './BOMItemList.vue'
import BOMItemForm from './BOMItemForm.vue'
import BOMTreeView from './BOMTreeView.vue'

const route = useRoute()
const router = useRouter()
const bomStore = useBomStore()

const activeTab = ref('info')
const bomFormVisible = ref(false)
const itemFormVisible = ref(false)
const selectedItem = ref<BOMItemDTO | null>(null)
const isEditItem = ref(false)

const bomId = computed(() => Number(route.params.id))

onMounted(async () => {
  await loadBom()
})

async function loadBom(): Promise<void> {
  try {
    await bomStore.fetchBomById(bomId.value)
    await loadBomItems()
    await loadBomTree()
  } catch {
    message.error('Failed to load BOM')
    router.push('/boms')
  }
}

async function loadBomItems(): Promise<void> {
  try {
    await bomStore.fetchBomItems(bomId.value)
  } catch {
    message.error('Failed to load BOM items')
  }
}

async function loadBomTree(): Promise<void> {
  try {
    await bomStore.fetchBomTree(bomId.value)
  } catch {
    message.error('Failed to load BOM tree')
  }
}

function formatDate(date?: string): string {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

function handleEdit(): void {
  bomFormVisible.value = true
}

async function handleCopy(): Promise<void> {
  try {
    await bomStore.copyBom(bomId.value, `${bomStore.currentBom?.name} (Copy)`)
    message.success('BOM copied successfully')
    router.push('/boms')
  } catch {
    message.error('Failed to copy BOM')
  }
}

async function handleDelete(): Promise<void> {
  try {
    await bomStore.deleteBom(bomId.value)
    message.success('BOM deleted successfully')
    router.push('/boms')
  } catch {
    message.error('Failed to delete BOM')
  }
}

async function handleBomFormSubmit(values: BOMDTO): Promise<void> {
  try {
    await bomStore.updateBom(bomId.value, values)
    message.success('BOM updated successfully')
    bomFormVisible.value = false
    await loadBom()
  } catch {
    message.error('Failed to update BOM')
  }
}

function handleAddItem(): void {
  selectedItem.value = null
  isEditItem.value = false
  itemFormVisible.value = true
}

function handleEditItem(item: BOMItemDTO): void {
  selectedItem.value = { ...item }
  isEditItem.value = true
  itemFormVisible.value = true
}

async function handleDeleteItem(item: BOMItemDTO): Promise<void> {
  try {
    await bomStore.removeBomItem(bomId.value, item.id!)
    message.success('BOM item removed successfully')
  } catch {
    message.error('Failed to remove BOM item')
  }
}

async function handleItemFormSubmit(values: BOMItemDTO): Promise<void> {
  try {
    if (isEditItem.value && selectedItem.value?.id) {
      await bomStore.updateBomItem(bomId.value, selectedItem.value.id, values)
      message.success('BOM item updated successfully')
    } else {
      await bomStore.addBomItem(bomId.value, values)
      message.success('BOM item added successfully')
    }
    itemFormVisible.value = false
  } catch {
    message.error(isEditItem.value ? 'Failed to update BOM item' : 'Failed to add BOM item')
  }
}
</script>

<style scoped>
.bom-detail {
  padding: 0;
}
</style>
