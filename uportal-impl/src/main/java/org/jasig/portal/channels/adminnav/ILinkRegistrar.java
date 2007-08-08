/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.channels.adminnav;

import java.util.Map;

/**
 * Represents an object that can add links to the AdminNavChannel's underlying
 * model.
 * 
 * @author mboyd@sungardsct.com
 *
 */
public interface ILinkRegistrar
{
    /**
     * Dynamically adds a link to the implementation.
     * 
     * @param fname
     * @param labelId
     * @param resolver
     * @param parameters
     */
    public void addLink(String fname, String labelId, ILabelResolver resolver,
            Map parameters);
}
