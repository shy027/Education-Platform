package com.edu.platform.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.community.entity.CommunityComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论Mapper
 *
 * @author Education Platform
 */
@Mapper
public interface CommunityCommentMapper extends BaseMapper<CommunityComment> {
}
