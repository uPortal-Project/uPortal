/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides file download capability for the portal.
 *
 * @author <a href="mailto:svenkatesh@interactivebusiness.com">Sridhar Venkatesh</a>
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 */
public class DownloadDispatchWorker implements IWorkerRequestProcessor {
    
    private static final Log log = LogFactory.getLog(DownloadDispatchWorker.class);
    
    public void processWorkerDispatch(PortalControlStructures pcs) throws PortalException {
        HttpServletRequest req=pcs.getHttpServletRequest();
        HttpServletResponse res=pcs.getHttpServletResponse();

        // determine the channel, follow the same logic as the standard uPortal processing.
        // (although, in general, worker processors can make their own rules
        String channelTarget=null;
        Hashtable targetParams = new Hashtable();

        // check if the uP_channelTarget parameter has been passed
        channelTarget=req.getParameter("uP_channelTarget");
        if(channelTarget==null) {
            // determine target channel id
            UPFileSpec upfs=new UPFileSpec(req);
            channelTarget=upfs.getTargetNodeId();
        }

        // gather parameters
        if(channelTarget!=null) {
            Enumeration en = req.getParameterNames();
            if (en != null) {
                while (en.hasMoreElements()) {
                    String pName= (String) en.nextElement();
                    if (!pName.equals ("uP_channelTarget")) {
                        Object[] val= (Object[]) req.getParameterValues(pName);
                        if (val == null) {
                            val = ((RequestParamWrapper)req).getObjectParameterValues(pName);
                        }
                        targetParams.put(pName, val);
                    }
                }
            }

            IChannel ch = pcs.getChannelManager().getChannelInstance(channelTarget);

            if(ch!=null) {
                // set pcs
                if(ch instanceof IPrivilegedChannel) {
                    ((IPrivilegedChannel)ch).setPortalControlStructures(pcs);
                }
                // set runtime data
                ChannelRuntimeData rd = new ChannelRuntimeData();
                rd.setParameters(targetParams);
                rd.setBrowserInfo(new BrowserInfo(req));
                rd.setHttpRequestMethod(req.getMethod());
				rd.setRemoteAddress(req.getRemoteAddr());
                rd.setUPFile(new UPFileSpec(null,UPFileSpec.RENDER_METHOD,UserInstance.USER_LAYOUT_ROOT_NODE,channelTarget,null));
                
                if (ch instanceof org.jasig.portal.IMimeResponse) {
                  ch.setRuntimeData(rd);

                  org.jasig.portal.IMimeResponse ds = (org.jasig.portal.IMimeResponse)ch;
                  ServletOutputStream out = null;
                  InputStream ios = null;
                    try {

                        // Set the headers if available
                        Map httpHeaders = ds.getHeaders();
                        if (httpHeaders != null) {
                            Set headerKeys = httpHeaders.keySet();
                            Iterator it = headerKeys.iterator();
                            while (it.hasNext()) {
                                String param = (String)it.next();
                                String value = (String)httpHeaders.get(param);
                                res.setHeader(param, value);
                            }
                            httpHeaders.clear();
                        }

                        // Set the MIME content type
                        res.setContentType (ds.getContentType());

                        // Set the data
                        out = res.getOutputStream();
                        ios = ds.getInputStream();
                        if (ios != null) {
                            int size = 0;
                            byte[] contentBytes = new byte[8192];
                            while ((size = ios.read(contentBytes)) != -1) {
                                out.write(contentBytes,0, size);
                            }
                        } else {
                            /**
                             * The channel has more complicated processing it needs to do on the
                             * output stream
                             */
                            ds.downloadData(out);
                        }
                        out.flush();
                    } catch (Exception e) {
                        ds.reportDownloadError(e);
                    } finally {
                        try {
                            if (ios != null) 
                                ios.close();
                            if (out != null) 
                                out.close();
                        } catch (IOException ioe) {
                            log.error( "DownloadDispatchWorker:processWorkerDispatch unable to close IOStream ", ioe);
                        }
                    }
                } else if (ch instanceof org.jasig.portal.IDirectResponse) {
                    //We are allowing the rendering of URLs in the IDirectResponse interface
                    //so the tag needs to be set for the uPfile
                    rd.getUPFile().setTagId(PortalSessionManager.INTERNAL_TAG_VALUE);
                    rd.setTargeted(true);
                    ch.setRuntimeData(rd);
                    
                    org.jasig.portal.IDirectResponse dirResp = (org.jasig.portal.IDirectResponse)ch;
                    
                    dirResp.setResponse(res);                    
                } else {
                    log.error( "DownloadDispatchWorker::processWorkerDispatch(): Channel (instanceId=\""+channelTarget+"\" needs to implement org.jasig.portal.IMimeResponse interface in order to download files.");
                }
            } else {
                log.warn("DownloadDispatchWorker::processWorkerDispatch(): unable to obtain instance a channel. instanceId=\""+channelTarget+"\".");
            }
        } else {
            log.error( "DownloadDispatchWorker::processWorkerDispatch(): unable to determine instance Id of the target channel. requestURL=\""+pcs.getHttpServletRequest().getRequestURI()+"\".");
        }
    }
}
