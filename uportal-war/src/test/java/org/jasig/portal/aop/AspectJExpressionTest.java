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

package org.jasig.portal.aop;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.SingletonAspectInstanceFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ReflectionUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:aopTestContext.xml")
public class AspectJExpressionTest {
    @Autowired
    private TestAspect testAspect;
    
    @Autowired
    private RepositoryPointcutInterface repositoryPointcutInterface;
    
    @Autowired
    private CountingMethodInterceptor countingMethodInterceptor;
    
    @Resource(name="allExecutions")
    private AspectJExpressionPointcut repositoryPointcutInterfaceExecutionPointcut;
    
    @Resource(name="countingMethodInterceptorRepositoryAdvice")
    private Advice countingMethodInterceptorRepositoryAdvice;
    
    

    @Before
    public void setup() {
        testAspect.resetCallCount();
        countingMethodInterceptor.resetCount();
    }
    
    @Test
    public void testRepositoryPointcut() {
        assertEquals(0, testAspect.getCallCount());
        final String r1 = this.repositoryPointcutInterface.methodOne("test");
        assertEquals(1, testAspect.getCallCount());
        final String r2 = this.repositoryPointcutInterface.methodTwo("test");
        assertEquals(2, testAspect.getCallCount());
    }
    
    @Test
    public void testSpringAopPointcut() {
        assertEquals(0, countingMethodInterceptor.getCount());
        this.repositoryPointcutInterface.methodOne("test");
        assertEquals(1, countingMethodInterceptor.getCount());
        this.repositoryPointcutInterface.methodOne("test");
        assertEquals(2, countingMethodInterceptor.getCount());
    }
    
    @Test
    public void testProgramaticPointcut() {
        final RepositoryPointcutInterface targetPointcutInterface = new RepositoryPointcutInterfaceImpl();
        
        final AspectJProxyFactory portletPreferencesProxyFactory = new AspectJProxyFactory(targetPointcutInterface);
        
        final Method interceptorMethod = ReflectionUtils.findMethod(CountingMethodInterceptor.class, "countInvocation", ProceedingJoinPoint.class);
        final AspectJAroundAdvice aspectJAroundAdvice = new AspectJAroundAdvice(
                interceptorMethod,
                repositoryPointcutInterfaceExecutionPointcut,
                new SingletonAspectInstanceFactory(this.countingMethodInterceptor));
        
        portletPreferencesProxyFactory.addAdvice(aspectJAroundAdvice);

        final RepositoryPointcutInterface proxiedPointcutInterface = (RepositoryPointcutInterface) portletPreferencesProxyFactory.getProxy();

        assertEquals(0, countingMethodInterceptor.getCount());
        proxiedPointcutInterface.methodOne("test");
        assertEquals(1, countingMethodInterceptor.getCount());
        proxiedPointcutInterface.methodOne("test");
        assertEquals(2, countingMethodInterceptor.getCount());
    }
    
    @Test
    public void testProgramaticPointcut2() {
        final RepositoryPointcutInterface targetPointcutInterface = new RepositoryPointcutInterfaceImpl();
        
        final AspectJProxyFactory portletPreferencesProxyFactory = new AspectJProxyFactory(targetPointcutInterface);
        portletPreferencesProxyFactory.addAdvice(countingMethodInterceptorRepositoryAdvice);
        final RepositoryPointcutInterface proxiedPointcutInterface = (RepositoryPointcutInterface) portletPreferencesProxyFactory.getProxy();

        assertEquals(0, countingMethodInterceptor.getCount());
        proxiedPointcutInterface.methodOne("test");
        assertEquals(1, countingMethodInterceptor.getCount());
        proxiedPointcutInterface.methodOne("test");
        assertEquals(2, countingMethodInterceptor.getCount());
    }
    
    
}
