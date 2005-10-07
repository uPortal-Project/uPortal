/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.immutable;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.PortalException;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.LayoutEventListener;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;


/**
 * Wraps {@link IUserLayoutManager} interface to prevent access to mutator methods.
 *
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to associate it with the 
 * ImmutableUserLayoutSAXFilter and to separate it from the general Layout API.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
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

    public void getUserLayout(ContentHandler ch) throws PortalException {
        //todo: we should probably cache the output here and save a DOM transform - the user layout never changes here
        man.getUserLayout(new ImmutableUserLayoutSAXFilter(ch));
        //        man.getUserLayout(ch);
    }


    public void getUserLayout(String nodeId, ContentHandler ch) throws PortalException {
        man.getUserLayout(nodeId,new ImmutableUserLayoutSAXFilter(ch));
    }


    public void setLayoutStore(IUserLayoutStore ls) {
    }


    public void loadUserLayout() throws PortalException {
        man.loadUserLayout();
    }

    public void saveUserLayout() throws PortalException {}

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

    /**
     * A factory method to create an empty <code>IUserLayoutNodeDescription</code> instance
     *
     * @param nodeType a node type value
     * @return an <code>IUserLayoutNodeDescription</code> instance
     * @exception PortalException if the error occurs.
     */
    public IUserLayoutNodeDescription createNodeDescription( int nodeType ) throws PortalException {
         return man.createNodeDescription(nodeType);
    }

    public boolean addLayoutEventListener(LayoutEventListener l) {
        return false;
    }
    public boolean removeLayoutEventListener(LayoutEventListener l) {
        return false;
    }

    /**
     * Ignores this call to prevent changes to the layout.
     */
    public void processLayoutParameters(IPerson person, 
            UserPreferences userPrefs, 
            HttpServletRequest req) throws PortalException
    {
    }
}
