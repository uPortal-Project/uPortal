/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.spring.properties;

import java.beans.PropertyEditorSupport;

import javax.portlet.WindowState;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class WindowStateEditor extends PropertyEditorSupport {
    /* (non-Javadoc)
     * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.setValue(new WindowState(text));
    }
}
