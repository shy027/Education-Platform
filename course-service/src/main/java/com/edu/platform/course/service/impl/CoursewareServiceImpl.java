package com.edu.platform.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.course.util.PermissionUtil;
import com.edu.platform.course.dto.internal.CoursewareInfoDTO;
import com.edu.platform.course.dto.request.CoursewareQueryRequest;
import com.edu.platform.course.dto.request.CoursewareUpdateRequest;
import com.edu.platform.course.dto.request.CoursewareUploadRequest;
import com.edu.platform.course.dto.response.CoursewareDetailResponse;
import com.edu.platform.course.dto.response.CoursewareResponse;
import com.edu.platform.course.entity.Course;
import com.edu.platform.course.entity.CourseChapter;
import com.edu.platform.course.entity.CourseCourseware;
import com.edu.platform.course.entity.CoursewareProgress;
import com.edu.platform.course.mapper.CourseChapterMapper;
import com.edu.platform.course.mapper.CourseCoursewareMapper;
import com.edu.platform.course.mapper.CourseMapper;
import com.edu.platform.course.mapper.CoursewareProgressMapper;
import com.edu.platform.course.service.CoursewareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课件服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoursewareServiceImpl implements CoursewareService {
    
    private final CourseCoursewareMapper coursewareMapper;
    private final CoursewareProgressMapper progressMapper;
    private final CourseMapper courseMapper;
    private final CourseChapterMapper chapterMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadCourseware(Long courseId, CoursewareUploadRequest request, Long userId) {
        // 验证课程存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "课程不存在");
        }
        
        // 验证章节存在且属于该课程
        CourseChapter chapter = chapterMapper.selectById(request.getChapterId());
        if (chapter == null || !chapter.getCourseId().equals(courseId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "章节不存在或不属于该课程");
        }
        
        // 验证权限：必须是教师或以上角色
        PermissionUtil.requireTeacherOrAbove();
        
        // 创建课件
        CourseCourseware courseware = new CourseCourseware();
        BeanUtil.copyProperties(request, courseware);
        courseware.setCourseId(courseId);
        courseware.setCreatorId(userId);
        courseware.setViewCount(0);
        courseware.setDownloadCount(0);
        courseware.setAuditStatus(0); // 待审核
        courseware.setStatus(1); // 启用
        
        coursewareMapper.insert(courseware);
        
        log.info("课件上传成功: wareId={}, courseId={}, userId={}", 
                courseware.getId(), courseId, userId);
        
        return courseware.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseware(CoursewareUpdateRequest request, Long userId) {
        // 查询课件
        CourseCourseware courseware = coursewareMapper.selectById(request.getId());
        if (courseware == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "课件不存在");
        }
        
        // 验证权限：创建者或管理员
        if (!courseware.getCreatorId().equals(userId) && 
            !PermissionUtil.isAdminOrLeader()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限修改此课件");
        }
        
        // 更新课件信息
        CourseCourseware updateEntity = new CourseCourseware();
        updateEntity.setId(request.getId());
        if (request.getWareTitle() != null) {
            updateEntity.setWareTitle(request.getWareTitle());
        }
        if (request.getCoverUrl() != null) {
            updateEntity.setCoverUrl(request.getCoverUrl());
        }
        if (request.getDescription() != null) {
            updateEntity.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            updateEntity.setSortOrder(request.getSortOrder());
        }
        if (request.getAllowDownload() != null) {
            updateEntity.setAllowDownload(request.getAllowDownload());
        }
        
        coursewareMapper.updateById(updateEntity);
        
        log.info("课件更新成功: wareId={}, userId={}", request.getId(), userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseware(Long wareId, Long userId) {
        // 查询课件
        CourseCourseware courseware = coursewareMapper.selectById(wareId);
        if (courseware == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "课件不存在");
        }
        
        // 验证权限：创建者或管理员
        if (!courseware.getCreatorId().equals(userId) && 
            !PermissionUtil.isAdminOrLeader()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限删除此课件");
        }
        
        // 软删除
        coursewareMapper.deleteById(wareId);
        
        log.info("课件删除成功: wareId={}, userId={}", wareId, userId);
    }
    
    @Override
    public Page<CoursewareResponse> getCoursewareList(Long courseId, CoursewareQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<CourseCourseware> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCourseware::getCourseId, courseId)
               .eq(request.getChapterId() != null, CourseCourseware::getChapterId, request.getChapterId())
               .eq(request.getWareType() != null, CourseCourseware::getWareType, request.getWareType())
               .eq(request.getAuditStatus() != null, CourseCourseware::getAuditStatus, request.getAuditStatus())
               .orderByAsc(CourseCourseware::getSortOrder)
               .orderByDesc(CourseCourseware::getCreatedTime);
        
        // 分页查询
        Page<CourseCourseware> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<CourseCourseware> coursewarePage = coursewareMapper.selectPage(page, wrapper);
        
        // 转换为响应对象
        List<CoursewareResponse> responseList = coursewarePage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        // 构建分页响应
        Page<CoursewareResponse> responsePage = new Page<>(
                coursewarePage.getCurrent(),
                coursewarePage.getSize(),
                coursewarePage.getTotal()
        );
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoursewareDetailResponse getCoursewareDetail(Long wareId, Long userId) {
        // 查询课件
        CourseCourseware courseware = coursewareMapper.selectById(wareId);
        if (courseware == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "课件不存在");
        }
        
        // 增加观看次数
        CourseCourseware updateEntity = new CourseCourseware();
        updateEntity.setId(wareId);
        updateEntity.setViewCount(courseware.getViewCount() + 1);
        coursewareMapper.updateById(updateEntity);
        
        // 转换为详情响应
        CoursewareDetailResponse response = new CoursewareDetailResponse();
        BeanUtil.copyProperties(courseware, response);
        
        // 查询章节名称
        CourseChapter chapter = chapterMapper.selectById(courseware.getChapterId());
        if (chapter != null) {
            response.setChapterName(chapter.getChapterName());
        }
        
        // TODO: 通过OpenFeign获取创建者和审核人姓名
        
        // 查询学习进度
        if (userId != null) {
            LambdaQueryWrapper<CoursewareProgress> progressWrapper = new LambdaQueryWrapper<>();
            progressWrapper.eq(CoursewareProgress::getWareId, wareId)
                          .eq(CoursewareProgress::getUserId, userId);
            CoursewareProgress progress = progressMapper.selectOne(progressWrapper);
            
            if (progress != null) {
                response.setProgressSeconds(progress.getProgressSeconds());
                response.setCompleted(progress.getCompleted());
                
                // 计算进度百分比
                if (courseware.getDuration() != null && courseware.getDuration() > 0) {
                    int percent = (int) ((progress.getProgressSeconds() * 100.0) / courseware.getDuration());
                    response.setProgressPercent(Math.min(percent, 100));
                }
            }
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditCourseware(Long wareId, Integer auditStatus, Long auditorId) {
        // 验证权限：管理员或校领导
        PermissionUtil.requireAdminOrLeader();
        
        // 查询课件
        CourseCourseware courseware = coursewareMapper.selectById(wareId);
        if (courseware == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "课件不存在");
        }
        
        // 更新审核状态
        CourseCourseware updateEntity = new CourseCourseware();
        updateEntity.setId(wareId);
        updateEntity.setAuditStatus(auditStatus);
        updateEntity.setAuditTime(LocalDateTime.now());
        updateEntity.setAuditorId(auditorId);
        
        coursewareMapper.updateById(updateEntity);
        
        log.info("课件审核完成: wareId={}, auditStatus={}, auditorId={}", 
                wareId, auditStatus, auditorId);
    }
    
    /**
     * 转换为响应对象
     */
    private CoursewareResponse convertToResponse(CourseCourseware courseware) {
        CoursewareResponse response = new CoursewareResponse();
        BeanUtil.copyProperties(courseware, response);
        
        // 查询章节名称
        CourseChapter chapter = chapterMapper.selectById(courseware.getChapterId());
        if (chapter != null) {
            response.setChapterName(chapter.getChapterName());
        }
        
        // TODO: 通过OpenFeign获取创建者和审核人姓名
        
        return response;
    }
    
    @Override
    public void updateAuditStatus(Long coursewareId, Integer auditStatus) {
        CourseCourseware courseware = coursewareMapper.selectById(coursewareId);
        if (courseware == null) {
            throw new BusinessException("课件不存在");
        }
        
        CourseCourseware updateEntity = new CourseCourseware();
        updateEntity.setId(coursewareId);
        updateEntity.setAuditStatus(auditStatus);
        updateEntity.setAuditTime(LocalDateTime.now());
        
        coursewareMapper.updateById(updateEntity);
        
        log.info("更新课件审核状态: coursewareId={}, auditStatus={}", coursewareId, auditStatus);
    }
    
    @Override
    public CoursewareInfoDTO getCoursewareInfo(Long coursewareId) {
        CourseCourseware courseware = coursewareMapper.selectById(coursewareId);
        if (courseware == null) {
            throw new BusinessException("课件不存在");
        }
        
        CoursewareInfoDTO dto = new CoursewareInfoDTO();
        dto.setId(courseware.getId());
        dto.setTitle(courseware.getWareTitle());
        dto.setDescription(courseware.getDescription());
        dto.setCreatorId(courseware.getCreatorId());
        
        // TODO: 通过OpenFeign获取创建者姓名
        dto.setCreatorName("用户" + courseware.getCreatorId());
        
        return dto;
    }
}
