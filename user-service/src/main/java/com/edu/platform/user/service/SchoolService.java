package com.edu.platform.user.service;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.user.dto.request.JoinSchoolRequest;
import com.edu.platform.user.dto.response.SchoolResponse;

/**
 * 学校服务接口
 *
 * @author Education Platform
 */
public interface SchoolService {
    
    /**
     * 加入学校
     *
     * @param userId 用户ID
     * @param schoolId 学校ID
     * @param request 加入请求
     */
    void joinSchool(Long userId, Long schoolId, JoinSchoolRequest request);
    
    /**
     * 获取学校列表
     *
     * @param keyword 搜索关键词
     * @param province 省份
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 学校列表
     */
    PageResult<SchoolResponse> getSchoolList(String keyword, String province, Integer pageNum, Integer pageSize);
    
    /**
     * 获取学校详情
     *
     * @param schoolId 学校ID
     * @return 学校详情
     */
    SchoolResponse getSchoolDetail(Long schoolId);
    
}
