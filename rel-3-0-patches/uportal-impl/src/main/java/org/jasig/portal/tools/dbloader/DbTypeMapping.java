/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.dbloader;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections15.map.CaseInsensitiveMap;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Holds mapping information from generic data types for table columns
 * to specific types for different databases.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
 * @version $Revision$
 */
public class DbTypeMapping {
    String dbName;
    String dbVersion;
    String driverName;
    String driverVersion;
    Map<String, Type> typesByGenericName = new CaseInsensitiveMap<Type>(); 

    public String getDbName() {
        return dbName;
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverVersion() {
        return driverVersion;
    }

    public Collection<Type> getTypes() {
        return typesByGenericName.values();
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setDriverVersion(String driverVersion) {
        this.driverVersion = driverVersion;
    }

    public void addType(Type type) {
        typesByGenericName.put(type.getGeneric(), type);
    }

    public String getMappedDataTypeName(String genericDataTypeName) {
        final Type typeMapping = typesByGenericName.get(genericDataTypeName);
        if (typeMapping != null) {
            return typeMapping.getLocal();
        }
        
        return null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("dbName", this.dbName)
            .append("dbVersion", this.dbVersion)
            .append("driverName", this.driverName)
            .append("driverVersion", this.driverVersion)
            .append("types", this.typesByGenericName != null ? this.typesByGenericName.values() : null).toString();
    }
}
