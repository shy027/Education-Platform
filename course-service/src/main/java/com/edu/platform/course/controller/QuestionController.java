package com.edu.platform.course.controller;

import com.edu.platform.common.result.PageResult;
import com.edu.platform.common.result.Result;
import com.edu.platform.course.dto.request.QuestionCreateRequest;
import com.edu.platform.course.dto.request.QuestionQueryRequest;
import com.edu.platform.course.dto.request.QuestionUpdateRequest;
import com.edu.platform.course.dto.response.QuestionResponse;
import com.edu.platform.course.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import com.alibaba.excel.EasyExcel;
import com.edu.platform.course.dto.request.excel.QuestionExcelDTO;
import com.edu.platform.course.excel.QuestionExcelListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.platform.course.entity.ExamQuestion;
import com.edu.platform.course.mapper.ExamQuestionMapper;
import java.util.Collections;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目管理控制器
 */
@Tag(name = "题目管理")
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final ExamQuestionMapper questionMapper;

    @Operation(summary = "创建题目")
    @PostMapping
    public Result<Long> createQuestion(@RequestBody QuestionCreateRequest request) {
        Long questionId = questionService.createQuestion(request);
        return Result.success(questionId);
    }

    @Operation(summary = "更新题目")
    @PutMapping("/{questionId}")
    public Result<Void> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody QuestionUpdateRequest request) {
        questionService.updateQuestion(questionId, request);
        return Result.success();
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{questionId}")
    public Result<Void> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return Result.success();
    }

    @Operation(summary = "获取题目详情")
    @GetMapping("/{questionId}")
    public Result<QuestionResponse> getQuestionDetail(@PathVariable Long questionId) {
        QuestionResponse response = questionService.getQuestionDetail(questionId);
        return Result.success(response);
    }

    @Operation(summary = "查询题目列表")
    @GetMapping
    public Result<PageResult<QuestionResponse>> listQuestions(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        QuestionQueryRequest request = new QuestionQueryRequest();
        request.setCourseId(courseId);
        request.setChapterId(chapterId);
        request.setQuestionType(questionType);
        request.setDifficulty(difficulty);
        request.setKeyword(keyword);
        request.setCreatorId(creatorId);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        PageResult<QuestionResponse> result = questionService.listQuestions(request);
        return Result.success(result);
    }

    @Operation(summary = "下载题目导入模板")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("题目导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        // 构造一条示例数据
        List<QuestionExcelDTO> exampleList = new ArrayList<>();
        QuestionExcelDTO example = new QuestionExcelDTO();
        example.setQuestionType("单选题");
        example.setDifficulty("简单");
        example.setContent("Java中所有类的父类是？");
        example.setOptionA("String");
        example.setOptionB("Object");
        example.setOptionC("System");
        example.setOptionD("Class");
        example.setAnswer("B");
        example.setAnalysis("Object类是Java中所有类的超类。");
        exampleList.add(example);
        
        EasyExcel.write(response.getOutputStream(), QuestionExcelDTO.class)
                .sheet("题目导入模板")
                .doWrite(exampleList);
    }

    @Operation(summary = "导入题目")
    @PostMapping("/import")
    public Result<Void> importQuestions(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long courseId) throws IOException {
        
        EasyExcel.read(file.getInputStream(), QuestionExcelDTO.class, 
                new QuestionExcelListener(questionService, courseId)).sheet().doRead();
                
        return Result.success();
    }

    @Operation(summary = "智能推荐抽题")
    @GetMapping("/recommend")
    public Result<List<QuestionResponse>> recommendQuestions(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String dimensions,
            @RequestParam(defaultValue = "10") Integer count) {
        
        // 此处为简化的推荐算法(在没有真实AI微服务响应下的同维度随机抽题回退策略)
        // 实际如果要接入大模型AI微服务，可以在这组装prompt调用ai-service。由于提问者指出“题库智能推荐”，故此处从数据库匹配。
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getIsDeleted, 0).eq(ExamQuestion::getStatus, 1);
        if (courseId != null) {
            wrapper.eq(ExamQuestion::getCourseId, courseId);
        }
        wrapper.last("ORDER BY RAND() LIMIT " + count);
        
        List<ExamQuestion> questions = questionMapper.selectList(wrapper);
        List<QuestionResponse> results = new ArrayList<>();
        for (ExamQuestion q : questions) {
            results.add(questionService.getQuestionDetail(q.getId()));
        }
        return Result.success(results);
    }
}
