package com.edu.platform.community.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.community.dto.response.FileUploadResponse;
import com.edu.platform.community.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 *
 * @author Education Platform
 */
@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/v1/community/files")
@RequiredArgsConstructor
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    
    @Operation(summary = "上传图片", description = "支持jpg, jpeg, png, gif, bmp, webp格式,最大10MB")
    @PostMapping("/image")
    public Result<FileUploadResponse> uploadImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileUploadService.uploadImage(file);
        return Result.success("上传成功", response);
    }
    
    @Operation(summary = "上传文档", description = "支持pdf, doc, docx, xls, xlsx, ppt, pptx, txt格式,最大10MB")
    @PostMapping("/document")
    public Result<FileUploadResponse> uploadDocument(
            @Parameter(description = "文档文件") @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileUploadService.uploadDocument(file);
        return Result.success("上传成功", response);
    }
}
