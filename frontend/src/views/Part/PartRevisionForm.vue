<template>
  <FormModal
    :open="open"
    title="Create New Revision"
    :confirm-loading="loading"
    :initial-values="initialValues"
    :rules="rules"
    :width="600"
    @update:open="$emit('update:open', $event)"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <template #default="{ form }">
      <a-form-item label="Revision" name="revision">
        <a-input
          v-model:value="form.revision"
          placeholder="e.g., A, B, 1.0, 2.1"
        />
      </a-form-item>

      <a-form-item label="Description" name="description">
        <a-textarea
          v-model:value="form.description"
          placeholder="Enter revision description"
          :rows="3"
        />
      </a-form-item>

      <a-form-item label="Revision Notes" name="revisionNotes">
        <a-textarea
          v-model:value="form.revisionNotes"
          placeholder="Enter revision notes or change summary"
          :rows="2"
        />
      </a-form-item>

      <a-form-item label="Lifecycle State" name="lifecycleState">
        <a-select
          v-model:value="form.lifecycleState"
          placeholder="Select lifecycle state"
        >
          <a-select-option value="DRAFT">Draft</a-select-option>
          <a-select-option value="RELEASED">Released</a-select-option>
          <a-select-option value="OBSOLETE">Obsolete</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="MPN" name="mpn">
        <a-input
          v-model:value="form.mpn"
          placeholder="Manufacturer Part Number"
        />
      </a-form-item>

      <a-form-item label="Manufacturer" name="manufacturer">
        <a-input
          v-model:value="form.manufacturer"
          placeholder="Manufacturer name"
        />
      </a-form-item>

      <a-form-item label="Datasheet URL" name="datasheet">
        <a-input
          v-model:value="form.datasheet"
          placeholder="https://..."
        />
      </a-form-item>

      <a-form-item label="Price" name="price">
        <a-input-number
          v-model:value="form.price"
          :min="0"
          :precision="2"
          :step="0.01"
          style="width: 100%"
          placeholder="0.00"
        />
      </a-form-item>

      <a-form-item label="Currency" name="currency">
        <a-select
          v-model:value="form.currency"
          placeholder="Select currency"
        >
          <a-select-option value="USD">USD</a-select-option>
          <a-select-option value="EUR">EUR</a-select-option>
          <a-select-option value="GBP">GBP</a-select-option>
          <a-select-option value="CNY">CNY</a-select-option>
        </a-select>
      </a-form-item>
    </template>
  </FormModal>
</template>

<script setup lang="ts">
import FormModal from '@/components/FormModal.vue'
import type { PartRevisionDTO } from '@/types/part'

interface Props {
  open: boolean
  partId: number
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', values: PartRevisionDTO): void
}>()

const initialValues = {
  revision: '',
  description: '',
  revisionNotes: '',
  lifecycleState: 'DRAFT',
  mpn: '',
  manufacturer: '',
  datasheet: '',
  price: undefined,
  currency: 'USD',
}

const rules = {
  revision: [
    { required: true, message: 'Please enter revision identifier', trigger: 'blur' },
    { max: 20, message: 'Revision must be 20 characters or less', trigger: 'blur' },
  ],
  lifecycleState: [
    { required: true, message: 'Please select lifecycle state', trigger: 'change' },
  ],
}

const handleSubmit = (values: Record<string, unknown>) => {
  const revisionData: PartRevisionDTO = {
    partId: 0, // Will be set by parent
    revision: values.revision as string,
    description: values.description as string,
    revisionNotes: values.revisionNotes as string,
    lifecycleState: values.lifecycleState as string,
    mpn: values.mpn as string,
    manufacturer: values.manufacturer as string,
    datasheet: values.datasheet as string,
    price: values.price as number,
    currency: values.currency as string,
  }
  emit('submit', revisionData)
}

const handleCancel = () => {
  // Cancel is handled by FormModal
}
</script>
