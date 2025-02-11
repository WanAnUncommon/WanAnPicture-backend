package com.example.controller;

import com.example.annotation.AuthCheck;
import com.example.common.BaseResponse;
import com.example.common.ResultUtils;
import com.example.constant.UserConstant;
import com.example.exception.BusinessException;
import com.example.exception.ErrorCode;
import com.example.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * 文件Controller
 *
 * @author WanAn
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
    @Resource
    private CosManager cosManager;

    /**
     * 测试文件上传
     *
     * @param multipartFile 文件
     * @return 文件url
     */
    @PostMapping("/test/upload")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<String> testUpload(@RequestPart("file") MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        String path = String.format("/test/%s", filename);
        File tempFile = null;
        try {
            tempFile = File.createTempFile(path, null);
            multipartFile.transferTo(tempFile);
            cosManager.putObject(path, tempFile);
            return ResultUtils.success(path);
        } catch (Exception e) {
            log.error("上传文件失败,path={}", path, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
        } finally {
            // 删除临时文件
            if (tempFile != null) {
                boolean delete = tempFile.delete();
                if (!delete) {
                    log.error("删除临时文件失败,path={}", path);
                }
            }
        }
    }

    /**
     * 测试文件下载
     *
     * @param filePath 文件路径
     */
    @GetMapping("/test/get")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public void testGet(String filePath, HttpServletResponse httpServletResponse) throws IOException {
        COSObjectInputStream inputStream = null;
        try {
            COSObject cosObject = cosManager.getObject(filePath);
            inputStream = cosObject.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(inputStream);
            httpServletResponse.setContentType("application/octet-stream;charset=UTF-8");
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=" + filePath);
            httpServletResponse.getOutputStream().write(bytes);
            httpServletResponse.getOutputStream().flush();
        } catch (Exception e) {
            log.error("下载文件失败,path={}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载文件失败");
        } finally {
            // 释放流
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
