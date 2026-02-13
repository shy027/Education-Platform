package com.edu.platform.report.service;

/**
 * OSS文件服务接口
 *
 * @author Education Platform
 */
public interface OssFileService {
    
    /**
     * 上传PDF文件到OSS
     *
     * @param pdfBytes PDF字节数组
     * @param fileName 文件名
     * @param courseId 课程ID
     * @return OSS文件URL
     */
    String uploadPdf(byte[] pdfBytes, String fileName, Long courseId);
    
    /**
     * 删除OSS文件
     *
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
    
    /**
     * 生成预签名下载URL(有效期1小时)
     *
     * @param fileUrl 文件URL
     * @return 预签名URL
     */
    String generatePresignedUrl(String fileUrl);
}
