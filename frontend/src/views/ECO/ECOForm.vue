<template>
  <FormModal
    :open="open"
    :title="isEditing ? 'Edit ECO' : 'Create ECO'"
    :confirm-loading="ecoStore.loading"
    :initial-values="formInitialValues"
    :rules="formRules"
    :width="600"
    @update:open="$emit('update:open', $event)"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <template #default="{ form }">
      <a-form-item label="Title" name="title">
        <a-input v-model:value="form.title" placeholder="Enter ECO title" />
      </a-form-item>
      <a-form-item label="Description" name="description">
        <a-textarea
          v-model:value="form.description"
          placeholder="Enter ECO description"
          :rows="4"
        />
      </a-form-item>
      <a-form-item label="Type" name="type">
        <a-select v-model:value="form.type" placeholder="Select type">
          <a-select-option value="STANDARD">Standard</a-select-option>
          <a-select-option value="EMERGENCY">Emergency</a-select-option>
          <a-select-option value="TEMPORARY">Temporary</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="Effective Date" name="effectiveDate">
        <a-date-picker
          v-model:value="form.effectiveDate"
          style="width: 100%"
          placeholder="Select effective date"
        />
      </a-form-item>
    </template>
  </FormModal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useEcoStore } from '@/stores/eco'
import FormModal from '@/components/FormModal.vue'
import type { ECODTO } from '@/types/eco'

interface Props {
  open: boolean
  editingEco?: ECODTO | null
}

const props = withDefaults(defineProps<Props>(), {
  editingEco: null,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const ecoStore = useEcoStore()

const isEditing = computed(() => !!props.editingEco?.id)

const formInitialValues = computed(() => {
  if (props.editingEco) {
    return {
      title: props.editingEco.title,
      description: props.editingEco.description || '',
      type: props.editingEco.type || 'STANDARD',
      effectiveDate: props.editingEco.effectiveDate || null,
    }
  }
  return {
    title: '',
    description: '',
    type: 'STANDARD',
    effectiveDate: null,
  }
})

const formRules = {
  title: [
    { required: true, message: 'Please enter ECO title', trigger: 'blur' },
    { min: 3, max: 200, message: 'Title must be between 3 and 200 characters', trigger: 'blur' },
  ],
  type: [
    { required: true, message: 'Please select type', trigger: 'change' },
  ],
}

async function handleOk(values: Record<string, unknown>) {
  const ecoData: ECODTO = {
    ecoNumber: props.editingEco?.ecoNumber || '',
    title: values.title as string,
    description: values.description as string,
    type: values.type as string,
    effectiveDate: values.effectiveDate as string | undefined,
  }

  if (isEditing.value && props.editingEco?.id) {
    await ecoStore.updateEco(props.editingEco.id, ecoData)
  } else {
    await ecoStore.createEco(ecoData)
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
