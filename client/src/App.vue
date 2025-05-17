<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/services/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const drawer = ref(true)
const rail = ref(true)

const user = computed(() => userStore.user)
const isAuthPage = computed(() => {
  return ['/login', '/register'].includes(route.path)
})

const userTypeText = computed(() => {
  const typeMap = {
    0: '病人',
    1: '医生',
    2: '管理员'
  }
  return typeMap[user.value?.userType] || ''
})

// 获取用户名的首字母
const userInitial = computed(() => {
  return user.value?.realName ? user.value.realName.charAt(0).toUpperCase() : ''
})

const menuItems = computed(() => {
  const baseItems = [
    { 
      title: '首页', 
      icon: 'mdi-home', 
      to: user.value?.userType === 2 ? '/admin/home' : 
          user.value?.userType === 1 ? '/doctor/home' : '/patient/home'
    },
    { title: '个人中心', icon: 'mdi-account', to: '/profile' }
  ]

  if (user.value?.userType === 2) {
    return [
      ...baseItems,
      { title: '用户管理', icon: 'mdi-account-group', to: '/admin/users' },
      { title: '医疗方向管理', icon: 'mdi-hospital-building', to: '/admin/directions' },
      { title: '智能体管理', icon: 'mdi-robot', to: '/admin/agents' },
      { title: '提示词模板', icon: 'mdi-file-document-outline', to: '/admin/templates' },
      { title: '向量数据库', icon: 'mdi-database', to: '/admin/vector' },
      { title: '精确查找数据库', icon: 'mdi-database-search', to: '/admin/precise' }
    ]
  }

  if (user.value?.userType === 1) {
    return [
      ...baseItems,
      { title: '处方管理', icon: 'mdi-file-document-multiple', to: '/doctor/prescriptions' },
      { title: '向量数据库', icon: 'mdi-database', to: '/doctor/vector' },
      { title: '精确查找数据库', icon: 'mdi-database-search', to: '/doctor/precise' }
    ]
  }

  if (user.value?.userType === 0) {
    return [
      ...baseItems,
      { title: '智能体', icon: 'mdi-robot', to: '/patient/agents' },
      { title: '聊天记录', icon: 'mdi-message-text-clock', to: '/patient/conversations' },
      { title: '处方列表', icon: 'mdi-medical-bag', to: '/patient/prescriptions' }
    ]
  }

  return baseItems
})

const handleLogout = async () => {
  try {
    const response = await authApi.logout()
    // 无论API返回什么，都清除用户状态并跳转到登录页
    userStore.clearUser()
    router.push('/login')
  } catch (error) {
    console.error('注销失败:', error)
    // 即使发生错误，也清除本地用户状态
    userStore.clearUser()
    router.push('/login')
  }
}
</script>

<template>
  <v-app>
    <v-navigation-drawer
      v-if="!isAuthPage"
      v-model="drawer"
      :rail="rail"
      permanent
      @mouseenter="rail = false"
      @mouseleave="rail = true"
      class="sidebar"
    >
      <v-list-item
        :title="user?.realName"
        :subtitle="userTypeText"
        class="sidebar-header"
      >
        <template v-slot:prepend>
          <div v-if="rail" class="user-avatar">
            <div class="avatar-ring"></div>
            <span class="avatar-text">{{ userInitial }}</span>
          </div>
          <v-icon v-else icon="mdi-account-circle" size="large" color="primary"></v-icon>
        </template>
        <template v-slot:append>
          <v-btn
            icon="mdi-chevron-left"
            variant="text"
            @click.stop="rail = !rail"
          ></v-btn>
        </template>
      </v-list-item>

      <v-divider></v-divider>

      <v-list density="compact" nav class="sidebar-menu">
        <v-list-item
          v-for="item in menuItems"
          :key="item.title"
          :value="item.title"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="item.title"
          class="sidebar-item"
          :class="{ 'active': $route.path === item.to }"
        >
        </v-list-item>
      </v-list>

      <template v-slot:append>
        <div class="pa-2">
          <v-btn
            block
            color="error"
            variant="tonal"
            :prepend-icon="rail ? '' : 'mdi-logout'"
            @click="handleLogout"
            class="logout-btn"
          >
            <template v-if="!rail">退出登录</template>
            <template v-else>
              <v-icon>mdi-logout</v-icon>
            </template>
          </v-btn>
        </div>
      </template>
    </v-navigation-drawer>

    <v-main>
      <router-view></router-view>
    </v-main>
  </v-app>
</template>

<style scoped>
.sidebar {
  background: linear-gradient(180deg, #f5f7fa 0%, #e4e8f0 100%);
  border-right: 1px solid rgba(0, 0, 0, 0.05);
}

.sidebar-header {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.user-avatar {
  width: 32px;
  height: 32px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid rgb(var(--v-theme-primary));
  opacity: 0.8;
  animation: pulse 2s infinite;
}

.avatar-text {
  font-size: 0.9rem;
  font-weight: 600;
  color: rgb(var(--v-theme-primary));
  z-index: 1;
  background: white;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.4;
  }
  100% {
    transform: scale(1);
    opacity: 0.8;
  }
}

.sidebar-menu {
  padding: 8px;
}

.sidebar-item {
  border-radius: 8px;
  margin: 4px 0;
  transition: all 0.3s ease;
}

.sidebar-item:hover {
  background-color: rgba(var(--v-theme-primary), 0.1);
}

.sidebar-item.active {
  background-color: rgb(var(--v-theme-primary));
  color: white;
}

.sidebar-item.active :deep(.v-list-item__prepend) {
  color: white;
}

.logout-btn {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

:deep(.v-navigation-drawer__scrim) {
  background-color: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(4px);
}
</style>
