package com.edu.platform.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 跨模块用户统计 Mapper
 * (因为所有微服务共用 education_platform 库，故直接写SQL查询，以避免繁琐的Feign调用)
 */
@Mapper
public interface UserStatsMapper {

    // ========== 教师统计 ==========

    @Select("SELECT COUNT(*) FROM course_info WHERE teacher_id = #{userId} AND is_deleted = 0")
    int countTeacherCourses(@Param("userId") Long userId);

    @Select("SELECT COUNT(DISTINCT user_id) FROM course_member WHERE member_role = 0 AND is_deleted = 0 AND course_id IN (SELECT id FROM course_info WHERE teacher_id = #{userId} AND is_deleted = 0)")
    int countTeacherStudents(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM course_task WHERE is_deleted = 0 AND course_id IN (SELECT id FROM course_info WHERE teacher_id = #{userId} AND is_deleted = 0)")
    int countTeacherTasks(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM community_post WHERE user_id = #{userId} AND is_deleted = 0")
    int countTeacherTopics(@Param("userId") Long userId);

    // ========== 学生统计 ==========

    @Select("SELECT COUNT(*) FROM course_member WHERE user_id = #{userId} AND member_role = 0 AND is_deleted = 0")
    int countStudentJoinedCourses(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM exam_record WHERE user_id = #{userId} AND status = 1")
    int countStudentFinishedHomework(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM community_comment WHERE user_id = #{userId} AND is_deleted = 0")
    int countStudentDiscussions(@Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(progress_seconds), 0) FROM courseware_progress WHERE user_id = #{userId}")
    int getStudentStudySeconds(@Param("userId") Long userId);

    // ========== 管理员/校领导统计 ==========

    @Select("SELECT COUNT(*) FROM user_school WHERE is_deleted = 0")
    int countSchools();

    @Select("SELECT COUNT(*) FROM audit_record WHERE audit_result = 0")
    int countPendingAudits();

    @Select("SELECT COUNT(*) FROM resource WHERE is_deleted = 0")
    int countTotalResources();

    @Select("SELECT COUNT(*) FROM user_account WHERE is_deleted = 0")
    int countTotalUsers();
}
