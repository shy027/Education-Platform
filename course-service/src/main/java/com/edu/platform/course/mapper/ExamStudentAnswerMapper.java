package com.edu.platform.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.course.entity.ExamStudentAnswer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生答题明细表 Mapper
 */
@Mapper
public interface ExamStudentAnswerMapper extends BaseMapper<ExamStudentAnswer> {
}
