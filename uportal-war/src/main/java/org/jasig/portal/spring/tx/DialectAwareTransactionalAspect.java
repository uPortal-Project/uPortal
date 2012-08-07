package org.jasig.portal.spring.tx;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jasig.portal.concurrency.caching.RequestCache;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DialectAwareTransactionalAspect {


    
    @Pointcut(value="execution(public * *(..))")
    public void anyPublicMethod() { }
    
    @Around("anyPublicMethod() && @annotation(requestCache)")
    public Object dialectAwareTransactional(ProceedingJoinPoint pjp, RequestCache requestCache) throws Throwable {
        
    }
}
