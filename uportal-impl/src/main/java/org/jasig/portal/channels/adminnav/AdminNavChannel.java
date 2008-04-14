/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.channels.adminnav; 


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.xml.sax.ContentHandler;


/**
 * This channel is a wrapper around an implementation that provides urls (links)
 * to other channels. Typically, this is by functional name so that when
 * selected those channels are rendered in focus mode. Links are added
 * dynamically by calling the addLink() method which is delegated to the
 * plugged-in implementation.
 * 
 * CHeader channel presents the "Channel Admin" link which brings this channel
 * into focused mode when selected. CHeader delegates to canAccess() to
 * determine if there is any content within this channel that is accessible to
 * the current user. If so then it will render the ChannelAdmin link. Similarly,
 * when the implementation renders it should only present those links to
 * channels for which the current user has authorization.
 * 
 * Localization of link labels is supported through instances of ILabelResolver
 * passed in at link registration time.
 * 
 * A channel can provide links to this channel by calling their
 * ChannelRuntimeData.getFnameActionURL() passing the functional name of this
 * channel "admin.navigation.links".
 * 
 * @author Keith Stacks, kstacks@sungardsct.com
 * @author Mark Boyd, mboyd@sungardsct.com
 */
public class AdminNavChannel extends BaseChannel implements ICacheable
{
    // Used for informational, error, and debug logging
    private static Log LOG = LogFactory.getLog(AdminNavChannel.class);

    // Internal model of channel.
    private static INavigationModel model = null;
    
    private static final String IMPL_CLASS = 
        "org.jasig.portal.channels.adminnav.AdminNavigation.implementation";

    private static Throwable INSTANTIATION_PROBLEM = null;
    /********** Static initializer to load declared implementation *********/
    
    private static synchronized INavigationModel getNavModel()
    {
        if (model != null) {
            return model;
        }
        
        
        try
        {
            String implClass = PropertiesManager.getProperty(IMPL_CLASS);
            Class cls = Class.forName(implClass);
            model = (INavigationModel) cls.newInstance();
        }
        catch( Throwable t )
        {
            INSTANTIATION_PROBLEM = t;
            LOG.error("Unable to load implementation of administrative " +
                    "navigational links.", t);
        }
        
        return model;
    }
    
    /********* Channel Methods ***********/

    /**
     * Checks to see if the rendering document needs to be updated for the 
     * user's locale.
     */
    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException
    {
        final INavigationModel model = getNavModel();
        if (model != null)
            model.setRuntimeData(rd);
    }
    /**
     *  Render the links.
     *
     *  @param  out  stream that handles output
     */
    public void renderXML( ContentHandler out )
        throws PortalException
    {
        final INavigationModel model = getNavModel();
        if (model != null)
            model.renderXML(out);
    }

    /********* Worker methods *************/

    /**
     * Delegates to the plugged-in model to answer if the user represented by
     * the passed-in authorization principal has access to this channel. If any
     * of the information available in the implementation should be accessible
     * to the user then the model should return true. 
     */
    public static boolean canAccess(IAuthorizationPrincipal ap)
    {
        final INavigationModel model = getNavModel();
        if (model != null)
            return model.canAccess(ap);
        else
            return false;
    }

    /**
     * Returns an object that can be used to add links at runtime to the 
     * underlying administrative navigational links model.
     */
    public static ILinkRegistrar getLinkRegistrar()
    {
        final INavigationModel model = getNavModel();
        return model;
    }
    
    /**
     * Delegates to the plugged-in implementation to generate a key as part of
     * its implementation of the ICacheable interface.
     * 
     * @see org.jasig.portal.ICacheable#generateKey()
     */
    public ChannelCacheKey generateKey()
    {
        final INavigationModel model = getNavModel();
        if (model != null)
            return model.generateKey();
        else 
            return null;
    }
    
    /**
     * Delegates to the plugged-in implementation to determine using the 
     * passed-in validity object if cached output can be reused as part of
     * its implementation of the ICacheable interface.
     * 
     * @see org.jasig.portal.ICacheable#isCacheValid(java.lang.Object)
     */
    public boolean isCacheValid(Object validity)
    {
        final INavigationModel model = getNavModel();
        if (model != null)
            return model.isCacheValid(validity);
        return false;
    }
    
    /**
     * Passes the passed-in channel static configuration information to the
     * plugged-in model.
     */
    public void setStaticData(ChannelStaticData sd) throws PortalException
    {
        final INavigationModel model = getNavModel();
        if (model != null)
            model.setStaticData(sd);
        else
            throw new PortalException("No implementation available due to " +
                    "the following problem.", INSTANTIATION_PROBLEM);
    }
}
