package com.edu.platform.course.controller;

import com.edu.platform.common.annotation.RequireTeacherOrAbove;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.response.FileUploadResponse;
import com.edu.platform.course.service.CoursewareFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 课件文件上传控制器
 *
 * @author Education Platform
 */
@RestController
@RequestMapping("/api/v1/courseware/files")
@RequiredArgsConstructor
@Tag(name = "课件文件上传", description = "课件文件上传接口(视频、文档、音频、封面)")
public class CoursewareFileController {
    
    private final CoursewareFileService fileService;
    
    @PostMapping("/video")
    @RequireTeacherOrAbove
    @Operation(summary = "上传视频", description = "上传视频课件文件,无大小限制")
    public Result<FileUploadResponse> uploadVideo(
            @Parameter(description = "视频文件") @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileService.uploadVideo(file);
        return Result.success(response);
    }
    
    @PostMapping("/pdf")
    @RequireTeacherOrAbove
    @Operation(summary = "上传PDF文档", description = "上传PDF文档课件,无大小限制")
    public Result<FileUploadResponse> uploadPdf(
            @Parameter(description = "PDF文件") @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileService.uploadPdf(file);
        return Result.success(response);
    }
    
    @PostMapping("/audio")
    @RequireTeacherOrAbove
    @Operation(summary = "上传音频", description = "上传音频课件文件,无大小限制")
    public Result<FileUploadResponse> uploadAudio(
            @Parameter(description = "音频文件") @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileService.uploadAudio(file);
        return Result.success(response);
    }
    
    @PostMapping("/ppt")
    @RequireTeacherOrAbove
    @Operation(summary = "上传PPT文档", description = "上传PPT课件文件,无大小限制")
    public Result<FileUploadResponse> uploadPpt(
            @Parameter(description = "PPT文件") @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileService.uploadPpt(file);
        return Result.success(response);
    }
    
    @PostMapping("/cover")
    @RequireTeacherOrAbove
    @Operation(summary = "上传封面", description = "上传课件封面图片,无大小限制")
    public Result<FileUploadResponse> uploadCover(
            @Parameter(description = "封面图片") @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileService.uploadCover(file);
        return Result.success(response);
    }
}
