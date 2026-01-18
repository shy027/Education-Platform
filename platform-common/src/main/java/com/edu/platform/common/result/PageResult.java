package com.edu.platform.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 *
 * @author Education Platform
 */
@Data
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    public PageResult() {
    }
    
    public PageResult(Long total, List<T> list) {
        this.total = total;
        this.list = list;
    }
    
    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(Long total, List<T> list) {
        return new PageResult<>(total, list);
    }
    
}
