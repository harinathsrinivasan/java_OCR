package kapia.dev.aop;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AbstractMonitoringInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Date;

@Aspect
@Component
public class ServicePerformanceMonitorInterceptor extends AbstractMonitoringInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicePerformanceMonitorInterceptor.class);

    private static String loggingLevel = "INFO";

    public ServicePerformanceMonitorInterceptor() {
    }

    public ServicePerformanceMonitorInterceptor(boolean useDynamicLogger) {
        setUseDynamicLogger(useDynamicLogger);
    }

    @Override
    protected Object invokeUnderTrace(MethodInvocation invocation, Log log) throws Throwable {

        String methodName = invocation.getMethod().getName();
        String className = invocation.getMethod().getDeclaringClass().getSimpleName();

        final StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        if (loggingLevel.equals("DEBUG") || loggingLevel.equals("TRACE"))
            LOGGER.debug("Execution of method " + className + "." + methodName + " started at:" + new Date());

        try {
            return invocation.proceed();
        } finally {
            stopWatch.stop();
            long time = stopWatch.getTotalTimeMillis();
            if (loggingLevel.equals("INFO") || loggingLevel.equals("DEBUG") || loggingLevel.equals("TRACE"))
                LOGGER.info("Execution time of " + className + "." + methodName + " :: " + time + " ms");
            if (loggingLevel.equals("DEBUG") || loggingLevel.equals("TRACE"))
                LOGGER.debug("Execution of method " + className + "." + methodName + " ended at: " + new Date() + " and took " + time + " ms");
            if (time > 1000 && !loggingLevel.equals("ERROR"))
                LOGGER.warn("Method execution longer than 1000 ms!");
        }
    }

    public void setLoggingLevel(String loggingLevel) {
        this.loggingLevel = loggingLevel.toUpperCase().trim();
    }
}
