/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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


import org.jasig.portal.channels.CError;
import org.jasig.portal.security.*;
import javax.servlet.http.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;
import org.xml.sax.*;
import org.w3c.dom.*;
import java.io.*;

// this class shall have the burden of squeezing content
// out of channels.

// future prospects:
//  - Wrap IChannel classes
//     this should be done by parsing through
//     HTML that IChannel can output
//
//  - more complex caching ?
//  - Validation and timeouts
//      these two are needed for smooth operation of the portal
//      sometimes channels will timeout with information retreival
//      then the content should be skipped
//

public class ChannelManager {
    private HttpServletRequest req;
    private HttpServletResponse res;
    private UserLayoutManager ulm;

    private PortalControlStructures pcs;

    private Hashtable channelTable;
    private Hashtable rendererTable;

    private String channelTarget;
    private Hashtable targetParams;
    private BrowserInfo binfo;

    public static String channelAddressingPathElement="channel";
    public String uPElement;

    public ChannelManager () {
        channelTable = new Hashtable ();
        rendererTable = new Hashtable ();
    }

    public ChannelManager (HttpServletRequest request, HttpServletResponse response, UserLayoutManager manager,String uPElement) {
        this ();
        this.ulm=manager;
        pcs=new PortalControlStructures();
        pcs.setUserLayoutManager(ulm);
        pcs.setChannelManager(this);
        this.setReqNRes(request,response,uPElement);
    }

    public void setUserLayoutManager(UserLayoutManager m) {
        ulm=m;
    }

    public void setReqNRes (HttpServletRequest request, HttpServletResponse response, String uPElement) {
        this.req = request;
        this.res = response;
        this.pcs.setHttpServletRequest(request);
        this.pcs.setHttpServletResponse(response);
        this.binfo=new BrowserInfo(request);
        this.uPElement=uPElement;
        rendererTable.clear ();
        processRequestChannelParameters (request);
    }



    /**
     * Look through request parameters for "channelTarget" and
     * pass corresponding actions/params to the channel
     * @param the request object
     */
    private void processRequestChannelParameters (HttpServletRequest req)
    {
        // clear the previous settings
        channelTarget = null;
        targetParams = new Hashtable ();
        String sp=req.getServletPath();
        if(sp!=null) {
            int si1=sp.indexOf(this.channelAddressingPathElement+"/");
            if(si1!=-1) {
                si1+=channelAddressingPathElement.length()+1;
                int si2=sp.indexOf("/",si1);
                if(si2!=-1) {
                    channelTarget=sp.substring(si1,si2);
                    if(channelTarget==null) {
                        Logger.log(Logger.ERROR,"ChannelManager.processRequestChannelParameters() : malformed channel address. Null channel target ID.");
                        return;
                    }
                    Logger.log(Logger.DEBUG,"ChannelManager::processRequestChannelParameters() : channelTarget=\""+channelTarget+"\".");
                    Enumeration en = req.getParameterNames ();
                    if (en != null) {
                        while (en.hasMoreElements ()) {
                            String pName= (String) en.nextElement ();
                            if (!pName.equals ("channelTarget")) {
                                Object[] val= (Object[]) req.getParameterValues(pName);
                                if (val == null) {
                                  val = ((PortalSessionManager.RequestParamWrapper)req).getObjectParameterValues(pName);
                                }
                                targetParams.put (pName, val);
                            }
                        }
                    }
                    // check if the channel is an IPrivilegedChannel, and if it is,
                    // pass portal control structures and runtime data.
                    Object chObj;
                    if ((chObj=channelTable.get(channelTarget)) == null) {
                        try {
                            chObj=instantiateChannel(channelTarget);
                        } catch (Exception e) {
                            CError errorChannel=new CError(CError.SET_STATIC_DATA_EXCEPTION,e,channelTarget,null);
                            channelTable.put(channelTarget,errorChannel);
                            chObj=errorChannel;
                            Logger.log(Logger.ERROR,"ChannelManager::processRequestChannelParameters() : unable to pass find/create an instance of a channel. Bogus ID ? ! (id=\""+channelTarget+"\").");
                        }
                    }
                    if(chObj!=null && (chObj instanceof IPrivilegedChannel)) {
                        IPrivilegedChannel isc=(IPrivilegedChannel) chObj;

                        try {
                            isc.setPortalControlStructures(pcs);
                        } catch (Exception e) {
                            channelTable.remove(isc);
                            CError errorChannel=new CError(CError.SET_PCS_EXCEPTION,e,channelTarget,isc);
                            channelTable.put(channelTarget,errorChannel);
                            isc=errorChannel;
                            // set portal control structures
                            try {
                                errorChannel.setPortalControlStructures(pcs);
                            } catch (Exception e2) {
                                // things are looking bad for our hero
                                StringWriter sw=new StringWriter();
                                e2.printStackTrace(new PrintWriter(sw));
                                sw.flush();
                                Logger.log(Logger.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                            }
                        }

                        ChannelRuntimeData rd = new ChannelRuntimeData ();
                        rd.setParameters(targetParams);
                        rd.setHttpRequest (req);
                        rd.setBrowserInfo(binfo);
                        /*			String reqURI = req.getRequestURI ();
                        reqURI = reqURI.substring (reqURI.lastIndexOf ("/") + 1, reqURI.length ());
                        rd.setBaseActionURL (reqURI + "?channelTarget=" + channelTarget + "&");*/
                        rd.setBaseActionURL(req.getContextPath()+"/channel/"+channelTarget+"/"+uPElement);
                        try {
                            isc.setRuntimeData (rd);
                        }
                        catch (Exception e) {
                            channelTable.remove(isc);
                            CError errorChannel=new CError(CError.SET_RUNTIME_DATA_EXCEPTION,e,channelTarget,isc);
                            channelTable.put(channelTarget,errorChannel);
                            isc=errorChannel;
                            // demand output
                            try {
                                ChannelRuntimeData erd = new ChannelRuntimeData ();
                                erd.setHttpRequest (req);
                                erd.setBrowserInfo(binfo);
                                erd.setBaseActionURL(req.getContextPath()+"/channel/"+channelTarget+"/"+uPElement);
                                errorChannel.setPortalControlStructures(pcs);
                                errorChannel.setRuntimeData (erd);
                            } catch (Exception e2) {
                                // things are looking bad for our hero
                                StringWriter sw=new StringWriter();
                                e2.printStackTrace(new PrintWriter(sw));
                                sw.flush();
                                Logger.log(Logger.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                            }
                        }
                    }
                }
            }
        }
    }

    public IChannel instantiateChannel(String chanID) {
        if (channelTable.get(chanID) != null) {
            // reinstantiation
            channelTable.remove(chanID);
        }
        // get channel information from the user layout manager
        Element elChannel=(Element) ulm.getNode(chanID);
        if(elChannel!=null) {
            String className=elChannel.getAttribute("class");
            long timeOut=java.lang.Long.parseLong(elChannel.getAttribute("timeout"));
            Hashtable params=new Hashtable();
            NodeList paramsList=elChannel.getElementsByTagName("parameter");
            int nnodes=paramsList.getLength();
            for(int i=0;i<nnodes;i++) {
                Element param=(Element) paramsList.item(i);
                params.put(param.getAttribute("name"),param.getAttribute("value"));
            }
            try {
                return instantiateChannel(chanID,className,timeOut,params);
            } catch (Exception ex) {
                Logger.log(Logger.ERROR,"ChannelManager::instantiateChannel() : unable to instantiate channel class \""+className+"\". "+ex);
                return null;
            }
        } else return null;

    }
    private IChannel instantiateChannel(String chanID, String className, long timeOut, Hashtable params) throws Exception {
        IChannel ch=null;
        ch = (org.jasig.portal.IChannel) Class.forName (className).newInstance ();

        // construct a ChannelStaticData object
        ChannelStaticData sd = new ChannelStaticData ();
        sd.setChannelID (chanID);
        sd.setTimeout (timeOut);
        sd.setParameters (params);
        ch.setStaticData (sd);
        channelTable.put (chanID,ch);
        // get person object from UsreLayoutManager
        sd.setPerson(ulm.getPerson());
        // security context is saved in the session as well
        // Eventually it should be retrieved from authenticationService (?)
        sd.setSecurityContext((ISecurityContext) req.getSession(false).getAttribute("up_SecurityContext"));
        return ch;
    }

    /**
     * Start rendering the channel in a separate thread.
     * This function retreives a particular channel from cache, passes parameters to the
     * channel and then creates a new ChannelRenderer object to render the channel in a
     * separate thread.
     * @param chanID channel ID (unique)
     * @param className name of the channel class
     * @param params a table of parameters
     */
    public void startChannelRendering (String chanID, String className, long timeOut, Hashtable params)
    {
        // see if the channel is cached
        IChannel ch;

        if ((ch = (IChannel) channelTable.get (chanID)) == null) {
            try {
                ch=instantiateChannel(chanID,className,timeOut,params);
            } catch (Exception e) {
                CError errorChannel=new CError(CError.SET_STATIC_DATA_EXCEPTION,e,chanID,null);
                channelTable.put(chanID,errorChannel);
                ch=errorChannel;
            }
        }

        ChannelRuntimeData rd=null;

        if(!chanID.equals(channelTarget)) {
            if((ch instanceof IPrivilegedChannel)) {
                // send the control structures
                try {
                    ((IPrivilegedChannel) ch).setPortalControlStructures(pcs);
                } catch (Exception e) {
                    channelTable.remove(ch);
                    CError errorChannel=new CError(CError.SET_PCS_EXCEPTION,e,chanID,ch);
                    channelTable.put(chanID,errorChannel);
                    ch=errorChannel;
                    // set portal control structures
                    try {
                        errorChannel.setPortalControlStructures(pcs);
                    } catch (Exception e2) {
                        // things are looking bad for our hero
                        StringWriter sw=new StringWriter();
                        e2.printStackTrace(new PrintWriter(sw));
                        sw.flush();
                        Logger.log(Logger.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                    }
                }
            }
            rd = new ChannelRuntimeData ();
            rd.setHttpRequest (req);
            rd.setBrowserInfo(binfo);
            rd.setBaseActionURL(req.getContextPath()+"/channel/"+chanID+"/"+uPElement);
        } else {
            if(!(ch instanceof IPrivilegedChannel)) {
                rd = new ChannelRuntimeData ();
                rd.setParameters(targetParams);
                rd.setBrowserInfo(binfo);
                rd.setHttpRequest (req);
                rd.setBaseActionURL(req.getContextPath()+"/channel/"+chanID+"/"+uPElement);
            }
        }

        ChannelRenderer cr = new ChannelRenderer (ch,rd);
        cr.setTimeout (timeOut);
        cr.startRendering ();
        rendererTable.put (chanID,cr);
    }



    /**
     * Output channel content.
     * Note that startChannelRendering had to be invoked on this channel prior to calling this function.
     * @param chanID unique channel ID
     * @param dh document handler that will receive channel content
     */
    public void outputChannel (String chanID, DocumentHandler dh) {
        ChannelRenderer cr;

        if ((cr = (ChannelRenderer) rendererTable.get (chanID)) != null) {
            rendererTable.remove(chanID);
            ChannelSAXStreamFilter custodian = new ChannelSAXStreamFilter (dh);
            try {
                int out = cr.outputRendering (custodian);
                if(out==cr.RENDERING_TIMED_OUT) {
                    // rendering has timed out
                    IChannel badChannel=(IChannel) channelTable.get(chanID);
                    channelTable.remove(badChannel);
                    CError errorChannel=new CError(CError.TIMEOUT_EXCEPTION,(Exception) null,chanID,badChannel);
                    channelTable.put(chanID,errorChannel);
                    // demand output
                    try {
                        ChannelRuntimeData rd = new ChannelRuntimeData ();
                        rd.setBrowserInfo(binfo);
                        rd.setHttpRequest (req);
                        rd.setBaseActionURL(req.getContextPath()+"/channel/"+chanID+"/"+uPElement);
                        errorChannel.setRuntimeData (rd);

                        errorChannel.setPortalControlStructures(pcs);
                        errorChannel.renderXML(dh);
                    } catch (Exception e) {
                        // things are looking bad for our hero
                        StringWriter sw=new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        sw.flush();
                        Logger.log(Logger.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                    }
                }

            } catch (InternalPortalException ipe) {
                // this implies that the channel has thrown an exception during
                // renderXML() call. No events had been placed onto the DocumentHandler,
                // so that an Error channel can be rendered in place.
                Exception channelException=ipe.getException();
                if(channelException!=null) {
                    // see if the renderXML() has thrown a PortalException
                        // hand it over to the Error channel
                    IChannel badChannel=(IChannel) channelTable.get(chanID);
                    channelTable.remove(badChannel);
                    CError errorChannel=new CError(CError.RENDER_TIME_EXCEPTION,channelException,chanID,badChannel);
                    channelTable.put(chanID,errorChannel);
                    // demand output
                    try {
                        ChannelRuntimeData rd = new ChannelRuntimeData ();
                        rd.setBrowserInfo(binfo);
                        rd.setHttpRequest (req);
                        rd.setBaseActionURL(req.getContextPath()+"/channel/"+chanID+"/"+uPElement);
                        errorChannel.setRuntimeData (rd);

                        errorChannel.setPortalControlStructures(pcs);
                        errorChannel.renderXML(dh);
                    } catch (Exception e) {
                        // things are looking bad for our hero
                        StringWriter sw=new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        sw.flush();
                        Logger.log(Logger.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                    }

                } else {
                    Logger.log(Logger.ERROR,"ChannelManager::outputChannels() : received InternalPortalException that doesn't carry a channel exception inside !?");
                }
            }
            catch (Exception e) {
                // This implies that the channel has been successful in completing renderXML()
                // method, but somewhere down the line things went wrong. Most likely,
                // a buffer output routine threw. This means that we are likely to have partial
                // output in the document handler. Really bad !
                Logger.log(Logger.ERROR,"ChannelManager::outputChannel() : post-renderXML() processing threw!"+e);
            }
        }
        else {
            Logger.log (Logger.ERROR,"ChannelManager::outputChannel() : ChannelRenderer for chanID=\""+chanID+"\" is absent from cache !!!");
        }
    }


    /**
     * passes Layout-level event to a channel
     * @param channel ID
     * @param PortalEvent object
     */
    public void passPortalEvent (String chanID, PortalEvent le) {
        IChannel ch= (IChannel) channelTable.get (chanID);

        if (ch != null) {
            ch.receiveEvent (le);
        }
        else
            Logger.log (Logger.ERROR, "ChannelManager::passPortalEvent() : trying to pass an event to a channel that is not in cache. (cahnel=\"" + chanID + "\")");
    }

    /**
     * Directly places a channel instance into the hashtable of active channels.
     * This is designed to be used by the error channel only.
     */

    public void addChannelInstance(String channelID,IChannel channelInstance) {
        if(channelTable.get(channelID)!=null)
            channelTable.remove(channelID);
        channelTable.put(channelID,channelInstance);
    }

}
