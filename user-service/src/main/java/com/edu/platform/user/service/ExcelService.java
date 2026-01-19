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
     */
    void downloadUserTemplate(HttpServletResponse response);
    
    /**
     * 导入用户
     *
     * @param file Excel文件
     * @return 导入结果
     */
    Map<String, Object> importUsers(MultipartFile file);
    
    /**
     * 导出用户列表
     *
     * @param request 查询条件
     * @param response HTTP响应
     */
    void exportUsers(UserQueryRequest request, HttpServletResponse response);
    
}
