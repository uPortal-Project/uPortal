/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.portlet;

import org.jasig.portal.IPrivilegedChannel;

/**
 * Marker interface for IChannels to communicate that they wish to be treated
 * in the ways we treat JSR-168 portlets being rendered via IChannels.  This
 * includes being rendered when minimized, etc.
 * 
 * uPortal infrastructure can check any given IChannel to see if it implements
 * this interface to determine whether it should treat it specially or not.
 * 
 * @since uPortal 2.5
 */
public interface IPortletAdaptor extends IPrivilegedChannel {
    
    // this marker interface declares no methods.

}

/*
 * TODO: better document exactly what special behavior is expected for
 * channels implementing this interface.
 * 
 * TODO: maybe this interface should extend IChannel.  Do we expect anything
 * other than an IChannel to want to use this marker?
 */

/*
 * A marker interface is an appropriate solution to the problem of IChannels 
 * communicating that they wish to be treated as JSR-168 portlets because
 * JSR-168-ness is a java coding time attribute of a particular IChannel 
 * implementation.  A particular IChannel either wishes to be treated in these
 * ways or it does not.  Presumably all IChannels that choose to implement this
 * interface indicating that they wish to be treated as JSR-168 portlets will
 * in fact be IChannel implementations that are backed by JSR-168 portlets, but
 * this is not a hard requirement.  In theory any other IChannel wishing to have
 * an opportunity to render content even when minimized (or take advantage of
 * other ways in which portlets are handled specially) could implement this
 * interface.
 * 
 * There are several places in the code where we need to treat IChannels seeking
 * to represent portlets differently than other portlets.  There are alternative
 * solutions to this problem that we could have adopted.
 * 
 * This approach was selected in preference to checking for the default CPortletAdaptor
 * directly, as that would have introduced a tight coupling to a particular 
 * channel implementation, would have made difficult replacing that default with
 * an alternate implementation.
 * 
 * This approach was selected in preference to checking against a portal.properties
 * property declaring the class name of the portlet adaptor, as that approach
 * seemed to make runtime configuration of what is better expressed as 
 * a compile-time communication of an IChannel implementation of what it expects.
 * 
 * Being a portlet adaptor is more a property of a particular piece of IChannel code
 * than it is a runtime configurable property of a uPortal.
 */

