package com.edu.platform.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.resource.entity.ResourceTagRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资源标签关联Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface ResourceTagRelationMapper extends BaseMapper<ResourceTagRelation> {
    
}
