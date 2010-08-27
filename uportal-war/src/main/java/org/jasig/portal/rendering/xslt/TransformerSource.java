/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering.xslt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;

import org.jasig.portal.utils.cache.CacheKey;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface TransformerSource {
    /**
     * Gets a XSLT Transformer
     */
    public Transformer getTransformer(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * Gets the CacheKey for the Transformer
     */
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response);
}
