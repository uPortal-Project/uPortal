/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error;

/**
 * Error codes specific to the CError channel.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class ErrorCode {

    /** Generic error code */
    public static final ErrorCode UNKNOWN_ERROR = 
        new ErrorCode("unknown error", -1, true);
    
    /** Generic error code */
    public static final ErrorCode GENERAL_ERROR = 
        new ErrorCode("general error", 0, true);
    
    /** Error code for failure during rendering. */
    public static final ErrorCode RENDER_TIME_EXCEPTION = 
        new ErrorCode("Render time exception", 1, true);
    
    /** Error code for errors at time static data is set */
    public static final ErrorCode SET_STATIC_DATA_EXCEPTION = 
        new ErrorCode("Set static data exception", 2, false);
    
    /** Error code for errors at time runtime data is set. */
    public static final ErrorCode SET_RUNTIME_DATA_EXCEPTION = 
        new ErrorCode("Set runtime data", 3, true);
    
    /** Error code for channels that fail to complete rendering during
     * allotted time.
     */
    public static final ErrorCode TIMEOUT_EXCEPTION = 
        new ErrorCode("Rendering timeout", 4, true);
    
    /** Error code representing failure when framework attempts to set
     * channel portal control structures.
     */
    public static final ErrorCode SET_PCS_EXCEPTION = 
        new ErrorCode("Set PCS exception", 5, true);
    
    /**
     * Error code representing failure to render due to lack of authorization.
     */
    public static final ErrorCode CHANNEL_AUTHORIZATION_EXCEPTION =
        new ErrorCode("Channel authorization exception", 6, false);
    
    /**
     * Error code representing the channel being just plain missing.
     */
    public static final ErrorCode CHANNEL_MISSING_EXCEPTION = 
        new ErrorCode("Channel missing", 7, false);
    
    private static ErrorCode[] codeArray = {
            GENERAL_ERROR, 
            RENDER_TIME_EXCEPTION, 
            SET_STATIC_DATA_EXCEPTION, 
            SET_RUNTIME_DATA_EXCEPTION, 
            TIMEOUT_EXCEPTION, 
            SET_PCS_EXCEPTION, 
            CHANNEL_AUTHORIZATION_EXCEPTION, 
            CHANNEL_MISSING_EXCEPTION};
    
    /**
     * Obtain the ErrorCode for the given code number, or null if the
     * code number does not correspond to an error code.
     * @param codeNum
     * @return ErrorCode or null.
     */
    public static ErrorCode codeForInt(int codeNum) {
        ErrorCode code = UNKNOWN_ERROR;
        if (codeNum > -1 && codeNum < codeArray.length)
            code = codeArray[codeNum];
        return code;
    }
    
    /**
     * Integer representing error code.
     */
    private final int codeNumber;
    
    /**
     * String label suggesting meaning of error code.
     */
    private final String label;

    /**
     * True if channel refresh is an appropriate response to this error, 
     * false otherwise.
     */
    private boolean refreshable;
    
    /**
     * Private constructor.
     * @param label - briefly describes error type
     * @param codeNumber - integer representing error type
     * @param refreshable - whether refresh is appropriate response
     */
    private ErrorCode(final String label, final int codeNumber, 
            final boolean refreshable){
        this.label = label;
        this.codeNumber = codeNumber;
        this.refreshable = refreshable;
    }
    
    public int getCode(){
        return this.codeNumber;
    }
    
    public String getLabel(){
        return this.label;
    }
    
    /**
     * Is this ErrorCode representative of a problem to which
     * channel refresh would be an appropriate response.
     * Some ErrorCodes are representative of problems that refreshing just
     * won't fix, such as a failure when setting static data.
     * @return
     */
    public boolean isRefreshAllowed() {
        return this.refreshable;
    }
    
    public String toString(){
        return this.label;
    }
    
}