/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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

import java.io.PrintWriter;

/**
 * An optional channel interface that allows channels to provide their content in a character form (as opposed to XML form).
 * Note: If a particular channel implements this optional interface, the portal
 * will make use of it if and only if character caching portal setting is turned on.
 * (it is not necessary for the channel to support caching, but character caching needs to be enabled for the portal)
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @author <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version $Revision$
 */
public interface ICharacterChannel extends IChannel {
    /**
     * Asks the channel to render its content as characters.
     * The method has the same call precedence as the IChannel.renderXML() method.
     * (i.e. if the channel also supports ICacheable, portal will try to find a cache entry prior calling this method)
     * @param pw a <code>PrintWriter</code> value into which the character output should be directed
     * @exception PortalException if an error occurs
     */
    public void renderCharacters(PrintWriter pw) throws PortalException;
}
