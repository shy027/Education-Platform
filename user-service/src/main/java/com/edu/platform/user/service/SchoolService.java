package com.edu.platform.user.service;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.user.dto.request.JoinSchoolRequest;
import com.edu.platform.user.dto.request.SchoolRequest;
import com.edu.platform.user.dto.response.SchoolResponse;

/**
 * 学校服务接口
 *
 * @author Education Platform
 */
public interface SchoolService {
    
    /**
     * 加入学校
     */
    void joinSchool(Long userId, Long schoolId, JoinSchoolRequest request);
    
    /**
     * 获取学校列表
     */
    PageResult<SchoolResponse> getSchoolList(String keyword, String province, Integer pageNum, Integer pageSize);
    
    /**
     * 获取学校详情
     */
    SchoolResponse getSchoolDetail(Long schoolId);

    /**
     * 获取总学校数量
     */
    Long getSchoolCount();

    /**
     * 创建学校
     */
    void createSchool(SchoolRequest request);

    /**
     * 更新学校
     */
    void updateSchool(Long id, SchoolRequest request);

    /**
     * 删除学校
     */
    void deleteSchool(Long id);
}
