/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.PortalException;
import org.jasig.portal.utils.XML;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

/**
 * CAbstractXslt is an abstract IChannel which implements the
 * boilerplate of applying a parameterized XSLT to an XML to render the channel
 * output.  Your IChannel can extend CAbstactXSLT and implement the template
 * methods to provide the XML, XSLT, and parameters.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public abstract class CAbstractXslt implements IChannel {

    /**
     * The most recently set ChannelRuntimeData.
     * This data is available to our subclasses via an accessor method.
     */
    private ChannelRuntimeData runtimeData;

    /**
     * The ChannelStaticData.
     * This data is available to our subclasses via an accessor method.
     */
    private ChannelStaticData staticData;
    
    
    /**
     * Commons Logging logger for the runtime class of this channel instance.
     */
    protected Log log = LogFactory.getLog(getClass());
    
    
    public final void setRuntimeData(ChannelRuntimeData rd) {
        /*
         * This implementation logs the received ChannelRuntimeData at trace
         * level and then stores it as the instance variable runtimeData, which is
         * available to extending classes via the accessor method getRuntimeData().
         */
        if (log.isTraceEnabled()) {
            if (log.isTraceEnabled()) {
                log.trace("Received channel runtime data: [" + rd + "]");
            }
        }

        this.runtimeData = rd;
        
        runtimeDataSet();
    }
    
    /**
     * This method is called on setRuntimeData() after CAbstractXslt has
     * updated its state such that a call to getRuntimeData() will return
     * the latest ChannelRuntimeData.
     */
    protected void runtimeDataSet() {
        // do-nothing default implementation
        // subclasses can override to be notified when setRuntimeData()
        // has been received.
    }
    
    protected final ChannelRuntimeData getRuntimeData() {
        return this.runtimeData;
    }

    public final void setStaticData(ChannelStaticData sd) {
        /*
         * This implementation logs the received ChannelStaticData at trace
         * level and then stores it as the instance variable staticData, which is
         * available to extending classes via the accessor method getStaticData().
         */
        if (log.isTraceEnabled()) {
            if (log.isTraceEnabled()) {
                log.trace("Received channel satic data: [" + sd + "]");
            }
        }

        this.staticData = sd;
        
        staticDataSet();
    }
    
    /**
     * This method is called on calls to setStaticData() after internal state
     * has been updated such that getStaticData() will return the 
     * ChannelStaticData.
     *
     */
    protected void staticDataSet() {
        // do-nothing default implementation
        // subclasses can override to be notified when static data was set.
    }

    protected final ChannelStaticData getStaticData() {
        return this.staticData;
    }
    
    public ChannelRuntimeProperties getRuntimeProperties() {
        /*
         * This basic implementation returns a dummy ChannelRuntimeProperties,
         * as it appears all known IChannel implementations currrently do.  This 
         * method is not final so that if you find some useful ChannelRuntimeProperties
         * to return you can return them.
         */
        return new ChannelRuntimeProperties();
    }
    
    public final void renderXML(ContentHandler out) throws PortalException {
        
        /*
         * We implement renderXML by invoking our template methods to get
         * the XML, XSLT URL, and XSLT stylesheet parameters, and then
         * performing a boilerplate XSLT transformation.
         */
        
        if (log.isTraceEnabled()) {
            log.trace("entering renderXML()");
        }
        
        try {
            // Perform the transformation
            XSLT xslt = XSLT.getTransformer(this, this.runtimeData.getLocales());
            
            Document xml = getXml();
            
            if (log.isTraceEnabled()) {
                log.trace("getXml() returned Document: [" + xml  + "]");
                String xmlAsString = XML.serializeNode(xml);
                log.trace("XML DOM was: [" + xmlAsString + "]");
            }
            
            
            
            if (xml == null) {
                throw new IllegalStateException("The Document we would transform, as returned by getXml(), was illegally null.");
            }
            
            xslt.setXML(xml);
            
            String xsltUri = getXsltUri();
            
            if (log.isTraceEnabled()) {
                log.trace("getXsltUri() returned: [" + xsltUri + "]");
            }
            
            if (xsltUri == null) {
                
                throw new IllegalStateException("The URI of our XSLT we would use to transform our Document, as returned by getXsltUri(), was illegally null.");
                
                /*
                 * It would probably be a neat feature to detect the case where
                 * getXsltUri() returns null and in that case dump the XML directly to
                 * the ContentHandler, but this is not yet implemented.
                 */
            }
            
            xslt.setXSL(xsltUri);
            
            xslt.setTarget(out);
            
            Map paramsMap = getStylesheetParams();
            
            if (log.isTraceEnabled()) {
                log.trace("getStylesheetParams() returned [" + paramsMap + "]");
            }
            
            if (paramsMap == null) {
                xslt.setStylesheetParameters(new HashMap());
            } else {
                // XSLT requires HashMap or HashTable rather than
                // accepting any Map. 
                // We accomodate this by dumping our paramsMap into a
                // HashMap to ensure it is of an acceptable type.
                // Skipping this putAll() where it is unnecessary probably wouldn't
                // result in any worthwhile performance difference.
                HashMap tempHashMap = new HashMap();
                tempHashMap.putAll(paramsMap);
                xslt.setStylesheetParameters(tempHashMap);
            }

            if (log.isTraceEnabled()) {
                log.trace("Configured XSLT as [" + xslt + "]");
            }
                
            xslt.transform();
        } catch (PortalException pe) {
            // we just re-throw PortalExceptions, confident that the
            // channel rendering framework will log and handle them, as 
            // defined by the IChannel API we are implementing.
            throw pe;
        } catch (RuntimeException re) {
            // we just re-throw RuntimeExceptions, confident that the
            // channel rendering framework will log and handle them.
            // Wrapping them in a PortalException would come at the cost of
            // their specificity and adds no value -- if they're going to be wrapped,
            // our client can wrap them just as well as we can, and if we can
            // skip wrapping them, so much the better.
            throw re;
        } catch (Exception e) {
            // we log and wrap in PortalExceptions Exceptions other than
            // PortalException and RuntimeException, so that we conform to
            // the IChannel API.
            log.error("Error rendering CAbstractXSLT instance.", e);
            throw new PortalException(e);
        }
    
        
        if (log.isTraceEnabled()) {
            log.trace("returning from renderXML()");
        }
    }
    
    /**
     * Get the Document we should feed to our XSLT.
     * 
     * This method is declared to throw Exception for maximum convenience of
     * the developer extending this class.  Such developers should catch or declare 
     * exceptions as appropriate to your needs.  Just because you can
     * throw Exception here doesn't mean you shouldn't, for example, fallback to 
     * a default XSLT URL when your cannot programmatically determine the URL
     * of your XSLT.  On the other hand, there's no reason for you to wrap SqlExceptions
     * if you're not going to do anything other than what this abstract class does with them
     * (logs them and wraps them in PortalExceptions).
     * 
     * The method invoking
     * this template method, renderXML(), is declared to throw PortalException by the IChannel
     * API.  Any PortalException or RuntimeException thrown by getXsltUri() will
     * be thrown all the way out of the abstract class's renderXML() method.  This approach
     * ensures that developers extending this class retain control over what exceptions
     * their implementions throw.  Note that you can map particular exceptions to particular
     * XML representations and thus particular CError displays as of uPortal 2.5.
     * 
     * Exceptions that are neither RuntimeExceptions nor PortalExceptions thrown by
     * this method will be logged and wrapped in PortalExceptions so that this channel
     * will conform to the IChannel API.
     * 
     * Implementations of this method should not return null.  When this method returns
     * null, renderXML() throws an IllegalStateException.
     * 
     * @return the Document we should feed to our XSLT.
     * @throws Exception including PortalException or any RuntimeException on failure
     */
    protected abstract Document getXml() throws Exception;

    /**
     * Get the URI whereat we can obtain the XSLT we should use to render.
     * 
     * This method is declared to throw Exception for maximum convenience of
     * the developer extending this class.  Such developers should catch or declare 
     * exceptions as appropriate to your needs.  Just because you can
     * throw Exception here doesn't mean you shouldn't, for example, fallback to 
     * a default XSLT URL when your cannot programmatically determine the URL
     * of your XSLT.  On the other hand, there's no reason for you to wrap SqlExceptions
     * if you're not going to do anything other than what this abstract class does with them
     * (logs them and wraps them in PortalExceptions).
     * 
     * The method invoking
     * this template method, renderXML(), is declared to throw PortalException by the IChannel
     * API.  Any PortalException or RuntimeException thrown by getXsltUri() will
     * be thrown all the way out of the abstract class's renderXML() method.  This approach
     * ensures that developers extending this class retain control over what exceptions
     * their implementions throw.  Note that you can map particular exceptions to particular
     * XML representations and thus particular CError displays as of uPortal 2.5.
     * 
     * Exceptions that are neither RuntimeExceptions nor PortalExceptions thrown by
     * this method will be logged and wrapped in PortalExceptions so that this channel
     * will conform to the IChannel API.
     * 
     * Implementations of this method should not return null.  The behavior of this class
     * when this method returns null is currently undefined.  The current implementation
     * is to throw IllegalStateException.  However, it might be an interesting improvement
     * to make the meaning of returning null here be to perform no transformation and just
     * dump the XML to the ContentHandler.
     * 
     * @return URI of the XSLT to use to render the channel
     * @throws Exception including PortalException or any RuntimeException on failure
     */
    protected abstract String getXsltUri() throws Exception;

    /**
     * Get a Map from parameter names to parameter values for parameters to
     * be passed to the XSLT.
     * 
     * Returning null is equivalent to returning an empty map and will not be considered
     * an error condition by the renderXML() implementation.
     * 
     * This method is declared to throw Exception for maximum convenience of
     * the developer extending this class.  Such developers should catch or declare 
     * exceptions as appropriate to your needs.  Just because you can
     * throw Exception here doesn't mean you shouldn't, for example, fallback to 
     * default XSLT parameters when you cannot programmatically determine some or
     * all of your XSLT parameters.  Or, if you have a very channel-specific UI you want to
     * render on failure, you might pass parameters to your XSLT characterizing the failure
     * and let your XSLT render the response.  
     * 
     * There's likely no reason for you to wrap IOExceptions
     * if you're not going to do anything other than what this abstract class does with them
     * (logs them and wraps them in PortalExceptions).
     * 
     * The method invoking
     * this template method, renderXML(), is declared to throw PortalException by the IChannel
     * API.  Any PortalException or RuntimeException thrown by getStylesheetParams() will
     * be thrown all the way out of the abstract class's renderXML() method.  This approach
     * ensures that developers extending this class retain control over what exceptions
     * their implementions throw.  Note that you can map particular exceptions to particular
     * XML representations and thus particular CError displays as of uPortal 2.5.
     * 
     * Exceptions that are neither RuntimeExceptions nor PortalExceptions thrown by
     * this method will be logged and wrapped in PortalExceptions so that this channel
     * will conform to the IChannel API.
     * 
     * @return a Map from parameter names to parameter values, or null (equivalent to empty Map).
     * @throws Exception including PortalException or any RuntimeException on failure.
     */
    protected abstract Map getStylesheetParams() throws Exception;

    
}
