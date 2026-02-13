package com.edu.platform.report.generator;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * PDF生成工具类
 *
 * @author Education Platform
 */
@Slf4j
@Component
public class PdfGenerator {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 生成课程报告PDF
     *
     * @param data 报告数据
     * @return PDF字节数组
     */
    public byte[] generateCourseReport(Map<String, Object> data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // 设置中文字体
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            document.setFont(font);
            
            // 添加标题
            addTitle(document, "课程思政教学成效报告");
            addSubTitle(document, "Course Ideological Education Report");
            
            // 添加生成时间
            document.add(new Paragraph("生成时间: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT));
            
            document.add(new Paragraph("\n"));
            
            // 1. 课程基本信息
            addSectionTitle(document, "一、课程基本信息");
            addCourseInfo(document, data);
            
            // 2. 整体概况
            addSectionTitle(document, "二、整体概况");
            addOverallSummary(document, data);
            
            // 3. 五维度分析
            addSectionTitle(document, "三、五维度分析");
            addDimensionAnalysis(document, data);
            
            // 4. 学习行为分析
            addSectionTitle(document, "四、学习行为分析");
            addBehaviorAnalysis(document, data);
            
            // 5. 优秀学生
            addSectionTitle(document, "五、优秀学生案例");
            addTopStudents(document, data);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("生成课程报告PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加主标题
     */
    private void addTitle(Document document, String title) {
        Paragraph p = new Paragraph(title)
            .setFontSize(24)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.BLUE);
        document.add(p);
    }
    
    /**
     * 添加副标题
     */
    private void addSubTitle(Document document, String subtitle) {
        Paragraph p = new Paragraph(subtitle)
            .setFontSize(14)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.GRAY);
        document.add(p);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * 添加章节标题
     */
    private void addSectionTitle(Document document, String title) {
        Paragraph p = new Paragraph(title)
            .setFontSize(16)
            .setBold()
            .setFontColor(ColorConstants.DARK_GRAY);
        document.add(p);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * 添加课程基本信息
     */
    private void addCourseInfo(Document document, Map<String, Object> data) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        addTableRow(table, "课程名称", data.getOrDefault("courseName", "未知课程").toString());
        addTableRow(table, "授课教师", data.getOrDefault("teacherName", "未知教师").toString());
        addTableRow(table, "学生人数", data.getOrDefault("studentCount", 0).toString());
        addTableRow(table, "报告周期", data.getOrDefault("reportPeriod", "最近30天").toString());
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * 添加整体概况
     */
    private void addOverallSummary(Document document, Map<String, Object> data) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        addTableRow(table, "参与学生数", data.getOrDefault("activeStudents", 0).toString());
        addTableRow(table, "平均素养得分", formatScore(data.get("avgScore")));
        addTableRow(table, "优秀学生数", data.getOrDefault("excellentCount", 0).toString());
        addTableRow(table, "良好学生数", data.getOrDefault("goodCount", 0).toString());
        addTableRow(table, "合格学生数", data.getOrDefault("passCount", 0).toString());
        addTableRow(table, "待提升学生数", data.getOrDefault("needImprovementCount", 0).toString());
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * 添加五维度分析
     */
    private void addDimensionAnalysis(Document document, Map<String, Object> data) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // 表头
        table.addHeaderCell(createHeaderCell("维度名称"));
        table.addHeaderCell(createHeaderCell("平均得分"));
        
        // 数据行
        addTableRow(table, "价值观认同", formatScore(data.get("dimension1Avg")));
        addTableRow(table, "思想品德", formatScore(data.get("dimension2Avg")));
        addTableRow(table, "社会责任", formatScore(data.get("dimension3Avg")));
        addTableRow(table, "创新精神", formatScore(data.get("dimension4Avg")));
        addTableRow(table, "团队协作", formatScore(data.get("dimension5Avg")));
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * 添加行为分析
     */
    private void addBehaviorAnalysis(Document document, Map<String, Object> data) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // 表头
        table.addHeaderCell(createHeaderCell("行为类型"));
        table.addHeaderCell(createHeaderCell("总次数"));
        
        // 数据行
        @SuppressWarnings("unchecked")
        Map<String, Integer> behaviorStats = (Map<String, Integer>) data.getOrDefault("behaviorStats", Map.of());
        
        addTableRow(table, "浏览课件", behaviorStats.getOrDefault("VIEW_COURSEWARE", 0).toString());
        addTableRow(table, "完成课件", behaviorStats.getOrDefault("COMPLETE_COURSEWARE", 0).toString());
        addTableRow(table, "提交任务", behaviorStats.getOrDefault("SUBMIT_TASK", 0).toString());
        addTableRow(table, "发布讨论", behaviorStats.getOrDefault("CREATE_POST", 0).toString());
        addTableRow(table, "发表评论", behaviorStats.getOrDefault("CREATE_COMMENT", 0).toString());
        addTableRow(table, "点赞帖子", behaviorStats.getOrDefault("LIKE_POST", 0).toString());
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * 添加优秀学生
     */
    private void addTopStudents(Document document, Map<String, Object> data) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // 表头
        table.addHeaderCell(createHeaderCell("排名"));
        table.addHeaderCell(createHeaderCell("学生姓名"));
        table.addHeaderCell(createHeaderCell("综合得分"));
        table.addHeaderCell(createHeaderCell("等级"));
        
        // 数据行(从data中获取真实数据)
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topStudents = (List<Map<String, Object>>) data.get("topStudents");
        
        if (topStudents != null && !topStudents.isEmpty()) {
            int rank = 1;
            for (Map<String, Object> student : topStudents) {
                String userName = student.get("userName") != null ? student.get("userName").toString() : "未知";
                String score = formatScore(student.get("totalScore"));
                String level = student.get("level") != null ? student.get("level").toString() : "未评定";
                
                addTableRow(table, String.valueOf(rank), userName, score, level);
                rank++;
            }
        } else {
            // 如果没有数据,显示提示信息
            addTableRow(table, "-", "暂无数据", "-", "-");
        }
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * 添加表格行
     */
    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)));
        table.addCell(new Cell().add(new Paragraph(value)));
    }
    
    /**
     * 添加表格行(多列)
     */
    private void addTableRow(Table table, String... values) {
        for (String value : values) {
            table.addCell(new Cell().add(new Paragraph(value)));
        }
    }
    
    /**
     * 创建表头单元格
     */
    private Cell createHeaderCell(String text) {
        return new Cell()
            .add(new Paragraph(text).setBold())
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setTextAlignment(TextAlignment.CENTER);
    }
    
    /**
     * 格式化分数
     */
    private String formatScore(Object score) {
        if (score == null) {
            return "0.00";
        }
        if (score instanceof BigDecimal) {
            return ((BigDecimal) score).setScale(2, java.math.RoundingMode.HALF_UP).toString();
        }
        if (score instanceof Number) {
            return String.format("%.2f", ((Number) score).doubleValue());
        }
        return score.toString();
    }
}
