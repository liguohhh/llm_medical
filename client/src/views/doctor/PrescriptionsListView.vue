<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { doctorApi } from '@/services/doctorApi'

const router = useRouter()
const userStore = useUserStore()
const user = ref(userStore.user)
const prescriptions = ref([])
const loading = ref(true)
const error = ref(null)
const searchQuery = ref('')
const filterStatus = ref('all')

// 加载处方列表
const loadPrescriptions = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await doctorApi.getPrescriptionList()
    if (response.code === 200 && response.data) {
      prescriptions.value = response.data
    } else {
      error.value = response.message || '加载处方列表失败'
    }
  } catch (err) {
    console.error('加载处方列表错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 查看处方详情
const viewPrescription = (prescriptionId) => {
  router.push({
    name: 'doctorPrescription',
    params: { prescriptionId }
  })
}

// 获取审核状态标签颜色
const getStatusColor = (status) => {
  switch (status) {
    case 0: return 'warning' // 未审核
    case 1: return 'success' // 已通过
    case 2: return 'error'   // 未通过
    default: return 'grey'
  }
}

// 获取审核状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '未通过'
    default: return '未知'
  }
}

// 获取审核状态图标
const getStatusIcon = (status) => {
  switch (status) {
    case 0: return 'mdi-clock-outline'
    case 1: return 'mdi-check-circle'
    case 2: return 'mdi-close-circle'
    default: return 'mdi-help-circle'
  }
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '未知时间'
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 过滤处方
const filteredPrescriptions = computed(() => {
  let filtered = prescriptions.value
  
  // 按状态过滤
  if (filterStatus.value !== 'all') {
    const statusMap = {
      'pending': 0,
      'approved': 1,
      'rejected': 2
    }
    filtered = filtered.filter(p => p.reviewStatus === statusMap[filterStatus.value])
  }
  
  // 按搜索词过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(prescription => 
      (prescription.userName && prescription.userName.toLowerCase().includes(query)) ||
      (prescription.agentName && prescription.agentName.toLowerCase().includes(query)) ||
      (prescription.directionName && prescription.directionName.toLowerCase().includes(query))
    )
  }
  
  return filtered
})

// 组件挂载时加载数据
onMounted(() => {
  loadPrescriptions()
})
</script>

<template>
  <v-container fluid class="prescription-container pa-0">
    <v-card class="prescription-card">
      <v-app-bar density="compact" color="primary" class="prescription-header">
        <v-btn
          icon="mdi-arrow-left"
          variant="text"
          class="mr-2"
          :to="{ name: 'doctorHome' }"
          color="white"
        ></v-btn>
        <v-app-bar-title>
          <span class="text-white">待审核处方</span>
          <span class="text-caption ml-2 text-white">
            ({{ user?.directionName || '未知方向' }})
          </span>
        </v-app-bar-title>
        <template v-slot:append>
          <v-btn
            icon="mdi-refresh"
            variant="text"
            @click="loadPrescriptions"
            :loading="loading"
            color="white"
          ></v-btn>
        </template>
      </v-app-bar>
      
      <v-card-text class="pa-4">
        <div class="d-flex flex-wrap align-center mb-4">
          <v-text-field
            v-model="searchQuery"
            prepend-inner-icon="mdi-magnify"
            label="搜索处方"
            density="compact"
            hide-details
            variant="outlined"
            class="flex-grow-1 mr-2"
            bg-color="white"
            clearable
          ></v-text-field>
          
          <v-btn-toggle
            v-model="filterStatus"
            color="primary"
            density="compact"
            mandatory
            class="mt-2 mt-sm-0"
          >
            <v-btn value="all">全部</v-btn>
            <v-btn value="pending">待审核</v-btn>
            <v-btn value="approved">已通过</v-btn>
            <v-btn value="rejected">未通过</v-btn>
          </v-btn-toggle>
        </div>
        
        <!-- 加载中 -->
        <div v-if="loading" class="text-center py-8">
          <v-progress-circular indeterminate color="primary" size="64" class="mb-4"></v-progress-circular>
          <div class="text-h6">加载处方列表中...</div>
        </div>
        
        <!-- 错误提示 -->
        <div v-else-if="error" class="text-center py-8">
          <v-icon size="64" color="error" class="mb-4">mdi-alert-circle</v-icon>
          <div class="text-h6 mb-2">出错了</div>
          <div class="text-body-1 text-medium-emphasis mb-4">
            {{ error }}
          </div>
          <v-btn color="primary" @click="loadPrescriptions">
            重试
          </v-btn>
        </div>
        
        <!-- 空数据提示 -->
        <div v-else-if="filteredPrescriptions.length === 0" class="text-center py-8">
          <v-icon size="64" color="grey" class="mb-4">mdi-medical-bag</v-icon>
          <div class="text-h6 mb-2">暂无处方</div>
          <div class="text-body-1 text-medium-emphasis mb-4">
            {{ filterStatus === 'all' ? '当前没有需要处理的处方' : '没有符合筛选条件的处方' }}
          </div>
          <v-btn color="primary" @click="loadPrescriptions">
            刷新列表
          </v-btn>
        </div>
        
        <!-- 处方列表 -->
        <div v-else class="prescription-list">
          <v-card
            v-for="prescription in filteredPrescriptions"
            :key="prescription.id"
            class="mb-4 prescription-item"
            @click="viewPrescription(prescription.id)"
            elevation="2"
            variant="elevated"
            :class="{'highlight-pending': prescription.reviewStatus === 0}"
          >
            <v-card-item>
              <template v-slot:prepend>
                <v-avatar :color="prescription.reviewStatus === 0 ? 'warning' : 'primary'" class="mr-3">
                  <v-icon color="white">{{ prescription.reviewStatus === 0 ? 'mdi-clock-outline' : 'mdi-medical-bag' }}</v-icon>
                </v-avatar>
              </template>
              
              <v-card-title class="pb-1">
                {{ prescription.userName || '未知病人' }}
                <v-chip
                  :color="getStatusColor(prescription.reviewStatus)"
                  size="small"
                  text-color="white"
                  class="ml-2 font-weight-medium"
                >
                  <v-icon size="small" start>{{ getStatusIcon(prescription.reviewStatus) }}</v-icon>
                  {{ getStatusText(prescription.reviewStatus) }}
                </v-chip>
              </v-card-title>
              
              <v-card-subtitle>
                {{ prescription.agentName || '未知智能助手' }} · {{ prescription.directionName || '未知医疗方向' }}
              </v-card-subtitle>
              
              <template v-slot:append>
                <v-btn
                  icon="mdi-chevron-right"
                  variant="text"
                  color="primary"
                  @click.stop="viewPrescription(prescription.id)"
                ></v-btn>
              </template>
            </v-card-item>
            
            <v-card-text class="pt-0">
              <div class="d-flex align-center text-caption text-medium-emphasis">
                <v-icon size="small" class="mr-1">mdi-calendar</v-icon>
                <span>生成时间: {{ formatDateTime(prescription.createTime) }}</span>
              </div>
              
              <div v-if="prescription.reviewStatus === 1" class="d-flex align-center text-caption text-success mt-1">
                <v-icon size="small" class="mr-1" color="success">mdi-check-circle</v-icon>
                <span>已通过审核 · {{ prescription.reviewerName || '未知医生' }}</span>
              </div>
              
              <div v-else-if="prescription.reviewStatus === 2" class="d-flex align-center text-caption text-error mt-1">
                <v-icon size="small" class="mr-1" color="error">mdi-close-circle</v-icon>
                <span>未通过审核 · {{ prescription.reviewerName || '未知医生' }}</span>
              </div>
              
              <div v-else class="d-flex align-center text-caption text-warning mt-1">
                <v-icon size="small" class="mr-1" color="warning">mdi-clock-outline</v-icon>
                <span>等待医生审核</span>
              </div>
            </v-card-text>
            
            <v-divider></v-divider>
            
            <v-card-actions>
              <v-btn
                size="small"
                color="primary"
                variant="text"
                @click.stop="viewPrescription(prescription.id)"
              >
                查看详情
              </v-btn>
              
              <v-spacer></v-spacer>
              
              <v-btn
                v-if="prescription.conversationId"
                size="small"
                color="secondary"
                variant="text"
                :to="{ name: 'doctorConversation', params: { conversationId: prescription.conversationId } }"
                @click.stop
              >
                查看对话
              </v-btn>
              
              <v-btn
                v-if="prescription.reviewStatus === 0"
                size="small"
                color="success"
                variant="text"
                @click.stop="viewPrescription(prescription.id)"
              >
                审核处方
              </v-btn>
            </v-card-actions>
          </v-card>
        </div>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<style scoped>
.prescription-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.prescription-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 0;
}

.prescription-header {
  z-index: 10;
}

.prescription-list {
  padding: 4px 0;
}

.prescription-item {
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
  border-radius: 12px;
}

.prescription-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1) !important;
}

.highlight-pending {
  border-left: 4px solid var(--v-theme-warning);
}

.v-card-text {
  background-color: #f5f5f5;
  flex: 1;
  overflow-y: auto;
}
</style> 