package org.jasig.portal.utils.cache;

import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * Creates {@link TagTrackingCacheEventListener} instances
 */
public class SpringCacheEventListenerFactory extends CacheEventListenerFactory {
    public static final String BEAN_NAME = "beanName";
    
    @Override
    public CacheEventListener createCacheEventListener(Properties properties) {
        final String beanName = properties.getProperty(BEAN_NAME);
        return new LazyCacheEventListener(beanName);
    }
    
    private static class LazyCacheEventListener implements CacheEventListener {
        private final String beanName;
        private CacheEventListener delegate;
        
        public LazyCacheEventListener(String beanName) {
            this.beanName = beanName;
        }
        
        /**
         * Always resolves to the same delegate object, no need for thread-sync checks
         */
        private CacheEventListener getDelegate() {
            CacheEventListener d = this.delegate;
            if (d == null) {
                final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
                d = applicationContext.getBean(beanName, CacheEventListener.class);
                this.delegate = d;
            }
            return d;
        }

        @Override
        public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
            this.getDelegate().notifyElementRemoved(cache, element);
        }

        @Override
        public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
            this.getDelegate().notifyElementPut(cache, element);
        }

        @Override
        public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
            this.getDelegate().notifyElementUpdated(cache, element);
        }

        @Override
        public void notifyElementExpired(Ehcache cache, Element element) {
            this.getDelegate().notifyElementExpired(cache, element);
        }

        @Override
        public void notifyElementEvicted(Ehcache cache, Element element) {
            this.getDelegate().notifyElementEvicted(cache, element);
        }

        @Override
        public void notifyRemoveAll(Ehcache cache) {
            this.getDelegate().notifyRemoveAll(cache);
        }

        @Override
        public void dispose() {
            this.getDelegate().dispose();
            this.delegate = null;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return new LazyCacheEventListener(beanName);
        }
    }
}
