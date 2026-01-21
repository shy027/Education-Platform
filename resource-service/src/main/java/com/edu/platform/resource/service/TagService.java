package com.edu.platform.resource.service;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.resource.dto.request.TagCreateRequest;
import com.edu.platform.resource.dto.request.TagQueryRequest;
import com.edu.platform.resource.dto.request.TagUpdateRequest;
import com.edu.platform.resource.dto.response.TagResponse;

import java.util.List;

/**
 * 标签服务接口
 *
 * @author Education Platform
 */
public interface TagService {
    
    /**
     * 标签列表查询(分页)
     *
     * @param request 查询请求
     * @return 标签列表
     */
    PageResult<TagResponse> getTagList(TagQueryRequest request);
    
    /**
     * 创建标签
     *
     * @param request 创建请求
     * @return 标签ID
     */
    Long createTag(TagCreateRequest request);
    
    /**
     * 更新标签
     *
     * @param tagId 标签ID
     * @param request 更新请求
     */
    void updateTag(Long tagId, TagUpdateRequest request);
    
    /**
     * 删除标签
     *
     * @param tagId 标签ID
     */
    void deleteTag(Long tagId);
    
    /**
     * 获取所有启用的标签(不分页)
     *
     * @return 标签列表
     */
    List<TagResponse> getAllEnabledTags();
    
}
