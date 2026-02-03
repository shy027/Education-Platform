package com.edu.platform.community.service;

import com.edu.platform.community.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 *
 * @author Education Platform
 */
public interface FileUploadService {
    
    /**
     * 上传图片
     * 
     * @param file 图片文件
     * @return 上传响应
     */
    FileUploadResponse uploadImage(MultipartFile file);
    
    /**
     * 上传文档
     * 
     * @param file 文档文件
     * @return 上传响应
     */
    FileUploadResponse uploadDocument(MultipartFile file);
    
    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
}
