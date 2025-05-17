<script setup>
import { ref, computed, onMounted } from 'vue'
import { doctorApi } from '@/services/doctorApi'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'
import DeleteConfirmDialog from '@/components/common/DeleteConfirmDialog.vue'

// 状态变量
const namespaces = ref([])
const selectedCategory = ref(null)
const loading = ref(false)
const error = ref(null)
const search = ref('')

// 对话框状态
const deleteDialog = ref(false)
const deleteCategoryUid = ref(null)
const deleteLoading = ref(false)

// 加载大类列表
const loadCategories = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await doctorApi.getPreciseCategories()
    if (response.code === 200) {
      namespaces.value = response.data || []
      
      // 如果有选中的大类，更新它
      if (selectedCategory.value) {
        const updated = namespaces.value.find(n => n.uid === selectedCategory.value.uid)
        if (updated) {
          selectedCategory.value = updated
        } else {
          // 如果选中的大类不存在了，清除选择
          selectedCategory.value = null
        }
      }
      
      // 如果没有选中的大类但列表不为空，选择第一个
      if (!selectedCategory.value && namespaces.value.length > 0) {
        selectCategory(namespaces.value[0])
      }
    } else {
      error.value = response.message || '获取大类列表失败'
    }
  } catch (err) {
    console.error('加载大类列表出错:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 选择大类
const selectCategory = (category) => {
  selectedCategory.value = category
  emit('select-category', category)
}

// 打开删除确认对话框
const openDeleteDialog = (categoryUid) => {
  deleteCategoryUid.value = categoryUid
  deleteDialog.value = true
}

// 删除大类
const deleteCategory = async () => {
  if (!deleteCategoryUid.value) return
  
  deleteLoading.value = true
  
  try {
    const response = await doctorApi.deletePreciseCategory(deleteCategoryUid.value)
    if (response.code === 200) {
      // 如果删除的是当前选中的大类，清除选择
      if (selectedCategory.value && selectedCategory.value.uid === deleteCategoryUid.value) {
        selectedCategory.value = null
      }
      
      // 重新加载大类列表
      await loadCategories()
    } else {
      error.value = response.message || '删除大类失败'
    }
  } catch (err) {
    console.error('删除大类出错:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    deleteLoading.value = false
    deleteDialog.value = false
  }
}

// 添加大类
const handleAddCategory = () => {
  emit('add-category')
}

// 编辑大类
const handleEditCategory = (category) => {
  emit('edit-category', category)
}

// 过滤后的大类列表
const filteredCategories = computed(() => {
  if (!search.value) return namespaces.value
  
  const searchLower = search.value.toLowerCase()
  return namespaces.value.filter(category => 
    category.name.toLowerCase().includes(searchLower)
  )
})

// 定义事件
const emit = defineEmits(['select-category', 'add-category', 'edit-category'])

// 暴露方法给父组件
defineExpose({
  loadCategories
})

// 组件挂载时加载大类列表
onMounted(() => {
  loadCategories()
})
</script>

<template>
  <v-card class="mb-4">
    <v-card-title class="d-flex justify-space-between align-center">
      <span>大类列表</span>
      <div>
        <v-btn
          color="primary"
          size="small"
          prepend-icon="mdi-plus"
          @click="handleAddCategory"
          class="me-2"
        >
          添加大类
        </v-btn>
        <v-btn
          color="secondary"
          size="small"
          prepend-icon="mdi-refresh"
          @click="loadCategories"
          :loading="loading"
        >
          刷新
        </v-btn>
      </div>
    </v-card-title>
    
    <v-card-text>
      <!-- 搜索框 -->
      <v-text-field
        v-model="search"
        label="搜索大类"
        prepend-inner-icon="mdi-magnify"
        variant="outlined"
        density="compact"
        hide-details
        class="mb-4"
      ></v-text-field>
      
      <!-- 加载指示器 -->
      <loading-indicator v-if="loading" />
      
      <!-- 错误提示 -->
      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        closable
        class="mb-3"
        @click:close="error = null"
      >
        {{ error }}
      </v-alert>
      
      <!-- 大类列表 -->
      <v-list v-if="!loading && filteredCategories.length > 0" lines="two">
        <v-list-item
          v-for="category in filteredCategories"
          :key="category.uid"
          :title="category.name"
          :subtitle="`ID: ${category.uid}`"
          :active="selectedCategory && selectedCategory.uid === category.uid"
          @click="selectCategory(category)"
          class="rounded-lg mb-2 category-item"
          :class="{ 'selected-category': selectedCategory && selectedCategory.uid === category.uid }"
        >
          <template v-slot:append>
            <div class="d-flex">
              <v-btn
                icon="mdi-pencil"
                variant="text"
                size="small"
                color="primary"
                @click.stop="handleEditCategory(category)"
                class="me-1"
              ></v-btn>
              <v-btn
                icon="mdi-delete"
                variant="text"
                size="small"
                color="error"
                @click.stop="openDeleteDialog(category.uid)"
              ></v-btn>
            </div>
          </template>
        </v-list-item>
      </v-list>
      
      <!-- 空状态 -->
      <v-alert
        v-else-if="!loading && filteredCategories.length === 0"
        type="info"
        variant="tonal"
        class="mb-3"
      >
        {{ search ? '没有找到匹配的大类' : '暂无大类，请添加' }}
      </v-alert>
    </v-card-text>
    
    <!-- 删除确认对话框 -->
    <delete-confirm-dialog
      v-model="deleteDialog"
      title="删除大类"
      :content="`确定要删除此大类吗？此操作将同时删除该大类下的所有条目，且不可恢复。`"
      :loading="deleteLoading"
      @confirm="deleteCategory"
    />
  </v-card>
</template>

<style scoped>
.category-item {
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.category-item:hover {
  background-color: rgba(var(--v-theme-primary), 0.05);
}

.selected-category {
  background-color: rgba(var(--v-theme-primary), 0.1);
  border: 1px solid rgba(var(--v-theme-primary), 0.3);
}
</style> 