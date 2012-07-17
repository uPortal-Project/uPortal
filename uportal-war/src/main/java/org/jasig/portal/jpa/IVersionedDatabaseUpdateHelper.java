package org.jasig.portal.jpa;

import org.jasig.portal.version.om.Version;

public interface IVersionedDatabaseUpdateHelper {
    /**
     * The persistence-unit name for the database this helper applies to
     */
    String getDatabaseName();
    
    /**
     * Version of the database this helper should be used to upgrade FROM.
     * <p/>
     * If this returns 4.0.2 then this helper will be used when updating from database
     * versions 4.0.0, 4.0.1, and 4.0.2. It will be ignored if updating from database
     * version 4.0.3 or later.
     */
    Version getVersion();
    
    /**
     * Run before updating to this version
     */
    void preUpdate();
    
    /**
     * Run after updating to this version
     */
    void postUpdate();
}
