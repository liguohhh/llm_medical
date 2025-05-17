<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/services/adminApi'
import StatCard from '@/components/admin/StatCard.vue'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'

const stats = ref({
  totalPatients: 0,
  totalDoctors: 0,
  totalAdmins: 0,
  totalDirections: 0,
  totalAgents: 0,
  totalConversations: 0,
  finishedConversations: 0,
  totalPrescriptions: 0
})

const loading = ref(true)
const error = ref('')

// 加载系统统计数据
const loadSystemStats = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await adminApi.getSystemStats()
    
    if (response.code === 200) {
      stats.value = response.data
    } else {
      error.value = response.message || '获取系统统计数据失败'
    }
  } catch (err) {
    console.error('加载系统统计数据出错:', err)
    error.value = '系统错误，请稍后再试'
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadSystemStats()
})
</script>

<template>
  <div class="position-relative">
    <v-card class="mb-4 stats-card">
      <v-card-title class="d-flex align-center py-4 px-6">
        <div class="d-flex align-center">
          <v-icon icon="mdi-chart-box" size="large" class="me-2 text-primary"></v-icon>
          <span class="text-h5">系统统计数据</span>
        </div>
        <v-spacer></v-spacer>
        <v-btn
          icon
          @click="loadSystemStats"
          :disabled="loading"
          variant="tonal"
          color="primary"
          class="refresh-btn"
          :class="{ 'rotate': loading }"
        >
          <v-icon>mdi-refresh</v-icon>
        </v-btn>
      </v-card-title>
      
      <v-card-text class="pa-6">
        <v-alert
          v-if="error"
          type="error"
          density="compact"
          class="mb-4"
        >
          {{ error }}
        </v-alert>
        
        <loading-indicator :loading="loading" overlay />
        
        <v-row>
          <v-col cols="12" sm="6" md="3">
            <stat-card
              title="病人总数"
              :value="stats.totalPatients"
              icon="mdi-account-multiple"
              color="primary"
            />
          </v-col>
          
          <v-col cols="12" sm="6" md="3">
            <stat-card
              title="医生总数"
              :value="stats.totalDoctors"
              icon="mdi-doctor"
              color="success"
            />
          </v-col>
          
          <v-col cols="12" sm="6" md="3">
            <stat-card
              title="管理员总数"
              :value="stats.totalAdmins"
              icon="mdi-shield-account"
              color="warning"
            />
          </v-col>
          
          <v-col cols="12" sm="6" md="3">
            <stat-card
              title="医疗方向总数"
              :value="stats.totalDirections"
              icon="mdi-hospital-building"
              color="info"
            />
          </v-col>
          
          <v-col cols="12" sm="6" md="3">
            <stat-card
              title="智能体总数"
              :value="stats.totalAgents"
              icon="mdi-robot"
              color="secondary"
            />
          </v-col>
          
          <v-col cols="12" sm="6" md="3">
            <stat-card
              title="对话总数"
              :value="stats.totalConversations"
              icon="mdi-message-text-outline"
              color="deep-purple-lighten-1"
            />
          </v-col>
          
          <v-col cols="12" sm="6" md="3">
            <stat-card
              title="已完成对话"
              :value="stats.finishedConversations"
              icon="mdi-message-text-check-outline"
              color="teal-lighten-1"
            />
          </v-col>
          
          <v-col cols="12" sm="6" md="3">
            <stat-card
              title="处方总数"
              :value="stats.totalPrescriptions"
              icon="mdi-file-document-outline"
              color="pink-lighten-1"
            />
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </div>
</template>

<style scoped>
.position-relative {
  position: relative;
}

.stats-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.refresh-btn {
  transition: transform 0.3s ease;
}

.refresh-btn.rotate {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style> 