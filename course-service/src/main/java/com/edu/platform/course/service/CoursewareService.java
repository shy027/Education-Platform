package com.edu.platform.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.course.dto.request.CoursewareQueryRequest;
import com.edu.platform.course.dto.request.CoursewareUpdateRequest;
import com.edu.platform.course.dto.request.CoursewareUploadRequest;
import com.edu.platform.course.dto.response.CoursewareDetailResponse;
import com.edu.platform.course.dto.response.CoursewareResponse;

/**
 * 课件服务
 *
 * @author Education Platform
 */
public interface CoursewareService {
    
    /**
     * 上传课件
     */
    Long uploadCourseware(Long courseId, CoursewareUploadRequest request, Long userId);
    
    /**
     * 更新课件
     */
    void updateCourseware(CoursewareUpdateRequest request, Long userId);
    
    /**
     * 删除课件
     */
    void deleteCourseware(Long wareId, Long userId);
    
    /**
     * 获取课件列表
     */
    Page<CoursewareResponse> getCoursewareList(Long courseId, CoursewareQueryRequest request);
    
    /**
     * 获取课件详情
     */
    CoursewareDetailResponse getCoursewareDetail(Long wareId, Long userId);
    
    /**
     * 审核课件
     */
    void auditCourseware(Long wareId, Integer auditStatus, Long auditorId);
}
