/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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

import java.util.*;
import javax.servlet.ServletContext;

/**
 * MimeTypeCache.
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
public class MimeTypeCache
{
    public final static String RCS_ID = "@(#) $Header$";

    private static Map cContextToCache = new HashMap();
    
    public static String getMimeType(
        ServletContext       context,
        String               resourceName
        )
    {
        // get the mime-type from the servlet context
        // we cache and synchronize the mime type retrieval
        // because too many calls to the container method
        // crashes the IWS 6.0sp5 web server

        resourceName = resourceName.toLowerCase();
                
        String mimeType = null;

        int index = resourceName.lastIndexOf( "." );

        if ( index >= 0 )
        {
            String extension = resourceName.substring( index );

            Map mimeTypeCache = null;
            synchronized ( cContextToCache )
            {
                mimeTypeCache = (Map)cContextToCache.get( context );
                if ( null == mimeTypeCache )
                {
                    mimeTypeCache = Collections.synchronizedMap(
                        new HashMap() );
                    cContextToCache.put( context, mimeTypeCache );
                }
            }
            
            if ( ! mimeTypeCache.containsKey( extension ) )
            {
                synchronized ( context )
                {
                    mimeType = context.getMimeType( resourceName );
                }
                
                mimeTypeCache.put( extension, mimeType );
            }
            else
            {
                mimeType = (String)mimeTypeCache.get( extension );
            }
        }
        else
        {
            mimeType = "application/octet-stream";
        }

        return mimeType;
    }
}
