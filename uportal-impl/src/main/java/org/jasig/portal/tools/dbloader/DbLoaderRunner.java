/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
        
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final IDbLoader dbLoader = (IDbLoader) applicationContext.getBean("dbLoader", IDbLoader.class);
        dbLoader.process(configuration);
    }

    protected static void printHelp(final Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(120, "HibernateDbLoader", "", options, "", true);
    }
}
