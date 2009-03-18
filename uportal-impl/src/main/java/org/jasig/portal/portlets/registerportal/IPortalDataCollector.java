/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal;

/**
 * Used to get a single piece of data about the portal
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalDataCollector<T> {
    /**
     * The key that identifies the data being retrieved. Will never be null and must
     * be immutable (subsiquent calls to the the method will always return the same key).
     * 
     * @return The key that identifies the data being retrieved. Will never be null
     */
    public String getKey();
    
    /**
     * @return The data, current as of this call.
     */
    public T getData();
}
