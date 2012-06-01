package org.hibernate.cache.ehcache;

import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.apache.commons.lang.StringUtils;
import org.hibernate.cache.CacheException;
import org.hibernate.cfg.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Region factory that loads the {@link CacheManager} by name
 * 
 * @author Eric Dalquist
 */
public class SpringBeanEhCacheRegionFactory extends AbstractEhcacheRegionFactory {
    private static final long serialVersionUID = 1L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    public static final String CACHE_MANAGER_NAME = "org.jasig.portal.cache.CacheManagerName";
    
    public void start(Settings settings, Properties properties) throws CacheException {
        this.settings = settings;
        try {
            String cacheManagerBeanName = null;
            if (properties != null) {
                cacheManagerBeanName = StringUtils.trimToNull(properties
                        .getProperty(CACHE_MANAGER_NAME));
            }
            
            if (cacheManagerBeanName == null) {
                throw new IllegalArgumentException("The '" + CACHE_MANAGER_NAME + "' property must be set");
            }
                
            logger.debug("Getting CacheManager bean named {}", cacheManagerBeanName);
            manager = CacheManager.getCacheManager(cacheManagerBeanName);
            mbeanRegistrationHelper.registerMBean(manager, properties);
        }
        catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        //Assume spring will stop the cache manager
        manager = null;
    }

}
