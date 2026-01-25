package com.edu.platform.common.dto.request;

import lombok.Data;

/**
 * 分页请求基类
 */
@Data
public class PageRequest {

    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
