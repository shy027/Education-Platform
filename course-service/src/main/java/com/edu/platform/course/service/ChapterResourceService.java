package com.edu.platform.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.platform.course.entity.CourseChapterResource;

public interface ChapterResourceService extends IService<CourseChapterResource> {

    /**
     * 绑定资源
     */
    void bindResource(Long courseId, Long chapterId, Long resourceId);

    /**
     * 解绑资源
     */
    void unbindResource(Long courseId, Long chapterId, Long resourceId);
}
