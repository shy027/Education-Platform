package com.edu.platform.resource.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.constant.Constants;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.resource.dto.request.TagCreateRequest;
import com.edu.platform.resource.dto.request.TagQueryRequest;
import com.edu.platform.resource.dto.request.TagUpdateRequest;
import com.edu.platform.resource.dto.response.TagResponse;
import com.edu.platform.resource.entity.ResourceTag;
import com.edu.platform.resource.entity.ResourceTagRelation;
import com.edu.platform.resource.mapper.ResourceTagMapper;
import com.edu.platform.resource.mapper.ResourceTagRelationMapper;
import com.edu.platform.resource.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    
    private final ResourceTagMapper resourceTagMapper;
    private final ResourceTagRelationMapper resourceTagRelationMapper;
    
    @Override
    public PageResult<TagResponse> getTagList(TagQueryRequest request) {
        // 参数校验
        if (request.getPageNum() == null || request.getPageNum() < 1) {
            request.setPageNum(Constants.DEFAULT_PAGE_NUM);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(Constants.DEFAULT_PAGE_SIZE);
        }
        if (request.getPageSize() > Constants.MAX_PAGE_SIZE) {
            request.setPageSize(Constants.MAX_PAGE_SIZE);
        }
        
        // 构建查询条件
        LambdaQueryWrapper<ResourceTag> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(request.getTagName())) {
            wrapper.like(ResourceTag::getTagName, request.getTagName());
        }
        
        if (request.getCategoryId() != null) {
            wrapper.eq(ResourceTag::getCategoryId, request.getCategoryId());
        }
        
        if (request.getStatus() != null) {
            wrapper.eq(ResourceTag::getStatus, request.getStatus());
        }
        
        wrapper.orderByAsc(ResourceTag::getSortOrder)
               .orderByDesc(ResourceTag::getCreatedTime);
        
        // 分页查询
        Page<ResourceTag> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<ResourceTag> result = resourceTagMapper.selectPage(page, wrapper);
        
        // 转换为响应对象
        List<TagResponse> list = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(result.getTotal(), list);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTag(TagCreateRequest request) {
        // 检查标签名称是否已存在
        LambdaQueryWrapper<ResourceTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTag::getTagName, request.getTagName());
        Long count = resourceTagMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "标签名称已存在");
        }
        
        // 创建标签
        ResourceTag tag = new ResourceTag();
        BeanUtil.copyProperties(request, tag);
        tag.setUseCount(0);
        tag.setStatus(1); // 默认启用
        if (tag.getSortOrder() == null) {
            tag.setSortOrder(0);
        }
        
        resourceTagMapper.insert(tag);
        log.info("创建标签成功, tagId={}, tagName={}", tag.getId(), tag.getTagName());
        
        return tag.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(Long tagId, TagUpdateRequest request) {
        // 检查标签是否存在
        ResourceTag tag = resourceTagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "标签不存在");
        }
        
        // 检查标签名称是否重复(排除自己)
        if (StrUtil.isNotBlank(request.getTagName()) && !request.getTagName().equals(tag.getTagName())) {
            LambdaQueryWrapper<ResourceTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ResourceTag::getTagName, request.getTagName())
                   .ne(ResourceTag::getId, tagId);
            Long count = resourceTagMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "标签名称已存在");
            }
        }
        
        // 更新标签信息
        BeanUtil.copyProperties(request, tag);
        resourceTagMapper.updateById(tag);
        
        log.info("更新标签成功, tagId={}, tagName={}", tagId, tag.getTagName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long tagId) {
        // 检查标签是否存在
        ResourceTag tag = resourceTagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "标签不存在");
        }
        
        // 检查是否有资源关联该标签
        LambdaQueryWrapper<ResourceTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTagRelation::getTagId, tagId);
        Long count = resourceTagRelationMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), 
                    "该标签下还有 " + count + " 个资源,无法删除");
        }
        
        // 逻辑删除标签
        resourceTagMapper.deleteById(tagId);
        
        log.info("删除标签成功, tagId={}, tagName={}", tagId, tag.getTagName());
    }
    
    @Override
    public List<TagResponse> getAllEnabledTags() {
        LambdaQueryWrapper<ResourceTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTag::getStatus, 1)
               .orderByAsc(ResourceTag::getSortOrder)
               .orderByAsc(ResourceTag::getTagName);
        
        List<ResourceTag> tags = resourceTagMapper.selectList(wrapper);
        return tags.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为响应对象
     */
    private TagResponse convertToResponse(ResourceTag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setTagName(tag.getTagName());
        response.setTagColor(tag.getTagColor());
        response.setCategoryId(tag.getCategoryId());
        response.setDescription(tag.getDescription());
        response.setUseCount(tag.getUseCount());
        response.setSortOrder(tag.getSortOrder());
        response.setStatus(tag.getStatus());
        response.setCreatedTime(tag.getCreatedTime());
        return response;
    }
    
}
