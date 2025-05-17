<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { userAdminApi } from '@/services/userAdminApi'
import UserEditDialog from '@/components/admin/UserEditDialog.vue'
import ResetPasswordDialog from '@/components/admin/ResetPasswordDialog.vue'
import DeleteConfirmDialog from '@/components/admin/DeleteConfirmDialog.vue'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'

const props = defineProps({
  userType: {
    type: Number,
    required: true
  }
})

// 状态变量
const users = ref([])
const loading = ref(false)
const error = ref('')
const search = ref('')

// 分页
const page = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)

// 对话框状态
const editDialog = ref(false)
const selectedUser = ref(null)
const resetDialog = ref(false)
const resetUserId = ref(null)
const resetUserName = ref('')
const deleteDialog = ref(false)
const deleteUserId = ref(null)
const deleteUserName = ref('')
const deleteLoading = ref(false)

// 用户类型文本
const userTypeText = computed(() => {
  const typeMap = {
    0: '病人',
    1: '医生'
  }
  return typeMap[props.userType] || '用户'
})

// 表格头部
const headers = computed(() => {
  const baseHeaders = [
    { title: 'ID', key: 'id', sortable: true },
    { title: '用户名', key: 'username', sortable: true },
    { title: '真实姓名', key: 'realName', sortable: true },
    { title: '性别', key: 'gender', sortable: true },
    { title: '年龄', key: 'age', sortable: true },
    { title: '注册时间', key: 'createTime', sortable: true }
  ]
  
  // 如果是医生类型，添加医疗方向列
  if (props.userType === 1) {
    baseHeaders.splice(5, 0, { title: '医疗方向', key: 'direction.name', sortable: true })
  }
  
  // 添加操作列
  baseHeaders.push({ title: '操作', key: 'actions', sortable: false })
  
  return baseHeaders
})

// 监听用户类型变化，重新加载数据
watch(() => props.userType, () => {
  page.value = 1 // 重置为第一页
  loadUsers()
})

// 监听分页变化，重新加载数据
watch([page, pageSize], () => {
  loadUsers()
})

// 组件挂载时加载数据
onMounted(() => {
  loadUsers()
})

// 加载用户数据
const loadUsers = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await userAdminApi.getUserList(props.userType, page.value, pageSize.value)
    if (response.code === 200) {
      users.value = response.data || []
      // 假设API返回了总条目数
      totalItems.value = response.total || users.value.length
    } else {
      error.value = response.message || '加载用户数据失败'
    }
  } catch (err) {
    console.error('加载用户数据错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 打开编辑对话框
const openEditDialog = (user = null) => {
  selectedUser.value = user
  editDialog.value = true
}

// 打开重置密码对话框
const openResetDialog = (userId, userName) => {
  resetUserId.value = userId
  resetUserName.value = userName
  resetDialog.value = true
}

// 打开删除确认对话框
const openDeleteDialog = (userId, userName) => {
  deleteUserId.value = userId
  deleteUserName.value = userName
  deleteDialog.value = true
}

// 删除用户
const deleteUser = async () => {
  deleteLoading.value = true
  
  try {
    const response = await userAdminApi.deleteUser(deleteUserId.value)
    if (response.code === 200) {
      // 重新加载数据
      loadUsers()
      // 关闭对话框
      deleteDialog.value = false
    } else {
      error.value = response.message || '删除用户失败'
    }
  } catch (err) {
    console.error('删除用户错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    deleteLoading.value = false
  }
}

// 格式化性别
const formatGender = (gender) => {
  return gender === 1 ? '男' : gender === 2 ? '女' : '未知'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19).replace(/-/g, '/')
}

// 用户保存回调
const handleUserSaved = () => {
  loadUsers()
}

// 密码重置回调
const handlePasswordReset = () => {
  // 可以显示提示消息或执行其他操作
}

// 筛选用户数据（本地搜索）
const filteredUsers = computed(() => {
  if (!search.value) return users.value
  
  const searchLower = search.value.toLowerCase()
  return users.value.filter(user => 
    user.username?.toLowerCase().includes(searchLower) ||
    user.realName?.toLowerCase().includes(searchLower) ||
    (user.direction?.name && user.direction.name.toLowerCase().includes(searchLower))
  )
})
</script>

<template>
  <div class="user-table">
    <div class="d-flex justify-space-between align-center mb-4">
      <h2 class="text-h5 font-weight-bold">{{ userTypeText }}管理</h2>
      
      <div class="d-flex">
        <v-text-field
          v-model="search"
          prepend-inner-icon="mdi-magnify"
          label="搜索用户"
          single-line
          hide-details
          density="compact"
          variant="outlined"
          class="me-4"
          style="width: 240px"
        ></v-text-field>
        
        <v-btn
          color="primary"
          prepend-icon="mdi-account-plus"
          @click="openEditDialog()"
        >
          添加{{ userTypeText }}
        </v-btn>
      </div>
    </div>
    
    <v-card class="user-table-card">
      <div class="position-relative">
        <loading-indicator :loading="loading" />
        
        <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          class="mb-4"
          density="compact"
        >
          {{ error }}
        </v-alert>
        
        <v-data-table
          :headers="headers"
          :items="filteredUsers"
          :loading="loading"
          class="user-data-table"
        >
          <!-- 性别列 -->
          <template v-slot:item.gender="{ item }">
            {{ formatGender(item.gender) }}
          </template>
          
          <!-- 医疗方向列 -->
          <template v-slot:item.direction.name="{ item }">
            {{ item.direction?.name || '-' }}
          </template>
          
          <!-- 创建时间列 -->
          <template v-slot:item.createTime="{ item }">
            {{ formatDate(item.createTime) }}
          </template>
          
          <!-- 操作列 -->
          <template v-slot:item.actions="{ item }">
            <div class="d-flex">
              <v-icon 
                icon="mdi-pencil" 
                size="small" 
                color="primary" 
                class="me-2 action-icon"
                @click="openEditDialog(item)"
                title="编辑"
              ></v-icon>
              
              <v-icon 
                icon="mdi-lock-reset" 
                size="small" 
                color="warning" 
                class="me-2 action-icon"
                @click="openResetDialog(item.id, item.realName)"
                title="重置密码"
              ></v-icon>
              
              <v-icon 
                icon="mdi-delete" 
                size="small" 
                color="error" 
                class="action-icon"
                @click="openDeleteDialog(item.id, item.realName)"
                title="删除"
              ></v-icon>
            </div>
          </template>
          
          <!-- 空状态 -->
          <template v-slot:no-data>
            <div class="text-center pa-4">
              <p class="text-medium-emphasis">暂无{{ userTypeText }}数据</p>
              <v-btn 
                color="primary" 
                variant="text" 
                @click="loadUsers" 
                class="mt-2"
              >
                刷新
              </v-btn>
            </div>
          </template>
        </v-data-table>
        
        <div class="d-flex justify-center py-4">
          <v-pagination
            :model-value="page"
            @update:model-value="page = $event"
            :total-visible="7"
            :length="Math.ceil(totalItems / pageSize)"
            rounded
          ></v-pagination>
        </div>
      </div>
    </v-card>
    
    <!-- 编辑对话框 -->
    <user-edit-dialog
      :model-value="editDialog"
      @update:model-value="editDialog = $event"
      :user="selectedUser"
      :user-type="userType"
      @saved="handleUserSaved"
    />
    
    <!-- 重置密码对话框 -->
    <reset-password-dialog
      :model-value="resetDialog"
      @update:model-value="resetDialog = $event"
      :user-id="resetUserId"
      :user-name="resetUserName"
      @completed="handlePasswordReset"
    />
    
    <!-- 删除确认对话框 -->
    <delete-confirm-dialog
      :model-value="deleteDialog"
      @update:model-value="deleteDialog = $event"
      title="确认删除用户"
      :message="`确定要删除${userTypeText}『${deleteUserName}』吗？此操作无法撤销。`"
      :loading="deleteLoading"
      @confirm="deleteUser"
    />
  </div>
</template>

<style scoped>
.user-table-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.user-data-table {
  width: 100%;
}

.position-relative {
  position: relative;
}

.action-icon {
  cursor: pointer;
  opacity: 0.7;
  transition: all 0.2s ease;
}

.action-icon:hover {
  opacity: 1;
  transform: scale(1.2);
}
</style> 