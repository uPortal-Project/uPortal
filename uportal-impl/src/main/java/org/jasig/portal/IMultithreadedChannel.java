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
