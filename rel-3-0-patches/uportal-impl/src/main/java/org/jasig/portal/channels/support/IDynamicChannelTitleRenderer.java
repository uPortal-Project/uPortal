/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.support;

/**
 * Interface for IChannelRenderers that support dynamic channel titles.
 * Dynamic channel titles were added in uPortal 2.5.1.  In order to maintain
 * backwards compatibility, rather than adding this method to IChannelRenderer
 * itself, it is added to this optional extension interface.  IChannelRenderers
 * implementing this extention interface can take advantage of dynamic channel
 * support in the uPortal framework (specifically, in ChannelManager).
 * IChannelRenderers not changed to implement this interface will continue to
 * behave exactly as before dynamic channel title capabilities were introduced.
 * @since uPortal 2.5.1
 */
public interface IDynamicChannelTitleRenderer {

    /**
     * Get the dynamic channel title, if any, for the channel
     * that this renderer has rendered.
     * 
     * This method must not be executed concurently with outputRendering():
     * the rendering framework should either first request the channel content
     * and then requets the title, or first request the title and then request
     * the channel content, but not both concurrently.  Both this method and 
     * outputRendering() block on the channel rendering worker thread, blocking
     * until the thread completes rendering the channel, or until the thread
     * times out.
     * 
     * @return String representing the dynamic channel title, or null if no 
     * dynamic channel title.
     */
    public String getChannelTitle();
    
}
