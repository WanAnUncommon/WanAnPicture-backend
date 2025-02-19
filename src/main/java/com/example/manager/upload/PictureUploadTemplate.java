package com.example.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.config.CosClientConfig;
import com.example.exception.BusinessException;
import com.example.exception.ErrorCode;
import com.example.manager.CosManager;
import com.example.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 文件存储服务模版
 *
 * @author WanAn
 */
@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param inputSource    文件输入源
     * @param uploadPathPrefix 上传路径前缀
     * @return 文件信息
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        validPicture(inputSource);
        // 构建文件名及上传路径
        String originalFilename = getOriginalFilename(inputSource);
        String uuid = RandomUtil.randomString(16);
        String date = DateUtil.formatDate(new Date());
        String uploadFileName = String.format("%s_%s.%s", date, uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);
        // 上传文件
        File tempFile = null;
        try {
            tempFile = File.createTempFile(uploadPath, null);
            // 处理文件
            processFile(inputSource, tempFile);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, tempFile);
            // 获取图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)){
                // 获取压缩后的图片
                CIObject compressedCiObject = objectList.get(0);
                // 封装压缩图片返回结果
                return buildResult(originalFilename, compressedCiObject);
            }
            // 构建文件信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            return buildResult(originalFilename, uploadPath, tempFile, imageInfo);
        } catch (Exception e) {
            log.error("上传文件失败,path={}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
        } finally {
            // 删除临时文件
            deleteTemFile(tempFile);
        }
    }

    /**
     * 构建压缩文件信息结果
     *
     * @param originalFilename 文件原始名称
     * @param compressedCiObject 压缩后的图片
     * @return 文件信息
     */
    private UploadPictureResult buildResult(String originalFilename, CIObject compressedCiObject) {
        int width = compressedCiObject.getWidth();
        int height = compressedCiObject.getHeight();
        double scale = NumberUtil.round((1.0 * width) / height, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl("https://" + cosClientConfig.getHost() + "/" + compressedCiObject);
        uploadPictureResult.setName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(width);
        uploadPictureResult.setPicHeight(height);
        uploadPictureResult.setPicScale(scale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        return uploadPictureResult;
    }

    /**
     * 构建文件信息结果
     *
     * @param originalFilename 文件原始名称
     * @param uploadPath       上传路径
     * @param tempFile         临时文件
     * @param imageInfo        图片信息
     * @return 文件信息
     */
    private UploadPictureResult buildResult(String originalFilename, String uploadPath, File tempFile, ImageInfo imageInfo) {
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();
        double scale = NumberUtil.round((1.0 * width) / height, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl("https://" + cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(tempFile));
        uploadPictureResult.setPicWidth(width);
        uploadPictureResult.setPicHeight(height);
        uploadPictureResult.setPicScale(scale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        return uploadPictureResult;
    }

    /**
     * 获取文件原始名称
     *
     * @param inputSource 文件输入源
     * @return 文件原始名称
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 处理文件并生成本地临时文件
     *
     * @param inputSource 文件输入源
     * @param tempFile    临时文件
     */
    protected abstract void processFile(Object inputSource, File tempFile) throws IOException;

    /**
     * 校验图片
     *
     * @param inputSource 文件输入源
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 删除临时文件
     *
     * @param tempFile 临时文件
     */
    private static void deleteTemFile(File tempFile) {
        if (tempFile != null) {
            boolean delete = tempFile.delete();
            if (!delete) {
                log.error("删除临时文件失败,path={}", tempFile.getAbsolutePath());
            }
        }
    }
}
