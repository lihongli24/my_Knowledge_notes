package com.demo.li.spring.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author lihongli
 * create：2020/4/18 11:21 下午
 */
@Aspect
@Component
public class OrderServiceAop {
    @Pointcut("execution (* com.demo.li.spring.ioc.*.* (..))")
    public void pointCut(){

    }

    @Before("pointCut()")
    public void before(){

    }
}
