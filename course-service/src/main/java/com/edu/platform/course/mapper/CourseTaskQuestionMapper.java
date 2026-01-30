package com.edu.platform.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.course.entity.CourseTaskQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务题目关联表 Mapper
 */
@Mapper
public interface CourseTaskQuestionMapper extends BaseMapper<CourseTaskQuestion> {
}
