package com.edu.platform.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.report.entity.StudentProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生思政素养画像Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface StudentProfileMapper extends BaseMapper<StudentProfile> {
    
}
