/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import java.sql.SQLException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * Testcase for CGenericXSLT.
 * We test basics like making the ChannelStaticData and ChannelRuntimeData
 * available to subclasses, spot check proper response to exceptions, and test
 * some edge behaviors with null return values.
 * We notably do not currently test but should test applying a valid XSLT to
 * valid XML with valid parameters.
 * @version $Revision$ $Date$
 */
public class CAbstractXsltTest extends TestCase {

    // TODO: test the case where everything is working as expected.
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that CAbstractXSLT exposes to its subclasses the runtime data it receives.
     */
    public void testGetRuntimeData() {

        MockXSLTChannel instance = new MockXSLTChannel();
        assertNull(instance.getRuntimeData());
        
        ChannelRuntimeData runtimeDataA = new ChannelRuntimeData();
        instance.setRuntimeData(runtimeDataA);
        assertSame(runtimeDataA, instance.getRuntimeData());
        
        ChannelRuntimeData runtimeDataB = new ChannelRuntimeData();
        instance.setRuntimeData(runtimeDataB);
        
    }

    /**
     * Test that CAbstractXSLT exposes to its subclasses the static data it receives.
     */
    public void testGetStaticData() {
        MockXSLTChannel instance = new MockXSLTChannel();
        
        assertNull(instance.getStaticData());
        
        ChannelStaticData sd = new ChannelStaticData();
        instance.setStaticData(sd);
        assertSame(sd, instance.getStaticData());
    }

    /**
     * Test that when we try to renderXML on an implementation that returns a
     * null Document for getXml, we throw IllegalStateException.
     * @throws PortalException
     */
    public void testRenderXMLNullDocument() throws PortalException {
        try {
            MockXSLTChannel instance = new MockXSLTChannel();
            instance.setStaticData(new ChannelStaticData());
            instance.setRuntimeData(new ChannelRuntimeData());
            instance.renderXML(new DummyContentHandler());
        } catch (IllegalStateException ise) {
            // expected
            return;
        }
        fail("Expected to fail with IllegalStateException because our XML was null.");
    }

    /**
     * Test that when getXml() throws a RuntimeException that exception
     * is thrown by renderXML().
     * @throws PortalException
     */
    public void testRenderXMLGetXmlThrowsRuntimeException() throws PortalException {
        RuntimeException runtimeException = new RuntimeException();
        
        MockXSLTChannel mock = new MockXSLTChannel();
        mock.setStaticData(new ChannelStaticData());
        mock.setRuntimeData(new ChannelRuntimeData());
        mock.setThrownFromGetXml(runtimeException);
        
        try {
            mock.renderXML(new DummyContentHandler());
        } catch (RuntimeException rte) {
            assertSame(runtimeException, rte);
            // good, expected throw behavior.
            return;
        }
    }
    
    /**
     * Test that when getXstlUri() throws a PortalException that exception
     * is thrown by renderXML().
     * @throws ParserConfigurationException
     */
    public void testRenderXMLGetXsltUriThrowsPortalException() throws ParserConfigurationException {
        PortalException portalException = new PortalException();
        
        MockXSLTChannel mock = new MockXSLTChannel();
        mock.setStaticData(new ChannelStaticData());
        mock.setRuntimeData(new ChannelRuntimeData());
        Document blankDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        mock.setDocument(blankDoc);
        mock.setThrownFromGetXsltUri(portalException);
        
        try {
            mock.renderXML(new DummyContentHandler());
        } catch (PortalException pe) {
            assertSame(portalException, pe);
            // good, expected throw behavior.
            return;
        }
        fail("Should have thrown the PortalException that getXsltUri() threw.");
    }
    
    /**
     * Test that when getStylesheetParams() throws SQLException 
     * the renderXML() implementation properly wraps that exception into a
     * PortalException in conformance with the IChannel API.
     * @throws ParserConfigurationException
     */
    public void testRenderXMLGetStylesheetParamsThrowsSqlException() throws ParserConfigurationException {
        SQLException sqlException = new SQLException();
        
        MockXSLTChannel mock = new MockXSLTChannel();
        mock.setStaticData(new ChannelStaticData());
        mock.setRuntimeData(new ChannelRuntimeData());
        Document blankDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        mock.setDocument(blankDoc);
        mock.setXsltUriString("anxslt.xsl");
        mock.setThrownFromGetStylesheetParams(sqlException);
        
        try {
            mock.renderXML(new DummyContentHandler());
        } catch (PortalException pe) {
            assertSame(sqlException, pe.getCause());
            // good, expected throw behavior.
            return;
        }
        fail("Should have thrown a PortalException wrapping the SqlException that getStylesheetParams() threw.");
    }
    
    
    /**
     * A Mock implementation of CAbstractXSLT that provides configuration points
     * for scripting the template method return values or exception throw behavior.
     */
    private class MockXSLTChannel extends CAbstractXslt {

        /**
         * The Document that getXml() will return when thrownFromGetXml 
         * is null.
         */
        private Document document;
        
        /**
         * When not null, the Exception that will be thrown from getXml().
         */
        private Exception thrownFromGetXml;
        
        /**
         * The String that getXsltUri() will return when thrownFromGetXsltUri is null.
         */
        private String xsltUriString;
        
        /**
         * When not null, the Exception that will be thrown from
         * getXsltUri().
         */
        private Exception thrownFromGetXsltUri;
        
        /**
         * The Map that getStylesheetParams() will return except when
         * thrownFromGetStylesheetParams is not null.
         */
        private Map stylesheetParamMap;
        
        /**
         * When not null, the Exception that getStylesheetParams() will throw.
         */
        private Exception thrownFromGetStylesheetParams;
        
        
        protected Document getXml() throws Exception {
            /*
             * This mock implementation responds to this method call in the
             * configured way.  If we have an Exception to throw we throw it,
             * otherwise we return the configured return value.
             */
            if (this.thrownFromGetXml != null) {
                throw this.thrownFromGetXml;
            }
            
            return this.document;
        }

        protected String getXsltUri() throws Exception {
            /*
             * This mock implementation responds to this method call in the
             * configured way.  If we have an Exception to throw we throw it,
             * otherwise we return the configured return value.
             */
            if (this.thrownFromGetXsltUri != null) {
                throw this.thrownFromGetXsltUri;
            } 
            
            return this.xsltUriString;
        }

        protected Map getStylesheetParams() throws Exception {
            /*
             * This mock implementation responds to this method call in the
             * configured way.  If we have an Exception to throw we throw it,
             * otherwise we return the configured return value.
             */
            
            if (this.thrownFromGetStylesheetParams != null) {
                throw this.thrownFromGetStylesheetParams;
            }
            
            return this.stylesheetParamMap;
        }

        public void receiveEvent(PortalEvent ev) {
            // do nothing
        }
        
        /**
         * Get the Document we will return on getXml() when we do not throw.
         * @return the Document we will return on getXml() when we do not throw.
         */
        Document getDocument() {
            return this.document;
        }
        
        /**
         * Set the Document we will return for getXml() where we do not throw.
         * @param document Document we will return on getXml() where we do not throw.
         */
        void setDocument(Document document) {
            this.document = document;
        }
        
        /**
         * Get the Map we should return on invocation of getStylesheetParams() where
         * we do not throw.
         * @return the Map we will return on getStylesheetParams() when we do not throw.
         */
        Map getStylesheetParamMap() {
            return this.stylesheetParamMap;
        }
        
        /**
         * Set the Map we will return on invocation of getStylesheetParams() in the case
         * where we do not throw.
         * @param stylesheetParamMap Map we should return on getStylesheetParams().
         */
        void setStylesheetParamMap(Map stylesheetParamMap) {
            this.stylesheetParamMap = stylesheetParamMap;
        }
        
        /**
         * Get the Throwable that we will throw on invocation of getStylesheetParams(),
         * or null if we will not throw.
         * @return the Throwable we will throw on getStylesheetParams(), or null if we will not throw.
         */
        Throwable getThrownFromGetStylesheetParams() {
            return this.thrownFromGetStylesheetParams;
        }
        
        /**
         * Set the Throwable we should throw on invocation of getStylesheetParams().
         * Set to null to configure not to throw on invocation of getStylesheetParams().
         * @param thrownFromGetStylesheetParams Exception to throw, or null not to throw.
         */
        void setThrownFromGetStylesheetParams(
                Exception thrownFromGetStylesheetParams) {
            this.thrownFromGetStylesheetParams = thrownFromGetStylesheetParams;
        }
        
        /**
         * Get the Throwable we will throw on invocations of getXml(), or null if
         * we will not throw.
         * @return the Exception we will throw on getXml() or null if we will not throw.
         */
        Exception getThrownFromGetXml() {
            return this.thrownFromGetXml;
        }
        
        /**
         * Set the Throwable we will throw on invocations of getXml().
         * Set to null to configure not to throw on invocations of
         * getXml() and instead return our document.
         * @param thrownFromGetXml Exception to throw or null to not throw.
         */
        void setThrownFromGetXml(Exception thrownFromGetXml) {
            this.thrownFromGetXml = thrownFromGetXml;
        }
        
        /**
         * Get the Throwable that we will throw on invocation of getXsltUri(), or
         * null if we will not throw.
         * @return the Throwable we will throw, or null if we will not throw.
         */
        Throwable getThrownFromGetXsltUri() {
            return this.thrownFromGetXsltUri;
        }
        
        /**
         * Set the Throwable that we will throw on getXsltUri() invocations.
         * When set to null, we will not throw but will instead return the String
         * we are configured to return.  When set to a non-null, we will throw
         * rather than return the String.
         * @param thrownFromGetXsltUri a Exception or null indicating do not throw.
         */
        void setThrownFromGetXsltUri(Exception thrownFromGetXsltUri) {
            this.thrownFromGetXsltUri = thrownFromGetXsltUri;
        }
        
        /**
         * Get the String that we will return on getXsltUri() invocations 
         * when we do not throw anything.
         * @return Returns the String we will return on getXsltUri() invocations.
         */
        String getXsltUriString() {
            return this.xsltUriString;
        }
        
        /**
         * Set the String that this Mock Object will return for invocations of
         * getXsltUri().  This setting will be overridden when we are configured to
         * throw a Throwable on getXsltUri() invocation.
         * @param xsltUriString The String we should return when we do not throw.
         */
        void setXsltUriString(String xsltUriString) {
            this.xsltUriString = xsltUriString;
        }
    }
    
    /**
     * A totally uninteresting stub ContentHandler that we use to test renderXML
     * exception handling behavior.
     */
    private static class DummyContentHandler 
        implements ContentHandler {

        public void setDocumentLocator(Locator locator) {
            // do nothing
        }

        public void startDocument() throws SAXException {
            // do nothing
        }

        public void endDocument() throws SAXException {
            // do nothing
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            // do nothing
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            // do nothing
        }

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            // do nothing
        }

        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            // do nothing
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            // do nothing
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            // do nothing
        }

        public void processingInstruction(String target, String data) throws SAXException {
            // do nothing
        }

        public void skippedEntity(String name) throws SAXException {
            // do nothing
        }
    }
    
}
