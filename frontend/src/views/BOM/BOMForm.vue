<template>
  <FormModal
    :open="open"
    :title="isEdit ? 'Edit BOM' : 'Create BOM'"
    :confirm-loading="loading"
    :initial-values="initialValues"
    :rules="rules"
    ok-text="Save"
    @update:open="$emit('update:open', $event)"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <template #default="{ form }">
      <a-form-item label="Name" name="name">
        <a-input
          v-model:value="form.name"
          placeholder="Enter BOM name"
        />
      </a-form-item>
      <a-form-item label="Assembly ID" name="assemblyId">
        <a-input-number
          v-model:value="form.assemblyId"
          placeholder="Enter assembly ID"
          style="width: 100%"
        />
      </a-form-item>
      <a-form-item label="Status" name="status">
        <a-select
          v-model:value="form.status"
          placeholder="Select status"
        >
          <a-select-option value="draft">Draft</a-select-option>
          <a-select-option value="active">Active</a-select-option>
          <a-select-option value="released">Released</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="Comments" name="comments">
        <a-textarea
          v-model:value="form.comments"
          placeholder="Enter comments"
          :rows="3"
        />
      </a-form-item>
    </template>
  </FormModal>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { BOMDTO } from '@/types/bom'
import FormModal from '@/components/FormModal.vue'

interface Props {
  open: boolean
  bom?: BOMDTO | null
  isEdit?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  bom: null,
  isEdit: false,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', values: BOMDTO): void
}>()

const loading = ref(false)

const initialValues = computed(() => {
  if (props.bom) {
    return {
      name: props.bom.name,
      assemblyId: props.bom.assemblyId,
      status: props.bom.status || 'draft',
      comments: props.bom.comments || '',
    }
  }
  return {
    name: '',
    assemblyId: undefined,
    status: 'draft',
    comments: '',
  }
})

const rules = {
  name: [
    { required: true, message: 'Please enter BOM name', trigger: 'blur' },
  ],
  assemblyId: [
    { required: true, message: 'Please enter assembly ID', trigger: 'blur' },
  ],
}

function handleSubmit(values: Record<string, unknown>): void {
  emit('submit', values as unknown as BOMDTO)
}

function handleCancel(): void {
  // FormModal handles the cancel
}
</script>
