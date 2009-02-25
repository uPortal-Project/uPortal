/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.spring.properties;

import java.beans.PropertyEditorSupport;

import javax.portlet.PortletMode;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletModeEditor extends PropertyEditorSupport {
    /* (non-Javadoc)
     * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.setValue(new PortletMode(text));
    }
}
