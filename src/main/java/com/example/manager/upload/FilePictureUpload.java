package com.example.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 文件图片上传
 *
 * @author WanAn
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File tempFile) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(tempFile);
    }

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAM_ERROR, "上传文件为空");
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(multipartFile.getSize() > ONE_MB * 3, ErrorCode.PARAM_ERROR, "上传文件超过了3MB");
        // 校验文件后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAM_ERROR, "上传文件格式不正确");
    }
}
