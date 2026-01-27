package com.edu.platform.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.course.dto.request.ProgressRecordRequest;
import com.edu.platform.course.dto.response.StudentProgressResponse;
import com.edu.platform.course.entity.CoursewareProgress;

/**
 * 学习进度服务
 *
 * @author Education Platform
 */
public interface CoursewareProgressService {
    
    /**
     * 记录学习进度
     */
    void recordProgress(Long wareId, ProgressRecordRequest request, Long userId);
    
    /**
     * 获取学习进度
     */
    CoursewareProgress getProgress(Long wareId, Long userId);
    
    /**
     * 获取课件的所有学生学习进度（教师用）
     */
    Page<StudentProgressResponse> getCoursewareProgress(Long wareId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取课程下所有课件的学生学习进度统计（教师用）
     */
    Page<StudentProgressResponse> getCourseProgress(Long courseId, Long userId, Integer pageNum, Integer pageSize);
}
