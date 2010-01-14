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

package org.jasig.portal;

import java.text.DecimalFormat;
 
public class UploadStatus {

    private int status;
    private long maxSize;

    public UploadStatus(int status, long maxSize) {
        this.status = status;
        this.maxSize = maxSize;
    }

    /**
     * Provides the status for the current upload.
     * @return the upload status as an int.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Provides the max upload file size set in portal properties.
     * @return the max upload file size as an int.
     */
    public int getMaxSize() {
        return (int)maxSize;
    }

    /**
     * Provides the max upload file size (in Megabytes) 
     * which are set in portal properties.
     * @return <code>java.lang.String</code> - the max upload file size.
     */
    public String getFormattedMaxSize() {
        double availableSpace = maxSize / (1024.0 * 1024.0);
        return (fileSizeFormatter.format(availableSpace) + "MB");
    }

    /* static members */
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;

    private static final String SIZE_FORMAT = "##0.##";
    private static DecimalFormat fileSizeFormatter =
        new DecimalFormat(SIZE_FORMAT);

} // end UploadStatus class
