/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal;

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.car.CarResources;
import org.jasig.portal.layout.IUserLayoutChannelDescription;
import org.jasig.portal.layout.IUserLayoutManager;

/**
 * A factory class that produces <code>IChannel</code> instances.
 * This class maintains a lazily-loaded, but permanent
 * cache of channels that implement one of uPortal's 
 * multithreaded interfaces, IMultithreadedChannel or one of its variants.
 *
 * @author <a href="mailto:pkharchenko@unicon.net">Peter Kharchenko</a>
 * @version $Revision$
 */
public class ChannelFactory {

    private static final Log log = LogFactory.getLog(ChannelFactory.class);
    
    // table of multithreaded channels
    private static final Hashtable staticChannels = new Hashtable();
    
    // create a CAR class loader object for loading channel classes from CARs
    // Note that the current class loader is passed as the parent and is
    // searched before CARs are. So if a class exists in the VM classpath
    // _and_ in a CAR the one on the classpath will be found first.
    private static ClassLoader classLoader = CarResources.getInstance()
        .getClassLoader();
    

    /**
     * Instantiate a channel from information supplied by the user layout manager.
     *
     * @param channelSubscribeId a <code>String</code> value
     * @param ulm an <code>IUserLayoutManager</code> value
     * @param sessionId a <code>String</code> HTTP session Id value
     * @return an <code>IChannel</code> instance
     * @exception PortalException if an error occurs
     */
    public static IChannel instantiateLayoutChannel(String channelSubscribeId, IUserLayoutManager ulm, String sessionId) throws PortalException {
        // get channel information from the user layout manager
        IUserLayoutChannelDescription channel=(IUserLayoutChannelDescription) ulm.getNode(channelSubscribeId);
        if(channel!=null) {
            String className=channel.getClassName();
            String channelPublishId=channel.getChannelPublishId();
            long timeOut=channel.getTimeout();
            try {
                return instantiateChannel(channelSubscribeId,channelPublishId, className,timeOut,channel.getParameterMap(),sessionId);
            } catch (Exception ex) {
                log.error("ChannelManager::instantiateChannel() : unable to instantiate channel class \""+className+"\". "+ex);
                return null;
            }
        } else return null;
    }

    /**
     * Construct channel instance based on a channel description object.
     *
     * @param description an <code>IUserLayoutChannelDescription</code> value
     * @param sessionId a <code>String</code> HTTP session Id value
     * @return an <code>IChannel</code> value
     */
    public static IChannel instantiateLayoutChannel(IUserLayoutChannelDescription description, String sessionId) throws PortalException {
        return instantiateChannel(description.getChannelSubscribeId(),description.getChannelPublishId(), description.getClassName(),description.getTimeout(),description.getParameterMap(),sessionId);
    }

    private static IChannel instantiateChannel(String channelSubscribeId, String channelPublishId, String className, long timeOut, Map params, String sessionId) throws PortalException {
      String uid = sessionId + "/" + channelSubscribeId;
      return instantiateChannel(className, uid);
    }

    /**
     * Produce an IChannel based on a java class name.  If the java class
     * specified implements a channel interface other than
     * <code>org.jasig.portal.IChannel</code>, it will be wrapped by an
     * appropriate adapter class that does implement IChannel.
     * @param className the channel's java class name
     * @param uid a unique ID for use with multithreaded channels
     * @return an <code>IChannel</code> object
     */
    public static synchronized IChannel instantiateChannel(String className, String uid) throws PortalException {
        IChannel ch = null;
        boolean exists = false;
        // Avoid instantiating a multithreaded channel more than once
        // by storing it in a staticChannels table.
        Object cobj = staticChannels.get(className);
        if (cobj != null) {
            exists = true;
        } else {
            Class channelClass = null;
            try {
                // Load the class using the CAR class loader which uses
                // the default class loader before looking into the CARs
                channelClass = classLoader.loadClass(className);                
            } catch (Exception e) {
                throw new PortalException("Unable to load class '" + className + "'", e);
            }
            try {
                cobj =  channelClass.newInstance();
            } catch (Throwable t) {
                throw new PortalException("Unable to instantiate class '" + className + "'", new Exception(t.getMessage()));
            }            
        }

        // determine what kind of a channel it is.
        // (perhaps, later this all could be moved to JNDI factories, so everything would be transparent)
        if (cobj instanceof IMultithreadedCharacterChannel) {
            if (cobj instanceof IMultithreadedCacheable) {
                if (cobj instanceof IMultithreadedPrivileged) {
                    if (cobj instanceof IMultithreadedMimeResponse) {
                        ch = new MultithreadedPrivilegedCacheableMimeResponseCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                    } else if (cobj instanceof IMultithreadedDirectResponse) {
                        // cacheable, privileged and direct response
                        ch = new MultithreadedPrivilegedCacheableDirectResponseCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);                        
                    } else {
                        // both cacheable and privileged
                        ch = new MultithreadedPrivilegedCacheableCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                    }
                } else {
                    if (cobj instanceof IMultithreadedMimeResponse) {
                        ch = new MultithreadedCacheableMimeResponseCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                    } else {
                        // just cacheable
                        ch = new MultithreadedCacheableCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                    }
                }
            } else if (cobj instanceof IMultithreadedPrivileged) {
                if (cobj instanceof IMultithreadedMimeResponse) {
                    ch = new MultithreadedPrivilegedMimeResponseCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                } else {
                    ch = new MultithreadedPrivilegedCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                }
            } else {
                if (cobj instanceof IMultithreadedMimeResponse) {
                    ch = new MultithreadedMimeResponseCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                } else {
                    // plain multithreaded
                    ch = new MultithreadedCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                }
            }
            // see if we need to add the instance to the staticChannels
            if (!exists) {
                staticChannels.put(className, cobj);
            }
        } else if (cobj instanceof IMultithreadedChannel) {
            if (cobj instanceof IMultithreadedCacheable) {
                if (cobj instanceof IMultithreadedPrivileged) {
                    if (cobj instanceof IMultithreadedMimeResponse) {
                        ch = new MultithreadedPrivilegedCacheableMimeResponseChannelAdapter((IMultithreadedChannel)cobj, uid);
                    } else {
                        // both cacheable and privileged
                        ch = new MultithreadedPrivilegedCacheableChannelAdapter((IMultithreadedChannel)cobj, uid);
                    }
                } else {
                    if (cobj instanceof IMultithreadedMimeResponse) {
                        ch = new MultithreadedCacheableMimeResponseChannelAdapter((IMultithreadedChannel)cobj, uid);
                    } else {
                        // just cacheable
                        ch = new MultithreadedCacheableChannelAdapter((IMultithreadedChannel)cobj, uid);
                    }
                }
            } else if (cobj instanceof IMultithreadedPrivileged) {
                if (cobj instanceof IMultithreadedMimeResponse) {
                    ch = new MultithreadedPrivilegedMimeResponseChannelAdapter((IMultithreadedChannel)cobj, uid);
                } else {
                    ch = new MultithreadedPrivilegedChannelAdapter((IMultithreadedChannel)cobj, uid);
                }
            } else {
                if (cobj instanceof IMultithreadedMimeResponse) {
                    ch = new MultithreadedMimeResponseChannelAdapter((IMultithreadedChannel)cobj, uid);
                } else {
                    // plain multithreaded
                    ch = new MultithreadedChannelAdapter((IMultithreadedChannel)cobj, uid);
                }
            }
            // see if we need to add the instance to the staticChannels
            if (!exists) {
                staticChannels.put(className, cobj);
            }
        } else {
            // vanilla IChannel
            ch = (IChannel)cobj;
        }

        return ch;
    }
}
