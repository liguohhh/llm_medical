package edu.haut.baoxinyang.server.controller;

import edu.haut.baoxinyang.server.common.R;
import edu.haut.baoxinyang.server.dto.ConversationDetailDTO;
import edu.haut.baoxinyang.server.dto.PrescriptionDTO;
import edu.haut.baoxinyang.server.dto.PrescriptionReviewDTO;
import edu.haut.baoxinyang.server.dto.StatsDTO;
import edu.haut.baoxinyang.server.security.SecurityUser;
import edu.haut.baoxinyang.server.service.DoctorService;
import edu.haut.baoxinyang.server.service.LlmManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 医生控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final LlmManageService llmManageService;

    /**
     * 获取当前医生ID
     */
    private Long getCurrentDoctorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            return securityUser.getUser().getId();
        }
        return null;
    }

    /**
     * 获取处方列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param sort 排序字段
     * @param order 排序方式 asc/desc
     * @param status 审核状态 0-待审核 1-已通过 2-已拒绝
     * @param keyword 关键词搜索
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 处方列表
     */
    @GetMapping("/prescriptions")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Map<String, Object>> getPrescriptions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createTime") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Long doctorId = getCurrentDoctorId();
        if (doctorId == null) {
            return R.error("未登录");
        }
        
        Map<String, Object> result = doctorService.getPrescriptions(
                doctorId, page, size, sort, order, status, keyword, startDate, endDate);
        
        return R.ok(result);
    }

    /**
     * 获取处方详情
     * 
     * @param prescriptionId 处方ID
     * @return 处方详情
     */
    @GetMapping("/prescriptions/{prescriptionId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<PrescriptionDTO> getPrescriptionDetail(@PathVariable Long prescriptionId) {
        Long doctorId = getCurrentDoctorId();
        if (doctorId == null) {
            return R.error("未登录");
        }
        
        PrescriptionDTO prescription = doctorService.getPrescriptionDetail(prescriptionId, doctorId);
        if (prescription == null) {
            return R.error("处方不存在或无权限查看");
        }
        
        return R.ok(prescription);
    }

    /**
     * 审核处方
     * 
     * @param prescriptionId 处方ID
     * @param reviewDTO 审核信息
     * @return 审核结果
     */
    @PostMapping("/prescriptions/{prescriptionId}/review")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<PrescriptionDTO> reviewPrescription(
            @PathVariable Long prescriptionId,
            @RequestBody @Valid PrescriptionReviewDTO reviewDTO) {
        
        Long doctorId = getCurrentDoctorId();
        if (doctorId == null) {
            return R.error("未登录");
        }
        
        // 验证审核状态
        Integer status = reviewDTO.getStatus();
        if (status == null || (status != 1 && status != 2)) {
            return R.error("无效的审核状态，必须为1(通过)或2(拒绝)");
        }
        
        PrescriptionDTO prescription = doctorService.reviewPrescription(
                prescriptionId, doctorId, status, reviewDTO.getComment());
        
        if (prescription == null) {
            return R.error("处方不存在或无权限审核");
        }
        
        return R.ok(prescription);
    }

    /**
     * 获取对话详情
     * 
     * @param conversationId 对话ID
     * @return 对话详情
     */
    @GetMapping("/conversations/{conversationId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<ConversationDetailDTO> getConversationDetail(@PathVariable Long conversationId) {
        Long doctorId = getCurrentDoctorId();
        if (doctorId == null) {
            return R.error("未登录");
        }
        
        ConversationDetailDTO conversation = doctorService.getConversationDetail(conversationId, doctorId);
        if (conversation == null) {
            return R.error("对话不存在或无权限查看");
        }
        
        return R.ok(conversation);
    }

    /**
     * 获取医生首页统计数据
     * 
     * @return 统计数据
     */
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<StatsDTO> getDashboardStats() {
        Long doctorId = getCurrentDoctorId();
        if (doctorId == null) {
            return R.error("未登录");
        }
        
        StatsDTO stats = doctorService.getDashboardStats(doctorId);
        return R.ok(stats);
    }

    /**
     * 获取医生个人信息
     * 
     * @return 医生个人信息
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Map<String, Object>> getDoctorProfile() {
        Long doctorId = getCurrentDoctorId();
        if (doctorId == null) {
            return R.error("未登录");
        }
        
        Map<String, Object> profile = doctorService.getDoctorProfile(doctorId);
        if (profile == null) {
            return R.error("获取个人信息失败");
        }
        
        return R.ok(profile);
    }

    /**
     * 更新医生个人信息
     * 
     * @param profile 个人信息
     * @return 更新结果
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Map<String, Object>> updateDoctorProfile(@RequestBody Map<String, Object> profile) {
        Long doctorId = getCurrentDoctorId();
        if (doctorId == null) {
            return R.error("未登录");
        }
        
        Map<String, Object> updatedProfile = doctorService.updateDoctorProfile(doctorId, profile);
        if (updatedProfile == null) {
            return R.error("更新个人信息失败");
        }
        
        return R.ok(updatedProfile);
    }

    /**
     * 修改密码
     * 
     * @param passwordMap 包含旧密码和新密码的Map
     * @return 修改结果
     */
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<String> changePassword(@RequestBody Map<String, String> passwordMap) {
        Long doctorId = getCurrentDoctorId();
        if (doctorId == null) {
            return R.error("未登录");
        }
        
        String oldPassword = passwordMap.get("oldPassword");
        String newPassword = passwordMap.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return R.error("旧密码和新密码不能为空");
        }
        
        boolean success = doctorService.changePassword(doctorId, oldPassword, newPassword);
        if (!success) {
            return R.error("修改密码失败，请检查旧密码是否正确");
        }
        
        return R.ok("密码修改成功");
    }
    
    /**
     * 以下是向量数据库相关接口，复用LlmManageService
     */
    
    /**
     * 获取向量数据库命名空间列表
     */
    @GetMapping("/llm/vector/namespaces")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<List<String>> getNamespaces() {
        List<String> namespaces = llmManageService.getVectorNamespaces();
        return R.ok(namespaces);
    }
    
    /**
     * 创建新的命名空间
     */
    @PostMapping("/llm/vector/namespace")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Boolean> createNamespace(@RequestParam String namespace) {
        boolean success = llmManageService.createVectorNamespace(namespace);
        return success ? R.ok(true) : R.error("创建命名空间失败");
    }
    
    /**
     * 获取向量数据库文档列表
     */
    @GetMapping("/llm/vector/documents/{namespace}")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<List<Map<String, Object>>> getDocuments(@PathVariable String namespace) {
        List<Map<String, Object>> documents = llmManageService.getVectorDocuments(namespace);
        return R.ok(documents);
    }
    
    /**
     * 上传文档到向量数据库
     */
    @PostMapping("/llm/vector/upload")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("namespace") String namespace,
            @RequestParam(value = "chunkSize", required = false) Integer chunkSize,
            @RequestParam(value = "chunkOverlap", required = false) Integer chunkOverlap,
            @RequestParam(value = "metadata", required = false) String metadata) {
        
        if (file.isEmpty()) {
            return R.error("文件不能为空");
        }
        
        Map<String, Object> result = llmManageService.uploadVectorDocument(file, namespace, chunkSize, chunkOverlap);
        if (result == null) {
            return R.error("上传失败");
        }
        
        return R.ok(result);
    }
    
    /**
     * 删除向量数据库文档
     */
    @DeleteMapping("/llm/vector/document")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Boolean> deleteDocument(
            @RequestParam String namespace,
            @RequestParam String docId) {
        
        boolean success = llmManageService.deleteVectorDocument(namespace, docId);
        return success ? R.ok(true) : R.error("删除文档失败");
    }
    
    /**
     * 删除命名空间
     */
    @DeleteMapping("/llm/vector/namespace/{namespace}")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Boolean> deleteNamespace(@PathVariable String namespace) {
        boolean success = llmManageService.deleteVectorNamespace(namespace);
        return success ? R.ok(true) : R.error("删除命名空间失败");
    }
    
    /**
     * 以下是精确查找数据库相关接口，复用LlmManageService
     */
    
    /**
     * 获取精确查找数据库大类列表
     */
    @GetMapping("/llm/precise/categories")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<List<Map<String, Object>>> getPreciseCategories() {
        List<Map<String, Object>> categories = llmManageService.getPreciseCategories();
        return R.ok(categories);
    }
    
    /**
     * 创建大类
     */
    @PostMapping("/llm/precise/category")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Map<String, Object>> createPreciseCategory(@RequestBody Map<String, String> category) {
        String name = category.get("name");
        if (name == null || name.trim().isEmpty()) {
            return R.error("大类名称不能为空");
        }
        
        Map<String, Object> result = llmManageService.createPreciseCategory(name);
        return result != null ? R.ok(result) : R.error("创建大类失败");
    }
    
    /**
     * 更新大类
     */
    @PutMapping("/llm/precise/category/{categoryUid}")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Map<String, Object>> updatePreciseCategory(
            @PathVariable String categoryUid,
            @RequestBody Map<String, String> category) {
        
        String name = category.get("name");
        if (name == null || name.trim().isEmpty()) {
            return R.error("大类名称不能为空");
        }
        
        Map<String, Object> result = llmManageService.updatePreciseCategory(categoryUid, name);
        return result != null ? R.ok(result) : R.error("更新大类失败");
    }
    
    /**
     * 删除大类
     */
    @DeleteMapping("/llm/precise/category/{categoryUid}")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Boolean> deletePreciseCategory(@PathVariable String categoryUid) {
        boolean result = llmManageService.deletePreciseCategory(categoryUid);
        return result ? R.ok(true) : R.error("删除大类失败");
    }
    
    /**
     * 获取精确查找数据库条目列表
     */
    @GetMapping("/llm/precise/entries/{categoryUid}")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Map<String, Object>> getPreciseEntries(@PathVariable String categoryUid) {
        Map<String, Object> entries = llmManageService.getPreciseEntries(categoryUid);
        return R.ok(entries);
    }
    
    /**
     * 创建条目
     */
    @PostMapping("/llm/precise/entry/{categoryUid}")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Map<String, Object>> createPreciseEntry(
            @PathVariable String categoryUid,
            @RequestBody Map<String, Object> entryData) {
        
        String description = (String) entryData.get("description");
        String content = (String) entryData.get("content");
        @SuppressWarnings("unchecked")
        List<String> keywords = (List<String>) entryData.get("keywords");
        Integer weight = entryData.get("weight") != null ? 
                Integer.parseInt(entryData.get("weight").toString()) : 1;
        Boolean isEnabled = entryData.get("isEnabled") != null ? 
                Boolean.parseBoolean(entryData.get("isEnabled").toString()) : true;
        
        Map<String, Object> result = llmManageService.createPreciseEntry(
                categoryUid, description, content, keywords, weight, isEnabled);
        
        return result != null ? R.ok(result) : R.error("创建条目失败");
    }
    
    /**
     * 更新条目
     */
    @PutMapping("/llm/precise/entry/{entryUid}")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Map<String, Object>> updatePreciseEntry(
            @PathVariable String entryUid,
            @RequestBody Map<String, Object> entryData) {
        
        String description = (String) entryData.get("description");
        String content = (String) entryData.get("content");
        @SuppressWarnings("unchecked")
        List<String> keywords = (List<String>) entryData.get("keywords");
        Integer weight = entryData.get("weight") != null ? 
                Integer.parseInt(entryData.get("weight").toString()) : 1;
        Boolean isEnabled = entryData.get("isEnabled") != null ? 
                Boolean.parseBoolean(entryData.get("isEnabled").toString()) : true;
        
        Map<String, Object> result = llmManageService.updatePreciseEntry(
                entryUid, description, content, keywords, weight, isEnabled);
        
        return result != null ? R.ok(result) : R.error("更新条目失败");
    }
    
    /**
     * 删除条目
     */
    @DeleteMapping("/llm/precise/entry/{entryUid}")
    @PreAuthorize("hasRole('DOCTOR')")
    public R<Boolean> deletePreciseEntry(@PathVariable String entryUid) {
        boolean result = llmManageService.deletePreciseEntry(entryUid);
        return result ? R.ok(true) : R.error("删除条目失败");
    }
} 