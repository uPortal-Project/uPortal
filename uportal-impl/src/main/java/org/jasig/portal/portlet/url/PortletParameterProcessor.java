/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.url.IWritableHttpServletRequest;
import org.jasig.portal.url.processing.IRequestParameterProcessor;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletParameterProcessor implements IRequestParameterProcessor {

    /* (non-Javadoc)
     * @see org.jasig.portal.url.processing.IRequestParameterProcessor#processParameters(org.jasig.portal.url.IWritableHttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public boolean processParameters(IWritableHttpServletRequest req, HttpServletResponse res) {
        // TODO Auto-generated method stub
        return false;
    }

}
