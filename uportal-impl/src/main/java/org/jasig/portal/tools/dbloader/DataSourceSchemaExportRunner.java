/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.tools.dbloader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DataSourceSchemaExportRunner {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final Options options = new Options();
        
        final Option beanNameOpt = new Option("b", "beanName", true, "The name of the ISchemaExport bean to execute.");
        beanNameOpt.setRequired(true);
        options.addOption(beanNameOpt);
        
        final Option exportOpt = new Option("e", "export", false, "If the generated SQL should be run against the database");
        options.addOption(exportOpt);
        
        final Option dropOpt = new Option("d", "drop", false, "If the database objects should be dropped before creation");
        options.addOption(dropOpt);
        
        final Option createOpt = new Option("c", "create", false, "If the creation of the database objects should be done");
        options.addOption(createOpt);

        final Option outputFileOpt = new Option("o", "outputFile", true, "The file to write out the SQL to; optional.");
        options.addOption(outputFileOpt);
        
        final Option ignoreNotFoundOpt = new Option("i", "ignoreNotFound", false, "If not finding the specified Spring bean should result in an exception.");
        options.addOption(ignoreNotFoundOpt);
        
        final CommandLine commandLine;
        try {
            final Parser cliParser = new PosixParser();
            commandLine = cliParser.parse(options, args);
        }
        catch (ParseException exp) {
            printHelp(options);
            return;
        }
        
        if (commandLine.getOptions().length == 0) {
            printHelp(options);
            return;
        }
        
        final String beanName = commandLine.getOptionValue(beanNameOpt.getOpt());
        final boolean export = commandLine.hasOption(exportOpt.getOpt());
        final boolean create = commandLine.hasOption(createOpt.getOpt());
        final boolean drop = commandLine.hasOption(dropOpt.getOpt());
        final String outputFile = commandLine.getOptionValue(outputFileOpt.getOpt());
        final boolean ignoreNotFound = commandLine.hasOption(ignoreNotFoundOpt.getOpt());
        
        
        try {
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            final ISchemaExport schemaExport;
            try {
                schemaExport = (ISchemaExport) applicationContext.getBean(beanName, ISchemaExport.class);
            }
            catch (NoSuchBeanDefinitionException e) {
                if (ignoreNotFound) {
                    return;
                }
    
                throw new IllegalArgumentException("Could not find ISchemaExport bean named '" + beanName + "'", e);
            }
            
            schemaExport.hbm2ddl(export, create, drop, outputFile);
        }
        finally {
            PortalApplicationContextLocator.shutdown();
        }
    }

    protected static void printHelp(final Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(120, "DataSourceSchemaExportRunner", "", options, "", true);
    }

}
