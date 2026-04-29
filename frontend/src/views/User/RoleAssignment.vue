<template>
  <div class="role-assignment">
    <div class="role-header">
      <h4>Assigned Roles</h4>
      <a-button type="primary" size="small" @click="showAssignModal = true">
        <template #icon><PlusOutlined /></template>
        Assign Role
      </a-button>
    </div>

    <LoadingSpinner v-if="loading" size="small" />

    <template v-else>
      <a-table
        v-if="roles.length > 0"
        :columns="columns"
        :data-source="roles"
        :pagination="false"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a-tag color="blue">{{ record.name }}</a-tag>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-popconfirm
              title="Are you sure you want to remove this role?"
              @confirm="handleRemoveRole(record.id)"
            >
              <a-button type="link" danger size="small">
                <template #icon><DeleteOutlined /></template>
              </a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>

      <EmptyState
        v-else
        title="No Roles Assigned"
        description="This user has no roles assigned yet."
        icon="inbox"
      />
    </template>

    <!-- Assign Role Modal -->
    <a-modal
      v-model:open="showAssignModal"
      title="Assign Role"
      :confirm-loading="assignLoading"
      @ok="handleAssignRole"
      @cancel="showAssignModal = false"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="Role">
          <a-select
            v-model:value="selectedRoleId"
            placeholder="Select a role"
            :loading="availableRolesLoading"
          >
            <a-select-option
              v-for="role in filteredAvailableRoles"
              :key="role.id"
              :value="role.id"
            >
              {{ role.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import type { TableColumnType } from 'ant-design-vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import EmptyState from '@/components/EmptyState.vue'
import type { RoleDTO } from '@/types/user'
import api from '@/api'

interface Props {
  userId: number
  roles: RoleDTO[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  (e: 'assign', roleId: number): void
  (e: 'remove', roleId: number): void
}>()

const showAssignModal = ref(false)
const assignLoading = ref(false)
const selectedRoleId = ref<number | undefined>(undefined)
const availableRoles = ref<RoleDTO[]>([])
const availableRolesLoading = ref(false)

const columns: TableColumnType[] = [
  {
    title: 'Role Name',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: 'Description',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true,
  },
  {
    title: 'Actions',
    key: 'actions',
    width: 80,
    align: 'center',
  },
]

// Filter out already assigned roles
const filteredAvailableRoles = computed(() => {
  const assignedIds = props.roles.map((r) => r.id)
  return availableRoles.value.filter((r) => !assignedIds.includes(r.id))
})

const fetchAvailableRoles = async () => {
  availableRolesLoading.value = true
  try {
    // Fetch all roles from the API
    // The backend should have a roles endpoint
    const response = await api.get<RoleDTO[]>('/roles')
    availableRoles.value = response.data
  } catch {
    // If roles endpoint doesn't exist, use predefined roles
    availableRoles.value = [
      { id: 1, name: 'ADMIN', description: 'System Administrator' },
      { id: 2, name: 'USER', description: 'Regular User' },
      { id: 3, name: 'MANAGER', description: 'Manager' },
      { id: 4, name: 'ENGINEER', description: 'Engineer' },
    ]
  } finally {
    availableRolesLoading.value = false
  }
}

const handleAssignRole = () => {
  if (!selectedRoleId.value) return

  assignLoading.value = true
  try {
    emit('assign', selectedRoleId.value)
    showAssignModal.value = false
    selectedRoleId.value = undefined
  } finally {
    assignLoading.value = false
  }
}

const handleRemoveRole = (roleId: number) => {
  emit('remove', roleId)
}

onMounted(() => {
  fetchAvailableRoles()
})
</script>

<style scoped>
.role-assignment {
  width: 100%;
}

.role-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.role-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}
</style>
