package com.edu.platform.course.scheduler;

import com.edu.platform.course.entity.Course;
import com.edu.platform.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 课程定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseScheduler {

    private final CourseService courseService;

    /**
     * 每小时整点执行：将已过结束时间的"进行中"课程自动归档
     * endTime 为空表示永不结束，不处理
     */
    @Scheduled(cron = "0 0 * * * *")
    public void autoArchiveExpiredCourses() {
        log.info("[CourseScheduler] 开始检查过期课程，时间: {}", LocalDateTime.now());

        courseService.lambdaUpdate()
                .eq(Course::getStatus, 1)
                .isNotNull(Course::getEndTime)
                .lt(Course::getEndTime, LocalDateTime.now())
                .set(Course::getStatus, 2)
                .update();

        log.info("[CourseScheduler] 过期课程自动归档完成");
    }
}
