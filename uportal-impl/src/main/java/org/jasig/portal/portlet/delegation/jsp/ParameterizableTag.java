/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.delegation.jsp;

/**
 * Used by {@link ParamTag} to add parameters to another tag
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ParameterizableTag {
    /**
     * Adds the name and value parameter pair. If the parameter already exists the value
     * is added to the list of values for the parameters.
     */
    public void addParameter(String name, String value);
}
