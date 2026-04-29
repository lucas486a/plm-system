<template>
  <FormModal
    :open="open"
    :title="isEditing ? 'Edit ECR' : 'Create ECR'"
    :confirm-loading="ecrStore.loading"
    :initial-values="formInitialValues"
    :rules="formRules"
    :width="600"
    @update:open="$emit('update:open', $event)"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <template #default="{ form }">
      <a-form-item label="Title" name="title">
        <a-input v-model:value="form.title" placeholder="Enter ECR title" />
      </a-form-item>
      <a-form-item label="Description" name="description">
        <a-textarea
          v-model:value="form.description"
          placeholder="Enter ECR description"
          :rows="4"
        />
      </a-form-item>
      <a-form-item label="Priority" name="priority">
        <a-select v-model:value="form.priority" placeholder="Select priority">
          <a-select-option value="LOW">Low</a-select-option>
          <a-select-option value="MEDIUM">Medium</a-select-option>
          <a-select-option value="HIGH">High</a-select-option>
        </a-select>
      </a-form-item>
    </template>
  </FormModal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useEcrStore } from '@/stores/ecr'
import FormModal from '@/components/FormModal.vue'
import type { ECRDTO } from '@/types/ecr'

interface Props {
  open: boolean
  editingEcr?: ECRDTO | null
}

const props = withDefaults(defineProps<Props>(), {
  editingEcr: null,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const ecrStore = useEcrStore()

const isEditing = computed(() => !!props.editingEcr?.id)

const formInitialValues = computed(() => {
  if (props.editingEcr) {
    return {
      title: props.editingEcr.title,
      description: props.editingEcr.description || '',
      priority: props.editingEcr.priority || 'MEDIUM',
    }
  }
  return {
    title: '',
    description: '',
    priority: 'MEDIUM',
  }
})

const formRules = {
  title: [
    { required: true, message: 'Please enter ECR title', trigger: 'blur' },
    { min: 3, max: 200, message: 'Title must be between 3 and 200 characters', trigger: 'blur' },
  ],
  priority: [
    { required: true, message: 'Please select priority', trigger: 'change' },
  ],
}

async function handleOk(values: Record<string, unknown>) {
  const ecrData: ECRDTO = {
    ecrNumber: props.editingEcr?.ecrNumber || '',
    title: values.title as string,
    description: values.description as string,
    priority: values.priority as string,
  }

  if (isEditing.value && props.editingEcr?.id) {
    await ecrStore.updateEcr(props.editingEcr.id, ecrData)
  } else {
    await ecrStore.createEcr(ecrData)
  }

  emit('success')
}

function handleCancel() {
  emit('update:open', false)
}
</script>

<style scoped>
/* Form modal styles handled by FormModal component */
</style>
