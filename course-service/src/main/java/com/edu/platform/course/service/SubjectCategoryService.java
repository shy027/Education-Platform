package com.edu.platform.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.platform.course.dto.request.SubjectCategoryRequest;
import com.edu.platform.course.dto.response.SubjectCategoryResponse;
import com.edu.platform.course.entity.SubjectCategory;

import java.util.List;

/**
 * 学科领域分类服务接口
 */
public interface SubjectCategoryService extends IService<SubjectCategory> {

    /**
     * 获取所有已启用的学科分类（所有人可用）
     */
    List<SubjectCategoryResponse> getAllEnabledSubjects();

    /**
     * 分页获取所有学科分类（管理员用）
     */
    Page<SubjectCategoryResponse> pageSubjects(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 新增学科分类
     */
    void createSubject(SubjectCategoryRequest request);

    /**
     * 更新学科分类
     */
    void updateSubject(Long id, SubjectCategoryRequest request);

    /**
     * 删除学科分类
     */
    void deleteSubject(Long id);

    /**
     * 修改启用状态
     */
    void updateStatus(Long id, Integer isEnabled);
}
