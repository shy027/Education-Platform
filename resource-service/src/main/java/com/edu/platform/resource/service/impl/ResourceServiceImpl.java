package com.edu.platform.resource.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.resource.client.AuditClient;
import com.edu.platform.resource.dto.client.AuditRecordVO;
import com.edu.platform.resource.dto.client.ManualAuditRecordRequest;
import com.edu.platform.resource.dto.request.ResourceAuditRequest;
import com.edu.platform.resource.dto.request.ResourceCreateRequest;
import com.edu.platform.resource.dto.request.ResourceAttachmentRequest;
import com.edu.platform.resource.dto.request.ResourceQueryRequest;
import com.edu.platform.resource.dto.request.ResourceUpdateRequest;
import com.edu.platform.resource.dto.response.AuditLogResponse;
import com.edu.platform.resource.dto.response.ResourceDetailResponse;
import com.edu.platform.resource.dto.response.ResourceResponse;
import com.edu.platform.resource.entity.*;
import com.edu.platform.resource.mapper.*;
import com.edu.platform.resource.service.ResourceService;
import com.edu.platform.resource.mq.AuditRequestSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    
    private final ResourceMapper resourceMapper;
    private final ResourceAttachmentMapper attachmentMapper;
    private final ResourceTagRelationMapper tagRelationMapper;
    private final ResourceTagMapper tagMapper;
    private final ResourceCategoryMapper categoryMapper;
    private final AuditClient auditClient;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private AuditRequestSender auditRequestSender;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createResource(ResourceCreateRequest request, Long userId, String userRole) {
        // 创建资源实体
        Resource resource = new Resource();
        resource.setTitle(request.getTitle());
        resource.setContent(request.getContent());
        resource.setSummary(request.getSummary());
        resource.setCoverUrl(request.getCoverUrl());
        resource.setCategoryId(request.getCategoryId());
        resource.setResourceType(request.getResourceType());
        resource.setCreatorId(userId);
        resource.setViewCount(0);
        resource.setDownloadCount(0);
        resource.setLikeCount(0);
        resource.setCollectCount(0);
        
        // 根据角色设置状态
        // 初始状态统一为草稿
        if ("ADMIN".equals(userRole)) {
            resource.setCreatorType(1); // 管理员
        } else {
            resource.setCreatorType(2); // 教师
        }
        resource.setStatus(0); // 草稿
        resource.setAuditStatus(null);
        
        resourceMapper.insert(resource);
        
        // 关联标签
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                ResourceTagRelation relation = new ResourceTagRelation();
                relation.setResourceId(resource.getId());
                relation.setTagId(tagId);
                tagRelationMapper.insert(relation);
            }
        }
        
        // 关联附件
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (ResourceAttachmentRequest attachmentReq : request.getAttachments()) {
                ResourceAttachment attachment = new ResourceAttachment();
                BeanUtil.copyProperties(attachmentReq, attachment);
                attachment.setResourceId(resource.getId());
                attachmentMapper.insert(attachment);
            }
        } else if (StrUtil.isNotBlank(request.getFileUrl())) {
            // 便捷上传逻辑：如果提供了 fileUrl 但没有附件列表，自动创建一个附件
            ResourceAttachment attachment = new ResourceAttachment();
            attachment.setResourceId(resource.getId());
            attachment.setFileUrl(request.getFileUrl());
            attachment.setFileName(request.getTitle()); // 默认使用标题作为文件名
            // 根据 resourceType 设置初步的 fileType
            if (request.getResourceType() != null) {
                switch (request.getResourceType()) {
                    case 2: attachment.setFileType("video"); break;
                    case 3: attachment.setFileType("pdf"); break;
                    case 4: attachment.setFileType("audio"); break;
                    default: attachment.setFileType("other");
                }
            }
            attachmentMapper.insert(attachment);
        }
        
        log.info("创建资源成功: resourceId={}, userId={}, role={}", resource.getId(), userId, userRole);

        // 发送审核请求消息
        if (auditRequestSender != null) {
            if ("ADMIN".equals(userRole)) {
                // 管理员直接发布，默认通过
                auditRequestSender.sendAdminResourceAuditRequest(
                        resource.getId(), userId, resource.getTitle(), resource.getSummary());
            }
            // 教师创建的资源是草稿，等提交审核时再发送
        }

        return resource.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResource(Long resourceId, ResourceUpdateRequest request, Long userId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 权限检查:只能修改自己创建的资源
        if (!resource.getCreatorId().equals(userId)) {
            throw new BusinessException("无权修改此资源");
        }
        
        // 更新基本信息
        if (StrUtil.isNotBlank(request.getTitle())) {
            resource.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            resource.setContent(request.getContent());
        }
        if (request.getSummary() != null) {
            resource.setSummary(request.getSummary());
        }
        if (request.getCoverUrl() != null) {
            resource.setCoverUrl(request.getCoverUrl());
        }
        if (request.getCategoryId() != null) {
            resource.setCategoryId(request.getCategoryId());
        }
        if (request.getResourceType() != null) {
            resource.setResourceType(request.getResourceType());
        }
        
        resourceMapper.updateById(resource);
        
        // 更新标签关联
        if (request.getTagIds() != null) {
            // 删除旧关联
            LambdaQueryWrapper<ResourceTagRelation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ResourceTagRelation::getResourceId, resourceId);
            tagRelationMapper.delete(wrapper);
            
            // 添加新关联
            for (Long tagId : request.getTagIds()) {
                ResourceTagRelation relation = new ResourceTagRelation();
                relation.setResourceId(resourceId);
                relation.setTagId(tagId);
                tagRelationMapper.insert(relation);
            }
        }
        
        // 更新附件
        if (request.getAttachments() != null) {
            // 删除旧附件
            LambdaQueryWrapper<ResourceAttachment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ResourceAttachment::getResourceId, resourceId);
            attachmentMapper.delete(wrapper);
            
            // 添加新附件
            for (ResourceAttachmentRequest attachmentReq : request.getAttachments()) {
                ResourceAttachment attachment = new ResourceAttachment();
                BeanUtil.copyProperties(attachmentReq, attachment);
                attachment.setResourceId(resourceId);
                attachmentMapper.insert(attachment);
            }
        } else if (StrUtil.isNotBlank(request.getFileUrl())) {
            // 便捷上传更新逻辑
            LambdaQueryWrapper<ResourceAttachment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ResourceAttachment::getResourceId, resourceId);
            attachmentMapper.delete(wrapper);

            ResourceAttachment attachment = new ResourceAttachment();
            attachment.setResourceId(resourceId);
            attachment.setFileUrl(request.getFileUrl());
            attachment.setFileName(resource.getTitle());
            if (resource.getResourceType() != null) {
                switch (resource.getResourceType()) {
                    case 2: attachment.setFileType("video"); break;
                    case 3: attachment.setFileType("pdf"); break;
                    case 4: attachment.setFileType("audio"); break;
                    default: attachment.setFileType("other");
                }
            }
            attachmentMapper.insert(attachment);
        }
        
        log.info("更新资源成功: resourceId={}, userId={}", resourceId, userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResource(Long resourceId, Long userId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 权限检查
        if (!resource.getCreatorId().equals(userId)) {
            throw new BusinessException("无权删除此资源");
        }
        
        // 删除资源
        resourceMapper.deleteById(resourceId);
        
        // 删除标签关联
        LambdaQueryWrapper<ResourceTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTagRelation::getResourceId, resourceId);
        tagRelationMapper.delete(wrapper);
        
        log.info("删除资源成功: resourceId={}, userId={}", resourceId, userId);
    }
    
    @Override
    public ResourceDetailResponse getResourceDetail(Long resourceId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        ResourceDetailResponse response = BeanUtil.copyProperties(resource, ResourceDetailResponse.class);
        
        // 获取分类名称
        if (resource.getCategoryId() != null) {
            ResourceCategory category = categoryMapper.selectById(resource.getCategoryId());
            if (category != null) {
                response.setCategoryName(category.getCategoryName());
            }
        }
        
        // 获取标签列表
        List<ResourceResponse.TagInfo> responseTags = getResourceTags(resourceId);
        List<ResourceDetailResponse.TagInfo> detailTags = responseTags.stream()
                .map(tag -> {
                    ResourceDetailResponse.TagInfo info = new ResourceDetailResponse.TagInfo();
                    info.setId(tag.getId());
                    info.setTagName(tag.getTagName());
                    info.setTagColor(tag.getTagColor());
                    return info;
                }).collect(Collectors.toList());
        response.setTags(detailTags);
        
        // 获取附件列表
        response.setAttachments(getResourceAttachments(resourceId));
        
        return response;
    }
    
    @Override
    public PageResult<ResourceResponse> getResourceList(ResourceQueryRequest request) {
        Page<Resource> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索 (支持空格分词，匹配标题、摘要及分类名)
        if (StrUtil.isNotBlank(request.getKeyword())) {
            String[] keywords = request.getKeyword().split("\\s+");
            wrapper.and(w -> {
                for (int i = 0; i < keywords.length; i++) {
                    String kw = keywords[i];
                    if (i == 0) {
                        w.like(Resource::getTitle, kw)
                         .or().like(Resource::getSummary, kw)
                         .or().inSql(Resource::getCategoryId, "SELECT id FROM resource_category WHERE category_name LIKE '%" + kw + "%'");
                    } else {
                        w.or().like(Resource::getTitle, kw)
                         .or().like(Resource::getSummary, kw)
                         .or().inSql(Resource::getCategoryId, "SELECT id FROM resource_category WHERE category_name LIKE '%" + kw + "%'");
                    }
                }
            });
        }
        
        // 分类筛选 (支持层级穿透)
        if (request.getCategoryId() != null) {
            List<Long> categoryIds = getDescendantCategoryIds(request.getCategoryId());
            categoryIds.add(request.getCategoryId());
            wrapper.in(Resource::getCategoryId, categoryIds);
        }

        // 类型筛选
        if (request.getResourceType() != null) {
            wrapper.eq(Resource::getResourceType, request.getResourceType());
        }
        
        // 状态筛选 (优先使用 statusList)
        if (request.getStatusList() != null && !request.getStatusList().isEmpty()) {
            wrapper.in(Resource::getStatus, request.getStatusList());
        } else if (request.getStatus() != null) {
            wrapper.eq(Resource::getStatus, request.getStatus());
        }
        
        // 创建者筛选
        if (request.getCreatorId() != null) {
            wrapper.eq(Resource::getCreatorId, request.getCreatorId());
        }
        
        // 标签筛选
        if (request.getTagId() != null) {
            wrapper.inSql(Resource::getId, "SELECT resource_id FROM resource_tag_relation WHERE tag_id = " + request.getTagId());
        }
        
        // 排序方式
        if ("views".equals(request.getSortMode())) {
            wrapper.orderByDesc(Resource::getViewCount);
        } else {
            wrapper.orderByDesc(Resource::getCreatedTime);
        }
        
        Page<Resource> resultPage = resourceMapper.selectPage(page, wrapper);
        
        List<ResourceResponse> responses = resultPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new PageResult<>(resultPage.getTotal(), responses);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForAudit(Long resourceId, Long userId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 权限检查
        if (!resource.getCreatorId().equals(userId)) {
            throw new BusinessException("无权提交此资源");
        }
        
        // 状态检查：只有非待审核状态的资源才能提交审核（避免重复提交）
        if (resource.getStatus() == 1) {
            throw new BusinessException("该资源已在审核中，请勿重复提交");
        }
        
        // 判断是否为管理员：管理员提交则直接发布
        if (com.edu.platform.common.utils.UserContext.hasRole("ADMIN")) {
            resource.setStatus(2); // 已发布
            resource.setAuditStatus(1); // 审核通过
            resource.setPublishedTime(LocalDateTime.now());
            resourceMapper.updateById(resource);
            log.info("管理员直接发布资源成功: resourceId={}, userId={}", resourceId, userId);
            return;
        }

        // 普通用户流程：更新状态为待审核
        resource.setStatus(1); // 待审核
        resource.setAuditStatus(0); // 待审核
        resourceMapper.updateById(resource);
        
        // 发送审核请求消息给audit-service
        if (auditRequestSender != null) {
            auditRequestSender.sendResourceAuditRequest(
                    resourceId, userId, resource.getTitle(), resource.getSummary());
        }
        
        log.info("提交审核成功: resourceId={}, userId={}", resourceId, userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditResource(Long resourceId, ResourceAuditRequest request, Long auditorId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 状态检查
        if (resource.getStatus() != 1) {
            throw new BusinessException("只有待审核状态的资源才能审核");
        }
        
        // 更新资源状态
        if (request.getAuditResult() == 1) {
            // 审核通过
            resource.setStatus(2); // 已发布
            resource.setAuditStatus(1); // 通过
            resource.setPublishedTime(LocalDateTime.now());
        } else {
            // 审核拒绝
            resource.setStatus(3); // 已拒绝
            resource.setAuditStatus(2); // 拒绝
        }
        
        resource.setAuditTime(LocalDateTime.now());
        resource.setAuditorId(auditorId);
        resource.setAuditRemark(request.getAuditRemark());
        resourceMapper.updateById(resource);
        
        // 记录审核记录到审核中心
        try {
            ManualAuditRecordRequest auditRecordRequest = ManualAuditRecordRequest.builder()
                    .contentType("RESOURCE")
                    .contentId(resourceId)
                    .auditResult(request.getAuditResult())
                    .auditReason(request.getAuditRemark())
                    .auditorId(auditorId)
                    .build();
            auditClient.recordManualAudit(auditRecordRequest);
        } catch (Exception e) {
            log.error("上报审核记录到中心失败: resourceId={}, error={}", resourceId, e.getMessage());
        }
        
        log.info("审核资源成功: resourceId={}, result={}, auditorId={}", 
                resourceId, request.getAuditResult(), auditorId);
    }
    
    @Override
    public PageResult<ResourceResponse> getPendingList(Integer pageNum, Integer pageSize) {
        Page<Resource> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getStatus, 1); // 待审核
        wrapper.orderByAsc(Resource::getCreatedTime); // 按提交时间升序
        
        Page<Resource> resultPage = resourceMapper.selectPage(page, wrapper);
        
        List<ResourceResponse> responses = resultPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new PageResult<>(resultPage.getTotal(), responses);
    }
    
    @Override
    public List<AuditLogResponse> getAuditLogs(Long resourceId) {
        // 权限校验：非管理员只能查看自己的资源
        if (!UserContext.hasRole("ROLE_ADMIN")) {
            Resource resource = resourceMapper.selectById(resourceId);
            if (resource == null || !resource.getCreatorId().equals(UserContext.getUserId())) {
                log.warn("越权访问审核记录: userId={}, resourceId={}", UserContext.getUserId(), resourceId);
                return Collections.emptyList();
            }
        }

        try {
            Result<PageResult<AuditRecordVO>> auditRecordsResult = 
                    auditClient.getAuditRecords("RESOURCE", resourceId);
            
            if (auditRecordsResult != null && auditRecordsResult.isSuccess() 
                    && auditRecordsResult.getData() != null) {
                return auditRecordsResult.getData().getList().stream().map(log -> {
                    AuditLogResponse response = new AuditLogResponse();
                    response.setId(log.getId());
                    response.setAuditorId(log.getAuditorId());
                    response.setAuditResult(log.getAuditResult());
                    response.setAuditRemark(log.getAuditReason());
                    response.setAuditTime(log.getAuditTime());
                    // TODO: 调用User服务获取审核人名称
                    response.setAuditorName("管理员");
                    return response;
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("从审核中心获取历史记录失败: resourceId={}, error={}", resourceId, e.getMessage());
        }
        
        return Collections.emptyList();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineResource(Long resourceId, Long userId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 状态检查
        if (resource.getStatus() != 2) {
            throw new BusinessException("只有已发布的资源才能下架");
        }
        
        // 更新状态
        resource.setStatus(4); // 已下架
        resourceMapper.updateById(resource);
        
        log.info("下架资源成功: resourceId={}, userId={}", resourceId, userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long resourceId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource != null) {
            resource.setViewCount(resource.getViewCount() + 1);
            resourceMapper.updateById(resource);
        }
    }
    
    /**
     * 转换为响应DTO
     */
    private ResourceResponse convertToResponse(Resource resource) {
        ResourceResponse response = BeanUtil.copyProperties(resource, ResourceResponse.class);
        
        // 获取分类名称
        if (resource.getCategoryId() != null) {
            ResourceCategory category = categoryMapper.selectById(resource.getCategoryId());
            if (category != null) {
                response.setCategoryName(category.getCategoryName());
            }
        }
        
        // 获取标签列表
        response.setTags(getResourceTags(resource.getId()));
        
        return response;
    }
    
    /**
     * 获取资源标签列表
     */
    private List<ResourceResponse.TagInfo> getResourceTags(Long resourceId) {
        LambdaQueryWrapper<ResourceTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTagRelation::getResourceId, resourceId);
        List<ResourceTagRelation> relations = tagRelationMapper.selectList(wrapper);
        
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> tagIds = relations.stream()
                .map(ResourceTagRelation::getTagId)
                .collect(Collectors.toList());
        
        List<ResourceTag> tags = tagMapper.selectBatchIds(tagIds);
        
        return tags.stream().map(tag -> {
            ResourceResponse.TagInfo tagInfo = new ResourceResponse.TagInfo();
            tagInfo.setId(tag.getId());
            tagInfo.setTagName(tag.getTagName());
            tagInfo.setTagColor(tag.getTagColor());
            return tagInfo;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取资源附件列表
     */
    private List<ResourceDetailResponse.AttachmentInfo> getResourceAttachments(Long resourceId) {
        LambdaQueryWrapper<ResourceAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceAttachment::getResourceId, resourceId);
        wrapper.orderByAsc(ResourceAttachment::getSortOrder);
        List<ResourceAttachment> attachments = attachmentMapper.selectList(wrapper);
        
        return attachments.stream().map(attachment -> {
            ResourceDetailResponse.AttachmentInfo info = new ResourceDetailResponse.AttachmentInfo();
            info.setId(attachment.getId());
            info.setFileName(attachment.getFileName());
            info.setFileUrl(attachment.getFileUrl());
            info.setFileSize(attachment.getFileSize());
            info.setFileType(attachment.getFileType());
            info.setDuration(attachment.getDuration());
            info.setThumbnailUrl(attachment.getThumbnailUrl());
            info.setPageCount(attachment.getPageCount());
            return info;
        }).collect(Collectors.toList());
    }

    /**
     * 由audit-service回调，更新资源审核状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAuditStatus(Long resourceId, Integer auditStatus, Long auditorId, String auditRemark) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }

        Resource updateEntity = new Resource();
        updateEntity.setId(resourceId);
        updateEntity.setAuditStatus(auditStatus);
        updateEntity.setAuditorId(auditorId);
        updateEntity.setAuditRemark(auditRemark);
        updateEntity.setAuditTime(LocalDateTime.now());

        if (auditStatus == 1) {
            // 审核通过 → 已发布
            updateEntity.setStatus(2);
            updateEntity.setPublishedTime(LocalDateTime.now());
        } else if (auditStatus == 2) {
            // 审核拒绝 → 已拒绝
            updateEntity.setStatus(3);
        }

        resourceMapper.updateById(updateEntity);
        log.info("资源审核状态已更新: resourceId={}, auditStatus={}, auditorId={}", resourceId, auditStatus, auditorId);
    }

    @Override
    public List<ResourceResponse> listResponsesByIds(List<Long> resourceIds) {
        if (cn.hutool.core.collection.CollUtil.isEmpty(resourceIds)) {
            return new ArrayList<>();
        }
        List<Resource> resources = resourceMapper.selectBatchIds(resourceIds);
        return resources.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    @Override
    public java.util.Map<String, Object> getResourceStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 总资源数 (已发布)
        Long totalResources = resourceMapper.selectCount(new LambdaQueryWrapper<Resource>()
                .eq(Resource::getStatus, 2));
        stats.put("totalResources", totalResources);
        
        // 资源类型分布
        List<Resource> allResources = resourceMapper.selectList(new LambdaQueryWrapper<Resource>()
                .select(Resource::getResourceType)
                .isNotNull(Resource::getResourceType));
        
        java.util.Map<Integer, Long> distribution = allResources.stream()
                .collect(Collectors.groupingBy(Resource::getResourceType, Collectors.counting()));
        
        // 转换为更友好的名称 (这里假设 1:文章, 2:视频, 3:文档, 4:音频)
        java.util.Map<String, Long> namedDistribution = new java.util.HashMap<>();
        distribution.forEach((type, count) -> {
            String name;
            switch (type) {
                case 1: name = "文章"; break;
                case 2: name = "视频"; break;
                case 3: name = "文档"; break;
                case 4: name = "音频"; break;
                default: name = "其他";
            }
            namedDistribution.put(name, count);
        });
        
        stats.put("typeDistribution", namedDistribution);
        
        return stats;
    }

    /**
     * 递归获取所有下级分类ID
     */
    private List<Long> getDescendantCategoryIds(Long parentId) {
        List<Long> result = new ArrayList<>();
        LambdaQueryWrapper<ResourceCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceCategory::getParentId, parentId);
        List<ResourceCategory> children = categoryMapper.selectList(wrapper);
        
        if (children != null) {
            for (ResourceCategory child : children) {
                result.add(child.getId());
                result.addAll(getDescendantCategoryIds(child.getId()));
            }
        }
        return result;
    }
}
