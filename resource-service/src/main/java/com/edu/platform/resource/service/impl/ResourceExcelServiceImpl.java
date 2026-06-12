package com.edu.platform.resource.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.platform.common.exception.BusinessException;
import com.edu.platform.resource.dto.excel.ResourceImportExcelDTO;
import com.edu.platform.resource.entity.Resource;
import com.edu.platform.resource.entity.ResourceCategory;
import com.edu.platform.resource.entity.ResourceTag;
import com.edu.platform.resource.entity.ResourceTagRelation;
import com.edu.platform.resource.entity.ResourceAttachment;
import com.edu.platform.resource.mapper.ResourceAttachmentMapper;
import com.edu.platform.resource.mapper.ResourceCategoryMapper;
import com.edu.platform.resource.mapper.ResourceMapper;
import com.edu.platform.resource.mapper.ResourceTagMapper;
import com.edu.platform.resource.mapper.ResourceTagRelationMapper;
import com.edu.platform.resource.service.ResourceExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceExcelServiceImpl implements ResourceExcelService {

    private final ResourceMapper resourceMapper;
    private final ResourceCategoryMapper resourceCategoryMapper;
    private final ResourceTagMapper resourceTagMapper;
    private final ResourceTagRelationMapper resourceTagRelationMapper;
    private final ResourceAttachmentMapper resourceAttachmentMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("资源导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 获取所有分类名称
            List<String> categories = resourceCategoryMapper.selectList(null).stream()
                    .map(ResourceCategory::getCategoryName)
                    .collect(Collectors.toList());

            // 示例数据
            List<ResourceImportExcelDTO> demoData = new ArrayList<>();
            ResourceImportExcelDTO demo = new ResourceImportExcelDTO();
            demo.setTitle("示例：思政公开课");
            demo.setResourceTypeStr("视频");
            demo.setCategoryName(categories.isEmpty() ? "" : categories.get(0));
            demo.setTagNames("法治意识,理想信念");
            demo.setSummary("这是一个示例，带你了解如何导入资源。");
            demo.setCoverUrl("http://10.54.0.36/uploads/resource/cover/cover.jpg");
            demo.setFileUrl("http://10.54.0.36/uploads/resource/attachment/attachment.pdf");
            demoData.add(demo);

            EasyExcel.write(response.getOutputStream(), ResourceImportExcelDTO.class)
                    .registerWriteHandler(new DropDownWriteHandler(categories))
                    .sheet("资源导入")
                    .doWrite(demoData);

        } catch (Exception e) {
            log.error("下载资源导入模板失败", e);
            throw new BusinessException("下载模板失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importResources(MultipartFile file, Long adminId) {
        Map<String, Object> result = new HashMap<>();
        List<String> failDetails = new ArrayList<>();
        int[] counts = new int[2]; // 0: success, 1: fail

        Set<String> validTags = getValidIdeologicalTags();

        try {
            EasyExcel.read(file.getInputStream(), ResourceImportExcelDTO.class, new PageReadListener<ResourceImportExcelDTO>(dataList -> {
                for (ResourceImportExcelDTO data : dataList) {
                    processData(data, adminId, counts, failDetails, validTags);
                }
            })).sheet().doRead();
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException("读取Excel文件失败");
        }

        result.put("successCount", counts[0]);
        result.put("failCount", counts[1]);
        result.put("failDetails", failDetails);
        return result;
    }

    private Set<String> getValidIdeologicalTags() {
        Set<String> validTags = new HashSet<>();
        try {
            String configJson = stringRedisTemplate.opsForValue().get("sys_config:profile.resource_tag_weights");
            if (StrUtil.isNotBlank(configJson)) {
                if (configJson.startsWith("\"") && configJson.endsWith("\"")) {
                    configJson = objectMapper.readValue(configJson, String.class);
                }
                Map<String, Object> tagWeights = objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
                validTags.addAll(tagWeights.keySet());
            }
        } catch (Exception e) {
            log.warn("获取系统思政标签配置失败", e);
        }

        // 如果 Redis 中没有取到，使用系统默认的有效标签集合兜底
        if (validTags.isEmpty()) {
            validTags.addAll(Arrays.asList("法治意识", "心理健康", "理想信念", "社会责任", "科学素养", "文化自信", "专业理论", "技术技能", "职业道德", "工匠精神"));
        }
        return validTags;
    }

    private void processData(ResourceImportExcelDTO dto, Long adminId, int[] counts, List<String> failDetails, Set<String> validTags) {
        try {
            // 1. 必填校验
            if (StrUtil.isBlank(dto.getTitle())) {
                throw new IllegalArgumentException("资源标题不能为空");
            }
            if (StrUtil.isBlank(dto.getResourceTypeStr())) {
                throw new IllegalArgumentException("资源类型不能为空");
            }
            if (StrUtil.isBlank(dto.getTagNames())) {
                throw new IllegalArgumentException("标签不能为空");
            }
            if (StrUtil.isBlank(dto.getFileUrl())) {
                throw new IllegalArgumentException("资源链接不能为空");
            }

            // 2. 翻译资源类型
            Integer resourceType = null;
            switch (dto.getResourceTypeStr().trim()) {
                case "动画": resourceType = 1; break;
                case "视频": resourceType = 2; break;
                case "文档": resourceType = 3; break;
                case "音频": resourceType = 4; break;
                case "挂图": resourceType = 5; break;
                default: throw new IllegalArgumentException("资源类型不支持: " + dto.getResourceTypeStr());
            }

            // 3. 校验并获取分类ID (非必填)
            Long categoryId = null;
            if (StrUtil.isNotBlank(dto.getCategoryName())) {
                LambdaQueryWrapper<ResourceCategory> categoryWrapper = new LambdaQueryWrapper<>();
                categoryWrapper.eq(ResourceCategory::getCategoryName, dto.getCategoryName().trim());
                categoryWrapper.last("LIMIT 1");
                ResourceCategory category = resourceCategoryMapper.selectOne(categoryWrapper);
                if (category != null) {
                    categoryId = category.getId();
                }
            }

            // 4. 校验并获取标签ID
            List<Long> tagIds = new ArrayList<>();
            String[] tags = dto.getTagNames().split("[,，]");
            for (String tagName : tags) {
                String tName = tagName.trim();
                if (StrUtil.isNotBlank(tName) && validTags.contains(tName)) {
                    LambdaQueryWrapper<ResourceTag> tagWrapper = new LambdaQueryWrapper<>();
                    tagWrapper.eq(ResourceTag::getTagName, tName);
                    tagWrapper.last("LIMIT 1");
                    ResourceTag tag = resourceTagMapper.selectOne(tagWrapper);
                    if (tag == null) {
                        // 系统表中没有则自动创建，因为它是合法的思政标签
                        tag = new ResourceTag();
                        tag.setTagName(tName);
                        resourceTagMapper.insert(tag);
                    }
                    tagIds.add(tag.getId());
                }
            }
            
            if (tagIds.isEmpty()) {
                throw new IllegalArgumentException("未填写任何有效的系统思政标签");
            }

            // 5. 插入主表
            Resource resource = new Resource();
            resource.setTitle(dto.getTitle());
            resource.setResourceType(resourceType);
            resource.setCategoryId(categoryId);
            resource.setSummary(dto.getSummary());
            resource.setCoverUrl(dto.getCoverUrl());
            resource.setStatus(2); // 设为已发布
            resource.setCreatorId(adminId);
            resource.setCreatorType(1); // 管理员
            resource.setCreatedTime(LocalDateTime.now());
            resource.setViewCount(0);
            resource.setDownloadCount(0);
            resource.setLikeCount(0);
            resource.setCollectCount(0);
            resource.setIsDeleted(0);
            
            resourceMapper.insert(resource);

            // 6. 插入附件信息
            ResourceAttachment attachment = new ResourceAttachment();
            attachment.setResourceId(resource.getId());
            attachment.setFileUrl(dto.getFileUrl());
            attachment.setFileName(resource.getTitle());
            String fileType = "other";
            switch (resource.getResourceType()) {
                case 1: fileType = "image"; break;
                case 2: fileType = "video"; break;
                case 3: fileType = "pdf"; break;
                case 4: fileType = "audio"; break;
            }
            attachment.setFileType(fileType);
            resourceAttachmentMapper.insert(attachment);

            // 6. 插入标签关系
            for (Long tagId : tagIds) {
                ResourceTagRelation relation = new ResourceTagRelation();
                relation.setResourceId(resource.getId());
                relation.setTagId(tagId);
                resourceTagRelationMapper.insert(relation);
            }

            counts[0]++; // 成功数++
        } catch (Exception e) {
            log.error("导入资源行失败: {}", dto.getTitle(), e);
            failDetails.add(dto.getTitle() + " - 失败原因: " + e.getMessage());
            counts[1]++; // 失败数++
        }
    }

    /**
     * 下拉列表拦截器
     */
    private static class DropDownWriteHandler implements SheetWriteHandler {
        private final List<String> categories;
        private final List<String> types = Arrays.asList("动画", "视频", "文档", "音频", "挂图");

        public DropDownWriteHandler(List<String> categories) {
            this.categories = categories;
        }

        @Override
        public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
            Sheet sheet = writeSheetHolder.getSheet();
            DataValidationHelper helper = sheet.getDataValidationHelper();

            // 资源类型下拉 (列索引1)
            CellRangeAddressList typeRange = new CellRangeAddressList(1, 1000, 1, 1);
            DataValidationConstraint typeConstraint = helper.createExplicitListConstraint(types.toArray(new String[0]));
            DataValidation typeValidation = helper.createValidation(typeConstraint, typeRange);
            sheet.addValidationData(typeValidation);

            // 所属分类下拉 (列索引2)
            if (categories != null && !categories.isEmpty()) {
                // 如果分类太多可能超出 Excel 限制，最多截取 20 个
                List<String> safeCategories = categories.size() > 20 ? categories.subList(0, 20) : categories;
                CellRangeAddressList categoryRange = new CellRangeAddressList(1, 1000, 2, 2);
                DataValidationConstraint categoryConstraint = helper.createExplicitListConstraint(safeCategories.toArray(new String[0]));
                DataValidation categoryValidation = helper.createValidation(categoryConstraint, categoryRange);
                sheet.addValidationData(categoryValidation);
            }
        }
    }
}
