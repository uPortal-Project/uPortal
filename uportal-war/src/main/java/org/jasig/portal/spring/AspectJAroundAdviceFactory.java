package org.jasig.portal.spring;

import java.lang.reflect.Method;

import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.SingletonAspectInstanceFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.ReflectionUtils;

/**
 * Creates AspectJAroundAdvice while hiding a bit of the boilerplate
 * 
 * @author Eric Dalquist
 */
public class AspectJAroundAdviceFactory extends AbstractFactoryBean<AspectJAroundAdvice> {
    private Object aspect;
    private String method;
    private Class<?>[] args = new Class<?>[0];
    private AspectJExpressionPointcut pointcut;
    
    @Required
    public void setAspect(Object aspect) {
        this.aspect = aspect;
    }

    @Required
    public void setMethod(String method) {
        this.method = method;
    }

    @Required
    public void setArgs(Class<?>[] args) {
        this.args = args;
    }

    @Required
    public void setPointcut(AspectJExpressionPointcut pointcut) {
        this.pointcut = pointcut;
    }

    @Override
    public Class<?> getObjectType() {
        return AspectJAroundAdvice.class;
    }

    @Override
    protected AspectJAroundAdvice createInstance() throws Exception {
        final Class<? extends Object> aspectType = this.aspect.getClass();
        final Method method = ReflectionUtils.findMethod(aspectType, this.method, this.args);
        final SingletonAspectInstanceFactory aif = new SingletonAspectInstanceFactory(this.aspect);
        return new AspectJAroundAdvice(method, pointcut, aif);
    }
}
