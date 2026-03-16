package com.edu.platform.course.controller.internal;

import com.edu.platform.common.result.Result;
import com.edu.platform.common.dto.CourseScoringDTO;
import com.edu.platform.course.entity.Course;
import com.edu.platform.course.service.CourseService;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程内部接口控制器
 */
@Tag(name = "课程内部接口")
@RestController
@RequestMapping("/internal/course")
@RequiredArgsConstructor
public class CourseInternalController {

    private final CourseService courseService;

    @Operation(summary = "获取课程详情(内部调用)")
    @GetMapping("/{id}")
    public Result<CourseScoringDTO> getCourseDetail(@PathVariable Long id) {
        Course course = courseService.getById(id);
        if (course == null) {
            return Result.fail("课程不存在");
        }
        CourseScoringDTO dto = new CourseScoringDTO();
        BeanUtil.copyProperties(course, dto);
        return Result.success(dto);
    }
}
