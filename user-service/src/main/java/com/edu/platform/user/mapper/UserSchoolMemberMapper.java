package com.edu.platform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.user.entity.UserSchoolMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校成员Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface UserSchoolMemberMapper extends BaseMapper<UserSchoolMember> {
}
