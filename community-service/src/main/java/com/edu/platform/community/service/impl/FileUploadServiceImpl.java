package com.edu.platform.community.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.community.config.LocalStorageProperties;
import com.edu.platform.community.dto.response.FileUploadResponse;
import com.edu.platform.community.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传服务实现（本地存储版）
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final LocalStorageProperties storageProperties;

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

        // 上传到本地
        FileUploadResponse response = uploadToLocal(file, "community/images");

        log.info("图片上传成功, url={}", response.getUrl());
        return response;
    }

    @Override
    public FileUploadResponse uploadDocument(MultipartFile file) {
        log.info("上传文档, fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        // 验证文件
        validateFile(file, DOCUMENT_EXTENSIONS, "文档");

        // 上传到本地
        FileUploadResponse response = uploadToLocal(file, "community/documents");

        log.info("文档上传成功, url={}", response.getUrl());
        return response;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return;
        }

        try {
            // 从 URL 中提取相对路径，还原本地文件路径
            String uploadsPrefix = storageProperties.getBaseUrl() + "/uploads/";
            if (fileUrl.startsWith(uploadsPrefix)) {
                String relativePath = fileUrl.substring(uploadsPrefix.length());
                Path filePath = Paths.get(storageProperties.getPath(), relativePath);
                Files.deleteIfExists(filePath);
                log.info("文件删除成功: {}", filePath);
            } else {
                log.warn("非本地文件，跳过删除: {}", fileUrl);
            }
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
        }
    }

    /**
     * 上传文件到本地
     */
    private FileUploadResponse uploadToLocal(MultipartFile file, String subFolder) {
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        String fileName = IdUtil.simpleUUID() + "." + extension;

        // 构建本地路径
        String localDirPath = storageProperties.getPath() + "/" + subFolder;
        Path dirPath = Paths.get(localDirPath);

        try {
            Files.createDirectories(dirPath);

            Path filePath = dirPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            String fileUrl = storageProperties.getBaseUrl() + "/uploads/" + subFolder + "/" + fileName;

            FileUploadResponse response = new FileUploadResponse();
            response.setUrl(fileUrl);
            response.setFileName(originalFilename);
            response.setFileSize(file.getSize());

            return response;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.FAIL.getCode(), "文件上传失败: " + e.getMessage());
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
}
