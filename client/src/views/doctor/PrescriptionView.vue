<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { doctorApi } from '@/services/doctorApi'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const user = ref(userStore.user)
const prescriptionId = ref(route.params.prescriptionId)
const prescription = ref(null)
const loading = ref(true)
const error = ref(null)
const reviewComment = ref('')
const submitting = ref(false)
const reviewDialog = ref(false)
const reviewAction = ref(null) // 'approve' or 'reject'

// 加载处方详情
const loadPrescription = async () => {
  if (!prescriptionId.value) return
  
  loading.value = true
  error.value = null
  
  try {
    const response = await doctorApi.getPrescriptionDetail(prescriptionId.value)
    
    if (response.code === 200 && response.data) {
      prescription.value = response.data
    } else {
      error.value = response.message || '加载处方失败'
    }
  } catch (err) {
    console.error('加载处方错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 打开审核对话框
const openReviewDialog = (action) => {
  reviewAction.value = action
  reviewComment.value = ''
  reviewDialog.value = true
}

// 提交审核结果
const submitReview = async () => {
  if (submitting.value) return
  
  submitting.value = true
  
  try {
    const reviewStatus = reviewAction.value === 'approve' ? 1 : 2
    const response = await doctorApi.reviewPrescription(
      prescriptionId.value,
      reviewStatus,
      reviewComment.value
    )
    
    if (response.code === 200) {
      // 审核成功，关闭对话框并重新加载处方
      reviewDialog.value = false
      await loadPrescription()
      alert(reviewAction.value === 'approve' ? '处方已通过审核' : '处方已拒绝')
    } else {
      alert(response.message || '审核失败')
    }
  } catch (err) {
    console.error('提交审核错误:', err)
    alert('网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 查看对话记录
const viewConversation = () => {
  if (prescription.value && prescription.value.conversationId) {
    router.push({
      name: 'doctorConversation',
      params: { conversationId: prescription.value.conversationId }
    })
  }
}

// 返回处方列表
const goToPrescriptions = () => {
  router.push({ name: 'doctorPrescriptions' })
}

// 返回首页
const goToHome = () => {
  router.push({ name: 'doctorHome' })
}

// 获取审核状态颜色
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

// 组件挂载时加载处方
onMounted(() => {
  loadPrescription()
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
          @click="goToPrescriptions"
          color="white"
        ></v-btn>
        <v-app-bar-title>
          <span class="text-white">处方详情</span>
          <span v-if="prescription" class="text-caption ml-2 text-white">
            (ID: {{ prescription.id }})
          </span>
        </v-app-bar-title>
      </v-app-bar>
      
      <!-- 加载中 -->
      <v-card-text v-if="loading" class="text-center py-8">
        <v-progress-circular indeterminate color="primary" size="64" class="mb-4"></v-progress-circular>
        <div class="text-h6">加载处方中...</div>
      </v-card-text>
      
      <!-- 错误提示 -->
      <v-card-text v-else-if="error" class="text-center py-8">
        <v-icon size="64" color="error" class="mb-4">mdi-alert-circle</v-icon>
        <div class="text-h6 mb-2">出错了</div>
        <div class="text-body-1 text-medium-emphasis mb-4">
          {{ error }}
        </div>
        <v-btn color="primary" @click="goToHome">
          返回首页
        </v-btn>
      </v-card-text>
      
      <!-- 处方内容 -->
      <template v-else-if="prescription">
        <v-card-text class="prescription-content">
          <!-- 状态卡片 -->
          <v-card
            :color="getStatusColor(prescription.reviewStatus)"
            variant="outlined"
            class="mb-4 status-card"
          >
            <v-card-text class="text-center pa-4">
              <v-icon
                :color="getStatusColor(prescription.reviewStatus)"
                size="large"
                class="mb-2"
              >
                {{ getStatusIcon(prescription.reviewStatus) }}
              </v-icon>
              <div class="text-h6 font-weight-bold">
                {{ getStatusText(prescription.reviewStatus) }}
              </div>
              <div v-if="prescription.reviewStatus !== 0" class="text-body-2">
                审核医生: {{ prescription.reviewerName || '未知' }}
              </div>
            </v-card-text>
          </v-card>
          
          <!-- 处方信息卡片 -->
          <v-card variant="outlined" class="mb-4 info-card">
            <v-card-item>
              <v-card-title class="text-subtitle-1 font-weight-bold">
                <v-icon color="primary" class="mr-2">mdi-information</v-icon>
                处方信息
              </v-card-title>
            </v-card-item>
            
            <v-card-text class="pt-0">
              <v-list density="compact" class="bg-grey-lighten-4 rounded">
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon color="primary" class="mr-2">mdi-account</v-icon>
                  </template>
                  <v-list-item-title>病人姓名</v-list-item-title>
                  <v-list-item-subtitle>{{ prescription.userName || '未知' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon color="primary" class="mr-2">mdi-robot</v-icon>
                  </template>
                  <v-list-item-title>智能助手</v-list-item-title>
                  <v-list-item-subtitle>{{ prescription.agentName || '未知' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon color="primary" class="mr-2">mdi-medical-bag</v-icon>
                  </template>
                  <v-list-item-title>医疗方向</v-list-item-title>
                  <v-list-item-subtitle>{{ prescription.directionName || '未知' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon color="primary" class="mr-2">mdi-calendar</v-icon>
                  </template>
                  <v-list-item-title>生成时间</v-list-item-title>
                  <v-list-item-subtitle>{{ formatDateTime(prescription.createTime) }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
          
          <!-- 审核意见卡片 -->
          <v-card v-if="prescription.reviewStatus !== 0" variant="outlined" class="mb-4 review-card">
            <v-card-item>
              <v-card-title class="text-subtitle-1 font-weight-bold">
                <v-icon :color="prescription.reviewStatus === 1 ? 'success' : 'error'" class="mr-2">
                  {{ prescription.reviewStatus === 1 ? 'mdi-comment-check' : 'mdi-comment-alert' }}
                </v-icon>
                审核意见
              </v-card-title>
            </v-card-item>
            
            <v-card-text class="review-comment">
              {{ prescription.reviewComment || '无审核意见' }}
            </v-card-text>
          </v-card>
          
          <!-- 处方内容卡片 -->
          <v-card variant="outlined" class="prescription-content-card">
            <v-card-item>
              <v-card-title class="text-subtitle-1 font-weight-bold">
                <v-icon color="primary" class="mr-2">mdi-file-document</v-icon>
                处方内容
              </v-card-title>
            </v-card-item>
            
            <v-card-text>
              <div class="prescription-text">
                <div v-for="(line, index) in prescription.content.split('\n')" :key="index" class="prescription-line">
                  {{ line }}
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-card-text>
        
        <v-card-actions class="pa-4">
          <v-btn
            v-if="prescription.conversationId"
            color="secondary"
            variant="outlined"
            class="mr-2"
            @click="viewConversation"
            prepend-icon="mdi-chat"
          >
            查看对话
          </v-btn>
          
          <v-spacer></v-spacer>
          
          <template v-if="prescription.reviewStatus === 0">
            <v-btn color="error" variant="outlined" class="mr-2" @click="openReviewDialog('reject')" prepend-icon="mdi-close">
              拒绝处方
            </v-btn>
            <v-btn color="success" @click="openReviewDialog('approve')" prepend-icon="mdi-check">
              通过处方
            </v-btn>
          </template>
          <template v-else>
            <v-btn color="primary" variant="outlined" class="mr-2" @click="goToPrescriptions" prepend-icon="mdi-format-list-bulleted">
              返回列表
            </v-btn>
            <v-btn color="primary" @click="goToHome" prepend-icon="mdi-home">
              返回首页
            </v-btn>
          </template>
        </v-card-actions>
      </template>
      
      <!-- 空白状态 -->
      <v-card-text v-else class="text-center py-8">
        <v-icon size="64" color="primary" class="mb-4">mdi-file-document-outline</v-icon>
        <div class="text-h6 mb-2">未找到处方</div>
        <div class="text-body-1 text-medium-emphasis mb-4">
          该处方可能不存在或已被删除
        </div>
        <v-btn color="primary" @click="goToHome">
          返回首页
        </v-btn>
      </v-card-text>
    </v-card>
    
    <!-- 审核对话框 -->
    <v-dialog v-model="reviewDialog" max-width="500">
      <v-card>
        <v-card-title class="text-h5">
          {{ reviewAction === 'approve' ? '通过处方' : '拒绝处方' }}
        </v-card-title>
        
        <v-card-text>
          <p class="mb-4">
            {{ reviewAction === 'approve' ? '您确定要通过这个处方吗？' : '您确定要拒绝这个处方吗？' }}
          </p>
          
          <v-textarea
            v-model="reviewComment"
            label="审核意见"
            placeholder="请输入您的审核意见"
            rows="4"
            auto-grow
            variant="outlined"
            :color="reviewAction === 'approve' ? 'success' : 'error'"
          ></v-textarea>
        </v-card-text>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="reviewDialog = false">
            取消
          </v-btn>
          <v-btn
            :color="reviewAction === 'approve' ? 'success' : 'error'"
            :loading="submitting"
            @click="submitReview"
          >
            {{ reviewAction === 'approve' ? '通过' : '拒绝' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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

.prescription-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f5f5f5;
}

.status-card {
  border-width: 2px;
  border-radius: 12px;
}

.info-card, .review-card, .prescription-content-card {
  border-radius: 12px;
  background-color: white;
  transition: transform 0.2s, box-shadow 0.2s;
}

.info-card:hover, .review-card:hover, .prescription-content-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.prescription-text {
  background-color: #fafafa;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.prescription-line {
  margin-bottom: 4px;
  line-height: 1.5;
}

.prescription-line:last-child {
  margin-bottom: 0;
}

.review-comment {
  font-style: italic;
  padding: 8px 16px;
  background-color: #f5f5f5;
  border-radius: 8px;
  border-left: 4px solid;
  border-color: v-bind('prescription?.reviewStatus === 1 ? "var(--v-theme-success)" : "var(--v-theme-error)"');
}
</style> 