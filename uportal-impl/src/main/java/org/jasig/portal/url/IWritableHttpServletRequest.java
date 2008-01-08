/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.url;

import javax.servlet.http.HttpServletRequest;

/**
 * IWritableHttpServletRequest is an extension of the {@link javax.servlet.http.HttpServletRequest} interface,
 * which allows to manage (add, remove and modify) request parameters.
 *
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision: 11911 $
 */
public interface IWritableHttpServletRequest extends HttpServletRequest {
    /**
     * Adds a value of a request parameter
     * 
     * @param name The name of the request parameter
     * @param value The value of the request parameter
     * @return <code>true</code> if the parameter already existsted
     * @throws IllegalArgumentException if name or value are null
     */
    public boolean addParameterValue(String name, String value);

    /**
     * Sets a value of request parameter (overwriting any previous values)
     * 
     * @param name The name of the request parameter
     * @param value The value of the request parameter
     * @return <code>true</code> if the parameter already existsted
     * @throws IllegalArgumentException if name or value are null
     */
    public boolean setParameterValue(String name, String value);

    /**
     * Sets a set of values of request parameter (overwriting any previous values)
     * 
     * @param name The name of the request parameter
     * @param values The values of the request parameter
     * @return <code>true</code> if the parameter already existsted
     * @throws IllegalArgumentException if name or values are null or if the values array contains a null
     */
    public boolean setParameterValues(String name, String[] values);

    /**
     * Removes any values of an existing parameter
     * 
     * @param name The name of the request parameter to remove
     * @return <code>true</code> if the parameter existed
     * @throws IllegalArgumentException if name is null
     */
    public boolean deleteParameter(String name);

    /**
     * Removes all occurances of the specific value of a request parameter
     * 
     * @param name The name of the request parameter
     * @param value The value to remove all occurances of
     * @return <code>true</code> if the parameter value existed
     * @throws IllegalArgumentException if name or value are null
     */
    public boolean deleteParameterValue(String name, String value);

}
