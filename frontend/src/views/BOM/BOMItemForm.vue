<template>
  <FormModal
    :open="open"
    :title="isEdit ? 'Edit BOM Item' : 'Add BOM Item'"
    :confirm-loading="loading"
    :initial-values="initialValues"
    :rules="rules"
    ok-text="Save"
    @update:open="$emit('update:open', $event)"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <template #default="{ form }">
      <a-form-item label="Part Revision ID" name="partRevisionId">
        <a-input-number
          v-model:value="form.partRevisionId"
          placeholder="Enter part revision ID"
          style="width: 100%"
        />
      </a-form-item>
      <a-form-item label="Quantity" name="quantity">
        <a-input-number
          v-model:value="form.quantity"
          placeholder="Enter quantity"
          :min="0"
          :step="0.01"
          style="width: 100%"
        />
      </a-form-item>
      <a-form-item label="Find Number" name="findNumber">
        <a-input-number
          v-model:value="form.findNumber"
          placeholder="Enter find number"
          style="width: 100%"
        />
      </a-form-item>
      <a-form-item label="Designator" name="designator">
        <a-input
          v-model:value="form.designator"
          placeholder="Enter designator"
        />
      </a-form-item>
      <a-form-item label="Mounted" name="isMounted">
        <a-switch v-model:checked="form.isMounted" />
      </a-form-item>
      <a-form-item label="Scrap Factor" name="scrapFactor">
        <a-input-number
          v-model:value="form.scrapFactor"
          placeholder="Enter scrap factor"
          :min="0"
          :max="1"
          :step="0.01"
          style="width: 100%"
        />
      </a-form-item>
      <a-form-item label="Comment" name="comment">
        <a-textarea
          v-model:value="form.comment"
          placeholder="Enter comment"
          :rows="2"
        />
      </a-form-item>
    </template>
  </FormModal>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { BOMItemDTO } from '@/types/bom'
import FormModal from '@/components/FormModal.vue'

interface Props {
  open: boolean
  bomId: number
  item?: BOMItemDTO | null
  isEdit?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  item: null,
  isEdit: false,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', values: BOMItemDTO): void
}>()

const loading = ref(false)

const initialValues = computed(() => {
  if (props.item) {
    return {
      partRevisionId: props.item.partRevisionId,
      quantity: props.item.quantity,
      findNumber: props.item.findNumber,
      designator: props.item.designator || '',
      isMounted: props.item.isMounted ?? true,
      scrapFactor: props.item.scrapFactor ?? 0,
      comment: props.item.comment || '',
    }
  }
  return {
    partRevisionId: undefined,
    quantity: 1,
    findNumber: undefined,
    designator: '',
    isMounted: true,
    scrapFactor: 0,
    comment: '',
  }
})

const rules = {
  partRevisionId: [
    { required: true, message: 'Please enter part revision ID', trigger: 'blur' },
  ],
  quantity: [
    { required: true, message: 'Please enter quantity', trigger: 'blur' },
  ],
}

function handleSubmit(values: Record<string, unknown>): void {
  const bomItem: BOMItemDTO = {
    ...values,
    bomId: props.bomId,
  } as unknown as BOMItemDTO
  emit('submit', bomItem)
}

function handleCancel(): void {
  // FormModal handles the cancel
}
</script>
