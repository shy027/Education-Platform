package com.edu.platform.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.course.entity.ExamQuestionDimension;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目维度关联表 Mapper
 */
@Mapper
public interface ExamQuestionDimensionMapper extends BaseMapper<ExamQuestionDimension> {
}
