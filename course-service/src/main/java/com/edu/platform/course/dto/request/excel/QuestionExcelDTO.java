package com.edu.platform.course.dto.request.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class QuestionExcelDTO {

    @ExcelProperty(value = "题型（必填：单选题、多选题、判断题、填空题、简答题）", index = 0)
    private String questionType;

    @ExcelProperty(value = "难度（选填：简单、中等、困难、很难、极难，默认简单）", index = 1)
    private String difficulty;

    @ExcelProperty(value = "题目内容（必填）", index = 2)
    private String content;

    @ExcelProperty(value = "选项A（选择题填写）", index = 3)
    private String optionA;

    @ExcelProperty(value = "选项B（选择题填写）", index = 4)
    private String optionB;

    @ExcelProperty(value = "选项C（选择题填写）", index = 5)
    private String optionC;

    @ExcelProperty(value = "选项D（选择题填写）", index = 6)
    private String optionD;

    @ExcelProperty(value = "选项E（选择题填写）", index = 7)
    private String optionE;

    @ExcelProperty(value = "选项F（选择题填写）", index = 8)
    private String optionF;

    @ExcelProperty(value = "标准答案（必填：单选填大写字母如A，多选填ABC，判断填对/错，填空和简答直接填答案文字）", index = 9)
    private String answer;

    @ExcelProperty(value = "题目解析（选填）", index = 10)
    private String analysis;

    @ExcelProperty(value = "所属学科（管理员导入必填，教师导入留空或系统自动推断）", index = 11)
    private String categoryName;

    @ExcelProperty(value = "素养维度（以逗号分隔，如填 1,3 或是包含文字。选填，未识别系统自动使用默认值）", index = 12)
    private String dimensions;
}
