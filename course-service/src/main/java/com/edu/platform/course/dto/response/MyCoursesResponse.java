package com.edu.platform.course.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 我的课程响应（增强版）
 */
@Data
public class MyCoursesResponse {

    /**
     * 我教的课程
     */
    private List<MyCourseItem> teaching;
    
    /**
     * 我学的课程
     */
    private List<MyCourseItem> learning;
    
    /**
     * 我协助的课程（助教）
     */
    private List<MyCourseItem> assisting;
    
    /**
     * 课程项
     */
    @Data
    public static class MyCourseItem {
        private Long courseId;
        private String courseCode;
        private String courseName;
        private String courseCover;
        private String subjectArea;
        private Integer myRole;  // 我在该课程中的角色
        private Integer studentCount;
        private Integer status;
    }
}
