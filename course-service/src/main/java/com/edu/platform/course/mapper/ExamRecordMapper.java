package com.edu.platform.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.course.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 做题记录表 Mapper
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {
}
