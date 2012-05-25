package org.jasig.portal.aop;

import org.aspectj.lang.ProceedingJoinPoint;

public class CountingMethodInterceptor {
    private int count = 0;
    
    public int getCount() {
        return count;
    }
    
    public void resetCount() {
        count = 0;
    }

    public Object countInvocation(ProceedingJoinPoint pjp) throws Throwable {
        count++;
        return pjp.proceed();
    }
}
