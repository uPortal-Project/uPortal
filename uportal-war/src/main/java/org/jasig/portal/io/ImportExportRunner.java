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

package org.jasig.portal.io;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.tools.ant.util.FileUtils;
import org.jasig.portal.io.xml.IDataImportExportService;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ImportExportRunner {
    public static void main(String[] args) {
        final Options options = new Options();

        //operation options
        final OptionGroup operationOptionGroup = new OptionGroup();
        operationOptionGroup.addOption(new Option("i", "import", false, "Import one or more data files. dir must be specified"));
        operationOptionGroup.addOption(new Option("e", "export", false, "Import export one or more pieces of portal data, if no type all will be exported, if no typeId all of the type will be exported"));
        operationOptionGroup.addOption(new Option("x", "delete", false, "Delete a piece of portal data, type and typeId must be specified"));
        operationOptionGroup.addOption(new Option("l", "list", false, "List portal data available for export or delete"));
        operationOptionGroup.setRequired(true);
        options.addOptionGroup(operationOptionGroup);
        
        options.addOption(new Option("d", "dir", true, "Base directory to import from or export to"));
        options.addOption(new Option("p", "pattern", true, "Ant pattern files must match to be imported"));
        options.addOption(new Option("f", "file", true, "Specific file to import"));
        options.addOption(new Option("t", "type", true, "Portal data type to export or delete"));
        options.addOption(new Option("id", "typeId", true, "Data id to export or delete"));

        final CommandLineParser parser = new PosixParser();
        final CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
            printHelp(options);
            return;
        }
        
        
        if (commandLine.hasOption("import")) {
            if (commandLine.hasOption("dir")) {
                final String directoryPath = commandLine.getOptionValue("dir");
                final String pattern = commandLine.getOptionValue("pattern");
                final File directory = getAbsoluteFile(directoryPath);
                
                final IDataImportExportService dataImportExportService = getDataImportExportService();
                dataImportExportService.importData(directory, pattern, null);
            }
            else if (commandLine.hasOption("file")) {
                final String filePath = commandLine.getOptionValue("file");
                final File file = getAbsoluteFile(filePath);
                
                final IDataImportExportService dataImportExportService = getDataImportExportService();
                dataImportExportService.importData(new FileSystemResource(file));
            }
            else {
                System.err.println("Either dir or file options must be specified when importing.");
                printHelp(options);
            }
        }
        else {
            System.err.println("Unknown operation specified.");
            printHelp(options);
        }
    }

    /**
     * @return
     */
    protected static IDataImportExportService getDataImportExportService() {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final IDataImportExportService dataImportExportService = applicationContext.getBean(IDataImportExportService.class);
        return dataImportExportService;
    }

    protected static File getAbsoluteFile(final String filePath) {
        final File file;
        if (FileUtils.isAbsolutePath(filePath)) {
            file = new File(filePath);
        }
        else {
            file = new File(new File(System.getProperty("user.dir")), filePath);
        }
        
        try {
            return file.getCanonicalFile();
        }
        catch (IOException e) {
            return file;
        }
    }

    protected static void printHelp(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        final PrintWriter pw = new PrintWriter(System.err);
        formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, "java " + ImportExportRunner.class.getName(),
                null, options, HelpFormatter.DEFAULT_LEFT_PAD,
                HelpFormatter.DEFAULT_DESC_PAD, null, true);
        pw.flush();
    }
}
