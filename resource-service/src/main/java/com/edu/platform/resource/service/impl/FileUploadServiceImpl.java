package com.edu.platform.resource.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.resource.config.LocalStorageProperties;
import com.edu.platform.resource.dto.response.AttachmentUploadResponse;
import com.edu.platform.resource.service.FileUploadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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
    private static final List<String> IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
    // 允许的视频格式
    private static final List<String> VIDEO_TYPES = Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv");
    // 允许的音频格式
    private static final List<String> AUDIO_TYPES = Arrays.asList("mp3", "wav", "m4a", "aac", "flac", "ogg");
    // 允许的PDF及文档格式
    private static final List<String> PDF_TYPES = Arrays.asList("pdf");

    // 图片最大10MB
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    // 视频最大200MB
    private static final long MAX_VIDEO_SIZE = 200 * 1024 * 1024;
    // 音频最大200MB
    private static final long MAX_AUDIO_SIZE = 200 * 1024 * 1024;
    // PDF最大10MB
    private static final long MAX_PDF_SIZE = 10 * 1024 * 1024;

    @Override
    public AttachmentUploadResponse uploadImage(MultipartFile file) {
        validateFile(file, IMAGE_TYPES, MAX_IMAGE_SIZE);
        return uploadToLocal(file, "resource/cover");
    }

    @Override
    public AttachmentUploadResponse uploadAudio(MultipartFile file) {
        validateFile(file, AUDIO_TYPES, MAX_AUDIO_SIZE);
        return uploadToLocal(file, "resource/attachment");
    }

    @Override
    public AttachmentUploadResponse uploadVideo(MultipartFile file) {
        validateFile(file, VIDEO_TYPES, MAX_VIDEO_SIZE);
        AttachmentUploadResponse response = uploadToLocal(file, "resource/attachment");

        // 提取视频元数据（需要临时文件来解析）
        File tempFile = null;
        try {
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
        AttachmentUploadResponse response = uploadToLocal(file, "resource/attachment");

        String extension = getFileExtension(file.getOriginalFilename());
        // 仅提取PDF页数
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

    /**
     * 上传文件到本地服务器
     *
     * @param file      上传的文件
     * @param subFolder 存储子目录，例如 "course/cover", "resource/attachment"
     * @return 上传结果（含可访问的URL）
     */
    private AttachmentUploadResponse uploadToLocal(MultipartFile file, String subFolder) {
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        // 使用 UUID 作为文件名前缀，彻底避免重名冲突
        String fileName = IdUtil.simpleUUID() + "." + extension;

        // 完整本地存储路径: /home/uadmin/deploy/uploads/course/cover/xxxx.jpg
        String localDirPath = storageProperties.getPath() + "/" + subFolder;
        Path dirPath = Paths.get(localDirPath);

        try {
            // 若目录不存在则自动创建
            Files.createDirectories(dirPath);

            Path filePath = dirPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            // 对外访问的 URL: http://10.54.0.36/uploads/course/cover/xxxx.jpg
            String fileUrl = storageProperties.getBaseUrl() + "/uploads/" + subFolder + "/" + fileName;

            log.info("文件上传成功(本地): {}", fileUrl);

            AttachmentUploadResponse response = new AttachmentUploadResponse();
            response.setFileName(originalFilename);
            response.setFileUrl(fileUrl);
            response.setFileSize(file.getSize());
            response.setFileType(extension);
            response.setMimeType(file.getContentType());

            return response;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.FAIL.getCode(), "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 验证文件格式和大小
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
     * 获取文件扩展名（小写）
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件无扩展名");
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    @Override
    public void proxyPdf(String fileUrl, HttpServletResponse response) {
        if (StrUtil.isBlank(fileUrl) || !fileUrl.toLowerCase().contains(".pdf")) {
            throw new BusinessException("非合法的PDF预览请求");
        }

        try {
            // 从 URL 中提取相对路径，还原本地文件路径
            // URL 格式: http://10.54.0.36/uploads/resource/attachment/xxxx.pdf
            String uploadsPrefix = storageProperties.getBaseUrl() + "/uploads/";
            String relativePath = fileUrl.startsWith(uploadsPrefix)
                    ? fileUrl.substring(uploadsPrefix.length())
                    : fileUrl;

            Path filePath = Paths.get(storageProperties.getPath(), relativePath);
            File localFile = filePath.toFile();

            log.info("开始代理预览PDF: {}", filePath);

            if (!localFile.exists()) {
                throw new BusinessException("文件不存在: " + filePath);
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=" + IdUtil.simpleUUID() + ".pdf");

            try (InputStream is = new FileInputStream(localFile);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
        } catch (Exception e) {
            log.error("PDF代理预览失败: " + fileUrl, e);
            throw new BusinessException("无法加载该PDF文件进行预览");
        }
    }

}
