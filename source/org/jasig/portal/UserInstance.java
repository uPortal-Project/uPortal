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


package  org.jasig.portal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.jasig.portal.car.CarResources;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.IALFolderDescription;
import org.jasig.portal.layout.IUserLayoutChannelDescription;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutNodeDescription;
import org.jasig.portal.layout.IAggregatedUserLayoutManager;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.serialize.CachingSerializer;
import org.jasig.portal.serialize.OutputFormat;
import org.jasig.portal.serialize.XMLSerializer;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.StatsRecorder;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.SAX2DuplicatingFilterImpl;
import org.jasig.portal.utils.SoftHashMap;
import org.jasig.portal.utils.URLUtil;
import org.jasig.portal.utils.XSLT;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;


/**
 * A class handling holding all user state information. The class is also reponsible for
 * request processing and orchestrating the entire rendering procedure.
 * (this is a replacement for the good old LayoutBean class)
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 */
public class UserInstance implements HttpSessionBindingListener {
    public static final int guestUserId = 1;

    // To debug structure and/or theme transformations, set these to true
    // and the XML fed to those transformations will be printed to the log.
    private static final boolean logXMLBeforeStructureTransformation = PropertiesManager.getPropertyAsBoolean(UserInstance.class.getName() + ".log_xml_before_structure_transformation");
    private static final boolean logXMLBeforeThemeTransformation = PropertiesManager.getPropertyAsBoolean(UserInstance.class.getName() + ".log_xml_before_theme_transformation");;

    // manages layout and preferences
    private IUserPreferencesManager uPreferencesManager;
    // manages channel instances and channel rendering
    private ChannelManager channelManager;
    // manages locale
    private LocaleManager localeManager;

    // contains information relating client names to media and mime types
    private static MediaManager mediaM;

    // system profile mapper standalone instance
    private StandaloneChannelRenderer p_browserMapper = null;

    // lock preventing concurrent rendering
    private Object p_rendering_lock;

    // global rendering cache
    public static final boolean CACHE_ENABLED=PropertiesManager.getPropertyAsBoolean("org.jasig.portal.UserInstance.cache_enabled");
    private static final int SYSTEM_XSLT_CACHE_MIN_SIZE=PropertiesManager.getPropertyAsInt("org.jasig.portal.UserInstance.system_xslt_cache_min_size");
    private static final int SYSTEM_CHARACTER_BLOCK_CACHE_MIN_SIZE=PropertiesManager.getPropertyAsInt("org.jasig.portal.UserInstance.system_character_block_cache_min_size");
    public static final boolean CHARACTER_CACHE_ENABLED=PropertiesManager.getPropertyAsBoolean("org.jasig.portal.UserInstance.character_cache_enabled");

    // a string that will be used to designate user layout root node in .uP files
    public static final String USER_LAYOUT_ROOT_NODE=IALFolderDescription.ROOT_FOLDER_ID;

    private static final String WORKER_PROPERTIES_FILE_NAME = "/properties/worker.properties";
    private static Properties workerProperties;

    // string that defines which character set to use for content
    private static final String CHARACTER_SET = "UTF-8";

    final SoftHashMap systemCache=new SoftHashMap(SYSTEM_XSLT_CACHE_MIN_SIZE);
    final SoftHashMap systemCharacterCache=new SoftHashMap(SYSTEM_CHARACTER_BLOCK_CACHE_MIN_SIZE);

    protected IPerson person;

    private IUserLayoutNodeDescription newNodeDescription = null;

    public UserInstance (IPerson person) {
        this.person=person;

        // init the media manager
        if(mediaM==null) {
            String mediaPropsUrl = this.getClass().getResource("/properties/media.properties").toString();
            String mimePropsUrl = this.getClass().getResource("/properties/mime.properties").toString();
            String serializerPropsUrl = this.getClass().getResource("/properties/serializer.properties").toString();
            mediaM = new MediaManager(mediaPropsUrl, mimePropsUrl, serializerPropsUrl);
        }
    }

    /**
     * Prepares for and initates the rendering cycle.
     * @param req the servlet request object
     * @param res the servlet response object
     */
    public void writeContent (HttpServletRequest req, HttpServletResponse res) throws PortalException {
        // instantiate user layout manager and check to see if the profile mapping has been established
        if (p_browserMapper != null) {
            try {
                p_browserMapper.prepare(req);
            } catch (Exception e) {
                throw new PortalException(e);
            }
        }

        // instantiate locale manager (uPortal i18n)
        if (localeManager == null) {
            localeManager = new LocaleManager(person, req.getHeader("Accept-Language"));
        }

        if (uPreferencesManager == null || uPreferencesManager.isUserAgentUnmapped()) {
            uPreferencesManager = new UserPreferencesManager(req, this.getPerson(), localeManager);
        } else {
            // p_browserMapper is no longer needed
            p_browserMapper = null;
        }

        if (uPreferencesManager.isUserAgentUnmapped()) {
            // unmapped browser
            if (p_browserMapper== null) {
                p_browserMapper = new org.jasig.portal.channels.CSelectSystemProfile();
                p_browserMapper.initialize(new Hashtable(), "CSelectSystemProfile", true, true, false, 10000, getPerson());
            }
            try {
                p_browserMapper.render(req, res);
            } catch (PortalException pe) {
                throw new PortalException(pe);
            } catch (Throwable t) {
                // something went wrong trying to show CSelectSystemProfileChannel
                LogService.log(LogService.ERROR,"UserInstance::writeContent() : CSelectSystemProfileChannel.render() threw: "+t);
                throw new PortalException("CSelectSystemProfileChannel.render() threw: "+t);
            }
            // don't go any further!
            return;
        }

        // if we got to this point, we can proceed with the rendering
        if (channelManager == null) {
            channelManager = new ChannelManager(uPreferencesManager);
            uPreferencesManager.getUserLayoutManager().addLayoutEventListener(channelManager);
            p_rendering_lock=new Object();
        }
        renderState (req, res, this.channelManager, this.localeManager, uPreferencesManager, p_rendering_lock);
    }

    /**
     * <code>renderState</code> method orchestrates the rendering pipeline.
     * @param req the <code>HttpServletRequest</code>
     * @param res the <code>HttpServletResponse</code>
     * @param channelManager the <code>ChannelManager</code> instance
     * @param upm the <code>IUserPreferencesManager</code> instance
     * @param rendering_lock a lock for rendering on a single user
     * @exception PortalException if an error occurs
     */
    public void renderState (HttpServletRequest req, HttpServletResponse res, ChannelManager channelManager, LocaleManager localeManager, IUserPreferencesManager upm, Object rendering_lock) throws PortalException {
        uPreferencesManager = upm;
        // process possible worker dispatch
        if(!processWorkerDispatch(req,res,channelManager)) {
            synchronized(rendering_lock) {
                // This function does ALL the content gathering/presentation work.
                // The following filter sequence is processed:
                //        userLayoutXML (in UserPreferencesManager)
                //              |
                //        incorporate StructureAttributes
                //              |
                //        Structure transformation
                //              + (buffering step)
                //        ChannelRendering Buffer
                //              |
                //        ThemeAttributesIncorporation Filter
                //              |
                //        Theme Transformation
                //              |
                //        ChannelIncorporation filter
                //              |
                //        Serializer (XHTML/WML/HTML/etc.)
                //              |
                //        JspWriter
                //

                try {

                    // call layout manager to process all user-preferences-related request parameters
                    // this will update UserPreference object contained by UserPreferencesManager, so that
                    // appropriate attribute incorporation filters and parameter tables can be constructed.
                    uPreferencesManager.processUserPreferencesParameters(req);

                    // determine uPElement (optimistic prediction) --begin
                    // We need uPElement for ChannelManager.setReqNRes() call. That call will distribute uPElement
                    // to Privileged channels. We assume that Privileged channels are smart enough not to delete
                    // themselves in the detach mode !

                    // In general transformations will start at the userLayoutRoot node, unless
                    // we are rendering something in a detach mode.
                    IUserLayoutNodeDescription rElement = null;
                    // see if an old detach target exists in the servlet path


                    UPFileSpec upfs=new UPFileSpec(req);
                    String rootNodeId = upfs.getMethodNodeId();
                    if(rootNodeId==null) {
                        rootNodeId=USER_LAYOUT_ROOT_NODE;
                    }

                    // give channels the current locale manager
                    channelManager.setLocaleManager(localeManager);

                    // see if a new root target has been specified
                    String newRootNodeId = req.getParameter("uP_detach_target");

                    // set optimistic uPElement value
                    UPFileSpec uPElement=new UPFileSpec(PortalSessionManager.INTERNAL_TAG_VALUE,UPFileSpec.RENDER_METHOD,rootNodeId,null,null);

                    if(newRootNodeId!=null) {
                        // set a new root
                        uPElement.setMethodNodeId(newRootNodeId);
                    }

                    IUserLayoutManager ulm=uPreferencesManager.getUserLayoutManager();

                    // process events that have to be handed directly to the userPreferencesManager.
                    // (examples of such events are "remove channel", "minimize channel", etc.
                    //  basically things that directly affect the userLayout structure)
                    try {
                        processUserLayoutParameters(req,channelManager);
                    } catch (PortalException pe) {
                        LogService.log(LogService.ERROR, "UserInstance.renderState(): processUserLayoutParameters() threw an exception - " + pe.getMessage());
                    }


                    // determine uPElement (optimistic prediction) --end

                    // set up the channel manager

                    channelManager.startRenderingCycle(req, res, uPElement);


                    // after this point the layout is determined

                    UserPreferences userPreferences=uPreferencesManager.getUserPreferences();
                    StructureStylesheetDescription ssd= uPreferencesManager.getStructureStylesheetDescription();
                    ThemeStylesheetDescription tsd=uPreferencesManager.getThemeStylesheetDescription();

                    // verify upElement and determine rendering root --begin
                    if (newRootNodeId != null && (!newRootNodeId.equals(rootNodeId))) {
                        // see if the new detach traget is valid
                        try {
                            rElement = ulm.getNode(newRootNodeId);
                        } catch (PortalException e) {
                            rElement=null;
                        }

                        if (rElement != null) {
                            // valid new root id was specified. need to redirect
                            // peterk: should we worry about forwarding
                            // parameters here ? or those passed with detach
                            // always get sacked ?
                            // Andreas: Forwarding parameters with detach
                            // are not lost anymore with the URLUtil class.

                            // Skip the uP_detach_target parameter since
                            // it has already been processed
                            String[] skipParams = new String[]
                                {"uP_detach_target"};

                            try {
                                URLUtil.redirect(req, res, newRootNodeId,
                                    true, skipParams, CHARACTER_SET);
                            } catch (PortalException pe) {
                                StringBuffer sb = new StringBuffer(128);
                                sb.append("UserInstance::renderState() : ");
                                sb.append("PortalException occurred while ");
                                sb.append("redirecting.");
                                sb.append(pe.toString());
                                LogService.log(LogService.ERROR,
                                    sb.toString(), pe);
                            }
                            return;
                        }
                    }
                    // else ignore new id, proceed with the old root target (or the lack of such)
                    if (rootNodeId != null) {
                        // LogService.log(LogService.DEBUG,"UserInstance::renderState() : uP_detach_target=\""+rootNodeId+"\".");
                        try {
                            rElement = ulm.getNode(rootNodeId);
                        } catch (PortalException e) {
                            rElement=null;
                        }
                    }
                    // if we haven't found root node so far, set it to the userLayoutRoot
                    if (rElement == null) {
                        rootNodeId=USER_LAYOUT_ROOT_NODE;
                    }

                    // update the render target
                    uPElement.setMethodNodeId(rootNodeId);

                    // inform channel manager about the new uPElement value
                    channelManager.setUPElement(uPElement);
                    // verify upElement and determine rendering root --begin

                    // Disable page caching
                    res.setHeader("pragma", "no-cache");
                    res.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
                    res.setDateHeader("Expires", 0);
                    // set the response mime type
                    res.setContentType(tsd.getMimeType() + "; charset=" +
                        CHARACTER_SET);
                    // obtain the writer - res.getWriter() must occur after res.setContentType()
                    PrintWriter out = res.getWriter();
                    // get a serializer appropriate for the target media
                    BaseMarkupSerializer markupSerializer = mediaM.getSerializerByName(tsd.getSerializerName(), out);
                    // set up the serializer
                    markupSerializer.asContentHandler();
                    // see if we can use character caching
                    boolean ccaching=(CHARACTER_CACHE_ENABLED && (markupSerializer instanceof CachingSerializer));
                    channelManager.setCharacterCaching(ccaching);
                    // initialize ChannelIncorporationFilter
                    //            ChannelIncorporationFilter cif = new ChannelIncorporationFilter(markupSerializer, channelManager); // this should be slightly faster then the ccaching version, may be worth adding support later
                    CharacterCachingChannelIncorporationFilter cif = new CharacterCachingChannelIncorporationFilter(markupSerializer, channelManager,UserInstance.CACHE_ENABLED && UserInstance.CHARACTER_CACHE_ENABLED);
                    String cacheKey=null;
                    boolean output_produced=false;
                    if(UserInstance.CACHE_ENABLED) {
                        boolean ccache_exists=false;
                        // obtain the cache key
                        cacheKey=constructCacheKey(this.getPerson(),rootNodeId);
                        if(ccaching) {
                            // obtain character cache
                            CharacterCacheEntry cCache=(CharacterCacheEntry) this.systemCharacterCache.get(cacheKey);
                            if(cCache!=null && cCache.channelIds!=null && cCache.systemBuffers!=null) {
                                ccache_exists=true;
                                LogService.log(LogService.DEBUG,"UserInstance::renderState() : retreived transformation character block cache for a key \""+cacheKey+"\"");
                                // start channel threads
                                for(int i=0;i<cCache.channelIds.size();i++) {
                                    String channelSubscribeId=(String) cCache.channelIds.get(i);
                                    if(channelSubscribeId!=null) {
                                        try {
                                            channelManager.startChannelRendering(channelSubscribeId);
                                        } catch (PortalException e) {
                                            LogService.log(LogService.ERROR,"UserInstance::renderState() : unable to start rendering channel (subscribeId=\""+channelSubscribeId+"\", user="+person.getID()+" layoutId="+uPreferencesManager.getCurrentProfile().getLayoutId()+e.getRecordedException().toString());
                                        }
                                    } else {
                                        LogService.log(LogService.ERROR,"UserInstance::renderState() : channel entry "+Integer.toString(i)+" in character cache is invalid (user="+person.getID()+")!");
                                    }
                                }
                                channelManager.commitToRenderingChannelSet();

                                // go through the output loop
                                int ccsize=cCache.systemBuffers.size();
                                if(cCache.channelIds.size()!=ccsize-1) {
                                    LogService.log(LogService.ERROR,"UserInstance::renderState() : channelIds character cache has invalid size !");
                                    LogService.log(LogService.ERROR,"UserInstance::renderState() : ccache cotnains "+cCache.systemBuffers.size()+" system buffers and "+cCache.channelIds.size()+" channel entries");

                                }
                                CachingSerializer cSerializer=(CachingSerializer) markupSerializer;
                                cSerializer.setDocumentStarted(true);

                                for(int sb=0; sb<ccsize-1;sb++) {
                                    cSerializer.printRawCharacters((String)cCache.systemBuffers.get(sb));

                                    //LogService.log(LogService.DEBUG,"----------printing frame piece "+Integer.toString(sb));
                                    //LogService.log(LogService.DEBUG,(String)cCache.systemBuffers.get(sb));

                                    // get channel output
                                    String channelSubscribeId=(String) cCache.channelIds.get(sb);
                                    channelManager.outputChannel(channelSubscribeId,markupSerializer);
                                }

                                // print out the last block
                                cSerializer.printRawCharacters((String)cCache.systemBuffers.get(ccsize-1));
                                //LogService.log(LogService.DEBUG,"----------printing frame piece "+Integer.toString(ccsize-1));
                                //LogService.log(LogService.DEBUG,(String)cCache.systemBuffers.get(ccsize-1));

                                cSerializer.flush();
                                output_produced=true;
                            }
                        }
                        // if this failed, try XSLT cache
                        if((!ccaching) || (!ccache_exists)) {
                            // obtain XSLT cache

                            SAX2BufferImpl cachedBuffer=(SAX2BufferImpl) this.systemCache.get(cacheKey);
                            if(cachedBuffer!=null) {
                                // replay the buffer to channel incorporation filter
                                LogService.log(LogService.DEBUG,"UserInstance::renderState() : retreived XSLT transformation cache for a key \""+cacheKey+"\"");
                                // attach rendering buffer downstream of the cached buffer
                                ChannelRenderingBuffer crb = new ChannelRenderingBuffer((XMLReader)cachedBuffer,channelManager,ccaching);
                                // attach channel incorporation filter downstream of the channel rendering buffer
                                cif.setParent(crb);
                                crb.setOutputAtDocumentEnd(true);
                                cachedBuffer.outputBuffer((ContentHandler)crb);

                                output_produced=true;
                            }
                        }
                    }
                    // fallback on the regular rendering procedure
                    if(!output_produced) {

                        // obtain transformer handlers for both structure and theme stylesheets
                        TransformerHandler ssth = XSLT.getTransformerHandler(ResourceLoader.getResourceAsURL(UserInstance.class, ssd.getStylesheetURI()).toString());
                        TransformerHandler tsth = XSLT.getTransformerHandler(tsd.getStylesheetURI(), localeManager.getLocales(), this);

                        // obtain transformer references from the handlers
                        Transformer sst=ssth.getTransformer();
                        Transformer tst=tsth.getTransformer();

                        // empty transformer to do dom2sax transition
                        Transformer emptyt=TransformerFactory.newInstance().newTransformer();

                        // initialize ChannelRenderingBuffer and attach it downstream of the structure transformer
                        ChannelRenderingBuffer crb = new ChannelRenderingBuffer(channelManager,ccaching);
                        ssth.setResult(new SAXResult(crb));

                        // determine and set the stylesheet params
                        // prepare .uP element and detach flag to be passed to the stylesheets
                        // Including the context path in front of uPElement is necessary for phone.com browsers to work
                        sst.setParameter("baseActionURL", uPElement.getUPFile());
                        // construct idempotent version of the uPElement
                        UPFileSpec uPIdempotentElement=new UPFileSpec(uPElement);
                        uPIdempotentElement.setTagId(PortalSessionManager.IDEMPOTENT_URL_TAG);
                        sst.setParameter("baseIdempotentActionURL",uPElement.getUPFile());

                        Hashtable supTable = userPreferences.getStructureStylesheetUserPreferences().getParameterValues();
                        for (Enumeration e = supTable.keys(); e.hasMoreElements();) {
                            String pName = (String)e.nextElement();
                            String pValue = (String)supTable.get(pName);
                            LogService.log(LogService.DEBUG, "UserInstance::renderState() : setting sparam \"" + pName + "\"=\"" + pValue + "\".");
                            sst.setParameter(pName, pValue);
                        }

                        // all the parameters are set up, fire up structure transformation

                        // filter to fill in channel/folder attributes for the "structure" transformation.
                        StructureAttributesIncorporationFilter saif = new StructureAttributesIncorporationFilter(ssth, userPreferences.getStructureStylesheetUserPreferences());

                        // This is a debug statement that will print out XML incoming to the
                        // structure transformation to a log file serializer to a printstream
                        StringWriter dbwr1 = null;
                        OutputFormat outputFormat = null;
                        if (logXMLBeforeStructureTransformation) {
                            dbwr1 = new StringWriter();
                            outputFormat = new OutputFormat();
                            outputFormat.setIndenting(true);
                            XMLSerializer dbser1 = new XMLSerializer(dbwr1, outputFormat);
                            SAX2DuplicatingFilterImpl dupl1 = new SAX2DuplicatingFilterImpl(ssth, dbser1);
                            dupl1.setParent(saif);
                        }

                        // if operating in the detach mode, need wrap everything
                        // in a document node and a <layout_fragment> node
                        boolean detachMode=!rootNodeId.equals(USER_LAYOUT_ROOT_NODE);
                        if (detachMode) {
                            saif.startDocument();
                            saif.startElement("","layout_fragment","layout_fragment", new org.xml.sax.helpers.AttributesImpl());

                            //                            emptyt.transform(new DOMSource(rElement),new SAXResult(new ChannelSAXStreamFilter((ContentHandler)saif)));
                            if(rElement==null) {
                                ulm.getUserLayout(new ChannelSAXStreamFilter((ContentHandler)saif));
                            } else {
                                ulm.getUserLayout(rElement.getId(),new ChannelSAXStreamFilter((ContentHandler)saif));
                            }

                            saif.endElement("","layout_fragment","layout_fragment");
                            saif.endDocument();
                        } else {
                            if(rElement==null) {
                                ulm.getUserLayout((ContentHandler)saif);
                            } else {
                                ulm.getUserLayout(rElement.getId(),(ContentHandler)saif);
                            }
                            //                            emptyt.transform(new DOMSource(rElement),new SAXResult((ContentHandler)saif));
                        }
                        // all channels should be rendering now

                        // Debug piece to print out the recorded pre-structure transformation XML
                        if (logXMLBeforeStructureTransformation) {
                            LogService.log(LogService.DEBUG, "UserInstance::renderState() : XML incoming to the structure transformation :\n\n" + dbwr1.toString() + "\n\n");
                        }

                        // prepare for the theme transformation

                        // set up of the parameters
                        tst.setParameter("baseActionURL", uPElement.getUPFile());
                        tst.setParameter("baseIdempotentActionURL",uPIdempotentElement.getUPFile());

                        Hashtable tupTable = userPreferences.getThemeStylesheetUserPreferences().getParameterValues();
                        for (Enumeration e = tupTable.keys(); e.hasMoreElements();) {
                            String pName = (String)e.nextElement();
                            String pValue = (String)tupTable.get(pName);
                            LogService.log(LogService.DEBUG, "UserInstance::renderState() : setting tparam \"" + pName + "\"=\"" + pValue + "\".");
                            tst.setParameter(pName, pValue);
                        }
                        // tst.setParameter("locale", localeManager.getLocaleFromSessionParameter());

                        // initialize a filter to fill in channel attributes for the "theme" (second) transformation.
                        // attach it downstream of the channel rendering buffer
                        ThemeAttributesIncorporationFilter taif = new ThemeAttributesIncorporationFilter((XMLReader)crb, userPreferences.getThemeStylesheetUserPreferences());
                        // attach theme transformation downstream of the theme attribute incorporation filter
                        taif.setAllHandlers(tsth);

                        // This is a debug statement that will print out XML incoming to the
                        // theme transformation to a log file serializer to a printstream
                        StringWriter dbwr2 = null;
                        if (logXMLBeforeThemeTransformation) {
                            dbwr2 = new StringWriter();
                            XMLSerializer dbser2 = new XMLSerializer(dbwr2, outputFormat);
                            SAX2DuplicatingFilterImpl dupl2 = new SAX2DuplicatingFilterImpl(tsth, dbser2);
                            dupl2.setParent(taif);
                        }

                        if(UserInstance.CACHE_ENABLED && !ccaching) {
                            // record cache
                            // attach caching buffer downstream of the theme transformer
                            SAX2BufferImpl newCache=new SAX2BufferImpl();
                            tsth.setResult(new SAXResult(newCache));

                            // attach channel incorporation filter downstream of the caching buffer
                            cif.setParent(newCache);

                            systemCache.put(cacheKey,newCache);
                            newCache.setOutputAtDocumentEnd(true);
                            LogService.log(LogService.DEBUG,"UserInstance::renderState() : recorded transformation cache with key \""+cacheKey+"\"");
                        } else {
                            // attach channel incorporation filter downstream of the theme transformer
                            tsth.setResult(new SAXResult(cif));
                        }
                        // fire up theme transformation
                        crb.stopBuffering();
                        crb.outputBuffer();
                        crb.clearBuffer();

                        // Debug piece to print out the recorded pre-theme transformation XML
                        if (logXMLBeforeThemeTransformation) {
                            LogService.log(LogService.DEBUG, "UserInstance::renderState() : XML incoming to the theme transformation :\n\n" + dbwr2.toString() + "\n\n");
                        }


                        if(UserInstance.CACHE_ENABLED && ccaching) {
                            // save character block cache
                            CharacterCacheEntry ce=new CharacterCacheEntry();
                            ce.systemBuffers=cif.getSystemCCacheBlocks();
                            ce.channelIds=cif.getChannelIdBlocks();
                            if(ce.systemBuffers==null || ce.channelIds==null) {
                                LogService.log(LogService.ERROR,"UserInstance::renderState() : CharacterCachingChannelIncorporationFilter returned invalid cache entries!");
                            } else {
                                // record cache
                                systemCharacterCache.put(cacheKey,ce);
                                LogService.log(LogService.DEBUG,"UserInstance::renderState() : recorded transformation character block cache with key \""+cacheKey+"\"");

                                /*
                                  LogService.log(LogService.DEBUG,"Printing transformation cache system blocks:");
                                  for(int i=0;i<ce.systemBuffers.size();i++) {
                                  LogService.log(LogService.DEBUG,"----------piece "+Integer.toString(i));
                                  LogService.log(LogService.DEBUG,(String)ce.systemBuffers.get(i));
                                  }
                                  LogService.log(LogService.DEBUG,"Printing transformation cache channel IDs:");
                                  for(int i=0;i<ce.channelIds.size();i++) {
                                  LogService.log(LogService.DEBUG,"----------channel entry "+Integer.toString(i));
                                  LogService.log(LogService.DEBUG,(String)ce.channelIds.get(i));
                                  }
                                */

                            }
                        }

                    }
                    // signal the end of the rendering round
                    channelManager.finishedRenderingCycle();
                } catch (PortalException pe) {
                    throw pe;
                } catch (Exception e) {
                    throw new PortalException(e);
                }
            }
        }
    }

    /**
     * <code>getRenderingLock</code> returns a rendering lock for this session.
     * @param sessionId current session id
     * @return rendering lock <code>Object</code>
     */
    Object getRenderingLock(String sessionId) {
        if(p_rendering_lock==null) {
            p_rendering_lock=new Object();
        }
        return p_rendering_lock;
    }

    private String constructCacheKey(IPerson person,String rootNodeId) throws PortalException {
        StringBuffer sbKey = new StringBuffer(1024);
        sbKey.append(person.getID()).append(",");
        sbKey.append(rootNodeId).append(",");
        sbKey.append(uPreferencesManager.getUserPreferences().getCacheKey());
        sbKey.append(uPreferencesManager.getUserLayoutManager().getCacheKey());
        return sbKey.toString();
    }


    /**
     * Gets the person object from the session.  Null is returned if
     * no person is logged in
     * @return the person object, null if no person is logged in
     */
    public IPerson getPerson () {
        return  this.person;
    }

    /**
     * This notifies UserInstance that it has been unbound from the session.
     * Method triggers cleanup in ChannelManager.
     *
     * @param bindingEvent an <code>HttpSessionBindingEvent</code> value
     */
    public void valueUnbound(HttpSessionBindingEvent bindingEvent) {
        if(channelManager!=null)
            channelManager.finishedSession();
    if(uPreferencesManager!=null) {
      uPreferencesManager.finishedSession(bindingEvent);
      try {      
        IUserLayoutManager ulm = uPreferencesManager.getUserLayoutManager(); 
		if ( ulm instanceof TransientUserLayoutManagerWrapper )
		  ulm = ((TransientUserLayoutManagerWrapper)ulm).getOriginalLayoutManager();   
        if ( !(ulm instanceof IAggregatedUserLayoutManager) )
          ulm.saveUserLayout();    
      } catch ( Exception e ) {
		  LogService.log(LogService.ERROR, "UserInstance::valueUnbound(): Error occured while saving the user layout "+e);    
      }
    }

        // Record the destruction of the session
        StatsRecorder.recordSessionDestroyed(person);
        GroupService.finishedSession(person);
    }

    /**
     * Notifies UserInstance that it has been bound to a session.
     *
     * @param bindingEvent a <code>HttpSessionBindingEvent</code> value
     */
    public void valueBound (HttpSessionBindingEvent bindingEvent) {
        // Record the creation of the session
        StatsRecorder.recordSessionCreated(person);
    }

    /**
     * Process layout action events.
     * Events are described by the following request params:
     * uP_help_target
     * uP_about_target
     * uP_edit_target
     * uP_remove_target
     * uP_detach_target
     * @param req a <code>HttpServletRequest</code> value
     * @param channelManager a <code>ChannelManager</code> value
     * @exception PortalException if an error occurs
     */
    private synchronized void processUserLayoutParameters (HttpServletRequest req, ChannelManager channelManager) throws PortalException {
     try {

       IUserLayoutManager ulm = uPreferencesManager.getUserLayoutManager();
       String newNodeId = null;

        // Sending the theme stylesheets parameters based on the user security context
        UserPreferences userPrefs = uPreferencesManager.getUserPreferences();
        ThemeStylesheetUserPreferences themePrefs = userPrefs.getThemeStylesheetUserPreferences();
        StructureStylesheetUserPreferences structPrefs = userPrefs.getStructureStylesheetUserPreferences();
        
        String authenticated = String.valueOf(person.getSecurityContext().isAuthenticated());
        structPrefs.putParameterValue("authenticated", authenticated);
        String userName = person.getFullName();
        if (userName != null && userName.trim().length() > 0)
            themePrefs.putParameterValue("userName", userName);
        try {
            if (ChannelStaticData.getAuthorizationPrincipal(person).canPublish()) {
                themePrefs.putParameterValue("authorizedFragmentPublisher", "true");
                themePrefs.putParameterValue("authorizedChannelPublisher", "true");
            }
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
        
        String[] values;
        if ((values = req.getParameterValues("uP_help_target")) != null) {
            for (int i = 0; i < values.length; i++) {
                channelManager.passPortalEvent(values[i], new PortalEvent(PortalEvent.HELP_BUTTON_EVENT));
            }
        }
        if ((values = req.getParameterValues("uP_about_target")) != null) {
            for (int i = 0; i < values.length; i++) {
                channelManager.passPortalEvent(values[i], new PortalEvent(PortalEvent.ABOUT_BUTTON_EVENT));
            }
        }
        if ((values = req.getParameterValues("uP_edit_target")) != null) {
            for (int i = 0; i < values.length; i++) {
                channelManager.passPortalEvent(values[i], new PortalEvent(PortalEvent.EDIT_BUTTON_EVENT));
            }
        }
        if ((values = req.getParameterValues("uP_detach_target")) != null) {
            channelManager.passPortalEvent(values[0], new PortalEvent(PortalEvent.DETACH_BUTTON_EVENT));
        }

        if ((values = req.getParameterValues("uP_request_move_targets")) != null) {
            if ( values[0].trim().length() == 0 ) values[0] = null;
             ulm.markMoveTargets(values[0]);
        } else {
             ulm.markMoveTargets(null);
          }

        if ((values = req.getParameterValues("uP_request_add_targets")) != null) {
            String value;
            int nodeType = values[0].equals("folder")?IUserLayoutNodeDescription.FOLDER:IUserLayoutNodeDescription.CHANNEL;
            IUserLayoutNodeDescription nodeDesc = ulm.createNodeDescription(nodeType);
            nodeDesc.setName("Unnamed");
            if ( nodeType == IUserLayoutNodeDescription.CHANNEL && (value = req.getParameter("channelPublishID")) != null ) {
             String contentPublishId = value.trim();
             if ( contentPublishId.length() > 0 ) {
              ((IUserLayoutChannelDescription)nodeDesc).setChannelPublishId(contentPublishId);
              themePrefs.putParameterValue("channelPublishID",contentPublishId);
             }
            } else if ( nodeType == IUserLayoutNodeDescription.FOLDER && (value = req.getParameter("fragmentPublishID")) != null ) {
				String contentPublishId = value.trim();
				String fragmentRootId = CommonUtils.nvl(req.getParameter("fragmentRootID"));
				if ( contentPublishId.length() > 0 && fragmentRootId.length() > 0 ) {
				  IALFolderDescription folderDesc = (IALFolderDescription) nodeDesc;	
               	  folderDesc.setFragmentId(contentPublishId);
				  folderDesc.setFragmentNodeId(fragmentRootId);
				} 
				//themePrefs.putParameterValue("uP_fragmentPublishID",contentPublishId);	
            }	
            newNodeDescription = nodeDesc;
            ulm.markAddTargets(newNodeDescription);
        } else {
            ulm.markAddTargets(null);
          }

        if ((values = req.getParameterValues("uP_add_target")) != null) {
         String[] values1, values2;
         String value = null;
         int nodeType = values[0].equals("folder")?IUserLayoutNodeDescription.FOLDER:IUserLayoutNodeDescription.CHANNEL;
         values1 =  req.getParameterValues("targetNextID");
         if ( values1 != null && values1.length > 0 )
            value = values1[0];
         if ( (values2 = req.getParameterValues("targetParentID")) != null ) {
          if (  newNodeDescription != null ) {
            if ( CommonUtils.nvl(value).trim().length() == 0 ) 
             value = null;
            
            // Adding a new node
            newNodeId = ulm.addNode(newNodeDescription,values2[0],value).getId();
            
            // if the new node is a fragment being added - we need to re-load the layout
            if ( newNodeDescription instanceof IALFolderDescription ) {
              IALFolderDescription folderDesc = (IALFolderDescription) newNodeDescription;	
              if ( folderDesc.getFragmentNodeId() != null ) {
                ulm.saveUserLayout();
                ulm.loadUserLayout();
              }  
            }
			 
          }
         }
            newNodeDescription = null;
        }

        if ((values = req.getParameterValues("uP_move_target")) != null) {
         String[] values1, values2;
         String value = null;
         values1 = req.getParameterValues("targetNextID");
         if ( values1 != null && values1.length > 0 )
            value = values1[0];
         if ( (values2 = req.getParameterValues("targetParentID")) != null ) {
            if ( CommonUtils.nvl(value).trim().length() == 0 ) value = null;
            ulm.moveNode(values[0],values2[0],value);
         }
        }

        if ((values = req.getParameterValues("uP_rename_target")) != null) {
         String[] values1;
         if ( (values1 = req.getParameterValues("uP_target_name")) != null ) {
            IUserLayoutNodeDescription nodeDesc = ulm.getNode(values[0]);
            if ( nodeDesc != null ) {
             String oldName = nodeDesc.getName();
             nodeDesc.setName(values1[0]);
             if ( !ulm.updateNode(nodeDesc) )
              nodeDesc.setName(oldName);
            }
         }
        }

        if ((values = req.getParameterValues("uP_remove_target")) != null) {
            for (int i = 0; i < values.length; i++) {
                ulm.deleteNode(values[i]);
            }
        }

        String param = req.getParameter("uP_cancel_targets");
        if ( param != null && param.equals("true") ) {
           ulm.markAddTargets(null);
           ulm.markMoveTargets(null);
           newNodeDescription = null;
        }
        
		param = req.getParameter("uP_reload_layout");
		if ( param != null && param.equals("true") ) {
		  ulm.loadUserLayout();
		}
        
		param = req.getParameter("uPcFM_action");
		if ( param != null ) { 
		  if ( ulm instanceof TransientUserLayoutManagerWrapper )
		    ulm = ((TransientUserLayoutManagerWrapper)ulm).getOriginalLayoutManager();
		  if ( ulm instanceof IAggregatedUserLayoutManager ) {		
			IAggregatedUserLayoutManager alm = (IAggregatedUserLayoutManager) ulm;
			String fragmentId = req.getParameter("uP_fragmentID"); 
			if ( param.equals("edit") && fragmentId != null ) {
		         if ( CommonUtils.parseInt(fragmentId) > 0 ) 
		           alm.loadFragment(fragmentId); 
		         else 
		           alm.loadUserLayout();
		    } else if ( param.equals("save") ) {
			       alm.saveFragment();
			}
		  }	  
		}
		
		// If we have created a new node we need to let the structure XSL know about it
		structPrefs.putParameterValue("newNodeID",CommonUtils.nvl(newNodeId));


      } catch ( Exception e ) {
          e.printStackTrace();
          throw new PortalException(e);
        }
    }

    /**
     * A method will determine if current request is a worker dispatch, and if so process it appropriatly
     * @param req the <code>HttpServletRequest</code>
     * @param res the <code>HttpServletResponse</code>
     * @param cm the <code>ChannelManager</code> instance
     * @return a <code>boolean</code> value
     * @exception PortalException if an error occurs
     */
    protected boolean processWorkerDispatch(HttpServletRequest req, HttpServletResponse res, ChannelManager cm) throws PortalException {

        HttpSession session = req.getSession(false);
        if(session!=null) {
            // determine uPFile
            try {
                UPFileSpec upfs=new UPFileSpec(req);
                // is this a worker method ?
                if(upfs.getMethod()!=null && upfs.getMethod().equals(UPFileSpec.WORKER_URL_ELEMENT)) {
                    // this is a worker dispatch, process it
                    // determine worker type

                    String workerName=upfs.getMethodNodeId();

                    if(workerName!=null) {
                        if(UserInstance.workerProperties==null) {
                            // load worker properties
                            try {
                                UserInstance.workerProperties=ResourceLoader.getResourceAsProperties(UserInstance.class, WORKER_PROPERTIES_FILE_NAME);
                            } catch (IOException ioe) {
                                LogService.log(LogService.ERROR, "UserInstance::processWorkerDispatch() : Unable to load worker.properties file. "+ioe);
                            }
                            // now add in component archive declared workers
                            CarResources cRes = CarResources.getInstance();
                            cRes.getWorkers( UserInstance.workerProperties );
                        }

                        String dispatchClassName=UserInstance.workerProperties.getProperty(workerName);
                        if(dispatchClassName==null) {
                            throw new PortalException("UserInstance::processWorkerDispatch() : Unable to find processing class for the worker type \""+workerName+"\". Please check worker.properties");
                        } else {
                            // try to instantiate a worker class
                            try {
                                Object obj = CarResources.getInstance()
                                    .getClassLoader()
                                    .loadClass( dispatchClassName )
                                    .newInstance();
                                IWorkerRequestProcessor wrp=(IWorkerRequestProcessor) obj;
                                // invoke processor
                                try {
                                    wrp.processWorkerDispatch(new PortalControlStructures(req,res,cm,uPreferencesManager));
                                } catch (PortalException pe) {
                                    throw pe;
                                } catch (RuntimeException re) {
                                    throw new PortalException(re);
                                }
                            } catch (ClassNotFoundException cnfe) {
                                throw new PortalException("UserInstance::processWorkerDispatch() : Unable to find processing class (\""+dispatchClassName+"\") for the worker type \""+workerName+"\". Please check worker.properties",cnfe);
                            } catch (InstantiationException ie) {
                                throw new PortalException("UserInstance::processWorkerDispatch() : Unable to instantiate processing class (\""+dispatchClassName+"\") for the worker type \""+workerName+"\". Please check worker.properties",ie);
                            } catch (IllegalAccessException iae) {
                                throw new PortalException("UserInstance::processWorkerDispatch() : Unable to access processing class (\""+dispatchClassName+"\") for the worker type \""+workerName+"\". Please check worker.properties",iae);
                            }
                        }
                    } else {
                        throw new PortalException("UserInstance::processWorkerDispatch() : Unable to determine worker type.  uPFile=\""+upfs.getUPFile()+"\".");
                    }

                    return true;
                } else {
                    return false;
                }
            } catch (IndexOutOfBoundsException iobe) {
                // ill-constructed URL
                return false;
            }
        }
        // will never get here
        return false;
    }

}



