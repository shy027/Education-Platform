package com.edu.platform.community.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.Result;
import com.edu.platform.community.client.CourseServiceClient;
import com.edu.platform.community.dto.response.CourseMemberDTO;
import com.edu.platform.community.entity.CommunityGroupMember;
import com.edu.platform.community.mapper.CommunityGroupMemberMapper;
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
    
    @Autowired
    private CommunityGroupMemberMapper groupMemberMapper;
    
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
    
    /**
     * 检查用户是否为小组成员
     *
     * @param userId 用户ID
     * @param groupId 小组ID
     * @return 是否为成员
     */
    public boolean isGroupMember(Long userId, Long groupId) {
        CommunityGroupMember member = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<CommunityGroupMember>()
                .eq(CommunityGroupMember::getGroupId, groupId)
                .eq(CommunityGroupMember::getUserId, userId)
                .eq(CommunityGroupMember::getJoinStatus, 1) // 已审批通过
        );
        return member != null;
    }
    
    /**
     * 检查用户是否为小组成员(抛出异常)
     *
     * @param userId 用户ID
     * @param groupId 小组ID
     * @return 成员信息
     */
    public CommunityGroupMember checkGroupMember(Long userId, Long groupId) {
        CommunityGroupMember member = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<CommunityGroupMember>()
                .eq(CommunityGroupMember::getGroupId, groupId)
                .eq(CommunityGroupMember::getUserId, userId)
                .eq(CommunityGroupMember::getJoinStatus, 1)
        );
        
        if (member == null) {
            throw new BusinessException("您不是该小组成员");
        }
        
        return member;
    }
    
    /**
     * 检查用户是否为小组成员或教师
     *
     * @param userId 用户ID
     * @param groupId 小组ID
     * @param courseId 课程ID
     * @return 是否有权限
     */
    public boolean checkGroupMemberOrTeacher(Long userId, Long groupId, Long courseId) {
        // 先检查是否为教师
        if (isTeacher(userId, courseId)) {
            return true;
        }
        
        // 再检查是否为小组成员
        return isGroupMember(userId, groupId);
    }
    
    /**
     * 检查用户是否为教师
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 是否为教师
     */
    public boolean isTeacher(Long userId, Long courseId) {
        try {
            CourseMemberDTO memberDTO = checkCourseMember(userId, courseId);
            return memberDTO.getMemberRole() == 1 || memberDTO.getMemberRole() == 2;
        } catch (Exception e) {
            return false;
        }
    }
    
}
