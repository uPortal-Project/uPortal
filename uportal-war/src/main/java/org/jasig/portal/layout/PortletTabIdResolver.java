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
package org.jasig.portal.layout;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class mimics the behaviour of XPath expression '/layout/folder/folder[@ID=$nodeId or
 * descendant::node()[@ID=$nodeId]]/@ID' - it searches for identifier of tab that contains a node with given identifier.
 * The rationale behind this method is because it is much faster using DOM introspection (~0.1ms), than XPath queries
 * (0-60ms <a href="http://stackoverflow.com/questions/6340802/java-xpath-apache-jaxp-implementation-performance">
 * without optimization</a>).
 * 
 * @author Arvīds Grabovskis
 */
public class PortletTabIdResolver implements INodeIdResolver {
    private final String layoutNodeId;

    public PortletTabIdResolver(String layoutNodeId) {
        this.layoutNodeId = layoutNodeId;
    }

    @Override
    public String traverseDocument(Document document) {
        // '/layout' - layouts
        for (Node root = document.getFirstChild(); root != null; root = root.getNextSibling()) {
            // '/layout/folder' - root/header/footer folders
            for (Node rootFolder = root.getFirstChild(); rootFolder != null; rootFolder = rootFolder.getNextSibling()) {
                // '/layout/folder/folder' - tabs
                for (Node tab = rootFolder.getFirstChild(); tab != null; tab = tab.getNextSibling()) {
                    if (containsElmentWithId(tab, layoutNodeId)) {
                        return ((Element) tab).getAttribute("ID");
                    }
                }
            }
        }
        return null;
    }

    /**
     * Recursevly find out whether node contains a folder or channel with given identifier.
     * 
     * @param node Where to search.
     * @param id Identifier to search for.
     * @return true if node or any of its descendats contain an element with given identifier, false otherwise.
     */
    private boolean containsElmentWithId(Node node, String id) {
        String nodeName = node.getNodeName();
        if ("channel".equals(nodeName) || "folder".equals(nodeName)) {
            Element e = (Element) node;
            if (id.equals(e.getAttribute("ID"))) {
                return true;
            }
            if ("folder".equals(nodeName)) {
                for (Node child = e.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (containsElmentWithId(child, id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}