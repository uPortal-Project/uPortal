package org.jasig.portal.layout;

import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.xml.sax.ContentHandler;
import java.util.List;

import  org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;


/**
 * Wraps {@link IUserLayoutManager} interface to prevent access to mutator methods.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
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

    public UserLayoutNodeDescription getNode(String nodeId) throws PortalException {
        return man.getNode(nodeId);
    }

    public UserLayoutNodeDescription addNode(UserLayoutNodeDescription node, String parentId, String nextSiblingId) throws PortalException {
        return null;
    }

    public boolean moveNode(String nodeId, String parentId, String nextSiblingId) throws PortalException {
        return false;
    }

    public boolean deleteNode(String nodeId) throws PortalException {
        return false;
    }

    public boolean updateNode(UserLayoutNodeDescription node) throws PortalException {
        return false;
    }


    public boolean canAddNode(UserLayoutNodeDescription node, String parentId, String nextSiblingId) throws PortalException {
        return false;
    }

    public boolean canMoveNode(String nodeId, String parentId, String nextSiblingId) throws PortalException {
        return false;
    }

    public boolean canDeleteNode(String nodeId) throws PortalException {
        return false;
    }

    public boolean canUpdateNode(String nodeId) throws PortalException {
        return false;
    }

    public void markAddTargets(UserLayoutNodeDescription node) {}

    public void markMoveTargets(String nodeId) throws PortalException {}

    public String getParentId(String nodeId) throws PortalException {
        return man.getParentId(nodeId);
    }

    public List getChildIds(String nodeId) throws PortalException {
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
    public void setUserLayoutDOM(DocumentImpl doc) {}

    // This method should be removed whenever it becomes possible
    public Document getUserLayoutDOM() throws PortalException {
        return man.getUserLayoutDOM();
    }

    public int getLayoutId() {
        return man.getLayoutId();
    }
}
