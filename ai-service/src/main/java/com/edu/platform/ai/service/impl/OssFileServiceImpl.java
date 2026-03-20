package com.edu.platform.ai.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.edu.platform.ai.config.AliyunOssProperties;
import com.edu.platform.ai.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * OSS 文件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssFileServiceImpl implements FileService {

    private final OSS ossClient;
    private final AliyunOssProperties ossProperties;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String fileName = UUID.randomUUID().toString() + extension;
        String objectName = ossProperties.getFolder() + "/" + folder + "/" + fileName;
        
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossProperties.getBucketName(),
                    objectName,
                    inputStream
            );
            
            ossClient.putObject(putObjectRequest);
            
            // 返回文件 URL
            return "https://" + ossProperties.getBucketName() + "." + 
                   ossProperties.getEndpoint() + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传到 OSS 失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败");
        }
    }
}
