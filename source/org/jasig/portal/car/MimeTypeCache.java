package org.jasig.portal.car;

import java.util.*;
import javax.servlet.ServletContext;

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
