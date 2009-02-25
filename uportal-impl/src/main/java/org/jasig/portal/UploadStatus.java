/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
