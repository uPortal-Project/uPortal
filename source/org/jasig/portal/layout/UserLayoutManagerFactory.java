/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.layout;

import java.lang.reflect.Constructor;

import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.UserProfile;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.StatsRecorder;


/**
 * A factory class for obtaining {@link IUserLayoutManager} implementations.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public class UserLayoutManagerFactory {
    static Class coreUserLayoutManagerImpl=SimpleUserLayoutManager.class;

    static {
        // Retrieve the class name of the core IUserLayoutManager implementation
        String className = PropertiesManager.getProperty("org.jasig.portal.layout.UserLayoutManagerFactory.coreImplementation");
        if (className == null)
            LogService.log(LogService.ERROR, "UserLayoutManagerFactory: org.jasig.portal.layout.UserLayoutManagerFactory.coreImplementation must be specified in portal.properties");
        try {
            Class newClass = Class.forName(className);
            coreUserLayoutManagerImpl=newClass;
        } catch (Exception e) {
            LogService.log(LogService.ERROR, "UserLayoutManagerFactory: Could not instantiate " + className, e);
        }
    }

    /**
     * Obtain a regular user layout manager implementation
     *
     * @return an <code>IUserLayoutManager</code> value
     */
    public static IUserLayoutManager getUserLayoutManager(IPerson person, UserProfile profile) throws PortalException {
        try {
            Class[] cArgsClasses={IPerson.class,UserProfile.class,IUserLayoutStore.class};
            Constructor c=coreUserLayoutManagerImpl.getConstructor(cArgsClasses);
            Object[] cArgs={person,profile,UserLayoutStoreFactory.getUserLayoutStoreImpl()};
            IUserLayoutManager ulm = (IUserLayoutManager)c.newInstance(cArgs);
            ulm.addLayoutEventListener(StatsRecorder.newLayoutEventListener(person, profile));
            
            // Wrap the implementation to provide lookup by fname
            // support which basically merges a non-persisted channel
            // into the layout
            IUserLayoutManager ulmWrapper = new TransientUserLayoutManagerWrapper(ulm);
            return ulmWrapper;
        } catch (Exception e) {
            throw new PortalException("Unable to instantiate a \""+coreUserLayoutManagerImpl.getName()+"\"",e);
        }
    }

    /**
     * Returns an immutable version of a user layout manager.
     *
     * @param man an <code>IUserLayoutManager</code> value
     * @return an immutable <code>IUserLayoutManager</code> value
     * @exception PortalException if an error occurs
     */
    public static IUserLayoutManager immutableUserLayoutManager(IUserLayoutManager man) throws PortalException {
        return new ImmutableUserLayoutManagerWrapper(man);
    }
}
