/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

/**
 * An implementation of IPortalDocument that wraps a standard
 * <code>Document</code> object.
 *
 * @see org.w3c.dom.Document for decorator method descriptions.
 *
 * @author Nick Bolton
 * @version $Revision$
 * @deprecated use w3c DOM level 3 Documents directly instead.
 */
public class PortalDocumentImpl implements IPortalDocument {

    private static final Log log = LogFactory.getLog(PortalDocumentImpl.class);
    
    public Document document = null;

    PortalDocumentImpl() {
        try {
            this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (Exception e) {
            log.fatal("Error instantiating a Document.", e);
            throw new RuntimeException("Error instantiating a document.", e);
        }
    }

    PortalDocumentImpl(Document doc) {
        this.document = doc;
    }
    
    /**
     * Get a Hashtable mapping from identifier Strings to the identified nodes.
     * @return a Hashtable from Strings to nodes.
     */
    public final Hashtable getIdentifiers() {
        
        // TODO implement this
        // likely implementation is to traverse Document, read all identifiers, and
        // generate the Hashtable.
        
        // log a stack trace that will help us figure out who is calling us.
        log.fatal("Invocation of PortalDocumentImpl.getIdentifiers()", new RuntimeException());
        
        return new Hashtable(); 
    }
    
    public final void setIdentifiers( Hashtable identifiers ) {
         log.fatal("Invocation of PortalDocumentImpl.setIdentifiers()", new RuntimeException());
         throw new UnsupportedOperationException("setIdentifiers not supported.");
    }
    
    /**
     * Registers an identifier name with a specified element.
     *
     * @param key a key used to store an <code>Element</code> object.
     * @param element an <code>Element</code> object to map.
     * @exception DOMException if the element does not belong to the
     * document.
     */
    public void putIdentifier(String key, Element element)
    throws DOMException {
        throw new UnsupportedOperationException("Attempt to put identifier [" + key + "]");
    }

    /**
     * Copies the element cache from the source document. This will
     * provide equivalent mappings from IDs to elements in this
     * document provided the elements exist in the source document.
     * If no element exists, it will be skipped.
     *
     * @param sourceDoc The source doc to copy from.
     */
    public void copyCache(IPortalDocument sourceDoc) {
        throw new UnsupportedOperationException("copyCache() no longer implemented.");
    }

  


    // decorator methods

 
    public Element getElementById(String key) {
        return this.document.getElementById(key);
    }

    public DocumentType getDoctype() {
        return this.document.getDoctype();
    }

    public DOMImplementation getImplementation() {
        return this.document.getImplementation();
    }

    public Element getDocumentElement() {
        return this.document.getDocumentElement();
    }

    public Element createElement(String tagName) throws DOMException {
        return this.document.createElement(tagName);
    }

    public DocumentFragment createDocumentFragment() {
        return this.document.createDocumentFragment();
    }

    public Text createTextNode(String data) {
        return this.document.createTextNode(data);
    }

    public Comment createComment(String data) {
        return this.document.createComment(data);
    }

    public CDATASection createCDATASection(String data) throws DOMException {
        return this.document.createCDATASection(data);
    }

    public ProcessingInstruction createProcessingInstruction(String target,
        String data) throws DOMException {
        return this.document.createProcessingInstruction(target, data);
    }

    public Attr createAttribute(String name) throws DOMException {
        return this.document.createAttribute(name);
    }

    public EntityReference createEntityReference(String name)
        throws DOMException {
        
        return this.document.createEntityReference(name);
    }

    public NodeList getElementsByTagName(String tagname) {
        return this.document.getElementsByTagName(tagname);
    }

    public Node importNode(Node importedNode, boolean deep)
    throws DOMException {
        return this.document.importNode(importedNode, deep);
    }

    public Element createElementNS(String namespaceURI, String qualifiedName)
    throws DOMException {
        return this.document.createElementNS(namespaceURI, qualifiedName);
    }

    public Attr createAttributeNS(String namespaceURI, String qualifiedName)
    throws DOMException {
        return this.document.createAttributeNS(namespaceURI, qualifiedName);
    }

    public NodeList getElementsByTagNameNS(String namespaceURI,
        String localName) {
        return this.document.getElementsByTagNameNS(namespaceURI, localName);
    }

    public String getNodeName() {
        return this.document.getNodeName();
    }

    public String getNodeValue() throws DOMException {
        return this.document.getNodeValue();
    }

    public void setNodeValue(String nodeValue) throws DOMException {
        this.document.setNodeValue(nodeValue);
    }

    public short getNodeType() {
        return this.document.getNodeType();
    }

    public Node getParentNode() {
        return this.document.getParentNode();
    }

    public NodeList getChildNodes() {
        return this.document.getChildNodes();
    }

    public Node getFirstChild() {
        return this.document.getFirstChild();
    }

    public Node getLastChild() {
        return this.document.getLastChild();
    }

    public Node getPreviousSibling() {
        return this.document.getPreviousSibling();
    }

    public Node getNextSibling() {
        return this.document.getNextSibling();
    }

    public NamedNodeMap getAttributes() {
        return this.document.getAttributes();
    }

    public Document getOwnerDocument() {
        return this.document.getOwnerDocument();
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return this.document.insertBefore(newChild, refChild);
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return this.document.replaceChild(newChild, oldChild);
    }

    public Node removeChild(Node oldChild) throws DOMException {
        return this.document.removeChild(oldChild);
    }

    public Node appendChild(Node newChild) throws DOMException {
        return this.document.appendChild(newChild);
    }

    public boolean hasChildNodes() {
        return this.document.hasChildNodes();
    }

    public Node cloneNode(boolean deep) {
        return this.document.cloneNode(deep);
    }

    public void normalize() {
        this.document.normalize();
    }

    public boolean isSupported(String feature, String version) {
        return this.document.isSupported(feature, version);
    }

    public String getNamespaceURI() {
        return this.document.getNamespaceURI();
    }

    public String getPrefix() {
        return this.document.getPrefix();
    }

    public void setPrefix(String prefix) throws DOMException {
        this.document.setPrefix(prefix);
    }

    public String getLocalName() {
        return this.document.getLocalName();
    }

    public boolean hasAttributes() {
        return this.document.hasAttributes();
    }
    
    public String toString() {
        return XML.serializeNode(this);
    }

    public Node adoptNode(Node arg0) throws DOMException {
        return this.document.adoptNode(arg0);
    }

    public short compareDocumentPosition(Node arg0) throws DOMException {
        return this.document.compareDocumentPosition(arg0);
    }
 
    public boolean equals(Object obj) {
        return this.document.equals(obj);
    }

    public String getBaseURI() {
        return this.document.getBaseURI();
    }

    public String getDocumentURI() {
        return this.document.getDocumentURI();
    }

    public DOMConfiguration getDomConfig() {
        return this.document.getDomConfig();
    }

    public Object getFeature(String arg0, String arg1) {
        return this.document.getFeature(arg0, arg1);
    }

    public String getInputEncoding() {
        return this.document.getInputEncoding();
    }

    public boolean getStrictErrorChecking() {
        return this.document.getStrictErrorChecking();
    }
 
    public String getTextContent() throws DOMException {
        return this.document.getTextContent();
    }

    public Object getUserData(String arg0) {
        return this.document.getUserData(arg0);
    }

    public String getXmlEncoding() {
        return this.document.getXmlEncoding();
    }

    public boolean getXmlStandalone() {
        return this.document.getXmlStandalone();
    }

    public String getXmlVersion() {
        return this.document.getXmlVersion();
    }

    public int hashCode() {
        return this.document.hashCode();
    }

    public boolean isDefaultNamespace(String arg0) {
        return this.document.isDefaultNamespace(arg0);
    }

    public boolean isEqualNode(Node arg0) {
        return this.document.isEqualNode(arg0);
    }

    public boolean isSameNode(Node arg0) {
        return this.document.isSameNode(arg0);
    }

    public String lookupNamespaceURI(String arg0) {
        return this.document.lookupNamespaceURI(arg0);
    }

    public String lookupPrefix(String arg0) {
        return this.document.lookupPrefix(arg0);
    }

    public void normalizeDocument() {
        this.document.normalizeDocument();
    }

    public Node renameNode(Node arg0, String arg1, String arg2)
            throws DOMException {
        return this.document.renameNode(arg0, arg1, arg2);
    }

    public void setDocumentURI(String arg0) {
        this.document.setDocumentURI(arg0);
    }

    public void setStrictErrorChecking(boolean arg0) {
        this.document.setStrictErrorChecking(arg0);
    }

    public void setTextContent(String arg0) throws DOMException {
        this.document.setTextContent(arg0);
    }

    public Object setUserData(String arg0, Object arg1, UserDataHandler arg2) {
        return this.document.setUserData(arg0, arg1, arg2);
    }
    
    public void setXmlStandalone(boolean arg0) throws DOMException {
        this.document.setXmlStandalone(arg0);
    }
    
    public void setXmlVersion(String arg0) throws DOMException {
        this.document.setXmlVersion(arg0);
    }
}
