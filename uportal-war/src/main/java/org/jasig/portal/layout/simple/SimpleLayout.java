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

package org.jasig.portal.layout.simple;

import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.INodeIdResolver;
import org.jasig.portal.layout.dlm.DistributedUserLayout;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.UserLayoutNodeDescription;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The simple user layout implementation. This
 * layout is based on a Document.
 * 
 * Prior to uPortal 2.5, this class existed in the org.jasig.portal.layout package.
 * It was moved to its present package to reflect that it is part of the
 * Simple Layout Management implementation.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class SimpleLayout implements IUserLayout {
    
    private final DistributedUserLayout userLayout;
    private final Document layout;
    private final String layoutId;
    private final String cacheKey;
    
    private final Log log = LogFactory.getLog(getClass());
    
    public SimpleLayout(DistributedUserLayout userLayout, String layoutId, String cacheKey) {
        this.userLayout = userLayout;
        this.layout = this.userLayout.getLayout();
        this.layoutId = layoutId;
        this.cacheKey = cacheKey;
    }

    @Override
    public void writeTo(Document document) throws PortalException {
        document.appendChild(document.importNode(layout.getDocumentElement(), true));
    }

    @Override
    public void writeTo(String nodeId, Document document) throws PortalException {
        document.appendChild(document.importNode(layout.getElementById(nodeId), true));
    }

    @Override
    public IUserLayoutNodeDescription getNodeDescription(String nodeId) throws PortalException {
        Element element = layout.getElementById(nodeId);
        return UserLayoutNodeDescription.createUserLayoutNodeDescription(element);
    }

    @Override
    public String getParentId(String nodeId) throws PortalException {
        String parentId = null;
        Element element = layout.getElementById(nodeId);
        if (element != null) {
            Node parent = element.getParentNode();
            if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
                Element parentE = (Element)parent;
                parentId = parentE.getAttribute("ID");
            }
        }
        return parentId;
    }

    @Override
    public Enumeration getChildIds(String nodeId) throws PortalException {
        Vector v = new Vector();
        IUserLayoutNodeDescription node = getNodeDescription(nodeId);
        if (node instanceof IUserLayoutFolderDescription) {
            Element element = layout.getElementById(nodeId);
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

    @Override
    public String getNextSiblingId(String nodeId) throws PortalException {
        String nextSiblingId = null;
        Element element = layout.getElementById(nodeId);
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

    @Override
    public String getPreviousSiblingId(String nodeId) throws PortalException {
        String prevSiblingId = null;
        Element element = layout.getElementById(nodeId);
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

    @Override
    public String getCacheKey() throws PortalException {
        return cacheKey;
    }

    @Override
    public String getId() {
        return layoutId;
    }

    @Override
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
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.IUserLayout#findNodeId(javax.xml.xpath.XPathExpression)
     */
    @Override
    public String findNodeId(XPathExpression xpathExpression) throws PortalException {
        try {
            return xpathExpression.evaluate(this.layout);
        }
        catch (XPathExpressionException e) {
            throw new PortalException("Exception while executing XPathExpression: " + xpathExpression, e);
        }
    }
    
    @Override
    public String findNodeId(INodeIdResolver finder) {
        return finder.traverseDocument(this.layout);
    }

    @Override
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

    @Override
    public String getRootId() {
        String rootNode = null;
        try {
            
            String expression = "/layout/folder";
            XPathFactory fac = XPathFactory.newInstance();
            XPath xpath = fac.newXPath();
            Element rootNodeE = (Element) xpath.evaluate(expression, layout, 
                    XPathConstants.NODE);
            
            rootNode = rootNodeE.getAttribute("ID");
        } catch (Exception e) {
            log.error("Error getting root id.", e);
        }
        return rootNode;
    }

    @Override
    public Set<String> getFragmentNames() {
        return this.userLayout.getFragmentNames();
    }

    @Override
    public IStylesheetUserPreferences getDistributedStructureStylesheetUserPreferences() {
        return this.userLayout.getDistributedStructureStylesheetUserPreferences();
    }

    @Override
    public IStylesheetUserPreferences getDistributedThemeStylesheetUserPreferences() {
        return this.userLayout.getDistributedThemeStylesheetUserPreferences();
    }
}
