package com.edu.platform.report.constant;

/**
 * 素养等级枚举
 *
 * @author Education Platform
 */
public enum ProfileLevelEnum {
    
    /**
     * 优秀
     */
    EXCELLENT("优秀", 90),
    
    /**
     * 良好
     */
    GOOD("良好", 80),
    
    /**
     * 合格
     */
    QUALIFIED("合格", 60),
    
    /**
     * 待提升
     */
    TO_IMPROVE("待提升", 0);
    
    private final String level;
    private final int minScore;
    
    ProfileLevelEnum(String level, int minScore) {
        this.level = level;
        this.minScore = minScore;
    }
    
    public String getLevel() {
        return level;
    }
    
    public int getMinScore() {
        return minScore;
    }
    
    /**
     * 根据分数获取等级
     */
    public static String getLevelByScore(double score) {
        if (score >= EXCELLENT.minScore) {
            return EXCELLENT.level;
        } else if (score >= GOOD.minScore) {
            return GOOD.level;
        } else if (score >= QUALIFIED.minScore) {
            return QUALIFIED.level;
        } else {
            return TO_IMPROVE.level;
        }
    }
    
}
