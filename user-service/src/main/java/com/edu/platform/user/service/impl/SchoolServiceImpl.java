package com.edu.platform.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.constant.Constants;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.user.dto.request.JoinSchoolRequest;
import com.edu.platform.user.dto.response.SchoolResponse;
import com.edu.platform.user.entity.UserSchool;
import com.edu.platform.user.entity.UserSchoolMember;
import com.edu.platform.user.mapper.UserSchoolMapper;
import com.edu.platform.user.mapper.UserSchoolMemberMapper;
import com.edu.platform.user.service.SchoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学校服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {
    
    private final UserSchoolMapper userSchoolMapper;
    private final UserSchoolMemberMapper userSchoolMemberMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinSchool(Long userId, Long schoolId, JoinSchoolRequest request) {
        // 检查学校是否存在
        UserSchool school = userSchoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "学校不存在");
        }
        
        // 检查是否已加入
        LambdaQueryWrapper<UserSchoolMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSchoolMember::getSchoolId, schoolId);
        wrapper.eq(UserSchoolMember::getUserId, userId);
        if (userSchoolMemberMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("已加入该学校");
        }
        
        // 创建成员记录
        UserSchoolMember member = new UserSchoolMember();
        member.setSchoolId(schoolId);
        member.setUserId(userId);
        member.setMemberType(request.getMemberType());
        member.setDepartment(request.getDepartment());
        member.setJobNumber(request.getJobNumber());
        member.setJoinTime(LocalDateTime.now());
        member.setStatus(1); // 在职/在读
        
        userSchoolMemberMapper.insert(member);
        log.info("用户加入学校成功: userId={}, schoolId={}", userId, schoolId);
    }
    
    @Override
    public PageResult<SchoolResponse> getSchoolList(String keyword, String province, Integer pageNum, Integer pageSize) {
        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = Constants.DEFAULT_PAGE_NUM;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = Constants.DEFAULT_PAGE_SIZE;
        }
        if (pageSize > Constants.MAX_PAGE_SIZE) {
            pageSize = Constants.MAX_PAGE_SIZE;
        }
        
        // 构建查询条件
        LambdaQueryWrapper<UserSchool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSchool::getStatus, 1); // 正常状态
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(UserSchool::getSchoolName, keyword)
                    .or().like(UserSchool::getSchoolCode, keyword));
        }
        
        if (StrUtil.isNotBlank(province)) {
            wrapper.eq(UserSchool::getProvince, province);
        }
        
        wrapper.orderByDesc(UserSchool::getCreatedTime);
        
        // 分页查询
        Page<UserSchool> page = new Page<>(pageNum, pageSize);
        Page<UserSchool> result = userSchoolMapper.selectPage(page, wrapper);
        
        // 转换为响应对象
        List<SchoolResponse> list = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(result.getTotal(), list);
    }
    
    @Override
    public SchoolResponse getSchoolDetail(Long schoolId) {
        UserSchool school = userSchoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "学校不存在");
        }
        
        SchoolResponse response = convertToResponse(school);
        
        // 统计教师和学生数量
        LambdaQueryWrapper<UserSchoolMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSchoolMember::getSchoolId, schoolId);
        wrapper.eq(UserSchoolMember::getStatus, 1);
        
        // 教师数量
        wrapper.eq(UserSchoolMember::getMemberType, 2);
        response.setTeacherCount(userSchoolMemberMapper.selectCount(wrapper).intValue());
        
        // 学生数量
        wrapper.clear();
        wrapper.eq(UserSchoolMember::getSchoolId, schoolId);
        wrapper.eq(UserSchoolMember::getStatus, 1);
        wrapper.eq(UserSchoolMember::getMemberType, 3);
        response.setStudentCount(userSchoolMemberMapper.selectCount(wrapper).intValue());
        
        // 课程数量暂时设为0,后续从课程服务获取
        response.setCourseCount(0);
        
        return response;
    }
    
    /**
     * 转换为响应对象
     */
    private SchoolResponse convertToResponse(UserSchool school) {
        SchoolResponse response = new SchoolResponse();
        response.setSchoolId(school.getId());
        response.setSchoolCode(school.getSchoolCode());
        response.setSchoolName(school.getSchoolName());
        response.setProvince(school.getProvince());
        response.setCity(school.getCity());
        response.setAddress(school.getAddress());
        response.setLogoUrl(school.getLogoUrl());
        response.setDescription(school.getDescription());
        return response;
    }
    
}
