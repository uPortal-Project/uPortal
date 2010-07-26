/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.registry;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class NotAPortletException extends DataRetrievalFailureException {

    public NotAPortletException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public NotAPortletException(String msg) {
        super(msg);
    }
}
