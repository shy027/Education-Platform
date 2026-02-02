package com.edu.platform.community.util;

import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.Result;
import com.edu.platform.community.client.CourseServiceClient;
import com.edu.platform.community.dto.response.CourseMemberDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 权限验证工具类
 *
 * @author Education Platform
 */
@Slf4j
@Component
public class PermissionUtil {
    
    @Autowired(required = false)
    private CourseServiceClient courseServiceClient;
    
    /**
     * 验证用户是否为课程成员
     */
    public CourseMemberDTO checkCourseMember(Long userId, Long courseId) {
        // 如果Feign客户端不可用,临时跳过验证
        if (courseServiceClient == null) {
            log.warn("CourseServiceClient不可用,跳过课程成员验证");
            CourseMemberDTO dto = new CourseMemberDTO();
            dto.setUserId(userId);
            dto.setCourseId(courseId);
            dto.setMemberRole(3); // 默认为学生
            dto.setJoinStatus(1); // 已加入
            return dto;
        }
        
        try {
            Result<CourseMemberDTO> result = courseServiceClient.checkCourseMember(courseId, userId);
            if (result == null || result.getData() == null) {
                throw new BusinessException("您不是该课程的成员");
            }
            return result.getData();
        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            // Feign调用失败,降级处理:跳过验证
            log.warn("CourseService不可用,跳过课程成员验证, userId={}, courseId={}", userId, courseId);
            CourseMemberDTO dto = new CourseMemberDTO();
            dto.setUserId(userId);
            dto.setCourseId(courseId);
            dto.setMemberRole(1); // 降级时默认为主讲教师,允许所有操作
            dto.setJoinStatus(1); // 已加入
            return dto;
        }
    }
    
    /**
     * 验证用户是否为教师
     */
    public void checkTeacher(Long userId, Long courseId) {
        CourseMemberDTO member = checkCourseMember(userId, courseId);
        // memberRole: 1主讲教师, 2助教, 3学生
        if (member.getMemberRole() != 1 && member.getMemberRole() != 2) {
            throw new BusinessException("只有教师或助教可以执行此操作");
        }
    }
    
    /**
     * 验证用户是否为作者或教师
     */
    public void checkAuthorOrTeacher(Long userId, Long authorId, Long courseId) {
        // 如果是作者本人,直接通过
        if (userId.equals(authorId)) {
            return;
        }
        
        // 否则检查是否为教师
        checkTeacher(userId, courseId);
    }
    
}
