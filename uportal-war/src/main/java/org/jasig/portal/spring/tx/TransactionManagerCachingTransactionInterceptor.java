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
