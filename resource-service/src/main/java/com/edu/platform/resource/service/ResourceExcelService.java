package com.edu.platform.resource.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * 资源 Excel 服务
 *
 * @author Education Platform
 */
public interface ResourceExcelService {

    /**
     * 下载导入模板
     *
     * @param response 响应
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 批量导入资源
     *
     * @param file    Excel文件
     * @param adminId 当前管理员ID
     * @return 导入结果明细
     */
    Map<String, Object> importResources(MultipartFile file, Long adminId);
}
