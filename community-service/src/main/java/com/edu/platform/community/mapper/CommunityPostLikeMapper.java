package com.edu.platform.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.community.entity.CommunityPostLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子点赞Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface CommunityPostLikeMapper extends BaseMapper<CommunityPostLike> {
}
