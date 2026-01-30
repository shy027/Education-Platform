package com.edu.platform.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.course.entity.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目表 Mapper
 */
@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {
}
