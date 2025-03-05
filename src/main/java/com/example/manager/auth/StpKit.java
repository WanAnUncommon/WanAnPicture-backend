package com.example.manager.auth;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * StpLogic门面类
 *
 * @author WanAn
 */
@Component
public class StpKit {
    public static final String SPACE_TYPE="space";

    // 默认会话对象
    public static final StpLogic DEFAULT = StpUtil.stpLogic;

    // space会话对象
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);
}
