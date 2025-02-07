package com.example.aop;

import com.example.annotation.AuthCheck;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import com.example.model.entity.User;
import com.example.model.enums.UserRoleEnum;
import com.example.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验切面
 *
 * @author WanAn
 */
@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 切点
     * @param authCheck 权限校验注解
     * @return Object
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取当前登录用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        User loginUser = userService.getLoginUser(request);
        // 权限校验
        String mustRole = authCheck.mustRole();
        // 不需要权限，放行
        if (mustRole == null) {
            joinPoint.proceed();
        }
        String userRole = loginUser.getUserRole();
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(userRole);
        ThrowUtils.throwIf(userRoleEnum == null, ErrorCode.NO_AUTH);
        // 需要admin，但是当前用户不是admin
        ThrowUtils.throwIf(mustRole.equals(UserRoleEnum.ADMIN.getValue())
                && !userRoleEnum.equals(UserRoleEnum.ADMIN), ErrorCode.NO_AUTH);
        return joinPoint.proceed();
    }
}
