package com.edu.platform.ai.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传文件到 OSS
     * @param file 文件对象
     * @param folder 文件夹名称
     * @return 文件访问 URL
     */
    String uploadFile(MultipartFile file, String folder);
}
