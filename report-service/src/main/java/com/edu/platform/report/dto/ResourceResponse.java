package com.edu.platform.report.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResourceResponse {
    private Long id;
    private List<TagInfo> tags;

    @Data
    public static class TagInfo {
        private Long id;
        private String tagName;
        private String tagColor;
    }
}
