package com.edu.platform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.user.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {
}
