/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.rendering;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.commons.collections15.map.ReferenceMap;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.ChannelRenderingBuffer;
import org.jasig.portal.ChannelSAXStreamFilter;
import org.jasig.portal.CharacterCachingChannelIncorporationFilter;
import org.jasig.portal.IUserInstance;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IWorkerRequestProcessor;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.StructureAttributesIncorporationFilter;
import org.jasig.portal.StructureStylesheetDescription;
import org.jasig.portal.ThemeAttributesIncorporationFilter;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.UserInstance;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.car.CarResources;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.url.IPortletRequestParameterManager;
import org.jasig.portal.portlet.url.PortletRequestInfo;
import org.jasig.portal.portlet.url.RequestType;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.serialize.CachingSerializer;
import org.jasig.portal.serialize.OutputFormat;
import org.jasig.portal.serialize.XMLSerializer;
import org.jasig.portal.tools.versioning.Version;
import org.jasig.portal.tools.versioning.VersionsManager;
import org.jasig.portal.utils.MovingAverage;
import org.jasig.portal.utils.MovingAverageSample;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.SAX2DuplicatingFilterImpl;
import org.jasig.portal.utils.URLUtil;
import org.jasig.portal.utils.XSLT;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

/**
 * Hard-coded rendering pipeline workflow, copied almost exactly from the uPortal 2.X UserInstance code.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StaticRenderingPipeline implements IPortalRenderingPipeline {
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
    private static final Map<String, SAX2BufferImpl> systemCache = Collections.synchronizedMap(new ReferenceMap<String, SAX2BufferImpl>(ReferenceMap.HARD, ReferenceMap.SOFT, SYSTEM_XSLT_CACHE_MIN_SIZE, .75f, true));
    private static final Map<String, CharacterCacheEntry> systemCharacterCache = Collections.synchronizedMap(new ReferenceMap<String, CharacterCacheEntry>(ReferenceMap.HARD, ReferenceMap.SOFT, SYSTEM_CHARACTER_BLOCK_CACHE_MIN_SIZE, .75f, true));

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

    static {
        //TODO do via setter injection
        try {
            workerProperties = ResourceLoader.getResourceAsProperties(StaticRenderingPipeline.class, WORKER_PROPERTIES_FILE_NAME);
        }
        catch (IOException ioe) {
            throw new PortalException("Unable to load worker.properties file. ", ioe);
        }
        // now add in component archive declared workers
        CarResources cRes = CarResources.getInstance();
        cRes.getWorkers(workerProperties);
    }
    
    public static MovingAverageSample getLastRenderSample() {
        return lastRender;
    }
    
    protected final Log log = LogFactory.getLog(this.getClass());
    
    private IPortletRequestParameterManager portletRequestParameterManager;
    private IPortletWindowRegistry portletWindowRegistry;
    
    /**
     * @return the portletRequestParameterManager
     */
    public IPortletRequestParameterManager getPortletRequestParameterManager() {
        return this.portletRequestParameterManager;
    }
    /**
     * @param portletRequestParameterManager the portletRequestParameterManager to set
     */
    @Required
    public void setPortletRequestParameterManager(IPortletRequestParameterManager portletRequestParameterManager) {
        Validate.notNull(portletRequestParameterManager, "portletRequestParameterManager can not be null");
        this.portletRequestParameterManager = portletRequestParameterManager;
    }
    
    /**
     * @return the portletWindowRegistry
     */
    public IPortletWindowRegistry getPortletWindowRegistry() {
        return this.portletWindowRegistry;
    }
    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Required
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        Validate.notNull(portletWindowRegistry, "portletWindowRegistry can not be null");
        this.portletWindowRegistry = portletWindowRegistry;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.IPortalRenderingPipeline#renderState(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.jasig.portal.IUserInstance)
     */
    public void renderState(HttpServletRequest req, HttpServletResponse res, IUserInstance userInstance) throws PortalException {
        final IPerson person = userInstance.getPerson();
        final LocaleManager localeManager = userInstance.getLocaleManager();
        final IUserPreferencesManager uPreferencesManager = userInstance.getPreferencesManager();
        final ChannelManager channelManager = userInstance.getChannelManager();
        final Object renderingLock = userInstance.getRenderingLock();
        
        // process possible portlet action
        final Set<IPortletWindowId> targetedPortletWindowIds = this.portletRequestParameterManager.getTargetedPortletWindowIds(req);
        for (final IPortletWindowId targetedPortletWindowId : targetedPortletWindowIds) {
            final PortletRequestInfo portletRequestInfo = this.portletRequestParameterManager.getPortletRequestInfo(req, targetedPortletWindowId);
            
            if (RequestType.ACTION.equals(portletRequestInfo.getRequestType())) {
                final IPortletEntity targetedPortletEntity = this.portletWindowRegistry.getParentPortletEntity(req, targetedPortletWindowId);
                final String channelSubscribeId = targetedPortletEntity.getChannelSubscribeId();
                final boolean actionExecuted = channelManager.doChannelAction(req, res, channelSubscribeId, false);
                
                if (actionExecuted) {
                    // The action completed, return immediately
                    return;
                }

                // The action failed, continue and try to render normally
                break;
            }
        }
        
        // process possible worker dispatch
        final boolean workerDispatched = this.processWorkerDispatchIfNecessary(req, res, uPreferencesManager, channelManager);
        if (workerDispatched) {
            //If a worker was dispatched to return immediately
            return;
        }
        
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

                UPFileSpec upfs = new UPFileSpec(req);
                String rootNodeId = upfs.getMethodNodeId();
                if (rootNodeId == null) {
                    rootNodeId = UPFileSpec.USER_LAYOUT_ROOT_NODE;
                }

                // give channels the current locale manager
                channelManager.setLocaleManager(localeManager);

                // see if a new root target has been specified
                String newRootNodeId = req.getParameter("uP_detach_target");

                // set optimistic uPElement value
                UPFileSpec uPElement = new UPFileSpec(PortalSessionManager.INTERNAL_TAG_VALUE,
                        UPFileSpec.RENDER_METHOD, rootNodeId, null, null);

                if (newRootNodeId != null) {
                    // set a new root
                    uPElement.setMethodNodeId(newRootNodeId);
                }

                IUserLayoutManager ulm = uPreferencesManager.getUserLayoutManager();

                // determine uPElement (optimistic prediction) --end

                // set up the channel manager

                channelManager.startRenderingCycle(req, res, uPElement);

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

                // inform channel manager about the new uPElement value
                channelManager.setUPElement(uPElement);
                // verify upElement and determine rendering root --begin

                // Disable page caching
                res.setHeader("pragma", "no-cache");
                res.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
                res.setDateHeader("Expires", 0);
                // set the response mime type
                res.setContentType(tsd.getMimeType() + "; charset=" + CHARACTER_SET);
                // obtain the writer - res.getWriter() must occur after res.setContentType()
                PrintWriter out = res.getWriter();
                // get a serializer appropriate for the target media
                BaseMarkupSerializer markupSerializer = MEDIA_MANAGER.getSerializerByName(tsd.getSerializerName(),
                        out);
                // set up the serializer
                markupSerializer.asContentHandler();
                // see if we can use character caching
                boolean ccaching = (CHARACTER_CACHE_ENABLED && (markupSerializer instanceof CachingSerializer));
                channelManager.setCharacterCaching(ccaching);
                // pass along the serializer name
                channelManager.setSerializerName(tsd.getSerializerName());
                // initialize ChannelIncorporationFilter
                CharacterCachingChannelIncorporationFilter cif = new CharacterCachingChannelIncorporationFilter(markupSerializer, channelManager, CACHE_ENABLED && CHARACTER_CACHE_ENABLED, req, res);

                String cacheKey = null;
                boolean output_produced = false;
                if (CACHE_ENABLED) {
                    boolean ccache_exists = false;
                    // obtain the cache key
                    cacheKey = constructCacheKey(uPreferencesManager, rootNodeId);
                    if (ccaching) {
                        // obtain character cache
                        CharacterCacheEntry cCache = systemCharacterCache.get(cacheKey);
                        if (cCache != null && cCache.channelIds != null && cCache.systemBuffers != null && cCache.systemBuffers.size() > 0) {
                            ccache_exists = true;
                            if (log.isDebugEnabled())
                                log
                                        .debug("retreived transformation character block cache for a key \""
                                                + cacheKey + "\"");
                            // start channel threads
                            for (int i = 0; i < cCache.channelIds.size(); i++) {
                                String channelSubscribeId = cCache.channelIds.get(i);
                                if (channelSubscribeId != null) {
                                    try {
                                        channelManager.startChannelRendering(req, res, channelSubscribeId);
                                    }
                                    catch (PortalException e) {
                                        log
                                                .error("unable to start rendering channel (subscribeId=\""
                                                        + channelSubscribeId
                                                        + "\", user="
                                                        + person.getID()
                                                        + " layoutId="
                                                        + uPreferencesManager.getCurrentProfile().getLayoutId()
                                                        + e.getCause().toString());
                                    }
                                }
                                else {
                                    log.error("channel entry " + Integer.toString(i)
                                            + " in character cache is invalid (user=" + person.getID() + ")!");
                                }
                            }
                            channelManager.commitToRenderingChannelSet();

                            // go through the output loop
                            int ccsize = cCache.systemBuffers.size();
                            if (cCache.channelIds.size() != ccsize - 1) {
                                log
                                        .error("channelIds character cache has invalid size !  "
                                                + "ccache contains "
                                                + cCache.systemBuffers.size()
                                                + " system buffers and "
                                                + cCache.channelIds.size() + " channel entries");

                            }
                            CachingSerializer cSerializer = (CachingSerializer) markupSerializer;
                            cSerializer.setDocumentStarted(true);

                            for (int sb = 0; sb < ccsize - 1; sb++) {
                                cSerializer.printRawCharacters(cCache.systemBuffers.get(sb));
                                if (log.isDebugEnabled()) {
                                    log.debug("----------printing frame piece " + Integer.toString(sb));
                                    log.debug(cCache.systemBuffers.get(sb));
                                }

                                // get channel output
                                String channelSubscribeId = cCache.channelIds.get(sb);
                                channelManager.outputChannel(req, res, channelSubscribeId, markupSerializer);
                            }

                            // print out the last block

                            cSerializer.printRawCharacters(cCache.systemBuffers.get(ccsize - 1));
                            if (log.isDebugEnabled()) {
                                log.debug("----------printing frame piece " + Integer.toString(ccsize - 1));
                                log.debug(cCache.systemBuffers.get(ccsize - 1));
                            }

                            cSerializer.flush();
                            output_produced = true;
                        }
                    }
                    // if this failed, try XSLT cache
                    if ((!ccaching) || (!ccache_exists)) {
                        // obtain XSLT cache

                        SAX2BufferImpl cachedBuffer = systemCache.get(cacheKey);
                        if (cachedBuffer != null) {
                            // replay the buffer to channel incorporation filter
                            if (log.isDebugEnabled()) {
                                log.debug("retreived XSLT transformation cache for a key '" + cacheKey + "'");
                            }
                            
                            // attach rendering buffer downstream of the cached buffer
                            ChannelRenderingBuffer crb = new ChannelRenderingBuffer((XMLReader) cachedBuffer, channelManager, ccaching, req, res);
                            
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

                    // initialize ChannelRenderingBuffer and attach it downstream of the structure transformer
                    ChannelRenderingBuffer crb = new ChannelRenderingBuffer(channelManager, ccaching, req, res);
                    ssth.setResult(new SAXResult(crb));

                    // determine and set the stylesheet params
                    // prepare .uP element and detach flag to be passed to the stylesheets
                    // Including the context path in front of uPElement is necessary for phone.com browsers to work
                    sst.setParameter("baseActionURL", uPElement.getUPFile());
                    // construct idempotent version of the uPElement
                    UPFileSpec uPIdempotentElement = new UPFileSpec(uPElement);
                    uPIdempotentElement.setTagId(PortalSessionManager.IDEMPOTENT_URL_TAG);
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

                    // set up of the parameters
                    tst.setParameter("baseActionURL", uPElement.getUPFile());
                    tst.setParameter("baseIdempotentActionURL", uPIdempotentElement.getUPFile());

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

                    // tst.setParameter("locale", localeManager.getLocaleFromSessionParameter());

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

                    if (CACHE_ENABLED && !ccaching) {
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

                    if (CACHE_ENABLED && ccaching) {
                        // save character block cache
                        CharacterCacheEntry ce = new CharacterCacheEntry();
                        ce.systemBuffers = cif.getSystemCCacheBlocks();
                        ce.channelIds = cif.getChannelIdBlocks();
                        if (ce.systemBuffers == null || ce.channelIds == null) {
                            log
                                    .error("CharacterCachingChannelIncorporationFilter returned invalid cache entries!");
                        }
                        else {
                            // record cache
                            systemCharacterCache.put(cacheKey, ce);
                            if (log.isDebugEnabled()) {
                                log
                                        .debug("recorded transformation character block cache with key \""
                                                + cacheKey + "\"");

                                log.debug("Printing transformation cache system blocks:");
                                for (int i = 0; i < ce.systemBuffers.size(); i++) {
                                    log.debug("----------piece " + Integer.toString(i));
                                    log.debug(ce.systemBuffers.get(i));
                                }
                                log.debug("Printing transformation cache channel IDs:");
                                for (int i = 0; i < ce.channelIds.size(); i++) {
                                    log.debug("----------channel entry " + Integer.toString(i));
                                    log.debug(ce.channelIds.get(i));
                                }
                            }
                        }
                    }

                }
                // signal the end of the rendering round
                channelManager.finishedRenderingCycle();
            }
            catch (PortalException pe) {
                throw pe;
            }
            catch (Exception e) {
                throw new PortalException(e);
            }
            finally {
                lastRender = renderTimes.add(System.currentTimeMillis() - startTime);
            }
        }
    }


    /**
     * A method will determine if current request is a worker dispatch, and if so process it appropriately
     * 
     * @return true if a worker was successfully dispatched to, false if no worker dispatch was requested.
     * @exception PortalException if an error occurs while dispatching
     */
    protected boolean processWorkerDispatchIfNecessary(HttpServletRequest req, HttpServletResponse res, IUserPreferencesManager uPreferencesManager, ChannelManager cm) throws PortalException {
        final HttpSession session = req.getSession(false);
        if (session != null) {
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
                final CarResources carResources = CarResources.getInstance();
                final ClassLoader carClassLoader = carResources.getClassLoader();
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