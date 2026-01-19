package com.edu.platform.user.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import java.util.Date;

/**
 * 用户导出Excel模型
 *
 * @author Education Platform
 */
@Data
public class UserExportExcel {
    
    @ExcelProperty(value = "用户ID", index = 0)
    private Long id;
    
    @ExcelProperty(value = "用户名", index = 1)
    private String username;
    
    @ExcelProperty(value = "真实姓名", index = 2)
    private String realName;
    
    @ExcelProperty(value = "手机号", index = 3)
    private String phone;
    
    @ExcelProperty(value = "邮箱", index = 4)
    private String email;
    
    @ExcelProperty(value = "性别", index = 5)
    private String gender;
    
    @ExcelProperty(value = "角色", index = 6)
    private String roles;
    
    @ExcelProperty(value = "学校", index = 7)
    private String schoolName;
    
    @ExcelProperty(value = "院系", index = 8)
    private String department;
    
    @ExcelProperty(value = "工号/学号", index = 9)
    private String jobNumber;
    
    @ExcelProperty(value = "状态", index = 10)
    private String status;
    
    @ExcelProperty(value = "最后登录时间", index = 11)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;
    
    @ExcelProperty(value = "注册时间", index = 12)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date createdTime;
    
}
