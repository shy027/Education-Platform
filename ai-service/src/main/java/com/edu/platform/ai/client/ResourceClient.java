package com.edu.platform.ai.client;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "resource-service")
public interface ResourceClient {

    @GetMapping("/api/v1/resources")
    Result<PageResult<ResourceDTO>> pageResources(
            @RequestParam(value = "status", defaultValue = "2") Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "50") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId);

    @Data
    class ResourceDTO {
        private Long id;
        private String title;
        private String summary;
        private List<TagInfo> tags;
        
        @Data
        public static class TagInfo {
            private String tagName;
        }
    }
}
