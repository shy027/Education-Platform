package com.edu.platform.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.course.entity.Course;
import com.edu.platform.course.entity.CourseChapterResource;
import com.edu.platform.course.mapper.CourseChapterResourceMapper;
import com.edu.platform.course.service.ChapterResourceService;
import com.edu.platform.course.service.CourseService;
import com.edu.platform.course.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChapterResourceServiceImpl extends ServiceImpl<CourseChapterResourceMapper, CourseChapterResource> implements ChapterResourceService {

    private final CourseService courseService;

    @Override
    public void bindResource(Long courseId, Long chapterId, Long resourceId) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }

        // 权限校验
        if (!hasManagePermission(courseId, currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该课程");
        }

        // 检查是否已经绑定
        long count = this.count(new LambdaQueryWrapper<CourseChapterResource>()
                .eq(CourseChapterResource::getChapterId, chapterId)
                .eq(CourseChapterResource::getResourceId, resourceId)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "该资源已绑定到此章节");
        }

        CourseChapterResource entity = new CourseChapterResource();
        entity.setCourseId(courseId);
        entity.setChapterId(chapterId);
        entity.setResourceId(resourceId);
        entity.setCreatorId(currentUserId);
        entity.setCreatedTime(LocalDateTime.now());
        this.save(entity);
    }

    @Override
    public void unbindResource(Long courseId, Long chapterId, Long resourceId) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }

        // 权限校验
        if (!hasManagePermission(courseId, currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该课程");
        }

        this.remove(new LambdaQueryWrapper<CourseChapterResource>()
                .eq(CourseChapterResource::getChapterId, chapterId)
                .eq(CourseChapterResource::getResourceId, resourceId)
        );
    }

    private boolean hasManagePermission(Long courseId, Long userId) {
        if (PermissionUtil.isAdminOrLeader()) {
            return true;
        }
        Course course = courseService.getById(courseId);
        return course != null && course.getTeacherId().equals(userId);
    }
}
