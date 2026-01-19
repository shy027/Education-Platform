package com.edu.platform.user.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 用户导入Excel模型
 *
 * @author Education Platform
 */
@Data
public class UserImportExcel {
    
    @ExcelProperty(value = "用户名*", index = 0)
    private String username;
    
    @ExcelProperty(value = "密码", index = 1)
    private String password;
    
    @ExcelProperty(value = "真实姓名*", index = 2)
    private String realName;
    
    @ExcelProperty(value = "手机号*", index = 3)
    private String phone;
    
    @ExcelProperty(value = "邮箱", index = 4)
    private String email;
    
    @ExcelProperty(value = "性别", index = 5)
    private String gender;
    
    @ExcelProperty(value = "角色*", index = 6)
    private String roleCode;
    
    @ExcelProperty(value = "学号/工号", index = 7)
    private String studentNo;
    
    @ExcelProperty(value = "学校ID", index = 8)
    private String schoolId;
    
    @ExcelProperty(value = "院系", index = 9)
    private String department;
    
    @ExcelProperty(value = "专业", index = 10)
    private String major;
    
}
