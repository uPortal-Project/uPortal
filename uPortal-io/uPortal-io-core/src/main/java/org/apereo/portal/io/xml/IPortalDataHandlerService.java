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
package org.apereo.portal.io.xml;

import java.io.File;
import java.util.Set;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import org.springframework.core.io.Resource;

/**
 * Service that can import, export, and delete portal data.
 *
 */
public interface IPortalDataHandlerService {

    /** Options that control behavior of batch import operations */
    public class BatchImportOptions extends BatchOptions {
        private boolean recursive = true;
        private boolean ignoreNonDataFiles = true;

        public BatchImportOptions setRecursive(boolean recursive) {
            this.recursive = recursive;
            return this;
        }

        public BatchImportOptions setIgnoreNonDataFiles(boolean ignoreNonDataFiles) {
            this.ignoreNonDataFiles = ignoreNonDataFiles;
            return this;
        }

        @Override
        public BatchImportOptions setFailOnError(boolean failOnError) {
            super.setFailOnError(failOnError);
            return this;
        }

        @Override
        public BatchImportOptions setLogDirectoryParent(File logDirectoryParent) {
            super.setLogDirectoryParent(logDirectoryParent);
            return this;
        }

        @Override
        public BatchImportOptions setLogDirectoryParent(String logDirectoryParent) {
            super.setLogDirectoryParent(logDirectoryParent);
            return this;
        }
        /** @return defaults to true */
        public final boolean isRecursive() {
            return this.recursive;
        }
        /** @return defaults to true */
        public final boolean isIngoreNonDataFiles() {
            return this.ignoreNonDataFiles;
        }
    }

    /** Options that control behavior of batch export operations */
    public class BatchExportOptions extends BatchOptions {
        @Override
        public BatchExportOptions setFailOnError(boolean failOnError) {
            super.setFailOnError(failOnError);
            return this;
        }

        @Override
        public BatchExportOptions setLogDirectoryParent(File logDirectoryParent) {
            super.setLogDirectoryParent(logDirectoryParent);
            return this;
        }

        @Override
        public BatchExportOptions setLogDirectoryParent(String logDirectoryParent) {
            super.setLogDirectoryParent(logDirectoryParent);
            return this;
        }
    }

    /** Options that control behavior of batch operations */
    public class BatchOptions {
        private boolean failOnError = true;
        private File logDirectoryParent = null;

        public BatchOptions setFailOnError(boolean failOnError) {
            this.failOnError = failOnError;
            return this;
        }

        public BatchOptions setLogDirectoryParent(File logDirectoryParent) {
            this.logDirectoryParent = logDirectoryParent;
            return this;
        }

        public BatchOptions setLogDirectoryParent(String logDirectoryParent) {
            this.logDirectoryParent = new File(logDirectoryParent);
            return this;
        }
        /** @return defaults to true */
        public final boolean isFailOnError() {
            return failOnError;
        }
        /** @return The directory to log the export to, if null a temp directory is used. */
        public final File getLogDirectoryParent() {
            return logDirectoryParent;
        }
    }

    /** Import data from the specified resource */
    public void importData(Resource resource);

    /** Import data from the specified source with the default {@link PortalDataKey}. */
    public void importData(Source source);

    /**
     * Import data from the specified source with the specified {@link PortalDataKey}.
     *
     * @since 4.1
     */
    public void importData(Source source, PortalDataKey portalDataKey);

    /**
     * Import a batch of files from an archive.
     *
     * @param archive Archive to import data files from
     * @param options Optional set of options to better control the import
     */
    public void importDataArchive(Resource archive, BatchImportOptions options);

    /**
     * Import a batch of files from a directory.
     *
     * @param directory Base directory to import from
     * @param pattern Optional ant path matcher pattern used for matching files to import. If not
     *     specified the default pattern set is used
     * @param options Optional set of options to better control the import
     */
    public void importDataDirectory(File directory, String pattern, BatchImportOptions options);

    /** @return All portal data types that can be exported */
    public Iterable<IPortalDataType> getExportPortalDataTypes();

    /** @return All portal data types that can be deleted */
    public Iterable<IPortalDataType> getDeletePortalDataTypes();

    /**
     * @return All portal data for a specific portal data type, some types may return an empty set
     *     even if they contain data due size constraints.
     */
    public Iterable<? extends IPortalData> getPortalData(String typeId);

    /**
     * Export the portal data for the specified type and id writing it to the provided XML
     * Transformer Result
     *
     * @param typeId Type of the portal data to export
     * @param dataId Id of the data to export
     * @param result XML Result to write the exported data to
     * @return The recommended file name to use for the result
     */
    public String exportData(String typeId, String dataId, Result result);

    /**
     * Export the portal data for the specified type and save it to the specified directory
     *
     * @param typeId Type of the portal data to export
     * @param dataId Id of the data to export
     * @param directory Directory to save exported data to
     * @return True if the specified data was found, false if no data exists for the type and data
     *     ids
     */
    public boolean exportData(String typeId, String dataId, File directory);

    /**
     * Export all the portal data for each type and save it to the specified directory
     *
     * @param typeIds TypeIds from {@link #getExportPortalDataTypes()} to export all data for
     * @param directory Directory to save exported data to
     * @param options Optional set of options to better control the export
     */
    public void exportAllDataOfType(
            Set<String> typeIds, File directory, BatchExportOptions options);

    /**
     * Export all portal data for all data type and save it to the specified directory
     *
     * @param directory Directory to save exported data to
     * @param options Optional set of options to better control the export
     */
    public void exportAllData(File directory, BatchExportOptions options);

    /**
     * Delete the portal data for the specified type and id.
     *
     * @param typeId id of the portal type to delete data from
     * @param dataId the id of the data to delete
     */
    public void deleteData(String typeId, String dataId);
}
