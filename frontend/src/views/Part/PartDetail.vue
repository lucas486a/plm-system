<template>
  <div class="part-detail">
    <PageHeader
      :title="currentPart?.name || 'Part Details'"
      :subtitle="currentPart?.partNumber"
      :breadcrumbs="[
        { label: 'Home', path: '/' },
        { label: 'Parts', path: '/parts' },
        { label: currentPart?.partNumber || 'Detail' },
      ]"
    >
      <template #actions>
        <a-button @click="showEditModal">
          <template #icon><EditOutlined /></template>
          Edit
        </a-button>
        <a-popconfirm
          title="Are you sure you want to delete this part?"
          @confirm="handleDelete"
        >
          <a-button danger>
            <template #icon><DeleteOutlined /></template>
            Delete
          </a-button>
        </a-popconfirm>
      </template>
    </PageHeader>

    <LoadingSpinner v-if="loading && !currentPart" />

    <template v-else-if="currentPart">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="info" tab="Basic Information">
          <a-descriptions bordered :column="{ xxl: 2, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }">
            <a-descriptions-item label="Part Number">
              {{ currentPart.partNumber }}
            </a-descriptions-item>
            <a-descriptions-item label="Name">
              {{ currentPart.name }}
            </a-descriptions-item>
            <a-descriptions-item label="Description" :span="2">
              {{ currentPart.description || 'N/A' }}
            </a-descriptions-item>
            <a-descriptions-item label="Part Type">
              <a-tag :color="getPartTypeColor(currentPart.partType)">
                {{ currentPart.partType || 'N/A' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="Default Unit">
              {{ currentPart.defaultUnit || 'N/A' }}
            </a-descriptions-item>
            <a-descriptions-item label="Version">
              {{ currentPart.version || 'N/A' }}
            </a-descriptions-item>
            <a-descriptions-item label="Created By">
              {{ currentPart.createdBy || 'N/A' }}
            </a-descriptions-item>
            <a-descriptions-item label="Created At">
              {{ formatDate(currentPart.createdAt) }}
            </a-descriptions-item>
            <a-descriptions-item label="Updated At">
              {{ formatDate(currentPart.updatedAt) }}
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>

        <a-tab-pane key="revisions" tab="Revision History">
          <PartRevisionList
            :revisions="revisions"
            :loading="revisionsLoading"
            @create-revision="showRevisionForm = true"
          />
        </a-tab-pane>
      </a-tabs>
    </template>

    <EmptyState
      v-else
      title="Part Not Found"
      description="The requested part could not be found."
      icon="file"
      :show-action="true"
      action-text="Back to Parts"
      @action="$router.push('/parts')"
    />

    <PartForm
      v-model:open="editModalVisible"
      :part="currentPart"
      :loading="editLoading"
      @submit="handleEditSubmit"
    />

    <PartRevisionForm
      v-model:open="showRevisionForm"
      :part-id="partId"
      :loading="revisionLoading"
      @submit="handleCreateRevision"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import EmptyState from '@/components/EmptyState.vue'
import PartRevisionList from './PartRevisionList.vue'
import PartForm from './PartForm.vue'
import PartRevisionForm from './PartRevisionForm.vue'
import { usePartStore } from '@/stores/part'
import type { PartDTO, PartRevisionDTO } from '@/types/part'

const route = useRoute()
const router = useRouter()
const partStore = usePartStore()

const { currentPart, revisions, loading } = partStore

const activeTab = ref('info')
const editModalVisible = ref(false)
const editLoading = ref(false)
const showRevisionForm = ref(false)
const revisionLoading = ref(false)
const revisionsLoading = ref(false)

const partId = computed(() => Number(route.params.id))

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

const fetchPartData = async () => {
  try {
    await partStore.fetchPartById(partId.value)
    await fetchRevisions()
  } catch {
    message.error('Failed to fetch part details')
  }
}

const fetchRevisions = async () => {
  revisionsLoading.value = true
  try {
    await partStore.fetchRevisions(partId.value)
  } catch {
    message.error('Failed to fetch revisions')
  } finally {
    revisionsLoading.value = false
  }
}

const showEditModal = () => {
  editModalVisible.value = true
}

const handleEditSubmit = async (values: PartDTO) => {
  editLoading.value = true
  try {
    await partStore.updatePart(partId.value, values)
    message.success('Part updated successfully')
    editModalVisible.value = false
  } catch {
    message.error('Failed to update part')
  } finally {
    editLoading.value = false
  }
}

const handleDelete = async () => {
  try {
    await partStore.deletePart(partId.value)
    message.success('Part deleted successfully')
    router.push('/parts')
  } catch {
    message.error('Failed to delete part')
  }
}

const handleCreateRevision = async (values: PartRevisionDTO) => {
  revisionLoading.value = true
  try {
    await partStore.createRevision(partId.value, {
      ...values,
      partId: partId.value,
    })
    message.success('Revision created successfully')
    showRevisionForm.value = false
  } catch {
    message.error('Failed to create revision')
  } finally {
    revisionLoading.value = false
  }
}

watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      fetchPartData()
    }
  }
)

onMounted(() => {
  if (partId.value) {
    fetchPartData()
  }
})
</script>

<style scoped>
.part-detail {
  width: 100%;
}
</style>
