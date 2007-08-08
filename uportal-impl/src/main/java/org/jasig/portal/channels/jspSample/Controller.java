package org.jasig.portal.channels.jspSample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.channels.jsp.*;

/**
 * Channel that portrays usage of the JSP channel type.
 * 
 * @author Mark Boyd
 */
public class Controller implements IController
{
    public static final String RCS_ID = "@(#) $Header$";

    private static final String JSP = "events.view";
    private ChannelCacheKey mCacheKey = null;
    Map<String, Object> jspBeans = new HashMap<String, Object>();
    ArrayList<String> events = new ArrayList<String>();

    /**
     * Set up cache key and the map of objects that will appear on the request.
     *
     * @see org.jasig.portal.channels.jsp.IController#getJspToRender()
     */
    public void setStaticData(ChannelStaticData csd)
    {
        mCacheKey = new ChannelCacheKey();
        mCacheKey.setKey(csd.getChannelSubscribeId());
        mCacheKey.setKeyValidity(new Object());
        
        // this "events" list will appear on the request object for the jsp
        jspBeans.put("events", events);
    }

    /**
     * Return our map of objects made available to the jsp.
     */
    public Map processRuntimeData(ChannelRuntimeData drd, HttpSession s)
    {
        return jspBeans;
    }

    /**
     * Return a map jsp pages and their keys. Which one should be used will
     * be indicated for a rendering cycle via the getJspToRender() method.
     *
     */
    public Map getJspMap()
    {
        Map jsps = new HashMap();
        jsps.put(JSP, "events.jsp");
        return jsps;
    }

    /**
     * Return the key of the jsp to render. Only one in this simple channel.
     */
    public String getJspToRender()
    {
        return JSP;
    }

    /**
     * Record in our list that a new event has occurred.
     * 
     * @see org.jasig.portal.channels.jsp.IController#receiveEvent(org.jasig.portal.PortalEvent)
     */
    public void receiveEvent(PortalEvent pe)
    {
        events.add("Message "+ pe.getEventName());
        mCacheKey.setKeyValidity(new Object());
    }

    /**
     * Returns our cache key used to determine cache status.
     * 
     * @see org.jasig.portal.ICacheable#generateKey()
     */
    public ChannelCacheKey generateKey()
    {
        return mCacheKey;
    }

    /**
     * Tests to see if our validity object has changed and if so then the cache
     * is stale.
     * 
     * @see org.jasig.portal.ICacheable#isCacheValid(java.lang.Object)
     */
    public boolean isCacheValid(Object validity)
    {
        return validity == mCacheKey.getKeyValidity();
    }
}
