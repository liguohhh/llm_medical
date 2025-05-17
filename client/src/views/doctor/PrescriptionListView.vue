<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { doctorApi } from '@/services/doctorApi'

const router = useRouter()
const loading = ref(true)
const error = ref(null)
const showError = ref(false)
const prescriptions = ref([])
const totalItems = ref(0)
const noDataText = computed(() => loading.value ? '加载中...' : '暂无处方数据')

// 分页和筛选参数
const pagination = reactive({
  page: 1,
  itemsPerPage: 10,
  sortBy: ['createTime'],
  sortDesc: [true]
})

// 筛选条件
const filters = reactive({
  status: null,
  keyword: '',
  dateRange: []
})

// 状态选项
const statusOptions = [
  { title: '全部状态', value: null },
  { title: '待审核', value: 0 },
  { title: '已通过', value: 1 },
  { title: '已拒绝', value: 2 }
]

// 表头定义
const headers = [
  { title: 'ID', key: 'id', sortable: true, align: 'start' },
  { title: '患者姓名', key: 'userName', sortable: true, align: 'start' },
  { title: '医疗方向', key: 'directionName', sortable: true, align: 'start' },
  { title: '智能助手', key: 'agentName', sortable: true, align: 'start' },
  { title: '生成时间', key: 'createTime', sortable: true, align: 'start' },
  { title: '审核状态', key: 'reviewStatus', sortable: true, align: 'center' },
  { title: '操作', key: 'actions', sortable: false, align: 'center' }
]

// 设置错误信息并显示
const setError = (message) => {
  error.value = message;
  showError.value = true;
};

// 关闭错误提示
const closeError = () => {
  showError.value = false;
};

// 加载处方列表
const loadPrescriptions = async () => {
  loading.value = true;
  
  try {
    const params = {
      page: pagination.page,
      size: pagination.itemsPerPage,
      sort: pagination.sortBy[0] || 'createTime',
      order: pagination.sortDesc[0] ? 'desc' : 'asc',
      status: filters.status,
      keyword: filters.keyword || undefined
    }
    
    // 添加日期范围
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }
    
    const response = await doctorApi.getPrescriptionList(params);
    
    if (response.code === 200 && response.data) {
      prescriptions.value = response.data.records || [];
      totalItems.value = response.data.total || 0;
    } else {
      setError(response.message || '加载处方列表失败');
    }
  } catch (err) {
    console.error('加载处方列表错误:', err);
    setError('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 处理分页变化
const handlePageChange = () => {
  loadPrescriptions()
}

// 处理筛选变化
const handleFilterChange = () => {
  pagination.page = 1 // 重置到第一页
  loadPrescriptions()
}

// 重置筛选条件
const resetFilters = () => {
  filters.status = null
  filters.keyword = ''
  filters.dateRange = []
  handleFilterChange()
}

// 查看处方详情
const viewPrescription = (item) => {
  router.push({
    name: 'doctorPrescriptionDetail',
    params: { prescriptionId: item.id }
  })
}

// 查看对话记录
const viewConversation = (item) => {
  if (item.conversationId) {
    router.push({
      name: 'doctorConversation',
      params: { conversationId: item.conversationId }
    })
  }
}

// 获取状态颜色
const getStatusColor = (status) => {
  switch (status) {
    case 0: return 'warning' // 未审核
    case 1: return 'success' // 已通过
    case 2: return 'error'   // 未通过
    default: return 'grey'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '未通过'
    default: return '未知'
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

// 组件挂载时加载处方列表
onMounted(() => {
  loadPrescriptions()
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
                <v-icon icon="mdi-file-document-multiple" size="large" color="white" class="me-3"></v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1 d-inline-block">处方管理</h1>
                <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
                  查看和管理患者处方
                </p>
              </div>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 筛选区域 -->
    <v-row>
      <v-col cols="12">
        <v-card class="content-card mb-4">
          <div class="card-color-stripe info-stripe"></div>
          <div class="card-inner">
            <v-card-title class="d-flex align-center pb-2 pt-4 px-4">
              <v-icon class="mr-2">mdi-filter</v-icon>
              筛选条件
            </v-card-title>
            
            <v-divider></v-divider>
            
            <v-card-text class="pa-4">
              <v-row dense>
                <v-col cols="12" sm="3">
                  <v-text-field
                    v-model="filters.keyword"
                    label="搜索处方"
                    placeholder="患者姓名/ID"
                    variant="outlined"
                    density="compact"
                    hide-details
                    clearable
                    @update:model-value="handleFilterChange"
                    prepend-inner-icon="mdi-magnify"
                  ></v-text-field>
                </v-col>
                
                <v-col cols="12" sm="3">
                  <v-select
                    v-model="filters.status"
                    :items="statusOptions"
                    label="审核状态"
                    variant="outlined"
                    density="compact"
                    hide-details
                    clearable
                    @update:model-value="handleFilterChange"
                  ></v-select>
                </v-col>
                
                <v-col cols="12" sm="4">
                  <v-date-picker-range
                    v-model="filters.dateRange"
                    label="日期范围"
                    variant="outlined"
                    density="compact"
                    hide-details
                    @update:model-value="handleFilterChange"
                  ></v-date-picker-range>
                </v-col>
                
                <v-col cols="12" sm="2" class="d-flex align-center">
                  <v-btn
                    color="primary"
                    variant="outlined"
                    class="ml-auto"
                    @click="resetFilters"
                  >
                    重置筛选
                  </v-btn>
                </v-col>
              </v-row>
            </v-card-text>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 数据表格 -->
    <v-row>
      <v-col cols="12">
        <v-card class="content-card">
          <div class="card-color-stripe primary-stripe"></div>
          <div class="card-inner">
            <v-card-title class="d-flex align-center pb-2 pt-4 px-4">
              <v-icon class="mr-2">mdi-clipboard-list</v-icon>
              处方列表
            </v-card-title>
            
            <v-divider></v-divider>
            
            <v-card-text class="pa-4">
              <v-data-table-server
                v-model:items-per-page="pagination.itemsPerPage"
                v-model:page="pagination.page"
                v-model:sort-by="pagination.sortBy"
                v-model:sort-desc="pagination.sortDesc"
                :headers="headers"
                :items="prescriptions"
                :items-length="totalItems"
                :loading="loading"
                :no-data-text="noDataText"
                class="prescription-table"
                @update:options="handlePageChange"
              >
                <!-- 自定义ID列 -->
                <template v-slot:item.id="{ item }">
                  <div class="font-weight-medium">{{ item.id }}</div>
                </template>
                
                <!-- 自定义创建时间列 -->
                <template v-slot:item.createTime="{ item }">
                  {{ formatDateTime(item.createTime) }}
                </template>
                
                <!-- 自定义状态列 -->
                <template v-slot:item.reviewStatus="{ item }">
                  <v-chip
                    :color="getStatusColor(item.reviewStatus)"
                    size="small"
                    class="font-weight-medium"
                  >
                    {{ getStatusText(item.reviewStatus) }}
                  </v-chip>
                </template>
                
                <!-- 自定义操作列 -->
                <template v-slot:item.actions="{ item }">
                  <div class="d-flex justify-center">
                    <v-tooltip text="查看处方详情" location="top">
                      <template v-slot:activator="{ props }">
                        <v-btn
                          icon="mdi-file-document"
                          size="small"
                          color="primary"
                          variant="text"
                          class="mx-1"
                          v-bind="props"
                          @click="viewPrescription(item)"
                        ></v-btn>
                      </template>
                    </v-tooltip>
                    
                    <v-tooltip text="查看对话记录" location="top">
                      <template v-slot:activator="{ props }">
                        <v-btn
                          icon="mdi-chat"
                          size="small"
                          color="secondary"
                          variant="text"
                          class="mx-1"
                          v-bind="props"
                          @click="viewConversation(item)"
                          :disabled="!item.conversationId"
                        ></v-btn>
                      </template>
                    </v-tooltip>
                  </div>
                </template>
              </v-data-table-server>
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

.prescription-table {
  border-radius: 8px;
  overflow: hidden;
}

@media (max-width: 960px) {
  .content-card {
    margin-bottom: 16px;
  }
}
</style> 