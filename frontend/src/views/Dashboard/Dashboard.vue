<template>
  <Layout :username="authStore.user?.username">
    <div class="dashboard">
      <PageHeader title="Dashboard" subtitle="Overview of your PLM system" />

      <a-spin :spinning="dashboardStore.loading">
        <a-row :gutter="[16, 16]" class="stat-cards">
          <a-col :xs="24" :sm="12" :lg="6">
            <StatCard
              title="Total Parts"
              :value="dashboardStore.stats?.totalParts ?? 0"
              :icon="ShoppingOutlined"
              color="#1890ff"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :lg="6">
            <StatCard
              title="Documents"
              :value="dashboardStore.stats?.totalDocuments ?? 0"
              :icon="FileTextOutlined"
              color="#52c41a"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :lg="6">
            <StatCard
              title="Open ECRs"
              :value="dashboardStore.stats?.openECRs ?? 0"
              :icon="ExclamationCircleOutlined"
              color="#faad14"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :lg="6">
            <StatCard
              title="Open ECOs"
              :value="dashboardStore.stats?.openECOs ?? 0"
              :icon="SwapOutlined"
              color="#ff4d4f"
            />
          </a-col>
        </a-row>

        <a-row :gutter="[16, 16]" class="content-row">
          <a-col :xs="24" :lg="16">
            <RecentActivity :activities="dashboardStore.stats?.recentActivities ?? []" />
          </a-col>
          <a-col :xs="24" :lg="8">
            <QuickActions />
          </a-col>
        </a-row>
      </a-spin>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import {
  ShoppingOutlined,
  FileTextOutlined,
  ExclamationCircleOutlined,
  SwapOutlined,
} from '@ant-design/icons-vue'
import Layout from '@/components/Layout.vue'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from './StatCard.vue'
import RecentActivity from './RecentActivity.vue'
import QuickActions from './QuickActions.vue'
import { useDashboardStore } from '@/stores/dashboard'
import { useAuthStore } from '@/stores/auth'

const dashboardStore = useDashboardStore()
const authStore = useAuthStore()

onMounted(async () => {
  try {
    await dashboardStore.fetchStats()
  } catch {
    // Error is handled in store
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-cards {
  margin-bottom: 16px;
}

.content-row {
  margin-top: 16px;
}
</style>
