<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { doctorApi } from '@/services/doctorApi'

const router = useRouter()
const userStore = useUserStore()
const user = ref(userStore.user)
const loading = ref(true)
const error = ref(null)
const showError = ref(false)
const stats = ref({
  totalPrescriptions: 0,
  pendingReview: 0,
  approvedPrescriptions: 0,
  rejectedPrescriptions: 0
})

// 设置错误信息并显示
const setError = (message) => {
  error.value = message
  showError.value = true
}

// 关闭错误提示
const closeError = () => {
  showError.value = false
}

// 加载统计数据
const loadStats = async () => {
  loading.value = true
  
  try {
    const response = await doctorApi.getDashboardStats()
    
    if (response.code === 200 && response.data) {
      stats.value = response.data
    } else {
      setError(response.message || '加载统计数据失败')
    }
  } catch (err) {
    console.error('加载统计数据错误:', err)
    setError('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 导航到处方列表
const goToPrescriptions = (status = null) => {
  router.push({
    name: 'doctorPrescriptions',
    query: status !== null ? { status } : {}
  })
}

// 导航到个人资料
const goToProfile = () => {
  router.push({ name: 'doctorProfile' })
}

// 欢迎信息
const greetingMessage = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
}

// 组件挂载时加载统计数据
onMounted(() => {
  loadStats()
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
                <v-icon icon="mdi-home" size="large" color="white" class="me-3"></v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1 d-inline-block">医生主页</h1>
                <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
                  {{ greetingMessage() }}，{{ user.name || '医生' }}，欢迎使用医疗智能问答系统
                </p>
              </div>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 统计卡片 -->
    <v-row class="main-content-row">
      <v-col cols="12">
        <v-card class="content-card">
          <div class="card-color-stripe primary-stripe"></div>
          <div class="card-inner">
            <v-card-title class="d-flex align-center pb-2 pt-4 px-4">
              <v-icon class="mr-2">mdi-chart-box</v-icon>
              处方统计
            </v-card-title>
            
            <v-divider></v-divider>
            
            <v-card-text class="pa-4">
              <v-row>
                <!-- 总处方数 -->
                <v-col cols="12" sm="6" md="3">
                  <v-card
                    class="stat-card"
                    color="primary"
                    theme="dark"
                    @click="goToPrescriptions()"
                  >
                    <v-card-text class="d-flex flex-column align-center pa-4">
                      <v-icon icon="mdi-file-document-multiple" size="48" class="mb-2"></v-icon>
                      <div class="text-h4 font-weight-bold mb-1">
                        {{ loading ? '...' : stats.totalPrescriptions }}
                      </div>
                      <div class="text-subtitle-1">总处方数</div>
                    </v-card-text>
                  </v-card>
                </v-col>
                
                <!-- 待审核处方 -->
                <v-col cols="12" sm="6" md="3">
                  <v-card
                    class="stat-card"
                    color="warning"
                    theme="dark"
                    @click="goToPrescriptions(0)"
                  >
                    <v-card-text class="d-flex flex-column align-center pa-4">
                      <v-icon icon="mdi-clock-outline" size="48" class="mb-2"></v-icon>
                      <div class="text-h4 font-weight-bold mb-1">
                        {{ loading ? '...' : stats.pendingReview }}
                      </div>
                      <div class="text-subtitle-1">待审核</div>
                    </v-card-text>
                  </v-card>
                </v-col>
                
                <!-- 已通过处方 -->
                <v-col cols="12" sm="6" md="3">
                  <v-card
                    class="stat-card"
                    color="success"
                    theme="dark"
                    @click="goToPrescriptions(1)"
                  >
                    <v-card-text class="d-flex flex-column align-center pa-4">
                      <v-icon icon="mdi-check-circle" size="48" class="mb-2"></v-icon>
                      <div class="text-h4 font-weight-bold mb-1">
                        {{ loading ? '...' : stats.approvedPrescriptions }}
                      </div>
                      <div class="text-subtitle-1">已通过</div>
                    </v-card-text>
                  </v-card>
                </v-col>
                
                <!-- 已拒绝处方 -->
                <v-col cols="12" sm="6" md="3">
                  <v-card
                    class="stat-card"
                    color="error"
                    theme="dark"
                    @click="goToPrescriptions(2)"
                  >
                    <v-card-text class="d-flex flex-column align-center pa-4">
                      <v-icon icon="mdi-close-circle" size="48" class="mb-2"></v-icon>
                      <div class="text-h4 font-weight-bold mb-1">
                        {{ loading ? '...' : stats.rejectedPrescriptions }}
                      </div>
                      <div class="text-subtitle-1">已拒绝</div>
                    </v-card-text>
                  </v-card>
                </v-col>
              </v-row>
            </v-card-text>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 快速操作卡片 -->
    <v-row class="mt-4">
      <v-col cols="12">
        <v-card class="content-card">
          <div class="card-color-stripe info-stripe"></div>
          <div class="card-inner">
            <v-card-title class="d-flex align-center pb-2 pt-4 px-4">
              <v-icon class="mr-2">mdi-lightning-bolt</v-icon>
              快速操作
            </v-card-title>
            
            <v-divider></v-divider>
            
            <v-card-text class="pa-4">
              <v-row>
                <!-- 查看待审核处方 -->
                <v-col cols="12" sm="6" md="4">
                  <v-card
                    variant="outlined"
                    class="action-item"
                    @click="goToPrescriptions(0)"
                  >
                    <v-card-text class="d-flex align-center">
                      <v-avatar color="warning" class="mr-4">
                        <v-icon icon="mdi-clipboard-check" color="white"></v-icon>
                      </v-avatar>
                      <div>
                        <div class="text-subtitle-1 font-weight-bold">审核处方</div>
                        <div class="text-body-2 text-medium-emphasis">查看并审核待处理的处方</div>
                      </div>
                    </v-card-text>
                  </v-card>
                </v-col>
                
                <!-- 查看所有处方 -->
                <v-col cols="12" sm="6" md="4">
                  <v-card
                    variant="outlined"
                    class="action-item"
                    @click="goToPrescriptions()"
                  >
                    <v-card-text class="d-flex align-center">
                      <v-avatar color="primary" class="mr-4">
                        <v-icon icon="mdi-format-list-bulleted" color="white"></v-icon>
                      </v-avatar>
                      <div>
                        <div class="text-subtitle-1 font-weight-bold">所有处方</div>
                        <div class="text-body-2 text-medium-emphasis">查看所有处方记录</div>
                      </div>
                    </v-card-text>
                  </v-card>
                </v-col>
                
                <!-- 个人资料 -->
                <v-col cols="12" sm="6" md="4">
                  <v-card
                    variant="outlined"
                    class="action-item"
                    @click="goToProfile"
                  >
                    <v-card-text class="d-flex align-center">
                      <v-avatar color="secondary" class="mr-4">
                        <v-icon icon="mdi-account-cog" color="white"></v-icon>
                      </v-avatar>
                      <div>
                        <div class="text-subtitle-1 font-weight-bold">个人资料</div>
                        <div class="text-body-2 text-medium-emphasis">查看和更新个人信息</div>
                      </div>
                    </v-card-text>
                  </v-card>
                </v-col>
              </v-row>
            </v-card-text>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 错误提示 -->
    <v-snackbar
      v-model="showError"
      color="error"
      timeout="3000"
    >
      {{ error }}
      <template v-slot:actions>
        <v-btn
          color="white"
          variant="text"
          @click="closeError"
        >
          关闭
        </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<style scoped>
.v-container {
  max-width: 1400px;
  margin: 0 auto;
}

.main-content-row {
  min-height: 200px;
}

.content-card {
  border-radius: 16px !important;
  transition: transform 0.3s, box-shadow 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05) !important;
  background: linear-gradient(to bottom, rgba(255,255,255,0.95), rgba(255,255,255,0.98)) !important;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.card-color-stripe {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  z-index: 1;
}

.primary-stripe {
  background-color: var(--v-theme-primary);
}

.info-stripe {
  background: linear-gradient(to right, var(--v-theme-info), var(--v-theme-primary));
}

.card-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.action-item {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  height: 100%;
}

.action-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

@media (max-width: 960px) {
  .main-content-row {
    height: auto;
  }
  
  .content-card {
    margin-bottom: 16px;
  }
}
</style> 