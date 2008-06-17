/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.channels.guide;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.channels.CAbstractXslt;
import org.jasig.portal.utils.DTDResolver;
import org.jasig.portal.utils.ResourceLoader;
import org.w3c.dom.Document;

/**
 * A simple channel for introducing the capabilities of DLM in the portal. This
 * channel gets its content from a file, "dlmIntro.html", included with the
 * channel's source.
 * 
 * @author mboyd@sungardsct.com
 */
public class DlmIntroChannel extends CAbstractXslt implements ICacheable
{
    private Map cacheKeys = new HashMap();
    private String mediaBase = null;
    private static final String CONTENT_FILE = "dlmIntro.xml";
    private static final String STYLESHEET_FILE = "dlmIntro.xsl";
    private String currentSection = "1";

    /**
     * Create a globally shared cache key for a section with the validity
     * object being a string of the long value representing the last time
     * that the content file was updated.
     * 
     * @return
     */
    private ChannelCacheKey initKey(String id)
    {
        ChannelCacheKey key = new ChannelCacheKey();
        key.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
        key.setKey(this.getClass().getName()+":"+id);

        try
        {
            long contentModified = ResourceLoader.getResourceLastModified(this.getClass(), CONTENT_FILE);
            long stylesheetModified = ResourceLoader.getResourceLastModified(this.getClass(), STYLESHEET_FILE);

            if (contentModified > stylesheetModified)
                key.setKeyValidity("" + contentModified);
            else
                key.setKeyValidity("" + stylesheetModified);
        } catch (ResourceMissingException e)
        {
            // if we can't tell when the file was modified then it will 
            // force rendering everytime. This should never happen but 
            // handles that scenario if it does occur.
            key.setKeyValidity("1"); 
        }
        return key;
    }
    /**
     * Return our cache key which is a system cache key so all users share 
     * the same output and it never changes meaning that it never regenerates.
     * 
     * @see org.jasig.portal.ICacheable#generateKey()
     */
    public ChannelCacheKey generateKey()
    {
        ChannelCacheKey key = (ChannelCacheKey) this.cacheKeys
                .get(currentSection);
        if (key == null) // haven't asked for this section yet
            key = initKey(currentSection);

        return key;
    }

    public boolean isCacheValid(Object validity)
    {
        try
        {
            long lastRefreshed = Long.parseLong((String) validity);
            long contentModified = ResourceLoader.getResourceLastModified(this.getClass(), CONTENT_FILE);
            long stylesheetModified = ResourceLoader.getResourceLastModified(this.getClass(), STYLESHEET_FILE);

            if (contentModified > lastRefreshed ||
                    stylesheetModified > lastRefreshed)
                return false;
            return true;
        } catch (ResourceMissingException e)
        {
            // exception can't be thrown from here so toss this to force it to
            // look again for the file in renderCharacters and toss the 
            // exception there where it will be seen in the portal.
            return false;
        }
    }

    /**
     * Sets up the base media URL if not done already and determines which
     * section is desired by the user if any.
     */
    @Override
    protected void runtimeDataSet() throws PortalException
    {
        ChannelRuntimeData crd = getRuntimeData();
        // get an appropriate media path for this channel's images
        if ( mediaBase == null )
        {
            mediaBase = crd.getBaseMediaURL( this );
            String cls = getClass().getName();
            String pkg = cls.substring( 0, cls.lastIndexOf( '.' ) );
            mediaBase = mediaBase + pkg.replace( '.', '/' ) + '/';
        }
        String section = crd.getParameter("section");
        
        if (section == null || section.equals(""))
            currentSection = "1";
        else currentSection = section;
    }
	
	public void receiveEvent(PortalEvent ev) {
		// do nothing on events
	}

	@Override
	protected Map getStylesheetParams() throws Exception {
		// TODO Auto-generated method stub
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("baseActionUrl", this.getRuntimeData().getBaseActionURL(true));
		paramMap.put("baseMediaUrl", mediaBase);
		paramMap.put("selectedSection", currentSection);
		return paramMap;
	}
	
	@Override
	protected Document getXml() throws Exception {

        InputStream contents = ResourceLoader.getResourceAsStream(this.getClass(),
                CONTENT_FILE);
		
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        DTDResolver dtdResolver = new DTDResolver();
        docBuilder.setEntityResolver(dtdResolver);
        Document dom = docBuilder.parse(contents);
		return dom;
	}
	
	@Override
	protected String getXsltUri() throws Exception {
		return STYLESHEET_FILE;
	}
}
