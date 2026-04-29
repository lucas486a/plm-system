<template>
  <a-layout class="layout-container">
    <!-- Mobile overlay -->
    <div
      class="mobile-overlay"
      :class="{ visible: isMobile && !collapsed }"
      @click="collapsed = true"
    />

    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :width="240"
      :collapsed-width="isMobile ? 0 : 80"
      class="layout-sider"
      :class="{ 'mobile-sider': isMobile }"
    >
      <div class="logo">
        <h1 v-if="!collapsed">PLM System</h1>
        <h1 v-else>PLM</h1>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="inline"
        @click="handleMenuClick"
      >
        <a-menu-item key="dashboard">
          <template #icon>
            <DashboardOutlined />
          </template>
          <span>Dashboard</span>
        </a-menu-item>
        <a-menu-item key="products">
          <template #icon>
            <ShoppingOutlined />
          </template>
          <span>Products</span>
        </a-menu-item>
        <a-menu-item key="bom">
          <template #icon>
            <ApartmentOutlined />
          </template>
          <span>BOM</span>
        </a-menu-item>
        <a-menu-item key="ecr">
          <template #icon>
            <FileTextOutlined />
          </template>
          <span>ECR</span>
        </a-menu-item>
        <a-menu-item key="eco">
          <template #icon>
            <SwapOutlined />
          </template>
          <span>ECO</span>
        </a-menu-item>
        <a-menu-item key="users">
          <template #icon>
            <UserOutlined />
          </template>
          <span>Users</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="layout-header">
        <a-button
          type="text"
          class="trigger-btn"
          @click="toggleSidebar"
        >
          <MenuUnfoldOutlined v-if="collapsed" />
          <MenuFoldOutlined v-else />
        </a-button>
        <div class="header-right">
          <a-dropdown>
            <a class="user-dropdown" @click.prevent>
              <a-avatar :size="32">
                <template #icon><UserOutlined /></template>
              </a-avatar>
              <span class="username">{{ username }}</span>
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile">
                  <UserOutlined />
                  <span>Profile</span>
                </a-menu-item>
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined />
                  <span>Logout</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content class="layout-content">
        <slot />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  DashboardOutlined,
  ShoppingOutlined,
  ApartmentOutlined,
  FileTextOutlined,
  SwapOutlined,
  UserOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  LogoutOutlined,
} from '@ant-design/icons-vue'

interface Props {
  username?: string
}

withDefaults(defineProps<Props>(), {
  username: 'Admin',
})

const router = useRouter()
const collapsed = ref(false)
const isMobile = ref(false)
const selectedKeys = ref<string[]>(['dashboard'])

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    collapsed.value = true
  }
}

const toggleSidebar = () => {
  collapsed.value = !collapsed.value
}

const handleMenuClick = () => {
  // Close sidebar on mobile when menu item is clicked
  if (isMobile.value) {
    collapsed.value = true
  }
}

const handleLogout = () => {
  // TODO: Implement actual logout logic
  router.push('/login')
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.layout-sider {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 10;
  overflow: auto;
}

.layout-sider.mobile-sider {
  z-index: 10;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  margin: 0;
}

.logo h1 {
  color: #fff;
  font-size: 18px;
  margin: 0;
  white-space: nowrap;
}

.layout-header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 9;
}

.trigger-btn {
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;
}

.trigger-btn:hover {
  color: #1890ff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
}

.layout-content {
  margin: 24px;
  padding: 24px;
  background: #fff;
  min-height: 280px;
  border-radius: 4px;
}

/* Adjust content margin for sidebar */
.layout-container > :deep(.ant-layout) {
  margin-left: 240px;
  transition: margin-left 0.2s;
}

.layout-container > :deep(.ant-layout-sider-collapsed) + .ant-layout {
  margin-left: 80px;
}

/* Mobile styles */
@media (max-width: 768px) {
  .layout-container > :deep(.ant-layout) {
    margin-left: 0 !important;
  }

  .layout-header {
    padding: 0 12px;
  }

  .layout-content {
    margin: 12px;
    padding: 16px;
  }

  .username {
    display: none;
  }
}
</style>
