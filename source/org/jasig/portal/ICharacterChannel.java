/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
