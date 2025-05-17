<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/services/api'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'

const userStore = useUserStore()
const user = computed(() => userStore.user)
const loading = ref(false)
const error = ref('')

// 用户类型文本
const userTypeText = computed(() => {
  const typeMap = {
    0: '病人',
    1: '医生',
    2: '管理员'
  }
  return typeMap[user.value?.userType] || '未知'
})

// 性别文本
const genderText = computed(() => {
  const genderMap = {
    1: '男',
    2: '女'
  }
  return genderMap[user.value?.gender] || '未知'
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19).replace(/-/g, '/')
}

// 刷新用户信息
const refreshUserInfo = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await authApi.getUserInfo()
    if (response.code === 200) {
      userStore.setUser(response.data)
    } else {
      error.value = response.message || '获取用户信息失败'
    }
  } catch (err) {
    console.error('获取用户信息错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 组件挂载时刷新用户信息
onMounted(() => {
  refreshUserInfo()
})
</script>

<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-card class="mb-6" flat>
          <div 
            class="pa-5 rounded-t-lg" 
            style="background: linear-gradient(to right, #1976D2, #64B5F6);"
          >
            <div class="d-flex align-center">
              <div>
                <v-icon icon="mdi-account-circle" size="large" color="white" class="me-3"></v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1 d-inline-block">个人中心</h1>
                <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
                  查看您的个人资料信息
                </p>
              </div>
              <v-spacer></v-spacer>
              <v-btn
                color="white"
                variant="tonal"
                prepend-icon="mdi-refresh"
                @click="refreshUserInfo"
                :loading="loading"
                class="refresh-btn"
              >
                刷新信息
              </v-btn>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <v-row>
      <v-col cols="12" md="4">
        <v-card class="profile-card">
          <div class="profile-header">
            <div class="avatar-container">
              <div class="avatar-ring"></div>
              <div class="avatar-text">{{ user?.realName ? user.realName.charAt(0).toUpperCase() : '' }}</div>
            </div>
          </div>
          
          <v-card-text class="text-center profile-info">
            <h2 class="text-h5 font-weight-bold">{{ user?.realName || '加载中...' }}</h2>
            <v-chip
              :color="user?.userType === 0 ? 'info' : user?.userType === 1 ? 'success' : 'primary'"
              class="mt-2"
            >
              {{ userTypeText }}
            </v-chip>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col cols="12" md="8">
        <v-card class="info-card">
          <v-card-title class="d-flex align-center py-4 px-6">
            <v-icon icon="mdi-card-account-details" size="large" class="me-2 text-primary"></v-icon>
            基本信息
          </v-card-title>
          
          <v-divider></v-divider>
          
          <div class="position-relative">
            <loading-indicator :loading="loading" />
            
            <v-alert
              v-if="error"
              type="error"
              variant="tonal"
              class="ma-4"
              density="compact"
            >
              {{ error }}
            </v-alert>
            
            <v-list>
              <v-list-item>
                <template v-slot:prepend>
                  <v-icon color="primary" class="me-3">mdi-account</v-icon>
                </template>
                <v-list-item-title>用户名</v-list-item-title>
                <v-list-item-subtitle>{{ user?.username || '-' }}</v-list-item-subtitle>
              </v-list-item>
              
              <v-divider inset></v-divider>
              
              <v-list-item>
                <template v-slot:prepend>
                  <v-icon color="primary" class="me-3">mdi-badge-account</v-icon>
                </template>
                <v-list-item-title>真实姓名</v-list-item-title>
                <v-list-item-subtitle>{{ user?.realName || '-' }}</v-list-item-subtitle>
              </v-list-item>
              
              <v-divider inset></v-divider>
              
              <v-list-item>
                <template v-slot:prepend>
                  <v-icon color="primary" class="me-3">mdi-gender-male-female</v-icon>
                </template>
                <v-list-item-title>性别</v-list-item-title>
                <v-list-item-subtitle>{{ genderText }}</v-list-item-subtitle>
              </v-list-item>
              
              <v-divider inset></v-divider>
              
              <v-list-item>
                <template v-slot:prepend>
                  <v-icon color="primary" class="me-3">mdi-calendar-account</v-icon>
                </template>
                <v-list-item-title>年龄</v-list-item-title>
                <v-list-item-subtitle>{{ user?.age || '-' }} 岁</v-list-item-subtitle>
              </v-list-item>
              
              <template v-if="user?.userType === 1">
                <v-divider inset></v-divider>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon color="primary" class="me-3">mdi-hospital-building</v-icon>
                  </template>
                  <v-list-item-title>医疗方向</v-list-item-title>
                  <v-list-item-subtitle>{{ user?.direction?.name || '-' }}</v-list-item-subtitle>
                </v-list-item>
              </template>
              
              <v-divider inset></v-divider>
              
              <v-list-item>
                <template v-slot:prepend>
                  <v-icon color="primary" class="me-3">mdi-clock</v-icon>
                </template>
                <v-list-item-title>注册时间</v-list-item-title>
                <v-list-item-subtitle>{{ formatDate(user?.createTime) }}</v-list-item-subtitle>
              </v-list-item>
            </v-list>
          </div>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.v-container {
  max-width: 1400px;
  margin: 0 auto;
}

.profile-card, .info-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  height: 100%;
}

.profile-header {
  height: 120px;
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgba(var(--v-theme-primary), 0.7) 100%);
  border-radius: 12px 12px 0 0;
  position: relative;
  display: flex;
  justify-content: center;
}

.avatar-container {
  width: 100px;
  height: 100px;
  position: absolute;
  bottom: -50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 3px solid white;
  background-color: rgba(255, 255, 255, 0.8);
}

.avatar-text {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: rgb(var(--v-theme-primary));
  color: white;
  font-size: 2rem;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.profile-info {
  padding-top: 60px; /* 增加顶部内边距，让姓名和标签往下移 */
}

.position-relative {
  position: relative;
}

.refresh-btn {
  transition: transform 0.2s;
}

.refresh-btn:hover {
  transform: translateY(-2px);
}
</style> 