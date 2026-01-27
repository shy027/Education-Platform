package com.edu.platform.course.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.course.config.AliyunOssProperties;
import com.edu.platform.course.dto.response.FileUploadResponse;
import com.edu.platform.course.service.CoursewareFileService;
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
 * 课件文件服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoursewareFileServiceImpl implements CoursewareFileService {
    
    private final OSS ossClient;
    private final AliyunOssProperties ossProperties;
    
    // 允许的视频格式
    private static final List<String> VIDEO_TYPES = Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv", "webm");
    // 允许的文档格式（PDF）
    private static final List<String> PDF_TYPES = Arrays.asList("pdf");
    // 允许的PPT格式
    private static final List<String> PPT_TYPES = Arrays.asList("ppt", "pptx");
    // 允许的文档格式（其他）
    private static final List<String> DOC_TYPES = Arrays.asList("doc", "docx", "xls", "xlsx");
    // 允许的音频格式
    private static final List<String> AUDIO_TYPES = Arrays.asList("mp3", "wav", "aac", "flac", "m4a", "ogg");
    // 允许的图片格式
    private static final List<String> IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
    
    // 不限制文件大小（设置为-1表示无限制）
    private static final long NO_SIZE_LIMIT = -1;
    
    @Override
    public FileUploadResponse uploadVideo(MultipartFile file) {
        validateFile(file, VIDEO_TYPES, NO_SIZE_LIMIT);
        FileUploadResponse response = uploadToOss(file, "coursewares/videos");
        
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
    public FileUploadResponse uploadPdf(MultipartFile file) {
        validateFile(file, PDF_TYPES, NO_SIZE_LIMIT);
        FileUploadResponse response = uploadToOss(file, "coursewares/docs");
        
        // 提取PDF页数
        String extension = getFileExtension(file.getOriginalFilename());
        if ("pdf".equals(extension)) {
            try (InputStream is = file.getInputStream()) {
                PDDocument document = PDDocument.load(is);
                response.setPageCount(document.getNumberOfPages());
                document.close();
            } catch (Exception e) {
                log.error("PDF页数提取失败", e);
            }
        }
        
        return response;
    }
    
    @Override
    public FileUploadResponse uploadAudio(MultipartFile file) {
        validateFile(file, AUDIO_TYPES, NO_SIZE_LIMIT);
        return uploadToOss(file, "coursewares/audios");
    }
    
    @Override
    public FileUploadResponse uploadPpt(MultipartFile file) {
        validateFile(file, PPT_TYPES, NO_SIZE_LIMIT);
        FileUploadResponse response = uploadToOss(file, "coursewares/ppts");
        
        // PPT页数提取可以在后续版本中实现
        // 目前直接返回上传结果
        log.info("PPT上传成功，文件URL: {}", response.getFileUrl());
        
        return response;
    }
    
    @Override
    public FileUploadResponse uploadCover(MultipartFile file) {
        validateFile(file, IMAGE_TYPES, NO_SIZE_LIMIT);
        return uploadToOss(file, "coursewares/covers");
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
            
            FileUploadResponse response = new FileUploadResponse();
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
        
        // 如果maxSize为-1，表示不限制文件大小
        if (maxSize != NO_SIZE_LIMIT && file.getSize() > maxSize) {
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
