package org.jasig.portal.version;

import java.util.Map;

import javax.annotation.Resource;

import org.jasig.portal.version.dao.VersionDao;
import org.jasig.portal.version.om.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

/**
 * Bean that verifies the product version numbers in the configuration versus the database on startup
 * 
 * @author Eric Dalquist
 */
@Component
public class VersionVerifier implements InitializingBean {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
    private Map<String, Version> requiredProductVersions;
    private VersionDao versionDao;
    
    @Resource(name = "productVersions")
    public void setRequiredProductVersions(Map<String, Version> requiredProductVersions) {
        this.requiredProductVersions = ImmutableMap.copyOf(requiredProductVersions);
    }
    
    @Autowired
    public void setVersionDao(VersionDao versionDao) {
        this.versionDao = versionDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (final Map.Entry<String, Version> productVersionEntry : this.requiredProductVersions.entrySet()) {
            final String product = productVersionEntry.getKey();
            final Version dbVersion = this.versionDao.getVersion(product);
            if (dbVersion == null) {
                throw new ApplicationContextException("No Version exists for " + product + " in the database. Please run 'ant db-update'");
            }
            
            final Version expectedVersion = productVersionEntry.getValue();
			if (!dbVersion.equals(expectedVersion)) {
            	throw new ApplicationContextException("Database Version for " + product + " is " + dbVersion + " but the code version is " + expectedVersion + ". Please run 'ant db-update'");
            }
			
			logger.info("Versions {} match for {}", dbVersion, product);
        }
    }
}
