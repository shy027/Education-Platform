package com.edu.platform.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.edu.platform.course.dto.request.AnnouncementCreateRequest;
import com.edu.platform.course.dto.request.AnnouncementQueryRequest;
import com.edu.platform.course.dto.request.AnnouncementUpdateRequest;
import com.edu.platform.course.dto.response.AnnouncementResponse;
import com.edu.platform.course.entity.CourseAnnouncement;

/**
 * 公告服务接口
 */
public interface AnnouncementService extends IService<CourseAnnouncement> {

    /**
     * 创建公告
     *
     * @param request 创建请求
     * @return 公告ID
     */
    Long createAnnouncement(AnnouncementCreateRequest request);

    /**
     * 更新公告
     *
     * @param request 更新请求
     */
    void updateAnnouncement(AnnouncementUpdateRequest request);

    /**
     * 删除公告
     *
     * @param courseId 课程ID
     * @param id       公告ID
     */
    void deleteAnnouncement(Long courseId, Long id);

    /**
     * 获取公告详情
     *
     * @param courseId 课程ID
     * @param id       公告ID
     * @return 公告详情
     */
    AnnouncementResponse getAnnouncementDetail(Long courseId, Long id);

    /**
     * 分页查询公告
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<AnnouncementResponse> pageAnnouncements(AnnouncementQueryRequest request);

    /**
     * 置顶/取消置顶公告
     *
     * @param courseId 课程ID
     * @param id       公告ID
     * @param isTop    是否置顶
     */
    void toggleTop(Long courseId, Long id, Integer isTop);
}
