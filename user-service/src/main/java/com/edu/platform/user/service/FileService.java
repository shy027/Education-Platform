package com.edu.platform.user.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 *
 * @author Education Platform
 */
public interface FileService {
    
    /**
     * 上传文件
     *
     * @param file 文件
     * @param folder 文件夹(avatar/school/course等)
     * @return 文件URL
     */
    String uploadFile(MultipartFile file, String folder);
    
    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
    
    /**
     * 上传头像
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像URL
     */
    String uploadAvatar(MultipartFile file, Long userId);
    
}
