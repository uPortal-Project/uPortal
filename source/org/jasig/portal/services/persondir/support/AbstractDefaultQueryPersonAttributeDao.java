/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPerson;


/**
 * Several {@link org.jasig.portal.services.persondir.support.IPersonAttributeDao}
 * implementations need to provide a default attribute for running queries. The
 * following code is generally common between them.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision $
 */
public abstract class AbstractDefaultQueryPersonAttributeDao implements IPersonAttributeDao {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    /**
     * Defaults attribute to use for a simple query
     */
    private String defaultAttribute = IPerson.USERNAME;
    
    
    /**
     * Uses {@link Collections#singletonMap(java.lang.Object, java.lang.Object)}
     * to create a seed with the value rerturned by 
     * {@link #getDefaultAttributeName()} as the key and <code>uid</code>
     * as the value. {@link IPersonAttributeDao#getUserAttributes(Map)} is 
     * called with the new {@link Map} as the argument.
     * 
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getUserAttributes(java.lang.String)
     */
    public final Map getUserAttributes(final String uid) {
        final Map seed = Collections.singletonMap(this.getDefaultAttributeName(), uid);
        
        return this.getUserAttributes(seed);
    }

    
    /**
     * Returns the attribute set by {@link #setDefaultAttributeName(String)} or
     * if it has not been called the default value "uid" is returned.
     * 
     * @return The default single string query attribute
     */
    public final String getDefaultAttributeName() {
        return this.defaultAttribute;
    }
    
    /**
     * Sets the attribute to use for {@link #getUserAttributes(String)} queries.
     * It cannot be <code>null</code>.
     * 
     * @param name The attribute name to set as default.
     * @throws IllegalArgumentException if <code>name</code> is <code>null</code>.
     */
    public final void setDefaultAttributeName(final String name) {
        if (name == null)
            throw new IllegalArgumentException("The default attribute name must be null");

        this.defaultAttribute = name;
    }
}
