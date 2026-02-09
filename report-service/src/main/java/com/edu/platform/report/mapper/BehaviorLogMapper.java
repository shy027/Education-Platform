package com.edu.platform.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.report.entity.BehaviorLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习行为埋点Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface BehaviorLogMapper extends BaseMapper<BehaviorLog> {
    
}
