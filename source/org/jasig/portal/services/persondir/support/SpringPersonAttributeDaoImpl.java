/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.spring.PortalApplicationContextFacade;
import org.springframework.beans.factory.BeanFactory;

/**
 * This {@link IPersonAttributeDao} implementation uses the
 * {@link PortalApplicationContextFacade} to receive an {@link IPersonAttributeDao}
 * implementation to which it delegates all {@link IPersonAttributeDao}
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class SpringPersonAttributeDaoImpl implements IPersonAttributeDao {
    private static final Log LOG = LogFactory.getLog(SpringPersonAttributeDaoImpl.class);
    
    /**
     * IPersonAttributeDao implementation obtained from Spring beanfactory.
     */
    private IPersonAttributeDao delegate;
    
    /**
     * Creates a new {@link IPersonAttributeDao} using beans configured
     * in the uPortal application context.
     */
    public SpringPersonAttributeDaoImpl() {
        final BeanFactory factory = PortalApplicationContextFacade.getPortalApplicationContext();
            
        final Object objectDelegate = factory.getBean("personDirectory");
        
        if (objectDelegate == null)
            throw new NullPointerException("Spring config file did not declare a bean named 'personDirectory'.");
        
        if (!(objectDelegate instanceof IPersonAttributeDao))
            throw new ClassCastException("Spring config file declared a 'personDirectory' bean which was not an instance of IPersonAttributeDao.");
        
        this.delegate = (IPersonAttributeDao)objectDelegate;
    }

    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getDefaultAttributeName()
     */
    public String getDefaultAttributeName() {
        return delegate.getDefaultAttributeName();
    }
    
    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        return delegate.getPossibleUserAttributeNames();
    }
    
    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map getUserAttributes(final Map seed) {
        return delegate.getUserAttributes(seed);
    }
    
    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getUserAttributes(java.lang.String)
     */
    public Map getUserAttributes(final String uid) {
        return delegate.getUserAttributes(uid);
    }

}
