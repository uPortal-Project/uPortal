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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.channels.CError;
import org.jasig.portal.channels.CSecureInfo;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.IUserLayoutChannelDescription;
import org.jasig.portal.layout.IUserLayoutNodeDescription;
import org.jasig.portal.layout.LayoutEvent;
import org.jasig.portal.layout.LayoutEventListener;
import org.jasig.portal.layout.LayoutMoveEvent;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.serialize.CachingSerializer;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.StatsRecorder;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.SetCheckInSemaphore;
import org.jasig.portal.utils.SoftHashMap;
import org.xml.sax.ContentHandler;

import tyrex.naming.MemoryContext;

/**
 * ChannelManager shall have the burden of squeezing content out of channels.
 * <p>
 * Validation and timeouts, these two are needed for smooth operation of the portal
 * sometimes channels will timeout with information retreival then the content should
 * be skipped.
 *
 * @author Peter Kharchenko, pkharchenko@interactivebusiness.com
 * @version $Revision$
 */
public class ChannelManager implements LayoutEventListener {
    private IUserPreferencesManager upm;
    private PortalControlStructures pcs;

    private Hashtable channelTable;
    private Hashtable rendererTable;
    private Map channelCacheTable;

    private String channelTarget;
    private Hashtable targetParams;
    private BrowserInfo binfo;
    private LocaleManager lm;

    private Context portalContext;
    private Context channelContext;

    // inter-channel communication tables
    private HashMap iccTalkers;
    private HashMap iccListeners;

    // a set of channels requested for rendering, but
    // awaiting rendering set commit due to inter-channel
    // communication
    private Set pendingChannels;
    private boolean groupedRendering;

    private IAuthorizationPrincipal ap;

    /** Factory used to build all channel renderer objects. */
    private static final IChannelRendererFactory cChannelRendererFactory =
        ChannelRendererFactory.newInstance(
            ChannelManager.class.getName()
            );

    public UPFileSpec uPElement;

    // global channel rendering cache
    public static final int SYSTEM_CHANNEL_CACHE_MIN_SIZE=50; // this should be in a file somewhere
    public static final SoftHashMap systemCache=new SoftHashMap(SYSTEM_CHANNEL_CACHE_MIN_SIZE);

    public static final String channelAddressingPathElement="channel";

    private Set repeatRenderings=new HashSet();
    private boolean ccaching=false;

    public ChannelManager() {
        channelTable=new Hashtable();
        rendererTable=new Hashtable();
        iccTalkers=new HashMap();
        iccListeners=new HashMap();
        channelCacheTable=Collections.synchronizedMap(new WeakHashMap());
        groupedRendering=false;
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
        this.startRenderingCycle(request,response,uPElement);
    }


    /**
     * Creates a new <code>ChannelManager</code> instance.
     *
     * @param manager an <code>IUserPreferencesManager</code> value
     */
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
        if(groupedRendering) {
            // separate out the dependency group in s0

            HashSet s0=new HashSet();
            Set children;

            s0.add(channelTarget);
            pendingChannels.remove(channelTarget);
            children=getListeningChannels(channelTarget);
            if(children!=null && !children.isEmpty()) {
                children.retainAll(pendingChannels);
                while(!children.isEmpty()) {
                    // move to the next generation
                    HashSet newChildren=new HashSet();
                    for(Iterator ci=children.iterator();ci.hasNext();) {
                        String childId=(String)ci.next();
                        s0.add(childId);
                        pendingChannels.remove(childId);
                        Set currentChildren=getListeningChannels(childId);
                        if(currentChildren!=null) {
                            newChildren.addAll(currentChildren);
                        }
                    }
                    newChildren.retainAll(pendingChannels);
                    children=newChildren;
                }
            }

            // now s0 group must be synchronized at renderXML(), while the remaining pendingChildren can be rendered freely
            SetCheckInSemaphore s0semaphore= new SetCheckInSemaphore(new HashSet(s0));
            for(Iterator gi=s0.iterator();gi.hasNext();) {
                String channelSubscribeId=(String)gi.next();
                IChannelRenderer cr=(IChannelRenderer) rendererTable.get(channelSubscribeId);
                cr.startRendering(s0semaphore,channelSubscribeId);
            }

            for(Iterator oi=pendingChannels.iterator();oi.hasNext();) {
                String channelSubscribeId=(String)oi.next();
                IChannelRenderer cr=(IChannelRenderer) rendererTable.get(channelSubscribeId);
                cr.startRendering();
            }
        }
    }


    /**
     * Clean up after a rendering round.
     */
    public void finishedRenderingCycle() {
        // clean up
        rendererTable.clear();
        clearRepeatedRenderings();
        targetParams=null;
        pendingChannels=new HashSet();
        groupedRendering=false;
    }


    /**
     * Handle end-of-session cleanup
     *
     */
    public void finishedSession() {
        this.finishedRenderingCycle();

        // send SESSION_DONE event to all the channels
        PortalEvent ev=new PortalEvent(PortalEvent.SESSION_DONE);
        for(Enumeration enum=channelTable.elements();enum.hasMoreElements();) {
            IChannel ch = (IChannel)enum.nextElement();
            if (ch != null) {
                try {
                    ch.receiveEvent(ev);
                } catch (Exception e) {
                    LogService.log(LogService.ERROR, e);
                }
            }
        }

        // we dont' really need to clean anything here,
        // since the entire session will be destroyed
        //channelCacheTable.clear();
        //channelTable.clear()
    }

    /**
     * Outputs a channel in to a given content handler.
     * If the current rendering cycle is targeting character
     * cache output, and the content handler passed to the method
     * is an instance of <code>CachingSerializer</code>, the method
     * will take care of character cache compilation and store cache
     * in the tables.
     *
     * @param channelSubscribeId a <code>String</code> value
     * @param ch a <code>ContentHandler</code> value
     */
    public void outputChannel(String channelSubscribeId,ContentHandler ch) {
        // obtain IChannelRenderer
        IChannelRenderer cr=(IChannelRenderer)rendererTable.get(channelSubscribeId);
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
            handleRenderingError(channelSubscribeId,ch,t,renderingStatus,"encountered problem while trying to complete rendering","IChannelRenderer.completeRendering() threw",false);
            return;
        }

        if(renderingStatus==IChannelRenderer.RENDERING_SUCCESSFUL) {
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
                                    } else {
                                        LogService.log(LogService.ERROR,"ChannelManager::outputChannel() : character caching serializer returned NULL character cache for channel \""+channelSubscribeId+"\" !");
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    LogService.log(LogService.ERROR,"ChannelManager::outputChannel() :unable to compile character cache for channel \""+channelSubscribeId+"\"! Invalid encoding specified.",e);
                                } catch (IOException ioe) {
                                    LogService.log(LogService.ERROR,"ChannelManager::outputChannel() :IO exception occurred while compiling character cache for channel \""+channelSubscribeId+"\" !",ioe);
                                }
                            } else {
                                LogService.log(LogService.ERROR,"ChannelManager::outputChannel() : unable to reset cache state while compiling character cache for channel \""+channelSubscribeId+"\" ! Serializer was not caching when it should've been ! Partial output possible!");
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
                        handleRenderingError(channelSubscribeId,ch,null,renderingStatus,"unable to obtain channel rendering","IChannelRenderer.getBuffer() returned null",false);
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
                    handleRenderingError(channelSubscribeId,ch,null,renderingStatus,"unable to obtain channel rendering","IChannelRenderer.getBuffer() returned null",false);
                    return;
                }
            }

            // Obtain the channel description
            IUserLayoutChannelDescription channelDesc = null;
            try {
              channelDesc = (IUserLayoutChannelDescription)upm.getUserLayoutManager().getNode(channelSubscribeId);
            } catch (PortalException pe) {
              // Do nothing
            }

            // Tell the StatsRecorder that this channel has rendered
            StatsRecorder.recordChannelRendered(upm.getPerson(), upm.getCurrentProfile(), channelDesc);
        } else {
            handleRenderingError(channelSubscribeId,ch,null,renderingStatus,"unsuccessfull rendering","unsuccessfull rendering",false);
            return;
        }
    }

    /**
     * Check if repeated rendering has been attempted for a given channel.
     *
     * @param channelSubscribeId a <code>String</code> value
     * @return a <code>boolean</code> value
     */
    private boolean isRepeatedRenderingAttempt(String channelSubscribeId) {
        return repeatRenderings.contains(channelSubscribeId);
    }

    /**
     * Register a repeated rendering attempt for a particular channel.
     *
     * @param channelSubscribeId a <code>String</code> value
     */
    private void setRepeatedRenderingAttempt(String channelSubscribeId) {
        repeatRenderings.add(channelSubscribeId);
    }

    /**
     * Clear information about repeated rendering attempts.
     *
     */
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
                    System.out.println("CError produced the following exception. Please fix CError immediately!");
                    e.printStackTrace();
                } else {
                    message=message+" An following exception encountered while trying to render the error channel for channelSubscribeId=\""+channelSubscribeId+"\": "+t.toString();
                    System.out.println("CError produced the following exception. Please fix CError immediately!");
                    t.printStackTrace();
                }
            } else {
                // check status
                message=message+" channelRenderingStatus=";

                switch( renderingStatus )
                {
                    case IChannelRenderer.RENDERING_SUCCESSFUL:
                        message += "successful";
                        break;
                    case IChannelRenderer.RENDERING_FAILED:
                        message += "failed";
                        break;
                    case IChannelRenderer.RENDERING_TIMED_OUT:
                        message += "timed out";
                        break;
                    default:
                        message += "UNKNOWN CODE: " + renderingStatus;
                        break;
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
                    replaceWithErrorChannel(channelSubscribeId,CError.RENDER_TIME_EXCEPTION,channelException,technicalMessage,true);
                } else {
                    replaceWithErrorChannel(channelSubscribeId,CError.RENDER_TIME_EXCEPTION,t,technicalMessage,true);
                }
            } else {
                if(renderingStatus==IChannelRenderer.RENDERING_TIMED_OUT) {
                    replaceWithErrorChannel(channelSubscribeId,CError.TIMEOUT_EXCEPTION,t,technicalMessage,true);
                } else {
                    replaceWithErrorChannel(channelSubscribeId,CError.GENERAL_ERROR,t,technicalMessage,true);
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

    /**
     * A helper method to replace all occurences of a given channel instance with that of an error channel.
     *
     * @param channelSubscribeId a <code>String</code> value
     * @param errorCode an <code>int</code> value from CError.* constants
     * @param t a <code>Throwable</code> an exception that caused the problem
     * @param message a <code>String</code> an optional message to pass to the error channel
     * @param setRuntimeData a <code>boolean</code> wether the method should also set the ChannelRuntimeData for the newly instantiated error channel
     * @return an <code>IChannel</code> value of an error channel instance
     */
    private IChannel replaceWithErrorChannel(String channelSubscribeId,int errorCode, Throwable t, String message,boolean setRuntimeData) {
        // get and delete old channel instance
        IChannel oldInstance=(IChannel) channelTable.get(channelSubscribeId);
        channelTable.remove(channelSubscribeId);
        rendererTable.remove(channelSubscribeId);

        CError errorChannel=new CError(errorCode,t,channelSubscribeId,oldInstance,message);
        if(setRuntimeData) {
            ChannelRuntimeData rd=new ChannelRuntimeData();
            rd.setBrowserInfo(binfo);
            if (lm != null)  {
                rd.setLocales(lm.getLocales());
            }
            rd.setHttpRequestMethod(pcs.getHttpServletRequest().getMethod());
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
     * A helper method to replace all occurences of a secure channel instance
     * with that of a secure information channel.
     *
     * @param channelSubscribeId a <code>String</code> value
     * @param setRuntimeData a <code>boolean</code> wether the method should also set the ChannelRuntimeData for the newly instantiated secure info channel
     * @return an <code>IChannel</code> value of a secure info channel instance
     */
    private IChannel replaceWithSecureInfoChannel(String channelSubscribeId, boolean setRuntimeData) {
        // get and delete old channel instance
        IChannel oldInstance=(IChannel) channelTable.get(channelSubscribeId);
        channelTable.remove(channelSubscribeId);
        rendererTable.remove(channelSubscribeId);

        CSecureInfo secureInfoChannel=new CSecureInfo(channelSubscribeId,oldInstance);
        if(setRuntimeData) {
            ChannelRuntimeData rd=new ChannelRuntimeData();
            rd.setBrowserInfo(binfo);
            if (lm != null)  {
                rd.setLocales(lm.getLocales());
            }
            rd.setHttpRequestMethod(pcs.getHttpServletRequest().getMethod());
            UPFileSpec up=new UPFileSpec(uPElement);
            up.setTargetNodeId(channelSubscribeId);
            rd.setUPFile(up);
            try {
                secureInfoChannel.setRuntimeData(rd);
                secureInfoChannel.setPortalControlStructures(pcs);
            } catch (Throwable e) {
                // have to ignore this one, this is the last safety here
                StringWriter sw=new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                sw.flush();
                LogService.log(LogService.ERROR,"Encountered an exception while trying to set runtime data or portal control structures on the secure info channel!"+sw.toString());
            }
        }
        channelTable.put(channelSubscribeId,secureInfoChannel);
        return secureInfoChannel;
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
    private static Context getChannelJndiContext(Context portalContext,String sessionId,String userId,String layoutId) throws NamingException {
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
        cic.bind("portlet-ids",new ArrayList());

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
        IUserLayoutChannelDescription channel=(IUserLayoutChannelDescription) upm.getUserLayoutManager().getNode(channelSubscribeId);
        if(channel!=null)
            return instantiateChannel(channel);
        else
            return null;
    }

    private IChannel instantiateChannel(IUserLayoutChannelDescription cd) throws PortalException {
        IChannel ch=null;
        String channelSubscribeId=cd.getChannelSubscribeId();
        String channelPublishId=cd.getChannelPublishId();
        // check if the user has permissions to instantiate this channel
        if(ap==null) {
            EntityIdentifier ei = this.pcs.getUserPreferencesManager().getPerson().getEntityIdentifier();
            ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        }

        if(ap.canRender(Integer.parseInt(channelPublishId))) {

            // Instantiate the channel and notify the StatsRecorder
            ch = ChannelFactory.instantiateLayoutChannel(cd,this.pcs.getHttpServletRequest().getSession(false).getId());
            StatsRecorder.recordChannelInstantiated(upm.getPerson(), upm.getCurrentProfile(), cd);

            // Create and stuff the channel static data
            ChannelStaticData sd = new ChannelStaticData();
            sd.setChannelSubscribeId(channelSubscribeId);
            sd.setTimeout(cd.getTimeout());
            sd.setParameters(cd.getParameterMap());
            sd.setPerson(upm.getPerson());
            sd.setJNDIContext(channelContext);
            sd.setICCRegistry(new ICCRegistry(this,channelSubscribeId));
            sd.setChannelPublishId(cd.getChannelPublishId());

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
     * Passes a layout-level event to a channel.
     * @param channelSubscribeId the channel subscribe id
     * @param le the portal event
     */
    public void passPortalEvent(String channelSubscribeId, PortalEvent le) {
        IChannel ch= (IChannel) channelTable.get(channelSubscribeId);

        if (ch != null) {
            try {
                ch.receiveEvent(le);
            } catch (Exception e) {
                LogService.log(LogService.ERROR, e);
            }
        } else {
            LogService.log(LogService.ERROR, "ChannelManager::passPortalEvent() : trying to pass an event to a channel that is not in cache. (cahnel=\"" + channelSubscribeId + "\")");
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

        // see if this is targeted at an fname channel. if so then it takes
        // precedence. This is done so that a baseActionURL can be used for
        // the basis of an fname targeted channel with the fname query parm
        // appended to direct all query parms to the fname channel
        String fname = req.getParameter( Constants.FNAME_PARAM );

        if ( fname != null )
        {
            // need to get to wrapper for obtaining a subscribe id
            TransientUserLayoutManagerWrapper iulm =
                (TransientUserLayoutManagerWrapper) upm
                .getUserLayoutManager();
            // get a subscribe id for the fname
            try {
             channelTarget = iulm.getSubscribeId(fname);
            } catch ( PortalException pe ) {
               LogService.log(LogService.ERROR, "ChannelManager::processRequestChannelParameters(): Unable to get subscribe ID for fname="+fname);
              }
        }
        if ( channelTarget == null )
        {
            // check if the uP_channelTarget parameter has been passed
            channelTarget=req.getParameter("uP_channelTarget");
        }
        if(channelTarget==null) {
            // determine target channel id
            UPFileSpec upfs=new UPFileSpec(req);
            channelTarget=upfs.getTargetNodeId();
            LogService.log(LogService.DEBUG,"ChannelManager::processRequestChannelParameters() : channelTarget=\""+channelTarget+"\".");
        }

        if(channelTarget!=null) {
            // Obtain the channel description
            IUserLayoutChannelDescription channelDesc = null;
            try {
              channelDesc = (IUserLayoutChannelDescription)upm.getUserLayoutManager().getNode(channelTarget);
            } catch (PortalException pe) {
              // Do nothing
            }

            // Tell StatsRecorder that a user has interacted with the channel
            StatsRecorder.recordChannelTargeted(upm.getPerson(), upm.getCurrentProfile(), channelDesc);

            // process parameters
            Enumeration en = req.getParameterNames();
            if (en != null) {
                if(en.hasMoreElements()) {
                    // only do grouped rendering if there are some parameters passed
                    // to the target channel.
                    // detect if channel target talks to other channels
                    groupedRendering=hasListeningChannels(channelTarget);
                }
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
            // check if the channel is an IPrivilegedChannel, and if it is,
            // pass portal control structures and runtime data.
            IChannel chObj;
            if ((chObj=(IChannel)channelTable.get(channelTarget)) == null) {
                try {
                    chObj=instantiateChannel(channelTarget);
                } catch (Throwable e) {
                    LogService.log(LogService.ERROR,"ChannelManager::processRequestChannelParameters() : unable to pass find/create an instance of a channel. Bogus Id ? ! (id=\""+channelTarget+"\").");
                    LogService.log(LogService.ERROR,e);
                    chObj=replaceWithErrorChannel(channelTarget,CError.SET_STATIC_DATA_EXCEPTION,e,null,false);
                }
            }
            if(chObj!=null && (chObj instanceof IPrivileged)) {
                IPrivileged isc=(IPrivileged) chObj;

                try {
                    isc.setPortalControlStructures(pcs);
                } catch (Exception e) {
                    chObj=replaceWithErrorChannel(channelTarget,CError.SET_PCS_EXCEPTION,e,null,false);
                    isc=(IPrivileged) chObj;

                    // set portal control structures
                    try {
                        isc.setPortalControlStructures(pcs);
                    } catch (Exception e2) {
                        // things are looking bad for our hero
                        StringWriter sw=new StringWriter();
                        e2.printStackTrace(new PrintWriter(sw));
                        sw.flush();
                        LogService.log(LogService.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                    }

                }

                ChannelRuntimeData rd = new ChannelRuntimeData();
                rd.setParameters(targetParams);
                String qs = pcs.getHttpServletRequest().getQueryString();
                if (qs != null && qs.indexOf("=") == -1)
                  rd.setKeywords(qs);
                rd.setBrowserInfo(binfo);
                if (lm != null)  {
                    rd.setLocales(lm.getLocales());
                }
                rd.setHttpRequestMethod(pcs.getHttpServletRequest().getMethod());
                UPFileSpec up=new UPFileSpec(uPElement);
                up.setTargetNodeId(channelTarget);
                rd.setUPFile(up);

                // Check if channel is rendering as the root element of the layout
                String userLayoutRoot = upm.getUserPreferences().getStructureStylesheetUserPreferences().getParameterValue("userLayoutRoot");
                if (userLayoutRoot != null && !userLayoutRoot.equals("root")) {
                    rd.setRenderingAsRoot(true);
                }
                
                // Indicate that this channel is targeted
                rd.setTargeted(true);

                try {
                    chObj.setRuntimeData(rd);
                }
                catch (Exception e) {
                    chObj=replaceWithErrorChannel(channelTarget,CError.SET_RUNTIME_DATA_EXCEPTION,e,null,false);
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
            } catch (Throwable e) {
                return null;
            }
        }
        return ch;
    }


    /**
     * Removes channel instance from the internal caches.
     *
     * @param channelSubscribeId a <code>String</code> value
     */
    public void removeChannel(String channelSubscribeId) {
        IChannel ch=(IChannel)channelTable.get(channelSubscribeId);
        if(ch!=null) {
            channelCacheTable.remove(ch);
            try {
                ch.receiveEvent(new PortalEvent(PortalEvent.UNSUBSCRIBE));
            } catch (Exception e) {
                LogService.log(LogService.ERROR, e);
            }
            channelTable.remove(ch);
            LogService.log(LogService.DEBUG,"ChannelManager::removeChannel(): removed channel with subscribe id="+channelSubscribeId);
        }
    }

    /**
     * Signals the start of a new rendering cycle.
     *
     * @param request a <code>HttpServletRequest</code> value
     * @param response a <code>HttpServletResponse</code> value
     * @param uPElement an <code>UPFileSpec</code> value
     */
    public void startRenderingCycle(HttpServletRequest request, HttpServletResponse response, UPFileSpec uPElement) {
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
                LogService.log(LogService.ERROR,"ChannelManager::setReqNRes(): exception raised when trying to obtain initial JNDI context : "+ne);
            }
        }
        // construct a channel context
        if(channelContext==null) {
            try {
                channelContext=getChannelJndiContext(portalContext,request.getSession(false).getId(),Integer.toString(this.pcs.getUserPreferencesManager().getPerson().getID()),Integer.toString(this.pcs.getUserPreferencesManager().getCurrentProfile().getLayoutId()));
            } catch (NamingException ne) {
                LogService.log(LogService.ERROR,"ChannelManager::setReqNRes(): exception raised when trying to obtain channel JNDI context : "+ne);
            }
        }
    }

    /**
     * Specifies if this particular rendering cycle is using
     * character caching.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isCharacterCaching() {
        return this.ccaching;
    }

    /**
     * Specify that the current rendering cycle should be
     * using (or not) character caching.
     *
     * @param setting a <code>boolean</code> value
     */
    public void setCharacterCaching(boolean setting) {
        this.ccaching=setting;
    }

    /**
     * Specify <code>UPFileSpec</code> object that will be
     * used to construct file portion of the context path
     * in the auto-generated URLs, also known as the baseActionURL.
     *
     * @param uPElement an <code>UPFileSpec</code> value
     */
    public void setUPElement(UPFileSpec uPElement) {
        this.uPElement=uPElement;
    }

    /**
     * Specify <code>IUserPreferencesManager</code> to use.
     *
     * @param m an <code>IUserPreferencesManager</code> value
     */
    public void setUserPreferencesManager(IUserPreferencesManager m) {
        upm=m;
    }

    /**
     * Initiate channel rendering cycle.
     *
     * @param channelSubscribeId a <code>String</code> value
     * @return a <code>IChannelRenderer</code> value
     * @exception PortalException if an error occurs
     */
    public IChannelRenderer startChannelRendering(String channelSubscribeId) throws PortalException {
        return startChannelRendering(channelSubscribeId,false);
    }

    /**
     * Initiate channel rendering cycle, possibly disabling timeout.
     *
     * @param channelSubscribeId a <code>String</code> value
     * @param noTimeout a <code>boolean</code> value specifying if the
     *                  time out rendering control should be disabled.
     * @return a <code>IChannelRenderer</code> value
     * @exception PortalException if an error occurs
     */
    private IChannelRenderer startChannelRendering(String channelSubscribeId,boolean noTimeout) throws PortalException {
        // see if the channel has already been instantiated
        // see if the channel is cached
        IChannel ch;
        long timeOut=0;

        IUserLayoutNodeDescription node=upm.getUserLayoutManager().getNode(channelSubscribeId);
        if(!(node instanceof IUserLayoutChannelDescription)) {
            throw new PortalException("\""+channelSubscribeId+"\" is not a channel node !");
        }

        IUserLayoutChannelDescription channel=(IUserLayoutChannelDescription) node;
        timeOut=channel.getTimeout();

        ch = (IChannel) channelTable.get(channelSubscribeId);

        // replace channels that are specified as needing to be
        // rendered securely with CSecureInfo.
        if (!pcs.getHttpServletRequest().isSecure() && channel.isSecure()){
            if (ch == null || !(ch instanceof CSecureInfo)){
                ch = replaceWithSecureInfoChannel(channelSubscribeId,false);
            }
        }
        else{
            // A secure channel may not have been able to render at one
            // time but now it can, create its instance to replace the
            // cached CSecureInfo entry.
            if (ch == null || ch instanceof CSecureInfo) {
                try {
                    ch=instantiateChannel(channel);
                } catch (Throwable e) {
                    ch=replaceWithErrorChannel(channelSubscribeId,CError.SET_STATIC_DATA_EXCEPTION,e,null,false);
                }
            }
        }

        ChannelRuntimeData rd=null;

        if(!channelSubscribeId.equals(channelTarget)) {
            if((ch instanceof IPrivileged)) {
                // send the control structures
                try {
                    ((IPrivileged) ch).setPortalControlStructures(pcs);
                } catch (Exception e) {
                    ch=replaceWithErrorChannel(channelTarget,CError.SET_PCS_EXCEPTION,e,null,false);
                    channelTable.remove(ch);

                    // set portal control structures for the error channel
                    try {
                        ((IPrivileged) ch).setPortalControlStructures(pcs);
                    } catch (Exception e2) {
                        // things are looking bad for our hero
                        StringWriter sw=new StringWriter();
                        e2.printStackTrace(new PrintWriter(sw));
                        sw.flush();
                        LogService.log(LogService.ERROR,"ChannelManager::outputChannels : Error channel threw ! "+sw.toString());
                    }
                }
            }
            rd = new ChannelRuntimeData();
            rd.setBrowserInfo(binfo);
            if (lm != null)  {
                rd.setLocales(lm.getLocales());
            }
            rd.setHttpRequestMethod(pcs.getHttpServletRequest().getMethod());

            UPFileSpec up=new UPFileSpec(uPElement);
            up.setTargetNodeId(channelSubscribeId);
            rd.setUPFile(up);

        } else {
            // set up runtime data that will be passed to the IChannelRenderer
            if(!(ch instanceof IPrivileged)) {
                rd = new ChannelRuntimeData();
                rd.setParameters(targetParams);
                String qs = pcs.getHttpServletRequest().getQueryString();
                if (qs != null && qs.indexOf("=") == -1)
                  rd.setKeywords(qs);
                rd.setBrowserInfo(binfo);
                if (lm != null)  {
                    rd.setLocales(lm.getLocales());
                }
                rd.setHttpRequestMethod(pcs.getHttpServletRequest().getMethod());

                UPFileSpec up=new UPFileSpec(uPElement);
                up.setTargetNodeId(channelSubscribeId);
                rd.setUPFile(up);
            }
        }

        // Check if channel is rendering as the root element of the layout
        String userLayoutRoot = upm.getUserPreferences().getStructureStylesheetUserPreferences().getParameterValue("userLayoutRoot");
        if (rd != null && userLayoutRoot != null && !userLayoutRoot.equals("root")) {
            rd.setRenderingAsRoot(true);
        }

        // Build a new channel renderer instance.
        IChannelRenderer cr = cChannelRendererFactory.newInstance(
            ch,
            rd
            );

        cr.setCharacterCacheable(this.isCharacterCaching());
        if(ch instanceof ICacheable) {
            cr.setCacheTables(this.channelCacheTable);
        }

        if(noTimeout) {
            cr.setTimeout(0);
        } else {
            cr.setTimeout(timeOut);
        }

        if(groupedRendering && (isListeningToChannels(channelSubscribeId) || channelSubscribeId.equals(channelTarget))) {
            // channel might depend on the target channel
            pendingChannels.add(channelSubscribeId); // defer rendering start
        } else {
            cr.startRendering();
        }
        rendererTable.put(channelSubscribeId,cr);

        return cr;
    }

    synchronized void registerChannelDependency(String listenerChannelSubscribeId, String talkerChannelSubscribeId) {
        Set talkers=(Set)iccListeners.get(listenerChannelSubscribeId);
        if(talkers==null) {
            talkers=new HashSet();
            iccListeners.put(listenerChannelSubscribeId,talkers);
        }
        talkers.add(talkerChannelSubscribeId);

        Set listeners=(Set)iccTalkers.get(talkerChannelSubscribeId);
        if(listeners==null) {
            listeners=new HashSet();
            iccTalkers.put(talkerChannelSubscribeId,listeners);
        }
        listeners.add(listenerChannelSubscribeId);
    }


    private Set getListeningChannels(String talkerChannelSubscribeId) {
        return (Set)iccTalkers.get(talkerChannelSubscribeId);
    }

    private boolean isListeningToChannels(String listenerChannelSubscribeId) {
        return (iccListeners.get(listenerChannelSubscribeId)!=null);
    }

    private boolean hasListeningChannels(String talkerChannelSubscribeId) {
        return (iccTalkers.get(talkerChannelSubscribeId)!=null);
    }

    synchronized void removeChannelDependency(String listenerChannelSubscribeId, String talkerChannelSubscribeId) {
        Set talkers=(Set)iccListeners.get(listenerChannelSubscribeId);
        if(talkers!=null) {
            talkers.remove(talkerChannelSubscribeId);
            if(talkers.isEmpty()) {
                iccListeners.remove(listenerChannelSubscribeId);
            }
        }

        Set listeners=(Set)iccTalkers.get(talkerChannelSubscribeId);
        if(listeners!=null) {
            listeners.remove(listenerChannelSubscribeId);
            if(listeners.isEmpty()) {
                iccTalkers.remove(talkerChannelSubscribeId);
            }
        }
    }


    // LayoutEventListener interface implementation
    public void channelAdded(LayoutEvent ev) {}
    public void channelUpdated(LayoutEvent ev) {}
    public void channelMoved(LayoutMoveEvent ev) {}
    public void channelDeleted(LayoutMoveEvent ev) {
        this.removeChannel(ev.getNodeDescription().getId());
    }

    public void folderAdded(LayoutEvent ev) {}
    public void folderUpdated(LayoutEvent ev) {}
    public void folderMoved(LayoutMoveEvent ev) {}
    public void folderDeleted(LayoutMoveEvent ev) {}

    public void layoutLoaded() {}
    public void layoutSaved() {}

    public void setLocaleManager(LocaleManager lm) { this.lm = lm; }
}
