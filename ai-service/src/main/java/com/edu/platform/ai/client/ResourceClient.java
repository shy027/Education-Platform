package com.edu.platform.ai.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.platform.common.result.Result;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "resource-service")
public interface ResourceClient {

    @GetMapping("/api/v1/resources")
    Result<Page<ResourceDTO>> pageResources(
            @RequestParam(value = "status", defaultValue = "2") Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "50") Integer pageSize);

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
