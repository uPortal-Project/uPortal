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

import java.util.Map;
import java.util.Properties;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.StringUtils;

import com.google.common.collect.MapMaker;

/**
 * Caches lookups for TX managers specified by qualifier in a local map
 * 
 * @author Eric Dalquist
 */
public class TransactionManagerCachingTransactionInterceptor extends TransactionInterceptor {
    private static final long serialVersionUID = 1L;
    
    private final Map<String, PlatformTransactionManager> platformTransactionManagerCache = new MapMaker().weakValues().makeMap();
    
    public TransactionManagerCachingTransactionInterceptor() {
        super();
    }

    public TransactionManagerCachingTransactionInterceptor(PlatformTransactionManager ptm, Properties attributes) {
        super(ptm, attributes);
    }

    public TransactionManagerCachingTransactionInterceptor(PlatformTransactionManager ptm,
            TransactionAttributeSource tas) {
        super(ptm, tas);
    }

    protected PlatformTransactionManager determineTransactionManager(TransactionAttribute txAttr) {
        if (txAttr == null) {
            return super.determineTransactionManager(txAttr);
        }
        
        final String qualifier = txAttr.getQualifier();
        if (StringUtils.hasLength(qualifier)) {
            PlatformTransactionManager platformTransactionManager = platformTransactionManagerCache.get(qualifier);
            if (platformTransactionManager == null) {           
                platformTransactionManager = super.determineTransactionManager(txAttr);
                platformTransactionManagerCache.put(qualifier, platformTransactionManager);
            }
            
            return platformTransactionManager;
        }

        return super.determineTransactionManager(txAttr);
    }
}
