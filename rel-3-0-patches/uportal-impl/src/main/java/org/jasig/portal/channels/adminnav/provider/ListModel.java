/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.channels.adminnav.provider;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.Constants;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.channels.adminnav.ILabelResolver;
import org.jasig.portal.channels.adminnav.INavigationModel;
import org.jasig.portal.channels.adminnav.ResourceBundleResolver;
import org.jasig.portal.channels.adminnav.XMLLinksFileLoader;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * This channel provides a flat list of urls (links) to other channels using the
 * channel functional names. When selected these links cause that channel to be
 * rendered in focused mode.
 * 
 * This implementation supports both static and dynamic registration. Dynamic
 * registration takes place any time via calls to addLink(). The set of static
 * links is defined in /properties/adminNav.xml. Their locale specific text is
 * loaded from /properties/adminNav.properties or a suitable derivative for a
 * specific locale.
 * 
 * CHeader channel presents the "Channel Admin" link which brings this channel
 * into focused mode when selected. That link will only render if this channel
 * has registered links that point to channels for which the current user has
 * authorization. Similarly, when this channel renders, it only presents those
 * links to channels for which the current user has authorization.
 * 
 * Localization of link labels is supported through instances of ILabelResolver
 * passed in at link registration time.
 * 
 * @author Keith Stacks, kstacks@sungardsct.com
 * @author Mark Boyd, mboyd@sungardsct.com
 */
public class ListModel extends BaseChannel implements INavigationModel
{
    
    // Stylesheet file should be co-located with this java file.
    private static final String XSL_LOCATION = "navigation.xsl";

    // complete list of nav links available
    private static LinkedList cLinks = new LinkedList();

    // Rendering Document
    private Document mDoc = null;
    
    // property used to acquire localized header label
    private static final String HEADER_PROPERTY = "admin.nav.header";
    
    // config file location
    private static final String CONFIG_FILE = "/properties/adminNav.xml";
    
    // resource bundle base for config file labels
    private static final String BUNDLE_BASE = "properties.adminNav";
    
    // instance of our label resolver
    private static final ILabelResolver resolver 
        = new ResourceBundleResolver(BUNDLE_BASE);

    // Used for informational, error, and debug logging
    private static Log LOG = LogFactory.getLog(ListModel.class);

    
    // The cache key used to indicate whether the cached rendering output is
    // still valid or should be re-rendered.
    private ChannelCacheKey cacheKey = new ChannelCacheKey();

    public ListModel() 
    {
        XMLLinksFileLoader loader = new XMLLinksFileLoader(CONFIG_FILE, this,
                resolver);
    }

    /************* Utility class **************/
    
    private static class Link
    {
        String labelId = null;
        String url = null;
        ILabelResolver resolver = null;
        int publishIdOfTargetChannel = -1;
        
        public Link(String labelId, ILabelResolver resolver, String url, int pubIdOfTargetChannel)
        {
            this.labelId = labelId;
            this.resolver = resolver;
            this.url = url;
            this.publishIdOfTargetChannel = pubIdOfTargetChannel;
        }
    }
    
    /**
     * Returns true if the user represented by the passed-in authorization 
     * principal returns has access to any of the channels pointed to by 
     * registered links. This is used by CHeader to determine if the "Channel
     * Admin" link should be rendered. 
     */
    public boolean canAccess(IAuthorizationPrincipal ap)
    {
        for(Iterator iter = cLinks.iterator(); iter.hasNext(); )
        {
            Link link = (Link) iter.next();
            try
            {
                if (ap.canSubscribe(link.publishIdOfTargetChannel))
                return true;
            }
            catch (AuthorizationException e)
            {
                if (LOG.isDebugEnabled())
                    LOG.debug("Unable to determine if principal " +
                            ap.getPrincipalString() +
                            " can subscribe to channel with publish ID " + 
                            link.publishIdOfTargetChannel + ", and url " +
                            link.url, e);
            }
        }
        return false;
    }

    /**
     * Add a link to the channel indicated by the passed in functional name to
     * the list of links located in the admin navigation list. The label will be
     * the text shown in the UI for the link. The name/value pairs passed in via
     * the parameters argument will be appended as query parameters.
     * 
     * @param fname
     *            the functional name of a published channel. This must not be
     *            null and must correspond to the functional name of an already
     *            published channel.
     * @param labelId
     *            the test that should show in the UI for this link. This must
     *            not be null.
     * @param parameters
     *            additional query parameter name/value pairs to be appended to
     *            the URL if needed for the link. This value can be null if no
     *            additional parameters are needed.
     */
    public void addLink(String fname, String labelId, ILabelResolver resolver,
            Map parameters)
    {
        try
        {
            // first perform some edit checks
            if (fname == null || fname.equals(""))
                throw new Exception("'Functional Name' must be specified.");
                
            if (labelId == null || labelId.equals(""))
            {
                labelId = "unspecified";
                throw new Exception("'Label' must be specified.");
            }
            // now get pub ID of target channel
            IChannelRegistryStore crs =
                ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();
            ChannelDefinition chanDef = crs.getChannelDefinition(fname);
            int pubId = chanDef.getId();
            
            // next build the URL for the link
            String url =
                UPFileSpec.buildUPFile(
                    UPFileSpec.RENDER_METHOD,
                    UPFileSpec.USER_LAYOUT_ROOT_NODE,
                    null,
                    null);

            url = url + "?" + Constants.FNAME_PARAM + "=" + fname;

            if (parameters != null)
            {
                for (Iterator iter = parameters.keySet().iterator();
                    iter.hasNext();
                    )
                {
                    String name = (String) iter.next();
                    String value = (String) parameters.get(name);
                    url += "&" + name + "=" + value;
                }
            }
            cLinks.add(new Link(labelId, resolver, url, pubId));
            
            // force refresh of channel UI.
            cacheKey.setKeyValidity(new Locale("", ""));
        }
        catch (Exception e)
        {
            LOG.error(
                "Unable to add link '" + labelId
                    + "' to administration navigation list.", e);
        }
    }

    /**
     * Return the reused cache key. Only the internal validity is used and 
     * handed back via isCacheValid().
     */
    public ChannelCacheKey generateKey()
    {
        return cacheKey;
    }

    /**
     * The validity object used in our cache key is the locale used to generate
     * the XML for the channel. So cache refresh will only take place when the
     * user changes their locale.
     */
    public boolean isCacheValid(Object validity)
    {
        if (validity != cacheKey.getKeyValidity())
            return false;
        return true;
    }
    
    
    public void setStaticData(ChannelStaticData sd) throws PortalException
    {
        super.setStaticData(sd);
        cacheKey.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
        cacheKey.setKey(this.getClass().getName() + sd.getChannelSubscribeId());
        cacheKey.setKeyValidity(new Locale("",""));
    }
    
    /**
     * Checks to see if the rendering document needs to be updated for the 
     * user's locale.
     */
    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException
    {
        super.setRuntimeData(rd);
        
        // see if the user has changed their locale since the last time that
        // the model was generated.
        Locale[] locales = rd.getLocales();
        Locale currentLocale = null;
        
        if ( locales == null )
            currentLocale = Locale.US;
        else if (locales.length == 0)
            currentLocale = Locale.US;
        else if (locales[0] == null)
            currentLocale = Locale.US;
        else 
            currentLocale = locales[0];
        
        Locale lastLocale = (Locale) cacheKey.getKeyValidity(); 
        
        if (mDoc == null
                || !lastLocale.toString().equals(currentLocale.toString()))
        {
            generateXML(currentLocale);
            cacheKey.setKeyValidity(currentLocale);
        }
    }

    /**
     *  Render the links.
     *
     *  @param  out  stream that handles output
     */
    public void renderXML( ContentHandler out )
        throws PortalException
    {
        XSLT xslt = new XSLT(this);
        xslt.setXML(mDoc);
        xslt.setXSL(XSL_LOCATION);//optionsLabel
        xslt.setTarget(out);
        xslt.transform();
    }
    
    /**
     * Generates the XML DOM used in rendering the UI.
     **/
    private void generateXML(Locale locale)
    {
        Document doc = DocumentFactory.getNewDocument();
        Element root = doc.createElement("adminurls");
        doc.appendChild(root);
        Element heading = doc.createElement("heading");
        root.appendChild(heading);
        heading.appendChild(doc.createTextNode(resolveLabel(resolver,
                HEADER_PROPERTY, locale)));
        
        IAuthorizationPrincipal ap = staticData.getAuthorizationPrincipal();

        for(Iterator iter = cLinks.iterator(); iter.hasNext(); )
        {
            // determine if user has permission for rendering
            Link link = (Link)iter.next();
            
            try
            {
                if ( ap.canSubscribe(link.publishIdOfTargetChannel) )
                {
                    if (LOG.isDebugEnabled())
                        LOG.debug("User can render channel '"
                                + link.publishIdOfTargetChannel
                                + "' with url '" + link.url + "'");
                    
                    Element adminURLEl = doc.createElement("adminurl");
                    adminURLEl.setAttribute("desc", resolveLabel(link.resolver,
                            link.labelId, locale));
                    adminURLEl.appendChild(doc.createTextNode(link.url));
                    root.appendChild(adminURLEl);
                }
            }
            catch (AuthorizationException e)
            {
                if (LOG.isDebugEnabled())
                    LOG.debug("Unable to add link for channel '"
                            + link.publishIdOfTargetChannel + "' with url '"
                            + link.url + "'");
            }
        }

        mDoc = doc;
    }

    /**
     * Handles resolving labels and providing default if a null value is 
     * returned from a resolver or the resolver tosses a missing resource 
     * exception typical from underlying resource bundle implementations.
     * 
     * @param resolver2
     * @param labelId
     * @param locale
     * @return
     */
    private String resolveLabel(ILabelResolver resolver, String labelId, Locale locale)
    {
        String label = null;
        
        try
        {
            label = resolver.getLabel(labelId, locale);
        }
        catch(MissingResourceException mre)
        {
            // ignore since we handle null below.
            LOG.warn("Could not resolve labelId '" + labelId + "' for locale '" + locale + "' using resolver '" + resolver.getExternalForm() + "'", mre);
        }
        if (label == null)
        {
            StringBuffer sb = new StringBuffer()
            .append("???")
            .append(resolver.getClass().getName());
            
            String resExtForm = resolver.getExternalForm();
            
            if (resExtForm != null && ! resExtForm.equals(""))
                sb.append('{').append(resExtForm).append('}');
            
            sb.append("[")
            .append(labelId)
            .append("]???");
            label = sb.toString();
        }
        return label;
    }
}
