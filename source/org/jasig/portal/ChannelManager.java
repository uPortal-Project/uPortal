package org.jasig.portal;

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

import org.jasig.portal.channels.CError;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.services.LogService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import org.jasig.portal.utils.SoftHashMap;
// import org.jasig.portal.security.provider.ReferencePermissionManager;
import java.util.Collections;
import org.xml.sax.ContentHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.StringWriter;
import java.io.PrintWriter;

import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;

import tyrex.naming.MemoryContext;

/**
 * This class shall have the burden of squeezing content
 * out of channels.
 *
 * - Validation and timeouts
 *    these two are needed for smooth operation of the portal
 *    sometimes channels will timeout with information retreival
 *    then the content should be skipped
 *
 * @author Peter Kharchenko, pkharchenko@interactivebusiness.com
 * @version $Revision$
 */
public class ChannelManager {
    private IUserPreferencesManager ulm;
    private PortalControlStructures pcs;

    private Hashtable channelTable;
    private Hashtable rendererTable;
    private Map channelCacheTable;

    private String channelTarget;
    private Hashtable targetParams;
    private BrowserInfo binfo;

    private Context portalContext;
    private Context channelContext;

    private IAuthorizationPrincipal ap;

    public UPFileSpec uPElement;

    // global channel rendering cache
    public static final int SYSTEM_CHANNEL_CACHE_MIN_SIZE=50; // this should be in a file somewhere
    public static final SoftHashMap systemCache=new SoftHashMap(SYSTEM_CHANNEL_CACHE_MIN_SIZE);

    // table of multithreaded channels
    public static final Hashtable staticChannels=new Hashtable();
    public static final String channelAddressingPathElement="channel";


    public ChannelManager() {
        channelTable=new Hashtable();
        rendererTable=new Hashtable();
        channelCacheTable=Collections.synchronizedMap(new WeakHashMap());
    }
    
    /**
     * Creates a new <code>ChannelManager</code> instance.
     *
     * @param request a <code>HttpServletRequest</code> value
     * @param response a <code>HttpServletResponse</code> value
     * @param manager an <code>IUserPreferencesManager</code> value
     * @param uPElement an <code>UPFileSpec</code> that includes a tag number.
     */
    public ChannelManager(HttpServletRequest request, HttpServletResponse response, IUserPreferencesManager manager,UPFileSpec uPElement) {
        this();
        this.ulm=manager;
        pcs=new PortalControlStructures();
        pcs.setUserPreferencesManager(ulm);
        pcs.setChannelManager(this);
        this.setReqNRes(request,response,uPElement);
    }


    public ChannelManager(IUserPreferencesManager manager) {
        this();
        this.ulm=manager;
        pcs=new PortalControlStructures();
        pcs.setUserPreferencesManager(manager);
        pcs.setChannelManager(this);
    }


    /**
     * Directly places a channel instance into the hashtable of active channels.
     * This is designed to be used by the error channel only.
     */

    public void addChannelInstance(String channelSubscribeId,IChannel channelInstance) {
        if(channelTable.get(channelSubscribeId)!=null)
            channelTable.remove(channelSubscribeId);
        channelTable.put(channelSubscribeId,channelInstance);
    }


    /**
     * Clean up after a rendering round.
     */
    public void finishedRendering() {
        // clean up
        rendererTable.clear();
        targetParams=null;
    }


    /**
     * Handle end-of-session cleanup
     *
     */
    public void finishedSession() {
        this.finishedRendering();
        channelCacheTable.clear();
        // send SESSION_DONE event to all the channels
        PortalEvent ev=new PortalEvent(PortalEvent.SESSION_DONE);
        for(Enumeration e=channelTable.elements();e.hasMoreElements();) {
            ((IChannel)e.nextElement()).receiveEvent(ev);
        }
    }


    /**
     * Return channel rendering in a character form. If the character form is not available, SAXBufferImpl is returned, and
     * later replaced with character representation.
     *
     * @param channelSubscribeId channel subscribe Id
     * @param className channel class name
     * @param timeOut channel timeout
     * @param params channel publish/subscribe params
     * @return a character buffer (<code>String</code>) or a SAX buffer (<code>SAXBufferImpl</code>)
     */
    public Object getChannelCharacters(String channelSubscribeId, String channelPublishId, String className, long timeOut, Hashtable params) {
        ChannelRenderer cr;

        if(rendererTable.get(channelSubscribeId) == null) {
            if(className!=null && params!=null) {
                this.startChannelRendering(channelSubscribeId, channelPublishId, className, timeOut, params,true);
            } else {
                LogService.instance().log(LogService.ERROR,"ChannelManager:g:etChannelCharacters() : channel has not been instantiated ! Please use full version of getChannelCharacters() !");
            }
        }
        if((cr = (ChannelRenderer) rendererTable.get(channelSubscribeId)) != null) {

            SAX2BufferImpl dh=new SAX2BufferImpl();
            // in case there isn't a character cache
            ChannelSAXStreamFilter custodian = new ChannelSAXStreamFilter((ContentHandler)dh);
            try {
                int out = cr.completeRendering();
                if(out==cr.RENDERING_SUCCESSFUL) {
                    // try to get character output
                    String cbuffer=cr.getCharacters();
                    if(cbuffer!=null)  {
                        return cbuffer;
                    }
                    dh=cr.getBuffer();
                }

                if(out==cr.RENDERING_TIMED_OUT) {
                    // rendering has timed out
                    IChannel badChannel=(IChannel) channelTable.get(channelSubscribeId);
                    channelTable.remove(badChannel);
                    CError errorChannel=new CError(CError.TIMEOUT_EXCEPTION,(Exception) null,channelSubscribeId,badChannel);
                    // replace the channel object in ChannelRenderer (for cache recording purposes)
                    cr.setChannel(errorChannel);
                    // replace the channel object in the channel table
                    channelTable.put(channelSubscribeId,errorChannel);
                    // demand output
                    try {
                        ChannelRuntimeData rd = new ChannelRuntimeData();
                        rd.setBrowserInfo(binfo);
                        rd.setChannelSubscribeId(channelSubscribeId);

                        UPFileSpec up=new UPFileSpec(uPElement);
                        up.setTargetNodeId(channelSubscribeId);
                        rd.setUPFile(up);

                        errorChannel.setRuntimeData(rd);

                        errorChannel.setPortalControlStructures(pcs);
                        errorChannel.renderXML(dh);
                    } catch (Exception e) {
                        // things are looking bad for our hero
                        StringWriter sw=new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        sw.flush();
                        LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                    }
                }

            } catch (InternalPortalException ipe) {
                // this implies that the channel has thrown an exception during
                // renderXML() call. No events had been placed onto the ContentHandler,
                // so that an Error channel can be rendered in place.
                Throwable channelException=ipe.getException();
                if(channelException!=null) {
                    // see if the renderXML() has thrown a PortalException
                    // hand it over to the Error channel
                    IChannel badChannel=(IChannel) channelTable.get(channelSubscribeId);
                    channelTable.remove(badChannel);
                    CError errorChannel=new CError(CError.RENDER_TIME_EXCEPTION,channelException,channelSubscribeId,badChannel);
                    // replace the channel object in ChannelRenderer (for cache recording purposes)
                    cr.setChannel(errorChannel);
                    // replace the channel object in the channel table
                    channelTable.put(channelSubscribeId,errorChannel);
                    // demand output
                    try {
                        ChannelRuntimeData rd = new ChannelRuntimeData();
                        rd.setBrowserInfo(binfo);
                        rd.setChannelSubscribeId(channelSubscribeId);

                        UPFileSpec up=new UPFileSpec(uPElement);
                        up.setTargetNodeId(channelSubscribeId);
                        rd.setUPFile(up);

                        errorChannel.setRuntimeData(rd);

                        errorChannel.setPortalControlStructures(pcs);
                        errorChannel.renderXML(dh);
                    } catch (Exception e) {
                        // things are looking bad for our hero
                        StringWriter sw=new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        sw.flush();
                        LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                    }

                } else {
                    LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannels() : received InternalPortalException that doesn't carry a channel exception inside !?");
                }
            } catch (Exception e) {
                // This implies that the channel has been successful in completing renderXML()
                // method, but somewhere down the line things went wrong. Most likely,
                // a buffer output routine threw. This means that we are likely to have partial
                // output in the document handler. Really bad !
                LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannel() : post-renderXML() processing threw!"+e, e);
            } catch (Throwable t) {
              LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannel() : post-renderXML() processing threw!"+t, t);

            }
            return dh;
        } else {
            LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannel() : ChannelRenderer for channelSubscribeId=\""+channelSubscribeId+"\" is absent from cache !!!");
        }

        return null;
    }


    /**
     * <code>getChannelContext</code> generates a JNDI context that
     * will be passed to the regular channels. The context is pieced
     * together from the parts of the global portal context.
     *
     * @param portalContext uPortal JNDI context
     * @param sessionId current session id
     * @param userId id of a current user
     * @param layotId id of the layout used by the user
     * @return a channel <code>InitialContext</code> value
     */
    private static Context getChannelContext(Context portalContext,String sessionId,String userId,String layoutId) throws NamingException {
        // create a new InitialContext
        Context cic=new MemoryContext(new Hashtable());
        // get services context
        Context servicesContext=(Context)portalContext.lookup("services");
        // get channel-ids context
        Context channel_idsContext=(Context)portalContext.lookup("users/"+userId+"/layouts/"+layoutId+"/channel-ids");
        // get channel-obj context
        Context channel_objContext=(Context)portalContext.lookup("users/"+userId+"/sessions/"+sessionId+"/channel-obj");

        cic.bind("services",servicesContext);
        cic.bind("channel-ids",channel_idsContext);
        cic.bind("channel-obj",channel_objContext);

        return cic;
    }

    /**
     * Get the uPortal JNDI context
     * @return uPortal initial JNDI context
     * @exception NamingException
     */
    private static Context getPortalContext() throws NamingException {
        Hashtable environment = new Hashtable(5);
        // Set up the path
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jasig.portal.jndi.PortalInitialContextFactory");
        Context ctx = new InitialContext(environment);
        return(ctx);
    }


    /**
     * Instantiates a channel given just the channel subscribe Id.
     * 
     * @param channelSubscribeId a channel instance Id in the userLayout
     * @return an <code>IChannel</code> object
     */
    public IChannel instantiateChannel(String channelSubscribeId) {
        if (channelTable.get(channelSubscribeId) != null) {
            // reinstantiation
            channelTable.remove(channelSubscribeId);
        }
        // get channel information from the user layout manager
        Element elChannel=(Element) ulm.getUserLayoutNode(channelSubscribeId);
        if(elChannel!=null) {
            String className=elChannel.getAttribute("class");
            String channelPublishId=elChannel.getAttribute("chanID");
            long timeOut=java.lang.Long.parseLong(elChannel.getAttribute("timeout"));
            Hashtable params=new Hashtable();
            NodeList paramsList=elChannel.getElementsByTagName("parameter");
            int nnodes=paramsList.getLength();
            for(int i=0;i<nnodes;i++) {
                Element param=(Element) paramsList.item(i);
                params.put(param.getAttribute("name"),param.getAttribute("value"));
            }
            try {
                return instantiateChannel(channelSubscribeId,channelPublishId, className,timeOut,params);
            } catch (Exception ex) {
                LogService.instance().log(LogService.ERROR,"ChannelManager::instantiateChannel() : unable to instantiate channel class \""+className+"\". "+ex);
                return null;
            }
        } else return null;

    }


    private IChannel instantiateChannel(String channelSubscribeId, String channelPublishId, String className, long timeOut, Hashtable params) throws Exception {
        IChannel ch=null;

        // check if the user has permissions to instantiate this channel
        if(ap==null) {
            ap = AuthorizationService.instance().newPrincipal(Integer.toString(this.pcs.getUserPreferencesManager().getPerson().getID()), org.jasig.portal.security.IPerson.class);
        }

        if(ap.canRender(Integer.parseInt(channelPublishId))) {

            boolean exists=false;
            // this is somewhat of a cheating ... I am trying to avoid instantiating a multithreaded
            // channel more then once, but it's difficult to implement "instanceof" operation on
            // the java.lang.Class. So, I just look into the staticChannels table.
            Object cobj=staticChannels.get(className);
            if(cobj!=null) {
                exists=true;
            } else {
                cobj =  Class.forName(className).newInstance();
            }

            // determine what kind of a channel it is.
            // (perhaps, later this all could be moved to JNDI factories, so everything would be transparent)
            if(cobj instanceof IMultithreadedChannel) {
                String uid=this.pcs.getHttpServletRequest().getSession(false).getId()+"/"+channelSubscribeId;
                if(cobj instanceof IMultithreadedCacheable) {
                    if(cobj instanceof IPrivileged) {
                        // both cacheable and privileged
                        ch=new MultithreadedPrivilegedCacheableChannelAdapter((IMultithreadedChannel)cobj,uid);
                    } else {
                        // just cacheable
                        ch=new MultithreadedCacheableChannelAdapter((IMultithreadedChannel)cobj,uid);
                    }
                } else if(cobj instanceof IPrivileged) {
                    ch=new MultithreadedPrivilegedChannelAdapter((IMultithreadedChannel)cobj,uid);
                } else {
                    // plain multithreaded
                    ch=new MultithreadedChannelAdapter((IMultithreadedChannel)cobj,uid);
                }
                // see if we need to add the instance to the staticChannels
                if(!exists) {
                    staticChannels.put(className,cobj);
                }
            } else {
                // vanilla IChannel
                ch=(IChannel)cobj;
            }

            // construct a ChannelStaticData object
            ChannelStaticData sd = new ChannelStaticData();
            sd.setChannelSubscribeId(channelSubscribeId);
            sd.setTimeout(timeOut);
            sd.setParameters(params);
            // Set the Id of the channel that exists in UP_CHANNELS
            sd.setChannelPublishId(ulm.getChannelPublishId(channelSubscribeId));
            // Set the PermissionManager for this channel (no longer necessary)
            // sd.setPermissionManager(new ReferencePermissionManager("CHAN_ID." + ulm.getChannelSubscribeId(channelSubscribeId)));

            // get person object from UsreLayoutManager
            sd.setPerson(ulm.getPerson());

            sd.setJNDIContext(channelContext);

            ch.setStaticData(sd);
        } else {
            // user is not authorized to instantiate this channel
            // create an instance of an error channel instead
            ch=new CError(CError.CHANNEL_AUTHORIZATION_EXCEPTION,"You don't have authorization to render this channel.",channelSubscribeId,null);
        }

        channelTable.put(channelSubscribeId,ch);

        return ch;
    }


    /**
     * Output channel content.
     * Note that startChannelRendering had to be invoked on this channel prior to calling this function.
     * @param channelSubscribeId unique channel Id
     * @param dh document handler that will receive channel content
     */
    public void outputChannel(String channelSubscribeId, String channelPublishId, ContentHandler dh, String className, long timeOut, Hashtable params) {
        ChannelRenderer cr;

        if (rendererTable.get(channelSubscribeId) == null) {
            this.startChannelRendering(channelSubscribeId, channelPublishId, className, timeOut, params,false);
        }
        if ((cr = (ChannelRenderer) rendererTable.get(channelSubscribeId)) != null) {
            rendererTable.remove(channelSubscribeId);
            ChannelSAXStreamFilter custodian = new ChannelSAXStreamFilter(dh);
            try {
                int out = cr.outputRendering(custodian);
                if(out==cr.RENDERING_TIMED_OUT) {
                    // rendering has timed out
                    IChannel badChannel=(IChannel) channelTable.get(channelSubscribeId);
                    channelTable.remove(badChannel);
                    CError errorChannel=new CError(CError.TIMEOUT_EXCEPTION,(Exception) null,channelSubscribeId,badChannel);
                    // replace the channel object in ChannelRenderer (for cache recording purposes)
                    cr.setChannel(errorChannel);
                    // replace the channel object in the channel table
                    channelTable.put(channelSubscribeId,errorChannel);
                    // demand output
                    try {
                        ChannelRuntimeData rd = new ChannelRuntimeData();
                        rd.setBrowserInfo(binfo);
                        rd.setChannelSubscribeId(channelSubscribeId);

                        UPFileSpec up=new UPFileSpec(uPElement);
                        up.setTargetNodeId(channelSubscribeId);
                        rd.setUPFile(up);

                        errorChannel.setRuntimeData(rd);

                        errorChannel.setPortalControlStructures(pcs);
                        errorChannel.renderXML(dh);
                    } catch (Exception e) {
                        // things are looking bad for our hero
                        StringWriter sw=new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        sw.flush();
                        LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                    }
                }

            } catch (InternalPortalException ipe) {
                // this implies that the channel has thrown an exception during
                // renderXML() call. No events had been placed onto the ContentHandler,
                // so that an Error channel can be rendered in place.
                Throwable channelException=ipe.getException();
                if(channelException!=null) {
                    // see if the renderXML() has thrown a PortalException
                    // hand it over to the Error channel
                    IChannel badChannel=(IChannel) channelTable.get(channelSubscribeId);
                    channelTable.remove(badChannel);
                    CError errorChannel=new CError(CError.RENDER_TIME_EXCEPTION,channelException,channelSubscribeId,badChannel);
                    // replace the channel object in ChannelRenderer (for cache recording purposes)
                    cr.setChannel(errorChannel);
                    // replace the channel object in the channel table
                    channelTable.put(channelSubscribeId,errorChannel);
                    // demand output
                    try {
                        ChannelRuntimeData rd = new ChannelRuntimeData();
                        rd.setBrowserInfo(binfo);
                        rd.setChannelSubscribeId(channelSubscribeId);

                        UPFileSpec up=new UPFileSpec(uPElement);
                        up.setTargetNodeId(channelSubscribeId);
                        rd.setUPFile(up);

                        errorChannel.setRuntimeData(rd);

                        errorChannel.setPortalControlStructures(pcs);
                        errorChannel.renderXML(dh);
                    } catch (Exception e) {
                        // things are looking bad for our hero
                        StringWriter sw=new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        sw.flush();
                        LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                    }

                } else {
                    LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannels() : received InternalPortalException that doesn't carry a channel exception inside !?");
                }
            } catch (Exception e) {
                // This implies that the channel has been successful in completing renderXML()
                // method, but somewhere down the line things went wrong. Most likely,
                // a buffer output routine threw. This means that we are likely to have partial
                // output in the document handler. Really bad !
                LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannel() : post-renderXML() processing threw!"+e);
            } catch (Throwable t) {
              // Some strange runtime error
                LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannel() : post-renderXML() processing threw!"+t);
            }
        } else {
            LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannel() : ChannelRenderer for channelSubscribeId=\""+channelSubscribeId+"\" is absent from cache !!!");
        }
    }


    /**
     * passes Layout-level event to a channel
     * @param channelSubscribeId channel subscribe id
     * @param PortalEvent object
     */
    public void passPortalEvent(String channelSubscribeId, PortalEvent le) {
        IChannel ch= (IChannel) channelTable.get(channelSubscribeId);

        if (ch != null) {
            ch.receiveEvent(le);
        } else {
            LogService.instance().log(LogService.ERROR, "ChannelManager::passPortalEvent() : trying to pass an event to a channel that is not in cache. (cahnel=\"" + channelSubscribeId + "\")");
        }
    }


    /**
     * Determine target channel and pass corresponding
     * actions/params to that channel
     * @param req the <code>HttpServletRequest</code>
     */
    private void processRequestChannelParameters(HttpServletRequest req)
    {
        // clear the previous settings
        channelTarget = null;
        targetParams = new Hashtable();
        
        // check if the uP_channelTarget parameter has been passed
        channelTarget=req.getParameter("uP_channelTarget");
        if(channelTarget==null) {
            // determine target channel id
            UPFileSpec upfs=new UPFileSpec(req);
            channelTarget=upfs.getTargetNodeId();
            LogService.instance().log(LogService.DEBUG,"ChannelManager::processRequestChannelParameters() : channelTarget=\""+channelTarget+"\".");
        }

        if(channelTarget!=null) {
            Enumeration en = req.getParameterNames();
            if (en != null) {
                while (en.hasMoreElements()) {
                    String pName= (String) en.nextElement();
                    if (!pName.equals ("uP_channelTarget")) {
                        Object[] val= (Object[]) req.getParameterValues(pName);
                        if (val == null) {
                            val = ((PortalSessionManager.RequestParamWrapper)req).getObjectParameterValues(pName);
                        }
                        targetParams.put(pName, val);
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
                    LogService.instance().log(LogService.ERROR,"ChannelManager::processRequestChannelParameters() : unable to pass find/create an instance of a channel. Bogus Id ? ! (id=\""+channelTarget+"\").");
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
                        LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                    }
                }

                ChannelRuntimeData rd = new ChannelRuntimeData();
                rd.setParameters(targetParams);
                rd.setBrowserInfo(binfo);
                rd.setChannelSubscribeId(channelTarget);

                UPFileSpec up=new UPFileSpec(uPElement);
                up.setTargetNodeId(channelTarget);
                rd.setUPFile(up);

                try {
                    isc.setRuntimeData(rd);
                }
                catch (Exception e) {
                    channelTable.remove(isc);
                    CError errorChannel=new CError(CError.SET_RUNTIME_DATA_EXCEPTION,e,channelTarget,isc);
                    channelTable.put(channelTarget,errorChannel);
                    isc=errorChannel;
                    // demand output
                    try {
                        ChannelRuntimeData erd = new ChannelRuntimeData();
                        erd.setBrowserInfo(binfo);
                        erd.setChannelSubscribeId(channelTarget);

                        UPFileSpec eup=new UPFileSpec(uPElement);
                        eup.setTargetNodeId(channelTarget);
                        erd.setUPFile(up);

                        errorChannel.setPortalControlStructures(pcs);
                        errorChannel.setRuntimeData(erd);
                    } catch (Exception e2) {
                        // things are looking bad for our hero
                        StringWriter sw=new StringWriter();
                        e2.printStackTrace(new PrintWriter(sw));
                        sw.flush();
                        LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                    }
                }
            }
        }
    }

    /**
     * Obtain an instance of a channel.
     *
     * @param channelSubscribeId a <code>String</code> value
     * @return an <code>IChannel</code> object
     */
    public IChannel getChannelInstance(String channelSubscribeId) {
        IChannel ch=(IChannel)channelTable.get(channelSubscribeId);
        if(ch==null) {
            try {
                ch=instantiateChannel(channelSubscribeId);
            } catch (Exception e) {
                return null;
            }
        }
        return ch;
    }


    public void removeChannel(String channelSubscribeId) {
        IChannel ch=(IChannel)channelTable.get(channelSubscribeId);
        if(ch!=null) {
            try {
                if(ulm.removeChannel(channelSubscribeId)) {
                    // clean up channel cache
                    channelCacheTable.remove(ch);
                    ch.receiveEvent(new PortalEvent(PortalEvent.SESSION_DONE));
                    channelTable.remove(ch);
                }
            } catch (PortalException gre) {
                LogService.instance().log(LogService.ERROR,"ChannelManager::removeChannel(): exception raised when trying to remove a channel : "+gre);
            }
        }
    }

    
    public void setChannelCharacterCache(String channelSubscribeId,String ccache) {
        ChannelRenderer cr;
        if ((cr=(ChannelRenderer)rendererTable.get(channelSubscribeId)) != null) {
            cr.setCharacterCache(ccache);
        } else {
            LogService.instance().log(LogService.ERROR,"ChannelManager::setChannelCharacterCache() : channel with channelSubscribeId=\""+channelSubscribeId+"\" is not present in the renderer cache!");
        }
    }


    public void setReqNRes(HttpServletRequest request, HttpServletResponse response, UPFileSpec uPElement) {
        this.pcs.setHttpServletRequest(request);
        this.pcs.setHttpServletResponse(response);
        this.binfo=new BrowserInfo(request);
        this.uPElement=uPElement;
        rendererTable.clear();
        processRequestChannelParameters(request);

        // check portal JNDI context
        if(portalContext==null) {
            try {
                portalContext=getPortalContext();
            } catch (NamingException ne) {
                LogService.instance().log(LogService.ERROR,"ChannelManager::setReqNRes(): exception raised when trying to obtain initial JNDI context : "+ne);
            }
        }
        // construct a channel context
        if(channelContext==null) {
            try {
                channelContext=getChannelContext(portalContext,request.getSession(false).getId(),Integer.toString(this.pcs.getUserPreferencesManager().getPerson().getID()),Integer.toString(this.pcs.getUserPreferencesManager().getCurrentProfile().getProfileId()));
            } catch (NamingException ne) {
                LogService.instance().log(LogService.ERROR,"ChannelManager::setReqNRes(): exception raised when trying to obtain channel JNDI context : "+ne);
            }
        }
    }


    public void setUPElement(UPFileSpec uPElement) {
        this.uPElement=uPElement;
    }

    public void setUserPreferencesManager(IUserPreferencesManager m) {
        ulm=m;
    }


    /**
     * Start rendering the channel in a separate thread.
     * This function retreives a particular channel from cache, passes parameters to the
     * channel and then creates a new ChannelRenderer object to render the channel in a
     * separate thread.
     * @param channelSubscribeId channel subscribe id
     * @param className name of the channel class
     * @param params a table of parameters
     */
    public void startChannelRendering(String channelSubscribeId, String channelPublishId, String className, long timeOut, Hashtable params,boolean ccacheable)
    {
        // see if the channel is cached
        IChannel ch;

        if ((ch = (IChannel) channelTable.get(channelSubscribeId)) == null) {
            try {
                ch=instantiateChannel(channelSubscribeId,channelPublishId,className,timeOut,params);
            } catch (Exception e) {
                CError errorChannel=new CError(CError.SET_STATIC_DATA_EXCEPTION,e,channelSubscribeId,null);
                channelTable.put(channelSubscribeId,errorChannel);
                ch=errorChannel;
            }
        }

        ChannelRuntimeData rd=null;

        if(!channelSubscribeId.equals(channelTarget)) {
            if((ch instanceof IPrivilegedChannel)) {
                // send the control structures
                try {
                    ((IPrivilegedChannel) ch).setPortalControlStructures(pcs);
                } catch (Exception e) {
                    channelTable.remove(ch);
                    CError errorChannel=new CError(CError.SET_PCS_EXCEPTION,e,channelSubscribeId,ch);
                    channelTable.put(channelSubscribeId,errorChannel);
                    ch=errorChannel;
                    // set portal control structures
                    try {
                        errorChannel.setPortalControlStructures(pcs);
                    } catch (Exception e2) {
                        // things are looking bad for our hero
                        StringWriter sw=new StringWriter();
                        e2.printStackTrace(new PrintWriter(sw));
                        sw.flush();
                        LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                    }
                }
            }
            rd = new ChannelRuntimeData();
            rd.setBrowserInfo(binfo);
            rd.setChannelSubscribeId(channelSubscribeId);

            UPFileSpec up=new UPFileSpec(uPElement);
            up.setTargetNodeId(channelSubscribeId);
            rd.setUPFile(up);

        } else {
            if(!(ch instanceof IPrivilegedChannel)) {
                rd = new ChannelRuntimeData();
                rd.setParameters(targetParams);
                rd.setBrowserInfo(binfo);
                rd.setChannelSubscribeId(channelSubscribeId);

                UPFileSpec up=new UPFileSpec(uPElement);
                up.setTargetNodeId(channelSubscribeId);
                rd.setUPFile(up);

            }
        }

        // Check if channel is rendering as the root element of the layout
        String userLayoutRoot = ulm.getUserPreferences().getStructureStylesheetUserPreferences().getParameterValue("userLayoutRoot");
        if (rd != null && userLayoutRoot != null && !userLayoutRoot.equals("root"))
          rd.setRenderingAsRoot(true);

        ChannelRenderer cr = new ChannelRenderer(ch,rd);
        cr.setCharacterCacheable(ccacheable);
        if(ch instanceof ICacheable) {
            cr.setCacheTables(this.channelCacheTable);
        }
        cr.setTimeout(timeOut);
        cr.startRendering();
        rendererTable.put(channelSubscribeId,cr);
    }
}
