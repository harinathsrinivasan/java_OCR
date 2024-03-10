package kapia.dev.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/*

    @AspectJ refers to a style of declaring aspects as
    regular Java classes annotated with annotations.
    The @AspectJ style was introduced by the AspectJ project as part of the AspectJ 5 release.
    Spring interprets the same annotations as AspectJ 5,
    using a library supplied by AspectJ for pointcut parsing and matching.
    The AOP runtime is still pure Spring AOP, though,
    and there is no dependency on the AspectJ compiler or weaver.

    You can register aspect classes as regular beans in your Spring XML configuration,
    via @Bean methods in @Configuration classes,
    or have Spring autodetect them through classpath scanning -
    the same as any other Spring-managed bean.
    However, note that the @Aspect annotation is not sufficient for
    autodetection in the classpath.
    For that purpose, you need to add a separate @Component annotation (or, alternatively,
    a custom stereotype annotation that qualifies, as per the rules of Spring’s component scanner).

 */

@Aspect
@Component
public class ControllerLoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerLoggingAspect.class);

    @Around("execution(* kapia.dev.ocr.OCRController.processImage(..))")
    public Object profileAllMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();

        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();

        final StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        Object result = proceedingJoinPoint.proceed();
        stopWatch.stop();

        LOGGER.info("Execution time of " + className + "." + methodName + " :: " + stopWatch.getTotalTimeMillis() + " ms");

        return result;
    }

}
