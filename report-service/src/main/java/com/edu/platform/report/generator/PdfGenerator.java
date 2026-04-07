package com.edu.platform.report.generator;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * PDF生成工具类 (Thymeleaf + OpenHTMLtoPDF 版本)
 *
 * @author Education Platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PdfGenerator {
    
    private final TemplateEngine templateEngine;
    private final ChartGenerator chartGenerator;
    
    /**
     * 生成课程报告PDF
     */
    public byte[] generateCourseReport(Map<String, Object> data) {
        try {
            // 准备图表并转为 Base64
            @SuppressWarnings("unchecked")
            Map<String, Double> involvedDimensions = (Map<String, Double>) data.getOrDefault("involvedDimensions", Map.of());
            
            byte[] radarBytes = chartGenerator.generateRadarChart("课程维度分析分布图", involvedDimensions);
            data.put("radarChart", Base64.getEncoder().encodeToString(radarBytes));
            
            @SuppressWarnings("unchecked")
            Map<String, Integer> behaviorStats = (Map<String, Integer>) data.getOrDefault("behaviorStats", Map.of());
            byte[] barBytes = chartGenerator.generateBarChart("学习行为分布图", "行为类型", "次数", behaviorStats);
            data.put("barChart", Base64.getEncoder().encodeToString(barBytes));
            
            data.put("now", LocalDateTime.now());
            
            // 渲染 HTML
            Context context = new Context();
            context.setVariables(data);
            String html = templateEngine.process("course_report", context);
            
            // 转为 PDF
            return renderPdf(html);
        } catch (Exception e) {
            log.error("生成课程报告PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成学校报告PDF
     */
    public byte[] generateSchoolReport(Map<String, Object> data) {
        try {
            // 准备图表并转为 Base64
            @SuppressWarnings("unchecked")
            Map<String, String> sDimensionNames = (Map<String, String>) data.getOrDefault("dimensionNames", Map.of());
            
            Map<String, Double> sScores = new HashMap<>();
            sScores.put(sDimensionNames.getOrDefault("dimension1", "维度1"), ((BigDecimal)data.getOrDefault("dimension1Avg", BigDecimal.ZERO)).doubleValue());
            sScores.put(sDimensionNames.getOrDefault("dimension2", "维度2"), ((BigDecimal)data.getOrDefault("dimension2Avg", BigDecimal.ZERO)).doubleValue());
            sScores.put(sDimensionNames.getOrDefault("dimension3", "维度3"), ((BigDecimal)data.getOrDefault("dimension3Avg", BigDecimal.ZERO)).doubleValue());
            sScores.put(sDimensionNames.getOrDefault("dimension4", "维度4"), ((BigDecimal)data.getOrDefault("dimension4Avg", BigDecimal.ZERO)).doubleValue());
            sScores.put(sDimensionNames.getOrDefault("dimension5", "维度5"), ((BigDecimal)data.getOrDefault("dimension5Avg", BigDecimal.ZERO)).doubleValue());
            
            byte[] radarBytes = chartGenerator.generateRadarChart("全域五维度分布图", sScores);
            data.put("radarChart", Base64.getEncoder().encodeToString(radarBytes));
            
            @SuppressWarnings("unchecked")
            Map<String, Integer> sBehaviorStats = (Map<String, Integer>) data.getOrDefault("behaviorStats", Map.of());
            byte[] barBytes = chartGenerator.generateBarChart("全域学习行为统计", "行为类型", "次数", sBehaviorStats);
            data.put("barChart", Base64.getEncoder().encodeToString(barBytes));
            
            data.put("now", LocalDateTime.now());
            
            // 渲染 HTML
            Context context = new Context();
            context.setVariables(data);
            String html = templateEngine.process("school_report", context);
            
            // 转为 PDF
            return renderPdf(html);
        } catch (Exception e) {
            log.error("生成学校报告PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage());
        }
    }
    
    private byte[] renderPdf(String html) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            
            // 注册中文字体 (解决 # 乱码问题)
            File fontFile = new File("C:/Windows/Fonts/simsun.ttc");
            if (fontFile.exists()) {
                builder.useFont(fontFile, "SimSun");
            } else {
                log.warn("未找到系统字体 simsun.ttc，PDF 中文渲染可能失败");
            }
            
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        }
    }
}
