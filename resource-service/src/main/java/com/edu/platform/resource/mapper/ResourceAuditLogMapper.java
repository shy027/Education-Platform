package com.edu.platform.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.resource.entity.ResourceAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资源审核记录Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface ResourceAuditLogMapper extends BaseMapper<ResourceAuditLog> {
    
}
