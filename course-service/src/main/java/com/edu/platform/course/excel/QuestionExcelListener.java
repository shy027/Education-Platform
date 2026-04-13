package com.edu.platform.course.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.edu.platform.course.dto.request.QuestionCreateRequest;
import com.edu.platform.course.dto.request.excel.QuestionExcelDTO;
import com.edu.platform.course.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class QuestionExcelListener implements ReadListener<QuestionExcelDTO> {

    private static final int BATCH_COUNT = 100;
    private List<QuestionExcelDTO> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    
    private final QuestionService questionService;
    private final Long courseId;
    
    // 如果想要完全容错解析“专业理论”等中文字段至"1,3"等，可以在此处增加一个 map 字典，或直接让 Service 保存原始 String 让业务需要时去处理
    // 本次简化处理：直接让系统存储字符串（如果为文本则存文本，如果为1,3等数字则存数字）
    
    public QuestionExcelListener(QuestionService questionService, Long courseId) {
        this.questionService = questionService;
        this.courseId = courseId;
    }

    @Override
    public void invoke(QuestionExcelDTO data, AnalysisContext context) {
        // 题型和题目内容任意一个为空，则该题无效，直接跳过
        if (StringUtils.isBlank(data.getContent()) || StringUtils.isBlank(data.getQuestionType())) {
            log.warn("跳过无效题目（题型或内容为空）: {}", data);
            return;
        }
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("所有数据解析完成！");
    }

    private void saveData() {
        log.info("{}条数据，开始存储数据库！", cachedDataList.size());
        for (QuestionExcelDTO dto : cachedDataList) {
            try {
                QuestionCreateRequest req = convertToRequest(dto);
                questionService.createQuestion(req);
            } catch (Exception e) {
                log.error("解析单条题目失败: {}", dto, e);
            }
        }
        log.info("存储数据库成功！");
    }

    private QuestionCreateRequest convertToRequest(QuestionExcelDTO dto) {
        QuestionCreateRequest req = new QuestionCreateRequest();
        req.setCourseId(courseId != null ? courseId : 0L);
        req.setContent(dto.getContent().trim());
        req.setAnalysis(dto.getAnalysis());
        
        // 学科处理：将 Excel 中的学科名称传给 Service 进行 ID 映射
        req.setCategoryName(dto.getCategoryName());
        
        // 维度处理：支持数字、文字及其混合输入
        String dim = dto.getDimensions();
        if (StringUtils.isNotBlank(dim)) {
            req.setDimensions(parseDimensions(dim));
        } else {
            // 如果没填，使用兜底维度：比如 "1"
            req.setDimensions("1");
        }
        
        // 难度设置
        req.setDifficulty(parseDifficulty(dto.getDifficulty()));
        
        // 题型解析
        String typeStr = dto.getQuestionType();
        int type = parseType(typeStr);
        req.setQuestionType(type);
        
        // 答案预处理：去掉所有空格并转大写（支持 "A BC"、"a b c" 等各种写法）
        String rawAnswer = dto.getAnswer();
        String normalizedAnswer = StringUtils.isBlank(rawAnswer)
                ? ""
                : rawAnswer.replaceAll("\\s+", "").toUpperCase();
        
        List<QuestionCreateRequest.QuestionOptionDTO> options = new ArrayList<>();
        
        // 依据题型处理答案和选项
        if (type == 1 || type == 2) {
            // 选择题
            addOptionIfExist(options, "A", dto.getOptionA(), normalizedAnswer);
            addOptionIfExist(options, "B", dto.getOptionB(), normalizedAnswer);
            addOptionIfExist(options, "C", dto.getOptionC(), normalizedAnswer);
            addOptionIfExist(options, "D", dto.getOptionD(), normalizedAnswer);
            addOptionIfExist(options, "E", dto.getOptionE(), normalizedAnswer);
            addOptionIfExist(options, "F", dto.getOptionF(), normalizedAnswer);
            req.setOptions(options);
        } else if (type == 3) {
            // 判断题：对/T/true/正确 均认为正确
            boolean isCorrect = "对".equals(normalizedAnswer)
                    || "T".equals(normalizedAnswer)
                    || "TRUE".equals(normalizedAnswer)
                    || "正确".equals(normalizedAnswer);
            QuestionCreateRequest.QuestionOptionDTO opT = new QuestionCreateRequest.QuestionOptionDTO();
            opT.setOptionLabel("A"); opT.setContent("正确"); opT.setSortOrder(1); opT.setIsCorrect(isCorrect);
            QuestionCreateRequest.QuestionOptionDTO opF = new QuestionCreateRequest.QuestionOptionDTO();
            opF.setOptionLabel("B"); opF.setContent("错误"); opF.setSortOrder(2); opF.setIsCorrect(!isCorrect);
            options.add(opT);
            options.add(opF);
            req.setOptions(options);
        } else if (type == 4) {
            // 填空题
            req.setCorrectAnswer(StringUtils.isBlank(normalizedAnswer) ? dto.getAnswer() : dto.getAnswer().trim());
        } else if (type == 5) {
            // 简答题（保留原始答案空格，只去首尾空格）
            req.setReferenceAnswer(StringUtils.isBlank(dto.getAnswer()) ? null : dto.getAnswer().trim());
        }
        
        return req;
    }

    private void addOptionIfExist(List<QuestionCreateRequest.QuestionOptionDTO> options, String label, String content, String normalizedAnswer) {
        if (StringUtils.isNotBlank(content)) {
            QuestionCreateRequest.QuestionOptionDTO opt = new QuestionCreateRequest.QuestionOptionDTO();
            opt.setOptionLabel(label);
            opt.setContent(content.trim());
            opt.setSortOrder(options.size() + 1);
            // normalizedAnswer 已经是去空格+大写，直接匹配
            opt.setIsCorrect(StringUtils.isNotBlank(normalizedAnswer) && normalizedAnswer.contains(label));
            options.add(opt);
        }
    }

    private String parseDimensions(String dimStr) {
        if (StringUtils.isBlank(dimStr)) return "1";
        
        // 分隔符支持中英文逗号、分号以及空格
        String[] parts = dimStr.split("[,，;；\\s]+");
        java.util.Set<String> results = new java.util.LinkedHashSet<>();
        
        for (String part : parts) {
            String p = part.trim();
            if (StringUtils.isBlank(p)) continue;
            
            // 如果是纯数字，直接添加
            if (StringUtils.isNumeric(p)) {
                results.add(p);
                continue;
            }
            
            // 文字匹配
            if (p.contains("知识") || p.contains("技能")) results.add("1");
            else if (p.contains("品格") || p.contains("操守")) results.add("2");
            else if (p.contains("创新") || p.contains("实践")) results.add("3");
            else if (p.contains("责任") || p.contains("担当")) results.add("4");
            else if (p.contains("发展") || p.contains("适应")) results.add("5");
        }
        
        return results.isEmpty() ? "1" : String.join(",", results);
    }

    private int parseType(String typeStr) {
        if (StringUtils.isBlank(typeStr)) return 1; // 默认单选
        if (typeStr.contains("多选")) return 2;
        if (typeStr.contains("判断")) return 3;
        if (typeStr.contains("填空")) return 4;
        if (typeStr.contains("简答")) return 5;
        if (typeStr.contains("编程")) return 6;
        return 1;
    }

    private int parseDifficulty(String diff) {
        if (StringUtils.isBlank(diff)) return 1;
        if (diff.contains("中等")) return 2;
        if (diff.contains("较难") || diff.contains("困难")) return 3;
        if (diff.contains("很难")) return 4;
        if (diff.contains("极难")) return 5;
        return 1;
    }
}
