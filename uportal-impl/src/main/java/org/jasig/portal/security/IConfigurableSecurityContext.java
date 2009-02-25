/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
