package com.edu.platform.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.course.entity.CoursewareProgress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习进度Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface CoursewareProgressMapper extends BaseMapper<CoursewareProgress> {
}
