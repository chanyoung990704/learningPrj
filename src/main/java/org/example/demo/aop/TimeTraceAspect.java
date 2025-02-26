package org.example.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Component
@Aspect
public class TimeTraceAspect {

    @Pointcut("@annotation(org.example.demo.aop.TimeTrace)")
    private void timeTracePointcut() {}


    @Around("timeTracePointcut()")
    public Object timeTrace(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            return joinPoint.proceed();
        }finally {
            stopWatch.stop();
            log.info("{} - {} - Total time = {}s",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    stopWatch.getTotalTimeSeconds());
        }
    }
}
