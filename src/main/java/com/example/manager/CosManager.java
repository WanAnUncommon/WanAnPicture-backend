package com.example.manager;

import cn.hutool.core.io.FileUtil;
import com.example.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * COS 对象存储服务
 *
 * @author WanAn
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
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传图片文件
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        PicOperations picOperations = new PicOperations();
        // 设置图片处理参数，返回图片原本信息
        picOperations.setIsPicInfo(1);
        // 压缩图片成webp格式
        String webpKey= FileUtil.mainName(key)+".webp";
        List<PicOperations.Rule> rules=new ArrayList<>();
        PicOperations.Rule rule = new PicOperations.Rule();
        rule.setBucket(cosClientConfig.getBucket());
        rule.setFileId(webpKey);
        rule.setRule("imageMogr2/format/webp");
        rules.add(rule);
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 获取文件
     *
     * @param filePath 文件路径
     */
    public COSObject getObject(String filePath) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), filePath);
        return cosClient.getObject(getObjectRequest);
    }
}
