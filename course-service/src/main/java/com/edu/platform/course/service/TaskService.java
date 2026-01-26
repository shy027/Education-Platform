package com.edu.platform.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.platform.course.dto.request.TaskCreateRequest;
import com.edu.platform.course.dto.request.TaskQueryRequest;
import com.edu.platform.course.dto.request.TaskUpdateRequest;
import com.edu.platform.course.dto.response.TaskResponse;
import com.edu.platform.course.entity.CourseTask;

/**
 * 任务服务接口
 */
public interface TaskService extends IService<CourseTask> {

    /**
     * 创建任务
     *
     * @param request 创建请求
     * @return 任务ID
     */
    Long createTask(TaskCreateRequest request);

    /**
     * 更新任务
     *
     * @param request 更新请求
     */
    void updateTask(TaskUpdateRequest request);

    /**
     * 删除任务
     *
     * @param courseId 课程ID
     * @param id       任务ID
     */
    void deleteTask(Long courseId, Long id);

    /**
     * 获取任务详情
     *
     * @param courseId 课程ID
     * @param id       任务ID
     * @return 任务详情
     */
    TaskResponse getTaskDetail(Long courseId, Long id);

    /**
     * 分页查询任务
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<TaskResponse> pageTasks(TaskQueryRequest request);

    /**
     * 修改任务状态
     *
     * @param courseId 课程ID
     * @param id       任务ID
     * @param status   状态
     */
    void updateStatus(Long courseId, Long id, Integer status);
}
