package com.edu.platform.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.course.context.UserContext;
import com.edu.platform.course.dto.request.ChapterCreateRequest;
import com.edu.platform.course.dto.request.ChapterUpdateRequest;
import com.edu.platform.course.dto.response.ChapterTreeResponse;
import com.edu.platform.course.entity.Course;
import com.edu.platform.course.entity.CourseChapter;
import com.edu.platform.course.mapper.CourseChapterMapper;
import com.edu.platform.course.service.ChapterService;
import com.edu.platform.course.service.CourseService;
import com.edu.platform.course.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 章节服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterServiceImpl extends ServiceImpl<CourseChapterMapper, CourseChapter> implements ChapterService {

    private final CourseService courseService;

    @Override
    public List<ChapterTreeResponse> getChapterTree(Long courseId) {
        // 1. 查询所有章节
        List<CourseChapter> chapters = this.list(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId)
                .orderByAsc(CourseChapter::getSortOrder));

        if (CollUtil.isEmpty(chapters)) {
            return new ArrayList<>();
        }

        // 2. 转换为 Response 对象
        List<ChapterTreeResponse> allNodes = chapters.stream().map(c -> {
            ChapterTreeResponse resp = new ChapterTreeResponse();
            BeanUtil.copyProperties(c, resp);
            return resp;
        }).collect(Collectors.toList());

        // 3. 构建树
        // 3.1 找到所有根节点 (parentId = 0)
        List<ChapterTreeResponse> rootNodes = allNodes.stream()
                .filter(node -> node.getParentId() == 0)
                .sorted(Comparator.comparingInt(ChapterTreeResponse::getSortOrder))
                .collect(Collectors.toList());

        // 3.2 递归/遍历查找子节点
        Map<Long, List<ChapterTreeResponse>> childrenMap = allNodes.stream()
                .filter(node -> node.getParentId() != 0)
                .collect(Collectors.groupingBy(ChapterTreeResponse::getParentId));

        for (ChapterTreeResponse root : rootNodes) {
            fillChildren(root, childrenMap);
        }

        return rootNodes;
    }

    private void fillChildren(ChapterTreeResponse parent, Map<Long, List<ChapterTreeResponse>> childrenMap) {
        List<ChapterTreeResponse> children = childrenMap.get(parent.getId());
        if (CollUtil.isNotEmpty(children)) {
            children.sort(Comparator.comparingInt(ChapterTreeResponse::getSortOrder));
            parent.setChildren(children);
            for (ChapterTreeResponse child : children) {
                fillChildren(child, childrenMap);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChapter(ChapterCreateRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        // 权限校验：只有课程教师或管理员可以创建章节
        if (!hasManagePermission(request.getCourseId(), currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权创建章节");
        }
        
        // 检查课程是否存在
        Course course = courseService.getById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "课程不存在");
        }
        
        CourseChapter chapter = new CourseChapter();
        BeanUtil.copyProperties(request, chapter);
        
        // 默认状态发布
        chapter.setStatus(1);
        
        this.save(chapter);
        return chapter.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChapter(ChapterUpdateRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        CourseChapter chapter = this.getById(request.getId());
        if (chapter == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "章节不存在");
        }
        
        // 权限校验：只有课程教师或管理员可以更新章节
        if (!hasManagePermission(chapter.getCourseId(), currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权修改章节");
        }
        
        BeanUtil.copyProperties(request, chapter);
        this.updateById(chapter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        CourseChapter chapter = this.getById(id);
        if (chapter == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "章节不存在");
        }
        
        // 权限校验：只有课程教师或管理员可以删除章节
        if (!hasManagePermission(chapter.getCourseId(), currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除章节");
        }
        
        // 检查是否有子章节
        long count = this.count(new LambdaQueryWrapper<CourseChapter>().eq(CourseChapter::getParentId, id));
        if (count > 0) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "该章节下存在子章节,无法删除");
        }
        
        // TODO: 检查是否有课件关联 (CoursewareService)
        
        this.removeById(id);
    }
    
    /**
     * 检查是否有管理权限（课程教师、校领导或管理员）
     */
    private boolean hasManagePermission(Long courseId, Long userId) {
        // 管理员或校领导有权限
        if (PermissionUtil.isAdminOrLeader()) {
            return true;
        }
        
        // 检查是否是课程教师
        Course course = courseService.getById(courseId);
        return course != null && course.getTeacherId().equals(userId);
    }
}
