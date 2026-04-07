package com.edu.platform.report.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.edu.platform.report.config.AliyunOssProperties;
import com.edu.platform.report.service.OssFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;

/**
 * OSS文件服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssFileServiceImpl implements OssFileService {
    
    private final OSS ossClient;
    private final AliyunOssProperties ossProperties;
    
    // PDF最大50MB
    private static final long MAX_PDF_SIZE = 50 * 1024 * 1024;
    
    @Override
    public String uploadPdf(byte[] pdfBytes, String fileName, Long courseId) {
        // 1. 验证文件大小
        if (pdfBytes.length > MAX_PDF_SIZE) {
            throw new RuntimeException("PDF文件不能超过50MB");
        }
        
        // 2. 构建路径: education/reports/{courseId}/fileName
        String objectName = ossProperties.getFolder() + "/" + courseId + "/" + fileName;
        
        try {
            // 3. 上传到OSS
            ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
            PutObjectRequest request = new PutObjectRequest(
                ossProperties.getBucketName(),
                objectName,
                inputStream
            );
            ossClient.putObject(request);
            
            // 4. 返回URL
            String fileUrl = "https://" + ossProperties.getBucketName() + "." + 
                           ossProperties.getEndpoint() + "/" + objectName;
            
            log.info("PDF文件上传成功: {}", fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("PDF文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            String objectName = extractObjectName(fileUrl);
            if (objectName != null) {
                // 如果提取结果还是URL，说明解析逻辑可能有问题，打印警告
                if (objectName.startsWith("http")) {
                    log.warn("未能成功从URL中提取对象名称: {}", fileUrl);
                }
                ossClient.deleteObject(ossProperties.getBucketName(), objectName);
                log.info("OSS文件删除成功: {}", objectName);
            }
        } catch (Exception e) {
            log.error("OSS文件删除失败: {}", fileUrl, e);
        }
    }
    
    @Override
    public String generatePresignedUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new RuntimeException("文件URL不能为空");
        }
        
        String objectName = extractObjectName(fileUrl);
        if (objectName == null) {
            throw new RuntimeException("无效的文件URL");
        }
        
        try {
            // 生成1小时有效期的预签名URL
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            URL url = ossClient.generatePresignedUrl(
                ossProperties.getBucketName(),
                objectName,
                expiration
            );
            
            log.info("生成预签名URL成功: {}", url.toString());
            return url.toString();
            
        } catch (Exception e) {
            log.error("生成预签名URL失败", e);
            throw new RuntimeException("生成下载链接失败: " + e.getMessage());
        }
    }
    
    /**
     * 从URL中提取objectName (鲁棒且解码版本)
     */
    private String extractObjectName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return null;
        
        String path = null;
        try {
            // 1. 如果不是URL开头(不包含://)，则认为已经是objectName
            if (!fileUrl.contains("://")) {
                path = fileUrl;
            } else {
                // 2. 尝试定位域名后的路径部分
                String bucketEndpoint = ossProperties.getBucketName() + "." + ossProperties.getEndpoint();
                int index = fileUrl.indexOf(bucketEndpoint);
                
                if (index != -1) {
                    // 截取域名之后的部分
                    path = fileUrl.substring(index + bucketEndpoint.length());
                } else {
                    // 3. 兼容兜底: 解析URI路径
                    java.net.URI uri = new java.net.URI(fileUrl);
                    path = uri.getPath();
                }
            }
            
            if (path == null) return null;
            
            // 4. 重要: URL解码 (处理文件名中的特殊字符或编码)
            path = java.net.URLDecoder.decode(path, "UTF-8");
            
            // 5. 规范化: 去掉起始斜杠，处理双斜杠
            while (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path.replace("//", "/");
            
        } catch (Exception e) {
            log.error("解析OSS ObjectName失败: {}", fileUrl, e);
            return null;
        }
    }
}
