package com.edu.platform.community.controller;

import com.edu.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 *
 * @author Education Platform
 */
@Slf4j
@Tag(name = "文件管理", description = "社区文件上传相关接口")
@RestController
@RequestMapping("/api/v1/community/files")
public class FileController {
    
    @Operation(summary = "上传附件")
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "community") String folder) {
        
        log.info("上传文件: {}, folder: {}", file.getOriginalFilename(), folder);
        
        // TODO: 实现真实的OSS上传逻辑
        // String fileUrl = ossFileService.uploadFile(file, folder);
        
        // 临时返回模拟URL
        String fileUrl = "https://example.com/community/" + file.getOriginalFilename();
        
        Map<String, String> data = new HashMap<>();
        data.put("url", fileUrl);
        
        return Result.success("上传成功", data);
    }
    
}
