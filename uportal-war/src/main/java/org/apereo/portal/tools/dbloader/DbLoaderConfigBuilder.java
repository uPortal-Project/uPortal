/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.tools.dbloader;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Configuration for the DbLoader, includes commons-cli Options set for command line use.
 *
 */
public class DbLoaderConfigBuilder implements DbLoaderConfig {
    private String tablesFile;
    private String dataFile;
    private String scriptFile;
    private boolean dropTables;
    private boolean createTables;
    private boolean populateTables;

    public DbLoaderConfigBuilder setTablesFile(String tablesFile) {
        this.tablesFile = tablesFile;
        return this;
    }

    public DbLoaderConfigBuilder setDataFile(String dataFile) {
        this.dataFile = dataFile;
        return this;
    }

    public DbLoaderConfigBuilder setScriptFile(String scriptFile) {
        this.scriptFile = scriptFile;
        return this;
    }

    public DbLoaderConfigBuilder setDropTables(boolean dropTables) {
        this.dropTables = dropTables;
        return this;
    }

    public DbLoaderConfigBuilder setCreateTables(boolean createTables) {
        this.createTables = createTables;
        return this;
    }

    public DbLoaderConfigBuilder setPopulateTables(boolean populateTables) {
        this.populateTables = populateTables;
        return this;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.tools.dbloader.DbLoaderConfig#getTablesFile()
     */
    @Override
    public String getTablesFile() {
        return tablesFile;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.tools.dbloader.DbLoaderConfig#getDataFile()
     */
    @Override
    public String getDataFile() {
        return dataFile;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.tools.dbloader.DbLoaderConfig#getScriptFile()
     */
    @Override
    public String getScriptFile() {
        return this.scriptFile;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.tools.dbloader.DbLoaderConfig#isDropTables()
     */
    @Override
    public boolean isDropTables() {
        return dropTables;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.tools.dbloader.DbLoaderConfig#isCreateTables()
     */
    @Override
    public boolean isCreateTables() {
        return createTables;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.tools.dbloader.DbLoaderConfig#isPopulateTables()
     */
    @Override
    public boolean isPopulateTables() {
        return populateTables;
    }

    /** @see java.lang.Object#equals(Object) */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DbLoaderConfigBuilder)) {
            return false;
        }
        DbLoaderConfigBuilder rhs = (DbLoaderConfigBuilder) object;
        return new EqualsBuilder()
                .append(this.scriptFile, rhs.scriptFile)
                .append(this.dropTables, rhs.dropTables)
                .append(this.createTables, rhs.createTables)
                .append(this.tablesFile, rhs.tablesFile)
                .append(this.dataFile, rhs.dataFile)
                .append(this.populateTables, rhs.populateTables)
                .isEquals();
    }

    /** @see java.lang.Object#hashCode() */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-1864980211, 1008139621)
                .append(this.scriptFile)
                .append(this.dropTables)
                .append(this.createTables)
                .append(this.tablesFile)
                .append(this.dataFile)
                .append(this.populateTables)
                .toHashCode();
    }

    /** @see java.lang.Object#toString() */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("tablesFile", this.tablesFile)
                .append("dataFile", this.dataFile)
                .append("scriptFile", this.scriptFile)
                .append("dropTables", this.dropTables)
                .append("createTables", this.createTables)
                .append("populateTables", this.populateTables)
                .toString();
    }
}
