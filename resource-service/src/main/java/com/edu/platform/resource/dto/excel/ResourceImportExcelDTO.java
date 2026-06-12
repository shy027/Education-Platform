package com.edu.platform.resource.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 资源导入 Excel DTO
 *
 * @author Education Platform
 */
@Data
public class ResourceImportExcelDTO {

    @ExcelProperty("*资源标题")
    @ColumnWidth(30)
    private String title;

    @ExcelProperty("*资源类型 (动画/视频/文档/音频/挂图)")
    @ColumnWidth(25)
    private String resourceTypeStr;

    @ExcelProperty("所属分类 (选填)")
    @ColumnWidth(20)
    private String categoryName;

    @ExcelProperty("*标签 (多个用英文逗号,分隔，必须为系统配置的思政标签)")
    @ColumnWidth(40)
    private String tagNames;

    @ExcelProperty("简介")
    @ColumnWidth(40)
    private String summary;

    @ExcelProperty("封面链接 (URL或服务器相对路径)")
    @ColumnWidth(40)
    private String coverUrl;

    @ExcelProperty("*资源链接 (必填，URL或服务器相对路径)")
    @ColumnWidth(40)
    private String fileUrl;
}
