package com.edu.platform.report.service.impl;

import com.edu.platform.report.config.LocalStorageProperties;
import com.edu.platform.report.service.OssFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件服务实现 (保留类名 OssFileServiceImpl 以尽量不影响其它依赖处)
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssFileServiceImpl implements OssFileService {

    private final LocalStorageProperties storageProperties;

    // PDF最大50MB
    private static final long MAX_PDF_SIZE = 50 * 1024 * 1024;

    @Override
    public String uploadPdf(byte[] pdfBytes, String fileName, Long courseId) {
        // 1. 验证文件大小
        if (pdfBytes.length > MAX_PDF_SIZE) {
            throw new RuntimeException("PDF文件不能超过50MB");
        }

        // 2. 构建路径: report/course/{courseId}/{fileName} (根据用户的目录结构，报告分类)
        String subFolder = "report/course/" + courseId;
        String localDirPath = storageProperties.getPath() + "/" + subFolder;
        Path dirPath = Paths.get(localDirPath);

        try {
            // 3. 若目录不存在则自动创建
            Files.createDirectories(dirPath);

            Path filePath = dirPath.resolve(fileName);
            File localFile = filePath.toFile();

            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                fos.write(pdfBytes);
                fos.flush();
            }

            // 4. 返回URL
            String fileUrl = storageProperties.getBaseUrl() + "/uploads/" + subFolder + "/" + fileName;

            log.info("PDF文件生成成功(本地): {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("PDF文件生成失败", e);
            throw new RuntimeException("文件生成失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // 从 URL 中提取相对路径，还原本地文件路径
            // URL 格式: http://10.54.0.36/uploads/report/course/xxxx.pdf
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
    public String generatePresignedUrl(String fileUrl) {
        // 本地存储不需要预签名，直接返回原URL即可
        return fileUrl;
    }

}
