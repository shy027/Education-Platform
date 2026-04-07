package com.edu.platform.report.generator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * 图表生成工具类
 * 基于 JFreeChart 实现
 *
 * @author Education Platform
 */
@Component
public class ChartGenerator {

    /**
     * 生成雷达图 (用于五维度分析)
     */
    public byte[] generateRadarChart(String title, Map<String, Double> scores) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String group = "当前评分";
        
        scores.forEach((dimension, score) -> {
            dataset.addValue(score, group, dimension);
        });

        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        plot.setStartAngle(90);
        plot.setInteriorGap(0.30);
        plot.setToolTipGenerator(null);
        plot.setMaxValue(100.0); // 假设满分100
        
        // 设置颜色
        plot.setSeriesPaint(0, new Color(0, 123, 255, 150));
        plot.setOutlinePaint(Color.WHITE);

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        setStandardFont(chart);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(baos, chart, 500, 400);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("图表生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成柱状图 (用于行为分析)
     */
    public byte[] generateBarChart(String title, String categoryLabel, String valueLabel, Map<String, Integer> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        data.forEach((key, value) -> {
            dataset.addValue(value, "次数", key);
        });

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                categoryLabel,
                valueLabel,
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        // 自定义样式
        chart.setBackgroundPaint(Color.WHITE);
        setStandardFont(chart);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(baos, chart, 600, 400);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("图表生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成饼图 (用于等级分布)
     */
    public byte[] generatePieChart(String title, Map<String, Long> data) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        data.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true, true, false);

        chart.setBackgroundPaint(Color.WHITE);
        setStandardFont(chart);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(baos, chart, 500, 400);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("图表生成失败: " + e.getMessage());
        }
    }

    /**
     * 设置通用字体 (解决方框乱码问题)
     */
    private void setStandardFont(JFreeChart chart) {
        Font titleFont = new Font("SimSun", Font.BOLD, 18);
        Font standardFont = new Font("SimSun", Font.PLAIN, 12);
        
        // 1. 设置标题字体
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(titleFont);
        }
        
        // 2. 设置图例字体
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(standardFont);
        }
        
        // 3. 设置绘图区字体
        if (chart.getPlot() instanceof CategoryPlot) {
            CategoryPlot plot = chart.getCategoryPlot();
            // 设置轴标签字体
            plot.getDomainAxis().setLabelFont(standardFont);
            plot.getDomainAxis().setTickLabelFont(standardFont);
            plot.getRangeAxis().setLabelFont(standardFont);
            plot.getRangeAxis().setTickLabelFont(standardFont);
        } else if (chart.getPlot() instanceof PiePlot) {
            PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
            plot.setLabelFont(standardFont);
        } else if (chart.getPlot() instanceof SpiderWebPlot) {
            SpiderWebPlot plot = (SpiderWebPlot) chart.getPlot();
            plot.setLabelFont(standardFont);
        }
    }
}
