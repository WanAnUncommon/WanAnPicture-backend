package com.example.manager;

import com.example.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * COS 对象存储服务
 *
 * @author Wanwan
 */
@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传文件
     *
     * @param key 唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 获取文件
     *
     * @param filePath 文件路径
     */
    public COSObject getObject(String filePath){
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), filePath);
        return cosClient.getObject(getObjectRequest);
    }
}
