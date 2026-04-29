<template>
  <FormModal
    :open="open"
    :title="isEdit ? 'Edit User' : 'Create User'"
    :confirm-loading="loading"
    :initial-values="initialValues"
    :rules="rules"
    :width="600"
    @update:open="$emit('update:open', $event)"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <template #default="{ form }">
      <a-form-item label="Username" name="username">
        <a-input
          v-model:value="form.username"
          placeholder="Enter username"
          :disabled="isEdit"
        />
      </a-form-item>

      <a-form-item label="Email" name="email">
        <a-input
          v-model:value="form.email"
          placeholder="Enter email address"
        />
      </a-form-item>

      <a-form-item label="Full Name" name="fullName">
        <a-input
          v-model:value="form.fullName"
          placeholder="Enter full name"
        />
      </a-form-item>

      <a-form-item v-if="!isEdit" label="Password" name="password">
        <a-input-password
          v-model:value="form.password"
          placeholder="Enter password"
        />
      </a-form-item>
    </template>
  </FormModal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import FormModal from '@/components/FormModal.vue'
import type { UserDTO, CreateUserRequest } from '@/types/user'

interface Props {
  open: boolean
  user?: UserDTO | null
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  user: null,
  loading: false,
})

const emit = defineEmits(['update:open', 'submit'])

const isEdit = computed(() => !!props.user?.id)

const initialValues = computed(() => {
  if (props.user) {
    return {
      username: props.user.username,
      email: props.user.email,
      fullName: props.user.fullName || '',
      password: '',
    }
  }
  return {
    username: '',
    email: '',
    fullName: '',
    password: '',
  }
})

const rules = {
  username: [
    { required: true, message: 'Please enter username', trigger: 'blur' },
    { min: 3, max: 50, message: 'Username must be 3-50 characters', trigger: 'blur' },
  ],
  email: [
    { required: true, message: 'Please enter email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email', trigger: 'blur' },
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' },
  ],
}

const handleSubmit = (values: Record<string, unknown>) => {
  if (isEdit.value) {
    const userData: UserDTO = {
      username: values.username as string,
      email: values.email as string,
      fullName: values.fullName as string,
    }
    emit('submit', userData)
  } else {
    const userData: CreateUserRequest = {
      username: values.username as string,
      email: values.email as string,
      fullName: values.fullName as string,
      password: values.password as string,
    }
    emit('submit', userData)
  }
}

const handleCancel = () => {
  // Cancel is handled by FormModal
}
</script>
