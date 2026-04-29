<template>
  <div class="user-list">
    <PageHeader
      title="Users"
      subtitle="Manage system users and their roles"
      :breadcrumbs="[
        { label: 'Home', path: '/' },
        { label: 'Users' },
      ]"
    >
      <template #actions>
        <a-button type="primary" @click="showCreateModal">
          <template #icon><PlusOutlined /></template>
          Create User
        </a-button>
      </template>
    </PageHeader>

    <DataTable
      :columns="columns"
      :data="users"
      :loading="loading"
      :searchable="true"
      :exportable="false"
      :page-size="pagination.pageSize"
      @search="handleSearch"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'username'">
          <router-link :to="`/users/${record.id}`">
            {{ record.username }}
          </router-link>
        </template>
        <template v-else-if="column.key === 'isActive'">
          <a-tag :color="record.isActive ? 'green' : 'red'">
            {{ record.isActive ? 'Active' : 'Inactive' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'roles'">
          <a-tag v-for="role in record.roles" :key="role.id" color="blue">
            {{ role.name }}
          </a-tag>
          <span v-if="!record.roles?.length" class="no-roles">No roles</span>
        </template>
        <template v-else-if="column.key === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>
        <template v-else-if="column.key === 'actions'">
          <a-space>
            <a-button type="link" size="small" @click="showEditModal(record)">
              <template #icon><EditOutlined /></template>
            </a-button>
            <a-popconfirm
              title="Are you sure you want to delete this user?"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">
                <template #icon><DeleteOutlined /></template>
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <UserForm
      v-model:open="formVisible"
      :user="selectedUser"
      :loading="formLoading"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import type { TableColumnType } from 'ant-design-vue'
import PageHeader from '@/components/PageHeader.vue'
import DataTable from '@/components/DataTable.vue'
import UserForm from './UserForm.vue'
import { useUserStore } from '@/stores/user'
import type { UserDTO, CreateUserRequest } from '@/types/user'

const userStore = useUserStore()
const { users, loading, pagination } = userStore

const formVisible = ref(false)
const formLoading = ref(false)
const selectedUser = ref<UserDTO | null>(null)
const searchQuery = ref('')

const columns: TableColumnType[] = [
  {
    title: 'Username',
    dataIndex: 'username',
    key: 'username',
    sorter: true,
    width: 150,
  },
  {
    title: 'Email',
    dataIndex: 'email',
    key: 'email',
    sorter: true,
    ellipsis: true,
  },
  {
    title: 'Full Name',
    dataIndex: 'fullName',
    key: 'fullName',
    ellipsis: true,
  },
  {
    title: 'Status',
    dataIndex: 'isActive',
    key: 'isActive',
    width: 100,
    align: 'center',
  },
  {
    title: 'Roles',
    dataIndex: 'roles',
    key: 'roles',
    width: 200,
  },
  {
    title: 'Created At',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 150,
    sorter: true,
  },
  {
    title: 'Actions',
    key: 'actions',
    width: 100,
    align: 'center',
  },
]

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString()
}

const fetchUsers = async (page = 1) => {
  try {
    await userStore.fetchUsers({
      page,
      size: pagination.pageSize,
    })
  } catch {
    message.error('Failed to fetch users')
  }
}

const handleSearch = (value: string) => {
  searchQuery.value = value
  // For now, fetch all users and filter client-side
  // The DataTable component handles client-side filtering
  fetchUsers(1)
}

const handleTableChange = (pag: Record<string, unknown>) => {
  fetchUsers(pag.current as number)
}

const showCreateModal = () => {
  selectedUser.value = null
  formVisible.value = true
}

const showEditModal = (user: UserDTO) => {
  selectedUser.value = { ...user }
  formVisible.value = true
}

const handleFormSubmit = async (values: CreateUserRequest | UserDTO) => {
  formLoading.value = true
  try {
    if (selectedUser.value?.id) {
      await userStore.updateUser(selectedUser.value.id, values as UserDTO)
      message.success('User updated successfully')
    } else {
      await userStore.createUser(values as CreateUserRequest)
      message.success('User created successfully')
    }
    formVisible.value = false
    fetchUsers(pagination.current)
  } catch {
    message.error('Failed to save user')
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await userStore.deleteUser(id)
    message.success('User deleted successfully')
    fetchUsers(pagination.current)
  } catch {
    message.error('Failed to delete user')
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-list {
  width: 100%;
}

.no-roles {
  color: rgba(0, 0, 0, 0.45);
  font-style: italic;
}
</style>
