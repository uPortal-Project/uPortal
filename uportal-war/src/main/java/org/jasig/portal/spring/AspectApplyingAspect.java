package org.jasig.portal.spring;

import java.util.Collections;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.OrderComparator;

/**
 * Apply AOP Aspect(s) to the object returned by the invoked method.
 * 
 * @author Eric Dalquist
 */
public class AspectApplyingAspect {
    private List<Advice> advices;
    
    @Required
    public void setAdvices(List<Advice> advices) {
        this.advices = advices;
        Collections.sort(this.advices, OrderComparator.INSTANCE);
    }

    public Object applyAspect(ProceedingJoinPoint pjp) throws Throwable {
        final Object result = pjp.proceed();
        if (result == null) {
            return result;
        }
        
        final AspectJProxyFactory portletPreferencesProxyFactory = new AspectJProxyFactory(result);
        
        for (final Advice advice : this.advices) {
            portletPreferencesProxyFactory.addAdvice(advice);
        }
        
        return portletPreferencesProxyFactory.getProxy();
    }
}
