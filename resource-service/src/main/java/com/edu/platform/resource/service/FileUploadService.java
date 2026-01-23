package com.edu.platform.resource.service;

import com.edu.platform.resource.dto.response.AttachmentUploadResponse;
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
     * @param file 文件
     * @return 上传结果
     */
    AttachmentUploadResponse uploadImage(MultipartFile file);
    
    /**
     * 上传视频
     *
     * @param file 文件
     * @return 上传结果
     */
    AttachmentUploadResponse uploadVideo(MultipartFile file);
    
    /**
     * 上传PDF
     *
     * @param file 文件
     * @return 上传结果
     */
    AttachmentUploadResponse uploadPdf(MultipartFile file);
    
}
