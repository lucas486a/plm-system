<template>
  <a-modal
    :open="open"
    :title="title"
    :confirm-loading="confirmLoading"
    :width="width"
    :mask-closable="false"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="labelCol"
      :wrapper-col="wrapperCol"
    >
      <slot :form="formState" />
    </a-form>
    <template #footer>
      <a-button @click="handleCancel">Cancel</a-button>
      <a-button
        type="primary"
        :loading="confirmLoading"
        @click="handleOk"
      >
        {{ okText }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'

interface Props {
  open: boolean
  title: string
  confirmLoading?: boolean
  width?: number | string
  okText?: string
  rules?: Record<string, unknown>
  initialValues?: Record<string, unknown>
  labelCol?: { span: number }
  wrapperCol?: { span: number }
}

const props = withDefaults(defineProps<Props>(), {
  confirmLoading: false,
  width: 520,
  okText: 'Submit',
  rules: () => ({}),
  initialValues: () => ({}),
  labelCol: () => ({ span: 6 }),
  wrapperCol: () => ({ span: 16 }),
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'ok', values: Record<string, unknown>): void
  (e: 'cancel'): void
}>()

const formRef = ref<FormInstance | null>(null)
const formState = ref<Record<string, unknown>>({ ...props.initialValues })

watch(
  () => props.open,
  (newVal) => {
    if (newVal) {
      formState.value = { ...props.initialValues }
      formRef.value?.resetFields()
    }
  }
)

const handleOk = async () => {
  try {
    const values = await formRef.value?.validateFields()
    emit('ok', values as Record<string, unknown>)
  } catch (error) {
    // Validation failed
    console.error('Validation failed:', error)
  }
}

const handleCancel = () => {
  formRef.value?.resetFields()
  emit('update:open', false)
  emit('cancel')
}
</script>

<style scoped>
/* Add any custom styles here */
</style>
