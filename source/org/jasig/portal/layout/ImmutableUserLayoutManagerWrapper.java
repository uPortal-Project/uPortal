/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.layout;

import java.util.List;

import org.apache.xerces.dom.DocumentImpl;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;


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

    public boolean addLayoutEventListener(LayoutEventListener l){
        return false;
    }
    public boolean removeLayoutEventListener(LayoutEventListener l){
        return false;
    };
}
