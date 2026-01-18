package com.edu.platform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.user.entity.UserRelRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface UserRelRoleMapper extends BaseMapper<UserRelRole> {
}
