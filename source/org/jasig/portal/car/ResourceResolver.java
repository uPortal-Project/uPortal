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
