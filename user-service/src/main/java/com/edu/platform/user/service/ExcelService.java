package com.edu.platform.user.service;

import com.edu.platform.user.dto.request.UserQueryRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Excel服务接口
 *
 * @author Education Platform
 */
public interface ExcelService {
    
    /**
     * 下载用户导入模板
     *
     * @param response HTTP响应
     * @param schoolId 预填学校ID(可选,校领导时使用)
     */
    void downloadUserTemplate(HttpServletResponse response, Long schoolId);
    
    /**
     * 导入用户
     *
     * @param file Excel文件
     * @param currentSchoolId 当前操作者所属学校ID(若是校领导,则强制限制为此ID)
     * @return 导入结果
     */
    Map<String, Object> importUsers(MultipartFile file, Long currentSchoolId);

    /**
     * 单独创建一个用户（表单方式）
     *
     * @param request        用户信息
     * @param currentSchoolId 当前操作者的学校ID（校领导传此值；管理员传null）
     */
    void createSingleUser(com.edu.platform.user.dto.request.CreateUserRequest request, Long currentSchoolId);
    
    /**
     * 导出用户列表
     *
     * @param request 查询条件
     * @param response HTTP响应
     */
    void exportUsers(UserQueryRequest request, HttpServletResponse response);
    
}
