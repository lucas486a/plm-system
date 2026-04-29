<template>
  <FormModal
    :open="open"
    :title="isEdit ? 'Edit Part' : 'Create Part'"
    :confirm-loading="loading"
    :initial-values="initialValues"
    :rules="rules"
    :width="600"
    @update:open="$emit('update:open', $event)"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <template #default="{ form }">
      <a-form-item label="Part Number" name="partNumber">
        <a-input
          v-model:value="form.partNumber"
          placeholder="Enter part number"
          :disabled="isEdit"
        />
      </a-form-item>

      <a-form-item label="Name" name="name">
        <a-input
          v-model:value="form.name"
          placeholder="Enter part name"
        />
      </a-form-item>

      <a-form-item label="Description" name="description">
        <a-textarea
          v-model:value="form.description"
          placeholder="Enter part description"
          :rows="3"
        />
      </a-form-item>

      <a-form-item label="Part Type" name="partType">
        <a-select
          v-model:value="form.partType"
          placeholder="Select part type"
        >
          <a-select-option value="ASSEMBLY">Assembly</a-select-option>
          <a-select-option value="COMPONENT">Component</a-select-option>
          <a-select-option value="RAW_MATERIAL">Raw Material</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="Default Unit" name="defaultUnit">
        <a-input
          v-model:value="form.defaultUnit"
          placeholder="e.g., kg, pcs, m"
        />
      </a-form-item>
    </template>
  </FormModal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import FormModal from '@/components/FormModal.vue'
import type { PartDTO } from '@/types/part'

interface Props {
  open: boolean
  part?: PartDTO | null
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  part: null,
  loading: false,
})

const emit = defineEmits(['update:open', 'submit'])

const isEdit = computed(() => !!props.part?.id)

const initialValues = computed(() => {
  if (props.part) {
    return {
      partNumber: props.part.partNumber,
      name: props.part.name,
      description: props.part.description || '',
      partType: props.part.partType || undefined,
      defaultUnit: props.part.defaultUnit || '',
    }
  }
  return {
    partNumber: '',
    name: '',
    description: '',
    partType: undefined,
    defaultUnit: '',
  }
})

const rules = {
  partNumber: [
    { required: true, message: 'Please enter part number', trigger: 'blur' },
    { min: 2, max: 50, message: 'Part number must be 2-50 characters', trigger: 'blur' },
  ],
  name: [
    { required: true, message: 'Please enter part name', trigger: 'blur' },
    { min: 2, max: 100, message: 'Name must be 2-100 characters', trigger: 'blur' },
  ],
  partType: [
    { required: true, message: 'Please select part type', trigger: 'change' },
  ],
}

const handleSubmit = (values: Record<string, unknown>) => {
  const partData: PartDTO = {
    partNumber: values.partNumber as string,
    name: values.name as string,
    description: values.description as string,
    partType: values.partType as string,
    defaultUnit: values.defaultUnit as string,
  }
  emit('submit', partData)
}

const handleCancel = () => {
  // Cancel is handled by FormModal
}
</script>
