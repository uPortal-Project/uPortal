/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.utils.jsp;

import java.util.Collection;
import java.util.Map;

/**
 * JSP Static utility functions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class Util {
    
    public static boolean contains(Collection<?> coll, Object o) {
        return coll != null && coll.contains(o);
    }

    public static boolean containsKey(Map<?, ?> map, Object o) {
        return map != null && map.containsKey(o);
    }

    public static boolean containsValue(Map<?, ?> map, Object o) {
        return map != null && map.containsValue(o);
    }

}
