<template>
  <FormModal
    :open="open"
    :title="isEditing ? 'Edit Document' : 'Create Document'"
    :confirm-loading="submitting"
    :rules="rules"
    :initial-values="formState"
    :width="600"
    @ok="handleSubmit"
    @cancel="handleCancel"
    @update:open="$emit('update:open', $event)"
  >
    <template #default="{ form }">
      <a-form-item label="Document Number" name="documentNumber">
        <a-input
          v-model:value="form.documentNumber"
          placeholder="Enter document number"
          :disabled="isEditing"
        />
      </a-form-item>

      <a-form-item label="Title" name="title">
        <a-input
          v-model:value="form.title"
          placeholder="Enter document title"
        />
      </a-form-item>

      <a-form-item label="Type" name="documentType">
        <a-select
          v-model:value="form.documentType"
          placeholder="Select document type"
        >
          <a-select-option value="specification">Specification</a-select-option>
          <a-select-option value="drawing">Drawing</a-select-option>
          <a-select-option value="report">Report</a-select-option>
          <a-select-option value="procedure">Procedure</a-select-option>
          <a-select-option value="manual">Manual</a-select-option>
          <a-select-option value="other">Other</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="Description" name="description">
        <a-textarea
          v-model:value="form.description"
          placeholder="Enter document description"
          :rows="4"
        />
      </a-form-item>
    </template>
  </FormModal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { DocumentDTO } from '@/types/document'
import FormModal from '@/components/FormModal.vue'
import { useDocumentStore } from '@/stores/document'

interface Props {
  open: boolean
  document?: DocumentDTO | null
}

const props = withDefaults(defineProps<Props>(), {
  document: null,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success', document: DocumentDTO): void
  (e: 'cancel'): void
}>()

const documentStore = useDocumentStore()
const submitting = ref(false)

const isEditing = computed(() => !!props.document?.id)

const formState = computed(() => {
  if (props.document) {
    return {
      documentNumber: props.document.documentNumber || '',
      title: props.document.title || '',
      documentType: props.document.documentType || undefined,
      description: props.document.description || '',
    }
  }
  return {
    documentNumber: '',
    title: '',
    documentType: undefined,
    description: '',
  }
})

const rules = {
  documentNumber: [
    { required: true, message: 'Please enter document number' },
    { min: 2, max: 50, message: 'Document number must be between 2 and 50 characters' },
  ],
  title: [
    { required: true, message: 'Please enter document title' },
    { min: 2, max: 200, message: 'Title must be between 2 and 200 characters' },
  ],
  documentType: [
    { required: true, message: 'Please select document type' },
  ],
}

const handleSubmit = async (values: Record<string, unknown>) => {
  submitting.value = true

  try {
    const documentData: DocumentDTO = {
      documentNumber: values.documentNumber as string,
      title: values.title as string,
      documentType: values.documentType as string,
      description: values.description as string,
    }

    let result: DocumentDTO

    if (isEditing.value && props.document?.id) {
      result = await documentStore.updateDocument(props.document.id, documentData)
      message.success('Document updated successfully')
    } else {
      result = await documentStore.createDocument(documentData)
      message.success('Document created successfully')
    }

    emit('success', result)
    emit('update:open', false)
  } catch (err) {
    message.error(isEditing.value ? 'Failed to update document' : 'Failed to create document')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
/* Add any custom styles here */
</style>
