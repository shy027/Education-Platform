package com.edu.platform.resource.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.resource.dto.response.AttachmentUploadResponse;
import com.edu.platform.resource.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 *
 * @author Education Platform
 */
@Tag(name = "文件上传", description = "附件上传相关接口")
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    
    @Operation(summary = "上传图片")
    @PostMapping(value = "/image", consumes = "multipart/form-data")
    public Result<AttachmentUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.success(fileUploadService.uploadImage(file));
    }
    
    @Operation(summary = "上传视频")
    @PostMapping(value = "/video", consumes = "multipart/form-data")
    public Result<AttachmentUploadResponse> uploadVideo(@RequestParam("file") MultipartFile file) {
        return Result.success(fileUploadService.uploadVideo(file));
    }
    
    @Operation(summary = "上传PDF")
    @PostMapping(value = "/pdf", consumes = "multipart/form-data")
    public Result<AttachmentUploadResponse> uploadPdf(@RequestParam("file") MultipartFile file) {
        return Result.success(fileUploadService.uploadPdf(file));
    }
    
}
