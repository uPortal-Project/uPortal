/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout.simple;

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
import org.apereo.portal.PortalException;
import org.apereo.portal.layout.INodeIdResolver;
import org.apereo.portal.layout.IUserLayout;
import org.apereo.portal.layout.dlm.DistributedUserLayout;
import org.apereo.portal.layout.node.IUserLayoutFolderDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.layout.node.UserLayoutNodeDescription;
import org.apereo.portal.layout.om.IStylesheetUserPreferences;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The simple user layout implementation. This layout is based on a Document.
 *
 * <p>Prior to uPortal 2.5, this class existed in the org.apereo.portal.layout package. It was moved
 * to its present package to reflect that it is part of the Simple Layout Management implementation.
 *
 */
public class SimpleLayout implements IUserLayout {

    private final DistributedUserLayout userLayout;
    private final Document layout;
    private final String layoutId;

    private final Log log = LogFactory.getLog(getClass());

    public SimpleLayout(DistributedUserLayout userLayout, String layoutId) {
        this.userLayout = userLayout;
        this.layout = this.userLayout.getLayout();
        this.layoutId = layoutId;
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
                Element parentE = (Element) parent;
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
                    Element e = (Element) n;
                    if (e.getAttribute("ID") != null) {
                        v.add(e.getAttribute("ID"));
                    }
                }
            }
        }
        return v.elements();
    }

    @Override
    public String getId() {
        return layoutId;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.IUserLayout#findNodeId(javax.xml.xpath.XPathExpression)
     */
    @Override
    public String findNodeId(XPathExpression xpathExpression) throws PortalException {
        try {
            return xpathExpression.evaluate(this.layout);
        } catch (XPathExpressionException e) {
            throw new PortalException(
                    "Exception while executing XPathExpression: " + xpathExpression, e);
        }
    }

    @Override
    public String findNodeId(INodeIdResolver finder) {
        return finder.traverseDocument(this.layout);
    }

    @Override
    public String getRootId() {
        String rootNode = null;
        try {

            String expression = "/layout/folder";
            XPathFactory fac = XPathFactory.newInstance();
            XPath xpath = fac.newXPath();
            Element rootNodeE = (Element) xpath.evaluate(expression, layout, XPathConstants.NODE);

            rootNode = rootNodeE.getAttribute("ID");
        } catch (Exception e) {
            log.error("Error getting root id.", e);
        }
        return rootNode;
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
