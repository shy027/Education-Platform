package com.edu.platform.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.audit.entity.AuditRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核记录Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {
}
