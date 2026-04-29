<template>
  <div class="login-container">
    <a-card class="login-card" title="PLM System Login">
      <a-form
        :model="formState"
        name="loginForm"
        layout="vertical"
        @finish="handleLogin"
      >
        <a-form-item
          label="Username"
          name="username"
          :rules="[{ required: true, message: 'Please input your username' }]"
        >
          <a-input v-model:value="formState.username" placeholder="Username">
            <template #prefix>
              <UserOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          label="Password"
          name="password"
          :rules="[{ required: true, message: 'Please input your password' }]"
        >
          <a-input-password v-model:value="formState.password" placeholder="Password">
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            :loading="authStore.loading"
            block
          >
            Log in
          </a-button>
        </a-form-item>

        <a-alert
          v-if="authStore.error"
          :message="authStore.error"
          type="error"
          show-icon
          closable
          @close="authStore.error = null"
        />
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const formState = reactive({
  username: '',
  password: '',
})

async function handleLogin() {
  const success = await authStore.login(formState.username, formState.password)
  if (success) {
    router.push('/')
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}

.login-card {
  width: 400px;
}
</style>
