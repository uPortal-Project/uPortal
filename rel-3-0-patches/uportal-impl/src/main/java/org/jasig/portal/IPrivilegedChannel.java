/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Channels implementing this interface are considered "privileged" because
 * they will have access to internal portal control structures such as the
 * HttpServletRequest, HttpServletReponse, and UserPreferencesManager.  IPrivilegedChannel
 * is intended for channels that are integral to the framework such as those that
 * manage user preferences, channel publishing, and channel subscription.
 * IPrivilegedChannel is NOT intended for "normal" channels.  Channels
 * should normally implement {@link IChannel}.
 *
 * Portal administrators should only allow publishing/subscibing of channels
 * implementing IPrivilegedChannel if the following are true:
 *
 * <ul>
 * <li>The channel is an integral part of the uPortal framework, e.g. {@link org.jasig.portal.channels.CLogin}.</li>
 * <li>The channel is well-understood and will not cause harm.  An understanding of the
 * portal architecture is necessary to determine if this is true.</li>
 * <li>There is no way to implement the channel as an IChannel because access to
 * internal structures is absolutely necessary.</li>
 * </ul>
 * @author Peter Kharchenko, pkharchenko@interactivebusiness.com
 * @version $Revision$
 * @see IChannel
 * @see PortalControlStructures
 */
public interface IPrivilegedChannel extends IChannel, IPrivileged {}

