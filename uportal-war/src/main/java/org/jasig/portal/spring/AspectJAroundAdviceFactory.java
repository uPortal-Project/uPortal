/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
