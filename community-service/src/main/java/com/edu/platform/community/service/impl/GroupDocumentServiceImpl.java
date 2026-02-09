package com.edu.platform.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.Result;
import com.edu.platform.community.client.UserServiceClient;
import com.edu.platform.community.dto.request.CreateDocumentRequest;
import com.edu.platform.community.dto.request.UpdateDocumentRequest;
import com.edu.platform.community.dto.response.CourseMemberDTO;
import com.edu.platform.community.dto.response.DocumentDetailResponse;
import com.edu.platform.community.dto.response.DocumentHistoryResponse;
import com.edu.platform.community.dto.response.UserInfoDTO;
import com.edu.platform.community.entity.CommunityGroup;
import com.edu.platform.community.entity.GroupDocument;
import com.edu.platform.community.entity.GroupDocumentHistory;
import com.edu.platform.community.mapper.CommunityGroupMapper;
import com.edu.platform.community.mapper.GroupDocumentHistoryMapper;
import com.edu.platform.community.mapper.GroupDocumentMapper;
import com.edu.platform.community.service.GroupDocumentService;
import com.edu.platform.community.util.PermissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小组协作文档Service实现类
 */
@Slf4j
@Service
public class GroupDocumentServiceImpl implements GroupDocumentService {
    
    private final GroupDocumentMapper documentMapper;
    private final GroupDocumentHistoryMapper historyMapper;
    private final CommunityGroupMapper groupMapper;
    private final PermissionUtil permissionUtil;
    private final UserServiceClient userServiceClient;
    
    public GroupDocumentServiceImpl(GroupDocumentMapper documentMapper,
                                     GroupDocumentHistoryMapper historyMapper,
                                     CommunityGroupMapper groupMapper,
                                     PermissionUtil permissionUtil,
                                     UserServiceClient userServiceClient) {
        this.documentMapper = documentMapper;
        this.historyMapper = historyMapper;
        this.groupMapper = groupMapper;
        this.permissionUtil = permissionUtil;
        this.userServiceClient = userServiceClient;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDocument(Long groupId, CreateDocumentRequest request, Long userId) {
        log.info("创建协作文档, groupId={}, userId={}, title={}", groupId, userId, request.getTitle());
        
        // 1. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 2. 验证用户权限(仅教师可创建文档)
        permissionUtil.checkTeacher(userId, group.getCourseId());
        
        // 3. 创建文档
        GroupDocument document = new GroupDocument();
        document.setGroupId(groupId);
        document.setTopicId(request.getTopicId());
        document.setTitle(request.getTitle());
        document.setContent(request.getContent() != null ? request.getContent() : "");
        document.setVersion(1);
        document.setLastEditorId(userId);
        document.setLastEditTime(LocalDateTime.now());
        document.setCreatedTime(LocalDateTime.now());
        document.setUpdatedTime(LocalDateTime.now());
        
        documentMapper.insert(document);
        
        // 4. 保存初始版本到历史
        saveHistory(document.getId(), userId, document.getContent(), 1);
        
        log.info("创建协作文档成功, documentId={}", document.getId());
        return document.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDocument(Long groupId, Long documentId, UpdateDocumentRequest request, Long userId) {
        log.info("更新协作文档, groupId={}, documentId={}, userId={}", groupId, documentId, userId);
        
        // 1. 查询文档
        GroupDocument document = documentMapper.selectById(documentId);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }
        
        // 2. 验证文档属于该小组
        if (!document.getGroupId().equals(groupId)) {
            throw new BusinessException("文档不属于该小组");
        }
        
        // 3. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 4. 验证用户权限(小组成员或教师)
        permissionUtil.checkGroupMemberOrTeacher(userId, groupId, group.getCourseId());
        
        // 5. 版本控制(乐观锁)
        if (!document.getVersion().equals(request.getVersion())) {
            throw new BusinessException("文档已被其他用户修改,请刷新后重试");
        }
        
        // 6. 更新文档
        Integer newVersion = document.getVersion() + 1;
        GroupDocument updateDoc = new GroupDocument();
        updateDoc.setId(documentId);
        updateDoc.setContent(request.getContent());
        updateDoc.setVersion(newVersion);
        updateDoc.setLastEditorId(userId);
        updateDoc.setLastEditTime(LocalDateTime.now());
        updateDoc.setUpdatedTime(LocalDateTime.now());
        
        documentMapper.updateById(updateDoc);
        
        // 7. 保存历史版本
        saveHistory(documentId, userId, request.getContent(), newVersion);
        
        log.info("更新协作文档成功, documentId={}, newVersion={}", documentId, newVersion);
    }
    
    @Override
    public DocumentDetailResponse getDocument(Long groupId, Long documentId, Long userId) {
        log.info("获取协作文档, groupId={}, documentId={}, userId={}", groupId, documentId, userId);
        
        // 1. 查询文档
        GroupDocument document = documentMapper.selectById(documentId);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }
        
        // 2. 验证文档属于该小组
        if (!document.getGroupId().equals(groupId)) {
            throw new BusinessException("文档不属于该小组");
        }
        
        // 3. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 4. 验证用户权限(小组成员或教师)
        permissionUtil.checkGroupMemberOrTeacher(userId, groupId, group.getCourseId());
        
        // 5. 构建响应
        DocumentDetailResponse response = new DocumentDetailResponse();
        response.setDocumentId(document.getId());
        response.setGroupId(document.getGroupId());
        response.setTopicId(document.getTopicId());
        response.setTitle(document.getTitle());
        response.setContent(document.getContent());
        response.setVersion(document.getVersion());
        response.setLastEditorId(document.getLastEditorId());
        response.setLastEditTime(document.getLastEditTime());
        response.setCreatedTime(document.getCreatedTime());
        
        // 6. 获取最后编辑者信息
        if (document.getLastEditorId() != null) {
            try {
                Result<UserInfoDTO> result = userServiceClient.getUserById(document.getLastEditorId());
                if (result != null && result.getData() != null) {
                    response.setLastEditorName(result.getData().getRealName());
                } else {
                    response.setLastEditorName("未知用户");
                }
            } catch (Exception e) {
                log.warn("获取编辑者信息失败, userId={}", document.getLastEditorId(), e);
                response.setLastEditorName("未知用户");
            }
        }
        
        return response;
    }
    
    @Override
    public Page<DocumentHistoryResponse> getDocumentHistory(Long groupId, Long documentId, Integer pageNum, Integer pageSize, Long userId) {
        log.info("获取文档历史, groupId={}, documentId={}, pageNum={}, pageSize={}, userId={}", 
                groupId, documentId, pageNum, pageSize, userId);
        
        // 1. 查询文档
        GroupDocument document = documentMapper.selectById(documentId);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }
        
        // 2. 验证文档属于该小组
        if (!document.getGroupId().equals(groupId)) {
            throw new BusinessException("文档不属于该小组");
        }
        
        // 3. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 4. 验证用户权限(小组成员或教师)
        permissionUtil.checkGroupMemberOrTeacher(userId, groupId, group.getCourseId());
        
        // 5. 分页查询历史记录
        Page<GroupDocumentHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GroupDocumentHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupDocumentHistory::getDocumentId, documentId)
                    .orderByDesc(GroupDocumentHistory::getVersion);
        
        Page<GroupDocumentHistory> historyPage = historyMapper.selectPage(page, queryWrapper);
        
        // 6. 转换为响应对象
        Page<DocumentHistoryResponse> responsePage = new Page<>(historyPage.getCurrent(), historyPage.getSize(), historyPage.getTotal());
        List<DocumentHistoryResponse> responseList = convertToHistoryResponse(historyPage.getRecords());
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long groupId, Long documentId, Long userId) {
        log.info("删除协作文档, groupId={}, documentId={}, userId={}", groupId, documentId, userId);
        
        // 1. 查询文档
        GroupDocument document = documentMapper.selectById(documentId);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }
        
        // 2. 验证文档属于该小组
        if (!document.getGroupId().equals(groupId)) {
            throw new BusinessException("文档不属于该小组");
        }
        
        // 3. 查询小组
        CommunityGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException("小组不存在");
        }
        
        // 4. 验证用户权限(仅教师)
        CourseMemberDTO memberDTO = permissionUtil.checkCourseMember(userId, group.getCourseId());
        if (memberDTO.getMemberRole() != 1 && memberDTO.getMemberRole() != 2) {
            throw new BusinessException("只有教师可以删除文档");
        }
        
        // 5. 逻辑删除文档
        documentMapper.deleteById(documentId);
        
        log.info("删除协作文档成功, documentId={}", documentId);
    }
    
    /**
     * 保存历史版本
     */
    private void saveHistory(Long documentId, Long editorId, String content, Integer version) {
        GroupDocumentHistory history = new GroupDocumentHistory();
        history.setDocumentId(documentId);
        history.setEditorId(editorId);
        history.setContent(content);
        history.setVersion(version);
        history.setEditTime(LocalDateTime.now());
        
        historyMapper.insert(history);
    }
    
    /**
     * 转换为历史响应对象
     */
    private List<DocumentHistoryResponse> convertToHistoryResponse(List<GroupDocumentHistory> histories) {
        if (histories == null || histories.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有编辑者ID
        List<Long> editorIds = histories.stream()
                .map(GroupDocumentHistory::getEditorId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量获取用户信息
        Map<Long, UserInfoDTO> userMap = null;
        try {
            Result<Map<Long, UserInfoDTO>> result = userServiceClient.batchGetUserInfo(editorIds);
            if (result != null && result.getData() != null) {
                userMap = result.getData();
            }
        } catch (Exception e) {
            log.warn("批量获取用户信息失败", e);
        }
        
        // 转换
        List<DocumentHistoryResponse> responseList = new ArrayList<>();
        for (GroupDocumentHistory history : histories) {
            DocumentHistoryResponse response = new DocumentHistoryResponse();
            response.setHistoryId(history.getId());
            response.setDocumentId(history.getDocumentId());
            response.setEditorId(history.getEditorId());
            response.setContent(history.getContent());
            response.setVersion(history.getVersion());
            response.setEditTime(history.getEditTime());
            
            // 设置编辑者姓名
            if (userMap != null && userMap.containsKey(history.getEditorId())) {
                response.setEditorName(userMap.get(history.getEditorId()).getRealName());
            } else {
                response.setEditorName("未知用户");
            }
            
            responseList.add(response);
        }
        
        return responseList;
    }
}
