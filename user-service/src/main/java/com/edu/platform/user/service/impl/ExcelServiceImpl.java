package com.edu.platform.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.common.utils.PasswordUtil;
import com.edu.platform.user.dto.excel.UserExportExcel;
import com.edu.platform.user.dto.excel.UserImportExcel;
import com.edu.platform.user.dto.request.UserQueryRequest;
import com.edu.platform.user.entity.UserAccount;
import com.edu.platform.user.entity.UserRelRole;
import com.edu.platform.user.entity.UserRole;
import com.edu.platform.user.entity.UserSchool;
import com.edu.platform.user.entity.UserSchoolMember;
import com.edu.platform.user.mapper.UserAccountMapper;
import com.edu.platform.user.mapper.UserRelRoleMapper;
import com.edu.platform.user.mapper.UserRoleMapper;
import com.edu.platform.user.mapper.UserSchoolMapper;
import com.edu.platform.user.mapper.UserSchoolMemberMapper;
import com.edu.platform.user.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Excel服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {
    
    private final UserAccountMapper userAccountMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserRelRoleMapper userRelRoleMapper;
    private final UserSchoolMapper userSchoolMapper;
    private final UserSchoolMemberMapper userSchoolMemberMapper;
    
    @Override
    public void downloadUserTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("用户导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 创建示例数据
            List<UserImportExcel> list = new ArrayList<>();
            
            // 示例1: 学生
            UserImportExcel student = new UserImportExcel();
            student.setUsername("zhangsan");
            student.setPassword("123456");
            student.setRealName("张三");
            student.setPhone("13800138001");
            student.setEmail("zhangsan@example.com");
            student.setGender("男");
            student.setRoleCode("STUDENT");
            student.setStudentNo("2024001");
            student.setSchoolId("1");
            student.setDepartment("计算机学院");
            student.setMajor("软件工程");
            list.add(student);
            
            // 示例2: 教师
            UserImportExcel teacher = new UserImportExcel();
            teacher.setUsername("lisi");
            teacher.setPassword("123456");
            teacher.setRealName("李四");
            teacher.setPhone("13800138002");
            teacher.setEmail("lisi@example.com");
            teacher.setGender("女");
            teacher.setRoleCode("TEACHER");
            teacher.setStudentNo("T2024001");
            teacher.setSchoolId("1");
            teacher.setDepartment("计算机学院");
            teacher.setMajor("");
            list.add(teacher);
            
            EasyExcel.write(response.getOutputStream(), UserImportExcel.class)
                    .sheet("用户数据")
                    .doWrite(list);
                    
        } catch (IOException e) {
            log.error("下载模板失败", e);
            throw new BusinessException(ResultCode.FAIL.getCode(), "下载模板失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importUsers(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件不能为空");
        }
        
        List<UserImportExcel> successList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        
        try {
            EasyExcel.read(file.getInputStream(), UserImportExcel.class, 
                new PageReadListener<UserImportExcel>(dataList -> {
                    for (UserImportExcel data : dataList) {
                        try {
                            importUser(data);
                            successList.add(data);
                        } catch (Exception e) {
                            errorList.add(data.getUsername() + ": " + e.getMessage());
                        }
                    }
                })).sheet().doRead();
                
        } catch (IOException e) {
            log.error("导入用户失败", e);
            throw new BusinessException(ResultCode.FAIL.getCode(), "导入用户失败");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successList.size());
        result.put("errorCount", errorList.size());
        result.put("errors", errorList);
        
        return result;
    }
    
    @Override
    public void exportUsers(UserQueryRequest request, HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("用户列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 查询用户数据
            List<UserExportExcel> list = getUserExportData(request);
            
            EasyExcel.write(response.getOutputStream(), UserExportExcel.class)
                    .sheet("用户列表")
                    .doWrite(list);
                    
        } catch (IOException e) {
            log.error("导出用户失败", e);
            throw new BusinessException(ResultCode.FAIL.getCode(), "导出用户失败");
        }
    }
    
    /**
     * 导入单个用户
     */
    private void importUser(UserImportExcel data) {
        // 验证必填字段
        if (StrUtil.isBlank(data.getUsername())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名不能为空");
        }
        if (StrUtil.isBlank(data.getRealName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "真实姓名不能为空");
        }
        if (StrUtil.isBlank(data.getPhone())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "手机号不能为空");
        }
        if (StrUtil.isBlank(data.getRoleCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "角色不能为空");
        }
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getUsername, data.getUsername());
        if (userAccountMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "用户名已存在");
        }
        
        // 检查手机号是否已存在
        LambdaQueryWrapper<UserAccount> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(UserAccount::getPhone, data.getPhone());
        if (userAccountMapper.selectCount(phoneWrapper) > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "手机号已存在");
        }
        
        // 查询角色
        LambdaQueryWrapper<UserRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(UserRole::getRoleCode, data.getRoleCode());
        UserRole role = userRoleMapper.selectOne(roleWrapper);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "角色不存在: " + data.getRoleCode());
        }
        
        // 创建用户
        UserAccount user = new UserAccount();
        user.setUsername(data.getUsername());
        user.setRealName(data.getRealName());
        user.setPhone(data.getPhone());
        user.setEmail(data.getEmail());
        
        // 密码处理: 如果提供了密码则使用,否则默认123456
        String password = StrUtil.isNotBlank(data.getPassword()) ? data.getPassword() : "123456";
        user.setPassword(PasswordUtil.encode(password));
        
        // 性别处理: 男=1, 女=2, 其他=0
        Integer gender = 0;
        if ("男".equals(data.getGender())) {
            gender = 1;
        } else if ("女".equals(data.getGender())) {
            gender = 2;
        }
        user.setGender(gender);
        
        user.setStatus(1);
        
        userAccountMapper.insert(user);
        
        // 解析学校ID(用于角色关联)
        Long schoolId = null;
        if (StrUtil.isNotBlank(data.getSchoolId())) {
            try {
                schoolId = Long.parseLong(data.getSchoolId());
            } catch (NumberFormatException e) {
                log.warn("学校ID格式错误: {}", data.getSchoolId());
            }
        }
        
        // 分配角色
        UserRelRole userRelRole = new UserRelRole();
        userRelRole.setUserId(user.getId());
        userRelRole.setRoleId(role.getId());
        userRelRole.setSchoolId(schoolId); // 设置学校ID
        userRelRoleMapper.insert(userRelRole);
        
        // 如果提供了学校ID,关联学校
        if (schoolId != null) {
            UserSchool school = userSchoolMapper.selectById(schoolId);
            if (school != null) {
                UserSchoolMember member = new UserSchoolMember();
                member.setUserId(user.getId());
                member.setSchoolId(schoolId);
                
                // 根据角色设置成员类型: 1-校领导, 2-教师, 3-学生
                Integer memberType = 3; // 默认学生
                if ("ADMIN".equals(data.getRoleCode())) {
                    memberType = 1; // 校领导
                } else if ("TEACHER".equals(data.getRoleCode())) {
                    memberType = 2; // 教师
                }
                member.setMemberType(memberType);
                
                member.setJobNumber(data.getStudentNo()); // 工号/学号
                member.setDepartment(data.getDepartment());
                member.setJoinTime(java.time.LocalDateTime.now());
                member.setStatus(1);
                userSchoolMemberMapper.insert(member);
            }
        }
    }
    
    /**
     * 获取导出数据
     */
    private List<UserExportExcel> getUserExportData(UserQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(request.getUsername())) {
            wrapper.like(UserAccount::getUsername, request.getUsername());
        }
        if (StrUtil.isNotBlank(request.getRealName())) {
            wrapper.like(UserAccount::getRealName, request.getRealName());
        }
        if (StrUtil.isNotBlank(request.getPhone())) {
            wrapper.eq(UserAccount::getPhone, request.getPhone());
        }
        if (request.getStatus() != null) {
            wrapper.eq(UserAccount::getStatus, request.getStatus());
        }
        
        wrapper.orderByDesc(UserAccount::getCreatedTime);
        
        List<UserAccount> users = userAccountMapper.selectList(wrapper);
        
        // 如果指定了学校ID,需要过滤
        if (request.getSchoolId() != null) {
            users = users.stream()
                    .filter(user -> isUserInSchool(user.getId(), request.getSchoolId()))
                    .collect(Collectors.toList());
        }
        
        return users.stream().map(user -> {
            UserExportExcel excel = new UserExportExcel();
            excel.setId(user.getId());
            excel.setUsername(user.getUsername());
            excel.setRealName(user.getRealName());
            excel.setPhone(user.getPhone());
            excel.setEmail(user.getEmail());
            
            // 性别
            String gender = "未知";
            if (user.getGender() != null) {
                if (user.getGender() == 1) {
                    gender = "男";
                } else if (user.getGender() == 2) {
                    gender = "女";
                }
            }
            excel.setGender(gender);
            
            // 角色
            excel.setRoles(getUserRolesString(user.getId()));
            
            // 学校信息
            UserSchoolMember member = getUserSchoolMember(user.getId());
            if (member != null) {
                UserSchool school = userSchoolMapper.selectById(member.getSchoolId());
                excel.setSchoolName(school != null ? school.getSchoolName() : "");
                excel.setDepartment(member.getDepartment());
                excel.setJobNumber(member.getJobNumber());
            } else {
                excel.setSchoolName("");
                excel.setDepartment("");
                excel.setJobNumber("");
            }
            
            excel.setStatus(user.getStatus() == 1 ? "正常" : "禁用");
            excel.setLastLoginTime(user.getLastLoginTime() != null ? 
                    Timestamp.valueOf(user.getLastLoginTime()) : null);
            excel.setCreatedTime(Timestamp.valueOf(user.getCreatedTime()));
            return excel;
        }).collect(Collectors.toList());
    }
    
    /**
     * 判断用户是否在指定学校
     */
    private boolean isUserInSchool(Long userId, Long schoolId) {
        LambdaQueryWrapper<UserSchoolMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSchoolMember::getUserId, userId)
               .eq(UserSchoolMember::getSchoolId, schoolId);
        return userSchoolMemberMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 获取用户学校成员信息
     */
    private UserSchoolMember getUserSchoolMember(Long userId) {
        LambdaQueryWrapper<UserSchoolMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSchoolMember::getUserId, userId)
               .orderByDesc(UserSchoolMember::getJoinTime)
               .last("LIMIT 1");
        return userSchoolMemberMapper.selectOne(wrapper);
    }
    
    /**
     * 获取用户角色字符串
     */
    private String getUserRolesString(Long userId) {
        LambdaQueryWrapper<UserRelRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelRole::getUserId, userId);
        List<UserRelRole> userRoles = userRelRoleMapper.selectList(wrapper);
        
        if (userRoles.isEmpty()) {
            return "";
        }
        
        List<Long> roleIds = userRoles.stream()
                .map(UserRelRole::getRoleId)
                .collect(Collectors.toList());
        
        List<UserRole> roles = userRoleMapper.selectBatchIds(roleIds);
        
        return roles.stream()
                .map(UserRole::getRoleName)
                .collect(Collectors.joining(","));
    }
    
}
