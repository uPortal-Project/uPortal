/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.xml.sax.ContentHandler;

/**
 * An interface for multithreaded channels.
 * Multithreaded channels are trusted to keep their own state/session/user 
 * separation (instead of relying on the servlet engine to do so).
 * The methods are exact analogs of those in {@link IChannel} interface, 
 * but means to identify the channel instance are passed along 
 * with each method.
 * Please refer to {@link IChannel} interface for method descriptions.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 * @see IChannel
 * @see IMultithreadedCacheable
 */

public interface IMultithreadedChannel {

    /**
     * @param uid a string uniqly identifying a channel "instance" in the system.
     * For example, a combination of session id and channel instance id would fit the bill.
     */
    public void setStaticData (ChannelStaticData sd, String uid) throws PortalException;
    public void setRuntimeData (ChannelRuntimeData rd, String uid) throws PortalException;

    public void receiveEvent (PortalEvent ev,String uid);

    public ChannelRuntimeProperties getRuntimeProperties (String uid);
    public void renderXML (ContentHandler out,String uid) throws PortalException;
}
