<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/services/api'
import { validationRules } from '@/components/FormValidation'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)

// 登录表单数据
const loginForm = ref({
  username: '',
  password: ''
})

// 错误信息
const errorMessage = ref('')
const loading = ref(false)

// 处理登录请求
const handleLogin = async () => {
  // 验证表单
  const { valid } = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  errorMessage.value = ''

  try {
    const response = await authApi.login(loginForm.value)
    
    if (response.code === 200) {
      // 登录成功
      userStore.setUser(response.data)
      
      // 根据用户类型跳转到不同页面
      const userType = response.data.userType
      if (userType === 0) {
        // 病人
        router.push('/patient/home')
      } else if (userType === 1) {
        // 医生
        router.push('/doctor/home')
      } else if (userType === 2) {
        // 管理员
        router.push('/admin/home')
      } else {
        router.push('/')
      }
    } else {
      // 登录失败
      errorMessage.value = response.message || '登录失败'
    }
  } catch (error) {
    console.error('登录出错:', error)
    errorMessage.value = '登录过程中发生错误'
  } finally {
    loading.value = false
  }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}
</script>

<template>
  <v-container fluid fill-height class="login-container">
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card class="login-card">
          <div 
            class="pa-5 text-center rounded-t-lg" 
            style="background: linear-gradient(to right, #1976D2, #64B5F6);"
          >
            <h1 class="text-h4 font-weight-bold text-white mb-1">医疗辅助问答系统</h1>
            <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
              欢迎您登录系统
            </p>
          </div>
          
          <v-card-text class="pa-6 pt-8">
            <v-form @submit.prevent="handleLogin" ref="formRef">
              <v-text-field
                v-model="loginForm.username"
                label="用户名"
                prepend-icon="mdi-account"
                :rules="validationRules.username"
                variant="outlined"
                density="comfortable"
                bg-color="grey-lighten-5"
                class="mb-4"
                required
              ></v-text-field>
              
              <v-text-field
                v-model="loginForm.password"
                label="密码"
                prepend-icon="mdi-lock"
                :rules="validationRules.password"
                type="password"
                variant="outlined"
                density="comfortable"
                bg-color="grey-lighten-5"
                required
              ></v-text-field>
              
              <v-alert
                v-if="errorMessage"
                type="error"
                class="mt-4"
                density="compact"
                variant="tonal"
                border="start"
              >
                {{ errorMessage }}
              </v-alert>
              
              <div class="d-flex flex-column gap-4 mt-6">
                <v-btn
                  color="primary"
                  size="large"
                  block
                  type="submit"
                  :loading="loading"
                  elevation="2"
                >
                  登录
                </v-btn>
                
                <v-btn
                  variant="text"
                  @click="goToRegister"
                  class="mt-2"
                >
                  没有账号？点击注册
                </v-btn>
              </div>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.login-container {
  background-color: #f5f7fa;
  min-height: 100vh;
  background-image: linear-gradient(135deg, rgba(25, 118, 210, 0.05) 0%, rgba(100, 181, 246, 0.1) 100%);
}

.login-card {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}
</style> 