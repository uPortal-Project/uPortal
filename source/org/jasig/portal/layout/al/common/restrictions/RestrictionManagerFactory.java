/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.ILayout;
import org.jasig.portal.properties.PropertiesManager;


/**
 * A factory class for obtaining {@link IRestrictionManager} implementations.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.1
 */
public class RestrictionManagerFactory {
	
	private static final Log log = LogFactory.getLog(RestrictionManagerFactory.class);
    
    private static Class restrictionManagerClass=ALRestrictionManager.class;

    static {
        // Retrieve the class name of the core IRestrictionManager implementation
        String className = PropertiesManager.getProperty("org.jasig.portal.layout.restrictions.IRestrictionManager.implementation");
        if (className == null)
            log.error( "RestrictionManagerFactory: org.jasig.portal.layout.restrictions.IRestrictionManager.implementation must be specified in portal.properties");
        try {
            Class newClass = Class.forName(className);
            restrictionManagerClass=newClass;
        } catch (Exception e) {
            log.error( "RestrictionManagerFactory: Could not instantiate " + className, e);
        }
    }

    /**
     * Obtain a regular restriction manager implementation
     *
     * @return an <code>IRestrictionManager</code> value
     */
    public static IRestrictionManager getRestrictionManager(ILayout layout) throws PortalException {
        try {
            IRestrictionManager restrictionManager = (IRestrictionManager) restrictionManagerClass.newInstance();
            restrictionManager.setUserLayout(layout);
            return restrictionManager;
        } catch (Exception e) {
            throw new PortalException("Unable to instantiate a \""+restrictionManagerClass.getName()+"\"",e);
        }
    }
    
}
