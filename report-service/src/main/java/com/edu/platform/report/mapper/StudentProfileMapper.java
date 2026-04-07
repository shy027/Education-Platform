package com.edu.platform.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.report.entity.StudentProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 学生思政素养画像Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface StudentProfileMapper extends BaseMapper<StudentProfile> {
    
    @Select("SELECT member_type FROM user_school_member WHERE user_id = #{userId} LIMIT 1")
    Integer getMemberType(Long userId);

    @Select("SELECT school_id FROM user_school_member WHERE user_id = #{teacherId} AND member_type = 1 LIMIT 1")
    Integer getSchoolIdByTeacher(Long teacherId);

    @Select("SELECT AVG(total_score) FROM report_student_profile WHERE user_id IN (SELECT user_id FROM user_school_member WHERE school_id = #{schoolId})")
    BigDecimal getSchoolAvgScore(Long schoolId);
}
