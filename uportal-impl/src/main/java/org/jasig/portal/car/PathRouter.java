/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
