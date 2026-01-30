package com.edu.platform.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.course.dto.request.DimensionCreateRequest;
import com.edu.platform.course.dto.response.DimensionResponse;
import com.edu.platform.course.entity.ExamDimension;
import com.edu.platform.course.mapper.ExamDimensionMapper;
import com.edu.platform.course.service.DimensionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 能力维度管理服务实现
 * 维度为全局配置,由管理员统一管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DimensionServiceImpl implements DimensionService {

    private final ExamDimensionMapper dimensionMapper;

    @Override
    public Long createDimension(DimensionCreateRequest request) {
        // 检查同名维度
        Long count = dimensionMapper.selectCount(new LambdaQueryWrapper<ExamDimension>()
                .eq(ExamDimension::getName, request.getName())
                .eq(ExamDimension::getIsDeleted, 0)
        );

        if (count > 0) {
            throw new BusinessException("已存在同名维度");
        }

        ExamDimension dimension = new ExamDimension();
        dimension.setName(request.getName());
        dimension.setDescription(request.getDescription());
        dimension.setIsDeleted(0);

        dimensionMapper.insert(dimension);
        log.info("创建能力维度成功, dimensionId={}, name={}", dimension.getId(), request.getName());

        return dimension.getId();
    }

    @Override
    public List<DimensionResponse> listDimensions() {
        List<ExamDimension> dimensions = dimensionMapper.selectList(
                new LambdaQueryWrapper<ExamDimension>()
                        .eq(ExamDimension::getIsDeleted, 0)
                        .orderByAsc(ExamDimension::getCreatedTime)
        );

        return dimensions.stream()
                .map(this::buildDimensionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDimension(Long dimensionId) {
        ExamDimension dimension = dimensionMapper.selectById(dimensionId);
        if (dimension == null || dimension.getIsDeleted() == 1) {
            throw new BusinessException("维度不存在");
        }

        dimension.setIsDeleted(1);
        dimensionMapper.updateById(dimension);

        log.info("删除能力维度成功, dimensionId={}", dimensionId);
    }

    @Override
    public void updateDimension(Long dimensionId, DimensionCreateRequest request) {
        ExamDimension dimension = dimensionMapper.selectById(dimensionId);
        if (dimension == null || dimension.getIsDeleted() == 1) {
            throw new BusinessException("维度不存在");
        }

        // 检查同名维度(排除自己)
        Long count = dimensionMapper.selectCount(new LambdaQueryWrapper<ExamDimension>()
                .eq(ExamDimension::getName, request.getName())
                .ne(ExamDimension::getId, dimensionId)
                .eq(ExamDimension::getIsDeleted, 0)
        );

        if (count > 0) {
            throw new BusinessException("已存在同名维度");
        }

        if (StringUtils.hasText(request.getName())) {
            dimension.setName(request.getName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            dimension.setDescription(request.getDescription());
        }

        dimensionMapper.updateById(dimension);
        log.info("更新能力维度成功, dimensionId={}, name={}", dimensionId, request.getName());
    }

    private DimensionResponse buildDimensionResponse(ExamDimension dimension) {
        DimensionResponse response = new DimensionResponse();
        response.setId(dimension.getId());
        response.setName(dimension.getName());
        response.setDescription(dimension.getDescription());
        response.setCreatedTime(dimension.getCreatedTime());
        return response;
    }
}
