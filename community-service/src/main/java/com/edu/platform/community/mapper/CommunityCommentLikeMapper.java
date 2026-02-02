package com.edu.platform.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.community.entity.CommunityCommentLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论点赞Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface CommunityCommentLikeMapper extends BaseMapper<CommunityCommentLike> {
}
