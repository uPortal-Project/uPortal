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
