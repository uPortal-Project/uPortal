/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections15.map.ReferenceMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.channels.CSecureInfo;
import org.jasig.portal.channels.error.CError;
import org.jasig.portal.channels.error.ErrorCode;
import org.jasig.portal.channels.support.IDynamicChannelTitleRenderer;
import org.jasig.portal.events.EventPublisherLocator;
import org.jasig.portal.events.support.ChannelInstanciatedInLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelRenderedInLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelTargetedInLayoutPortalEvent;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.jndi.IJndiManager;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.LayoutEvent;
import org.jasig.portal.layout.LayoutEventListener;
import org.jasig.portal.layout.LayoutMoveEvent;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.portlet.url.RequestType;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.serialize.CachingSerializer;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.url.support.IChannelRequestParameterManager;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.SetCheckInSemaphore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jndi.JndiTemplate;
import org.xml.sax.ContentHandler;

import tyrex.naming.MemoryContext;

/**
 * ChannelManager shall have the burden of squeezing content out of channels.
 * <p>
 * Validation and timeouts, these two are needed for smooth operation of the portal
 * sometimes channels will timeout with information retrieval then the content should
 * be skipped.
 *
 * @author Peter Kharchenko, pkharchenko@unicon.net
 * @version $Revision$
 */
public class ChannelManager implements LayoutEventListener {
    private static final String PORTAL_CONTROL_STRUCTURES_MAP_ATTR = ChannelManager.class.getName() + ".PortalControlStructuresMap";

    public static final String channelAddressingPathElement = "channel";

    // Metrics
    private static final AtomicLong activeRenderers = new AtomicLong();
    private static final AtomicLong maxRenderThreads = new AtomicLong();
    
    public static long getActiveRenderers() {
        return activeRenderers.get();
    }
    
    public static long getMaxRenderThreads() {
        return maxRenderThreads.get();
    }
    
    // Factory used to build all channel renderer objects.
    private static final IChannelRendererFactory cChannelRendererFactory = 
        ChannelRendererFactory.newInstance(ChannelManager.class.getName(), activeRenderers, maxRenderThreads);
    
    // global channel rendering cache
    public static final int SYSTEM_CHANNEL_CACHE_MIN_SIZE = 50; // this should be in a file somewhere
    public static final Map<String, ChannelCacheEntry> systemCache = new ReferenceMap<String, ChannelCacheEntry>(ReferenceMap.HARD, ReferenceMap.SOFT, SYSTEM_CHANNEL_CACHE_MIN_SIZE, .75f, true);

    private static boolean useAnchors = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.ChannelManager.use_anchors", false);
    

    private static final Log log = LogFactory.getLog(ChannelManager.class);

    private final IUserPreferencesManager userPreferencesManager;
    
    // Channel rendering tables
    private final Map<String, IChannel> channelTable = new Hashtable<String, IChannel>();
    private final Map<String, IChannelRenderer> rendererTable = new Hashtable<String, IChannelRenderer>();
    private final Map<IChannel, Map<String, ChannelCacheEntry>> channelCacheTable = Collections.synchronizedMap(new WeakHashMap<IChannel, Map<String, ChannelCacheEntry>>());;

    // Tracks repeated rendering for an erroring channel 
    private final Set<String> repeatRenderings = new HashSet<String>();

    // inter-channel communication tables
    private final Map<String, Set<String>> iccTalkers = new HashMap<String, Set<String>>();
    private final Map<String, Set<String>> iccListeners = new HashMap<String, Set<String>>();

    // JNDI Context reference for channels
    private final Context channelContext;

    // Used for channel authorization decisions
    private final IAuthorizationPrincipal authorizationPrincipal;

    // a set of channels requested for rendering, but
    // awaiting rendering set commit due to inter-channel
    // communication
    private final Set<String> pendingChannels = new HashSet<String>();

    private String channelTarget;
    private Map<String, Object> targetParams;
    private UPFileSpec uPElement;
    
    private boolean characterCaching = false;
    private BrowserInfo browserInfo;
    private LocaleManager localeManager;
    private String serializerName;
    private boolean groupedRendering = false;
    

    /**
     * Creates a new <code>ChannelManager</code> instance.
     *
     * @param manager an <code>IUserPreferencesManager</code> value
     */
    public ChannelManager(IUserPreferencesManager manager, HttpSession httpSession) {
        this.userPreferencesManager = manager;
        
        
        final String sessionId = httpSession.getId();
        
        final IPerson person = this.userPreferencesManager.getPerson();
        final String personId = Integer.toString(person.getID());
        
        final UserProfile currentProfile = this.userPreferencesManager.getCurrentProfile();
        final String layoutId = Integer.toString(currentProfile.getLayoutId());

        this.channelContext = this.getChannelJndiContext(sessionId, personId, layoutId);
        
        
        final EntityIdentifier personEntityIdentifier = person.getEntityIdentifier();
        this.authorizationPrincipal = AuthorizationService.instance().newPrincipal(personEntityIdentifier.getKey(), personEntityIdentifier.getType());
    }
    


    /**
     * <code>getChannelContext</code> generates a JNDI context that
     * will be passed to the regular channels. The context is pieced
     * together from the parts of the global portal context.
     *
     * @param portalContext uPortal JNDI context
     * @param sessionId current session id
     * @param userId id of a current user
     * @param layoutId id of the layout used by the user
     * @return a channel <code>InitialContext</code> value
     */
    private Context getChannelJndiContext(String sessionId, String userId, String layoutId) {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final IJndiManager jndiManager = (IJndiManager)applicationContext.getBean("jndiManager", IJndiManager.class);
        final JndiTemplate jndiTemplate = jndiManager.getJndiTemplate();
        
        try {
            // create a new InitialContext
            final Context cic = new MemoryContext(new Hashtable<Object, Object>());
            // get services context
            final Context servicesContext = (Context) jndiTemplate.lookup("services", Context.class);
            // get channel-ids context
            final Context channel_idsContext = (Context) jndiTemplate.lookup("users/" + userId + "/layouts/" + layoutId + "/channel-ids", Context.class);
            // get channel-obj context
            final Context channel_objContext = (Context) jndiTemplate.lookup("users/" + userId + "/sessions/" + sessionId + "/channel-obj", Context.class);
    
            cic.bind("services", servicesContext);
            cic.bind("channel-ids", channel_idsContext);
            cic.bind("channel-obj", channel_objContext);
            cic.bind("portlet-ids", new ArrayList<Object>());
    
            return cic;
        }
        catch (NamingException ne) {
            log.error("Failed to create channel JNDI Context. No JNDI context will be available for rendering channels.", ne);
            return null;
        }
    }
    
    private PortalControlStructures getPortalControlStructuresForChannel(HttpServletRequest request, HttpServletResponse response, String channelSubscribeId) {
        Map<String, PortalControlStructures> existingPortalControlStructures = (Map<String, PortalControlStructures>)request.getAttribute(PORTAL_CONTROL_STRUCTURES_MAP_ATTR);
        if (existingPortalControlStructures == null) {
            existingPortalControlStructures = new HashMap<String, PortalControlStructures>();
            request.setAttribute(PORTAL_CONTROL_STRUCTURES_MAP_ATTR, existingPortalControlStructures);
        }
        
        PortalControlStructures pcs = existingPortalControlStructures.get(channelSubscribeId);
        if (pcs == null) {
            pcs = new PortalControlStructures(request, response, this, this.userPreferencesManager);
            existingPortalControlStructures.put(channelSubscribeId, pcs);
        }
        return pcs;
    }
    
    /**
     * Directly places a channel instance into the hashtable of active channels.
     * This is designed to be used by the error channel only.
     */
    public void setChannelInstance(String channelSubscribeId, IChannel channelInstance) {
        if (channelTable.get(channelSubscribeId) != null) {
            channelTable.remove(channelSubscribeId);
        }
        channelTable.put(channelSubscribeId, channelInstance);
    }

    /**
     * A method to notify <code>ChannelManager</code> that the channel set for
     * the current rendering cycle is complete.
     * Note: This information is used to identify relevant channel communication dependencies
     */
    public void commitToRenderingChannelSet() {
        if(groupedRendering) {
            // separate out the dependency group in s0

            Set<String> s0 = new HashSet<String>();
            Set<String> children;

            if (pendingChannels.contains(channelTarget)) {
                s0.add(channelTarget);
                pendingChannels.remove(channelTarget);
                children = getListeningChannels(channelTarget);
                if (children != null && !children.isEmpty()) {
                    children.retainAll(pendingChannels);
                    while (!children.isEmpty()) {
                        // move to the next generation
                        Set<String> newChildren = new HashSet<String>();
                        for (String childId : children) {
                            s0.add(childId);
                            pendingChannels.remove(childId);
                            Set<String> currentChildren = getListeningChannels(childId);
                            if (currentChildren != null) {
                                newChildren.addAll(currentChildren);
                            }
                        }
                        newChildren.retainAll(pendingChannels);
                        children = newChildren;
                    }
                }
            }

            // now s0 group must be synchronized at renderXML(), while the remaining pendingChildren can be rendered freely
            SetCheckInSemaphore s0semaphore= new SetCheckInSemaphore(new HashSet<String>(s0));
            for(Iterator<String> gi=s0.iterator();gi.hasNext();) {
                String channelSubscribeId=gi.next();
                IChannelRenderer cr=rendererTable.get(channelSubscribeId);
                cr.startRendering(s0semaphore,channelSubscribeId);
            }

            for(Iterator<String> oi=pendingChannels.iterator();oi.hasNext();) {
                String channelSubscribeId=oi.next();
                IChannelRenderer cr=rendererTable.get(channelSubscribeId);
                cr.startRendering();
            }
        }
    }


    /**
     * Clean up after a rendering round.
     */
    public void finishedRenderingCycle() {
        // clean up
        for (final IChannelRenderer channelRenderer : this.rendererTable.values()) {
            try {
                /*
                 * For well behaved, finished channel renderers, killing doesn't do
                 * anything.
                 *
                 * For runaway, not-finished channel renderers, killing instructs them to
                 * stop trying to render because at this point we can't use the
                 * results of their rendering anyway.  Furthermore, the current
                 * actual implementation
                 * of kill is for channel renderers to kill runaway threads.
                 */
                channelRenderer.kill();
            } catch (Throwable t) {
                /*
                 * We're trying to clean up.  A particular thread renderer we've asked
                 * to please die has failed to die in some potentially horrible way.
                 * This is unfortunate, but the best thing we can do about it is log
                 * the problem and then go on and ask the other ChannelRenderers to
                 * clean up.  If this one won't clean up properly, maybe at least some
                 * of the others will clean up.  By catching Throwable and handling
                 * it in this way, we prevent any particular ChannelRenderer's failure
                 * from blocking our asking other ChannelRenderers to clean up.
                 */
                log.error("Error cleaning up runaway channel renderer: [" + channelRenderer + "]", t);
            }

        }
        rendererTable.clear();
        repeatRenderings.clear();
        pendingChannels.clear();
        
        targetParams = null;
        groupedRendering = false;
    }


    /**
     * Handle end-of-session cleanup
     *
     */
    public void finishedSession(HttpSession session) {
        this.finishedRenderingCycle();
        
        final PortalControlStructures pcs = new PortalControlStructures(session, this, this.userPreferencesManager);

        // send SESSION_DONE event to all the channels
        final PortalEvent ev = PortalEvent.SESSION_DONE_EVENT;

        for (final IChannel ch : this.channelTable.values()) {
            if (ch != null) {
                try {
                    if (ch instanceof IPrivilegedChannel) {
                        ((IPrivilegedChannel) ch).setPortalControlStructures(pcs);
                    }

                    ch.receiveEvent(ev);
                }
                catch (Exception e) {
                    log.error("Error sending session done event to channel " + ch, e);
                }
            }
        }

        //Cleanup tables just to be nice
        channelTable.clear();
        channelCacheTable.clear();
        iccTalkers.clear();
        iccListeners.clear();
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
     * @param contentHandler a <code>ContentHandler</code> value
     */
    public void outputChannel(HttpServletRequest request, HttpServletResponse response, String channelSubscribeId, ContentHandler contentHandler) {
        // Set the subscribeId as the achorId for an anchoring serializer
        if (useAnchors && contentHandler instanceof IAnchoringSerializer) {
            IAnchoringSerializer as = (IAnchoringSerializer)contentHandler;
            as.startAnchoring(channelSubscribeId);
        }

        // obtain IChannelRenderer
        IChannelRenderer cr = rendererTable.get(channelSubscribeId);
        if (cr == null) {
            // channel rendering wasn't started ?
            try {
                cr = startChannelRendering(request, response, channelSubscribeId);
            }
            catch (PortalException pe) {
                // record, and go on
                log.error("Encountered a portal exception while trying to start channel rendering! :", pe);
            }
        }

        // complete rendering and check status
        int renderingStatus=-1;
        try {
            renderingStatus=cr.completeRendering();
        } catch (Throwable t) {
            handleRenderingError(request, response, channelSubscribeId,contentHandler,t,renderingStatus,"encountered problem while trying to complete rendering","IChannelRenderer.completeRendering() threw",false);
            return;
        }

        if(renderingStatus==IChannelRenderer.RENDERING_SUCCESSFUL) {
            // obtain content
            if(contentHandler instanceof CachingSerializer && this.characterCaching) {
                CachingSerializer cs=(CachingSerializer) contentHandler;
                // need to get characters
                String characterContent=cr.getCharacters();
                if(characterContent==null) {
                    // obtain a SAX Buffer content then
                    SAX2BufferImpl bufferedContent=cr.getBuffer();
                    if(bufferedContent!=null) {
                        // translate SAX Buffer into the character version
                        try {
                            if(!cs.startCaching()) {
                                log.error("unable to restart character cache while compiling character cache for channel '"+channelSubscribeId+"' !");
                            }
                            // dump SAX buffer into the serializer
                            bufferedContent.outputBuffer(contentHandler);
                            // extract compiled character cache
                            if(cs.stopCaching()) {
                                try {
                                    characterContent=cs.getCache();
                                	log.debug("outputChannel 2: "+characterContent);
                                    if(characterContent!=null) {
                                        // save compiled character cache
                                        cr.setCharacterCache(characterContent);
                                    } else {
                                        log.error("character caching serializer returned NULL character cache for channel '"+channelSubscribeId+"' !");
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    log.error("unable to compile character cache for channel '"+channelSubscribeId+"'! Invalid encoding specified.",e);
                                } catch (IOException ioe) {
                                    log.error("IO exception occurred while compiling character cache for channel '"+channelSubscribeId+"' !",ioe);
                                }
                            } else {
                                log.error("unable to reset cache state while compiling character cache for channel '"+channelSubscribeId+"' ! Serializer was not caching when it should've been ! Partial output possible!");
                                return;
                            }
                        } catch (IOException ioe) {
                            handleRenderingError(request, response, channelSubscribeId,contentHandler,ioe,renderingStatus,"encountered a problem compiling channel character content","Encountered IO exception while trying to output channel content SAX to the character caching serializer",true);
                            return;
                        } catch (org.xml.sax.SAXException se) {
                            handleRenderingError(request, response, channelSubscribeId,contentHandler,se,renderingStatus,"encountered a problem compiling channel character content","Encountered SAX exception while trying to output channel content SAX to the character caching serializer",true);
                            return;
                        }

                    } else {
                        handleRenderingError(request, response, channelSubscribeId,contentHandler,null,renderingStatus,"unable to obtain channel rendering","IChannelRenderer.getBuffer() returned null",false);
                        return;
                    }
                } else { // non-null characterContent case
                    // output character content
                    try {
                        cs.printRawCharacters(characterContent);
                    } catch (IOException ioe) {
                        if (log.isDebugEnabled())
                            log.debug("" +
                                    "exception thrown while trying to output character " +
                                    "cache for channelSubscribeId=" +
                                    "'"+channelSubscribeId + "'", ioe);
                    }
                }
            } else { // regular serializer case
                // need to output straight
                SAX2BufferImpl bufferedContent=cr.getBuffer();
                if(bufferedContent!=null) {
                    try {
                        // output to the serializer
                        ChannelSAXStreamFilter custodian = new ChannelSAXStreamFilter(contentHandler);
                        bufferedContent.outputBuffer(custodian);
                    } catch (Exception e) {
                        log.error("encountered an exception while trying to output SAX2 content of channel '"+channelSubscribeId+"' to a regular serializer. Partial output possible !",e);
                        return;
                    }
                } else {
                    handleRenderingError(request, response, channelSubscribeId,contentHandler,null,renderingStatus,"unable to obtain channel rendering","IChannelRenderer.getBuffer() returned null",false);
                    return;
                }
            }

            // Reset the anchorId for an anchoring serializer
            if (useAnchors && contentHandler instanceof IAnchoringSerializer) {
                IAnchoringSerializer as = (IAnchoringSerializer)contentHandler;
                as.stopAnchoring();
            }

            // Obtain the channel description
            IUserLayoutChannelDescription channelDesc = null;
            try {
              channelDesc = (IUserLayoutChannelDescription)userPreferencesManager.getUserLayoutManager().getNode(channelSubscribeId);
            } catch (PortalException pe) {
                // Just log exception
            	log.warn(pe,pe);
            }

            // Tell the StatsRecorder that this channel has rendered
            EventPublisherLocator.getApplicationEventPublisher().publishEvent(new ChannelRenderedInLayoutPortalEvent(this, userPreferencesManager.getPerson(), userPreferencesManager.getCurrentProfile(), channelDesc));
        } else {
            handleRenderingError(request, response, channelSubscribeId,contentHandler,null,renderingStatus,"unsuccessful rendering","unsuccessful rendering",false);
            return;
        }
    }

    /**
     * Handles rendering output errors by replacing a channel instance with that of an error channel.
     * (or giving up if the error channel is failing as well)
     *
     * @param channelSubscribeId a <code>String</code> value
     * @param contentHandler a <code>ContentHandler</code> value
     * @param t a <code>Throwable</code> value
     * @param renderingStatus an <code>int</code> value
     * @param commonMessage a <code>String</code> value
     * @param technicalMessage a <code>String</code> value
     * @param partialOutput a <code>boolean</code> value
     */
    private void handleRenderingError(HttpServletRequest request, HttpServletResponse response, String channelSubscribeId,ContentHandler contentHandler, Throwable t, int renderingStatus, String commonMessage, String technicalMessage,boolean partialOutput) {
        try {
            if (repeatRenderings.contains(channelSubscribeId)) {
                // this means that the error channel has failed :(
                String message="Unable to handle a rendering error through error channel.";
                if(t!=null) {
                    if(t instanceof InternalPortalException) {
                        InternalPortalException ipe=(InternalPortalException) t;
                        Throwable e=ipe.getCause();
                        message=message+" Error channel (channelSubscribeId='"+channelSubscribeId+"') has thrown the following exception: "+e.toString()+" Partial output possible !";
                        log.fatal("CError threw exception. Please fix CError immediately!", e);
                    } else {
                        message=message+" An following exception encountered while trying to render the error channel for channelSubscribeId='"+channelSubscribeId+"': "+t.toString();
                        log.fatal("CError threw exception. Please fix CError immediately!", t);
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
                log.error(message);
            } else {
                // first check for an exception
                if(t!=null ){
                    if(t instanceof InternalPortalException) {
                        InternalPortalException ipe=(InternalPortalException) t;
                        Throwable channelException=ipe.getCause();
                        replaceWithErrorChannel(request, response, channelSubscribeId, ErrorCode.RENDER_TIME_EXCEPTION,channelException,technicalMessage,true);
                    } else {
                        replaceWithErrorChannel(request, response, channelSubscribeId, ErrorCode.RENDER_TIME_EXCEPTION, t, technicalMessage, true);
                    }
                } else {
                    if(renderingStatus==IChannelRenderer.RENDERING_TIMED_OUT) {
                        replaceWithErrorChannel(request, response, channelSubscribeId,ErrorCode.TIMEOUT_EXCEPTION,t,technicalMessage,true);
                    } else {
                        replaceWithErrorChannel(request, response, channelSubscribeId,ErrorCode.GENERAL_ERROR,t,technicalMessage,true);
                    }
                }

                // remove channel renderer
                rendererTable.remove(channelSubscribeId);
                // re-try render
                if(!partialOutput) {
                    repeatRenderings.add(channelSubscribeId);
                    outputChannel(request, response, channelSubscribeId, contentHandler);
                }
            }
        } finally {
            // Set the subscribeId as the achorId for an anchoring serializer
            if (useAnchors && contentHandler instanceof IAnchoringSerializer) {
                IAnchoringSerializer as = (IAnchoringSerializer)contentHandler;
                as.stopAnchoring();
            }
        }
    }

    /**
     * A helper method to replace all occurences of a given channel instance
     * with that of an error channel.
     *
     * @param channelSubscribeId a <code>String</code> value
     * @param errorCode an ErrorCode
     * @param t a <code>Throwable</code> an exception that caused the problem
     * @param message a <code>String</code> an optional message to pass to the error channel
     * @param setRuntimeData a <code>boolean</code> wether the method should also set the ChannelRuntimeData for the newly instantiated error channel
     * @return an <code>IChannel</code> value of an error channel instance
     */
    private IChannel replaceWithErrorChannel(HttpServletRequest request, HttpServletResponse response, String channelSubscribeId, ErrorCode errorCode, Throwable t, String message, boolean setRuntimeData) {
        // get and delete old channel instance
        IChannel oldInstance=channelTable.get(channelSubscribeId);
        if (log.isWarnEnabled())
            log.warn("Replacing channel [" + oldInstance
                + "], which had subscribeId [" + channelSubscribeId
                + "] with error channel because of error code "
                + errorCode + " message: " + message + " and throwable [" + t +"]",t);

        channelTable.remove(channelSubscribeId);
        rendererTable.remove(channelSubscribeId);

        CError errorChannel =
            new CError(errorCode,t,channelSubscribeId,oldInstance,message);
        if(setRuntimeData) {
            ChannelRuntimeData rd=new ChannelRuntimeData();
            rd.setBrowserInfo(browserInfo);
            if (localeManager != null)  {
                rd.setLocales(localeManager.getLocales());
            }
            
            final PortalControlStructures pcs = this.getPortalControlStructuresForChannel(request, response, channelSubscribeId);
            
			rd.setRemoteAddress(pcs.getHttpServletRequest().getRemoteAddr());
            rd.setHttpRequestMethod(pcs.getHttpServletRequest().getMethod());
            UPFileSpec up=new UPFileSpec(uPElement);
            up.setTargetNodeId(channelSubscribeId);
            rd.setUPFile(up);
            try {
                errorChannel.setRuntimeData(rd);
                errorChannel.setPortalControlStructures(pcs);
            } catch (Throwable e) {

                log.error("Encountered an exception while trying to set runtime data or portal control structures on the error channel!", e);
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
    private IChannel replaceWithSecureInfoChannel(HttpServletRequest request, HttpServletResponse response, String channelSubscribeId, boolean setRuntimeData) {
        // get and delete old channel instance
        IChannel oldInstance=channelTable.get(channelSubscribeId);
        channelTable.remove(channelSubscribeId);
        rendererTable.remove(channelSubscribeId);

        CSecureInfo secureInfoChannel=new CSecureInfo(channelSubscribeId,oldInstance);
        if(setRuntimeData) {
            ChannelRuntimeData rd=new ChannelRuntimeData();
            rd.setBrowserInfo(browserInfo);
            if (localeManager != null)  {
                rd.setLocales(localeManager.getLocales());
            }
            
            final PortalControlStructures pcs = this.getPortalControlStructuresForChannel(request, response, channelSubscribeId);
            
            rd.setHttpRequestMethod(pcs.getHttpServletRequest().getMethod());
            rd.setRemoteAddress(pcs.getHttpServletRequest().getRemoteAddr());
            UPFileSpec up=new UPFileSpec(uPElement);
            up.setTargetNodeId(channelSubscribeId);
            rd.setUPFile(up);
            try {
                secureInfoChannel.setRuntimeData(rd);
                secureInfoChannel.setPortalControlStructures(pcs);
            } catch (Throwable e) {
                log.error("Encountered an exception while trying to set runtime data or portal control structures on the secure info channel!", e);
            }
        }
        channelTable.put(channelSubscribeId,secureInfoChannel);
        return secureInfoChannel;
    }


    /**
     * Instantiates a channel given just the channel subscribe Id.
     *
     * @param channelSubscribeId a channel instance Id in the userLayout
     * @return an <code>IChannel</code> object
     */
    public IChannel instantiateChannel(HttpServletRequest request, HttpServletResponse response, String channelSubscribeId) throws PortalException {
        if (channelTable.get(channelSubscribeId) != null) {
            // reinstantiation
            channelTable.remove(channelSubscribeId);
        }
        
        // get channel information from the user layout manager
        final IUserLayoutManager userLayoutManager = userPreferencesManager.getUserLayoutManager();
        final IUserLayoutChannelDescription channel = (IUserLayoutChannelDescription) userLayoutManager.getNode(channelSubscribeId);
        if (channel != null) {
            return this.instantiateChannel(request, response, channel);
        }

        return null;
    }

    private IChannel instantiateChannel(HttpServletRequest request, HttpServletResponse response, IUserLayoutChannelDescription channelDescription) throws PortalException {
        final String channelSubscribeId = channelDescription.getChannelSubscribeId();
        final String channelPublishId = channelDescription.getChannelPublishId();
        // check if the user has permissions to instantiate this channel

        final IChannel channel;
        if(authorizationPrincipal.canRender(Integer.parseInt(channelPublishId))) {
			if (request == null) {
                channel = new CError(ErrorCode.GENERAL_ERROR, "Unable to get SessionId. No HttpServletRequest provided.", channelSubscribeId, null);
            }
            else {
                final HttpSession session = request.getSession();
                final String sessionId = session.getId();
                channel = ChannelFactory.instantiateLayoutChannel(channelDescription, sessionId);

                if (channel == null) {
                    throw new IllegalStateException("ChannelFactory returned null on request to instantiate layout channel with id [" + sessionId + "] and description [" + channelDescription + "]");
                }

                final IPerson person = userPreferencesManager.getPerson();
                
                final ApplicationEventPublisher applicationEventPublisher = EventPublisherLocator.getApplicationEventPublisher();
                applicationEventPublisher.publishEvent(new ChannelInstanciatedInLayoutPortalEvent(this, person, userPreferencesManager.getCurrentProfile(), channelDescription));

                // Create and stuff the channel static data
                final ChannelStaticData channelStaticData = new ChannelStaticData(channelDescription.getParameterMap(), userPreferencesManager.getUserLayoutManager());
                channelStaticData.setChannelSubscribeId(channelSubscribeId);
                channelStaticData.setTimeout(channelDescription.getTimeout());
                channelStaticData.setPerson(person);
                channelStaticData.setJNDIContext(channelContext);
                channelStaticData.setICCRegistry(new ICCRegistry(this, channelSubscribeId));
                channelStaticData.setChannelPublishId(channelDescription.getChannelPublishId());
                channelStaticData.setSerializerName(serializerName);
                channelStaticData.setWebApplicationContext(PortalApplicationContextLocator.getRequiredWebApplicationContext());

                if (channel instanceof IPrivilegedChannel) {
                    this.feedPortalControlStructuresToChannel(request, response, channelSubscribeId, (IPrivilegedChannel)channel);
                }

                channel.setStaticData(channelStaticData);
            }
        }
        else {
            // user is not authorized to instantiate this channel
            // create an instance of an error channel instead
            channel = new CError(ErrorCode.CHANNEL_AUTHORIZATION_EXCEPTION, "You don't have authorization to render this channel.", channelSubscribeId, null);
        }

        this.channelTable.put(channelSubscribeId, channel);
        return channel;
    }


    /**
     * Passes a layout-level event to a channel.
     * @param channelSubscribeId the channel subscribe id
     * @param le the portal event
     */
    public void passPortalEvent(HttpServletRequest request, HttpServletResponse response, String channelSubscribeId, PortalEvent le) {
        IChannel ch = channelTable.get(channelSubscribeId);
        if (ch != null) {
            try {
                if (ch instanceof IPrivilegedChannel) {
                    final PortalControlStructures pcs = this.getPortalControlStructuresForChannel(request, response, channelSubscribeId);
                    ((IPrivilegedChannel)ch).setPortalControlStructures(pcs);
                }
                
                ch.receiveEvent(le);
            }
            catch (Exception e) {
                log.error("Error sending layout event " + le + " to channel " + ch, e);
            }
        }
        else {
            log.error("trying to pass an event to a channel that is not in cache. channelSubscribeId='" + channelSubscribeId + "'");
        }
    }


    /**
     * Determine target channel and pass corresponding
     * actions/params to that channel
     * @param request the <code>HttpServletRequest</code>
     */
    private void processRequestChannelParameters(HttpServletRequest request, HttpServletResponse response) {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final IChannelRequestParameterManager channelParameterManager = (IChannelRequestParameterManager)applicationContext.getBean("channelRequestParameterManager", IChannelRequestParameterManager.class);
        
        final Set<String> targetedChannelIds = channelParameterManager.getTargetedChannelIds(request);
        if (targetedChannelIds.size() > 0) {
            this.channelTarget = targetedChannelIds.iterator().next();
        }
        else {
            this.channelTarget = null;
        }

        if (channelTarget != null) {
            // Obtain the channel description
            IUserLayoutChannelDescription channelDesc = null;
            try {
              channelDesc = (IUserLayoutChannelDescription)userPreferencesManager.getUserLayoutManager().getNode(channelTarget);
            } catch (PortalException pe) {
              // Do nothing
            }

            // Tell StatsRecorder that a user has interacted with the channel
            final ApplicationEventPublisher applicationEventPublisher = EventPublisherLocator.getApplicationEventPublisher();
            applicationEventPublisher.publishEvent(new ChannelTargetedInLayoutPortalEvent(this, userPreferencesManager.getPerson(), userPreferencesManager.getCurrentProfile(), channelDesc));

            
            final Map<String, Object[]> channelParameters = channelParameterManager.getChannelParameters(request, channelTarget);
            if (channelParameters != null) {
                targetParams = new HashMap<String, Object>(channelParameters);
            }
            else {
                targetParams = null;
            }
            
            if(channelParameters != null && channelParameters.size() > 0) {
                // only do grouped rendering if there are some parameters passed
                // to the target channel.
                // detect if channel target talks to other channels
                groupedRendering = hasListeningChannels(channelTarget);
            }

            IChannel channel;
            if ((channel=channelTable.get(channelTarget)) == null) {
                try {
                    channel=instantiateChannel(request, response, channelTarget);
                } catch (Throwable e) {
					if (userPreferencesManager.getPerson().isGuest() == true)
					{
						// We get this alot when people's sessions have timed out and they get directed
						// to the guest page. Changed to WARN because there might be a need to note this
						// to diagnose problems with the guest layout.

						log.warn("unable to pass find/create an instance of a channel. Bogus Id ? ! (id='"+channelTarget+"').");
					}else{
						log.error("unable to pass find/create an instance of a channel. Bogus Id ? ! (id='"+channelTarget+"' uid='"+userPreferencesManager.getPerson().getID()+"').",e);
					}
                    channel=replaceWithErrorChannel(request, response, channelTarget, ErrorCode.SET_STATIC_DATA_EXCEPTION, e, null, false);
                }
            }
        }
    }

    private IChannel feedPortalControlStructuresToChannel(HttpServletRequest request, HttpServletResponse response, String channelSubscribeId, IPrivilegedChannel prvChanObj) {
        final PortalControlStructures pcs = this.getPortalControlStructuresForChannel(request, response, channelSubscribeId);
        
        try {
            prvChanObj.setPortalControlStructures(pcs);
        }
        catch (Exception e) {
            prvChanObj = (IPrivilegedChannel)replaceWithErrorChannel(request, response, channelSubscribeId, ErrorCode.SET_PCS_EXCEPTION, e, null, false);

            // set portal control structures
            try {
                prvChanObj.setPortalControlStructures(pcs);
            }
            catch (Exception e2) {
                // things are looking bad for our hero
                log.error("Error channel threw exception accepting PortalControlStructures", e2);
            }
        }
        
        return prvChanObj;
    }

    /**
     * Obtain an instance of a channel.
     *
     * @param channelSubscribeId a <code>String</code> value
     * @return an <code>IChannel</code> object
     */
    public IChannel getChannelInstance(HttpServletRequest request, HttpServletResponse response, String channelSubscribeId) {
        IChannel ch = channelTable.get(channelSubscribeId);
        if (ch == null) {
            try {
                ch = instantiateChannel(request, response, channelSubscribeId);
            }
            catch (Throwable e) {
                log.warn(e, e);
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
        IChannel ch=channelTable.get(channelSubscribeId);
        if(ch!=null) {
            channelCacheTable.remove(ch);
            try {
                ch.receiveEvent(PortalEvent.UNSUBSCRIBE_EVENT);
            } catch (Exception e) {
                log.error(e, e);
            }
            channelTable.remove(ch);
            if (log.isDebugEnabled())
                log.debug("removed channel with subscribe id="+channelSubscribeId);
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
        this.browserInfo = new BrowserInfo(request);
        this.uPElement = uPElement;
        this.rendererTable.clear();

        processRequestChannelParameters(request, response);
    }

    /**
     * Specifies if this particular rendering cycle is using
     * character caching.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isCharacterCaching() {
        return this.characterCaching;
    }

    /**
     * Specify that the current rendering cycle should be
     * using (or not) character caching.
     *
     * @param setting a <code>boolean</code> value
     */
    public void setCharacterCaching(boolean setting) {
        this.characterCaching=setting;
    }

    /**
     * Specify <code>UPFileSpec</code> object that will be
     * used to construct file portion of the context path
     * in the auto-generated URLs, also known as the baseActionURL.
     *
     * @param uPElement an <code>UPFileSpec</code> value
     */
    public void setUPElement(UPFileSpec uPElement) {
        this.uPElement = uPElement;
    }

    public boolean doChannelAction(HttpServletRequest request, HttpServletResponse response, String channelSubscribeId, boolean noTimeout) throws PortalException {
        // see if the channel has already been instantiated
        // see if the channel is cached
        IUserLayoutNodeDescription node = userPreferencesManager.getUserLayoutManager().getNode(channelSubscribeId);
        if (!(node instanceof IUserLayoutChannelDescription)) {
            throw new PortalException("'" + channelSubscribeId + "' is not a channel node !");
        }

        final IUserLayoutChannelDescription channel = (IUserLayoutChannelDescription) node;
        final long timeOut = channel.getTimeout();

        IChannel ch = channelTable.get(channelSubscribeId);
        final IChannel originalChannel = ch;

        if (ch == null || ch instanceof CSecureInfo) {
            try {
                ch = instantiateChannel(request, response, channel);
            }
            catch (Throwable e) {
                ch = replaceWithErrorChannel(request, response, channelSubscribeId, ErrorCode.SET_STATIC_DATA_EXCEPTION, e, null, false);
            }
        }
        
        if (ch instanceof IPrivilegedChannel) {
            ch = this.feedPortalControlStructuresToChannel(request, response, channelSubscribeId, (IPrivilegedChannel) ch);
        }
        
        //If the channel object has changed (likely now an error channel) return immediatly 
        if (originalChannel != ch) {
            return false;
        }
        
        final ChannelRuntimeData runtimeData = this.getChannelRuntimeData(request, channelSubscribeId, RequestType.ACTION);

        // Build a new channel renderer instance.
        final IChannelRenderer channelRenderer = cChannelRendererFactory.newInstance(ch, runtimeData);

        if (noTimeout) {
            channelRenderer.setTimeout(0);
        }
        else {
            channelRenderer.setTimeout(timeOut);
        }

        channelRenderer.startRendering();
        
        final int renderingStatus;
        try {
            renderingStatus = channelRenderer.completeRendering();
        }
        catch (Throwable t) {
            ch = replaceWithErrorChannel(request, response, channelSubscribeId, ErrorCode.RENDER_TIME_EXCEPTION, t, null, false);
            log.error("Failed to complete action", t);
            return false;
        }

        if (renderingStatus != IChannelRenderer.RENDERING_SUCCESSFUL) {
            final ErrorCode errorCode;
            if (renderingStatus == IChannelRenderer.RENDERING_TIMED_OUT) {
                errorCode = ErrorCode.TIMEOUT_EXCEPTION;
            }
            else {
                errorCode = ErrorCode.GENERAL_ERROR;
            }
            
            ch = replaceWithErrorChannel(request, response, channelSubscribeId, errorCode, null, "unsuccessful rendering", true);
            log.error("Action did not compplete successefully: " + renderingStatus);
            return false;
        }
        
        return true;
    }
        
    /**
     * Initiate channel rendering cycle.
     *
     * @param channelSubscribeId a <code>String</code> value
     * @return a <code>IChannelRenderer</code> value
     * @exception PortalException if an error occurs
     */
    public IChannelRenderer startChannelRendering(HttpServletRequest request, HttpServletResponse response, String channelSubscribeId) throws PortalException {
        return startChannelRendering(request, response, channelSubscribeId, false);
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
    private IChannelRenderer startChannelRendering(HttpServletRequest request, HttpServletResponse response, String channelSubscribeId, boolean noTimeout) throws PortalException {
        // see if the channel has already been instantiated
        // see if the channel is cached
        IChannel ch;
        long timeOut=0;

        IUserLayoutNodeDescription node=userPreferencesManager.getUserLayoutManager().getNode(channelSubscribeId);
        if(!(node instanceof IUserLayoutChannelDescription)) {
            throw new PortalException("'"+channelSubscribeId+"' is not a channel node !");
        }

        IUserLayoutChannelDescription channel=(IUserLayoutChannelDescription) node;
        timeOut=channel.getTimeout();

        ch = channelTable.get(channelSubscribeId);

        // replace channels that are specified as needing to be
        // rendered securely with CSecureInfo.
        if (!request.isSecure() && channel.isSecure()){
            if (!(ch instanceof CSecureInfo)){
                ch = replaceWithSecureInfoChannel(request, response, channelSubscribeId, false);
            }
        }
        else{
            // A secure channel may not have been able to render at one
            // time but now it can, create its instance to replace the
            // cached CSecureInfo entry.
            if (ch == null || ch instanceof CSecureInfo) {
                try {
                    ch = instantiateChannel(request, response, channel);
                }
                catch (Throwable e) {
                    ch = replaceWithErrorChannel(request, response, channelSubscribeId, ErrorCode.SET_STATIC_DATA_EXCEPTION, e, null, false);
                }
            }
        }

        if (ch instanceof IPrivilegedChannel) {
            this.feedPortalControlStructuresToChannel(request, response, channelSubscribeId, (IPrivilegedChannel) ch);
        }
        
        final ChannelRuntimeData runtimeData;
        if(!channelSubscribeId.equals(channelTarget)) {
            runtimeData = this.getChannelRuntimeData(request, channelSubscribeId, RequestType.RENDER);
        }
        else {
            // set up runtime data that will be passed to the IChannelRenderer
            runtimeData = this.getChannelRuntimeData(request, this.channelTarget, RequestType.RENDER);
        }

        // Build a new channel renderer instance.
        final IChannelRenderer channelRenderer = cChannelRendererFactory.newInstance(ch, runtimeData);

        channelRenderer.setCharacterCacheable(this.characterCaching);
        if(ch instanceof ICacheable) {
            channelRenderer.setCacheTables(this.channelCacheTable);
        }

        if (noTimeout) {
            channelRenderer.setTimeout(0);
        }
        else {
            channelRenderer.setTimeout(timeOut);
        }

        if (groupedRendering && (isListeningToChannels(channelSubscribeId) || channelSubscribeId.equals(channelTarget))) {
            // channel might depend on the target channel
            pendingChannels.add(channelSubscribeId); // defer rendering start
        }
        else {
            channelRenderer.startRendering();
        }
        rendererTable.put(channelSubscribeId, channelRenderer);

        return channelRenderer;
    }

    /**
     * Builds ChannelRuntimeData from a request, sets the parameters, query string
     * browser info, locale manager, method, remote address, and UPFileSpec
     */
    private ChannelRuntimeData getChannelRuntimeData(HttpServletRequest request, String channelSubscribeId, RequestType requestType) {
        final ChannelRuntimeData runtimeData = new ChannelRuntimeData();
        
        if (channelSubscribeId.equals(this.channelTarget)) {
            if (this.targetParams != null) {
                runtimeData.setParameters(this.targetParams);
            }
            
            final String queryString = request.getQueryString();
            if (queryString != null && queryString.indexOf("=") == -1) {
                runtimeData.setKeywords(queryString);
            }
            
            runtimeData.setTargeted(true);
        }
        
        runtimeData.setBrowserInfo(this.browserInfo);
        
        if (this.localeManager != null)  {
            runtimeData.setLocales(this.localeManager.getLocales());
        }
        runtimeData.setHttpRequestMethod(request.getMethod());
        runtimeData.setRemoteAddress(request.getRemoteAddr());

        final UPFileSpec upFile = new UPFileSpec(this.uPElement);
        upFile.setTargetNodeId(channelSubscribeId);
        runtimeData.setUPFile(upFile);
        
        final UserPreferences userPreferences = this.userPreferencesManager.getUserPreferences();
        final StructureStylesheetUserPreferences structureStylesheetUserPreferences = userPreferences.getStructureStylesheetUserPreferences();
        final String userLayoutRoot = structureStylesheetUserPreferences.getParameterValue("userLayoutRoot");
        if (!IUserLayout.ROOT_NODE_NAME.equals(userLayoutRoot)) {
            runtimeData.setRenderingAsRoot(true);
        }
        
        runtimeData.setRequestType(requestType);
        
        return runtimeData;
    }

    synchronized void registerChannelDependency(String listenerChannelSubscribeId, String talkerChannelSubscribeId) {
        Set<String> talkers=iccListeners.get(listenerChannelSubscribeId);
        if(talkers==null) {
            talkers=new HashSet<String>();
            iccListeners.put(listenerChannelSubscribeId,talkers);
        }
        talkers.add(talkerChannelSubscribeId);

        Set<String> listeners=iccTalkers.get(talkerChannelSubscribeId);
        if(listeners==null) {
            listeners=new HashSet<String>();
            iccTalkers.put(talkerChannelSubscribeId,listeners);
        }
        listeners.add(listenerChannelSubscribeId);
    }


    private Set<String> getListeningChannels(String talkerChannelSubscribeId) {
        return iccTalkers.get(talkerChannelSubscribeId);
    }

    private boolean isListeningToChannels(String listenerChannelSubscribeId) {
        return (iccListeners.get(listenerChannelSubscribeId)!=null);
    }

    private boolean hasListeningChannels(String talkerChannelSubscribeId) {
        return (iccTalkers.get(talkerChannelSubscribeId)!=null);
    }

    synchronized void removeChannelDependency(String listenerChannelSubscribeId, String talkerChannelSubscribeId) {
        Set<String> talkers=iccListeners.get(listenerChannelSubscribeId);
        if(talkers!=null) {
            talkers.remove(talkerChannelSubscribeId);
            if(talkers.isEmpty()) {
                iccListeners.remove(listenerChannelSubscribeId);
            }
        }

        Set<String> listeners=iccTalkers.get(talkerChannelSubscribeId);
        if(listeners!=null) {
            listeners.remove(listenerChannelSubscribeId);
            if(listeners.isEmpty()) {
                iccTalkers.remove(talkerChannelSubscribeId);
            }
        }
    }

    public String getChannelTarget() {
        return channelTarget;
    }

    // LayoutEventListener interface implementation
    public void channelAdded(LayoutEvent ev) {}
    public void channelUpdated(LayoutEvent ev) {}
    public void channelMoved(LayoutMoveEvent ev) {}
    public void channelDeleted(LayoutMoveEvent ev) {
        final IUserLayoutNodeDescription nodeDescription = ev.getNodeDescription();
        this.removeChannel(nodeDescription.getId());
    }

    public void folderAdded(LayoutEvent ev) {}
    public void folderUpdated(LayoutEvent ev) {}
    public void folderMoved(LayoutMoveEvent ev) {}
    public void folderDeleted(LayoutMoveEvent ev) {}

    public void layoutLoaded() {}
    public void layoutSaved() {}

    public void setLocaleManager(LocaleManager lm) {
        this.localeManager = lm;
    }

	/**
	 * Get the dynamic channel title for a given channelSubscribeID.
	 * Returns null if no dynamic channel (the rendering infrastructure
	 * calling this method should fall back on a default title when this
	 * method returns null).
	 * @since uPortal 2.5.1
	 * @param channelSubscribeId
	 * @throws IllegalArgumentException if channelSubcribeId is null
	 * @throws IllegalStateException if
	 */
	public String getChannelTitle(String channelSubscribeId) {

		if (log.isTraceEnabled()) {
			log.trace("ChannelManager getting dynamic title for channel with subscribe id=" + channelSubscribeId);
		}

		// obtain IChannelRenderer
		IChannelRenderer channelRenderer = rendererTable.get(channelSubscribeId);

        // default to null (no dynamic channel title.
        String channelTitle = null;

        // dynamic channel title support is not in IChannelRenderer itself because
        // that would have required a change to the IChannelRenderer interface
        if (channelRenderer instanceof IDynamicChannelTitleRenderer ) {

            final IDynamicChannelTitleRenderer channelTitleRenderer = (IDynamicChannelTitleRenderer) channelRenderer;
            channelTitle = channelTitleRenderer.getChannelTitle();

            if (log.isTraceEnabled()) {
            	log.trace("Dynamic title for channel with subscribe id=" + channelSubscribeId + " is [" + channelTitle + "].");
            }
        }

        return channelTitle;

	}

    public String getSubscribeId(String fname) throws PortalException
    {
        IUserLayoutManager ulm = userPreferencesManager.getUserLayoutManager();
        return ulm.getSubscribeId(fname);
    }
    
    /**
     * Sets the serializer name.
     * @return serializerName
     */
    public String getSerializerName() {
        return serializerName;
    }
    
    /**
     * Setter method for the serializer name.
     * @param serializerName
     */
    public void setSerializerName(String serializerName) {
        this.serializerName = serializerName;
    }
}
