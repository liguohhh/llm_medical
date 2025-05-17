<template>
  <!-- No changes to template section -->
</template>

<script setup>
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import { useDoctorApi } from '../../composables/useDoctorApi';

const route = useRoute();
const doctorApi = useDoctorApi();

// 错误处理相关
const error = ref(null);
const showError = ref(false);

// 设置错误信息并显示
const setError = (message) => {
  error.value = message;
  showError.value = true;
};

// 关闭错误提示
const closeError = () => {
  showError.value = false;
};

// 加载处方详情
const loadPrescriptionDetail = async () => {
  loading.value = true;
  
  try {
    const response = await doctorApi.getPrescriptionDetail(route.params.id);
    
    if (response.code === 200 && response.data) {
      prescription.value = response.data;
    } else {
      setError(response.message || '加载处方详情失败');
    }
  } catch (err) {
    console.error('加载处方详情错误:', err);
    setError('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 提交审核
const submitReview = async () => {
  if (!reviewForm.status) {
    setError('请选择审核结果');
    return;
  }
  
  reviewLoading.value = true;
  
  try {
    const response = await doctorApi.reviewPrescription(prescription.value.id, {
      status: reviewForm.status,
      comments: reviewForm.comments
    });
    
    if (response.code === 200) {
      // 更新处方状态
      prescription.value.status = reviewForm.status;
      prescription.value.reviewComments = reviewForm.comments;
      prescription.value.reviewTime = new Date().toISOString();
      
      // 关闭对话框
      reviewDialog.value = false;
      
      // 显示成功消息
      successMessage.value = '处方审核已提交';
      showSuccess.value = true;
    } else {
      setError(response.message || '提交审核失败');
    }
  } catch (err) {
    console.error('提交审核错误:', err);
    setError('网络错误，请稍后重试');
  } finally {
    reviewLoading.value = false;
  }
};
</script>

<style>
  /* No changes to style section */
</style> 