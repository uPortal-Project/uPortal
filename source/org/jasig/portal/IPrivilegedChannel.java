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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal;


/**
 * Channels implementing this interface are considered "privileged" because
 * they will have access to internal portal control structures such as the
 * HttpServletRequest, HttpServletReponse, and UserLayoutManager.  IPrivilegedChannel
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
public interface IPrivilegedChannel extends IXMLChannel
{

  /**
   * Passes portal control structure to the channel.
   * @see PortalControlStructures
   */
  public void setPortalControlStructures (PortalControlStructures pcs) throws PortalException;
}



