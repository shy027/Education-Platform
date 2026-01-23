package com.edu.platform.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.resource.entity.Resource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资源Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {
    
}
