package org.jasig.portal.version;

import java.util.Map;

import org.jasig.portal.version.dao.VersionDao;
import org.jasig.portal.version.om.Version;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;

public class VersionVerifier implements InitializingBean {
    private Map<String, String> requiredProductVersions;
    private VersionDao versionDao;
    
    public void setRequiredProductVersions(Map<String, String> requiredProductVersions) {
        this.requiredProductVersions = requiredProductVersions;
    }
    
    @Autowired
    public void setVersionDao(VersionDao versionDao) {
        this.versionDao = versionDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (final Map.Entry<String, String> productVersionEntry : this.requiredProductVersions.entrySet()) {
            final String product = productVersionEntry.getKey();
            final Version version = this.versionDao.getVersion(product);
            if (version == null) {
                throw new ApplicationContextException("No Version exists for " + product + " in the database. Please run 'ant db-update'");
            }
            
            /*
             * parse version string
             * if db version != expected fail with exception
             */
            
        }
    }
}
