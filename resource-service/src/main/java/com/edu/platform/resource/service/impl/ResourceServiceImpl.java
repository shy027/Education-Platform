package com.edu.platform.resource.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.PageResult;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ResourceAuditLogMapper auditLogMapper;
    
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
        resource.setCreatorId(userId);
        resource.setViewCount(0);
        resource.setDownloadCount(0);
        resource.setLikeCount(0);
        resource.setCollectCount(0);
        
        // 根据角色设置状态
        if ("ADMIN".equals(userRole)) {
            // 管理员直接发布
            resource.setCreatorType(1);
            resource.setStatus(2); // 已发布
            resource.setAuditStatus(1); // 审核通过
            resource.setPublishedTime(LocalDateTime.now());
        } else {
            // 教师保存为草稿
            resource.setCreatorType(2);
            resource.setStatus(0); // 草稿
            resource.setAuditStatus(null);
        }
        
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
        }
        
        log.info("创建资源成功: resourceId={}, userId={}, role={}", resource.getId(), userId, userRole);
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
        
        // 关键词搜索
        if (StrUtil.isNotBlank(request.getKeyword())) {
            wrapper.like(Resource::getTitle, request.getKeyword());
        }
        
        // 分类筛选
        if (request.getCategoryId() != null) {
            wrapper.eq(Resource::getCategoryId, request.getCategoryId());
        }
        
        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(Resource::getStatus, request.getStatus());
        }
        
        // 创建者筛选
        if (request.getCreatorId() != null) {
            wrapper.eq(Resource::getCreatorId, request.getCreatorId());
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Resource::getCreatedTime);
        
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
        
        // 状态检查
        if (resource.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的资源才能提交审核");
        }
        
        // 更新状态
        resource.setStatus(1); // 待审核
        resource.setAuditStatus(0); // 待审核
        resourceMapper.updateById(resource);
        
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
        
        // 记录审核日志
        ResourceAuditLog auditLog = new ResourceAuditLog();
        auditLog.setResourceId(resourceId);
        auditLog.setAuditorId(auditorId);
        auditLog.setAuditResult(request.getAuditResult());
        auditLog.setAuditRemark(request.getAuditRemark());
        auditLog.setAuditTime(LocalDateTime.now());
        auditLogMapper.insert(auditLog);
        
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
        LambdaQueryWrapper<ResourceAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceAuditLog::getResourceId, resourceId);
        wrapper.orderByDesc(ResourceAuditLog::getAuditTime);
        
        List<ResourceAuditLog> logs = auditLogMapper.selectList(wrapper);
        
        return logs.stream().map(log -> {
            AuditLogResponse response = new AuditLogResponse();
            BeanUtil.copyProperties(log, response);
            // TODO: 调用User服务获取审核人名称
            // 这里暂时设置为"管理员"
            response.setAuditorName("管理员");
            return response;
        }).collect(Collectors.toList());
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
    
}
