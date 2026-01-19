package com.edu.platform.user.controller;

import com.edu.platform.common.result.Result;
import com.edu.platform.user.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件管理控制器
 *
 * @author Education Platform
 */
@Tag(name = "文件管理", description = "文件上传下载相关接口")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        
        String fileUrl = fileService.uploadAvatar(file, userId);
        
        Map<String, String> data = new HashMap<>();
        data.put("url", fileUrl);
        
        return Result.success("上传成功", data);
    }
    
    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "temp") String folder) {
        
        String fileUrl = fileService.uploadFile(file, folder);
        
        Map<String, String> data = new HashMap<>();
        data.put("url", fileUrl);
        
        return Result.success("上传成功", data);
    }
    
    @Operation(summary = "删除文件")
    @DeleteMapping
    public Result<Void> deleteFile(@RequestParam("url") String fileUrl) {
        fileService.deleteFile(fileUrl);
        return Result.success("删除成功", null);
    }
    
}
