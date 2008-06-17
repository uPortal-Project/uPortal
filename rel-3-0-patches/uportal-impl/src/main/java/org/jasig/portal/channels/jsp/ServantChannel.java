/*
 * Created on Jan 25, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jasig.portal.channels.jsp;

import org.jasig.portal.PortalException;

/**
 * @author Mark Boyd
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ServantChannel extends Channel implements IServantController
{
    /**
     * Custom constructor used to create a an instance of this class
     * that implements the IServant interface. Since servants use the containing
     * channel's ChannelStaticData we don't want to have to corrupt that object
     * by setting the "controllerClass" parameter. Hence this constructor takes
     * the class name of the controller as a parameter. If this value is null 
     * it will result in the lookup in the ChannelStaticData object. 
     * 
     * Furthermore, since this is a servant its instance is unknown to 
     * ChannelManager. Therefore it will never have its PortalControlStructures
     * called which is manditory for the jsp channel type. Hence for using
     * any jsp channel type servant a containing channel must itself be an 
     * IPriviledgedChannel implementation and must pass the 
     * PortalControlStructures to this channel prior to calling setRuntimeData
     * and update them whenever its own setPortalControlStructures()
     * method is called.
     * 
     * WARNING: The jsp channel handles controllers from within CARs in a 
     * special manner to extract classes, properties, and JSPs from the CAR and 
     * make them available to the web server's classloader and JSP compiler.
     * This handling mechanism depends on the controller class not as yet being
     * loaded by the classloader before the jsp channel attempts to load the 
     * class. Therefore, the String passed into this constructor should not be
     * obtained from theControllerClass.getName() unless that class does not
     * reside in a CAR. Such a call causes that class to be loaded and will 
     * prevent the proper deployment of CAR resources for that class.
     * @throws PortalException
     */
    public ServantChannel(String controllerClassName) throws PortalException
    {
        super();
        this.mControllerClassname = controllerClassName;
    }
    
    
    /**
     * Delegates to the controller if it supports IJspChannelServant. Throws an
     * UnsupportedOperationException if it doesn't.
     * 
     * @see org.jasig.portal.channels.jsp.IServantController#isFinished()
     */
    public boolean isFinished()
    {
        if (mController == null)
            throw new NullPointerException("Servant controller has not been " +
                    "supplied.");
        if (mController != null && mController instanceof IServantController)
        {
            return ((IServantController)mController).isFinished();
        }
        throw new UnsupportedOperationException("JSP Controller "
                + mControllerClassname + " does not implement "
                + IServantController.class.getName() + ".");
    }

    /**
     * Delegates to the controller if it supports IJspChannelServant. Throws an
     * UnsupportedOperationException if it doesn't.  
     * 
     * @see org.jasig.portal.channels.jsp.IServantController#getResults()
     * @return Object[] the expected Object type should be documented by the
     *         IController implementation
     */
    public Object[] getResults()
    {
        if (mController == null)
            throw new NullPointerException("Servant controller has not been " +
                    "supplied.");
        if (mController != null && mController instanceof IServantController)
        {
            return ((IServantController)mController).getResults();
        }
        throw new UnsupportedOperationException("JSP Controller "
                + mControllerClassname + " does not implement "
                + IServantController.class.getName() + ".");
    }

    /**
     * Delegates to the controller if it supports IJspChannelServant. Throws an
     * UnsupportedOperationException if it doesn't.
     * 
     * @see org.jasig.portal.channels.jsp.IServantController#configure(java.lang.Object)
     */
    public void configure(Object o)
    {
        if (mController == null)
            throw new NullPointerException("Servant controller has not been " +
                    "supplied.");
        if (mController instanceof IServantController)
        {
            ((IServantController)mController).configure(o);
        }
        else
            throw new UnsupportedOperationException("JSP Controller "
                    + mController.getClass().getName() + " does not implement "
                    + IServantController.class.getName() + ".");
    }
}
