package com.edu.platform.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.community.entity.CommunityPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {
}
