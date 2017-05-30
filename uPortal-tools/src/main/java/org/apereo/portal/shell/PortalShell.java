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
package org.apereo.portal.shell;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.tools.ant.util.FileUtils;
import org.apereo.portal.utils.PortalApplicationContextLocator;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;

public class PortalShell {
    static final Logger LOGGER = LoggerFactory.getLogger(PortalShell.class);

    protected static Options getOptions() {
        final Options options = new Options();

        options.addOption(
                new Option("s", "script", true, "Groovy script to execute in the uPortal Shell."));

        return options;
    }

    public static void main(String[] args) throws Exception {
        final Options options = getOptions();

        final CommandLineParser parser = new PosixParser();
        final CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            printHelp(options);
            return;
        }

        final ApplicationContext applicationContext =
                PortalApplicationContextLocator.getApplicationContext();
        try {
            final Binding binding = new SpringBinding(applicationContext);
            binding.setVariable("logger", LOGGER);
            final CompilerConfiguration conf =
                    new CompilerConfiguration(System.getProperties());
            final GroovyShell shell = new GroovyShell(binding, conf);

            if (commandLine.hasOption("script")) {
                final String scriptName = commandLine.getOptionValue("script");
                final File scriptFile = getAbsoluteFile(scriptName);

                shell.run(scriptFile, args);
            }
        } finally {
            if (applicationContext instanceof DisposableBean) {
                ((DisposableBean) applicationContext).destroy();
            }
        }
    }

    protected static void printHelp(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        final PrintWriter pw = new PrintWriter(System.err);
        formatter.printHelp(
                pw,
                HelpFormatter.DEFAULT_WIDTH,
                "java " + PortalShell.class.getName(),
                null,
                options,
                HelpFormatter.DEFAULT_LEFT_PAD,
                HelpFormatter.DEFAULT_DESC_PAD,
                null,
                true);
        pw.flush();
    }

    protected static File getAbsoluteFile(final String filePath) {
        final File file;
        if (FileUtils.isAbsolutePath(filePath)) {
            file = new File(filePath);
        } else {
            file = new File(new File(System.getProperty("user.dir")), filePath);
        }

        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return file;
        }
    }
}
