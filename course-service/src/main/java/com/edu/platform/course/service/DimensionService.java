package com.edu.platform.course.service;

import com.edu.platform.course.dto.request.DimensionCreateRequest;
import com.edu.platform.course.dto.response.DimensionResponse;

import java.util.List;

/**
 * 能力维度管理服务
 */
public interface DimensionService {

    /**
     * 创建能力维度
     *
     * @param request 创建请求
     * @return 维度ID
     */
    Long createDimension(DimensionCreateRequest request);

    /**
     * 查询所有能力维度
     *
     * @return 维度列表
     */
    List<DimensionResponse> listDimensions();

    /**
     * 删除能力维度
     *
     * @param dimensionId 维度ID
     */
    void deleteDimension(Long dimensionId);

    /**
     * 更新能力维度
     *
     * @param dimensionId 维度ID
     * @param request 更新请求
     */
    void updateDimension(Long dimensionId, DimensionCreateRequest request);
}
