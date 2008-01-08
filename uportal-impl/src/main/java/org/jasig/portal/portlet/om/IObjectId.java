/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.om;

import java.io.Serializable;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IObjectId extends Serializable {
    /**
     * Returns the unique string ID of a object
     * <p>
     * Depending on the implementation of <code>toString()</code> is dangerous,
     * because the original implementation in <code>Object</code> is not
     * qualified.
     * </p>
     * @return the unique string ID of a object
     */
    public String getStringId();
}
