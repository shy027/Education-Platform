package com.edu.platform.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.TaskCreateRequest;
import com.edu.platform.course.dto.request.TaskQueryRequest;
import com.edu.platform.course.dto.request.TaskUpdateRequest;
import com.edu.platform.course.dto.response.TaskResponse;
import com.edu.platform.course.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理接口
 */
@Tag(name = "任务管理")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "创建任务")
    @PostMapping
    public Result<Long> createTask(@PathVariable Long courseId,
                                    @RequestBody @Validated TaskCreateRequest request) {
        request.setCourseId(courseId);
        Long id = taskService.createTask(request);
        return Result.success("创建成功", id);
    }

    @Operation(summary = "更新任务")
    @PutMapping
    public Result<Void> updateTask(@PathVariable Long courseId,
                                    @RequestBody @Validated TaskUpdateRequest request) {
        taskService.updateTask(request);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(@PathVariable Long courseId,
                                    @PathVariable Long id) {
        taskService.deleteTask(courseId, id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public Result<TaskResponse> getTaskDetail(@PathVariable Long courseId,
                                               @PathVariable Long id) {
        return Result.success(taskService.getTaskDetail(courseId, id));
    }

    @Operation(summary = "分页查询任务列表")
    @GetMapping
    public Result<Page<TaskResponse>> pageTasks(
            @PathVariable Long courseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer taskType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        TaskQueryRequest request = new TaskQueryRequest();
        request.setCourseId(courseId);
        request.setKeyword(keyword);
        request.setTaskType(taskType);
        request.setStatus(status);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        return Result.success(taskService.pageTasks(request));
    }

    @Operation(summary = "修改任务状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long courseId,
                                      @PathVariable Long id,
                                      @RequestParam Integer status) {
        taskService.updateStatus(courseId, id, status);
        return Result.success("状态修改成功", null);
    }
}
