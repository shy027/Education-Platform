package com.edu.platform.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.community.dto.request.CreateDocumentRequest;
import com.edu.platform.community.dto.request.UpdateDocumentRequest;
import com.edu.platform.community.dto.response.DocumentDetailResponse;
import com.edu.platform.community.dto.response.DocumentHistoryResponse;

/**
 * 小组协作文档Service接口
 */
public interface GroupDocumentService {
    
    /**
     * 创建文档
     *
     * @param groupId 小组ID
     * @param request 创建请求
     * @param userId 用户ID
     * @return 文档ID
     */
    Long createDocument(Long groupId, CreateDocumentRequest request, Long userId);
    
    /**
     * 更新文档内容
     *
     * @param groupId 小组ID
     * @param documentId 文档ID
     * @param request 更新请求
     * @param userId 用户ID
     */
    void updateDocument(Long groupId, Long documentId, UpdateDocumentRequest request, Long userId);
    
    /**
     * 获取文档详情
     *
     * @param groupId 小组ID
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 文档详情
     */
    DocumentDetailResponse getDocument(Long groupId, Long documentId, Long userId);
    
    /**
     * 获取文档编辑历史
     *
     * @param groupId 小组ID
     * @param documentId 文档ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param userId 用户ID
     * @return 历史记录
     */
    Page<DocumentHistoryResponse> getDocumentHistory(Long groupId, Long documentId, Integer pageNum, Integer pageSize, Long userId);
    
    /**
     * 删除文档
     *
     * @param groupId 小组ID
     * @param documentId 文档ID
     * @param userId 用户ID
     */
    void deleteDocument(Long groupId, Long documentId, Long userId);
}
