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

import java.io.PrintWriter;

/**
 * An optional channel interface that allows channels to provide their content in a character form (as opposed to XML form).
 * Note: If a particular channel implements this optional interface, the portal
 * will make use of it if and only if character caching portal setting is turned on.
 * (it is not necessary for the channel to support caching, but character caching needs to be enabled for the portal)
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @author <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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
