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
package org.jasig.portal.utils;

import java.util.Hashtable;
import java.util.Iterator;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
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

/**
 * An implementation of IPortalDocument that decorates a generic
 * <code>Document</code> object. This is used to locally store and manage
 * the ID element mappings regardless of the DOM implementation.
 *
 * @see org.w3c.dom.Document for decorator method descriptions.
 *
 * @author Nick Bolton
 * @version $Revision$
 */
public class PortalDocumentImpl implements IPortalDocument {

    private Hashtable identifiers = new Hashtable(1024);
    private final Hashtable keys = new Hashtable(1024);

    public Document document = null;

    PortalDocumentImpl() {
        document = DocumentFactory.__getNewDocument();
    }

    PortalDocumentImpl(Document doc) {
        document = doc;
    }
    
    public final Hashtable getIdentifiers() {
        return identifiers;   
    }
    
    public final void setIdentifiers( Hashtable identifiers ) {
        this.identifiers = identifiers;   
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
        if (element == null) {
            removeElement(key);
            return;
        }

        if (element.getOwnerDocument() != document) {
            StringBuffer msg = new StringBuffer();
            msg.append("Trying to cache an element that doesn't belong to ");
            msg.append("this document.");
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                msg.toString());
        }

        identifiers.put(key, element);
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
        for (Node n = this.getFirstChild(); n != null; n = n.getNextSibling()) {
            preserveCache(sourceDoc, n);
        }
    }

    private void removeElement(String key) {
        Element elem = getElementById(key);
        if ( elem != null )
         keys.remove(XML.serializeNode(elem));
        identifiers.remove(key); 
    }

    private void preserveCache(IPortalDocument sourceDoc, Node node) {
        if (node instanceof Element) {
            Element element = (Element) node;
            String serializedNode = XML.serializeNode(element);
            String key = ((PortalDocumentImpl)sourceDoc).
                getElementKey(serializedNode);

            if (key != null) {
                putIdentifier(key, element);
            }
        }

        node = node.getFirstChild();
        while (node != null) {
            preserveCache(sourceDoc, node);
            node = node.getNextSibling();
        }
    }

    private String getElementKey(String serializedNode) {
        String key = null;
        if ( keys.isEmpty() ) {
         Iterator itr = identifiers.keySet().iterator();   
         while (itr.hasNext()) {
            String id = (String) itr.next();
            Element element = (Element) identifiers.get(key);
            String value = XML.serializeNode(element);
            keys.put(value,id);
            if ( serializedNode.equals(value) )
             key = id;          
         }   
        } else 
            key = (String) keys.get(serializedNode); 
          return key;
    }

    // decorator methods

    /**
     * This method was overloaded to provide local element caching.
     */
    public Element getElementById(String key) {
        return (Element)identifiers.get(key);
    }

    public DocumentType getDoctype() {
        return document.getDoctype();
    }

    public DOMImplementation getImplementation() {
        return document.getImplementation();
    }

    public Element getDocumentElement() {
        return document.getDocumentElement();
    }

    public Element createElement(String tagName) throws DOMException {
        return document.createElement(tagName);
    }

    public DocumentFragment createDocumentFragment() {
        return document.createDocumentFragment();
    }

    public Text createTextNode(String data) {
        return document.createTextNode(data);
    }

    public Comment createComment(String data) {
        return document.createComment(data);
    }

    public CDATASection createCDATASection(String data) throws DOMException {
        return document.createCDATASection(data);
    }

    public ProcessingInstruction createProcessingInstruction(String target,
        String data) throws DOMException {
        return document.createProcessingInstruction(target, data);
    }

    public Attr createAttribute(String name) throws DOMException {
        return document.createAttribute(name);
    }

    public EntityReference createEntityReference(String name)
    throws DOMException {
        return document.createEntityReference(name);
    }

    public NodeList getElementsByTagName(String tagname) {
        return document.getElementsByTagName(tagname);
    }

    public Node importNode(Node importedNode, boolean deep)
    throws DOMException {
        return document.importNode(importedNode, deep);
    }

    public Element createElementNS(String namespaceURI, String qualifiedName)
    throws DOMException {
        return document.createElementNS(namespaceURI, qualifiedName);
    }

    public Attr createAttributeNS(String namespaceURI, String qualifiedName)
    throws DOMException {
        return document.createAttributeNS(namespaceURI, qualifiedName);
    }

    public NodeList getElementsByTagNameNS(String namespaceURI,
        String localName) {
        return document.getElementsByTagNameNS(namespaceURI, localName);
    }

    public String getNodeName() {
        return document.getNodeName();
    }

    public String getNodeValue() throws DOMException {
        return document.getNodeValue();
    }

    public void setNodeValue(String nodeValue) throws DOMException {
        document.setNodeValue(nodeValue);
    }

    public short getNodeType() {
        return document.getNodeType();
    }

    public Node getParentNode() {
        return document.getParentNode();
    }

    public NodeList getChildNodes() {
        return document.getChildNodes();
    }

    public Node getFirstChild() {
        return document.getFirstChild();
    }

    public Node getLastChild() {
        return document.getLastChild();
    }

    public Node getPreviousSibling() {
        return document.getPreviousSibling();
    }

    public Node getNextSibling() {
        return document.getNextSibling();
    }

    public NamedNodeMap getAttributes() {
        return document.getAttributes();
    }

    public Document getOwnerDocument() {
        return document.getOwnerDocument();
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return document.insertBefore(newChild, refChild);
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return document.replaceChild(newChild, oldChild);
    }

    public Node removeChild(Node oldChild) throws DOMException {
        return document.removeChild(oldChild);
    }

    public Node appendChild(Node newChild) throws DOMException {
        return document.appendChild(newChild);
    }

    public boolean hasChildNodes() {
        return document.hasChildNodes();
    }

    public Node cloneNode(boolean deep) {
        Document newDoc = (Document)document.cloneNode(deep);
        PortalDocumentImpl newNode = new PortalDocumentImpl(newDoc);

        // only copy the identifiers if it's a deep cloning. Otherwise,
        // the children won't exist and you'd have an identifier mapping
        // that was invalid.
        if (deep) {
            //newNode.copyCache(this);
            newNode.setIdentifiers((Hashtable)identifiers.clone());
        }
        return newNode;
    }

    public void normalize() {
        document.normalize();
    }

    public boolean isSupported(String feature, String version) {
        return document.isSupported(feature, version);
    }

    public String getNamespaceURI() {
        return document.getNamespaceURI();
    }

    public String getPrefix() {
        return document.getPrefix();
    }

    public void setPrefix(String prefix) throws DOMException {
        document.setPrefix(prefix);
    }

    public String getLocalName() {
        return document.getLocalName();
    }

    public boolean hasAttributes() {
        return document.hasAttributes();
    }

    // used for debugging

    void checkCache() {
        String key;
        Element element;

        System.out.println("CHECKING CACHE for: " + this + " (" +
            this.hashCode() + ")");

        Iterator itr = identifiers.keySet().iterator();
        while (itr.hasNext()) {
            key = (String)itr.next();
            element = (Element)identifiers.get(key);
            if (element.getOwnerDocument() != document) {
                System.out.println("ERROR: element does not belong to this document: " + key);
            }
        }
        System.out.println("DONE CHECKING CACHE for: " + this + " (" +
            this.hashCode() + ")\n");
    }

    void checkCaches(PortalDocumentImpl doc2) {
        String key;
        Element element1;
        Element element2;
        String xml1;
        String xml2;

        System.out.println("CHECKING CACHES for: " + this + " (" +
            this.hashCode() + ") and " + doc2 + "( " + doc2.hashCode() + ")");

        this.checkCache();
        doc2.checkCache();

        Iterator itr = this.identifiers.keySet().iterator();
        while (itr.hasNext()) {
            key = (String)itr.next();
            element1 = (Element)this.identifiers.get(key);
            element2 = (Element)doc2.identifiers.get(key);
            if (element2 == null) {
                System.out.println(
                    "ERROR: Mapping does not exist in doc2 for key: " + key);
                continue;
            }
            xml1 = XML.serializeNode(element1);
            xml2 = XML.serializeNode(element2);
            if (!xml1.equals(xml2)) {
                System.out.println("ERROR: xml differs for key: " + key);
                System.out.println("xml1...\n" + xml1);
                System.out.println("xml2...\n" + xml2);
            } else {
                System.out.println("ok key: " + key);
            }
        }
        System.out.println("DONE CHECKING CACHES for: " + this + " (" +
            this.hashCode() + ") and " + doc2 + "( " + doc2.hashCode() + ")");
    }

    void dumpCache() {
        String key;
        Node node;
        System.out.println("Element Map size: " + identifiers.size());

        Iterator itr = identifiers.keySet().iterator();
        while(itr.hasNext()) {
            key = (String)itr.next();
            node = (Node)identifiers.get(key);
            System.out.println("key/node: " + key + "/" + node +
                " (" + node.hashCode() + ")");
        }
    }
}
