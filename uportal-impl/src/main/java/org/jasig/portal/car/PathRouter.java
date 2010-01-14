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

package org.jasig.portal.car;

import org.xml.sax.ContentHandler;

/**
 * Holds a Path object for which it watches and answers true when that
 * path is seen. Also holds onto the handler for the sub-tree below
 * that path.
 * 
 * @author mboyd
 * @version $Revision$ $Date$
 **/
class PathRouter
{
    Path pathLookedFor = null;
    ContentHandler handler = null;

    PathRouter(Path p, ContentHandler handler)
    {
        this.pathLookedFor = p;
        this.handler = handler;
    }

    ContentHandler handler()
    {
        return handler;
    }

    boolean looksFor(Path aPath)
    {
        return pathLookedFor.equals(aPath);

    }
}
