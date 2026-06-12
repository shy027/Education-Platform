package com.edu.platform.user.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.user.config.LocalStorageProperties;
import com.edu.platform.user.service.FileService;
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
 * 文件服务实现（本地存储版）
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssFileServiceImpl implements FileService {

    private final LocalStorageProperties storageProperties;

    // 允许的图片格式
    private static final List<String> IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
    // 允许的文档格式
    private static final List<String> DOC_TYPES = Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx");
    // 图片最大10MB
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    // 文档最大10MB
    private static final long MAX_DOC_SIZE = 10 * 1024 * 1024;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        validateFile(file, extension);

        // 使用 UUID 作为文件名前缀，避免重名覆盖
        String fileName = IdUtil.simpleUUID() + "." + extension;

        // 完整本地存储路径，例如: /home/uadmin/deploy/uploads/user/avatar/xxxx.jpg
        String localDirPath = storageProperties.getPath() + "/" + folder;
        Path dirPath = Paths.get(localDirPath);

        try {
            // 若目录不存在则自动创建
            Files.createDirectories(dirPath);

            Path filePath = dirPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            // 对外访问的 URL: http://10.54.0.36/uploads/user/avatar/xxxx.jpg
            String fileUrl = storageProperties.getBaseUrl() + "/uploads/" + folder + "/" + fileName;

            log.info("文件上传成功(本地): {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.FAIL.getCode(), "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return;
        }

        try {
            // 从 URL 中提取相对路径，还原本地文件路径
            // URL 格式: http://10.54.0.36/uploads/user/avatar/xxxx.jpg
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

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        // 头像存储到 user/avatar/ 目录下（userId 作为子目录）
        return uploadFile(file, "user/avatar/" + userId);
    }

    /**
     * 获取文件扩展名（小写）
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件格式不正确");
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 验证文件类型和大小
     */
    private void validateFile(MultipartFile file, String extension) {
        long fileSize = file.getSize();

        if (IMAGE_TYPES.contains(extension)) {
            if (fileSize > MAX_IMAGE_SIZE) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "图片大小不能超过10MB");
            }
            return;
        }

        if (DOC_TYPES.contains(extension)) {
            if (fileSize > MAX_DOC_SIZE) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文档大小不能超过10MB");
            }
            return;
        }

        throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不支持的文件格式: " + extension);
    }

}
