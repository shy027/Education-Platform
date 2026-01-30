package com.edu.platform.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目表
 * 注意: 字段名直接对应数据库列名
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_question")
public class ExamQuestion extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属课程ID (0表示公共题库)
     */
    private Long courseId;

    /**
     * 关联章节ID
     */
    private Long chapterId;

    /**
     * 题目内容 (支持富文本)
     */
    private String content;

    /**
     * 题型: 1-单选 2-多选 3-判断 4-填空 5-简答 6-编程
     * 数据库字段: type
     */
    private Integer type;

    /**
     * 答案 (填空题存正确答案, 简答题/编程题存参考答案)
     * 数据库字段: answer
     */
    private String answer;

    /**
     * 题目解析
     */
    private String analysis;

    /**
     * 难度: 1-简单 2-中等 3-困难 4-很难 5-极难
     */
    private Integer difficulty;

    /**
     * 创建人
     */
    private Long creatorId;

    /**
     * 状态:0禁用,1正常
     */
    private Integer status;

    // ========== 兼容性方法 (Service层使用新命名) ==========

    /**
     * 获取题型 (兼容Service层)
     */
    public Integer getQuestionType() {
        return this.type;
    }

    /**
     * 设置题型 (兼容Service层)
     */
    public void setQuestionType(Integer questionType) {
        this.type = questionType;
    }

    /**
     * 获取正确答案 (兼容Service层)
     */
    public String getCorrectAnswer() {
        return this.answer;
    }

    /**
     * 设置正确答案 (兼容Service层)
     */
    public void setCorrectAnswer(String correctAnswer) {
        this.answer = correctAnswer;
    }

    /**
     * 获取参考答案 (兼容Service层)
     */
    public String getReferenceAnswer() {
        return this.answer;
    }

    /**
     * 设置参考答案 (兼容Service层)
     */
    public void setReferenceAnswer(String referenceAnswer) {
        this.answer = referenceAnswer;
    }

    /**
     * 设置分数 (数据库暂无此字段,仅用于兼容)
     */
    public void setScore(Double score) {
        // 数据库暂无score字段,忽略
    }

    /**
     * 获取分数 (数据库暂无此字段,仅用于兼容)
     */
    public Double getScore() {
        return null;
    }
}
