<template>
  <div class="eco-detail">
    <PageHeader
      :title="ecoStore.currentEco?.title || 'ECO Details'"
      :subtitle="ecoStore.currentEco?.ecoNumber"
      :breadcrumbs="[
        { label: 'ECOs', path: '/ecos' },
        { label: ecoStore.currentEco?.ecoNumber || 'Detail' },
      ]"
    >
      <template #actions>
        <a-space>
          <a-button
            v-if="ecoStore.currentEco?.status === 'DRAFT'"
            type="primary"
            @click="handleSubmit"
          >
            Submit
          </a-button>
          <a-button
            v-if="ecoStore.currentEco?.status === 'IN_PROGRESS'"
            type="primary"
            @click="showApprovalModal = true"
          >
            Approve / Reject
          </a-button>
          <a-button
            v-if="ecoStore.currentEco?.status === 'APPROVED'"
            type="primary"
            @click="handleApply"
          >
            Apply Changes
          </a-button>
          <a-button
            v-if="ecoStore.currentEco?.status === 'APPLIED'"
            type="primary"
            @click="handleClose"
          >
            Close ECO
          </a-button>
        </a-space>
      </template>
    </PageHeader>

    <LoadingSpinner v-if="ecoStore.loading && !ecoStore.currentEco" />

    <template v-else-if="ecoStore.currentEco">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="info" tab="Basic Information">
          <a-descriptions bordered :column="{ xxl: 2, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }">
            <a-descriptions-item label="ECO Number">
              {{ ecoStore.currentEco.ecoNumber }}
            </a-descriptions-item>
            <a-descriptions-item label="Status">
              <StatusBadge :status="ecoStore.currentEco.status || ''" :status-map="ecoStatusMap" />
            </a-descriptions-item>
            <a-descriptions-item label="Title">
              {{ ecoStore.currentEco.title }}
            </a-descriptions-item>
            <a-descriptions-item label="Type">
              <a-tag>{{ ecoStore.currentEco.type || 'Standard' }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="Description" :span="2">
              {{ ecoStore.currentEco.description || 'No description' }}
            </a-descriptions-item>
            <a-descriptions-item label="Source ECR">
              <router-link v-if="ecoStore.currentEco.ecrId" :to="`/ecrs/${ecoStore.currentEco.ecrId}`">
                View ECR
              </router-link>
              <span v-else>N/A</span>
            </a-descriptions-item>
            <a-descriptions-item label="Current Stage">
              {{ ecoStore.currentEco.currentStage || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="Effective Date">
              {{ formatDate(ecoStore.currentEco.effectiveDate) }}
            </a-descriptions-item>
            <a-descriptions-item label="Applied At">
              {{ formatDate(ecoStore.currentEco.appliedAt) }}
            </a-descriptions-item>
            <a-descriptions-item label="Created By">
              {{ ecoStore.currentEco.createdBy }}
            </a-descriptions-item>
            <a-descriptions-item label="Created At">
              {{ formatDate(ecoStore.currentEco.createdAt) }}
            </a-descriptions-item>
            <a-descriptions-item label="Updated By">
              {{ ecoStore.currentEco.updatedBy }}
            </a-descriptions-item>
            <a-descriptions-item label="Updated At">
              {{ formatDate(ecoStore.currentEco.updatedAt) }}
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>

        <a-tab-pane key="approval" tab="Approval History">
          <a-timeline>
            <a-timeline-item
              v-for="(approval, index) in ecoStore.approvals"
              :key="index"
              :color="approval.decision === 'APPROVED' ? 'green' : 'red'"
            >
              <p>
                <strong>{{ approval.decision }}</strong>
                at stage <a-tag>{{ approval.stage }}</a-tag>
              </p>
              <p v-if="approval.comments" class="timeline-comment">{{ approval.comments }}</p>
              <p class="timeline-date">{{ formatDate(approval.timestamp) }}</p>
            </a-timeline-item>
          </a-timeline>
          <EmptyState
            v-if="ecoStore.approvals.length === 0"
            title="No Approval History"
            description="No approval actions have been recorded yet."
            icon="file"
          />
        </a-tab-pane>

        <a-tab-pane key="drafts" tab="Component Drafts">
          <div class="drafts-header">
            <a-button
              v-if="ecoStore.currentEco.status === 'DRAFT' || ecoStore.currentEco.status === 'IN_PROGRESS'"
              type="primary"
              size="small"
              @click="showAddDraftModal = true"
            >
              <template #icon><PlusOutlined /></template>
              Add Component Draft
            </a-button>
          </div>
          <a-table
            :columns="draftColumns"
            :data-source="ecoStore.componentDrafts"
            :loading="draftsLoading"
            :pagination="false"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <a-tag :color="getActionColor(record.action)">
                  {{ record.action }}
                </a-tag>
              </template>
            </template>
          </a-table>
          <EmptyState
            v-if="ecoStore.componentDrafts.length === 0 && !draftsLoading"
            title="No Component Drafts"
            description="No component drafts have been added yet."
            icon="inbox"
          />
        </a-tab-pane>
      </a-tabs>
    </template>

    <!-- Approval Modal -->
    <ApprovalForm
      :open="showApprovalModal"
      :eco-id="ecoStore.currentEco?.id || 0"
      :current-stage="ecoStore.currentEco?.currentStage || ''"
      @update:open="showApprovalModal = $event"
      @success="handleApprovalSuccess"
    />

    <!-- Add Draft Modal -->
    <a-modal
      v-model:open="showAddDraftModal"
      title="Add Component Draft"
      @ok="handleAddDraft"
      @cancel="showAddDraftModal = false"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="Part Revision ID" required>
          <a-input-number v-model:value="newDraft.partRevisionId" style="width: 100%" />
        </a-form-item>
        <a-form-item label="Quantity" required>
          <a-input-number v-model:value="newDraft.quantity" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="Action" required>
          <a-select v-model:value="newDraft.action" placeholder="Select action">
            <a-select-option value="ADD">Add</a-select-option>
            <a-select-option value="REMOVE">Remove</a-select-option>
            <a-select-option value="MODIFY">Modify</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Designator">
          <a-input v-model:value="newDraft.designator" placeholder="Enter designator" />
        </a-form-item>
        <a-form-item label="Comment">
          <a-textarea v-model:value="newDraft.comment" placeholder="Enter comment" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useEcoStore } from '@/stores/eco'
import PageHeader from '@/components/PageHeader.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import EmptyState from '@/components/EmptyState.vue'
import ApprovalForm from './ApprovalForm.vue'
import type { ComponentDraftDTO } from '@/types/bom'
import type { TableColumnType } from 'ant-design-vue'

const route = useRoute()
const ecoStore = useEcoStore()

const activeTab = ref('info')
const draftsLoading = ref(false)
const showApprovalModal = ref(false)
const showAddDraftModal = ref(false)

const newDraft = reactive<Partial<ComponentDraftDTO>>({
  partRevisionId: 0,
  quantity: 1,
  action: 'ADD',
  designator: '',
  comment: '',
})

const ecoStatusMap: Record<string, { color: string; label: string }> = {
  draft: { color: 'default', label: 'Draft' },
  in_progress: { color: 'processing', label: 'In Progress' },
  approved: { color: 'success', label: 'Approved' },
  applied: { color: 'cyan', label: 'Applied' },
  closed: { color: 'purple', label: 'Closed' },
}

const draftColumns: TableColumnType[] = [
  { title: 'Part Revision ID', dataIndex: 'partRevisionId', key: 'partRevisionId' },
  { title: 'Revision', dataIndex: 'partRevisionRevision', key: 'partRevisionRevision' },
  { title: 'Quantity', dataIndex: 'quantity', key: 'quantity' },
  { title: 'Action', dataIndex: 'action', key: 'action' },
  { title: 'Designator', dataIndex: 'designator', key: 'designator' },
  { title: 'Comment', dataIndex: 'comment', key: 'comment', ellipsis: true },
]

function getActionColor(action: string): string {
  switch (action) {
    case 'ADD': return 'green'
    case 'REMOVE': return 'red'
    case 'MODIFY': return 'orange'
    default: return 'default'
  }
}

function formatDate(date?: string): string {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

async function handleSubmit() {
  const id = ecoStore.currentEco?.id
  if (!id) return
  await ecoStore.submitEco(id)
}

async function handleApply() {
  const id = ecoStore.currentEco?.id
  if (!id) return
  await ecoStore.applyEco(id)
}

async function handleClose() {
  const id = ecoStore.currentEco?.id
  if (!id) return
  await ecoStore.closeEco(id)
}

function handleApprovalSuccess() {
  showApprovalModal.value = false
  const id = ecoStore.currentEco?.id
  if (id) {
    ecoStore.fetchEcoById(id)
  }
}

async function handleAddDraft() {
  const ecoId = ecoStore.currentEco?.id
  if (!ecoId || !newDraft.partRevisionId) return

  await ecoStore.addComponentDraft(ecoId, {
    ecoId,
    partRevisionId: newDraft.partRevisionId!,
    quantity: newDraft.quantity || 1,
    action: newDraft.action as 'ADD' | 'REMOVE' | 'MODIFY',
    designator: newDraft.designator,
    comment: newDraft.comment,
  })

  showAddDraftModal.value = false
  newDraft.partRevisionId = 0
  newDraft.quantity = 1
  newDraft.action = 'ADD'
  newDraft.designator = ''
  newDraft.comment = ''
}

onMounted(async () => {
  const id = Number(route.params.id)
  if (id) {
    await ecoStore.fetchEcoById(id)
    draftsLoading.value = true
    try {
      await ecoStore.fetchComponentDrafts(id)
    } finally {
      draftsLoading.value = false
    }
  }
})
</script>

<style scoped>
.eco-detail {
  padding: 0;
}

.drafts-header {
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
