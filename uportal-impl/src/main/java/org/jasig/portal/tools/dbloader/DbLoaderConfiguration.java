/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.tools.dbloader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Configuration for the DbLoader, includes commons-cli Options set for command line use.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DbLoaderConfiguration {
    
    @SuppressWarnings("static-access")
    public static Options getOptions() {
        final Options options = new Options();
        
        
        options.addOption(OptionBuilder.withArgName("tables.xml")
                .hasArg()
                .withLongOpt("useTables")
                .withDescription("The tables.xml file to use")
                .create('t'));
        
        options.addOption(OptionBuilder.withArgName("data.xml")
                .hasArg()
                .withLongOpt("useData")
                .withDescription("The data.xml file to use")
                .create('d'));
        
        options.addOption(OptionBuilder.withLongOpt("scriptFile")
                .hasArg()
                .withLongOpt("scriptFile")
                .withDescription("Create a SQL script of drop and create operations")
                .create('c'));
        
        options.addOption(OptionBuilder.withLongOpt("dropTables")
                .withDescription("Drop tables")
                .create('D'));
        
        options.addOption(OptionBuilder.withLongOpt("createTables")
                .withDescription("Create tables")
                .create('C'));
        
        options.addOption(OptionBuilder.withLongOpt("populateTables")
                .withDescription("Populate tables")
                .create('P'));

        return options;
    }
    
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();
    
    private final Resource tablesFile;
    private final Resource dataFile;
    private final String scriptFile;
    private final boolean dropTables;
    private final boolean createTables;
    private final boolean populateTables;
    
    public DbLoaderConfiguration(CommandLine commandLine) {
        final String tablesFileString = commandLine.getOptionValue('t', "classpath:/properties/db/tables.xml");
        this.tablesFile = this.resourceLoader.getResource(tablesFileString);
        
        final String dataFileString = commandLine.getOptionValue('d', "classpath:/properties/db/data.xml");
        this.dataFile = this.resourceLoader.getResource(dataFileString);
        
        this.scriptFile = commandLine.getOptionValue('c');
        this.dropTables = commandLine.hasOption('D');
        this.createTables = commandLine.hasOption('C');
        this.populateTables = commandLine.hasOption('P');
    }
    
    public DbLoaderConfiguration(boolean dropTables, boolean createTables, boolean populateTables, Resource tablesFile, Resource dataFile, String scriptFile) {
        this.dropTables = dropTables;
        this.createTables = createTables;
        this.populateTables = populateTables;
        this.tablesFile = tablesFile;
        this.dataFile = dataFile;
        this.scriptFile = scriptFile;
    }



    /**
     * @return the tablesFile
     */
    public Resource getTablesFile() {
        return tablesFile;
    }

    /**
     * @return the dataFile
     */
    public Resource getDataFile() {
        return dataFile;
    }

    /**
     * @return the scriptFile
     */
    public String getScriptFile() {
        return this.scriptFile;
    }

    /**
     * @return the dropTables
     */
    public boolean isDropTables() {
        return dropTables;
    }

    /**
     * @return the createTables
     */
    public boolean isCreateTables() {
        return createTables;
    }

    /**
     * @return the populateTables
     */
    public boolean isPopulateTables() {
        return populateTables;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DbLoaderConfiguration)) {
            return false;
        }
        DbLoaderConfiguration rhs = (DbLoaderConfiguration) object;
        return new EqualsBuilder()
            .append(this.scriptFile, rhs.scriptFile)
            .append(this.dropTables, rhs.dropTables)
            .append(this.createTables, rhs.createTables)
            .append(this.tablesFile, rhs.tablesFile)
            .append(this.dataFile, rhs.dataFile)
            .append(this.populateTables, rhs.populateTables)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
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

    /**
     * @see java.lang.Object#toString()
     */
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
