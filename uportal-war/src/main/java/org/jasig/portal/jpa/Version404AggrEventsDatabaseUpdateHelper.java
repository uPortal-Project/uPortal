package org.jasig.portal.jpa;

import org.jasig.portal.jpa.BaseAggrEventsJpaDao.AggrEventsTransactional;
import org.jasig.portal.tools.dbloader.ISchemaExport;
import org.jasig.portal.version.VersionUtils;
import org.jasig.portal.version.om.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Update the aggregate stats db from 4.0.4
 * 
 * @author Eric Dalquist
 */
public class Version404AggrEventsDatabaseUpdateHelper implements IVersionedDatabaseUpdateHelper {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final Version version = VersionUtils.parseVersion("4.0.4");
    private ISchemaExport schemaExport;

    @Autowired
    @Qualifier(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    public void setSchemaExport(ISchemaExport schemaExport) {
        this.schemaExport = schemaExport;
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
        //Drop the aggregate events database tables
        logger.info("Dropping aggregate event tables for upgrade from " + getVersion());
        this.schemaExport.create(true, false, true, " ", false);
        
        //Create the aggregate events database tables
        logger.info("Creating aggregate event tables for upgrade from " + getVersion());
        this.schemaExport.create(true, true, false, " ", true);
        
        logger.warn("IMPORTANT: You must import your event aggregation configuration again!\n\tex: ant data-import -Dfile=/path/to/uportal/uportal-war/src/main/data/default_entities/event-aggregation/default.event-aggregation.xml");
    }

    @Override
    public void postUpdate() {
    }
}
