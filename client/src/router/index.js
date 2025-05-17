import { createRouter, createWebHistory } from 'vue-router'
import { authApi } from '@/services/api'
import { useUserStore } from '@/stores/user'
import UsersView from '@/views/admin/UsersView.vue'
import ProfileView from '@/views/ProfileView.vue'
import DirectionsView from '@/views/admin/DirectionsView.vue'
import AgentsView from '@/views/admin/AgentsView.vue'
import TemplatesView from '@/views/admin/TemplatesView.vue'
import AdminVectorView from '@/views/admin/VectorView.vue'
import AdminPreciseView from '@/views/admin/PreciseView.vue'
import PatientHomeView from '@/views/patient/HomeView.vue'
import PatientAgentsView from '@/views/patient/AgentsView.vue'
import PatientPrescriptionsListView from '@/views/patient/PrescriptionsListView.vue'
import DoctorLayout from '@/layouts/DoctorLayout.vue'
import DoctorVectorView from '@/views/doctor/VectorView.vue'
import DoctorPreciseView from '@/views/doctor/PreciseView.vue'

// 懒加载路由组件
const LoginView = () => import('@/views/LoginView.vue')
const RegisterView = () => import('@/views/RegisterView.vue')
const PatientChatView = () => import('@/views/patient/ChatView.vue')
const PatientPrescriptionView = () => import('@/views/patient/PrescriptionView.vue')
const DoctorHomeView = () => import('@/views/doctor/HomeView.vue')
const DoctorPrescriptionListView = () => import('@/views/doctor/PrescriptionListView.vue')
const DoctorPrescriptionView = () => import('@/views/doctor/PrescriptionView.vue')
const DoctorConversationView = () => import('@/views/doctor/ConversationView.vue')
const AdminHomeView = () => import('@/views/admin/HomeView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { requiresAuth: false }
    },
    {
      path: '/patient',
      meta: { requiresAuth: true, userType: 0 },
      children: [
        {
          path: 'home',
          name: 'patientHome',
          component: PatientHomeView,
          meta: { requiresAuth: true, roles: ['ROLE_PATIENT'] }
        },
        {
          path: 'chat',
          name: 'patientChatNew',
          component: PatientChatView
        },
        {
          path: 'chat/:conversationId',
          name: 'patientChat',
          component: PatientChatView,
          props: true
        },
        {
          path: 'agents',
          name: 'patientAgents',
          component: PatientAgentsView,
          meta: { requiresAuth: true, roles: ['ROLE_PATIENT'] }
        },
        {
          path: 'conversations',
          name: 'patientConversations',
          component: () => import('@/views/patient/ConversationsListView.vue'),
          meta: { requiresAuth: true, roles: ['ROLE_PATIENT'] }
        },
        {
          path: 'prescriptions',
          name: 'patientPrescriptions',
          component: PatientPrescriptionsListView,
          meta: { requiresAuth: true, roles: ['ROLE_PATIENT'] }
        },
        {
          path: 'prescription/:prescriptionId',
          name: 'patientPrescription',
          component: PatientPrescriptionView,
          props: true,
          meta: { requiresAuth: true, roles: ['ROLE_PATIENT'] }
        }
      ]
    },
    {
      path: '/doctor',
      component: DoctorLayout,
      meta: { requiresAuth: true, userType: 1 },
      children: [
        {
          path: 'home',
          name: 'doctorHome',
          component: DoctorHomeView,
          meta: { requiresAuth: true, roles: ['ROLE_DOCTOR'] }
        },
        {
          path: 'prescriptions',
          name: 'doctorPrescriptions',
          component: DoctorPrescriptionListView,
          meta: { requiresAuth: true, roles: ['ROLE_DOCTOR'] }
        },
        {
          path: 'prescription/:prescriptionId',
          name: 'doctorPrescriptionDetail',
          component: DoctorPrescriptionView,
          props: true,
          meta: { requiresAuth: true, roles: ['ROLE_DOCTOR'] }
        },
        {
          path: 'conversation/:conversationId',
          name: 'doctorConversation',
          component: DoctorConversationView,
          props: true,
          meta: { requiresAuth: true, roles: ['ROLE_DOCTOR'] }
        },
        {
          path: 'profile',
          name: 'doctorProfile',
          component: ProfileView,
          meta: { requiresAuth: true, roles: ['ROLE_DOCTOR'] }
        },
        {
          path: 'vector',
          name: 'doctorVector',
          component: DoctorVectorView,
          meta: { requiresAuth: true, roles: ['ROLE_DOCTOR'] }
        },
        {
          path: 'precise',
          name: 'doctorPrecise',
          component: DoctorPreciseView,
          meta: { requiresAuth: true, roles: ['ROLE_DOCTOR'] }
        }
      ]
    },
    {
      path: '/admin',
      meta: { requiresAuth: true, userType: 2 },
      children: [
        {
          path: 'home',
          name: 'adminHome',
          component: AdminHomeView
        }
      ]
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: UsersView,
      meta: { requiresAuth: true, adminOnly: true }
    },
    {
      path: '/admin/directions',
      name: 'admin-directions',
      component: DirectionsView,
      meta: { requiresAuth: true, adminOnly: true }
    },
    {
      path: '/admin/agents',
      name: 'admin-agents',
      component: AgentsView,
      meta: { requiresAuth: true, adminOnly: true }
    },
    {
      path: '/admin/templates',
      name: 'admin-templates',
      component: TemplatesView,
      meta: { requiresAuth: true, adminOnly: true }
    },
    {
      path: '/admin/vector',
      name: 'admin-vector',
      component: AdminVectorView,
      meta: { requiresAuth: true, adminOnly: true }
    },
    {
      path: '/admin/precise',
      name: 'admin-precise',
      component: AdminPreciseView,
      meta: { requiresAuth: true, adminOnly: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
      meta: { requiresAuth: true }
    }
  ]
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  // 获取用户状态
  const userStore = useUserStore()
  
  // 检查路由是否需要认证
  if (!to.meta.requiresAuth) {
    // 不需要认证的路由直接放行
    next()
    return
  }
  
  // 如果用户未登录，先尝试获取用户信息
  if (!userStore.isAuthenticated) {
    try {
      const response = await authApi.getUserInfo()
      if (response.code === 200) {
        // 获取用户信息成功，设置用户状态
        userStore.setUser(response.data)
      } else {
        // 获取用户信息失败，跳转到登录页
        next('/login')
        return
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      next('/login')
      return
    }
  }
  
  // 检查用户类型是否匹配
  if (to.meta.userType !== undefined && userStore.user.userType !== to.meta.userType) {
    // 用户类型不匹配，根据用户类型跳转到对应的首页
    const userType = userStore.user.userType
    if (userType === 0) {
      next('/patient/home')
    } else if (userType === 1) {
      next('/doctor/home')
    } else if (userType === 2) {
      next('/admin/home')
    } else {
      next('/login')
    }
    return
  }
  
  // 认证通过且用户类型匹配，放行
  next()
})

export default router
