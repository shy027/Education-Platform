package com.edu.platform.user.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.user.config.AliyunOssProperties;
import com.edu.platform.user.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * 文件服务实现(阿里云OSS)
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssFileServiceImpl implements FileService {
    
    private final OSS ossClient;
    private final AliyunOssProperties ossProperties;
    
    // 允许的图片格式
    private static final List<String> IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
    // 允许的文档格式
    private static final List<String> DOC_TYPES = Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx");
    // 图片最大2MB
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024;
    // 文档最大10MB
    private static final long MAX_DOC_SIZE = 10 * 1024 * 1024;
    
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件不能为空");
        }
        
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件名不能为空");
        }
        
        // 获取文件扩展名
        String extension = getFileExtension(originalFilename);
        
        // 验证文件类型和大小
        validateFile(file, extension);
        
        // 生成唯一文件名
        String fileName = IdUtil.simpleUUID() + "." + extension;
        
        // 构建完整路径: education/folder/fileName
        String objectName = ossProperties.getFolder() + "/" + folder + "/" + fileName;
        
        try (InputStream inputStream = file.getInputStream()) {
            // 上传到OSS
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossProperties.getBucketName(),
                    objectName,
                    inputStream
            );
            ossClient.putObject(putObjectRequest);
            
            // 返回文件URL
            String fileUrl = "https://" + ossProperties.getBucketName() + "." + 
                            ossProperties.getEndpoint() + "/" + objectName;
            
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.FAIL.getCode(), "文件上传失败");
        }
    }
    
    @Override
    public void deleteFile(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return;
        }
        
        try {
            // 从URL中提取objectName
            String objectName = extractObjectName(fileUrl);
            if (StrUtil.isNotBlank(objectName)) {
                ossClient.deleteObject(ossProperties.getBucketName(), objectName);
                log.info("文件删除成功: {}", objectName);
            }
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
        }
    }
    
    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        return uploadFile(file, "avatar/" + userId);
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件格式不正确");
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file, String extension) {
        long fileSize = file.getSize();
        
        // 验证图片
        if (IMAGE_TYPES.contains(extension)) {
            if (fileSize > MAX_IMAGE_SIZE) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), 
                        "图片大小不能超过2MB");
            }
            return;
        }
        
        // 验证文档
        if (DOC_TYPES.contains(extension)) {
            if (fileSize > MAX_DOC_SIZE) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), 
                        "文档大小不能超过10MB");
            }
            return;
        }
        
        throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), 
                "不支持的文件格式: " + extension);
    }
    
    /**
     * 从URL中提取objectName
     */
    private String extractObjectName(String fileUrl) {
        // URL格式: https://bucket.endpoint/education/folder/file.ext
        String prefix = "https://" + ossProperties.getBucketName() + "." + 
                       ossProperties.getEndpoint() + "/";
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        return null;
    }
    
}
