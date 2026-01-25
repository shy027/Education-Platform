package com.edu.platform.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.platform.course.dto.request.ChapterCreateRequest;
import com.edu.platform.course.dto.request.ChapterUpdateRequest;
import com.edu.platform.course.dto.response.ChapterTreeResponse;
import com.edu.platform.course.entity.CourseChapter;

import java.util.List;

/**
 * 章节服务接口
 */
public interface ChapterService extends IService<CourseChapter> {

    /**
     * 获取课程章节树
     *
     * @param courseId 课程ID
     * @return 树形结构
     */
    List<ChapterTreeResponse> getChapterTree(Long courseId);

    /**
     * 创建章节
     *
     * @param request 创建请求
     * @return 章节ID
     */
    Long createChapter(ChapterCreateRequest request);

    /**
     * 更新章节
     *
     * @param request 更新请求
     */
    void updateChapter(ChapterUpdateRequest request);

    /**
     * 删除章节
     *
     * @param id 章节ID
     */
    void deleteChapter(Long id);
}
