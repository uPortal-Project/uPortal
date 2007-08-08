/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;

import java.util.Properties;


/**
 * Allows an ISecurityContext to note that it can have a Properties passed to it
 * via a setter method.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public interface IConfigurableSecurityContext extends ISecurityContext {
    
    /**
     * Method used to set the properties of the SecurityContext.
     * 
     * @param props The properties to set on the SecurityContext.
     */
    public abstract void setProperties(Properties props);
}
