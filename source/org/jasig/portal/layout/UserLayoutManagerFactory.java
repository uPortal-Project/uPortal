package org.jasig.portal.layout;

import java.lang.reflect.Constructor;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.UserProfile;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.services.LogService;
import org.jasig.portal.security.IPerson;


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
            LogService.instance().log(LogService.ERROR, "UserLayoutManagerFactory: org.jasig.portal.layout.UserLayoutManagerFactory.coreImplementation must be specified in portal.properties");
        try {
            Class newClass = Class.forName(className);
            coreUserLayoutManagerImpl=newClass;
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, "UserLayoutManagerFactory: Could not instantiate " + className, e);
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
            return (IUserLayoutManager)c.newInstance(cArgs);
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
