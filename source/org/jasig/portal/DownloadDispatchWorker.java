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

import org.jasig.portal.services.LogService;

/**
 * Provides file download capability for the portal.
 *
 * @author <a href="mailto:svenkatesh@interactivebusiness.com">Sridhar Venkatesh</a>
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 */
public class DownloadDispatchWorker implements IWorkerRequestProcessor {
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
                // just give a default baseActionURL
                // this value should never really be used
                rd.setUPFile(new UPFileSpec(null,UPFileSpec.RENDER_METHOD,UserInstance.USER_LAYOUT_ROOT_NODE,channelTarget,null));
                ch.setRuntimeData(rd);

                if (ch instanceof org.jasig.portal.IMimeResponse) {
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
                            LogService.log(LogService.ERROR, "DownloadDispatchWorker:processWorkerDispatch unable to close IOStream "+ ioe);
                        }
                    }
                } else {
                    LogService.log(LogService.ERROR, "DownloadDispatchWorker::processWorkerDispatch(): Channel (instanceId=\""+channelTarget+"\" needs to implement org.jasig.portal.IMimeResponse interface in order to download files.");
                }
            } else {
                LogService.log(LogService.ERROR, "DownloadDispatchWorker::processWorkerDispatch(): unable to obtain instance a channel. instanceId=\""+channelTarget+"\".");
            }
        } else {
            LogService.log(LogService.ERROR, "DownloadDispatchWorker::processWorkerDispatch(): unable to determine instance Id of the target channel. requestURL=\""+pcs.getHttpServletRequest().getRequestURI()+"\".");
        }
    }
}
