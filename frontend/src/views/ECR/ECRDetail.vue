<template>
  <div class="ecr-detail">
    <PageHeader
      :title="ecrStore.currentEcr?.title || 'ECR Details'"
      :subtitle="ecrStore.currentEcr?.ecrNumber"
      :breadcrumbs="[
        { label: 'ECRs', path: '/ecrs' },
        { label: ecrStore.currentEcr?.ecrNumber || 'Detail' },
      ]"
    >
      <template #actions>
        <a-space>
          <a-button
            v-if="ecrStore.currentEcr?.status === 'DRAFT'"
            type="primary"
            @click="handleSubmit"
          >
            Submit for Evaluation
          </a-button>
          <a-button
            v-if="ecrStore.currentEcr?.status === 'SUBMITTED'"
            type="primary"
            @click="handleEvaluate"
          >
            Evaluate
          </a-button>
          <a-button
            v-if="ecrStore.currentEcr?.status === 'EVALUATED'"
            type="primary"
            @click="handleApprove"
          >
            Approve
          </a-button>
          <a-button
            v-if="ecrStore.currentEcr?.status === 'EVALUATED'"
            danger
            @click="handleReject"
          >
            Reject
          </a-button>
          <a-button
            v-if="ecrStore.currentEcr?.status === 'APPROVED'"
            type="primary"
            @click="handleConvertToEco"
          >
            Convert to ECO
          </a-button>
        </a-space>
      </template>
    </PageHeader>

    <LoadingSpinner v-if="ecrStore.loading && !ecrStore.currentEcr" />

    <template v-else-if="ecrStore.currentEcr">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="info" tab="Basic Information">
          <a-descriptions bordered :column="{ xxl: 2, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }">
            <a-descriptions-item label="ECR Number">
              {{ ecrStore.currentEcr.ecrNumber }}
            </a-descriptions-item>
            <a-descriptions-item label="Status">
              <StatusBadge :status="ecrStore.currentEcr.status || ''" :status-map="ecrStatusMap" />
            </a-descriptions-item>
            <a-descriptions-item label="Title">
              {{ ecrStore.currentEcr.title }}
            </a-descriptions-item>
            <a-descriptions-item label="Priority">
              <a-tag :color="getPriorityColor(ecrStore.currentEcr.priority)">
                {{ ecrStore.currentEcr.priority }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="Description" :span="2">
              {{ ecrStore.currentEcr.description || 'No description' }}
            </a-descriptions-item>
            <a-descriptions-item label="Created By">
              {{ ecrStore.currentEcr.createdBy }}
            </a-descriptions-item>
            <a-descriptions-item label="Created At">
              {{ formatDate(ecrStore.currentEcr.createdAt) }}
            </a-descriptions-item>
            <a-descriptions-item label="Updated By">
              {{ ecrStore.currentEcr.updatedBy }}
            </a-descriptions-item>
            <a-descriptions-item label="Updated At">
              {{ formatDate(ecrStore.currentEcr.updatedAt) }}
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>

        <a-tab-pane key="approval" tab="Approval History">
          <a-timeline>
            <a-timeline-item
              v-for="(event, index) in approvalHistory"
              :key="index"
              :color="getTimelineColor(event.action)"
            >
              <p>
                <strong>{{ event.action }}</strong>
                <span v-if="event.user"> by {{ event.user }}</span>
              </p>
              <p v-if="event.comment" class="timeline-comment">{{ event.comment }}</p>
              <p class="timeline-date">{{ formatDate(event.date) }}</p>
            </a-timeline-item>
          </a-timeline>
          <EmptyState
            v-if="approvalHistory.length === 0"
            title="No Approval History"
            description="No approval actions have been recorded yet."
            icon="file"
          />
        </a-tab-pane>

        <a-tab-pane key="parts" tab="Affected Parts">
          <div class="parts-header">
            <a-button
              v-if="ecrStore.currentEcr.status === 'DRAFT'"
              type="primary"
              size="small"
              @click="showAddPartModal = true"
            >
              <template #icon><PlusOutlined /></template>
              Add Part
            </a-button>
          </div>
          <a-table
            :columns="partColumns"
            :data-source="ecrStore.affectedParts"
            :loading="partsLoading"
            :pagination="false"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'actions'">
                <a-popconfirm
                  v-if="ecrStore.currentEcr?.status === 'DRAFT'"
                  title="Remove this part from affected list?"
                  @confirm="handleRemovePart(record.id)"
                >
                  <a-button type="link" size="small" danger>Remove</a-button>
                </a-popconfirm>
              </template>
            </template>
          </a-table>
          <EmptyState
            v-if="ecrStore.affectedParts.length === 0 && !partsLoading"
            title="No Affected Parts"
            description="No parts have been added to the affected list."
            icon="inbox"
          />
        </a-tab-pane>
      </a-tabs>
    </template>

    <!-- Add Part Modal -->
    <a-modal
      v-model:open="showAddPartModal"
      title="Add Affected Part"
      @ok="handleAddPart"
      @cancel="showAddPartModal = false"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="Part ID" required>
          <a-input-number v-model:value="newPartId" style="width: 100%" placeholder="Enter Part ID" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useEcrStore } from '@/stores/ecr'
import { useEcoStore } from '@/stores/eco'
import PageHeader from '@/components/PageHeader.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import EmptyState from '@/components/EmptyState.vue'
import { Modal, message } from 'ant-design-vue'
import type { TableColumnType } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const ecrStore = useEcrStore()
const ecoStore = useEcoStore()

const activeTab = ref('info')
const partsLoading = ref(false)
const showAddPartModal = ref(false)
const newPartId = ref<number | undefined>(undefined)

const ecrStatusMap: Record<string, { color: string; label: string }> = {
  draft: { color: 'default', label: 'Draft' },
  submitted: { color: 'processing', label: 'Submitted' },
  evaluated: { color: 'warning', label: 'Evaluated' },
  approved: { color: 'success', label: 'Approved' },
  rejected: { color: 'error', label: 'Rejected' },
}

const partColumns: TableColumnType[] = [
  { title: 'Part Number', dataIndex: 'partNumber', key: 'partNumber' },
  { title: 'Name', dataIndex: 'name', key: 'name' },
  { title: 'Revision', dataIndex: 'revision', key: 'revision' },
  { title: 'Actions', key: 'actions', width: 120 },
]

interface ApprovalEvent {
  action: string
  user?: string
  comment?: string
  date?: string
}

const approvalHistory = computed<ApprovalEvent[]>(() => {
  const ecr = ecrStore.currentEcr
  if (!ecr) return []

  const events: ApprovalEvent[] = []
  if (ecr.createdAt) {
    events.push({ action: 'Created', user: ecr.createdBy, date: ecr.createdAt })
  }
  if (ecr.status === 'SUBMITTED' || ecr.status === 'EVALUATED' || ecr.status === 'APPROVED' || ecr.status === 'REJECTED') {
    events.push({ action: 'Submitted for Evaluation', date: ecr.updatedAt })
  }
  if (ecr.status === 'EVALUATED' || ecr.status === 'APPROVED' || ecr.status === 'REJECTED') {
    events.push({ action: 'Evaluated', date: ecr.updatedAt })
  }
  if (ecr.status === 'APPROVED') {
    events.push({ action: 'Approved', date: ecr.updatedAt })
  }
  if (ecr.status === 'REJECTED') {
    events.push({ action: 'Rejected', date: ecr.updatedAt })
  }
  return events
})

function getPriorityColor(priority: string | undefined): string {
  switch (priority?.toUpperCase()) {
    case 'HIGH': return 'red'
    case 'MEDIUM': return 'orange'
    case 'LOW': return 'green'
    default: return 'default'
  }
}

function getTimelineColor(action: string): string {
  switch (action) {
    case 'Approved': return 'green'
    case 'Rejected': return 'red'
    case 'Submitted for Evaluation': return 'blue'
    case 'Evaluated': return 'orange'
    default: return 'gray'
  }
}

function formatDate(date?: string): string {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

async function handleSubmit() {
  const id = ecrStore.currentEcr?.id
  if (!id) return
  await ecrStore.submitEcr(id)
}

async function handleEvaluate() {
  const id = ecrStore.currentEcr?.id
  if (!id) return
  await ecrStore.evaluateEcr(id)
}

async function handleApprove() {
  const id = ecrStore.currentEcr?.id
  if (!id) return
  await ecrStore.approveEcr(id)
}

async function handleReject() {
  const id = ecrStore.currentEcr?.id
  if (!id) return
  Modal.confirm({
    title: 'Reject ECR',
    content: 'Are you sure you want to reject this ECR?',
    onOk: async () => {
      await ecrStore.rejectEcr(id)
    },
  })
}

async function handleConvertToEco() {
  const id = ecrStore.currentEcr?.id
  if (!id) return
  Modal.confirm({
    title: 'Convert to ECO',
    content: 'This will create a new ECO from this ECR. Continue?',
    onOk: async () => {
      const eco = await ecoStore.convertFromEcr(id)
      if (eco?.id) {
        router.push(`/ecos/${eco.id}`)
      }
    },
  })
}

async function handleAddPart() {
  const ecrId = ecrStore.currentEcr?.id
  if (!ecrId || !newPartId.value) {
    message.warning('Please enter a valid Part ID')
    return
  }
  await ecrStore.addAffectedPart(ecrId, newPartId.value)
  showAddPartModal.value = false
  newPartId.value = undefined
}

async function handleRemovePart(partId: number) {
  const ecrId = ecrStore.currentEcr?.id
  if (!ecrId) return
  await ecrStore.removeAffectedPart(ecrId, partId)
}

onMounted(async () => {
  const id = Number(route.params.id)
  if (id) {
    await ecrStore.fetchEcrById(id)
    partsLoading.value = true
    try {
      await ecrStore.fetchAffectedParts(id)
    } finally {
      partsLoading.value = false
    }
  }
})
</script>

<style scoped>
.ecr-detail {
  padding: 0;
}

.parts-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.timeline-comment {
  color: rgba(0, 0, 0, 0.65);
  margin: 4px 0;
}

.timeline-date {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
