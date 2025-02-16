package com.example.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.common.ResultUtils;
import com.example.config.CosClientConfig;
import com.example.exception.BusinessException;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import com.example.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 文件存储服务
 * 已废弃，请使用 {@link com.example.manager.upload.PictureUploadTemplate}
 *
 * @author WanAn
 */
@Service
@Slf4j
@Deprecated
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传路径前缀
     * @return 文件信息
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        validPicture(multipartFile);
        // 构建文件名及上传路径
        String originalFilename = multipartFile.getOriginalFilename();
        String uuid = RandomUtil.randomString(16);
        String date = DateUtil.formatDate(new Date());
        String uploadFileName = String.format("%s_%s.%s",date, uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);
        // 上传文件
        File tempFile = null;
        try {
            tempFile = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(tempFile);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, tempFile);
            // 构建文件信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double scale = NumberUtil.round((1.0 * width) / height, 2).doubleValue();
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl("https://"+cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(tempFile));
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicScale(scale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("上传文件失败,path={}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
        } finally {
            // 删除临时文件
            deleteTemFile(tempFile);
        }
    }

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

    /**
     * 校验图片文件
     *
     * @param multipartFile 文件
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAM_ERROR, "上传文件为空");
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(multipartFile.getSize() > ONE_MB * 3, ErrorCode.PARAM_ERROR, "上传文件超过了3MB");
        // 校验文件后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAM_ERROR, "上传文件格式不正确");
    }
}
