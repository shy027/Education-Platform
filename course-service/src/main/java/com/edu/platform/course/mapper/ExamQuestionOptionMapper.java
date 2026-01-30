package com.edu.platform.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.course.entity.ExamQuestionOption;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目选项表 Mapper
 */
@Mapper
public interface ExamQuestionOptionMapper extends BaseMapper<ExamQuestionOption> {
}
