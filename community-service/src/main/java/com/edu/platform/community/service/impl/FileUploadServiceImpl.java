package com.edu.platform.community.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.community.config.AliyunOssProperties;
import com.edu.platform.community.dto.response.FileUploadResponse;
import com.edu.platform.community.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {
    
    private final OSS ossClient;
    private final AliyunOssProperties ossProperties;
    
    // 允许的图片格式
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
    
    // 允许的文档格式
    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt");
    
    // 最大文件大小: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    @Override
    public FileUploadResponse uploadImage(MultipartFile file) {
        log.info("上传图片, fileName={}, size={}", file.getOriginalFilename(), file.getSize());
        
        // 验证文件
        validateFile(file, IMAGE_EXTENSIONS, "图片");
        
        // 上传到OSS
        FileUploadResponse response = uploadToOss(file, "community/images");
        
        log.info("图片上传成功, url={}", response.getUrl());
        return response;
    }
    
    @Override
    public FileUploadResponse uploadDocument(MultipartFile file) {
        log.info("上传文档, fileName={}, size={}", file.getOriginalFilename(), file.getSize());
        
        // 验证文件
        validateFile(file, DOCUMENT_EXTENSIONS, "文档");
        
        // 上传到OSS
        FileUploadResponse response = uploadToOss(file, "community/documents");
        
        log.info("文档上传成功, url={}", response.getUrl());
        return response;
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
    
    /**
     * 上传文件到OSS
     */
    private FileUploadResponse uploadToOss(MultipartFile file, String subFolder) {
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件名不能为空");
        }
        
        String extension = getFileExtension(originalFilename);
        String fileName = IdUtil.simpleUUID() + "." + extension;
        
        // 构建路径: education/community/subFolder/fileName
        String objectName = ossProperties.getFolder() + "/" + subFolder + "/" + fileName;
        
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossProperties.getBucketName(),
                    objectName,
                    inputStream
            );
            ossClient.putObject(putObjectRequest);
            
            String fileUrl = "https://" + ossProperties.getBucketName() + "." + 
                            ossProperties.getEndpoint() + "/" + objectName;
            
            FileUploadResponse response = new FileUploadResponse();
            response.setUrl(fileUrl);
            response.setFileName(originalFilename);
            response.setFileSize(file.getSize());
            
            return response;
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.FAIL.getCode(), "文件上传失败");
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file, List<String> allowedExtensions, String fileType) {
        // 验证文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件不能为空");
        }
        
        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), 
                    fileType + "大小不能超过10MB");
        }
        
        // 验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件名不合法");
        }
        
        String extension = getFileExtension(originalFilename);
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), 
                    "不支持的" + fileType + "格式,仅支持: " + String.join(", ", allowedExtensions));
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件无扩展名");
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
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
