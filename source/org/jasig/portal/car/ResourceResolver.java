/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.car;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * Enables resolving of external resources specified in xsl:import or xsl:include
 * elements or the document() allowing such resources to also be found
 * within CARs.
 *
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
public class ResourceResolver
    implements URIResolver
{
    /**
     * Allows external resources specified in xsl:import or xsl:include
     * elements or the document() method to be resolved in custom manner.
     * This allows resources in CARs to be accessed readily via these xsl
     * constructs. If the specified resource is not found within a CAR then
     * a null value is returned allowing the processor to try and resolve it
     * in some other way.
     * 
     * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
     */
    public Source resolve(String href, String base) 
    throws TransformerException
    {
        // first try loading a resource relative to the base URI
        if ( base != null )
        {
            try
            {
                URL ctx = new URL(base);
                URL res = new URL(ctx, href);
                InputStream is = res.openStream();
                return new StreamSource(is, res.toExternalForm());
            }
            catch (MalformedURLException e)
            {
            } 
            catch (IOException e1)
            {
            }
        }
        // if can't load relative to base see if this is a resource sitting in
        // a channel archive.
        URL res = CarResources.getInstance().getClassLoader().getResource(href);
        
        if (res != null)
        {
            InputStream is;
            try
            {
                is = res.openStream();
                return new StreamSource(is, res.toExternalForm());
            }
            catch (IOException e)
            {
            }
        }
        // oh well. Did the best we could. Let the default handling try.
        return null;
    }
}
