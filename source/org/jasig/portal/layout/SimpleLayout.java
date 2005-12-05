/* Copyright 2003,2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout;

import java.util.Enumeration;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.utils.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;

/**
 * The simple user layout implementation. This
 * layout is based on a Document.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class SimpleLayout implements IUserLayout {
    
    private Document layout;
    private String layoutId;
    private String cacheKey;

    private Log log = LogFactory.getLog(getClass());
    

    public SimpleLayout(String layoutId, Document layout) {
        this.layoutId = layoutId;
        this.layout = layout;
    }

    public void writeTo(ContentHandler ch) throws PortalException {
        try {
            XML.dom2sax(layout, ch);
        } catch (Exception e) {
            throw new PortalException(e);
        }
    }

    public void writeTo(String nodeId, ContentHandler ch) throws PortalException {
        try {
            XML.dom2sax(layout.getElementById(nodeId), ch);
        } catch (Exception e) {
            throw new PortalException(e);
        }
    }

    public void writeTo(Document document) throws PortalException {
        document.appendChild(document.importNode(layout.getDocumentElement(), true));
    }

    public void writeTo(String nodeId, Document document) throws PortalException {
        document.appendChild(document.importNode(layout.getElementById(nodeId), true));
    }

    public IUserLayoutNodeDescription getNodeDescription(String nodeId) throws PortalException {
        Element element = (Element) layout.getElementById(nodeId);
        return UserLayoutNodeDescription.createUserLayoutNodeDescription(element);
    }

    public String getParentId(String nodeId) throws PortalException {
        String parentId = null;
        Element element = (Element)layout.getElementById(nodeId);
        if (element != null) {
            Node parent = element.getParentNode();
            if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
                Element parentE = (Element)parent;
                parentId = parentE.getAttribute("ID");
            }
        }
        return parentId;
    }

    public Enumeration getChildIds(String nodeId) throws PortalException {
        Vector v = new Vector();
        IUserLayoutNodeDescription node = getNodeDescription(nodeId);
        if (node instanceof IUserLayoutFolderDescription) {
            Element element = (Element)layout.getElementById(nodeId);
            for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element)n;
                    if (e.getAttribute("ID") != null) {
                        v.add(e.getAttribute("ID"));
                    }
                }
            }
        }
        return v.elements();
    }

    public String getNextSiblingId(String nodeId) throws PortalException {
        String nextSiblingId = null;
        Element element = (Element)layout.getElementById(nodeId);
        if (element != null) {
            Node sibling = element.getNextSibling();
            // Find the next element node
            while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) {
                sibling = sibling.getNextSibling();
            }
            if (sibling != null) {
                Element e = (Element)sibling;
                nextSiblingId = e.getAttribute("ID");
            }
        }
        return nextSiblingId;
    }

    public String getPreviousSiblingId(String nodeId) throws PortalException {
        String prevSiblingId = null;
        Element element = (Element)layout.getElementById(nodeId);
        if (element != null) {
            Node sibling = element.getPreviousSibling();
            // Find the previous element node
            while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) {
                sibling = sibling.getPreviousSibling();
            }
            if (sibling != null) {
                Element e = (Element)sibling;
                prevSiblingId = e.getAttribute("ID");
            }
        }
        return prevSiblingId;
    }

    public String getCacheKey() throws PortalException {
        return cacheKey;
    }

    public boolean addLayoutEventListener(LayoutEventListener l) {
        // TODO: Implement this!
        return false;
    }

    public boolean removeLayoutEventListener(LayoutEventListener l) {
        // TODO: Implement this!
        return false;
    }

    public String getId() {
        return layoutId;
    }

    public String getNodeId(String fname) throws PortalException {
        String nodeId = null;
        NodeList nl = layout.getElementsByTagName("channel");
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element channelE = (Element)node;
                if (fname.equals(channelE.getAttribute("fname"))) {
                    nodeId = channelE.getAttribute("ID");
                    break;
                }
            }
        }
        return nodeId;
    }

    public Enumeration getNodeIds() throws PortalException {
        Vector v = new Vector();
        try {
            String expression = "*";
            XPathFactory fac = XPathFactory.newInstance();
            XPath xpath = fac.newXPath();
            NodeList nl = (NodeList) xpath.evaluate(expression, layout, 
                    XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element)node;
                    v.add(e.getAttribute("ID"));
                }
            }
        } catch (Exception e) {
            log.error("Exception getting node ids.", e);
        }
        return v.elements();
    }

    public String getRootId() {
        String rootNode = null;
        try {
            
            String expression = "/layout/folder";
            XPathFactory fac = XPathFactory.newInstance();
            XPath xpath = fac.newXPath();
            Element rootNodeE = (Element) xpath.evaluate(expression, layout, 
                    XPathConstants.NODESET);
            
            rootNode = rootNodeE.getAttribute("ID");
        } catch (Exception e) {
            log.error("Error getting root id.", e);
        }
        return rootNode;
    }

}
