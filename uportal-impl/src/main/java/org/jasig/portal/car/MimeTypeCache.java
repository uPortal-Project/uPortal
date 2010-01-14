/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.car;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * MimeTypeCache.
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
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
