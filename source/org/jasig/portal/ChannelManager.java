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
import java.util.HashSet;
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;

import org.jasig.portal.layout.UserLayoutChannelDescription;
import org.jasig.portal.layout.UserLayoutNodeDescription;

import tyrex.naming.MemoryContext;
import org.jasig.portal.serialize.CachingSerializer;

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
    private IUserPreferencesManager upm;
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

    private Set repeatRenderings=new HashSet();
    private boolean ccaching=false;

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
        this.upm=manager;
        pcs=new PortalControlStructures();
        pcs.setUserPreferencesManager(upm);
        pcs.setChannelManager(this);
        this.setReqNRes(request,response,uPElement);
    }


    public ChannelManager(IUserPreferencesManager manager) {
        this();
        this.upm=manager;
        pcs=new PortalControlStructures();
        pcs.setUserPreferencesManager(manager);
        pcs.setChannelManager(this);
    }


    /**
     * Directly places a channel instance into the hashtable of active channels.
     * This is designed to be used by the error channel only.
     */

    public void setChannelInstance(String channelSubscribeId,IChannel channelInstance) {
        if(channelTable.get(channelSubscribeId)!=null) {
            channelTable.remove(channelSubscribeId);
        }
        channelTable.put(channelSubscribeId,channelInstance);
    }

    /**
     * A method to notify <code>ChannelManager</code> that the channel set for
     * the current rendering cycle is complete.
     * Note: This information is used to identify relevant channel communication dependencies
     */
    public void commitToRenderingChannelSet() {
    }


    /**
     * Clean up after a rendering round.
     */
    public void finishedRendering() {
        // clean up
        rendererTable.clear();
        clearRepeatedRenderings();
        targetParams=null;
    }


    /**
     * Handle end-of-session cleanup
     *
     */
    public void finishedSession() {
        this.finishedRendering();

        // send SESSION_DONE event to all the channels
        PortalEvent ev=new PortalEvent(PortalEvent.SESSION_DONE);
        for(Enumeration e=channelTable.elements();e.hasMoreElements();) {
            ((IChannel)e.nextElement()).receiveEvent(ev);
        }

        //channelCacheTable.clear();
        //channelTable.clear()
    }

    public void outputChannel(String channelSubscribeId,ContentHandler ch) {
        // obtain ChannelRenderer
        ChannelRenderer cr=(ChannelRenderer)rendererTable.get(channelSubscribeId);
        if(cr==null) {
            // channel rendering wasn't started ?
            try {
                cr=startChannelRendering(channelSubscribeId);
            } catch (PortalException pe) {
                // record, and go on
                LogService.log(LogService.ERROR,"ChannelManager::outputChannel() : Encountered a portal exception while trying to start channel rendering! :"+pe);
            }
        }
        
        // complete rendering and check status
        int renderingStatus=-1;
        try {
            renderingStatus=cr.completeRendering();
        } catch (Throwable t) {
            handleRenderingError(channelSubscribeId,ch,t,renderingStatus,"encountered problem while trying to complete rendering","ChannelRenderer.completeRendering() threw",false);
            return;
        }

        if(renderingStatus==cr.RENDERING_SUCCESSFUL) {
            // obtain content
            if(ch instanceof CachingSerializer && this.isCharacterCaching()) {
                CachingSerializer cs=(CachingSerializer) ch;
                // need to get characters
                String characterContent=cr.getCharacters();
                if(characterContent==null) {
                    // obtain a SAX Buffer content then
                    SAX2BufferImpl bufferedContent=cr.getBuffer();
                    if(bufferedContent!=null) {
                        // translate SAX Buffer into the character version
                        try {
                            if(!cs.startCaching()) {
                                LogService.log(LogService.ERROR,"ChannelManager::outputChannel() : unable to restart character cache while compiling character cache for channel \""+channelSubscribeId+"\" !");
                            }
                            // dump SAX buffer into the serializer
                            bufferedContent.outputBuffer(ch);
                            // extract compiled character cache
                            if(cs.stopCaching()) {
                                try {
                                    characterContent=cs.getCache();
                                    if(characterContent!=null) {
                                        // save compiled character cache
                                        cr.setCharacterCache(characterContent);
                                        //LogService.log(LogService.DEBUG,"------ channel "+channelSubscribeId+" character block (compiled):");
                                        // LogService.log(LogService.DEBUG,characterContent);
                                    } else {
                                        LogService.log(LogService.ERROR,"ChannelManager::outputChannel() : character caching serializer returned NULL character cache for channel \""+channelSubscribeId+"\" !");
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    LogService.log(LogService.ERROR,"ChannelManager::outputChannel() :unable to compile character cache for channel \""+channelSubscribeId+"\"! Invalid encoding specified.",e);
                                } catch (IOException ioe) {
                                    LogService.log(LogService.ERROR,"ChannelManager::outputChannel() :IO exception occurred while compiling character cache for channel \""+channelSubscribeId+"\" !",ioe);
                                }
                            } else {
                                LogService.instance().log(LogService.ERROR,"ChannelManager::outputChannel() : unable to reset cache state while compiling character cache for channel \""+channelSubscribeId+"\" ! Serializer was not caching when it should've been ! Partial output possible!");
                                return;
                            }
                        } catch (IOException ioe) {
                            handleRenderingError(channelSubscribeId,ch,ioe,renderingStatus,"encountered a problem compiling channel character content","Encountered IO exception while trying to output channel content SAX to the character caching serializer",true);
                            return;
                        } catch (org.xml.sax.SAXException se) {
                            handleRenderingError(channelSubscribeId,ch,se,renderingStatus,"encountered a problem compiling channel character content","Encountered SAX exception while trying to output channel content SAX to the character caching serializer",true);
                            return;
                        }

                    } else {
                        handleRenderingError(channelSubscribeId,ch,null,renderingStatus,"unable to obtain channel rendering","ChannelRenderer.getBuffer() returned null",false);
                        return;
                    }                    
                } else { // non-null characterContent case
                    // output character content
                    try {
                        cs.printRawCharacters(characterContent);
                        // LogService.log(LogService.DEBUG,"------ channel "+channelSubscribeId+" character block (retrieved):");
                        // LogService.log(LogService.DEBUG,characterContent);
                    } catch (IOException ioe) {
                        LogService.log(LogService.DEBUG,"ChannelManager::outputChannel() : exception thrown while trying to output character cache for channelSubscribeId=\""+channelSubscribeId+"\". Message: "+ioe.getMessage());
                    }
                }
            } else { // regular serializer case
                // need to output straight
                SAX2BufferImpl bufferedContent=cr.getBuffer();
                if(bufferedContent!=null) {
                    try {
                        // output to the serializer
                        ChannelSAXStreamFilter custodian = new ChannelSAXStreamFilter(ch);
                        bufferedContent.outputBuffer(custodian);
                    } catch (Exception e) {
                        LogService.log(LogService.ERROR,"ChannelManager::outputChannel() : encountered an exception while trying to output SAX2 content of channel \""+channelSubscribeId+"\" to a regular serializer. Partial output possible !",e);
                        return;
                    }
                } else {
                    handleRenderingError(channelSubscribeId,ch,null,renderingStatus,"unable to obtain channel rendering","ChannelRenderer.getBuffer() returned null",false);
                    return;
                }
            }
        } else {
            handleRenderingError(channelSubscribeId,ch,null,renderingStatus,"unsuccessfull rendering","unsuccessfull rendering",false);
            return;
        }
    }

    private boolean isRepeatedRenderingAttempt(String channelSubscribeId) {
        return repeatRenderings.contains(channelSubscribeId);
    }

    private void setRepeatedRenderingAttempt(String channelSubscribeId) {
        repeatRenderings.add(channelSubscribeId);
    }

    private void clearRepeatedRenderings() {
        repeatRenderings.clear();
    }

    /**
     * Handles rendering output errors by replacing a channel instance with that of an error channel.
     * (or giving up if the error channel is failing as well)
     *
     * @param channelSubscribeId a <code>String</code> value
     * @param ch a <code>ContentHandler</code> value
     * @param t a <code>Throwable</code> value
     * @param renderingStatus an <code>int</code> value
     * @param commonMessage a <code>String</code> value
     * @param technicalMessage a <code>String</code> value
     * @param partialOutput a <code>boolean</code> value
     */
    private void handleRenderingError(String channelSubscribeId,ContentHandler ch, Throwable t, int renderingStatus, String commonMessage, String technicalMessage,boolean partialOutput) {
        if(isRepeatedRenderingAttempt(channelSubscribeId)) {
            // this means that the error channel has failed :(
            String message="ChannelManager::handleRenderingError() : Unable to handle a rendering error through error channel.";
            if(t!=null) {
                if(t instanceof InternalPortalException) {
                    InternalPortalException ipe=(InternalPortalException) t;
                    Throwable e=ipe.getException();
                    message=message+" Error channel (channelSubscribeId=\""+channelSubscribeId+"\") has thrown the following exception: "+e.toString()+" Partial output possible !";
                } else {
                    message=message+" An following exception encountered while trying to render the error channel for channelSubscribeId=\""+channelSubscribeId+"\": "+t.toString();
                }
            } else {
                // check status
                if(renderingStatus!=-1) {
                    message=message+" channelRenderingStatus="+ChannelRenderer.renderingStatus[renderingStatus];
                }
            }
            message=message+" "+technicalMessage;
            LogService.log(LogService.ERROR,message);
        } else {
            // first check for an exception
            if(t!=null ){
                if(t instanceof InternalPortalException) {
                    InternalPortalException ipe=(InternalPortalException) t;
                    Throwable channelException=ipe.getException();
                    replaceWithErrorChannel(channelSubscribeId,CError.RENDER_TIME_EXCEPTION,channelException,technicalMessage,false);
                } else {
                    replaceWithErrorChannel(channelSubscribeId,CError.RENDER_TIME_EXCEPTION,t,technicalMessage,false);
                }
            } else {
                if(renderingStatus==ChannelRenderer.RENDERING_TIMED_OUT) {
                    replaceWithErrorChannel(channelSubscribeId,CError.TIMEOUT_EXCEPTION,t,technicalMessage,false);
                } else {
                    replaceWithErrorChannel(channelSubscribeId,CError.GENERAL_ERROR,t,technicalMessage,false);
                }
            }

            // remove channel renderer
            rendererTable.remove(channelSubscribeId);
            // re-try render
            if(!partialOutput) {
                setRepeatedRenderingAttempt(channelSubscribeId);
                outputChannel(channelSubscribeId,ch);
            }
        }
    }

    private IChannel replaceWithErrorChannel(String channelSubscribeId,int errorCode, Throwable t, String message,boolean setRuntimeData) {
        // get and delete old channel instance
        IChannel oldInstance=(IChannel) channelTable.get(channelSubscribeId);
        channelTable.remove(channelSubscribeId);
        rendererTable.remove(channelSubscribeId);

        CError errorChannel=new CError(errorCode,t,channelSubscribeId,oldInstance,message);
        if(setRuntimeData) {
            ChannelRuntimeData rd=new ChannelRuntimeData();
            rd.setBrowserInfo(binfo);
            rd.setChannelSubscribeId(channelSubscribeId);
            UPFileSpec up=new UPFileSpec(uPElement);
            up.setTargetNodeId(channelSubscribeId);
            rd.setUPFile(up);
            try {
                errorChannel.setRuntimeData(rd);
                errorChannel.setPortalControlStructures(pcs);
            } catch (Throwable e) {
                // have to ignore this one, this is the last safety here
                StringWriter sw=new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                sw.flush();
                LogService.log(LogService.ERROR,"Encountered an exception while trying to set runtime data or portal control structures on the error channel!"+sw.toString());
            }
        }
        channelTable.put(channelSubscribeId,errorChannel);
        return errorChannel;
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
    public IChannel instantiateChannel(String channelSubscribeId) throws PortalException {
        if (channelTable.get(channelSubscribeId) != null) {
            // reinstantiation
            channelTable.remove(channelSubscribeId);
        }
        // get channel information from the user layout manager
        UserLayoutChannelDescription channel=(UserLayoutChannelDescription) upm.getUserLayoutManager().getNode(channelSubscribeId);
        if(channel!=null) {
            String className=channel.getClassName();
            String channelPublishId=channel.getChannelPublishId();
            long timeOut=channel.getTimeout();
            Hashtable params=new Hashtable(channel.getParameterMap());
            try {
                return instantiateChannel(channelSubscribeId,channelPublishId, className,timeOut,params);
            } catch (Exception ex) {
                LogService.instance().log(LogService.ERROR,"ChannelManager::instantiateChannel() : unable to instantiate channel class \""+className+"\". "+ex);
                return null;
            }
        } else return null;

    }


    private IChannel instantiateChannel(String channelSubscribeId, String channelPublishId, String className, long timeOut, Map params) throws Exception {
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
            try {
                UserLayoutChannelDescription channel=(UserLayoutChannelDescription) upm.getUserLayoutManager().getNode(channelSubscribeId);
                if(channel!=null) {
                    sd.setChannelPublishId(channel.getChannelPublishId());
                }
            } catch (Exception e) {};

            // Set the PermissionManager for this channel (no longer necessary)
            // sd.setPermissionManager(new ReferencePermissionManager("CHAN_ID." + ulm.getChannelSubscribeId(channelSubscribeId)));

            // get person object from UsreLayoutManager
            sd.setPerson(upm.getPerson());

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
                if(upm.getUserLayoutManager().deleteNode(channelSubscribeId)) {
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

    public boolean isCharacterCaching() {
        return this.ccaching;
    }

    public void setCharacterCaching(boolean setting) {
        this.ccaching=setting;
    }

    public void setUPElement(UPFileSpec uPElement) {
        this.uPElement=uPElement;
    }

    public void setUserPreferencesManager(IUserPreferencesManager m) {
        upm=m;
    }

    public ChannelRenderer startChannelRendering(String channelSubscribeId) throws PortalException {
        return startChannelRendering(channelSubscribeId,false);
    }

    private ChannelRenderer startChannelRendering(String channelSubscribeId,boolean noTimeout) throws PortalException {
        // see if the channel has already been instantiated
        // see if the channel is cached
        IChannel ch;
        long timeOut=0;
        
        UserLayoutNodeDescription node=upm.getUserLayoutManager().getNode(channelSubscribeId);
        if(!(node instanceof UserLayoutChannelDescription)) {
            throw new PortalException("\""+channelSubscribeId+"\" is not a channel node !");
        }
        
        UserLayoutChannelDescription channel=(UserLayoutChannelDescription) node;
        timeOut=channel.getTimeout();

        if ((ch = (IChannel) channelTable.get(channelSubscribeId)) == null) {
            try {
                ch=instantiateChannel(channelSubscribeId,channel.getChannelPublishId(),channel.getClassName(),channel.getTimeout(),channel.getParameterMap());
            } catch (Exception e) {
                ch=replaceWithErrorChannel(channelSubscribeId,CError.SET_STATIC_DATA_EXCEPTION,e,null,false);
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
        String userLayoutRoot = upm.getUserPreferences().getStructureStylesheetUserPreferences().getParameterValue("userLayoutRoot");
        if (rd != null && userLayoutRoot != null && !userLayoutRoot.equals("root"))
          rd.setRenderingAsRoot(true);

        ChannelRenderer cr = new ChannelRenderer(ch,rd);
        cr.setCharacterCacheable(this.isCharacterCaching());
        if(ch instanceof ICacheable) {
            cr.setCacheTables(this.channelCacheTable);
        }

        if(noTimeout) {
            cr.setTimeout(0);
        } else {
            cr.setTimeout(timeOut);
        }

        cr.startRendering();
        rendererTable.put(channelSubscribeId,cr);
        return cr;        

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
    public ChannelRenderer startChannelRendering(String channelSubscribeId, String channelPublishId, String className, long timeOut, Hashtable params,boolean ccacheable)
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
        String userLayoutRoot = upm.getUserPreferences().getStructureStylesheetUserPreferences().getParameterValue("userLayoutRoot");
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
        return cr;
    }
}
