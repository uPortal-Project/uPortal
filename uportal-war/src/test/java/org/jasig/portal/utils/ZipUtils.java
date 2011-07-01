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

package org.jasig.portal.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extract a Zip File or Zip InputStream to a directory
 * 
 * Code based on: http://piotrga.wordpress.com/2008/05/07/how-to-unzip-archive-in-java/
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class ZipUtils {
    protected static final Logger log = LoggerFactory.getLogger(ZipUtils.class);
    
    private ZipUtils() {
    }

    public static void extract(File archive, File outputDir) throws IOException {
        final ZipFile zipfile = new ZipFile(archive);
        for (final Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements();) {
            final ZipEntry entry = e.nextElement();

            final File outputFile = checkDirectories(entry, outputDir);
            if (outputFile != null) {
                final InputStream is = zipfile.getInputStream(entry);
                try {
                    writeFile(is, outputFile);
                }
                finally {
                    is.close();
                }
            }
        }
    }
    
    public static void extract(InputStream archive, File outputDir) throws IOException {
        final ZipInputStream zipInputStream = new ZipInputStream(archive);
        
        while (true) {
            final ZipEntry entry = zipInputStream.getNextEntry();
            if (entry == null) {
                break;
            }
            final File outputFile = checkDirectories(entry, outputDir);
            if (outputFile != null) {
                writeFile(zipInputStream, outputFile);
            }
        }
    }

    /**
     * Creates any required parent directories, returns the File to extract the entry to, returns null if there is no file to extract (such as a directory entry)
     */
    protected static File checkDirectories(ZipEntry entry, File outputDir) {
        final String name = entry.getName();
        
        if (entry.isDirectory()) {
            createDir(new File(outputDir, name));
            return null;
        }

        final File outputFile = new File(outputDir, name);
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }
        
        log.debug("Extracting " + name);
        return outputFile;
    }

    /**
     * Writes the input stream to the File using buffered streams
     */
    protected static void writeFile(final InputStream is, final File outputFile) throws IOException {
        final BufferedInputStream inputStream = new BufferedInputStream(is);
        final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        }
        finally {
            outputStream.close();
        }
    }

    private static void createDir(File dir) {
        log.debug("Creating dir " + dir.getName());
        if (!dir.mkdirs()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }
}