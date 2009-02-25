/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @version $Revision$
 * @see IChannel
 * @see IMultithreadedCacheable
 * 
 * @deprecated Use the IChannel* interfaces instead or write a portlet. For more information see: 
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IMultithreaded+Interfaces
 */

@Deprecated
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
