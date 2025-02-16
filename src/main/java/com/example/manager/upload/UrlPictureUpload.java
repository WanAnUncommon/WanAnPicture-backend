package com.example.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.example.exception.BusinessException;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * url图片上传
 *
 * @author WanAn
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void processFile(Object inputSource, File tempFile) {
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, tempFile);
    }

    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 非空校验
        ThrowUtils.throwIf(CharSequenceUtil.isBlank(fileUrl), ErrorCode.PARAM_ERROR, "url为空");
        // url格式校验
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "url格式错误");
        }
        // 校验协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"), ErrorCode.PARAM_ERROR, "url协议错误");
        // 发送HEAD请求校验文件元信息
        try (HttpResponse httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();) {
            // 未正常返回
            if (!httpResponse.isOk()) {
                return;
            }
            // 校验文件类型
            String contentType = httpResponse.header("Content-Type");
            if (CharSequenceUtil.isNotBlank(contentType)) {
                final List<String> ALLOW_FORMAT_LIST = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                if (!ALLOW_FORMAT_LIST.contains(contentType.toLowerCase())) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的文件格式");
                }
            }
            // 校验文件大小
            String contentLengthStr = httpResponse.header("Content-Length");
            if (CharSequenceUtil.isNotBlank(contentLengthStr)) {
                final long ONE_MB = 1024 * 1024;
                long contentLength = 0;
                try {
                    contentLength = Long.parseLong(contentLengthStr);
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "文件大小错误");
                }
                if (contentLength > ONE_MB * 3) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "上传文件超过了3MB");
                }
            }
        }
    }
}
