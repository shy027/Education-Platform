package com.edu.platform.ai.service.impl;

import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.fastjson2.JSON;
import com.edu.platform.ai.client.CourseClient;
import com.edu.platform.ai.client.ResourceClient;
import com.edu.platform.ai.dto.response.AiCourseAnalysisResponse;
import com.edu.platform.ai.dto.response.AiRecommendationResponse;
import com.edu.platform.ai.service.AiService;
import com.edu.platform.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    @Value("${ai.bailian.api-key}")
    private String apiKey;

    @Value("${ai.bailian.base-url:}")
    private String baseUrl;

    @Value("${ai.bailian.analyze.model}")
    private String analyzeModel;

    @Value("${ai.bailian.analyze.max-tokens:1000}")
    private Integer analyzeMaxTokens;

    @Value("${ai.bailian.analyze.temperature}")
    private Double analyzeTemperature;

    @Value("${ai.bailian.recommend.model:qwen-plus}")
    private String recommendModel;

    @Value("${ai.bailian.recommend.max-tokens:1000}")
    private Integer recommendMaxTokens;

    @Value("${ai.bailian.recommend.temperature:0.5}")
    private Double recommendTemperature;

    private final Tika tika = new Tika();
    private final CourseClient courseClient;
    private final ResourceClient resourceClient;

    @Override
    public AiCourseAnalysisResponse analyzeCourseDocument(MultipartFile file) {
        try {
            // 1. 文档解析 (PDF/Word -> Text)
            String content = tika.parseToString(file.getInputStream());
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("文档内容为空，无法分析");
            }
            log.info("提取到文档内容，长度: {}", content.length());

            // 2. 构造 Prompt
            String systemPrompt = "你是一位课程开发与思政教育领域的专家。根据教师提供的课程资料，提取核心信息并按 JSON 格式返回。\n" +
                    "请包含以下字段：\n" +
                    "1. courseName: 课程名称\n" +
                    "2. courseIntro: 课程简介（100-200字）\n" +
                    "3. subjectArea: 准确的学科领域名称（如：计机科学、机械工程、医学等）\n" +
                    "4. suggestedTags: 建议的分类标签列表 (3-5个)\n" +
                    "5. suggestedDimensions: 匹配的素养考核维度。请仅从以下列表中选择 key：\n" +
                    "   - dimension1: 专业理论\n" +
                    "   - dimension2: 技术技能\n" +
                    "   - dimension3: 职业认同\n" +
                    "   - dimension4: 工艺创新\n" +
                    "   - dimension5: 社会责任\n" +
                    "   - dimension6: 持续发展\n" +
                    "6. keywords: 核心关键词 (5个)\n\n" +
                    "严格按 JSON 格式输出，不要输出其他非 JSON 内容。\n" +
                    "输出格式示例：{\"courseName\":\"...\",\"courseIntro\":\"...\",\"subjectArea\":\"...\",\"suggestedTags\":[\"...\"],\"suggestedDimensions\":[\"dimension1\",\"dimension2\"],\"keywords\":[\"...\"]}";
            
            // 3. 调用阿里百炼
            Generation gen = new Generation();
            Message systemMsg = Message.builder().role(Role.SYSTEM.getValue()).content(systemPrompt).build();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content("以下是课程资料内容：\n" + content).build();
            
            GenerationParam param = GenerationParam.builder()
                    .model(analyzeModel)
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .temperature(analyzeTemperature.floatValue())
                    .maxTokens(analyzeMaxTokens) // 使用配置的 max-tokens
                    .apiKey(apiKey)
                    .build();
            
            GenerationResult result = gen.call(param);
            String jsonOutput = result.getOutput().getChoices().get(0).getMessage().getContent();
            log.debug("AI 输出 JSON: {}", jsonOutput);
            
            // 4. 清理并解析 JSON
            jsonOutput = cleanJsonOutput(jsonOutput);
            return JSON.parseObject(jsonOutput, AiCourseAnalysisResponse.class);
            
        } catch (Exception e) {
            log.error("AI 分析课程文档失败: {}", e.getMessage());
            throw new RuntimeException("AI 分析失败: " + e.getMessage());
        }
    }

    @Override
    public AiRecommendationResponse recommendResourcesByDocument(MultipartFile file) {
        try {
            // 1. 文档解析 (PDF/Word -> Text)
            String content = tika.parseToString(file.getInputStream());
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("文档内容为空，无法分析建议");
            }
            
            // 2. 获取候选资源库
            List<ResourceClient.ResourceDTO> resources = fetchCandidateResources();
            
            // 3. 执行推荐
            String courseContext = "课程参考文档内容摘要：\n" + (content.length() > 5000 ? content.substring(0, 5000) : content);
            return doRecommend(courseContext, resources);
            
        } catch (Exception e) {
            log.error("基于文档的 AI 资源推荐失败: {}", e.getMessage());
            throw new RuntimeException("推荐失败: " + e.getMessage());
        }
    }

    @Override
    public AiRecommendationResponse recommendResources(Long courseId) {
        try {
            // 1. 获取课程详情
            Result<CourseClient.CourseDetailDTO> courseResult = courseClient.getCourseDetail(courseId);
            if (!courseResult.isSuccess()) {
                log.error("获取课程信息失败，错误码：{}，原因：{}", courseResult.getCode(), courseResult.getMessage());
                throw new RuntimeException("获取课程信息失败: " + courseResult.getMessage());
            }
            if (courseResult.getData() == null) {
                throw new RuntimeException("课程数据为空");
            }
            CourseClient.CourseDetailDTO course = courseResult.getData();

            // 2. 获取候选资源库
            List<ResourceClient.ResourceDTO> resources = fetchCandidateResources();

            // 3. 执行推荐
            StringBuilder courseContext = new StringBuilder();
            courseContext.append("课程名称：").append(course.getCourseName() != null ? course.getCourseName() : "未命名").append("\n");
            courseContext.append("课程简介：").append(course.getCourseIntro() != null ? course.getCourseIntro() : "暂无").append("\n");
            courseContext.append("学科领域：").append(course.getSubjectArea() != null ? course.getSubjectArea() : "通用").append("\n");
            
            return doRecommend(courseContext.toString(), resources);

        } catch (Exception e) {
            log.error("AI 资源推荐失败: {}", e.getMessage());
            throw new RuntimeException("推荐失败: " + e.getMessage());
        }
    }

    /**
     * 实现通用的推荐逻辑
     */
    private AiRecommendationResponse doRecommend(String courseContext, List<ResourceClient.ResourceDTO> resources) throws Exception {
        // 构造 Prompt
        String systemPrompt = "你是一位课程资源匹配专家。根据课程信息/大纲内容从候选资源列表中选出最相关的 5-10 个资源。" +
                "严格按 JSON 格式输出数组，不输出其他内容。\n" +
                "输出格式示例：[{\"resourceId\":1, \"reason\":\"推荐理由\", \"matchScore\":0.95}]";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("课程背景信息：\n").append(courseContext).append("\n\n");
        userPrompt.append("候选资源列表：\n");
        userPrompt.append(JSON.toJSONString(resources.stream().map(r -> {
            return "ID:" + r.getId() + ", 标题:" + r.getTitle() + ", 摘要:" + r.getSummary();
        }).collect(Collectors.toList())));

        // 调用 AI
        Generation gen = new Generation();
        Message systemMsg = Message.builder().role(Role.SYSTEM.getValue()).content(systemPrompt).build();
        Message userMsg = Message.builder().role(Role.USER.getValue()).content(userPrompt.toString()).build();

        GenerationParam param = GenerationParam.builder()
                .model(recommendModel)
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .temperature(recommendTemperature.floatValue())
                .maxTokens(recommendMaxTokens)
                .apiKey(apiKey)
                .build();

        GenerationResult result = gen.call(param);
        String jsonOutput = result.getOutput().getChoices().get(0).getMessage().getContent();
        jsonOutput = cleanJsonOutput(jsonOutput);

        // 解析并合并标题
        List<AiRecommendationResponse.Recommendation> recommendations = 
                JSON.parseArray(jsonOutput, AiRecommendationResponse.Recommendation.class);
        
        for (AiRecommendationResponse.Recommendation rec : recommendations) {
            resources.stream()
                    .filter(r -> r.getId().equals(rec.getResourceId()))
                    .findFirst()
                    .ifPresent(r -> rec.setTitle(r.getTitle()));
        }

        AiRecommendationResponse response = new AiRecommendationResponse();
        response.setRecommendations(recommendations);
        return response;
    }

    private List<ResourceClient.ResourceDTO> fetchCandidateResources() {
        Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<ResourceClient.ResourceDTO>> resourceResult = 
                resourceClient.pageResources(2, 1, 50); // 获取前 50 条已发布资源
        if (!resourceResult.isSuccess()) {
            throw new RuntimeException("获取资源库失败: " + resourceResult.getMessage());
        }
        if (resourceResult.getData() == null) {
            throw new RuntimeException("资源库数据为空");
        }
        return resourceResult.getData().getRecords();
    }

    /**
     * 清理 AI 返回的 Markdown 代码块
     */
    private String cleanJsonOutput(String output) {
        if (output.contains("```json")) {
            output = output.substring(output.indexOf("```json") + 7);
            output = output.substring(0, output.lastIndexOf("```"));
        } else if (output.contains("```")) {
            output = output.substring(output.indexOf("```") + 3);
            output = output.substring(0, output.lastIndexOf("```"));
        }
        return output.trim();
    }
}
