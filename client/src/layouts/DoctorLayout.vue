<script setup>
// 不需要引入DoctorSidebar，使用App.vue中的全局侧边栏
</script>

<template>
  <div class="doctor-layout">
    <!-- 移除DoctorSidebar -->
    <div class="doctor-content">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.doctor-layout {
  display: flex;
  height: 100vh;
  width: 100%;
}

.doctor-content {
  flex: 1;
  overflow: auto;
  background-color: #f5f5f5;
}
</style> 