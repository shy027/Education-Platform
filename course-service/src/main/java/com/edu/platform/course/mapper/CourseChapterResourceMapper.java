package com.edu.platform.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.course.entity.CourseChapterResource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 章节资源关联Mapper
 */
@Mapper
public interface CourseChapterResourceMapper extends BaseMapper<CourseChapterResource> {
}
