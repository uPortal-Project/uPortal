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
package org.jasig.portal.spring.tx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.jasig.portal.hibernate.DelegatingHibernateIntegrator.HibernateConfiguration;
import org.jasig.portal.hibernate.HibernateConfigurationAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.google.common.collect.MapMaker;

public class DialectAwareTransactionInterceptor extends TransactionManagerCachingTransactionInterceptor implements HibernateConfigurationAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(DialectAwareTransactionInterceptor.class);
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Returned as the TX manager when skipping the TX. Doesn't actually do any db level TX work
     */
    private static final PlatformTransactionManager NOOP_TRANSACTION_MANAGER = new PlatformTransactionManager() {
        private Map<TransactionDefinition, DefaultTransactionStatus> statusCache = new MapMaker().weakKeys().weakValues().makeMap();
        
        protected final Logger logger = LoggerFactory.getLogger(DialectAwareTransactionInterceptor.class.getPackage().getName() + ".NoopTransactionManager");
        
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
            DefaultTransactionStatus status = statusCache.get(definition);
            if (status == null) {
                logger.debug("Creating new NOOP transaction with name [{}]: {}", definition.getName(), definition);
                status = new DefaultTransactionStatus(definition, true, false, definition.isReadOnly(), logger.isDebugEnabled(), null);
                statusCache.put(definition, status);
            }
            else {
                logger.debug("Using existing NOOP transaction with name [{}]: {}", definition.getName(), definition);
            }
            
            return status;
        }
        
        @Override
        public void commit(TransactionStatus status) throws TransactionException {
            if (status instanceof DefaultTransactionStatus) {
                final TransactionDefinition definition = (TransactionDefinition) ((DefaultTransactionStatus) status).getTransaction();
                if (statusCache.remove(definition) != null) {
                    logger.debug("Closing NOOP transaction with name [{}] after commit: {}", definition.getName(), definition);
                }
                else {
                    logger.debug("Can't commit NOOP transaction with name [{}], already closed: {}", definition.getName(), definition);
                }
            }
            else {
                logger.warn("TransactionStatus {} is not a DefaultTransactionStatus, no NOOP commit done: {}", status, status.getClass());
            }
        }
        
        @Override
        public void rollback(TransactionStatus status) throws TransactionException {
            if (status instanceof DefaultTransactionStatus) {
                final TransactionDefinition definition = (TransactionDefinition) ((DefaultTransactionStatus) status).getTransaction();
                if (statusCache.remove(definition) != null) {
                    logger.debug("Closing NOOP transaction with name [{}] after rollback: {}", definition.getName(), definition);
                }
                else {
                    logger.debug("Can't rollback NOOP transaction with name [{}], already closed: {}", definition.getName(), definition);
                }
            }
            else {
                logger.warn("TransactionStatus {} is not a DefaultTransactionStatus, no NOOP rollback done: {}", status, status.getClass());
            }
        }
    };
    
    private final Map<String, Class<? extends Dialect>> dialects = new ConcurrentHashMap<String, Class<? extends Dialect>>();
    private TransactionAttributeSource wrappedTransactionAttributeSource;
    
    @Override
    public boolean supports(String persistenceUnit) {
        return true;
    }

    @Override
    public void setConfiguration(String persistenceUnit, HibernateConfiguration hibernateConfiguration) {
        final SessionFactoryImplementor sessionFactory = hibernateConfiguration.getSessionFactory();
        this.dialects.put(persistenceUnit, sessionFactory.getDialect().getClass());
    }

    @Override
    public TransactionAttributeSource getTransactionAttributeSource() {
        TransactionAttributeSource tas = this.wrappedTransactionAttributeSource;
        if (tas == null) {
            final TransactionAttributeSource transactionAttributeSource = super.getTransactionAttributeSource();
            if (this.dialects.isEmpty()) {
                return transactionAttributeSource;
            }
            
            tas = new TransactionAttributeSourceWrapper(this.dialects, transactionAttributeSource);
            this.wrappedTransactionAttributeSource = tas;
        }
        return tas;
    }

    protected PlatformTransactionManager determineTransactionManager(TransactionAttribute txAttr) {
        if (txAttr instanceof SkipTransactionAttribute) {
            return NOOP_TRANSACTION_MANAGER;
        }
        
        return super.determineTransactionManager(txAttr);
    }
    
    private static final class TransactionAttributeSourceWrapper implements TransactionAttributeSource {
        private final Map<String, Class<? extends Dialect>> dialects;
        private final TransactionAttributeSource transactionAttributeSource;
        
        public TransactionAttributeSourceWrapper(Map<String, Class<? extends Dialect>> dialects, TransactionAttributeSource transactionAttributeSource) {
            this.dialects = dialects;
            this.transactionAttributeSource = transactionAttributeSource;
        }

        @Override
        public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
            final TransactionAttribute transactionAttribute = transactionAttributeSource.getTransactionAttribute(method, targetClass);
            
            //No dialect ignore support if transactionAttribute is null
            if (transactionAttribute == null) {
                return transactionAttribute;
            }
            
            final DialectAwareTransactional ann = getDialectAwareTransactionalAnnotation(method, targetClass);
            
            //No DialectAwareTransactional annotation, just return the original transactionAttribute
            if (ann == null) {
                return transactionAttribute;
            }
            
            //Check if a TX is needed for the Dialect
            final Class<? extends Dialect> dialect = determineDialect(method, transactionAttribute);
            final boolean ignored = isDialectIgnored(dialect, ann);
            
            //Dialect is ignored
            if (!ignored) {
                return transactionAttribute;
            }
            
            //Determine interfaces to proxy
            @SuppressWarnings("rawtypes")
            final Set<Class> interfaces = ClassUtils.getAllInterfacesAsSet(transactionAttribute);
            interfaces.add(SkipTransactionAttribute.class);
            
            //Proxy the existing transactionAttribute to mix in our SkipTransactionAttribute interface
            return (TransactionAttribute)Proxy.newProxyInstance(
                    DialectAwareTransactionInterceptor.class.getClassLoader(), 
                    interfaces.toArray(new Class<?>[interfaces.size()]), 
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            return method.invoke(transactionAttribute, args);
                        }
                    });
        }

        private boolean isDialectIgnored(Class<? extends Dialect> dialect, DialectAwareTransactional ann) {
            if (dialect == null) {
                return false;
            }

            boolean ignored = !ann.exclude();
            for (final Class<? extends Dialect> ignoredDialect : ann.value()) {
                if (ignoredDialect.isAssignableFrom(dialect)) {
                    ignored = !ignored;
                    break;
                }
            }
            return ignored;
        }

        private Class<? extends Dialect> determineDialect(Method method, TransactionAttribute transactionAttribute) {
            final Class<? extends Dialect> dialect;
            final String qualifier = transactionAttribute.getQualifier();
            if (StringUtils.hasLength(qualifier)) {
                dialect = this.dialects.get(qualifier);
            }
            else if (this.dialects.size() == 1) {
                dialect = this.dialects.values().iterator().next();
            }
            else if (!this.dialects.isEmpty()) {
                LOGGER.debug("No qualifier specified for @Transactional on {} and multiple Dialects are configured: {}", method, this.dialects.keySet());
                return null;
            }
            else {
                return null;
            }
            return dialect;
        }

        private DialectAwareTransactional getDialectAwareTransactionalAnnotation(Method method, Class<?> targetClass) {
            // Ignore CGLIB subclasses - introspect the actual user class.
            Class<?> userClass = ClassUtils.getUserClass(targetClass);
            // The method may be on an interface, but we need attributes from the target class.
            // If the target class is null, the method will be unchanged.
            Method specificMethod = ClassUtils.getMostSpecificMethod(method, userClass);
            
            DialectAwareTransactional ann = AnnotationUtils.getAnnotation(specificMethod, DialectAwareTransactional.class);
            if (ann == null) {
                ann = AnnotationUtils.getAnnotation(targetClass, DialectAwareTransactional.class);
            }
            return ann;
        }
    }

    private interface SkipTransactionAttribute extends TransactionAttribute {
    }
}
