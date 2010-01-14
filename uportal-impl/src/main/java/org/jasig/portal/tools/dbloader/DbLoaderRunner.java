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
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * Utility to execute DbLoader from the command line.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DbLoaderRunner {
    public static void main(String[] args) throws Exception {
        final Options options = DbLoaderConfiguration.getOptions();
        
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
        
        final DbLoaderConfiguration configuration = new DbLoaderConfiguration(commandLine);
        
        try {
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            final IDbLoader dbLoader = (IDbLoader) applicationContext.getBean("dbLoader", IDbLoader.class);
            dbLoader.process(configuration);
        }
        finally {
            PortalApplicationContextLocator.shutdown();
        }
    }

    protected static void printHelp(final Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(120, "HibernateDbLoader", "", options, "", true);
    }
}
