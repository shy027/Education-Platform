package com.edu.platform.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.platform.community.entity.GroupChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小组聊天消息Mapper接口
 */
@Mapper
public interface GroupChatMessageMapper extends BaseMapper<GroupChatMessage> {
}
