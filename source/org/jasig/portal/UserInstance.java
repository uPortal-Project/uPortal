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

import org.jasig.portal.utils.*;
import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.utils.XSLT;
import  javax.servlet.*;
import  javax.servlet.jsp.*;
import  javax.servlet.http.*;
import  java.io.*;
import  java.util.*;
import  java.text.*;
import  java.net.*;
import  org.w3c.dom.*;
import  org.apache.xalan.xpath.*;
import  org.apache.xalan.xslt.*;
import  org.apache.xml.serialize.*;
import org.jasig.portal.PropertiesManager;


/**
 * A class handling holding all user state information. The class is also reponsible for
 * request processing and orchestrating the entire rendering procedure.
 * (this is a replacement for the good old LayoutBean class)
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 */
public class UserInstance implements HttpSessionBindingListener {
    public static final int guestUserId = 1;
    
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
    public static final boolean CACHE_ENABLED=PropertiesManager.getBooleanProperty("org.jasig.portal.UserInstance.cache_enabled");
    private static final int SYSTEM_XSLT_CACHE_MIN_SIZE=Integer.parseInt(PropertiesManager.getProperty("org.jasig.portal.UserInstance.system_xslt_cache_min_size")); // this should be in a file somewhere
    private static final int SYSTEM_CHARACTER_BLOCK_CACHE_MIN_SIZE=Integer.parseInt(PropertiesManager.getProperty("org.jasig.portal.UserInstance.system_character_block_cache_min_size")); // this should be in a file somewhere
    private static final SoftHashMap systemCache=new SoftHashMap(SYSTEM_XSLT_CACHE_MIN_SIZE);
    private static final SoftHashMap systemCharacterCache=new SoftHashMap(SYSTEM_CHARACTER_BLOCK_CACHE_MIN_SIZE);
    public static final boolean CHARACTER_CACHE_ENABLED=PropertiesManager.getBooleanProperty("org.jasig.portal.UserInstance.character_cache_enabled");
    
    IPerson person;
    
    public UserInstance (IPerson person) {
        this.person=person;
      
        // init the media manager
        if(mediaM==null) {
            String fs = System.getProperty("file.separator");
            String propertiesDir = GenericPortalBean.getPortalBaseDir() + "properties" + fs;
            mediaM = new MediaManager(propertiesDir + "media.properties", propertiesDir + "mime.properties", propertiesDir + "serializer.properties");
        }
    }
   
    /**
     * Prepares for and initates the rendering cycle. 
     * @param the servlet request object
     * @param the servlet response object
     * @param the JspWriter object
     */
    public void writeContent (HttpServletRequest req, HttpServletResponse res, java.io.PrintWriter out) {
        try {
            // instantiate user layout manager and check to see if the profile mapping has been established
            if (p_browserMapper != null) {
                p_browserMapper.prepare(req);
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
                } catch (Exception e) {
                    // something went wrong trying to show CSelectSystemProfileChannel 
                    Logger.log(Logger.ERROR,"UserInstance::writeContent() : unable caught an exception while trying to display CSelectSystemProfileChannel! Exception:"+e);
                }
                // don't go any further!
                return;
            }
	    
            // if we got to this point, we can proceed with the rendering
            if (channelManager == null) {
                channelManager = new ChannelManager(uLayoutManager); 
                p_rendering_lock=new Object();
            }
	    
            // call layout manager to process all user-preferences-related request parameters
            // this will update UserPreference object contained by UserLayoutManager, so that
            // appropriate attribute incorporation filters and parameter tables can be constructed.
            uLayoutManager.processUserPreferencesParameters(req);
            renderState (req, res, out, this.channelManager, uLayoutManager.getUserLayout(), uLayoutManager.getUserPreferences(), uLayoutManager.getStructureStylesheetDescription(),uLayoutManager.getThemeStylesheetDescription(),p_rendering_lock);
        } catch (Exception e) {
            StringWriter sw=new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            sw.flush();
            Logger.log(Logger.ERROR,"UserInstance::writeContent() : an unknown exception occurred : "+sw.toString());
        }
    }
    
    /**
     * <code>renderState</code> method orchestrates the rendering pipeline. 
     * @param req the <code>HttpServletRequest</code>
     * @param res the <code>HttpServletResponse</code> 
     * @param out an output <code>java.io.PrintWriter</code>
     * @param channelManager the <code>ChannelManager</code> instance
     * @param userLayout the user layout
     * @param userPreferences user preferences
     * @param ssd a <code>StructureStylesheetDescription</code> of the structure stylesheet that's to be used
     * @param tsd a <code>ThemeStylesheetDescription</code> of the theme stylesheet that's to be used
     */
    public void renderState (HttpServletRequest req, HttpServletResponse res, java.io.PrintWriter out, ChannelManager channelManager, Document userLayout, UserPreferences userPreferences, StructureStylesheetDescription ssd, ThemeStylesheetDescription tsd, Object rendering_lock) throws Exception {
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


            // determine rendering root -start
            // In general transformations will start at the userLayoutRoot node, unless
            // we are rendering something in a detach mode.
            Node rElement = null;
            boolean detachMode = false;
            // see if an old detach target exists in the servlet path
            String detachId = null;
            String servletPath = req.getServletPath();
            String upFile = servletPath.substring(servletPath.lastIndexOf('/'), servletPath.length());
            int upInd = upFile.indexOf(".uP");
            if (upInd != -1) {
                // found a .uP specification at the end of the context path
                int detachInd = upFile.indexOf("detach_");
                if (detachInd != -1) {
                    detachId = upFile.substring(detachInd + 7, upInd);
                    //		  Logger.log(Logger.DEBUG,"UserInstance::renderState() : found detachId=\""+detachId+"\" in the .uP spec.");
                }
            }
            // see if a new detach target has been specified
            String newDetachId = req.getParameter("uP_detach_target");
            if (newDetachId != null && (!newDetachId.equals(detachId))) {
                // see if the new detach traget is valid
                rElement = userLayout.getElementById(newDetachId);
                if (rElement != null) {
                    // valid new detach id was specified. need to redirect
                    res.sendRedirect(req.getContextPath() + "/detach_" + newDetachId + ".uP");
                    return;
                }
            }
            // else ignore new id, proceed with the old detach target (or the lack of such)
            if (detachId != null) {
                // Logger.log(Logger.DEBUG,"UserInstance::renderState() : uP_detach_target=\""+detachId+"\".");
                rElement = userLayout.getElementById(detachId);
                detachMode = true;
            }
            // if we haven't found root node so far, set it to the userLayoutRoot
            if (rElement == null) {
                rElement = userLayout;
                detachMode = false;
            }
            String uPElement = "render.uP";
            if (detachMode) {
                Logger.log(Logger.DEBUG, "UserInstance::renderState() : entering detach mode for nodeId=\"" + detachId + "\".");
                uPElement = "detach_" + detachId + ".uP";
            }
            // determine rendering root -end


            // set up the channel manager
            channelManager.setReqNRes(req, res, uPElement);
            // process events that have to be handed directly to the userLayoutManager.
            // (examples of such events are "remove channel", "minimize channel", etc.
            //  basically things that directly affect the userLayout structure)
            try {
                processUserLayoutParameters(req,channelManager);
            } catch (PortalException pe) {
                Logger.log(Logger.ERROR, "UserInstance.renderState(): processUserLayoutParameters() threw an exception - " + pe.getMessage());
            }
            // set the response mime type
            res.setContentType(tsd.getMimeType());
            // get a serializer appropriate for the target media
            BaseMarkupSerializer markupSerializer = mediaM.getSerializerByName(tsd.getSerializerName(), out);
            // set up the serializer
            markupSerializer.asContentHandler();
            // see if we can use character caching
            boolean ccaching=(CHARACTER_CACHE_ENABLED && (markupSerializer instanceof CachingSerializer));
            // initialize ChannelIncorporationFilter
            //            ChannelIncorporationFilter cif = new ChannelIncorporationFilter(markupSerializer, channelManager);
            CharacterCachingChannelIncorporationFilter cif = new CharacterCachingChannelIncorporationFilter(markupSerializer, channelManager,this.CACHE_ENABLED && this.CHARACTER_CACHE_ENABLED);
            String cacheKey=null;
            boolean output_produced=false;
            if(this.CACHE_ENABLED) {
                boolean ccache_exists=false;
                // obtain the cache key
                cacheKey=constructCacheKey(this.getPerson(),userPreferences);
                if(ccaching) {
                    // obtain character cache
                    CharacterCacheEntry cCache=(CharacterCacheEntry) this.systemCharacterCache.get(cacheKey);
                    if(cCache!=null && cCache.channelIds!=null && cCache.systemBuffers!=null) {
                        ccache_exists=true;
                        Logger.log(Logger.DEBUG,"UserInstance::renderState() : retreived transformation character block cache for a key \""+cacheKey+"\"");
                        // start channel threads
                        for(int i=0;i<cCache.channelIds.size();i++) {
                            Vector chanEntry=(Vector) cCache.channelIds.get(i);
                            if(chanEntry!=null || chanEntry.size()!=2) {
                                String chanId=(String)chanEntry.get(0);
                                String chanClassName=(String)chanEntry.get(1);
                                Long timeOut=(Long)chanEntry.get(2);
                                Hashtable chanParams=(Hashtable)chanEntry.get(3);
                                channelManager.startChannelRendering(chanId,chanClassName,timeOut.longValue(),chanParams,true);
                            } else {
                                Logger.log(Logger.ERROR,"UserInstance::renderState() : channel entry "+Integer.toString(i)+" in character cache is invalid !");
                            }
                        }
                        // go through the output loop
                        int ccsize=cCache.systemBuffers.size();
                        if(cCache.channelIds.size()!=ccsize-1) {
                            Logger.log(Logger.ERROR,"UserInstance::renderState() : channelId character cache has invalid size !");
                        }
                        CachingSerializer cSerializer=(CachingSerializer) markupSerializer;
                        for(int sb=0; sb<ccsize-1;sb++) {
                            cSerializer.printRawCharacters((String)cCache.systemBuffers.get(sb));
                            // get channel output
                            Vector chanEntry=(Vector) cCache.channelIds.get(sb);
                            String chanId=(String)chanEntry.get(0);
                            String chanClassName=(String)chanEntry.get(1);
                            Long timeOut=(Long)chanEntry.get(2);
                            Hashtable chanParams=(Hashtable)chanEntry.get(3);
                            Object o=channelManager.getChannelCharacters (chanId, chanClassName,timeOut.longValue(),chanParams);
                            if(o!=null) {
                                if(o instanceof String) {
                                    Logger.log(Logger.DEBUG,"UserInstance::renderState() : received a character result for channelId=\""+chanId+"\"");
                                    cSerializer.printRawCharacters((String)o);
                                } else if(o instanceof SAXBufferImpl) {
                                    Logger.log(Logger.DEBUG,"UserInstance::renderState() : received an XSLT result for channelId=\""+chanId+"\"");
                                    // extract a character cache 

                                    // start new channel cache
                                    if(!cSerializer.startCaching()) {
                                        Logger.log(Logger.ERROR,"UserInstance::renderState() : unable to restart channel cache on a channel start!");
                                    }

                                    // output channel buffer
                                    ((SAXBufferImpl)o).outputBuffer(markupSerializer);

                                    // save the old cache state
                                    if(cSerializer.stopCaching()) {
                                        try {
                                            channelManager.setChannelCharacterCache(chanId,cSerializer.getCache());
                                        } catch (UnsupportedEncodingException e) {
                                            Logger.log(Logger.ERROR,"UserInstance::renderState() : unable to obtain character cache, invalid encoding specified ! "+e);
                                        } catch (IOException ioe) {
                                            Logger.log(Logger.ERROR,"UserInstance::renderState() : IO exception occurred while retreiving character cache ! "+ioe);
                                        }

                                    } else {
                                        Logger.log(Logger.ERROR,"UserInstance::renderState() : unable to reset cache state ! Serializer was not caching when it should've been !");
                                    }
                                } else {
                                    Logger.log(Logger.ERROR,"UserInstance::renderState() : ChannelManager.getChannelCharacters() returned an unidentified object!");
                                }
                            }
                        }

                        // print out the last block
                        out.write((String)cCache.systemBuffers.get(ccsize-1));
                        output_produced=true;
                    }
                }
                // if this failed, try XSLT cache
                if((!ccaching) || (!ccache_exists)) {
                    // obtain XSLT cache

                    SAXBufferImpl cachedBuffer=(SAXBufferImpl) this.systemCache.get(cacheKey);
                    if(cachedBuffer!=null) {
                        // replay the buffer to channel incorporation filter
                        Logger.log(Logger.DEBUG,"UserInstance::renderState() : retreived XSLT transformation cache for a key \""+cacheKey+"\"");
                        ChannelRenderingBuffer crb = new ChannelRenderingBuffer(channelManager,ccaching);
                        // check for the character cache
                        crb.setDocumentHandler(cif);
                        crb.setOutputAtDocumentEnd(true);
                        cachedBuffer.outputBuffer(crb);
                        output_produced=true;
                    }
                }
            } 
            // fallback on the regular rendering procedure
            if(!output_produced) {

                // obtain both structure and theme transformation stylesheet roots
                StylesheetRoot ss = XSLT.getStylesheetRoot(UtilitiesBean.fixURI(ssd.getStylesheetURI()));
                StylesheetRoot ts = XSLT.getStylesheetRoot(UtilitiesBean.fixURI(tsd.getStylesheetURI()));
                // obtain an XSLT processor
                XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
                // prepare .uP element and detach flag to be passed to the stylesheets
                // Including the context path in front of uPElement is necessary for phone.com browsers to work
                XString xuPElement = processor.createXString(req.getContextPath() + "/" + uPElement);
                
                // initialize ChannelRenderingBuffer
                ChannelRenderingBuffer crb = new ChannelRenderingBuffer(channelManager,ccaching);
                // now that pipeline is set up, determine and set the stylesheet params
                processor.setStylesheetParam("baseActionURL", xuPElement);
                Hashtable supTable = userPreferences.getStructureStylesheetUserPreferences().getParameterValues();
                for (Enumeration e = supTable.keys(); e.hasMoreElements();) {
                    String pName = (String)e.nextElement();
                    String pValue = (String)supTable.get(pName);
                    Logger.log(Logger.DEBUG, "UserInstance::renderState() : setting sparam \"" + pName + "\"=\"" + pValue + "\".");
                    processor.setStylesheetParam(pName, processor.createXString(pValue));
                }
                // all the parameters are set up, fire up structure transformation
                processor.setStylesheet(ss);
                processor.setDocumentHandler(crb);
                // filter to fill in channel/folder attributes for the "structure" transformation.
                StructureAttributesIncorporationFilter saif = new StructureAttributesIncorporationFilter(processor, userPreferences.getStructureStylesheetUserPreferences());
                // if operating in the detach mode, need wrap everything
                // in a document node and a <layout_fragment> node
                if (detachMode) {
                    saif.startDocument();
                    saif.startElement("layout_fragment", new org.xml.sax.helpers.AttributeListImpl());
                    UtilitiesBean.node2SAX(rElement, saif);
                    saif.endElement("layout_fragment");
                    saif.endDocument();
                } else if (rElement.getNodeType() == Node.DOCUMENT_NODE) {
                    UtilitiesBean.node2SAX(rElement, saif);
                } else {
                    // as it is, this should never happen
                    saif.startDocument();
                    UtilitiesBean.node2SAX(rElement, saif);
                    saif.endDocument();
                }
                // all channels should be rendering now
                // prepare processor for the theme transformation
                processor.reset();
                // set up of the parameters
                processor.setStylesheetParam("baseActionURL", xuPElement);
                Hashtable tupTable = userPreferences.getThemeStylesheetUserPreferences().getParameterValues();
                for (Enumeration e = tupTable.keys(); e.hasMoreElements();) {
                    String pName = (String)e.nextElement();
                    String pValue = (String)tupTable.get(pName);
                    Logger.log(Logger.DEBUG, "UserInstance::renderState() : setting tparam \"" + pName + "\"=\"" + pValue + "\".");
                    processor.setStylesheetParam(pName, processor.createXString(pValue));
                }
                processor.setStylesheet(ts);
                // initialize a filter to fill in channel attributes for the
                // "theme" (second) transformation.
                ThemeAttributesIncorporationFilter taif = new ThemeAttributesIncorporationFilter(processor, userPreferences.getThemeStylesheetUserPreferences());
                
                if(this.CACHE_ENABLED && !ccaching) {
                    // record cache
                    SAXBufferImpl newCache=new SAXBufferImpl(cif);
                    systemCache.put(cacheKey,newCache);
                    newCache.setOutputAtDocumentEnd(true);
                    //                    newCache.stopBuffering()
                    processor.setDocumentHandler(newCache);
                    Logger.log(Logger.DEBUG,"UserInstance::renderState() : recorded transformation cache with key \""+cacheKey+"\"");
                } else {
                    processor.setDocumentHandler(cif);
                }
                // fire up theme transformation
                crb.setDocumentHandler(taif);
                crb.stopBuffering();


                if(this.CACHE_ENABLED && ccaching) {
                    // save character block cache
                    CharacterCacheEntry ce=new CharacterCacheEntry();
                    ce.systemBuffers=cif.getSystemCCacheBlocks();
                    ce.channelIds=cif.getChannelIdBlocks();
                    if(ce.systemBuffers==null || ce.channelIds==null) {
                        Logger.log(Logger.ERROR,"UserInstance::renderState() : CharacterCachingChannelIncorporationFilter returned invalid cache entries!");
                    } else {
                        // record cache
                        systemCharacterCache.put(cacheKey,ce);
                        Logger.log(Logger.DEBUG,"UserInstance::renderState() : recorded transformation character block cache with key \""+cacheKey+"\"");
                        /*                        Logger.log(Logger.DEBUG,"Printing transformation cache system blocks:");
                        for(int i=0;i<ce.systemBuffers.size();i++) {
                            Logger.log(Logger.DEBUG,"----------piece "+Integer.toString(i));
                            Logger.log(Logger.DEBUG,(String)ce.systemBuffers.get(i));
                        }
                        Logger.log(Logger.DEBUG,"Printing transformation cache channel IDs:");
                        for(int i=0;i<ce.channelIds.size();i++) {
                            Logger.log(Logger.DEBUG,"----------channel entry "+Integer.toString(i));
                            Logger.log(Logger.DEBUG,(String)((Vector)ce.channelIds.get(i)).get(0));
                            }*/
                    }
                }
                
            }
            // signal the end of the rendering round
            channelManager.finishedRendering();
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

    private static String constructCacheKey(IPerson person,UserPreferences userPreferences) {
        StringBuffer sbKey = new StringBuffer(1024);
        sbKey.append(person.getID()).append(",");
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
}



