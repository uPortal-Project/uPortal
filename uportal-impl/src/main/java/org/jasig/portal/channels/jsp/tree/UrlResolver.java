/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.jsp.tree;

import java.util.HashMap;

/**
 * Special class used in the tree rendering JSP to translate supported JSP Map
 * semantics to dynamic lookup of a URL for the supported tree actions of 
 * expanding or collapsing children and showing or hiding aspects.
 * 
 * @author Mark Boyd
 *
 */
final class UrlResolver extends HashMap
{
    private ITreeActionUrlResolver resolver = null;
    private int urlType = -1;
    
    UrlResolver(ITreeActionUrlResolver resolver, int urlType)
    {
        this.resolver = resolver;
        this.urlType = urlType;
    }
    
    public Object get(Object key)
    {
        return resolver.getTreeActionUrl(urlType, (String) key);
    }
}
