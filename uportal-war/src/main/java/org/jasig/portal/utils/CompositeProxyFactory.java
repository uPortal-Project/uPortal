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
package org.jasig.portal.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Sets;

/**
 * Creates a proxy that is the composite of two objects. The composite proxy implements all interfaces of both
 * objects and attempts to delegate all method calls to the specified proxy object first and if that invocation
 * fails due to an {@link AbstractMethodError} the target object is invoked instead.
 * 
 * @author Eric Dalquist
 */
@SuppressWarnings("rawtypes")
public class CompositeProxyFactory {
    private static final ConcurrentMap<Class<?>, ImmutableSet<Method>> IMPL_METHOD_CACHE = new ConcurrentHashMap<Class<?>, ImmutableSet<Method>>();
    private static final ConcurrentMap<Class<?>, ImmutableSet<Class>> INTERFACES_CACHE = new ConcurrentHashMap<Class<?>, ImmutableSet<Class>>();
    
    /**
     * Create a proxy that implements all interfaces implemented by proxy and target. For every method invocation
     * on the compositeProxy if the proxy object implements the method it is invoked on the proxy object. If not it
     * is invoked on the target object.
     */
    @SuppressWarnings("unchecked")
    public static < T, P extends T> P createCompositeProxy(P proxy, T target) {
        final ProxyFactory proxyFactory = new ProxyFactory(target);
        
        final Class<?>[] interfaces = getInterfaces(proxy.getClass(), target.getClass());
        proxyFactory.setInterfaces(interfaces);
        proxyFactory.addAdvice(new CompositeProxyInterceptor(proxy));
        
        return (P)proxyFactory.getProxy();
    }
    
    /**
     * Get set of all interfaces implemented by the class
     */
    private static ImmutableSet<Class> getInterfaces(Class<?> c) {
        ImmutableSet<Class> interfaces = INTERFACES_CACHE.get(c);
        if (interfaces != null) {
            return interfaces;
        }
        
        interfaces = ImmutableSet.copyOf(ClassUtils.getAllInterfacesForClassAsSet(c));
        INTERFACES_CACHE.put(c, interfaces);
        
        return interfaces;
    }
    
    /**
     * The union of all interfaces implemented by the two classes.
     */
    private static Class[] getInterfaces(Class<?> proxyClass, Class<?> targetClass) {
        final Set<Class> proxyInterfaces = getInterfaces(proxyClass);
        final Set<Class> targetInterfaces = getInterfaces(targetClass);
        
        //TODO does this need caching?
        final ArrayList<Class> allInterfaces = new ArrayList<Class>(proxyInterfaces.size() + targetInterfaces.size());
        allInterfaces.addAll(proxyInterfaces);
        for (final Class c : targetInterfaces) {
            if (!proxyInterfaces.contains(c)) {
                allInterfaces.add(c);
            }
        }

        return allInterfaces.toArray(new Class[allInterfaces.size()]);
    }
    
    /**
     * Get the set of methods that the specified class implements
     */
    private static ImmutableSet<Method> getMethods(Class<?> c) {
        ImmutableSet<Method> methods = IMPL_METHOD_CACHE.get(c);
        if (methods != null) {
            return methods;
        }
        
        //The cached Method set should only be modified a few times
        final Builder<Method> methodsBuilder = ImmutableSet.builder();
        
        final Set<Class> interfaces = getInterfaces(c);
        for (final Class<?> interfaceClass : interfaces) {
            for (final Method method : interfaceClass.getMethods()) {
                methodsBuilder.add(method);
            }
        }
        for (final Method method : ReflectionUtils.getAllDeclaredMethods(c)) {
            methodsBuilder.add(method);
        }
        
        methods = methodsBuilder.build();
        IMPL_METHOD_CACHE.putIfAbsent(c, methods);
        
        return methods;
    }
    
    /**
     * Intercepter that handles the fall-back proxy logic for each invocation
     */
    private static final class CompositeProxyInterceptor implements MethodInterceptor {
        private ImmutableSet<Method> proxyMethods;
        private final Object proxy;

        public CompositeProxyInterceptor(Object proxy) {
            this.proxy = proxy;
            this.proxyMethods = getMethods(proxy.getClass());
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            final Method method = invocation.getMethod();
            
            //If the proxy target implements the specified method call it, if not delegate to the target
            if (this.proxyMethods.contains(method)) {
                final Object[] args = invocation.getArguments();
                try {
                    return method.invoke(proxy, args);
                }
                catch (InvocationTargetException e) {
                    if (e.getCause() instanceof AbstractMethodError) {
                        //remove method from proxyMethods via filtering, essentially mimics a CopyOnWrite Set
                        proxyMethods = ImmutableSet.copyOf(Sets.filter(proxyMethods, new Predicate<Method>() {
                            public boolean apply(Method input) {
                                return !method.equals(input);
                            }
                        }));
                        
                        //Update the method cache
                        IMPL_METHOD_CACHE.put(proxy.getClass(), proxyMethods);
                    }
                    else {
                        throw e;
                    }
                }
            }
                
            //fall back on calling the target object directly
            return invocation.proceed();
        }
    }
}
