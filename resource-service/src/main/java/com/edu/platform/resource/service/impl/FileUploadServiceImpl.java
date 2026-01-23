package com.edu.platform.resource.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.resource.config.AliyunOssProperties;
import com.edu.platform.resource.dto.response.AttachmentUploadResponse;
import com.edu.platform.resource.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
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
    private static final List<String> IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
    // 允许的视频格式
    private static final List<String> VIDEO_TYPES = Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv");
    // 允许的PDF格式
    private static final List<String> PDF_TYPES = Arrays.asList("pdf");
    
    // 图片最大5MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    // 视频最大500MB
    private static final long MAX_VIDEO_SIZE = 500 * 1024 * 1024;
    // PDF最大50MB
    private static final long MAX_PDF_SIZE = 50 * 1024 * 1024;
    
    @Override
    public AttachmentUploadResponse uploadImage(MultipartFile file) {
        validateFile(file, IMAGE_TYPES, MAX_IMAGE_SIZE);
        return uploadToOss(file, "images");
    }
    
    @Override
    public AttachmentUploadResponse uploadVideo(MultipartFile file) {
        validateFile(file, VIDEO_TYPES, MAX_VIDEO_SIZE);
        AttachmentUploadResponse response = uploadToOss(file, "videos");
        
        // 提取视频元数据
        File tempFile = null;
        try {
            // 创建临时文件用于解析
            tempFile = File.createTempFile("video_" + IdUtil.simpleUUID(), ".tmp");
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 InputStream is = file.getInputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }
            
            IsoFile isoFile = new IsoFile(tempFile.getAbsolutePath());
            MovieBox movieBox = isoFile.getMovieBox();
            if (movieBox != null) {
                MovieHeaderBox movieHeaderBox = movieBox.getMovieHeaderBox();
                if (movieHeaderBox != null) {
                    long duration = movieHeaderBox.getDuration() / movieHeaderBox.getTimescale();
                    response.setDuration((int) duration);
                }
            }
            isoFile.close();
            
        } catch (Exception e) {
            log.error("视频元数据提取失败", e);
            // 提取失败不影响上传结果
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        
        return response;
    }
    
    @Override
    public AttachmentUploadResponse uploadPdf(MultipartFile file) {
        validateFile(file, PDF_TYPES, MAX_PDF_SIZE);
        AttachmentUploadResponse response = uploadToOss(file, "docs");
        
        // 提取PDF页数
        try (InputStream is = file.getInputStream()) {
            PDDocument document = PDDocument.load(is);
            response.setPageCount(document.getNumberOfPages());
            document.close();
        } catch (Exception e) {
            log.error("PDF页数提取失败", e);
        }
        
        return response;
    }
    
    /**
     * 上传文件到OSS
     */
    private AttachmentUploadResponse uploadToOss(MultipartFile file, String subFolder) {
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件名不能为空");
        }
        
        String extension = getFileExtension(originalFilename);
        String fileName = IdUtil.simpleUUID() + "." + extension;
        
        // 构建路径: education/subFolder/fileName
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
            
            AttachmentUploadResponse response = new AttachmentUploadResponse();
            response.setFileName(originalFilename);
            response.setFileUrl(fileUrl);
            response.setFileSize(file.getSize());
            response.setFileType(extension);
            response.setMimeType(file.getContentType());
            
            return response;
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.FAIL.getCode(), "文件上传失败");
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file, List<String> allowedTypes, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件不能为空");
        }
        
        if (file.getSize() > maxSize) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), 
                    "文件大小超过限制: " + (maxSize / 1024 / 1024) + "MB");
        }
        
        String extension = getFileExtension(file.getOriginalFilename());
        if (!allowedTypes.contains(extension)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), 
                    "不支持的文件类型: " + extension);
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
    
}
