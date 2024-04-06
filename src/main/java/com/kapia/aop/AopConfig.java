package com.kapia.aop;

import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AopConfig {

    @Value("${logging.level.ServicePerformance.override:${logging.level.ServicePerformance.default}}")
    private String loggingLevel;

    @Pointcut("execution(* com.kapia.ocr.OCRService.processImage(..))")
    public void monitorService() {
    }

    @Bean
    public ServicePerformanceMonitorInterceptor servicePerformanceMonitorInterceptor() {
        ServicePerformanceMonitorInterceptor interceptor = new ServicePerformanceMonitorInterceptor();
        interceptor.setLoggingLevel(loggingLevel);
        return new ServicePerformanceMonitorInterceptor();
    }

    @Bean
    public Advisor performanceMonitorAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("com.kapia.aop.AopConfig.monitorService()");
        return new DefaultPointcutAdvisor(pointcut, servicePerformanceMonitorInterceptor());
    }

}
