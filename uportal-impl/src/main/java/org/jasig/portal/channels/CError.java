/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import java.io.PrintWriter;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IChannel;
import org.jasig.portal.ICharacterChannel;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.error.ErrorCode;
import org.xml.sax.ContentHandler;


/**
 * Deprecated legacy stub channel.
 * CError has been moved to the org.jasig.portal.error package.
 * This class exists to provide backwards compatibility -- it delegates to
 * the org.jasig.portal.error.CError channel.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @deprecated moved to org.jasig.portal.channels.error package.
 */
public class CError
    implements IPrivilegedChannel, ICacheable, ICharacterChannel {
    
    /** Generic error code */
    public static final int GENERAL_ERROR = 0;
    
    /** Error code for failure during rendering. */
    public static final int RENDER_TIME_EXCEPTION = 1;
    
    /** Error code for errors at time static data is set */
    public static final int SET_STATIC_DATA_EXCEPTION = 2;
    
    /** Error code for errors at time runtime data is set. */
    public static final int SET_RUNTIME_DATA_EXCEPTION = 3;
    
    /** Error code for channels that fail to complete rendering during
     * allotted time.
     */
    public static final int TIMEOUT_EXCEPTION = 4;
    
    /** Error code representing failure when framework attempts to set
     * channel portal control structures.
     */
    public static final int SET_PCS_EXCEPTION = 5;
    
    /**
     * Error code representing failure to render due to lack of authorization.
     */
    public static final int CHANNEL_AUTHORIZATION_EXCEPTION=6;
    
    /**
     * Error code representing the channel being just plain missing.
     */
    public static final int CHANNEL_MISSING_EXCEPTION=7;

    // codes defining exception types
    public static final int GENERAL_RENDERING_EXCEPTION=1;
    public static final int INTERNAL_TIMEOUT_EXCEPTION=2;
    public static final int AUTHORIZATION_EXCEPTION=3;
    public static final int RESOURCE_MISSING_EXCEPTION=4;

    /**
     * The modern CError instance to which we delegate.
     */
    private org.jasig.portal.channels.error.CError delegate;
    
    /**
     * Construct an uninitialized instance of the CError channel.
     */
    public CError() {
        this.delegate = new org.jasig.portal.channels.error.CError();
    }

    /**
     * Construct an instance of the Error channel representing a failure to render of 
     * a particular subscribed channel for reason of having thrown a Throwable.
     * @param errorCode - one of the static error codes of this class
     * @param throwable - cause of failed channel's failure
     * @param channelSubscribeId - identifies the failed channel
     * @param channelInstance - the failed channel
     */
    public CError(int errorCode, Throwable throwable, String channelSubscribeId, IChannel channelInstance) {
        ErrorCode codeObject = ErrorCode.codeForInt(errorCode);
        this.delegate = 
            new org.jasig.portal.channels.error.CError(codeObject, throwable, 
                    channelSubscribeId, channelInstance);
    }

    /**
     * Instantiate a CError representing a particular channel's failure,
     * including a message and errorCode, but not a Throwable.
     * @param errorCode - one of the static error codes of this class
     * @param message - describes error
     * @param channelSubscribeId - identifies failed channel
     * @param channelInstance - failed channel
     */
    public CError(int errorCode, String message,String channelSubscribeId,IChannel channelInstance) {
        ErrorCode codeObject = ErrorCode.codeForInt(errorCode);
        this.delegate = new org.jasig.portal.channels.error.CError(codeObject, message,
                channelSubscribeId, channelInstance);
    }

    /**
     * Instantiate a CError instance representing the failure of some particular channel,
     * including an error code, message, and the Throwable.
     * @param errorCode - one of the static error codes of this class
     * @param exception - thrown by the failed channel
     * @param channelSubscribeId - identifies failed channel
     * @param channelInstance - the failed channel instance
     * @param message - message describing failure
     */
    public CError(int errorCode, Throwable exception, String channelSubscribeId,IChannel channelInstance, String message) {
        ErrorCode codeObject = ErrorCode.codeForInt(errorCode);
        this.delegate = new org.jasig.portal.channels.error.CError(codeObject, exception, channelSubscribeId, channelInstance, message);
    }

    /**
     * Set the failure message.
     * @param m - a mesage describing the error.
     */
    public void setMessage(String m) {
        this.delegate.getErrorDocument().setMessage(m);
    }

    public void setPortalControlStructures(PortalControlStructures pcs) {
        this.delegate.setPortalControlStructures(pcs);
    }


    /*
     * This is so CError can be used by getUserLayout() as a placeholder for
     * channels that have either been deleted from the portal database or
     * the users permission to use the channel has been removed (permanently or
     * temporarily).
     */
    public void setStaticData(ChannelStaticData sd) {
       this.delegate.setStaticData(sd);
    }

    public void renderXML(ContentHandler out) {
       this.delegate.renderXML(out);
    }

    public ChannelCacheKey generateKey() {
       return this.delegate.generateKey();
    }

    public boolean isCacheValid(Object validity) {
        return this.delegate.isCacheValid(validity);
    }

    public void renderCharacters(PrintWriter out) throws PortalException {
        this.delegate.renderCharacters(out);
    }

    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
        this.delegate.setRuntimeData(rd);
    }

    public void receiveEvent(PortalEvent ev) {
        this.delegate.receiveEvent(ev);
    }

    public ChannelRuntimeProperties getRuntimeProperties() {
        return this.delegate.getRuntimeProperties();
    }

}