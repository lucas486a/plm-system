<template>
  <div class="user-detail">
    <PageHeader
      :title="currentUser?.fullName || currentUser?.username || 'User Details'"
      :subtitle="currentUser?.email"
      :breadcrumbs="[
        { label: 'Home', path: '/' },
        { label: 'Users', path: '/users' },
        { label: currentUser?.username || 'Detail' },
      ]"
    >
      <template #actions>
        <a-button @click="showEditModal">
          <template #icon><EditOutlined /></template>
          Edit
        </a-button>
        <a-popconfirm
          title="Are you sure you want to delete this user?"
          @confirm="handleDelete"
        >
          <a-button danger>
            <template #icon><DeleteOutlined /></template>
            Delete
          </a-button>
        </a-popconfirm>
      </template>
    </PageHeader>

    <LoadingSpinner v-if="loading && !currentUser" />

    <template v-else-if="currentUser">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="info" tab="Basic Information">
          <a-descriptions bordered :column="{ xxl: 2, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }">
            <a-descriptions-item label="Username">
              {{ currentUser.username }}
            </a-descriptions-item>
            <a-descriptions-item label="Email">
              {{ currentUser.email }}
            </a-descriptions-item>
            <a-descriptions-item label="Full Name">
              {{ currentUser.fullName || 'N/A' }}
            </a-descriptions-item>
            <a-descriptions-item label="Status">
              <a-tag :color="currentUser.isActive ? 'green' : 'red'">
                {{ currentUser.isActive ? 'Active' : 'Inactive' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="Version">
              {{ currentUser.version || 'N/A' }}
            </a-descriptions-item>
            <a-descriptions-item label="Created By">
              {{ currentUser.createdBy || 'N/A' }}
            </a-descriptions-item>
            <a-descriptions-item label="Created At">
              {{ formatDate(currentUser.createdAt) }}
            </a-descriptions-item>
            <a-descriptions-item label="Updated At">
              {{ formatDate(currentUser.updatedAt) }}
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>

        <a-tab-pane key="roles" tab="Roles">
          <RoleAssignment
            :user-id="userId"
            :roles="userRoles"
            :loading="rolesLoading"
            @assign="handleAssignRole"
            @remove="handleRemoveRole"
          />
        </a-tab-pane>
      </a-tabs>
    </template>

    <EmptyState
      v-else
      title="User Not Found"
      description="The requested user could not be found."
      icon="file"
      :show-action="true"
      action-text="Back to Users"
      @action="$router.push('/users')"
    />

    <UserForm
      v-model:open="editModalVisible"
      :user="currentUser"
      :loading="editLoading"
      @submit="handleEditSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import EmptyState from '@/components/EmptyState.vue'
import RoleAssignment from './RoleAssignment.vue'
import UserForm from './UserForm.vue'
import { useUserStore } from '@/stores/user'
import type { UserDTO, CreateUserRequest } from '@/types/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const { currentUser, userRoles, loading } = userStore

const activeTab = ref('info')
const editModalVisible = ref(false)
const editLoading = ref(false)
const rolesLoading = ref(false)

const userId = computed(() => Number(route.params.id))

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString()
}

const fetchUserData = async () => {
  try {
    await userStore.fetchUserById(userId.value)
    await fetchRoles()
  } catch {
    message.error('Failed to fetch user details')
  }
}

const fetchRoles = async () => {
  rolesLoading.value = true
  try {
    await userStore.fetchUserRoles(userId.value)
  } catch {
    message.error('Failed to fetch user roles')
  } finally {
    rolesLoading.value = false
  }
}

const showEditModal = () => {
  editModalVisible.value = true
}

const handleEditSubmit = async (values: CreateUserRequest | UserDTO) => {
  editLoading.value = true
  try {
    await userStore.updateUser(userId.value, values as UserDTO)
    message.success('User updated successfully')
    editModalVisible.value = false
  } catch {
    message.error('Failed to update user')
  } finally {
    editLoading.value = false
  }
}

const handleDelete = async () => {
  try {
    await userStore.deleteUser(userId.value)
    message.success('User deleted successfully')
    router.push('/users')
  } catch {
    message.error('Failed to delete user')
  }
}

const handleAssignRole = async (roleId: number) => {
  try {
    await userStore.assignRole(userId.value, roleId)
    message.success('Role assigned successfully')
  } catch {
    message.error('Failed to assign role')
  }
}

const handleRemoveRole = async (roleId: number) => {
  try {
    await userStore.removeRole(userId.value, roleId)
    message.success('Role removed successfully')
  } catch {
    message.error('Failed to remove role')
  }
}

watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      fetchUserData()
    }
  }
)

onMounted(() => {
  if (userId.value) {
    fetchUserData()
  }
})
</script>

<style scoped>
.user-detail {
  width: 100%;
}
</style>
