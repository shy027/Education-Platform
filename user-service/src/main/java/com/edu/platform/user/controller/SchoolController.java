package com.edu.platform.user.controller;

import com.edu.platform.common.constant.Constants;
import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.user.dto.request.JoinSchoolRequest;
import com.edu.platform.user.dto.response.SchoolResponse;
import com.edu.platform.user.service.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学校控制器
 *
 * @author Education Platform
 */
@Tag(name = "学校管理", description = "学校管理相关接口")
@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
public class SchoolController {
    
    private final SchoolService schoolService;
    
    @Operation(summary = "加入学校")
    @PostMapping("/{schoolId}/join")
    public Result<Void> joinSchool(
            HttpServletRequest request,
            @Parameter(description = "学校ID") @PathVariable Long schoolId,
            @Valid @RequestBody JoinSchoolRequest joinRequest) {
        Long userId = (Long) request.getAttribute(Constants.USER_ID);
        schoolService.joinSchool(userId, schoolId, joinRequest);
        return Result.success("加入成功", null);
    }
    
    @Operation(summary = "获取学校列表")
    @GetMapping
    public Result<PageResult<SchoolResponse>> getSchoolList(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "省份") @RequestParam(required = false) String province,
            @Parameter(description = "页码") @RequestParam(required = false) Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(required = false) Integer pageSize) {
        PageResult<SchoolResponse> result = schoolService.getSchoolList(keyword, province, pageNum, pageSize);
        return Result.success(result);
    }
    
    @Operation(summary = "获取学校详情")
    @GetMapping("/{schoolId}")
    public Result<SchoolResponse> getSchoolDetail(
            @Parameter(description = "学校ID") @PathVariable Long schoolId) {
        SchoolResponse response = schoolService.getSchoolDetail(schoolId);
        return Result.success(response);
    }
    
}
