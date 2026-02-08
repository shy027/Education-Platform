package com.edu.platform.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档编辑历史实体类
 */
@Data
@TableName("group_document_history")
public class GroupDocumentHistory {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 文档ID
     */
    private Long documentId;
    
    /**
     * 编辑者ID
     */
    private Long editorId;
    
    /**
     * 内容快照
     */
    private String content;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 编辑时间
     */
    private LocalDateTime editTime;
}
