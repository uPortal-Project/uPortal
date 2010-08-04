/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.om;

import org.apache.pluto.internal.InternalPortletPreference;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletPreference extends InternalPortletPreference {

    /**
     * Sets the read only state of the preference
     */
    void setReadOnly(boolean readOnly);
}
