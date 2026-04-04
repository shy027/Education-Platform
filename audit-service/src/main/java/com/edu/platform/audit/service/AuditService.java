package com.edu.platform.audit.service;

import com.edu.platform.audit.dto.request.AuditQueryRequest;
import com.edu.platform.audit.dto.request.AuditRequest;
import com.edu.platform.audit.dto.request.BatchAuditRequest;
import com.edu.platform.audit.dto.response.AuditRecordVO;
import com.edu.platform.audit.dto.response.BatchAuditResult;
import com.edu.platform.common.result.PageResult;

/**
 * 审核服务接口
 *
 * @author Education Platform
 */
public interface AuditService {
    
    /**
     * 查询待审核列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<AuditRecordVO> getPendingList(AuditQueryRequest request);
    
    /**
     * 人工审核
     *
     * @param recordId 审核记录ID
     * @param request 审核请求
     * @param auditorId 审核人ID
     */
    void manualAudit(Long recordId, AuditRequest request, Long auditorId);
    
    /**
     * 批量审核
     *
     * @param request 批量审核请求
     * @param auditorId 审核人ID
     * @return 批量审核结果
     */
    BatchAuditResult batchAudit(BatchAuditRequest request, Long auditorId);
    
    /**
     * 查询审核记录
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<AuditRecordVO> getAuditRecords(AuditQueryRequest request);

    /**
     * 获取审核统计信息
     * @return 统计集
     */
    java.util.Map<String, Object> getAuditStats();

    /**
     * 直接记录人工审核结果 (由其他业务服务上报)
     *
     * @param contentType 内容类型
     * @param contentId 内容ID
     * @param auditResult 审核结果
     * @param auditReason 审核原因
     * @param auditorId 审核人ID
     */
    void recordManualAudit(String contentType, Long contentId, Integer auditResult, String auditReason, Long auditorId);

    /**
     * 提交审核申请 (由其他业务服务在提交审核时调用)
     *
     * @param contentType 内容类型
     * @param contentId 内容ID
     */
    void submitAuditRequest(String contentType, Long contentId);
}
