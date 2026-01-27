package com.edu.platform.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.course.dto.request.ProgressRecordRequest;
import com.edu.platform.course.dto.response.StudentProgressResponse;
import com.edu.platform.course.entity.CourseCourseware;
import com.edu.platform.course.entity.CoursewareProgress;
import com.edu.platform.course.mapper.CourseCoursewareMapper;
import com.edu.platform.course.mapper.CoursewareProgressMapper;
import com.edu.platform.course.service.CourseMemberService;
import com.edu.platform.course.service.CoursewareProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习进度服务实现
 *
 * @author Education Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoursewareProgressServiceImpl implements CoursewareProgressService {
    
    private final CoursewareProgressMapper progressMapper;
    private final CourseCoursewareMapper coursewareMapper;
    private final CourseMemberService memberService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordProgress(Long wareId, ProgressRecordRequest request, Long userId) {
        // 查询课件
        CourseCourseware courseware = coursewareMapper.selectById(wareId);
        if (courseware == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "课件不存在");
        }
        
        // 验证用户是否为课程成员
        if (!memberService.isCourseMember(courseware.getCourseId(), userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "您不是该课程成员");
        }
        
        // 查询是否已有进度记录
        LambdaQueryWrapper<CoursewareProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoursewareProgress::getWareId, wareId)
               .eq(CoursewareProgress::getUserId, userId);
        CoursewareProgress existingProgress = progressMapper.selectOne(wrapper);
        
        if (existingProgress != null) {
            // 更新进度
            CoursewareProgress updateEntity = new CoursewareProgress();
            updateEntity.setId(existingProgress.getId());
            updateEntity.setProgressSeconds(request.getProgressSeconds());
            updateEntity.setCompleted(request.getCompleted());
            updateEntity.setLastViewTime(LocalDateTime.now());
            
            progressMapper.updateById(updateEntity);
            
            log.info("学习进度更新: wareId={}, userId={}, progress={}", 
                    wareId, userId, request.getProgressSeconds());
        } else {
            // 创建新进度记录
            CoursewareProgress progress = new CoursewareProgress();
            progress.setWareId(wareId);
            progress.setUserId(userId);
            progress.setProgressSeconds(request.getProgressSeconds());
            progress.setCompleted(request.getCompleted());
            progress.setLastViewTime(LocalDateTime.now());
            
            progressMapper.insert(progress);
            
            log.info("学习进度创建: wareId={}, userId={}, progress={}", 
                    wareId, userId, request.getProgressSeconds());
        }
    }
    
    @Override
    public CoursewareProgress getProgress(Long wareId, Long userId) {
        LambdaQueryWrapper<CoursewareProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoursewareProgress::getWareId, wareId)
               .eq(CoursewareProgress::getUserId, userId);
        return progressMapper.selectOne(wrapper);
    }
    
    @Override
    public Page<StudentProgressResponse> getCoursewareProgress(Long wareId, Integer pageNum, Integer pageSize) {
        // 查询课件信息
        CourseCourseware courseware = coursewareMapper.selectById(wareId);
        if (courseware == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "课件不存在");
        }
        
        // 分页查询该课件的所有学习进度
        LambdaQueryWrapper<CoursewareProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoursewareProgress::getWareId, wareId)
               .orderByDesc(CoursewareProgress::getLastViewTime);
        
        Page<CoursewareProgress> progressPage = new Page<>(pageNum, pageSize);
        progressPage = progressMapper.selectPage(progressPage, wrapper);
        
        // 转换为响应对象
        List<StudentProgressResponse> responseList = progressPage.getRecords().stream()
                .map(progress -> convertToStudentProgress(progress, courseware))
                .collect(Collectors.toList());
        
        // 构建分页响应
        Page<StudentProgressResponse> responsePage = new Page<>(
                progressPage.getCurrent(),
                progressPage.getSize(),
                progressPage.getTotal()
        );
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    @Override
    public Page<StudentProgressResponse> getCourseProgress(Long courseId, Long userId, Integer pageNum, Integer pageSize) {
        // 查询该用户在该课程下所有课件的学习进度
        // 首先获取课程下的所有课件
        LambdaQueryWrapper<CourseCourseware> coursewareWrapper = new LambdaQueryWrapper<>();
        coursewareWrapper.eq(CourseCourseware::getCourseId, courseId);
        List<CourseCourseware> coursewares = coursewareMapper.selectList(coursewareWrapper);
        
        if (coursewares.isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }
        
        // 获取所有课件ID
        List<Long> wareIds = coursewares.stream()
                .map(CourseCourseware::getId)
                .collect(Collectors.toList());
        
        // 查询该用户对这些课件的学习进度
        LambdaQueryWrapper<CoursewareProgress> progressWrapper = new LambdaQueryWrapper<>();
        progressWrapper.in(CoursewareProgress::getWareId, wareIds)
                      .eq(CoursewareProgress::getUserId, userId)
                      .orderByDesc(CoursewareProgress::getLastViewTime);
        
        Page<CoursewareProgress> progressPage = new Page<>(pageNum, pageSize);
        progressPage = progressMapper.selectPage(progressPage, progressWrapper);
        
        // 转换为响应对象
        List<StudentProgressResponse> responseList = progressPage.getRecords().stream()
                .map(progress -> {
                    CourseCourseware courseware = coursewares.stream()
                            .filter(w -> w.getId().equals(progress.getWareId()))
                            .findFirst()
                            .orElse(null);
                    return convertToStudentProgress(progress, courseware);
                })
                .collect(Collectors.toList());
        
        // 构建分页响应
        Page<StudentProgressResponse> responsePage = new Page<>(
                progressPage.getCurrent(),
                progressPage.getSize(),
                progressPage.getTotal()
        );
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    /**
     * 转换为学生进度响应
     */
    private StudentProgressResponse convertToStudentProgress(CoursewareProgress progress, CourseCourseware courseware) {
        StudentProgressResponse response = new StudentProgressResponse();
        BeanUtil.copyProperties(progress, response);
        
        if (courseware != null) {
            response.setWareTitle(courseware.getWareTitle());
            response.setDuration(courseware.getDuration());
            
            // 计算进度百分比
            if (courseware.getDuration() != null && courseware.getDuration() > 0) {
                int percent = (int) ((progress.getProgressSeconds() * 100.0) / courseware.getDuration());
                response.setProgressPercent(Math.min(percent, 100));
            }
        }
        
        // TODO: 通过OpenFeign获取用户姓名和学号
        // response.setUserName(userName);
        // response.setStudentNumber(studentNumber);
        
        return response;
    }
}
