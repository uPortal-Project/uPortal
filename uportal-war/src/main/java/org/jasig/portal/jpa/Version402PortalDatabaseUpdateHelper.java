package org.jasig.portal.jpa;

import org.jasig.portal.jpa.BasePortalJpaDao.PortalTransactional;
import org.jasig.portal.utils.JdbcUtils;
import org.jasig.portal.version.VersionUtils;
import org.jasig.portal.version.om.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * Update the portal db from 4.0.2
 * 
 * @author Eric Dalquist
 */
public class Version402PortalDatabaseUpdateHelper implements IVersionedDatabaseUpdateHelper {
    private final Version version = VersionUtils.parseVersion("4.0.2");
    private JdbcOperations jdbcOperations;
    
    @Autowired
    @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public String getDatabaseName() {
        return BasePortalJpaDao.PERSISTENCE_UNIT_NAME;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @PortalTransactional
    @Override
    public void preUpdate() {
        JdbcUtils.dropTableIfExists(this.jdbcOperations, "UP_MUTEX");
    }

    @Override
    public void postUpdate() {
    }
}
