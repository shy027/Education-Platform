package com.edu.platform.audit.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.audit.client.CommunityClient;
import com.edu.platform.audit.client.CourseClient;
import com.edu.platform.audit.client.ResourceClient;
import com.edu.platform.audit.dto.feign.CommentInfoDTO;
import com.edu.platform.audit.dto.feign.PostInfoDTO;
import com.edu.platform.audit.dto.feign.UpdateAuditStatusRequest;
import com.edu.platform.audit.dto.request.AuditQueryRequest;
import com.edu.platform.audit.dto.request.AuditRequest;
import com.edu.platform.audit.dto.request.BatchAuditRequest;
import com.edu.platform.audit.dto.response.AuditRecordVO;
import com.edu.platform.audit.dto.response.BatchAuditResult;
import com.edu.platform.audit.entity.AuditRecord;
import com.edu.platform.audit.enums.AuditMethod;
import com.edu.platform.audit.enums.AuditResult;
import com.edu.platform.audit.enums.ContentType;
import com.edu.platform.audit.enums.RiskLevel;
import com.edu.platform.audit.mapper.AuditRecordMapper;
import com.edu.platform.audit.service.AuditService;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审核服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {
    
    private final AuditRecordMapper auditRecordMapper;
    private final CourseClient courseClient;
    private final CommunityClient communityClient;
    private final ResourceClient resourceClient;
    
    @Override
    public PageResult<AuditRecordVO> getPendingList(AuditQueryRequest request) {
        Page<AuditRecord> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        
        // 仅查询待审核的记录
        wrapper.eq(AuditRecord::getAuditResult, AuditResult.PENDING.getCode());
        
        // 内容类型筛选
        if (StrUtil.isNotBlank(request.getContentType())) {
            wrapper.eq(AuditRecord::getContentType, request.getContentType());
        }
        
        // 风险等级筛选
        if (request.getRiskLevel() != null) {
            wrapper.eq(AuditRecord::getRiskLevel, request.getRiskLevel());
        }
        
        // 按创建时间升序 (先提交的先审核)
        wrapper.orderByAsc(AuditRecord::getCreatedTime);
        
        Page<AuditRecord> resultPage = auditRecordMapper.selectPage(page, wrapper);
        
        List<AuditRecordVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(resultPage.getTotal(), voList);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualAudit(Long recordId, AuditRequest request, Long auditorId) {
        // 查询审核记录
        AuditRecord record = auditRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("审核记录不存在");
        }
        
        // 状态检查
        if (!AuditResult.PENDING.getCode().equals(record.getAuditResult())) {
            throw new BusinessException("该记录已审核,无需重复审核");
        }
        
        // 更新审核记录
        record.setAuditMethod(AuditMethod.MANUAL.getCode());
        record.setAuditResult(request.getAuditResult());
        record.setAuditReason(request.getAuditReason());
        record.setAuditorId(auditorId);
        record.setAuditTime(LocalDateTime.now());
        
        auditRecordMapper.updateById(record);
        
        // TODO: 调用对应服务更新内容的审核状态
        updateContentAuditStatus(record.getContentType(), record.getContentId(), 
                                request.getAuditResult(), request.getAuditReason(), auditorId);
        
        log.info("审核完成: recordId={}, contentType={}, contentId={}, result={}, auditorId={}", 
                recordId, record.getContentType(), record.getContentId(), 
                request.getAuditResult(), auditorId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchAuditResult batchAudit(BatchAuditRequest request, Long auditorId) {
        BatchAuditResult result = new BatchAuditResult();
        
        for (Long recordId : request.getRecordIds()) {
            try {
                AuditRequest auditRequest = new AuditRequest();
                auditRequest.setAuditResult(request.getAuditResult());
                auditRequest.setAuditReason(request.getAuditReason());
                
                manualAudit(recordId, auditRequest, auditorId);
                result.incrementSuccess();
            } catch (Exception e) {
                log.error("批量审核失败: recordId={}, error={}", recordId, e.getMessage());
                result.incrementFail();
            }
        }
        
        log.info("批量审核完成: total={}, success={}, fail={}", 
                request.getRecordIds().size(), result.getSuccessCount(), result.getFailCount());
        
        return result;
    }
    
    @Override
    public PageResult<AuditRecordVO> getAuditRecords(AuditQueryRequest request) {
        Page<AuditRecord> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        
        // 内容类型筛选
        if (StrUtil.isNotBlank(request.getContentType())) {
            wrapper.eq(AuditRecord::getContentType, request.getContentType());
        }
        
        // 内容ID筛选
        if (request.getContentId() != null) {
            wrapper.eq(AuditRecord::getContentId, request.getContentId());
        }
        
        // 审核结果筛选
        if (request.getAuditResult() != null) {
            wrapper.eq(AuditRecord::getAuditResult, request.getAuditResult());
        }
        
        // 日期范围筛选
        if (StrUtil.isNotBlank(request.getStartDate())) {
            wrapper.ge(AuditRecord::getAuditTime, request.getStartDate());
        }
        if (StrUtil.isNotBlank(request.getEndDate())) {
            wrapper.le(AuditRecord::getAuditTime, request.getEndDate());
        }
        
        // 按审核时间倒序
        wrapper.orderByDesc(AuditRecord::getAuditTime);
        
        Page<AuditRecord> resultPage = auditRecordMapper.selectPage(page, wrapper);
        
        List<AuditRecordVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(resultPage.getTotal(), voList);
    }
    
    /**
     * 转换为VO
     */
    private AuditRecordVO convertToVO(AuditRecord record) {
        AuditRecordVO vo = new AuditRecordVO();
        vo.setRecordId(record.getId());
        vo.setContentType(record.getContentType());
        vo.setContentId(record.getContentId());
        vo.setAuditMethod(record.getAuditMethod());
        vo.setAuditResult(record.getAuditResult());
        vo.setAuditReason(record.getAuditReason());
        vo.setRiskLevel(record.getRiskLevel());
        vo.setAiConfidence(record.getAiConfidence());
        vo.setAuditorId(record.getAuditorId());
        vo.setAuditTime(record.getAuditTime());
        vo.setCreatedTime(record.getCreatedTime());
        
        // 设置枚举名称
        if (record.getContentType() != null) {
            try {
                vo.setContentTypeName(ContentType.fromCode(record.getContentType()).getName());
            } catch (Exception e) {
                vo.setContentTypeName(record.getContentType());
            }
        }
        
        if (record.getAuditMethod() != null) {
            try {
                vo.setAuditMethodName(AuditMethod.fromCode(record.getAuditMethod()).getName());
            } catch (Exception e) {
                vo.setAuditMethodName("未知");
            }
        }
        
        if (record.getAuditResult() != null) {
            try {
                vo.setAuditResultName(AuditResult.fromCode(record.getAuditResult()).getName());
            } catch (Exception e) {
                vo.setAuditResultName("未知");
            }
        }
        
        if (record.getRiskLevel() != null) {
            try {
                vo.setRiskLevelName(RiskLevel.fromCode(record.getRiskLevel()).getName());
            } catch (Exception e) {
                vo.setRiskLevelName("未知");
            }
        }
        
        // 通过Feign调用获取内容详情
        fetchContentDetails(record, vo);
        
        // TODO: 获取审核人和创建者姓名(需要user-service的Feign客户端)
        // vo.setCreatorName(...);
        // vo.setAuditorName(...);
        
        return vo;
    }
    
    /**
     * 更新内容的审核状态
     */
    private void updateContentAuditStatus(String contentType, Long contentId, 
                                         Integer auditResult, String auditReason, Long auditorId) {
        UpdateAuditStatusRequest request = new UpdateAuditStatusRequest();
        request.setAuditStatus(auditResult);
        request.setAuditorId(auditorId);
        
        try {
            switch (contentType) {
                case "COURSE":
                    java.util.Map<String, Object> courseReq = new java.util.HashMap<>();
                    courseReq.put("auditStatus", auditResult);
                    courseReq.put("auditorId", auditorId);
                    courseClient.updateAuditStatus(contentId, courseReq);
                    log.info("更新课程审核状态成功: contentId={}, auditResult={}", contentId, auditResult);
                    break;
                case "POST":
                    communityClient.updatePostAuditStatus(contentId, request);
                    log.info("更新帖子审核状态成功: contentId={}, auditResult={}", contentId, auditResult);
                    break;
                case "COMMENT":
                    communityClient.updateCommentAuditStatus(contentId, request);
                    log.info("更新评论审核状态成功: contentId={}, auditResult={}", contentId, auditResult);
                    break;
                case "RESOURCE":
                    java.util.Map<String, Object> resourceReq = new java.util.HashMap<>();
                    resourceReq.put("auditStatus", auditResult);
                    resourceReq.put("auditorId", auditorId);
                    resourceReq.put("auditRemark", auditReason);
                    resourceClient.updateAuditStatus(contentId, resourceReq);
                    log.info("更新资源审核状态成功: contentId={}, auditResult={}", contentId, auditResult);
                    break;
                default:
                    log.warn("未知的内容类型: {}", contentType);
            }
        } catch (Exception e) {
            log.error("更新内容审核状态失败: contentType={}, contentId={}, error={}", 
                contentType, contentId, e.getMessage(), e);
            throw new BusinessException("更新内容审核状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取内容详情
     */
    private void fetchContentDetails(AuditRecord record, AuditRecordVO vo) {
        try {
            switch (record.getContentType()) {
                case "COURSE":
                    // 课程暂不提供详情接口，仅设置基本标识
                    vo.setContentTitle("课程 #" + record.getContentId());
                    vo.setContentPreview("ID: " + record.getContentId());
                    break;
                case "POST":
                    Result<PostInfoDTO> postResult = 
                        communityClient.getPostInfo(record.getContentId());
                    if (postResult != null && postResult.isSuccess() 
                        && postResult.getData() != null) {
                        PostInfoDTO info = postResult.getData();
                        vo.setContentTitle(info.getTitle());
                        vo.setContentPreview(truncate(info.getContent(), 100));
                        vo.setCreatorId(info.getAuthorId());
                        vo.setCreatorName(info.getAuthorName());
                    }
                    break;
                case "COMMENT":
                    Result<CommentInfoDTO> commentResult = 
                        communityClient.getCommentInfo(record.getContentId());
                    if (commentResult != null && commentResult.isSuccess() 
                        && commentResult.getData() != null) {
                        CommentInfoDTO info = commentResult.getData();
                        vo.setContentTitle("评论");
                        vo.setContentPreview(truncate(info.getContent(), 100));
                        vo.setCreatorId(info.getAuthorId());
                        vo.setCreatorName(info.getAuthorName());
                    }
                    break;
            }
        } catch (Exception e) {
            log.warn("获取内容详情失败: contentType={}, contentId={}, error={}", 
                record.getContentType(), record.getContentId(), e.getMessage());
            // 失败不影响主流程,只是缺少详情信息
        }
    }
    
    /**
     * 截断文本
     */
    private String truncate(String text, int maxLength) {
        return text.substring(0, maxLength) + "...";
    }

    @Override
    public java.util.Map<String, Object> getAuditStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 待审核总数
        Long pendingCount = auditRecordMapper.selectCount(new LambdaQueryWrapper<AuditRecord>()
                .eq(AuditRecord::getAuditResult, AuditResult.PENDING.getCode()));
        stats.put("pendingAudits", pendingCount);
        
        // 已审核总数 (今日)
        java.time.LocalDateTime todayStart = java.time.LocalDateTime.now().with(java.time.LocalTime.MIN);
        Long processedToday = auditRecordMapper.selectCount(new LambdaQueryWrapper<AuditRecord>()
                .ne(AuditRecord::getAuditResult, AuditResult.PENDING.getCode())
                .ge(AuditRecord::getAuditTime, todayStart));
        stats.put("processedToday", processedToday);
        
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordManualAudit(String contentType, Long contentId, Integer auditResult, String auditReason, Long auditorId) {
        // 先查找是否有待审核的记录，如果有则更新，没有则新增
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditRecord::getContentType, contentType)
                .eq(AuditRecord::getContentId, contentId)
                .eq(AuditRecord::getAuditResult, AuditResult.PENDING.getCode())
                .orderByDesc(AuditRecord::getCreatedTime)
                .last("LIMIT 1");
        
        AuditRecord record = auditRecordMapper.selectOne(wrapper);
        if (record == null) {
            record = new AuditRecord();
            record.setContentType(contentType);
            record.setContentId(contentId);
            record.setCreatedTime(LocalDateTime.now());
        }
        
        record.setAuditMethod(AuditMethod.MANUAL.getCode());
        record.setAuditResult(auditResult);
        record.setAuditReason(auditReason);
        record.setAuditorId(auditorId);
        record.setAuditTime(LocalDateTime.now());
        
        if (record.getId() != null) {
            auditRecordMapper.updateById(record);
        } else {
            auditRecordMapper.insert(record);
        }
        
        log.info("内部审核记录已同步: contentType={}, contentId={}, result={}, auditorId={}", 
                contentType, contentId, auditResult, auditorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAuditRequest(String contentType, Long contentId) {
        // 逻辑调整：不再简单 count，而是尝试更新旧记录或插入新记录
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditRecord::getContentType, contentType)
                .eq(AuditRecord::getContentId, contentId)
                .orderByDesc(AuditRecord::getCreatedTime)
                .last("LIMIT 1");
        
        AuditRecord record = auditRecordMapper.selectOne(wrapper);
        
        if (record != null) {
            if (record.getAuditResult() == 0) {
                log.info("该内容已存在待审核记录，无需重复创建: contentType={}, contentId={}", contentType, contentId);
                return;
            }
            // 如果是已审核过的（通过或拒绝），现在重新提交，则将其状态重置为待审核
            record.setAuditResult(0); // 待审核
            record.setAuditMethod(null);
            record.setAuditReason(null);
            record.setAuditorId(null);
            record.setAuditTime(null);
            record.setCreatedTime(LocalDateTime.now()); // 更新提交时间
            auditRecordMapper.updateById(record);
            log.info("已将现有审核记录重置为待审核状态: contentType={}, contentId={}", contentType, contentId);
        } else {
            // 不存在记录，则插入新纪录
            record = new AuditRecord();
            record.setContentType(contentType);
            record.setContentId(contentId);
            record.setAuditResult(0); // 待审核
            record.setCreatedTime(LocalDateTime.now());
            auditRecordMapper.insert(record);
            log.info("已创建全新待审核记录: contentType={}, contentId={}", contentType, contentId);
        }
    }
}
