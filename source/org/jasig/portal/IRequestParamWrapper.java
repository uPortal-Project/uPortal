/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */
package org.jasig.portal;

import javax.servlet.http.HttpServletRequest;

public interface IRequestParamWrapper extends HttpServletRequest {
    /**
     * Return a String[] for this parameter
     * @param name the parameter name
     * @return String[] if parameter is not an Object[]
     */
    public String[] getParameterValues(String name);

    /**
     * Return the Object represented by this parameter name
     * @param name the parameter name
     * @return Object
     */
    public Object[] getObjectParameterValues(String name);
}
