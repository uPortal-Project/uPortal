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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.IWorkerRequestProcessor;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.RequestParamWrapper;
import org.jasig.portal.services.LogService;

/**
 * Class to handle incoming portal requests with specified worker of
 * "carrsrc". These request are for loading web elements out of an installed
 * channel archive file. The form of the URL is the traditional UPFileSpec
 * with a worker of "carrsrc" and a query parameter "carrsrc=<resourcePath>".
 * The resourcePath is the path to the resource from within the channel
 * archive.
 *
 * For example: if a channel existed with the directory structure of
 * "org/jasig/uportal/channels/email/" and within the email channel
 * base directory there was an "images" directory containing "mailbox.gif"
 * then the browser could access that image from the installed email channel
 * archive via "<uPFileSpecWithWorker.carrsrc>.uP?carrsrc=org/jasig/
 * uportal/channels/email/images/mailbox.gif".
 *
 * See the ChannelRuntimeData.getBaseMediaURL methods from which channels
 * can obtain the base URL dynamically without having to know if they are
 * deployed as a traditional channel or as a channel archive.
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
public class CarResourceWorker implements IWorkerRequestProcessor {

    private static CarResources resources = CarResources.getInstance();
    public final static String RCS_ID = "@(#) $Header$";

    /**
     * Create a CarResourceWorker.
     */
    public CarResourceWorker()
    {
    }
    
    /** 
     * Provides web access to channel resources stored in the channel archive files.
     * Housing the channel if the channel was installed in the uPortal using a CAR. 
     */
    public void processWorkerDispatch(PortalControlStructures pcs)
        throws PortalException
    {
        HttpServletRequest req=pcs.getHttpServletRequest();
        HttpServletResponse res=pcs.getHttpServletResponse();

        // get the named resource
        String resourceName = getResourceName( req );
        InputStream in = resources.getResourceAsStream( resourceName );

        if ( in == null )
        {
            res.setStatus( HttpServletResponse.SC_NOT_FOUND );
            return;
        }
        setContentType( res, resourceName );
        long resourceSize = resources.getResourceSize( resourceName );

        if ( resourceSize != -1 )
            res.setHeader( "Content-Length", "" + resourceSize );
        
        OutputStream out = null;
        try
        {
            out = res.getOutputStream();
            byte[] bytes = new byte[4096];
            int bytesRead = 0;
            bytesRead = in.read( bytes );
        
            while( bytesRead != -1 )
            {
                out.write( bytes, 0, bytesRead );
                bytesRead = in.read( bytes );
            }

            out.flush();
        }
        catch( IOException ioe )
        {
            throw new PortalException( "Error writing resource" );
        } finally {
			try {
				in.close();
				if (out != null)
					out.close();
			} catch (IOException ioe) {
				LogService.log(LogService.ERROR,
						"CarResourceWorker::processWorkerDispatch() could not close IO Stream"
								+ ioe);
			}
		}
    }

    /**
       Set the content type for the resource being served back. The
       ServletContext is used to obtain the proper mime-types.
       New/unknown types are defined in the deployment descriptor of
       the web application.  In the future, channels could provide
       their own override file of types that they wish to support
       beyond the defaults.
     */
    private void setContentType( HttpServletResponse res,
                                 String resourceName )
        throws PortalException
    {
        resourceName = resourceName.toLowerCase();
        
        ServletContext sc = PortalSessionManager.getInstance().
            getServletConfig().getServletContext();
        
        String mimeType = MimeTypeCache.getMimeType( sc, resourceName );

        if ( null != mimeType )
            res.setContentType(mimeType);
        else 
            throw new PortalException( "Unsupported resource type" +
                                       " '" + resourceName + "'" );
    }


    /**
       Set the content type for the resource being served back.
     */
    private String getResourceName( HttpServletRequest req )
        throws PortalException
    {
        // check if the resource name has been passed via the
        // query string parm 'car_rsrc'
        String resourceName = req.getParameter( CarResources.CAR_RESOURCE_PARM );

        if ( resourceName == null )
        {
            Enumeration e = req.getParameterNames();
            if ( e == null )
                throw new PortalException( "getParameterNames() is null." );
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter( sw );

            if ( ! e.hasMoreElements() )
                pw.print( "getParameterNames() is empty." +
                          " Req class = " + req.getClass().getName() );
                         
            while ( e.hasMoreElements() )
            {
                String parm = (String) e.nextElement();
                Object[] vals = req.getParameterValues( parm );
                if ( vals == null )
                {
                    pw.print( " " + parm + "(-)" );
                    if ( req instanceof RequestParamWrapper )
                        vals = ((RequestParamWrapper)req).getObjectParameterValues(parm);
                    if ( vals == null )
                        pw.print( " " + parm + "(both)=[]" );
                    else
                        pw.print( " " + parm + "(2nd)=[" );
                }
                else
                {
                    pw.print( " " + parm + "=[" );
                }
                if ( vals != null )
                {
                    for( int i=0; i<vals.length; i++ )
                    {
                        if ( i>0 )
                            pw.print( ", " );
                        pw.print( vals[i] );
                    }
                    pw.print( "]" );
                }
            }
            pw.flush();
            pw.close();
            throw new PortalException( "Resource name not specified. " +
                                       sw.toString() );
        }
        try
        {
            resourceName = URLDecoder.decode( resourceName );
        }
        catch( Exception ex )
        {
            throw new PortalException( "Unable to URLDecode the resource" +
                                       " name '" + resourceName + "'" );
        }
        return resourceName;
    }
}
