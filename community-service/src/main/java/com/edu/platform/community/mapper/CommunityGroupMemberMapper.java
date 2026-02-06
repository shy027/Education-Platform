package com.edu.platform.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.community.entity.CommunityGroupMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小组成员Mapper接口
 *
 * @author Education Platform
 */
@Mapper
public interface CommunityGroupMemberMapper extends BaseMapper<CommunityGroupMember> {
}
