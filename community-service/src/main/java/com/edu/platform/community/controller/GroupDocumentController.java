package com.edu.platform.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.community.dto.request.CreateDocumentRequest;
import com.edu.platform.community.dto.request.UpdateDocumentRequest;
import com.edu.platform.community.dto.response.DocumentDetailResponse;
import com.edu.platform.community.dto.response.DocumentHistoryResponse;
import com.edu.platform.community.service.GroupDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 小组协作文档Controller
 */
@Tag(name = "小组协作文档管理")
@RestController
@RequestMapping("/api/v1/community/groups")
public class GroupDocumentController {
    
    private final GroupDocumentService documentService;
    
    public GroupDocumentController(GroupDocumentService documentService) {
        this.documentService = documentService;
    }
    
    @Operation(summary = "创建协作文档")
    @PostMapping("/{groupId}/documents")
    public Result<Long> createDocument(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateDocumentRequest request) {
        Long userId = UserContext.getUserId();
        Long documentId = documentService.createDocument(groupId, request, userId);
        return Result.success("创建成功", documentId);
    }
    
    @Operation(summary = "更新文档内容")
    @PutMapping("/{groupId}/documents/{documentId}")
    public Result<?> updateDocument(
            @PathVariable Long groupId,
            @PathVariable Long documentId,
            @Valid @RequestBody UpdateDocumentRequest request) {
        Long userId = UserContext.getUserId();
        documentService.updateDocument(groupId, documentId, request, userId);
        return Result.success("更新成功");
    }
    
    @Operation(summary = "获取文档详情")
    @GetMapping("/{groupId}/documents/{documentId}")
    public Result<DocumentDetailResponse> getDocument(
            @PathVariable Long groupId,
            @PathVariable Long documentId) {
        Long userId = UserContext.getUserId();
        DocumentDetailResponse response = documentService.getDocument(groupId, documentId, userId);
        return Result.success("查询成功", response);
    }
    
    @Operation(summary = "获取文档编辑历史")
    @GetMapping("/{groupId}/documents/{documentId}/history")
    public Result<Page<DocumentHistoryResponse>> getDocumentHistory(
            @PathVariable Long groupId,
            @PathVariable Long documentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<DocumentHistoryResponse> page = documentService.getDocumentHistory(groupId, documentId, pageNum, pageSize, userId);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "删除文档(仅教师)")
    @DeleteMapping("/{groupId}/documents/{documentId}")
    public Result<?> deleteDocument(
            @PathVariable Long groupId,
            @PathVariable Long documentId) {
        Long userId = UserContext.getUserId();
        documentService.deleteDocument(groupId, documentId, userId);
        return Result.success("删除成功");
    }
}
