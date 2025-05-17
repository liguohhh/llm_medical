<script setup>
import { ref } from 'vue'
import CategoryList from '@/components/admin/CategoryList.vue'
import EntryList from '@/components/admin/EntryList.vue'
import CategoryEditDialog from '@/components/admin/CategoryEditDialog.vue'
import EntryEditDialog from '@/components/admin/EntryEditDialog.vue'

// 状态变量
const selectedCategory = ref(null)
const categoryListRef = ref(null)
const entryListRef = ref(null)

// 对话框状态
const categoryDialog = ref(false)
const entryDialog = ref(false)

// 编辑对象
const editCategory = ref(null)
const editEntry = ref(null)

// 处理大类选择
const handleSelectCategory = (category) => {
  selectedCategory.value = category
}

// 处理添加大类
const handleAddCategory = () => {
  editCategory.value = null
  categoryDialog.value = true
}

// 处理编辑大类
const handleEditCategory = (category) => {
  editCategory.value = category
  categoryDialog.value = true
}

// 处理大类保存成功
const handleCategorySaved = () => {
  if (categoryListRef.value) {
    categoryListRef.value.loadCategories()
  }
}

// 处理添加条目
const handleAddEntry = () => {
  editEntry.value = null
  entryDialog.value = true
}

// 处理编辑条目
const handleEditEntry = (entry) => {
  editEntry.value = entry
  entryDialog.value = true
}

// 处理条目保存成功
const handleEntrySaved = () => {
  if (entryListRef.value) {
    entryListRef.value.loadEntries()
  }
}
</script>

<template>
  <v-container>
    <v-card class="mb-6" flat>
      <div 
        class="pa-5 rounded-t-lg" 
        style="background: linear-gradient(to right, #1976D2, #64B5F6);"
      >
        <div class="d-flex align-center">
          <v-icon icon="mdi-database-search" size="large" color="white" class="me-3"></v-icon>
          <div>
            <h1 class="text-h4 font-weight-bold text-white mb-1">精确查找数据库管理</h1>
            <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
              管理精确查找数据库中的大类和条目
            </p>
          </div>
        </div>
      </div>
    </v-card>
    
    <v-row>
      <!-- 大类列表 -->
      <v-col cols="12" md="4">
        <category-list
          ref="categoryListRef"
          @select-category="handleSelectCategory"
          @add-category="handleAddCategory"
          @edit-category="handleEditCategory"
        />
      </v-col>
      
      <!-- 条目列表 -->
      <v-col cols="12" md="8">
        <entry-list
          ref="entryListRef"
          :category="selectedCategory"
          @add-entry="handleAddEntry"
          @edit-entry="handleEditEntry"
        />
      </v-col>
    </v-row>
    
    <!-- 大类编辑对话框 -->
    <category-edit-dialog
      v-model="categoryDialog"
      :category="editCategory"
      @saved="handleCategorySaved"
    />
    
    <!-- 条目编辑对话框 -->
    <entry-edit-dialog
      v-model="entryDialog"
      :entry="editEntry"
      :category="selectedCategory"
      @saved="handleEntrySaved"
    />
  </v-container>
</template>

<style scoped>
.v-container {
  max-width: 1400px;
}

.v-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
</style> 