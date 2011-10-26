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

package org.jasig.portal.layout.immutable;

import java.util.Enumeration;
import java.util.Set;

import javax.xml.stream.XMLEventReader;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription.LayoutNodeType;
import org.w3c.dom.Document;


/**
 * Wraps {@link IUserLayoutManager} interface to prevent access to mutator methods.
 *
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to associate it with the 
 * ImmutableUserLayoutSAXFilter and to separate it from the general Layout API.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0 $Revision$ $Date$
 */
public class ImmutableUserLayoutManagerWrapper implements IUserLayoutManager {
    IUserLayoutManager man=null;
    private String cacheKey=null;

    public ImmutableUserLayoutManagerWrapper(IUserLayoutManager manager) throws PortalException {
        this.man=manager;
        if(man==null) {
            throw new PortalException("Can not wrap a null IUserLayoutManager !");
        }
    }

    public IUserLayout getUserLayout() throws PortalException {
        return man.getUserLayout();
    }

    public void setUserLayout(IUserLayout userLayout) throws PortalException {
    }
    
    @Override
    public XMLEventReader getUserLayoutReader() {
        final XMLEventReader userLayoutReader = man.getUserLayoutReader();
        return new ImmutableUserLayoutXMLEventReader(userLayoutReader);
    }

    public void setLayoutStore(IUserLayoutStore ls) {
    }


    public void loadUserLayout() throws PortalException {
        man.loadUserLayout();
    }
    
    public void loadUserLayout(boolean reload) throws PortalException {
        man.loadUserLayout(reload);
    }

    public void saveUserLayout() throws PortalException {}
    
    @Override
    public Set<String> getAllSubscribedChannels() {
        return man.getAllSubscribedChannels();
    }

    public IUserLayoutNodeDescription getNode(String nodeId) throws PortalException {
        return man.getNode(nodeId);
    }

    public IUserLayoutNodeDescription addNode(IUserLayoutNodeDescription node, String parentId, String nextSiblingId) throws PortalException {
        return null;
    }

    public boolean moveNode(String nodeId, String parentId, String nextSiblingId) throws PortalException {
        return false;
    }

    public boolean deleteNode(String nodeId) throws PortalException {
        return false;
    }

    public boolean updateNode(IUserLayoutNodeDescription node) throws PortalException {
        return false;
    }


    public boolean canAddNode(IUserLayoutNodeDescription node, String parentId, String nextSiblingId) throws PortalException {
        return false;
    }

    public boolean canMoveNode(String nodeId, String parentId, String nextSiblingId) throws PortalException {
        return false;
    }

    public boolean canDeleteNode(String nodeId) throws PortalException {
        return false;
    }

    public boolean canUpdateNode(IUserLayoutNodeDescription nodeId) throws PortalException {
        return false;
    }

    public void markAddTargets(IUserLayoutNodeDescription node) {}

    public void markMoveTargets(String nodeId) throws PortalException {}

    public String getParentId(String nodeId) throws PortalException {
        return man.getParentId(nodeId);
    }

    public Enumeration getChildIds(String nodeId) throws PortalException {
        return man.getChildIds(nodeId);
    }

    public String getNextSiblingId(String nodeId) throws PortalException {
        return man.getNextSiblingId(nodeId);
    }

    public String getPreviousSiblingId(String nodeId) throws PortalException {
        return man.getPreviousSiblingId(nodeId);
    }


    public String getCacheKey() throws PortalException {
        // remember the cache key, since it never changes
        if(this.cacheKey==null) {
            cacheKey=man.getCacheKey();
        }
        return this.cacheKey;
    }

     // temp methods, to be removed (getDOM() might actually stay)
     // This method should be removed whenever it becomes possible
    public void setUserLayoutDOM(Document doc) {}

    // This method should be removed whenever it becomes possible
    public Document getUserLayoutDOM() throws PortalException {
        return man.getUserLayoutDOM();
    }

    public int getLayoutId() {
        return man.getLayoutId();
    }

    public String getRootFolderId() {
        return man.getRootFolderId();
    }
    
	/**
				 * Returns the depth of a node in the layout tree.
				 *
				 * @param nodeId a <code>String</code> value
				 * @return a depth value
				 * @exception PortalException if an error occurs
				 */
	 public int getDepth(String nodeId) throws PortalException {
		return man.getDepth(nodeId);
	 }


    /**
     * Returns a subscription id given a functional name.
     *
     * @param fname  the functional name to lookup.
     * @return a <code>String</code> subscription id.
     */
    public String getSubscribeId(String fname) throws PortalException {
        return man.getSubscribeId(fname);
    }

    /*
	 * @see org.jasig.portal.layout.IUserLayoutManager#getSubscribeId(java.lang.String, java.lang.String)
	 */
	public String getSubscribeId(String parentFolderId, String fname) {
		return man.getSubscribeId(parentFolderId, fname);
	}

	/**
     * A factory method to create an empty <code>IUserLayoutNodeDescription</code> instance
     *
     * @param nodeType a node type value
     * @return an <code>IUserLayoutNodeDescription</code> instance
     * @exception PortalException if the error occurs.
     */
    public IUserLayoutNodeDescription createNodeDescription( LayoutNodeType nodeType ) throws PortalException {
         return man.createNodeDescription(nodeType);
    }
}
