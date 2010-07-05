/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.rendering;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.CacheEntry;
import org.jasig.portal.CacheType;
import org.jasig.portal.ChannelContentCacheEntry;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.ChannelRenderingBuffer;
import org.jasig.portal.ChannelSAXStreamFilter;
import org.jasig.portal.CharacterCachingChannelIncorporationFilter;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IWorkerRequestProcessor;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.StructureAttributesIncorporationFilter;
import org.jasig.portal.StructureStylesheetDescription;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.ThemeAttributesIncorporationFilter;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.UserInstance;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.car.CarResources;
import org.jasig.portal.events.support.PageRenderTimePortalEvent;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.io.ChannelTitleIncorporationWiterFilter;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.PortletExecutionManager;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.serialize.CachingSerializer;
import org.jasig.portal.serialize.DebugCachingSerializer;
import org.jasig.portal.serialize.OutputFormat;
import org.jasig.portal.serialize.XMLSerializer;
import org.jasig.portal.tools.versioning.Version;
import org.jasig.portal.tools.versioning.VersionsManager;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletRequestInfo;
import org.jasig.portal.url.UrlType;
import org.jasig.portal.url.xml.BaseUrlXalanElements;
import org.jasig.portal.url.xml.PortletUrlXalanElements;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.utils.MovingAverage;
import org.jasig.portal.utils.MovingAverageSample;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.SAX2DuplicatingFilterImpl;
import org.jasig.portal.utils.URLUtil;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.web.skin.ResourcesDao;
import org.jasig.portal.web.skin.ResourcesXalanElements;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

/**
 * Hard-coded rendering pipeline workflow, copied almost exactly from the uPortal 2.X UserInstance code.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portalRenderingPipeline")
public class StaticRenderingPipeline implements IPortalRenderingPipeline, ApplicationEventPublisherAware, InitializingBean {
    // Metric counters
    private static final MovingAverage renderTimes = new MovingAverage();
    private static volatile MovingAverageSample lastRender = new MovingAverageSample();
    

    // To debug structure and/or theme transformations, set these to true
    // and the XML fed to those transformations will be printed to the log.
    private static final boolean logXMLBeforeStructureTransformation = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.UserInstance.log_xml_before_structure_transformation");
    private static final boolean logXMLBeforeThemeTransformation = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.UserInstance.log_xml_before_theme_transformation");;

    // global rendering cache configuration
    private static final boolean CACHE_ENABLED = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.UserInstance.cache_enabled");
    private static final boolean CHARACTER_CACHE_ENABLED = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.UserInstance.character_cache_enabled");
    private static final int SYSTEM_XSLT_CACHE_MIN_SIZE = PropertiesManager.getPropertyAsInt("org.jasig.portal.UserInstance.system_xslt_cache_min_size");
    private static final int SYSTEM_CHARACTER_BLOCK_CACHE_MIN_SIZE = PropertiesManager.getPropertyAsInt("org.jasig.portal.UserInstance.system_character_block_cache_min_size");
    
    // global rendering caches
    private static final Map<String, SAX2BufferImpl> systemCache = Collections.synchronizedMap(new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT, SYSTEM_XSLT_CACHE_MIN_SIZE, .75f, true));
    private static final Map<String, List<CacheEntry>> systemCharacterCache = Collections.synchronizedMap(new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT, SYSTEM_CHARACTER_BLOCK_CACHE_MIN_SIZE, .75f, true));

    /**
     * Listener that exposes full causal information when exceptions occur 
     * during transformation. 
     */
    private static final TransformErrorListener cErrListener =  new TransformErrorListener();
    
    // worker configuration
    private static final String WORKER_PROPERTIES_FILE_NAME = "/properties/worker.properties";
    private static final Properties workerProperties;

    // contains information relating client names to media and mime types
    private static final MediaManager MEDIA_MANAGER = MediaManager.getMediaManager();

    // string that defines which character set to use for content
    private static final String CHARACTER_SET = "UTF-8";

    // external login configuration
    private static final String SECURITY_PROPERTIES_FILE_NAME = "/properties/security.properties";
	private static final String CAS_LOGIN_URL_PROPERTY = "org.jasig.portal.channels.CLogin.CasLoginUrl";
    private static String externalLoginUrl = null;

    static {
        //TODO do via setter injection
        try {
            workerProperties = ResourceLoader.getResourceAsProperties(StaticRenderingPipeline.class, WORKER_PROPERTIES_FILE_NAME);
        }
        catch (IOException ioe) {
            throw new PortalException("Unable to load worker.properties file. ", ioe);
        }
        try {
			Properties securityProps = ResourceLoader.getResourceAsProperties(StaticRenderingPipeline.class,
					SECURITY_PROPERTIES_FILE_NAME);
			externalLoginUrl = securityProps.getProperty(CAS_LOGIN_URL_PROPERTY);
        }
        catch (IOException ioe) {
            throw new PortalException("Unable to load security.properties file. ", ioe);
        }
    }
    
    public static MovingAverageSample getLastRenderSample() {
        return lastRender;
    }
    
    protected final Log log = LogFactory.getLog(this.getClass());
    
    private IPortletWindowRegistry portletWindowRegistry;
    private ApplicationEventPublisher applicationEventPublisher;
    private CarResources carResources;
    private ResourcesDao resourcesDao;
    private PortletExecutionManager portletExecutionManager;
    private IPortalUrlProvider portalUrlProvider;
    
    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    
    /**
     * @param carResources the carResources to set
     */
    @Autowired
    public void setCarResources(CarResources carResources) {
        this.carResources = carResources;
    }
	/**
	 * @param resourcesDao the resourcesDao to set
	 */
	@Autowired
	public void setResourcesDao(ResourcesDao resourcesDao) {
		this.resourcesDao = resourcesDao;
	}

	@Autowired
	public void setPortletExecutionManager(PortletExecutionManager portletExecutionManager) {
        this.portletExecutionManager = portletExecutionManager;
    }
    
	@Autowired
    public IPortalUrlProvider getPortalUrlProvider() {
        return this.portalUrlProvider;
    }
	
    /**
     * Used to generate URLs in the theme XSL
     */
    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider portalUrlProvider) {
        this.portalUrlProvider = portalUrlProvider;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        this.carResources.getWorkers(workerProperties);        
    }
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher(org.springframework.context.ApplicationEventPublisher)
     */
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.IPortalRenderingPipeline#renderState(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.jasig.portal.IUserInstance)
     */
    public void renderState(HttpServletRequest req, HttpServletResponse res, IUserInstance userInstance) throws PortalException {
        final IPerson person = userInstance.getPerson();
        final LocaleManager localeManager = userInstance.getLocaleManager();
        final IUserPreferencesManager uPreferencesManager = userInstance.getPreferencesManager();
        final Object renderingLock = userInstance.getRenderingLock();
        
        // see if a new root target has been specified
        //String newRootNodeId = req.getParameter("uP_detach_target");

        // proccess possible portlet action
        final IPortalRequestInfo requestInfo = portalUrlProvider.getPortalRequestInfo(req);
        
        
        //final IPortletWindowId targetedPortletWindowId = this.portletRequestParameterManager.getTargetedPortletWindowId(req);
        final IPortletRequestInfo portletRequestInfo = requestInfo.getPortletRequestInfo();
        if (portletRequestInfo != null) {
            //final PortletUrl portletUrl = this.portletRequestParameterManager.getPortletRequestInfo(req, targetedPortletWindowId);
            
            if (UrlType.ACTION == requestInfo.getUrlType()) {
                final IPortletWindowId targetWindowId = portletRequestInfo.getTargetWindowId();
                final IPortletEntity targetedPortletEntity = this.portletWindowRegistry.getParentPortletEntity(req, targetWindowId);
                if (targetedPortletEntity != null) {
                    /*
                     * TODO should this work via window IDs or entityIDs? Probably both once action handling is moved out of the
                     * rendering pipeline with the new URL syntax code
                     */
                    this.portletExecutionManager.doPortletAction(targetedPortletEntity.getPortletEntityId(), req, res);
                    return;
                }
            }
        }

        /*
         * TODO we're going to ignore workers for now. Eventually we'll have to re-map EXCLUSIVE handling, again
         * that will probably wait until the new URL syntax code is in place
         */
//        // process possible worker dispatch
//        final boolean workerDispatched = this.processWorkerDispatchIfNecessary(req, res, uPreferencesManager, channelManager);
//        if (workerDispatched) {
//            //If a worker was dispatched to return immediately
//            return;
//        }
        
        //Set a larger buffer to allow for explicit flushing
        res.setBufferSize(16 * 1024);
        
        final long startTime = System.currentTimeMillis();
        synchronized (renderingLock) {
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
            //        Theme Transformatio
            //              |
            //        ChannelIncorporation filter
            //              |
            //        Serializer (XHTML/WML/HTML/etc.)
            //              |
            //        JspWriter
            //

            try {

                // determine uPElement (optimistic prediction) --begin
                // We need uPElement for ChannelManager.setReqNRes() call. That call will distribute uPElement
                // to Privileged channels. We assume that Privileged channels are smart enough not to delete
                // themselves in the detach mode !

                // In general transformations will start at the userLayoutRoot node, unless
                // we are rendering something in a detach mode.
                IUserLayoutNodeDescription rElement = null;
                // see if an old detach target exists in the servlet path

                UPFileSpec upfs = new UPFileSpec(req);
                String rootNodeId = upfs.getMethodNodeId();
                if (rootNodeId == null) {
                    rootNodeId = UPFileSpec.USER_LAYOUT_ROOT_NODE;
                }
//                // give channels the current locale manager
//                channelManager.setLocaleManager(localeManager);

                // see if a new root target has been specified
                String newRootNodeId = req.getParameter("uP_detach_target");

                // set optimistic uPElement value
                UPFileSpec uPElement = new UPFileSpec(UPFileSpec.RENDER_METHOD, rootNodeId, null, null);

                if (newRootNodeId != null) {
                    // set a new root
                    uPElement.setMethodNodeId(newRootNodeId);
                }

                IUserLayoutManager ulm = uPreferencesManager.getUserLayoutManager();

                // determine uPElement (optimistic prediction) --end

//                // set up the channel manager
//
//                channelManager.startRenderingCycle(req, res, uPElement);

                // after this point the layout is determined

                UserPreferences userPreferences = uPreferencesManager.getUserPreferences();
                StructureStylesheetDescription ssd = uPreferencesManager.getStructureStylesheetDescription();
                ThemeStylesheetDescription tsd = uPreferencesManager.getThemeStylesheetDescription();

                // verify upElement and determine rendering root --begin
                if (newRootNodeId != null && (!newRootNodeId.equals(rootNodeId))) {
                    // see if the new detach traget is valid
                    try {
                        rElement = ulm.getNode(newRootNodeId);
                    }
                    catch (PortalException e) {
                        rElement = null;
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
                        String[] skipParams = new String[] { "uP_detach_target" };

                        try {
                            URLUtil.redirect(req, res, newRootNodeId, true, skipParams, CHARACTER_SET);
                        }
                        catch (PortalException pe) {
                            log.error("PortalException occurred while redirecting",
                                    pe);
                        }
                        return;
                    }
                }

                // LogService.log(LogService.DEBUG,"uP_detach_target=\""+rootNodeId+"\".");
                try {
                    rElement = ulm.getNode(rootNodeId);
                }
                catch (PortalException e) {
                    rElement = null;
                }
                // if we haven't found root node so far, set it to the userLayoutRoot
                if (rElement == null) {
                    rootNodeId = UPFileSpec.USER_LAYOUT_ROOT_NODE;
                }

                // update the render target
                uPElement.setMethodNodeId(rootNodeId);

//                // inform channel manager about the new uPElement value
//                channelManager.setUPElement(uPElement);
//                // verify upElement and determine rendering root --begin
//                
//                // Increase output buffer size, buffer will be flushed before and after every <channel>
//                res.setBufferSize(16 * 1024);

                // Disable page caching
                res.setHeader("pragma", "no-cache");
                res.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
                res.setDateHeader("Expires", 0);
                // set the response mime type
                res.setContentType(tsd.getMimeType() + "; charset=" + CHARACTER_SET);
                // obtain the writer - res.getWriter() must occur after res.setContentType()
                Writer out = new BufferedWriter(res.getWriter(), 1024);
                // get a serializer appropriate for the target media
                BaseMarkupSerializer markupSerializer =
                    MEDIA_MANAGER.getSerializerByName(tsd.getSerializerName(),
                        new ChannelTitleIncorporationWiterFilter(out, this.portletExecutionManager, ulm, req, res));
                // set up the serializer
                markupSerializer.asContentHandler();
                // see if we can use character caching
//                channelManager.setCharacterCaching(CACHE_ENABLED);
//                // pass along the serializer name
//                channelManager.setSerializerName(tsd.getSerializerName());
                // initialize ChannelIncorporationFilter
                CharacterCachingChannelIncorporationFilter cif = new CharacterCachingChannelIncorporationFilter((CachingSerializer)markupSerializer, portletExecutionManager, ulm, CACHE_ENABLED && CHARACTER_CACHE_ENABLED, req, res);

                String cacheKey = null;
                boolean output_produced = false;
                if (CACHE_ENABLED) {
                    boolean ccache_exists = false;
                    // obtain the cache key
                    cacheKey = constructCacheKey(uPreferencesManager, rootNodeId);
                    if (CHARACTER_CACHE_ENABLED) {
                        // obtain character cache
                        List<CacheEntry> cacheEntries = systemCharacterCache.get(cacheKey);
                        if(cacheEntries!=null && cacheEntries.size()>0) {
                            ccache_exists = true;
                            if (log.isDebugEnabled())
                                log
                                        .debug("retreived transformation character block cache for a key \""
                                                + cacheKey + "\"");
                            // start channel threads
                            for(int i=0;i<cacheEntries.size();i++) {
                                CacheEntry ce = cacheEntries.get(i);
                                if (ce.getCacheType().equals(CacheType.CHANNEL_CONTENT)) {
                                    String channelSubscribeId = ((ChannelContentCacheEntry)ce).getChannelId();
                                    if(channelSubscribeId!=null) {
                                        try {
                                            this.portletExecutionManager.startPortletRender(channelSubscribeId, req, res);
//                                            channelManager.startChannelRendering(req, res, channelSubscribeId);
                                        } catch (PortalException e) {
                                            log.error("UserInstance::renderState() : unable to start rendering channel (subscribeId=\""+channelSubscribeId+"\", user="+person.getID()+" layoutId="+uPreferencesManager.getCurrentProfile().getLayoutId(),e);
                                        }
                                    } else {
                                        log.error("channel entry " + Integer.toString(i)
                                            + " in character cache is invalid (user=" + person.getID() + ")!");
                                    }
                                }
                            }
//                            channelManager.commitToRenderingChannelSet();

                            // go through the output loop
                            CachingSerializer cSerializer = (CachingSerializer) markupSerializer;
                            cSerializer.setDocumentStarted(true);

                            for(int sb=0; sb<cacheEntries.size();sb++) {
                                CacheEntry ce = cacheEntries.get(sb);
                                if (log.isDebugEnabled()) {
                                    DebugCachingSerializer dcs = new DebugCachingSerializer();
                                    log.debug("----------printing " + ce.getCacheType() + " cache block "+Integer.toString(sb));
                                    ce.replayCache(dcs, this.portletExecutionManager, req, res);
                                    log.debug(dcs.getCache());
                                }

                                // get cache block output
                                ce.replayCache(cSerializer, this.portletExecutionManager, req, res);
                            }

                            cSerializer.flush();
                            output_produced = true;
                        }
                    }
                    // if this failed, try XSLT cache
                    if ((!CACHE_ENABLED) || (!ccache_exists)) {
                        // obtain XSLT cache

                        SAX2BufferImpl cachedBuffer = systemCache.get(cacheKey);
                        if (cachedBuffer != null) {
                            // replay the buffer to channel incorporation filter
                            if (log.isDebugEnabled()) {
                                log.debug("retreived XSLT transformation cache for a key '" + cacheKey + "'");
                            }
                            
                            // attach rendering buffer downstream of the cached buffer
                            ChannelRenderingBuffer crb = new ChannelRenderingBuffer(cachedBuffer, this.portletExecutionManager, CACHE_ENABLED, req, res);
                            
                            // attach channel incorporation filter downstream of the channel rendering buffer
                            cif.setParent(crb);
                            crb.setOutputAtDocumentEnd(true);
                            cachedBuffer.outputBuffer(crb);

                            output_produced = true;
                        }
                    }
                }
                // fallback on the regular rendering procedure
                if (!output_produced) {

                    // obtain transformer handlers for both structure and theme stylesheets
                    TransformerHandler ssth = XSLT.getTransformerHandler(ResourceLoader.getResourceAsURL(this.getClass(), ssd.getStylesheetURI()).toString());
                    TransformerHandler tsth = XSLT.getTransformerHandler(tsd.getStylesheetURI(), localeManager
                            .getLocales(), this);

                    // obtain transformer references from the handlers
                    Transformer sst = ssth.getTransformer();
                    sst.setErrorListener(cErrListener);
                    Transformer tst = tsth.getTransformer();
                    tst.setErrorListener(cErrListener);
                    
                    // pass resourcesDao into transformer
                    tst.setParameter(ResourcesXalanElements.SKIN_RESOURCESDAO_PARAMETER_NAME, resourcesDao);

                    // initialize ChannelRenderingBuffer and attach it downstream of the structure transformer
                    ChannelRenderingBuffer crb = new ChannelRenderingBuffer(this.portletExecutionManager, CACHE_ENABLED, req, res);
                    ssth.setResult(new SAXResult(crb));

                    // determine and set the stylesheet params
                    // prepare .uP element and detach flag to be passed to the stylesheets
                    // Including the context path in front of uPElement is necessary for phone.com browsers to work
                    sst.setParameter("baseActionURL", uPElement.getUPFile());
                    // construct idempotent version of the uPElement
                    UPFileSpec uPIdempotentElement = new UPFileSpec(uPElement);
                    sst.setParameter("baseIdempotentActionURL", uPElement.getUPFile());

                    Hashtable<String, String> supTable = userPreferences.getStructureStylesheetUserPreferences()
                            .getParameterValues();
                    for (Map.Entry<String, String> param : supTable.entrySet()) {
                        String pName = param.getKey();
                        String pValue = param.getValue();
                        if (log.isDebugEnabled())
                            log.debug("setting sparam \"" + pName + "\"=\"" + pValue
                                    + "\".");
                        sst.setParameter(pName, pValue);
                    }

                    // all the parameters are set up, fire up structure transformation

                    // filter to fill in channel/folder attributes for the "structure" transformation.
                    StructureAttributesIncorporationFilter saif = new StructureAttributesIncorporationFilter(ssth,
                            userPreferences.getStructureStylesheetUserPreferences());

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
                    boolean detachMode = !rootNodeId.equals(UPFileSpec.USER_LAYOUT_ROOT_NODE);
                    if (detachMode) {
                        saif.startDocument();
                        saif.startElement("",
                                "layout_fragment",
                                "layout_fragment",
                                new org.xml.sax.helpers.AttributesImpl());

                        //                            emptyt.transform(new DOMSource(rElement),new SAXResult(new ChannelSAXStreamFilter((ContentHandler)saif)));
                        if (rElement == null) {
                            ulm.getUserLayout(new ChannelSAXStreamFilter((ContentHandler) saif));
                        }
                        else {
                            ulm.getUserLayout(rElement.getId(), new ChannelSAXStreamFilter((ContentHandler) saif));
                        }

                        saif.endElement("", "layout_fragment", "layout_fragment");
                        saif.endDocument();
                    }
                    else {
                        if (rElement == null) {
                            ulm.getUserLayout(saif);
                        }
                        else {
                            ulm.getUserLayout(rElement.getId(), saif);
                        }
                        //                            emptyt.transform(new DOMSource(rElement),new SAXResult((ContentHandler)saif));
                    }
                    // all channels should be rendering now

                    // Debug piece to print out the recorded pre-structure transformation XML
                    if (logXMLBeforeStructureTransformation) {
                        if (log.isDebugEnabled())
                            log
                                    .debug("XML incoming to the structure transformation :\n\n"
                                            + dbwr1.toString() + "\n\n");
                    }

                    // prepare for the theme transformation
                    
                    //TODO move into elements specific advisor interfaces when componentizing this class
                    //Setup the transformer parameters
                    tst.setParameter(BaseUrlXalanElements.PORTAL_URL_PROVIDER_PARAMETER, this.portalUrlProvider);
                    tst.setParameter(BaseUrlXalanElements.CURRENT_PORTAL_REQUEST, req);
                    tst.setParameter(PortletUrlXalanElements.PORTLET_WINDOW_REGISTRY_PARAMETER, this.portletWindowRegistry);
                    tst.setParameter("CONTEXT_PATH", req.getContextPath() + "/");

                    // set up of the parameters
                    tst.setParameter("baseActionURL", uPElement.getUPFile());
                    tst.setParameter("baseIdempotentActionURL", uPIdempotentElement.getUPFile());
                    if (externalLoginUrl != null) {
                        tst.setParameter("EXTERNAL_LOGIN_URL", externalLoginUrl);
                    }

                    Hashtable<String, String> tupTable = userPreferences.getThemeStylesheetUserPreferences()
                            .getParameterValues();
                    for (Map.Entry<String, String> param : tupTable.entrySet()) {
                        String pName = param.getKey();
                        String pValue = param.getValue();
                        if (log.isDebugEnabled())
                            log.debug("setting tparam \"" + pName + "\"=\"" + pValue
                                    + "\".");
                        tst.setParameter(pName, pValue);
                    }

                    VersionsManager versionsManager = VersionsManager.getInstance();
                    Version[] versions = versionsManager.getVersions();

                    for (Version version : versions) {
                        String paramName = "version-" + version.getFname();
                        tst.setParameter(paramName, version.dottedTriple());
                    }

                    // the uP_productAndVersion stylesheet parameter is deprecated
                    // instead use the more generic "version-UP_VERSION" generated from the
                    // framework's functional name when all versions are pulled immediately
                    // above.
                    Version uPortalVersion = versionsManager.getVersion(IPermission.PORTAL_FRAMEWORK);
                    tst.setParameter("uP_productAndVersion", "uPortal " + uPortalVersion.dottedTriple());

                    final Locale[] locales = localeManager.getLocales();
                    if (locales != null && locales.length > 0 && locales[0] != null) {
                        tst.setParameter("USER_LANG", locales[0].toString().replace('_', '-'));
                    }

                    // initialize a filter to fill in channel attributes for the "theme" (second) transformation.
                    // attach it downstream of the channel rendering buffer
                    ThemeAttributesIncorporationFilter taif = new ThemeAttributesIncorporationFilter(
                            (XMLReader) crb, userPreferences.getThemeStylesheetUserPreferences());
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

                    if (CACHE_ENABLED && !CACHE_ENABLED) {
                        // record cache
                        // attach caching buffer downstream of the theme transformer
                        SAX2BufferImpl newCache = new SAX2BufferImpl();
                        tsth.setResult(new SAXResult(newCache));

                        // attach channel incorporation filter downstream of the caching buffer
                        cif.setParent(newCache);

                        systemCache.put(cacheKey, newCache);
                        newCache.setOutputAtDocumentEnd(true);
                        if (log.isDebugEnabled())
                            log.debug("recorded transformation cache with key \""
                                    + cacheKey + "\"");
                    }
                    else {
                        // attach channel incorporation filter downstream of the theme transformer
                        tsth.setResult(new SAXResult(cif));
                    }
                    // fire up theme transformation
                    crb.stopBuffering();
                    crb.outputBuffer();
                    crb.clearBuffer();

                    // Debug piece to print out the recorded pre-theme transformation XML
                    if (logXMLBeforeThemeTransformation && log.isDebugEnabled()) {
                        log.debug("XML incoming to the theme transformation :\n\n"
                                + dbwr2.toString() + "\n\n");
                    }

                    if (CACHE_ENABLED) {
                        // save character block cache
                        List<CacheEntry> cacheBlocks = cif.getCacheBlocks();
                        if(cacheBlocks == null) {
                            log
                                    .error("CharacterCachingChannelIncorporationFilter returned invalid cache entries!");
                        }
                        else {
                            // record cache
                            systemCharacterCache.put(cacheKey, cacheBlocks);
                            if (log.isDebugEnabled()) {
                                log
                                        .debug("recorded transformation character block cache with key \""
                                                + cacheKey + "\"");

                                log.debug("Printing transformation cache blocks:");
                                for (int i=0; i<cacheBlocks.size(); i++) {
                                    CacheEntry ce = cacheBlocks.get(i);
                                    if (ce.getCacheType().equals(CacheType.CHARACTERS)) {
                                        log.debug("----------piece "+Integer.toString(i));
                                    } else if (ce.getCacheType().equals(CacheType.CHANNEL_CONTENT)) {
                                        log.debug("----------channel content entry "+Integer.toString(i));
                                    }
                                    DebugCachingSerializer dcs = new DebugCachingSerializer();
                                    ce.replayCache(dcs, this.portletExecutionManager, req, res);
                                    log.debug(dcs.getCache());
                                }
                            }
                        }
                    }

                }
//                // signal the end of the rendering round
//                channelManager.finishedRenderingCycle();
            }
            catch (PortalException pe) {
                throw pe;
            }
            catch (Exception e) {
                throw new PortalException(e);
            }
            finally {
                final long pageRenderTime = System.currentTimeMillis() - startTime;
                lastRender = renderTimes.add(pageRenderTime);
                
                //Get the user's profile
                final UserProfile userProfile = uPreferencesManager.getCurrentProfile();
                
                //Find the activeTab index
                final UserPreferences userPreferences = uPreferencesManager.getUserPreferences();
                final StructureStylesheetUserPreferences structureStylesheetUserPreferences = userPreferences.getStructureStylesheetUserPreferences();
                final String activeTab = structureStylesheetUserPreferences.getParameterValue("activeTab");
                final int activeTabIndex = org.apache.commons.lang.math.NumberUtils.toInt(activeTab, 1);
                
                //Get the user's layout and find the targeted folder (tab)
                final IUserLayoutManager userLayoutManager = uPreferencesManager.getUserLayoutManager();
                final IUserLayoutFolderDescription targetedNode = this.getActiveTab(userLayoutManager, activeTabIndex);
                
                //Create and publish the event.
                final PageRenderTimePortalEvent pageRenderTimePortalEvent = new PageRenderTimePortalEvent(this, person, userProfile, targetedNode, pageRenderTime);
                this.applicationEventPublisher.publishEvent(pageRenderTimePortalEvent);
            }
        }
    }

    protected IUserLayoutFolderDescription getActiveTab(final IUserLayoutManager userLayoutManager, final int activeTabIndex) {
        final String rootFolderId = userLayoutManager.getRootFolderId();
        final Enumeration<String> rootsChildren = userLayoutManager.getChildIds(rootFolderId);
        
        int tabIndex = 0;
        for (String topNodeId = rootsChildren.nextElement(); rootsChildren.hasMoreElements(); topNodeId = rootsChildren.nextElement()) {
            final IUserLayoutNodeDescription topNode = userLayoutManager.getNode(topNodeId);
            
            if (!topNode.isHidden() && IUserLayoutNodeDescription.FOLDER == topNode.getType() && IUserLayoutFolderDescription.REGULAR_TYPE == ((IUserLayoutFolderDescription)topNode).getFolderType()) {
                tabIndex++;
                if (tabIndex == activeTabIndex) {
                    return (IUserLayoutFolderDescription)topNode;
                }
            }
        }
        
        return null;
    }


    /**
     * A method will determine if current request is a worker dispatch, and if so process it appropriately
     * 
     * @return true if a worker was successfully dispatched to, false if no worker dispatch was requested.
     * @exception PortalException if an error occurs while dispatching
     */
    protected boolean processWorkerDispatchIfNecessary(HttpServletRequest req, HttpServletResponse res, IUserPreferencesManager uPreferencesManager, ChannelManager cm) throws PortalException {
        final HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }

        // determine uPFile
        final UPFileSpec upfs;
        try {
            upfs = new UPFileSpec(req);
        }
        catch (IndexOutOfBoundsException iobe) {
            // ill-constructed URL
            return false;
        }
        
        // is this a worker method ?
        if (UPFileSpec.WORKER_URL_ELEMENT.equals(upfs.getMethod())) {
            // this is a worker dispatch, process it
            // determine worker type

            final String workerName = upfs.getMethodNodeId();
            if (workerName == null) {
                throw new PortalException("Unable to determine worker type for name '" + workerName + "', uPFile='" + upfs.getUPFile() + "'.");
            }
            
            final String dispatchClassName = workerProperties.getProperty(workerName);
            if (dispatchClassName == null) {
                throw new PortalException("Unable to find processing class for the worker type '" + workerName + "'. Please check worker.properties");
            }
            
            // try to instantiate a worker class
            try {
                final ClassLoader carClassLoader = this.carResources.getClassLoader();
                final Class<? extends IWorkerRequestProcessor> dispatcherClass = (Class<IWorkerRequestProcessor>)carClassLoader.loadClass(dispatchClassName);
                final IWorkerRequestProcessor wrp = dispatcherClass.newInstance();

                // invoke processor
                try {
                    final PortalControlStructures portalControlStructures = new PortalControlStructures(req, res, cm, uPreferencesManager);
                    wrp.processWorkerDispatch(portalControlStructures);
                }
                catch (PortalException pe) {
                    throw pe;
                }
                catch (RuntimeException re) {
                    throw new PortalException(re);
                }
            }
            catch (ClassNotFoundException cnfe) {
                throw new PortalException("Unable to find processing class '" + dispatchClassName + "' for the worker type '" + workerName + "'. Please check worker.properties", cnfe);
            }
            catch (InstantiationException ie) {
                throw new PortalException("Unable to instantiate processing class '" + dispatchClassName + "' for the worker type '" + workerName + "'. Please check worker.properties", ie);
            }
            catch (IllegalAccessException iae) {
                throw new PortalException("Unable to access processing class '" + dispatchClassName + "' for the worker type '" + workerName + "'. Please check worker.properties", iae);
            }

            return true;
        }

        return false;
    }
    
    protected String constructCacheKey(IUserPreferencesManager uPreferencesManager, String rootNodeId) throws PortalException {
        final UserPreferences userPreferences = uPreferencesManager.getUserPreferences();
        final IUserLayoutManager userLayoutManager = uPreferencesManager.getUserLayoutManager();
        return rootNodeId + "," + userPreferences.getCacheKey() + userLayoutManager.getCacheKey();
    }
    
    
    protected static class CharacterCacheEntry {
        List<String> systemBuffers = null;
        List<String> channelIds = null;
    }
    
    /**
     * Calls {@link Map#clear()} on the system character cache.
     * 
     * @see org.jasig.portal.rendering.IPortalRenderingPipeline#clearSystemCharacterCache()
     */
    public void clearSystemCharacterCache() {
    	systemCharacterCache.clear();
    }

    /**
     * Class providing exposure to causal exception information for exceptions
     * incurred during transformation.
     * 
     * @author Mark Boyd
     */
    private static class TransformErrorListener implements ErrorListener {
        protected final Log log = LogFactory.getLog(UserInstance.class);

        public void error(TransformerException te) throws TransformerException {
            log.error("An error occurred during transforamtion.", te);
        }

        public void fatalError(TransformerException te) throws TransformerException {
            log.error("A fatal error occurred during transforamtion.", te);
        }

        public void warning(TransformerException te) throws TransformerException {
            log.error("A warning occurred during transforamtion.", te);
        }
    }
}
