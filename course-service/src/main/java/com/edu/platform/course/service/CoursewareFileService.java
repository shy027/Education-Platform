package com.edu.platform.course.service;

import com.edu.platform.course.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 课件文件服务
 *
 * @author Education Platform
 */
public interface CoursewareFileService {
    
    /**
     * 上传视频
     */
    FileUploadResponse uploadVideo(MultipartFile file);
    
    /**
     * 上传PDF文档
     */
    FileUploadResponse uploadPdf(MultipartFile file);
    
    /**
     * 上传音频
     */
    FileUploadResponse uploadAudio(MultipartFile file);
    
    /**
     * 上传PPT文档
     */
    FileUploadResponse uploadPpt(MultipartFile file);
    
    /**
     * 上传课件封面
     */
    FileUploadResponse uploadCover(MultipartFile file);
    
    /**
     * 删除文件
     */
    void deleteFile(String fileUrl);
}
