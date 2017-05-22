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
package org.apereo.portal.utils;

import com.google.common.base.Function;
import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import org.springframework.core.io.Resource;

/**
 * Scans a directory structure for files.
 *
 */
public interface DirectoryScanner {

    /**
     * Scan the specified directory. Returning a {@link Map} of the processed results.
     *
     * @param directory The directory to scan
     * @param fileFilter Used to filter the files during processing
     * @param fileProcessor Callback, called for each matched {@link File}
     * @return The Map of the processed results from the source file the result
     */
    public abstract <T> Map<File, T> scanDirectoryWithResults(
            File directory, FileFilter fileFilter, Function<Resource, T> fileProcessor);

    /**
     * Scan the specified directory.
     *
     * @param directory The directory to scan
     * @param fileFilter Used to filter the files during processing
     * @param fileProcessor Callback, called for each matched {@link File}
     */
    public abstract void scanDirectoryNoResults(
            File directory, FileFilter fileFilter, Function<Resource, ?> fileProcessor);
}
