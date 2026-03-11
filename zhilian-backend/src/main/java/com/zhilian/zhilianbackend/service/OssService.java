package com.zhilian.zhilianbackend.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:23
 * @Description: OSS 文件服务接口，提供文件上传、删除等对外能力
 **/
public interface OssService {

    /**
     * 上传文件到阿里云OSS
     *
     * @param file 要上传的文件（MultipartFile格式）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file);

    /**
     * 从OSS删除文件
     *
     * @param fileUrl 要删除的文件URL或文件名
     * @return true-删除成功 false-删除失败
     */
    boolean deleteFile(String fileUrl);
}