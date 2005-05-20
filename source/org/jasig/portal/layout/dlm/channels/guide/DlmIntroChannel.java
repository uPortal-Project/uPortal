/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.channels.guide;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.ICharacterChannel;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;

/**
 * A simple channel for introducing the capabilities of DLM in the portal. This
 * channel gets its content from a file, "dlmIntro.html", included with the
 * channel's source.
 * 
 * @author mboyd@sungardsct.com
 */
public class DlmIntroChannel extends BaseChannel implements ICacheable,
        ICharacterChannel
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
            File contentFile = ResourceLoader.getResourceAsFile(this.getClass(),
                    CONTENT_FILE);
            long contentModified = contentFile.lastModified();
            File stylesheetFile = ResourceLoader.getResourceAsFile(this.getClass(),
                    STYLESHEET_FILE);
            long stylesheetModified = stylesheetFile.lastModified();
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

    /**
     * Always returns true;
     * @see org.jasig.portal.ICacheable#isCacheValid(java.lang.Object)
     */
    public boolean isCacheValid(Object validity)
    {
        try
        {
            long lastRefreshed = Long.parseLong((String) validity);
            File contentFile = ResourceLoader.getResourceAsFile(this.getClass(),
                    CONTENT_FILE);
            File stylesheetFile = ResourceLoader.getResourceAsFile(this.getClass(),
                    STYLESHEET_FILE);
            long contentModified = contentFile.lastModified();
            // TODO remove stylesheet checking after development done since
            // it gets cached and regular users can't purge the cache.
            long stylesheetModified = stylesheetFile.lastModified();

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
     * Load and return the content of the channel. This comes from an html file
     * included with the channel's source files.
     * 
     * @see org.jasig.portal.ICharacterChannel#renderCharacters(java.io.PrintWriter)
     */
    public void renderCharacters(PrintWriter pw) throws PortalException
    {
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MMMMM.dd hh:mm aaa, z");
        try
        {
          /*  pw.println("<span class='uportal-crumbtrail'>Rendered at: "
                    + sdf.format(new Date(System.currentTimeMillis())) 
                    + "</span>");
                    */
            XSLT X;
            XSLT xslt = new XSLT(this);
            xslt.setXSL(STYLESHEET_FILE);
            
            File contents = ResourceLoader.getResourceAsFile(
                    this.getClass(), CONTENT_FILE);
            xslt.setXML(contents);
            ByteArrayOutputStream results = new ByteArrayOutputStream();
            xslt.setTarget(results);
            
            xslt.setStylesheetParameter("baseActionUrl", this.runtimeData
                    .getBaseActionURL(true));
            xslt.setStylesheetParameter("baseMediaUrl", mediaBase);
            xslt.setStylesheetParameter("selectedSection", currentSection);
            xslt.transform();
            String html = results.toString();
            pw.print(html);
        } catch (Exception e)
        {
            throw new PortalException("Problem generating content.", e);
        }
    }
    /**
     * Sets up the base media URL if not done already and determines which 
     * section is desired by the user if any.
     * 
     * @see org.jasig.portal.IChannel#setRuntimeData(org.jasig.portal.ChannelRuntimeData)
     */
    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException
    {
        super.setRuntimeData(rd);
        
        // get an appropriate media path for this channel's images
        if ( mediaBase == null )
        {
            mediaBase = rd.getBaseMediaURL( this );
            String cls = getClass().getName();
            String pkg = cls.substring( 0, cls.lastIndexOf( '.' ) );
            mediaBase = mediaBase + pkg.replace( '.', '/' ) + '/';
        }
        String section = (String) this.runtimeData.getParameter("section");
        
        if (section == null || section.equals(""))
            currentSection = "1";
        else currentSection = section;
    }
}
