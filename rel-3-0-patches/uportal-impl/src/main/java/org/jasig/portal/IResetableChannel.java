/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal;

/**
 * Describes an IChannel that can handle being refereshed or reset by the container
 * after an error.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IResetableChannel extends IChannel {
    /**
     * Prepare to be refereshed after an error. Information about the last
     * request should be cleared and the channel should be in a renderable
     * state when this method returns.
     * 
     * If the channel also implements {@link IPrivileged} {@link IPrivileged#setPortalControlStructures(PortalControlStructures)}
     * must be called before this method. {@link IChannel#setRuntimeData(ChannelRuntimeData)} must also be called before this method.
     */
    public void prepareForRefresh();
    
    /**
     * Prepare to be reset after an error. This channel instance may be destroyed
     * and re-created during the reset process so this method gives the old channel
     * a chance to clean up before the reset. State information stored anywhere outside
     * of {@link ChannelRuntimeData} should be cleared.
     * 
     * If the channel also implements {@link IPrivileged} {@link IPrivileged#setPortalControlStructures(PortalControlStructures)}
     * must be called before this method. {@link IChannel#setRuntimeData(ChannelRuntimeData)} must also be called before this method.
     */
    public void prepareForReset();
}
