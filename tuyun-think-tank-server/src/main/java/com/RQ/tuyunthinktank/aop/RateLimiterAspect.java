package com.RQ.tuyunthinktank.aop;

import com.RQ.tuyunthinktank.annotation.RateLimiter;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面实现
 */
@Aspect
@Component
public class RateLimiterAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(rateLimiter)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        String key = rateLimiter.key();
        int time = rateLimiter.time();
        int count = rateLimiter.count();

        // 获取当前请求
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        
        // 获取 IP 地址
        String ipAddr = getIpAddress(request);
        
        // 组合 Key: rate_limit:key:ip
        String redisKey = "rate_limit:" + key + ":" + ipAddr;
        
        // 计数
        Long increment = stringRedisTemplate.opsForValue().increment(redisKey);
        
        // 如果是第一次访问，设置过期时间
        if (increment != null && increment == 1) {
            stringRedisTemplate.expire(redisKey, time, TimeUnit.SECONDS);
        }
        
        // 如果超过限制，抛出异常
        if (increment != null && increment > count) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求过于频繁，请稍后再试");
        }
        
        return joinPoint.proceed();
    }
    
    /**
     * 获取 IP 地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
