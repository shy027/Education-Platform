package com.edu.platform.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.common.result.ResultCode;
import com.edu.platform.course.dto.request.SubjectCategoryRequest;
import com.edu.platform.course.dto.response.SubjectCategoryResponse;
import com.edu.platform.course.entity.SubjectCategory;
import com.edu.platform.course.mapper.SubjectCategoryMapper;
import com.edu.platform.course.service.SubjectCategoryService;
import com.edu.platform.course.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学科领域分类服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectCategoryServiceImpl extends ServiceImpl<SubjectCategoryMapper, SubjectCategory> implements SubjectCategoryService {

    @Override
    public List<SubjectCategoryResponse> getAllEnabledSubjects() {
        List<SubjectCategory> list = this.list(new LambdaQueryWrapper<SubjectCategory>()
                .eq(SubjectCategory::getIsEnabled, 1)
                .orderByAsc(SubjectCategory::getSortOrder)
                .orderByDesc(SubjectCategory::getCreatedTime));
        
        return list.stream().map(item -> {
            SubjectCategoryResponse resp = new SubjectCategoryResponse();
            BeanUtil.copyProperties(item, resp);
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<SubjectCategoryResponse> pageSubjects(Integer pageNum, Integer pageSize, String keyword) {
        checkAdmin();

        Page<SubjectCategory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SubjectCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(keyword), SubjectCategory::getName, keyword)
                .orderByAsc(SubjectCategory::getSortOrder)
                .orderByDesc(SubjectCategory::getCreatedTime);

        Page<SubjectCategory> categoryPage = this.page(page, wrapper);

        List<SubjectCategoryResponse> list = categoryPage.getRecords().stream().map(item -> {
            SubjectCategoryResponse resp = new SubjectCategoryResponse();
            BeanUtil.copyProperties(item, resp);
            return resp;
        }).collect(Collectors.toList());

        Page<SubjectCategoryResponse> resultPage = new Page<>();
        BeanUtil.copyProperties(categoryPage, resultPage, "records");
        resultPage.setRecords(list);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSubject(SubjectCategoryRequest request) {
        checkAdmin();

        // 检查名称是否重复
        long count = this.count(new LambdaQueryWrapper<SubjectCategory>().eq(SubjectCategory::getName, request.getName()));
        if (count > 0) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "学科名称已存在");
        }

        SubjectCategory category = new SubjectCategory();
        category.setName(request.getName());
        category.setSortOrder(request.getSortOrder());
        category.setIsEnabled(1); // 默认启用

        this.save(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSubject(Long id, SubjectCategoryRequest request) {
        checkAdmin();

        SubjectCategory category = this.getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "学科分类不存在");
        }

        // 如果修改了名称，检查是否重复
        if (!category.getName().equals(request.getName())) {
            long count = this.count(new LambdaQueryWrapper<SubjectCategory>().eq(SubjectCategory::getName, request.getName()));
            if (count > 0) {
                throw new BusinessException(ResultCode.FAIL.getCode(), "学科名称已存在");
            }
        }

        category.setName(request.getName());
        category.setSortOrder(request.getSortOrder());
        category.setUpdatedTime(LocalDateTime.now());
        this.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSubject(Long id) {
        checkAdmin();
        
        SubjectCategory category = this.getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "学科分类不存在");
        }

        // 注意：实际项目中如果该学科分类下已有课程，应该提示无法删除或做软删除。这里简单处理为直接删除。
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer isEnabled) {
        checkAdmin();

        SubjectCategory category = this.getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "学科分类不存在");
        }

        category.setIsEnabled(isEnabled);
        category.setUpdatedTime(LocalDateTime.now());
        this.updateById(category);
    }

    private void checkAdmin() {
        if (!PermissionUtil.isAdminOrLeader()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅管理员或校领导可操作学科分类");
        }
    }
}
