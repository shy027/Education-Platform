package com.edu.platform.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.platform.course.dto.request.CourseAuditRequest;
import com.edu.platform.course.dto.request.CourseCreateRequest;
import com.edu.platform.course.dto.request.CourseQueryRequest;
import com.edu.platform.course.dto.request.CourseUpdateRequest;
import com.edu.platform.course.dto.response.CourseDetailResponse;
import com.edu.platform.course.dto.response.CourseListResponse;
import com.edu.platform.course.entity.Course;

/**
 * 课程服务接口
 */
public interface CourseService extends IService<Course> {

    /**
     * 创建课程
     *
     * @param request 创建请求
     * @return 课程ID
     */
    Long createCourse(CourseCreateRequest request);

    /**
     * 更新课程
     *
     * @param request 更新请求
     */
    void updateCourse(CourseUpdateRequest request);

    /**
     * 获取课程详情
     *
     * @param id 课程ID
     * @return 课程详情
     */
    CourseDetailResponse getCourseDetail(Long id);

    /**
     * 分页查询课程
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<CourseListResponse> pageCourses(CourseQueryRequest request);

    /**
     * 更新课程状态 (发布/下架等)
     *
     * @param id     课程ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);
    
    /**
     * 获取待审核课程列表（管理员）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<CourseListResponse> getPendingCourses(CourseQueryRequest request);
    
    /**
     * 审核课程（管理员）
     *
     * @param id      课程ID
     * @param request 审核请求
     */
    void auditCourse(Long id, CourseAuditRequest request);
}
