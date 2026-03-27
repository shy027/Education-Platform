package com.edu.platform.ai.service.impl;

import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.fastjson2.JSON;
import com.edu.platform.ai.client.CourseClient;
import com.edu.platform.ai.client.ResourceClient;
import com.edu.platform.common.result.PageResult;
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
                    "3. subjectArea: 准确的学科领域名称（如：电子信息与计算机、机械工程类、医学与护理类等）\n" +
                    "4. suggestedTags: 建议的分类标签列表 (3-5个)\n" +
                    "5. suggestedDimensions: 匹配的素养考核维度。请仅从以下列表中选择 key：\n" +
                    "   - dimension1: 知识技能素养\n" +
                    "   - dimension2: 职业品格素养\n" +
                    "   - dimension3: 创新实践素养\n" +
                    "   - dimension4: 社会责任素养\n" +
                    "   - dimension5: 发展适应素养\n" +
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
            
            // 2. 获取候选资源库 (文档推荐暂时不带分类/关键字，抓取较多样本)
            List<ResourceClient.ResourceDTO> resources = fetchCandidateResources(null, null);
            
            // 3. 执行推荐
            String courseContext = "课程参考文档内容摘要：\n" + (content.length() > 5000 ? content.substring(0, 5000) : content);
            return doRecommend(courseContext, resources);
            
        } catch (Exception e) {
            log.error("基于文档的 AI 资源推荐失败: {}", e.getMessage());
            throw new RuntimeException("推荐失败: " + e.getMessage());
        }
    }

    @Override
    public AiRecommendationResponse recommendResources(Long courseId, Long chapterId) {
        try {
            // 1. 获取课程详情 (作为基础背景)
            Result<CourseClient.CourseDetailDTO> courseResult = courseClient.getCourseDetail(courseId);
            if (!courseResult.isSuccess() || courseResult.getData() == null) {
                log.error("获取课程信息失败: {}", courseResult.getMessage());
                throw new RuntimeException("获取课程信息失败");
            }
            CourseClient.CourseDetailDTO course = courseResult.getData();

            StringBuilder contextBuilder = new StringBuilder();
            String searchKeyword = course.getCourseName();

            // 2. 如果指定了章节，尝试获取章节课件内容作为深度语义背景
            if (chapterId != null) {
                Result<PageResult<CourseClient.CoursewareResponse>> wareResult = 
                        courseClient.getCoursewareList(courseId, chapterId, null, 100);
                
                if (wareResult.isSuccess() && wareResult.getData() != null) {
                    List<CourseClient.CoursewareResponse> chapterWares = wareResult.getData().getList();
                    if (chapterWares != null && !chapterWares.isEmpty()) {
                        // 过滤出 PDF(2) 和 PPT(3) 类型的课件进行内容深度解析
                        List<CourseClient.CoursewareResponse> relevantWares = chapterWares.stream()
                                .filter(w -> w.getWareType() == 2 || w.getWareType() == 3)
                                .limit(2)
                                .collect(Collectors.toList());

                        if (!relevantWares.isEmpty()) {
                            for (CourseClient.CoursewareResponse ware : relevantWares) {
                                String content = safeParseUrl(ware.getFileUrl());
                                if (content != null && !content.trim().isEmpty()) {
                                    contextBuilder.append("课件【").append(ware.getWareTitle()).append("】核心内容解析：\n");
                                    contextBuilder.append(content.length() > 3000 ? content.substring(0, 3000) : content).append("\n\n");
                                } else {
                                    contextBuilder.append("课件【").append(ware.getWareTitle()).append("】(仅标题)：").append(ware.getWareTitle()).append("\n");
                                }
                            }
                            searchKeyword = relevantWares.get(0).getWareTitle();
                        } else {
                            log.info("该章节无 PDF/PPT 课件，回退至课程级推荐。章节ID: {}", chapterId);
                        }
                    } else {
                        log.info("该章节无课件数据，将回退至课程级推荐。章节ID: {}", chapterId);
                    }
                }
            }

            // 3. 获取候选资源库 (优先使用检索词召回)
            List<ResourceClient.ResourceDTO> resources = fetchCandidateResources(searchKeyword, null);
            if (resources.isEmpty() && course.getSubjectArea() != null) {
                resources = fetchCandidateResources(course.getSubjectArea(), null);
            }
            if (resources.isEmpty()) {
                resources = fetchCandidateResources(null, null);
            }

            if (resources.isEmpty()) {
                AiRecommendationResponse emptyResponse = new AiRecommendationResponse();
                emptyResponse.setRecommendations(new java.util.ArrayList<>());
                return emptyResponse;
            }

            // 4. 构造 Prompt 执行 AI 语义匹配
            StringBuilder courseContext = new StringBuilder();
            courseContext.append("课程名称：").append(course.getCourseName()).append("\n");
            courseContext.append("课程简介：").append(course.getCourseIntro() != null ? course.getCourseIntro() : "暂无").append("\n");
            if (contextBuilder.length() > 0) {
                courseContext.append("学习情境与课件内容：\n").append(contextBuilder.toString());
            }
            courseContext.append("学科领域：").append(course.getSubjectArea() != null ? course.getSubjectArea() : "通用").append("\n");
            
            return doRecommend(courseContext.toString(), resources);

        } catch (Exception e) {
            log.error("AI 资源推荐失败: {}", e.getMessage());
            throw new RuntimeException("推荐失败: " + e.getMessage());
        }
    }

    /**
     * 安全地解析远程 URL 文档内容
     */
    private String safeParseUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("http")) return null;
        try (java.io.InputStream is = new java.net.URL(fileUrl).openStream()) {
            return tika.parseToString(is);
        } catch (Exception e) {
            log.warn("解析远端文档失败: {}, URL: {}", e.getMessage(), fileUrl);
            return null;
        }
    }

    /**
     * 实现通用的推荐逻辑
     */
    private AiRecommendationResponse doRecommend(String courseContext, List<ResourceClient.ResourceDTO> resources) throws Exception {
        // 构造 Prompt
        String systemPrompt = "你是一位专业的教学资源匹配专家。你的任务是根据课程的背景信息（包括名称、介绍、学科领域、素养要求及关键词），从候选资源列表中选出最相关的 5-10 个资源。\n\n" +
                "匹配逻辑要求：\n" +
                "1. 学科优先：如果定义了学科领域（如：电子信息与计算机），请匹配该学科及其下属细分学科（如：软件工程、网络工程、人工智能等）的资源。\n" +
                "2. 素养对齐：如果课程没有明确学科（显示为“通用”），或者学科匹配度有限，请重点根据“素养维度”（如社会责任、创新实践等）来寻找在价值观和能力培养上高度契合的资源。\n" +
                "3. 综合判断：结合课程简介和关键词，确保推荐的资源对教师备课或学生学习有实际帮助。\n\n" +
                "输出要求：严格按 JSON 格式输出数组，不输出任何解释性文字。\n" +
                "输出格式示例：[{\"resourceId\":1, \"reason\":\"推荐理由\", \"matchScore\":0.95}]";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("课程背景信息：\n").append(courseContext).append("\n\n");
        userPrompt.append("候选资源列表：\n");
        userPrompt.append(JSON.toJSONString(resources.stream().map(r -> {
            String tags = r.getTags() != null ? r.getTags().stream()
                    .map(ResourceClient.ResourceDTO.TagInfo::getTagName)
                    .collect(Collectors.joining(",")) : "";
            return "ID:" + r.getId() + ", 标题:" + r.getTitle() + ", 摘要:" + r.getSummary() + ", 标签:[" + tags + "]";
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
        log.info("AI 推荐原始输出: {}", jsonOutput);
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

    private List<ResourceClient.ResourceDTO> fetchCandidateResources(String keyword, Long categoryId) {
        Result<PageResult<ResourceClient.ResourceDTO>> resourceResult = 
                resourceClient.pageResources(2, 1, 100, keyword, categoryId); // 扩大搜索范围至 100
        if (!resourceResult.isSuccess()) {
            log.error("获取资源库失败: {}", resourceResult.getMessage());
            return new java.util.ArrayList<>();
        }
        if (resourceResult.getData() == null || resourceResult.getData().getList() == null) {
            log.warn("资源库返回数据为空, keyword=[{}]", keyword);
            return new java.util.ArrayList<>();
        }
        log.info("从资源库获取到 {} 条记录, keyword=[{}]", resourceResult.getData().getList().size(), keyword);
        return resourceResult.getData().getList();
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
