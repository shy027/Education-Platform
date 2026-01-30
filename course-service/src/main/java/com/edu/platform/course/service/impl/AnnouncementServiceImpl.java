package com.edu.platform.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.common.utils.UserContext;
import com.edu.platform.course.dto.request.AnnouncementCreateRequest;
import com.edu.platform.course.dto.request.AnnouncementQueryRequest;
import com.edu.platform.course.dto.request.AnnouncementUpdateRequest;
import com.edu.platform.course.dto.response.AnnouncementResponse;
import com.edu.platform.course.entity.Course;
import com.edu.platform.course.entity.CourseAnnouncement;
import com.edu.platform.course.mapper.CourseAnnouncementMapper;
import com.edu.platform.course.service.AnnouncementService;
import com.edu.platform.course.service.CourseService;
import com.edu.platform.course.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<CourseAnnouncementMapper, CourseAnnouncement> implements AnnouncementService {

    private final CourseService courseService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAnnouncement(AnnouncementCreateRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        // 检查课程是否存在
        Course course = courseService.getById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "课程不存在");
        }
        
        // 权限校验：教师、助教、管理员可以发布公告
        if (!hasPublishPermission(request.getCourseId(), currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权发布公告");
        }
        
        CourseAnnouncement announcement = new CourseAnnouncement();
        BeanUtil.copyProperties(request, announcement);
        announcement.setPublisherId(currentUserId);
        announcement.setPublishTime(LocalDateTime.now());
        announcement.setViewCount(0);
        
        this.save(announcement);
        return announcement.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAnnouncement(AnnouncementUpdateRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        CourseAnnouncement announcement = this.getById(request.getId());
        if (announcement == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "公告不存在");
        }
        
        // 权限校验：只有发布者本人、校领导或管理员可以更新
        if (!announcement.getPublisherId().equals(currentUserId) && !PermissionUtil.isAdminOrLeader()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权修改此公告");
        }
        
        if (StrUtil.isNotBlank(request.getTitle())) {
            announcement.setTitle(request.getTitle());
        }
        if (StrUtil.isNotBlank(request.getContent())) {
            announcement.setContent(request.getContent());
        }
        if (request.getIsTop() != null) {
            announcement.setIsTop(request.getIsTop());
        }
        
        this.updateById(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnouncement(Long courseId, Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        CourseAnnouncement announcement = this.getById(id);
        if (announcement == null || !announcement.getCourseId().equals(courseId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "公告不存在");
        }
        
        // 权限校验：只有发布者本人、校领导或管理员可以删除
        if (!announcement.getPublisherId().equals(currentUserId) && !PermissionUtil.isAdminOrLeader()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除此公告");
        }
        
        this.removeById(id);
    }

    @Override
    public AnnouncementResponse getAnnouncementDetail(Long courseId, Long id) {
        CourseAnnouncement announcement = this.getById(id);
        if (announcement == null || !announcement.getCourseId().equals(courseId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "公告不存在");
        }
        
        // 增加浏览次数
        announcement.setViewCount(announcement.getViewCount() + 1);
        this.updateById(announcement);
        
        AnnouncementResponse response = new AnnouncementResponse();
        BeanUtil.copyProperties(announcement, response);
        
        // TODO: 远程调用获取发布者姓名
        response.setPublisherName("发布者");
        
        return response;
    }

    @Override
    public Page<AnnouncementResponse> pageAnnouncements(AnnouncementQueryRequest request) {
        Page<CourseAnnouncement> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<CourseAnnouncement> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(request.getCourseId() != null, CourseAnnouncement::getCourseId, request.getCourseId())
                .like(StrUtil.isNotBlank(request.getKeyword()), CourseAnnouncement::getTitle, request.getKeyword())
                .eq(request.getIsTop() != null, CourseAnnouncement::getIsTop, request.getIsTop())
                .orderByDesc(CourseAnnouncement::getIsTop)
                .orderByDesc(CourseAnnouncement::getPublishTime);
        
        Page<CourseAnnouncement> announcementPage = this.page(page, wrapper);
        
        List<AnnouncementResponse> list = announcementPage.getRecords().stream().map(a -> {
            AnnouncementResponse resp = new AnnouncementResponse();
            BeanUtil.copyProperties(a, resp);
            // TODO: 填充发布者姓名
            resp.setPublisherName("发布者");
            return resp;
        }).collect(Collectors.toList());
        
        Page<AnnouncementResponse> resultPage = new Page<>();
        BeanUtil.copyProperties(announcementPage, resultPage, "records");
        resultPage.setRecords(list);
        
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTop(Long courseId, Long id, Integer isTop) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        
        CourseAnnouncement announcement = this.getById(id);
        if (announcement == null || !announcement.getCourseId().equals(courseId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "公告不存在");
        }
        
        // 权限校验：只有教师或管理员可以置顶
        if (!hasManagePermission(courseId, currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权置顶公告");
        }
        
        announcement.setIsTop(isTop);
        this.updateById(announcement);
    }
    
    /**
     * 检查是否有发布权限（教师、助教、校领导、管理员）
     */
    private boolean hasPublishPermission(Long courseId, Long userId) {
        if (PermissionUtil.isAdminOrLeader()) {
            return true;
        }
        
        Course course = courseService.getById(courseId);
        if (course != null && course.getTeacherId().equals(userId)) {
            return true;
        }
        
        // TODO: 检查是否是助教
        
        return false;
    }
    
    /**
     * 检查是否有管理权限（教师、校领导、管理员）
     */
    private boolean hasManagePermission(Long courseId, Long userId) {
        if (PermissionUtil.isAdminOrLeader()) {
            return true;
        }
        
        Course course = courseService.getById(courseId);
        return course != null && course.getTeacherId().equals(userId);
    }
}
