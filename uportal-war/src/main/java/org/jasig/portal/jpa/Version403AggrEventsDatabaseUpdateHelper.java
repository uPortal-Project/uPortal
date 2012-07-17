package org.jasig.portal.jpa;

import org.jasig.portal.jpa.BaseAggrEventsJpaDao.AggrEventsTransactional;
import org.jasig.portal.utils.JdbcUtils;
import org.jasig.portal.version.VersionUtils;
import org.jasig.portal.version.om.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * Update the aggregate stats db from 4.0.3
 * 
 * @author Eric Dalquist
 */
public class Version403AggrEventsDatabaseUpdateHelper implements IVersionedDatabaseUpdateHelper {
    private final Version version = VersionUtils.parseVersion("4.0.3");
    private JdbcOperations jdbcOperations;
    
    @Autowired
    @Qualifier(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public String getDatabaseName() {
        return BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @AggrEventsTransactional
    @Override
    public void preUpdate() {
        JdbcUtils.dropTableIfExists(this.jdbcOperations, "UP_EVENT_AGGR_CONF_INTRVL_EXC");
        JdbcUtils.dropTableIfExists(this.jdbcOperations, "UP_EVENT_AGGR_CONF_INTRVL_INC");
    }

    @Override
    public void postUpdate() {
    }
}
