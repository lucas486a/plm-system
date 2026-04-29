<template>
  <a-modal
    :open="open"
    title="ECO Approval"
    :confirm-loading="ecoStore.loading"
    :width="500"
    :mask-closable="false"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="Decision" name="decision">
        <a-radio-group v-model:value="formState.decision">
          <a-radio value="APPROVED">Approve</a-radio>
          <a-radio value="REJECTED">Reject</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="Stage" name="stage">
        <a-input v-model:value="formState.stage" placeholder="Approval stage" />
      </a-form-item>
      <a-form-item label="Approver ID" name="approverId">
        <a-input-number v-model:value="formState.approverId" style="width: 100%" placeholder="Enter approver ID" />
      </a-form-item>
      <a-form-item label="Comments" name="comments">
        <a-textarea
          v-model:value="formState.comments"
          placeholder="Enter comments (optional)"
          :rows="4"
        />
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button @click="handleCancel">Cancel</a-button>
      <a-button
        type="primary"
        :loading="ecoStore.loading"
        @click="handleOk"
      >
        Submit
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useEcoStore } from '@/stores/eco'
import type { FormInstance } from 'ant-design-vue'

interface Props {
  open: boolean
  ecoId: number
  currentStage?: string
}

const props = withDefaults(defineProps<Props>(), {
  currentStage: '',
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const ecoStore = useEcoStore()
const formRef = ref<FormInstance | null>(null)

const formState = reactive({
  decision: 'APPROVED',
  stage: props.currentStage || '',
  approverId: undefined as number | undefined,
  comments: '',
})

const formRules = {
  decision: [
    { required: true, message: 'Please select a decision', trigger: 'change' },
  ],
  stage: [
    { required: true, message: 'Please enter the approval stage', trigger: 'blur' },
  ],
  approverId: [
    { required: true, message: 'Please enter the approver ID', trigger: 'blur' },
  ],
}

watch(
  () => props.open,
  (newVal) => {
    if (newVal) {
      formState.decision = 'APPROVED'
      formState.stage = props.currentStage || ''
      formState.approverId = undefined
      formState.comments = ''
    }
  }
)

async function handleOk() {
  try {
    await formRef.value?.validateFields()

    if (!props.ecoId || !formState.approverId) return

    if (formState.decision === 'APPROVED') {
      await ecoStore.approveEco(
        props.ecoId,
        formState.approverId,
        formState.stage,
        formState.comments || undefined
      )
    } else {
      await ecoStore.rejectEco(
        props.ecoId,
        formState.approverId,
        formState.stage,
        formState.comments || undefined
      )
    }

    emit('success')
  } catch (error) {
    // Validation failed
    console.error('Validation failed:', error)
  }
}

function handleCancel() {
  formRef.value?.resetFields()
  emit('update:open', false)
}
</script>

<style scoped>
/* Approval form styles */
</style>
