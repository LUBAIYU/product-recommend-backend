package com.lzh.recommend.aop;

import com.lzh.recommend.constant.UserConsts;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.model.vo.UserVo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 自定义切面类
 *
 * @author by
 */
@Aspect
@Component
public class AuthAspect {

    /**
     * 校验管理员权限
     *
     * @param joinPoint 连接点
     */
    @Around("@annotation(com.lzh.recommend.annotation.MustAdmin)")
    public Object doAuth(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取请求对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取用户登录态
        Object object = request.getSession().getAttribute(UserConsts.USER_LOGIN_STATE);
        UserVo userVo = (UserVo) object;
        //判断是否为空
        if (userVo == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //判断是否为管理员
        Integer role = userVo.getRole();
        if (role == 1) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //放行
        return joinPoint.proceed();
    }
}
