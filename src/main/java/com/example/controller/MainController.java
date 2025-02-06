package com.example.controller;

import com.example.common.BaseResponse;
import com.example.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 主控制类
 *
 * @author WanAn
 */
@RestController
@RequestMapping("/")
public class MainController {
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<String> health(){
        return ResultUtils.success("server is running");
    }
}
