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

import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.BooleanLock;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.SAX2DuplicatingFilterImpl;
import org.jasig.portal.utils.SoftHashMap;
import org.jasig.portal.utils.XSLT;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSession;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import org.w3c.dom.Node;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.dom.DOMSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;

import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.serialize.CachingSerializer;
import org.jasig.portal.serialize.OutputFormat;
import org.jasig.portal.serialize.XMLSerializer;


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
    private static final boolean printXMLBeforeStructureTransformation = false;
    private static final boolean printXMLBeforeThemeTransformation = false;

    // manages layout and preferences
    UserLayoutManager uLayoutManager;
    // manages channel instances and channel rendering
    ChannelManager channelManager;


    // contains information relating client names to media and mime types
    static MediaManager mediaM;

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
    public static final String USER_LAYOUT_ROOT_NODE="userLayoutRootNode";

    private static final String WORKER_PROPERTIES_FILE_NAME = "/properties/worker.properties";
    private static Properties workerProperties;

    final SoftHashMap systemCache=new SoftHashMap(SYSTEM_XSLT_CACHE_MIN_SIZE);
    final SoftHashMap systemCharacterCache=new SoftHashMap(SYSTEM_CHARACTER_BLOCK_CACHE_MIN_SIZE);

    IPerson person;

    public UserInstance (IPerson person) {
        this.person=person;

        // init the media manager
        if(mediaM==null) {
            String mediaPropsUrl = this.getClass().getResource("/properties/media.properties").toString();
            String mimePropsUrl = this.getClass().getResource("/properties/media.properties").toString();
            String serializerPropsUrl = this.getClass().getResource("/properties/media.properties").toString();
            mediaM = new MediaManager(mediaPropsUrl, mimePropsUrl, serializerPropsUrl);
        }
    }

    /**
     * Prepares for and initates the rendering cycle.
     * @param the servlet request object
     * @param the servlet response object
     * @param the JspWriter object
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
        if (uLayoutManager==null || uLayoutManager.isUserAgentUnmapped()) {
            uLayoutManager = new UserLayoutManager(req, this.getPerson());
        } else {
            // p_browserMapper is no longer needed
            p_browserMapper = null;
        }

        if (uLayoutManager.isUserAgentUnmapped()) {
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
                LogService.instance().log(LogService.ERROR,"UserInstance::writeContent() : CSelectSystemProfileChannel.render() threw: "+t);
                throw new PortalException("CSelectSystemProfileChannel.render() threw: "+t);
            }
            // don't go any further!
            return;
        }

        // if we got to this point, we can proceed with the rendering
        if (channelManager == null) {
            channelManager = new ChannelManager(uLayoutManager);
            p_rendering_lock=new Object();
        }
        renderState (req, res, this.channelManager, uLayoutManager,p_rendering_lock);
    }

    /**
     * <code>renderState</code> method orchestrates the rendering pipeline.
     * @param req the <code>HttpServletRequest</code>
     * @param res the <code>HttpServletResponse</code>
     * @param channelManager the <code>ChannelManager</code> instance
     * @param ulm the <code>IUserLayout</code>
     * @param rendering_lock a lock for rendering on a single user
     */
    public void renderState (HttpServletRequest req, HttpServletResponse res, ChannelManager channelManager, IUserLayoutManager ulm, Object rendering_lock) throws PortalException {
        // process possible worker dispatch
        if(!processWorkerDispatch(req,res,channelManager,ulm)) {
            synchronized(rendering_lock) {
                // This function does ALL the content gathering/presentation work.
                // The following filter sequence is processed:
                //        userLayoutXML (in UserLayoutManager)
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
                    // this will update UserPreference object contained by UserLayoutManager, so that
                    // appropriate attribute incorporation filters and parameter tables can be constructed.
                    ulm.processUserPreferencesParameters(req);
                    PrintWriter out=res.getWriter();

                    // determine uPElement (optimistic prediction) --begin
                    // We need uPElement for ChannelManager.setReqNRes() call. That call will distribute uPElement
                    // to Privileged channels. We assume that Privileged channels are smart enough not to delete
                    // themselves in the detach mode !

                    // In general transformations will start at the userLayoutRoot node, unless
                    // we are rendering something in a detach mode.
                    Node rElement = null;
                    // see if an old detach target exists in the servlet path


                    UPFileSpec upfs=new UPFileSpec(req);
                    String rootNodeId = upfs.getMethodNodeId();
                    if(rootNodeId==null) {
                        rootNodeId=USER_LAYOUT_ROOT_NODE;
                    }

                    // see if a new root target has been specified
                    String newRootNodeId = req.getParameter("uP_detach_target");

                    // set optimistic uPElement value
                    String uPElement=null;
                    if(newRootNodeId!=null) {
                        // set a new root
                        uPElement=UPFileSpec.buildUPFileBase(PortalSessionManager.INTERNAL_TAG_VALUE,UPFileSpec.RENDER_METHOD,newRootNodeId,null,null);
                    } else {
                        uPElement=UPFileSpec.buildUPFileBase(PortalSessionManager.INTERNAL_TAG_VALUE,UPFileSpec.RENDER_METHOD,rootNodeId,null,null);
                    }
                    // determine uPElement (optimistic prediction) --end

                    // set up the channel manager
                    channelManager.setReqNRes(req, res, uPElement);
                    // process events that have to be handed directly to the userLayoutManager.
                    // (examples of such events are "remove channel", "minimize channel", etc.
                    //  basically things that directly affect the userLayout structure)
                    try {
                        processUserLayoutParameters(req,channelManager);
                    } catch (PortalException pe) {
                        LogService.instance().log(LogService.ERROR, "UserInstance.renderState(): processUserLayoutParameters() threw an exception - " + pe.getMessage());
                    }

                    // after this point the layout is determined
                    Document userLayout;
                    BooleanLock llock=ulm.getUserLayoutWriteLock();
                    synchronized(llock) {
                        // if the layout lock is dirty, prune the cache
                        if(llock.getValue()) {
                            LogService.instance().log(LogService.DEBUG,"UserInstance::writeContent() : pruning system caches.");
                            systemCache.clear();
                            systemCharacterCache.clear();
                            llock.setValue(false);
                        }
                        userLayout=ulm.getUserLayout();
                    }

                    UserPreferences userPreferences=ulm.getUserPreferences();
                    StructureStylesheetDescription ssd= ulm.getStructureStylesheetDescription();
                    ThemeStylesheetDescription tsd=ulm.getThemeStylesheetDescription();

                    // verify upElement and determine rendering root --begin
                    // reset uPElement
                    uPElement = UPFileSpec.RENDER_URL_ELEMENT;
                    if (newRootNodeId != null && (!newRootNodeId.equals(rootNodeId))) {
                        // see if the new detach traget is valid
                        rElement = userLayout.getElementById(newRootNodeId);
                        if (rElement != null) {
                            // valid new root id was specified. need to redirect
                            // peterk: should we worry about forwarding parameters here ? or those passed with detach always get sacked ?
                            res.sendRedirect(UPFileSpec.buildUPFile(PortalSessionManager.INTERNAL_TAG_VALUE,UPFileSpec.RENDER_METHOD,newRootNodeId,null,null));
                            // res.sendRedirect(UPFileSpec.DETACH_URL_ELEMENT+UPFileSpec.PORTAL_URL_SEPARATOR+newRootNodeId+UPFileSpec.PORTAL_URL_SEPARATOR+UPFileSpec.PORTAL_URL_SUFFIX);
                            return;
                        }
                    }
                    // else ignore new id, proceed with the old root target (or the lack of such)
                    if (rootNodeId != null) {
                        // LogService.instance().log(LogService.DEBUG,"UserInstance::renderState() : uP_detach_target=\""+rootNodeId+"\".");
                        rElement = userLayout.getElementById(rootNodeId);
                    }
                    // if we haven't found root node so far, set it to the userLayoutRoot
                    if (rElement == null) {
                        rElement = userLayout;
                        rootNodeId=USER_LAYOUT_ROOT_NODE;
                    }

                    uPElement=UPFileSpec.buildUPFileBase(PortalSessionManager.INTERNAL_TAG_VALUE,UPFileSpec.RENDER_METHOD,rootNodeId,null,null);

                    // inform channel manager about the new uPElement value
                    channelManager.setUPElement(uPElement);
                    // verify upElement and determine rendering root --begin



                    // set the response mime type
                    res.setContentType(tsd.getMimeType());
                    // get a serializer appropriate for the target media
                    BaseMarkupSerializer markupSerializer = mediaM.getSerializerByName(tsd.getSerializerName(), out);
                    // set up the serializer
                    markupSerializer.asContentHandler();
                    // see if we can use character caching
                    boolean ccaching=(CHARACTER_CACHE_ENABLED && (markupSerializer instanceof CachingSerializer));
                    // initialize ChannelIncorporationFilter
                    //            ChannelIncorporationFilter cif = new ChannelIncorporationFilter(markupSerializer, channelManager); // this should be slightly faster then the ccaching version, may be worth adding support later
                    CharacterCachingChannelIncorporationFilter cif = new CharacterCachingChannelIncorporationFilter(markupSerializer, channelManager,this.CACHE_ENABLED && this.CHARACTER_CACHE_ENABLED);
                    String cacheKey=null;
                    boolean output_produced=false;
                    if(this.CACHE_ENABLED) {
                        boolean ccache_exists=false;
                        // obtain the cache key
                        cacheKey=constructCacheKey(this.getPerson(),userPreferences,rootNodeId);
                        if(ccaching) {
                            // obtain character cache
                            CharacterCacheEntry cCache=(CharacterCacheEntry) this.systemCharacterCache.get(cacheKey);
                            if(cCache!=null && cCache.channelIds!=null && cCache.systemBuffers!=null) {
                                ccache_exists=true;
                                LogService.instance().log(LogService.DEBUG,"UserInstance::renderState() : retreived transformation character block cache for a key \""+cacheKey+"\"");
                                // start channel threads
                                for(int i=0;i<cCache.channelIds.size();i++) {
                                    Vector chanEntry=(Vector) cCache.channelIds.get(i);
                                    if(chanEntry!=null || chanEntry.size()!=2) {
                                        String chanId=(String)chanEntry.get(0);
                                        String chanClassName=(String)chanEntry.get(1);
                                        Long timeOut=(Long)chanEntry.get(2);
                                        Hashtable chanParams=(Hashtable)chanEntry.get(3);
                                        String channelPublishId=(String)chanEntry.get(4);
                                        channelManager.startChannelRendering(chanId,channelPublishId, chanClassName,timeOut.longValue(),chanParams,true);
                                    } else {
                                        LogService.instance().log(LogService.ERROR,"UserInstance::renderState() : channel entry "+Integer.toString(i)+" in character cache is invalid !");
                                    }
                                }
                                // go through the output loop
                                int ccsize=cCache.systemBuffers.size();
                                if(cCache.channelIds.size()!=ccsize-1) {
                                    LogService.instance().log(LogService.ERROR,"UserInstance::renderState() : channelId character cache has invalid size !");
                                }
                                CachingSerializer cSerializer=(CachingSerializer) markupSerializer;
                                cSerializer.setDocumentStarted(true);

                                for(int sb=0; sb<ccsize-1;sb++) {
                                    cSerializer.printRawCharacters((String)cCache.systemBuffers.get(sb));

                                    //LogService.instance().log(LogService.DEBUG,"----------printing frame piece "+Integer.toString(sb));
                                    //LogService.instance().log(LogService.DEBUG,(String)cCache.systemBuffers.get(sb));

                                    // get channel output
                                    Vector chanEntry=(Vector) cCache.channelIds.get(sb);
                                    String chanId=(String)chanEntry.get(0);
                                    String chanClassName=(String)chanEntry.get(1);
                                    Long timeOut=(Long)chanEntry.get(2);
                                    Hashtable chanParams=(Hashtable)chanEntry.get(3);
                                    String channelPublishId=(String)chanEntry.get(4);
                                    Object o=channelManager.getChannelCharacters (chanId, channelPublishId, chanClassName,timeOut.longValue(),chanParams);
                                    if(o!=null) {
                                        if(o instanceof String) {
                                            LogService.instance().log(LogService.DEBUG,"UserInstance::renderState() : received a character result for channelId=\""+chanId+"\"");
                                            cSerializer.printRawCharacters((String)o);
                                            //LogService.instance().log(LogService.DEBUG,"----------printing channel cache #"+Integer.toString(sb));
                                            //LogService.instance().log(LogService.DEBUG,(String)o);
                                        } else if(o instanceof SAX2BufferImpl) {
                                            LogService.instance().log(LogService.DEBUG,"UserInstance::renderState() : received an XSLT result for channelId=\""+chanId+"\"");
                                            // extract a character cache

                                            // start new channel cache
                                            if(!cSerializer.startCaching()) {
                                                LogService.instance().log(LogService.ERROR,"UserInstance::renderState() : unable to restart channel cache on a channel start!");
                                            }

                                            // output channel buffer
                                            if(o instanceof SAX2BufferImpl) {
                                                SAX2BufferImpl b=(SAX2BufferImpl) o;
                                                b.outputBuffer(markupSerializer);
                                            }

                                            // save the old cache state
                                            if(cSerializer.stopCaching()) {
                                                try {
                                                    channelManager.setChannelCharacterCache(chanId,cSerializer.getCache());
                                                    //LogService.instance().log(LogService.DEBUG,"----------generated channel cache #"+Integer.toString(sb));
                                                    //LogService.instance().log(LogService.DEBUG,cSerializer.getCache());
                                                } catch (UnsupportedEncodingException e) {
                                                    LogService.instance().log(LogService.ERROR,"UserInstance::renderState() : unable to obtain character cache, invalid encoding specified ! "+e);
                                                } catch (IOException ioe) {
                                                    LogService.instance().log(LogService.ERROR,"UserInstance::renderState() : IO exception occurred while retreiving character cache ! "+ioe);
                                                }

                                            } else {
                                                LogService.instance().log(LogService.ERROR,"UserInstance::renderState() : unable to reset cache state ! Serializer was not caching when it should've been !");
                                            }
                                        } else {
                                            LogService.instance().log(LogService.ERROR,"UserInstance::renderState() : ChannelManager.getChannelCharacters() returned an unidentified object!");
                                        }
                                    }
                                }

                                // print out the last block
                                cSerializer.printRawCharacters((String)cCache.systemBuffers.get(ccsize-1));
                                //LogService.instance().log(LogService.DEBUG,"----------printing frame piece "+Integer.toString(ccsize-1));
                                //LogService.instance().log(LogService.DEBUG,(String)cCache.systemBuffers.get(ccsize-1));

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
                                LogService.instance().log(LogService.DEBUG,"UserInstance::renderState() : retreived XSLT transformation cache for a key \""+cacheKey+"\"");
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
                        TransformerHandler ssth = XSLT.getTransformerHandler(PortalSessionManager.getResourceAsURL(ssd.getStylesheetURI()).toString());
                        TransformerHandler tsth = XSLT.getTransformerHandler(PortalSessionManager.getResourceAsURL(tsd.getStylesheetURI()).toString());

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
                        sst.setParameter("baseActionURL", new String(uPElement+UPFileSpec.PORTAL_URL_SUFFIX));
                        Hashtable supTable = userPreferences.getStructureStylesheetUserPreferences().getParameterValues();
                        for (Enumeration e = supTable.keys(); e.hasMoreElements();) {
                            String pName = (String)e.nextElement();
                            String pValue = (String)supTable.get(pName);
                            LogService.instance().log(LogService.DEBUG, "UserInstance::renderState() : setting sparam \"" + pName + "\"=\"" + pValue + "\".");
                            sst.setParameter(pName, pValue);
                        }
                        // all the parameters are set up, fire up structure transformation

                        // filter to fill in channel/folder attributes for the "structure" transformation.
                        StructureAttributesIncorporationFilter saif = new StructureAttributesIncorporationFilter(ssth, userPreferences.getStructureStylesheetUserPreferences());

                        // This is a debug statement that will print out XML incoming to the
                        // structure transformation to a log file serializer to a printstream
                        StringWriter dbwr1 = null;
                        OutputFormat outputFormat = null;
                        if (printXMLBeforeStructureTransformation) {
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

                            emptyt.transform(new DOMSource(rElement),new SAXResult(new ChannelSAXStreamFilter((ContentHandler)saif)));
                            saif.endElement("","layout_fragment","layout_fragment");
                            saif.endDocument();
                        } else {
                            emptyt.transform(new DOMSource(rElement),new SAXResult((ContentHandler)saif));
                        }
                        // all channels should be rendering now

                        // Debug piece to print out the recorded pre-structure transformation XML
                        if (printXMLBeforeStructureTransformation) {
                            LogService.instance().log(LogService.DEBUG, "UserInstance::renderState() : XML incoming to the structure transformation :\n\n" + dbwr1.toString() + "\n\n");
                        }

                        // prepare for the theme transformation

                        // set up of the parameters
                        tst.setParameter("baseActionURL", new String(uPElement+UPFileSpec.PORTAL_URL_SUFFIX));

                        Hashtable tupTable = userPreferences.getThemeStylesheetUserPreferences().getParameterValues();
                        for (Enumeration e = tupTable.keys(); e.hasMoreElements();) {
                            String pName = (String)e.nextElement();
                            String pValue = (String)tupTable.get(pName);
                            LogService.instance().log(LogService.DEBUG, "UserInstance::renderState() : setting tparam \"" + pName + "\"=\"" + pValue + "\".");
                            tst.setParameter(pName, pValue);
                        }

                        // initialize a filter to fill in channel attributes for the "theme" (second) transformation.
                        // attach it downstream of the channel rendering buffer
                        ThemeAttributesIncorporationFilter taif = new ThemeAttributesIncorporationFilter((XMLReader)crb, userPreferences.getThemeStylesheetUserPreferences());
                        // attach theme transformation downstream of the theme attribute incorporation filter
                        taif.setAllHandlers(tsth);

                        // This is a debug statement that will print out XML incoming to the
                        // theme transformation to a log file serializer to a printstream
                        StringWriter dbwr2 = null;
                        if (printXMLBeforeThemeTransformation) {
                            dbwr2 = new StringWriter();
                            XMLSerializer dbser2 = new XMLSerializer(dbwr2, outputFormat);
                            SAX2DuplicatingFilterImpl dupl2 = new SAX2DuplicatingFilterImpl(tsth, dbser2);
                            dupl2.setParent(taif);
                        }

                        if(this.CACHE_ENABLED && !ccaching) {
                            // record cache
                            // attach caching buffer downstream of the theme transformer
                            SAX2BufferImpl newCache=new SAX2BufferImpl();
                            tsth.setResult(new SAXResult(newCache));

                            // attach channel incorporation filter downstream of the caching buffer
                            cif.setParent(newCache);

                            systemCache.put(cacheKey,newCache);
                            newCache.setOutputAtDocumentEnd(true);
                            LogService.instance().log(LogService.DEBUG,"UserInstance::renderState() : recorded transformation cache with key \""+cacheKey+"\"");
                        } else {
                            // attach channel incorporation filter downstream of the theme transformer
                            tsth.setResult(new SAXResult(cif));
                        }
                        // fire up theme transformation
                        crb.stopBuffering();
                        crb.outputBuffer();
                        crb.clearBuffer();

                        // Debug piece to print out the recorded pre-theme transformation XML
                        if (printXMLBeforeThemeTransformation) {
                            LogService.instance().log(LogService.DEBUG, "UserInstance::renderState() : XML incoming to the theme transformation :\n\n" + dbwr2.toString() + "\n\n");
                        }


                        if(this.CACHE_ENABLED && ccaching) {
                            // save character block cache
                            CharacterCacheEntry ce=new CharacterCacheEntry();
                            ce.systemBuffers=cif.getSystemCCacheBlocks();
                            ce.channelIds=cif.getChannelIdBlocks();
                            if(ce.systemBuffers==null || ce.channelIds==null) {
                                LogService.instance().log(LogService.ERROR,"UserInstance::renderState() : CharacterCachingChannelIncorporationFilter returned invalid cache entries!");
                            } else {
                                // record cache
                                systemCharacterCache.put(cacheKey,ce);
                                LogService.instance().log(LogService.DEBUG,"UserInstance::renderState() : recorded transformation character block cache with key \""+cacheKey+"\"");

                                /*
                                  LogService.instance().log(LogService.DEBUG,"Printing transformation cache system blocks:");
                                  for(int i=0;i<ce.systemBuffers.size();i++) {
                                  LogService.instance().log(LogService.DEBUG,"----------piece "+Integer.toString(i));
                                  LogService.instance().log(LogService.DEBUG,(String)ce.systemBuffers.get(i));
                                  }
                                  LogService.instance().log(LogService.DEBUG,"Printing transformation cache channel IDs:");
                                  for(int i=0;i<ce.channelIds.size();i++) {
                                  LogService.instance().log(LogService.DEBUG,"----------channel entry "+Integer.toString(i));
                                  LogService.instance().log(LogService.DEBUG,(String)((Vector)ce.channelIds.get(i)).get(0));
                                  }
                                */


                            }
                        }
                        
                    }
                    // signal the end of the rendering round
                    channelManager.finishedRendering();
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

    private static String constructCacheKey(IPerson person,UserPreferences userPreferences,String rootNodeId) {
        StringBuffer sbKey = new StringBuffer(1024);
        sbKey.append(person.getID()).append(",");
        sbKey.append(rootNodeId).append(",");
        sbKey.append(userPreferences.getCacheKey());
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
    public void valueUnbound (HttpSessionBindingEvent bindingEvent) {
        channelManager.finishedSession();
    }

    /**
     * Notifies UserInstance that it has been bound to a session.
     *
     * @param bindingEvent a <code>HttpSessionBindingEvent</code> value
     */
    public void valueBound (HttpSessionBindingEvent bindingEvent) {
    }

    /**
     * Process layout action events.
     * Events are described by the following request params:
     * uP_help_target
     * uP_about_target
     * uP_edit_target
     * uP_remove_target
     * uP_detach_target
     * @param the servlet request object
     * @param the userLayout manager object
     */
    private void processUserLayoutParameters (HttpServletRequest req, ChannelManager channelManager) throws PortalException {
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
            for (int i = 0; i < values.length; i++) {
                channelManager.passPortalEvent(values[i], new PortalEvent(PortalEvent.DETACH_BUTTON_EVENT));
            }
        }
        if ((values = req.getParameterValues("uP_remove_target")) != null) {
            for (int i = 0; i < values.length; i++) {
                channelManager.removeChannel(values[i]);
            }
        }
    }

    private class CharacterCacheEntry {
        Vector systemBuffers;
        Vector channelIds;
        public CharacterCacheEntry() {
            systemBuffers=null;
            channelIds=null;
        }
    }

    /**
     * A method will determine if current request is a worker dispatch, and if so process it appropriatly
     * @param req the <code>HttpServletRequest</code>
     * @param res the <code>HttpServletResponse</code>
     * @param cm the <code>ChannelManager</code> instance
     * @param ulm the <code>IUserLayout</code>
     * @param rendering_lock a lock for rendering on a single user
     */
    protected static boolean processWorkerDispatch(HttpServletRequest req, HttpServletResponse res, ChannelManager cm, IUserLayoutManager ulm) throws PortalException {

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
                                UserInstance.workerProperties=new Properties();
                                UserInstance.workerProperties.load(UserInstance.class.getResourceAsStream(WORKER_PROPERTIES_FILE_NAME));
                            } catch (IOException ioe) {
                                LogService.instance().log(LogService.ERROR, "UserInstance::processWorkerDispatch() : Unable to load worker.properties file. "+ioe);
                            }
                        }
                        
                        String dispatchClassName=UserInstance.workerProperties.getProperty(workerName);
                        if(dispatchClassName==null) {
                            throw new PortalException("UserInstance::processWorkerDispatch() : Unable to find processing class for the worker type \""+workerName+"\". Please check worker.properties");
                        } else {
                            // try to instantiate a worker class
                            try {
                                Object obj=Class.forName(dispatchClassName).newInstance();
                                IWorkerRequestProcessor wrp=(IWorkerRequestProcessor) obj;
                                // invoke processor
                                try {
                                    wrp.processWorkerDispatch(new PortalControlStructures(req,res,cm,ulm));
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



