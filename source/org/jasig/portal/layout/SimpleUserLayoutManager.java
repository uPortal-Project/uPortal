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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.apache.xerces.dom.DocumentImpl;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * An implementation of a user layout manager that uses 2.0-release store implementations.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public class SimpleUserLayoutManager implements IUserLayoutManager {
    protected final IPerson owner;
    protected final UserProfile profile;
    protected IUserLayoutStore store=null;
    protected Set listeners=new HashSet();

    protected DocumentImpl userLayoutDocument=null;

    protected static Random rnd=new Random();
    protected String cacheKey="initialKey";

    private boolean dirtyState=false;

    public SimpleUserLayoutManager(IPerson owner, UserProfile profile, IUserLayoutStore store) throws PortalException {
        if(owner==null) {
            throw new PortalException("A non-null owner needs to be specified.");
        }

        if(profile==null) {
            throw new PortalException("A non-null profile needs to be specified.");
        }

        this.owner=owner;
        this.profile=profile;
        this.setLayoutStore(store);
        this.loadUserLayout();
    }


    // This method should be removed whenever it becomes possible
    private void setUserLayoutDOM(Document doc) {
        this.userLayoutDocument= (DocumentImpl) doc;
        this.updateCacheKey();
    }
    // This method should be removed whenever it becomes possible
    public Document getUserLayoutDOM() {
        return this.userLayoutDocument;
    }

    public void getUserLayout(ContentHandler ch) throws PortalException {
        Document ulm=this.getUserLayoutDOM();
        if(ulm==null) {
            throw new PortalException("User layout has not been initialized");
        } else {
            getUserLayout(ulm,ch);
        }
    }

    public void getUserLayout(String nodeId, ContentHandler ch) throws PortalException {
        Document ulm=this.getUserLayoutDOM();

        if(ulm==null) {
            throw new PortalException("User layout has not been initialized");
        } else {
            Node rootNode=ulm.getElementById(nodeId);
            if(rootNode==null) {
                throw new PortalException("A requested root node (with id=\""+nodeId+"\") is not in the user layout.");
            } else {
                getUserLayout(rootNode,ch);
            }
        }
    }

    protected void getUserLayout(Node n,ContentHandler ch) throws PortalException {
        // do a DOM2SAX transformation
        try {
            Transformer emptyt=TransformerFactory.newInstance().newTransformer();
            emptyt.transform(new DOMSource(n), new SAXResult(ch));
        } catch (Exception e) {
            throw new PortalException("Encountered an exception trying to output user layout",e);
        }
    }


    public void setLayoutStore(IUserLayoutStore store) {
        this.store=store;
    }

    protected IUserLayoutStore getLayoutStore() {
        return this.store;
    }


    public void loadUserLayout() throws PortalException {
        if(this.getLayoutStore()==null) {
            throw new PortalException("Store implementation has not been set.");
        } else {
            try {
                DocumentImpl uli=(DocumentImpl)this.getLayoutStore().getUserLayout(this.owner,this.profile);
                if(uli!=null) {
                    this.setUserLayoutDOM(uli);
                    clearDirtyFlag();
                    // inform listeners
                    for(Iterator i=listeners.iterator();i.hasNext();) {
                        LayoutEventListener lel=(LayoutEventListener)i.next();
                        lel.layoutLoaded();
                    }

                } else {
                    throw new PortalException("Null user layout returned for ownerId=\""+owner.getID()+"\", profileId=\""+profile.getProfileId()+"\", layoutId=\""+profile.getLayoutId()+"\"");
                }
            } catch (PortalException pe) {
                throw pe;
            } catch (Exception e) {
                throw new PortalException("Exception encountered while reading a layout for userId="+this.owner.getID()+", profileId="+this.profile.getProfileId(),e);
            }
        }
    }

    public void saveUserLayout() throws PortalException{
        if(isLayoutDirty()) {
            Document ulm=this.getUserLayoutDOM();
            if(ulm==null) {
                throw new PortalException("UserLayout has not been initialized.");
            } else {
                if(this.getLayoutStore()==null) {
                    throw new PortalException("Store implementation has not been set.");
                } else {
                    try {
                        this.getLayoutStore().setUserLayout(this.owner,this.profile,ulm,true);
                        // inform listeners
                        for(Iterator i=listeners.iterator();i.hasNext();) {
                            LayoutEventListener lel=(LayoutEventListener)i.next();
                            lel.layoutSaved();
                        }
                    } catch (PortalException pe) {
                        throw pe;
                    } catch (Exception e) {
                        throw new PortalException("Exception encountered while trying to save a layout for userId="+this.owner.getID()+", profileId="+this.profile.getProfileId(),e);
                    }
                }
            }
        }
    }

    public UserLayoutNodeDescription getNode(String nodeId) throws PortalException {
        Document ulm=this.getUserLayoutDOM();
        if(ulm==null) {
            throw new PortalException("UserLayout has not been initialized.");
        }

        // find an element with a given id
        Element element = (Element) ulm.getElementById(nodeId);
        if(element==null) {
            throw new PortalException("Element with ID=\""+nodeId+"\" doesn't exist.");
        }
        return UserLayoutNodeDescription.createUserLayoutNodeDescription(element);
    }


    public UserLayoutNodeDescription addNode(UserLayoutNodeDescription node, String parentId, String nextSiblingId) throws PortalException {
        boolean isChannel=false;
        UserLayoutNodeDescription parent=this.getNode(parentId);
        if(canAddNode(node,parent,nextSiblingId)) {
            // assign new Id

            if(this.getLayoutStore()==null) {
                throw new PortalException("Store implementation has not been set.");
            } else {
                try {
                    if(node instanceof UserLayoutChannelDescription) {
                        isChannel=true;
                        node.setId(this.getLayoutStore().generateNewChannelSubscribeId(owner));
                    } else {
                        node.setId(this.getLayoutStore().generateNewFolderId(owner));
                    }
                } catch (PortalException pe) {
                    throw pe;
                } catch (Exception e) {
                    throw new PortalException("Exception encountered while generating new usre layout node Id for userId="+this.owner.getID());
                }
            }

            DocumentImpl ulm=this.userLayoutDocument;
            Element childElement=node.getXML(this.getUserLayoutDOM());
            Element parentElement=(Element)ulm.getElementById(parentId);
            if(nextSiblingId==null) {
                parentElement.appendChild(childElement);
            } else {
                Node nextSibling=ulm.getElementById(nextSiblingId);
                parentElement.insertBefore(childElement,nextSibling);
            }
            markLayoutDirty();
            // register element id
            ulm.putIdentifier(node.getId(),childElement);
            this.updateCacheKey();
            
            // inform the listeners
            LayoutEvent ev=new LayoutEvent(this,node);
            for(Iterator i=listeners.iterator();i.hasNext();) {
                LayoutEventListener lel=(LayoutEventListener)i.next();
                if(isChannel) {
                    lel.channelAdded(ev);
                } else {
                    lel.folderAdded(ev);
                }
            }
            return node;
        }
        return null;
    }

    public boolean moveNode(String nodeId, String parentId, String nextSiblingId) throws PortalException  {

        UserLayoutNodeDescription parent=this.getNode(parentId);
        UserLayoutNodeDescription node=this.getNode(nodeId);
        String oldParentNodeId=getParentId(nodeId);
        if(canMoveNode(node,parent,nextSiblingId)) {
            // must be a folder
            Document ulm=this.getUserLayoutDOM();
            Element childElement=(Element)ulm.getElementById(nodeId);
            Element parentElement=(Element)ulm.getElementById(parentId);
            if(nextSiblingId==null) {
                parentElement.appendChild(childElement);
            } else {
                Node nextSibling=ulm.getElementById(nextSiblingId);
                parentElement.insertBefore(childElement,nextSibling);
            }
            markLayoutDirty();
            this.updateCacheKey();

            // inform the listeners
            boolean isChannel=false;
            if(node instanceof UserLayoutChannelDescription) {
                isChannel=true;
            }
            LayoutMoveEvent ev=new LayoutMoveEvent(this,node,oldParentNodeId);
            for(Iterator i=listeners.iterator();i.hasNext();) {
                LayoutEventListener lel=(LayoutEventListener)i.next();
                if(isChannel) {
                    lel.channelMoved(ev);
                } else {
                    lel.folderMoved(ev);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteNode(String nodeId) throws PortalException {
        if(canDeleteNode(nodeId)) {
            UserLayoutNodeDescription nodeDescription=this.getNode(nodeId);
            String parentNodeId=this.getParentId(nodeId);

            Document ulm=this.getUserLayoutDOM();
            Element childElement=(Element)ulm.getElementById(nodeId);
            Node parent=childElement.getParentNode();
            if(parent!=null) {
                parent.removeChild(childElement);
            } else {
                throw new PortalException("Node \""+nodeId+"\" has a NULL parent !");
            }
            markLayoutDirty();
            this.updateCacheKey();

            // inform the listeners
            boolean isChannel=false;
            if(nodeDescription instanceof UserLayoutChannelDescription) {
                isChannel=true;
            }
            LayoutMoveEvent ev=new LayoutMoveEvent(this,nodeDescription,parentNodeId);
            for(Iterator i=listeners.iterator();i.hasNext();) {
                LayoutEventListener lel=(LayoutEventListener)i.next();
                if(isChannel) {
                    lel.channelDeleted(ev);
                } else {
                    lel.folderDeleted(ev);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean updateNode(UserLayoutNodeDescription node) throws PortalException {
        boolean isChannel=false;
        if(canUpdateNode(node)) {
            // normally here, one would determine what has changed
            // but we'll just make sure that the node type has not
            // changed and then regenerate the node Element from scratch,
            // and attach any children it might have had to it.

            String nodeId=node.getId();
            String nextSiblingId=getNextSiblingId(nodeId);
            Element nextSibling=null;
            if(nextSiblingId!=null) {
                DocumentImpl ulm=this.userLayoutDocument;
                nextSibling=ulm.getElementById(nextSiblingId);
            }

            UserLayoutNodeDescription oldNode=getNode(nodeId);

            if(oldNode instanceof UserLayoutChannelDescription) {
                UserLayoutChannelDescription oldChannel=(UserLayoutChannelDescription) oldNode;
                if(node instanceof UserLayoutChannelDescription) {
                    isChannel=true;
                    DocumentImpl ulm=this.userLayoutDocument;
                    // generate new XML Element
                    Element newChannelElement=node.getXML(ulm);
                    Element oldChannelElement=(Element)ulm.getElementById(nodeId);
                    Node parent=oldChannelElement.getParentNode();
                    parent.removeChild(oldChannelElement);
                    parent.insertBefore(newChannelElement,nextSibling);
                    // register new child instead
                    ulm.putIdentifier(node.getId(),newChannelElement);

                    // inform the listeners
                    LayoutEvent ev=new LayoutEvent(this,node);
                    for(Iterator i=listeners.iterator();i.hasNext();) {
                        LayoutEventListener lel=(LayoutEventListener)i.next();
                        lel.channelUpdated(ev);
                    }
                } else {
                    throw new PortalException("Change channel to folder is not allowed by updateNode() method!");
                }
            } else if(oldNode instanceof UserLayoutRootDescription) {
                throw new PortalException("Update of root node is not currently allowed!");
            } else {
                 // must be a folder
                UserLayoutFolderDescription oldFolder=(UserLayoutFolderDescription) oldNode;
                if(node instanceof UserLayoutFolderDescription) {
                    DocumentImpl ulm=this.userLayoutDocument;
                    // generate new XML Element
                    Element newFolderElement=node.getXML(ulm);
                    Element oldFolderElement=(Element)ulm.getElementById(nodeId);
                    Node parent=oldFolderElement.getParentNode();

                    // move children
                    Vector children=new Vector();
                    for(Node n=oldFolderElement.getFirstChild(); n!=null;n=n.getNextSibling()) {
                        children.add(n);
                    }

                    for(int i=0;i<children.size();i++) {
                        newFolderElement.appendChild((Node)children.get(i));
                    }

                    // replace the actual node
                    parent.removeChild(oldFolderElement);
                    parent.insertBefore(newFolderElement,nextSibling);
                    // register new child instead
                    ulm.putIdentifier(node.getId(),newFolderElement);

                    // inform the listeners
                    LayoutEvent ev=new LayoutEvent(this,node);
                    for(Iterator i=listeners.iterator();i.hasNext();) {
                        LayoutEventListener lel=(LayoutEventListener)i.next();
                        lel.folderUpdated(ev);
                    }
                }
            }
            markLayoutDirty();
            this.updateCacheKey();



            return true;
        } else {
            return false;
        }
    }


    public boolean canAddNode(UserLayoutNodeDescription node, String parentId, String nextSiblingId) throws PortalException {
        return this.canAddNode(node,this.getNode(parentId),nextSiblingId);
    }

    protected boolean canAddNode(UserLayoutNodeDescription node,UserLayoutNodeDescription parent, String nextSiblingId) throws PortalException {
        // make sure sibling exists and is a child of nodeId
        if(nextSiblingId!=null) {
            UserLayoutNodeDescription sibling=getNode(nextSiblingId);
            if(sibling==null) {
                throw new PortalException("Unable to find a sibling node with id=\""+nextSiblingId+"\"");
            }
            if(!parent.getId().equals(getParentId(nextSiblingId))) {
                throw new PortalException("Given sibling (\""+nextSiblingId+"\") is not a child of a given parentId (\""+parent.getId()+"\")");
            }
        }

        return (parent!=null && parent instanceof UserLayoutFolderDescription && !parent.isImmutable());
    }

    public boolean canMoveNode(String nodeId, String parentId,String nextSiblingId) throws PortalException {
        return this.canMoveNode(this.getNode(nodeId),this.getNode(parentId),nextSiblingId);
    }

    protected boolean canMoveNode(UserLayoutNodeDescription node,UserLayoutNodeDescription parent, String nextSiblingId) throws PortalException {
        // is the current parent immutable ?
        UserLayoutNodeDescription currentParent=getNode(getParentId(node.getId()));
        if(currentParent==null) {
            throw new PortalException("Unable to determine a parent node for node with id=\""+node.getId()+"\"");
        }
        return (!currentParent.isImmutable() && canAddNode(node,parent,nextSiblingId));
    }

    public boolean canDeleteNode(String nodeId) throws PortalException {
        return canDeleteNode(this.getNode(nodeId));
    }

    protected boolean canDeleteNode(UserLayoutNodeDescription node) throws PortalException {
        return !(node==null || node.isUnremovable());
    }

    public boolean canUpdateNode(String nodeId) throws PortalException {
        return canUpdateNode(this.getNode(nodeId));
    }

    protected boolean canUpdateNode(UserLayoutNodeDescription node) {
        return !(node==null || node.isImmutable());
    }

    public void markAddTargets(UserLayoutNodeDescription node) {
        // get all folders
        this.updateCacheKey();
    }


    public void markMoveTargets(String nodeId) throws PortalException {
        UserLayoutNodeDescription node=getNode(nodeId);
        this.updateCacheKey();
    }

    public String getParentId(String nodeId) throws PortalException {
        Document ulm=this.getUserLayoutDOM();
        Element nelement=(Element)ulm.getElementById(nodeId);
        if(nelement!=null) {
            Node parent=nelement.getParentNode();
            if(parent!=null) {
                if(parent.getNodeType()!=Node.ELEMENT_NODE) {
                    throw new PortalException("Node with id=\""+nodeId+"\" is attached to something other then an element node.");
                } else {
                    Element e=(Element) parent;
                    return e.getAttribute("ID");
                }
            } else {
                return null;
            }
        } else {
            throw new PortalException("Node with id=\""+nodeId+"\" doesn't exist.");
        }
    }

    public String getNextSiblingId(String nodeId) throws PortalException {
        Document ulm=this.getUserLayoutDOM();
        Element nelement=(Element)ulm.getElementById(nodeId);
        if(nelement!=null) {
            Node nsibling=nelement.getNextSibling();
            // scroll to the next element node
            while(nsibling!=null && nsibling.getNodeType()!=Node.ELEMENT_NODE){
                nsibling=nsibling.getNextSibling();
            }
            if(nsibling!=null) {
                Element e=(Element) nsibling;
                return e.getAttribute("ID");
            } else {
                return null;
            }
        } else {
            throw new PortalException("Node with id=\""+nodeId+"\" doesn't exist.");
        }
    }

    public String getPreviousSiblingId(String nodeId) throws PortalException {
        Document ulm=this.getUserLayoutDOM();
        Element nelement=(Element)ulm.getElementById(nodeId);
        if(nelement!=null) {
            Node nsibling=nelement.getPreviousSibling();
            // scroll to the next element node
            while(nsibling!=null && nsibling.getNodeType()!=Node.ELEMENT_NODE){
                nsibling=nsibling.getNextSibling();
            }
            if(nsibling!=null) {
                Element e=(Element) nsibling;
                return e.getAttribute("ID");
            } else {
                return null;
            }
        } else {
            throw new PortalException("Node with id=\""+nodeId+"\" doesn't exist.");
        }
    }

    public List getChildIds(String nodeId) throws PortalException {
        Vector v=new Vector();
        UserLayoutNodeDescription node=getNode(nodeId);
        if(node instanceof UserLayoutFolderDescription) {
            Document ulm=this.getUserLayoutDOM();
            Element felement=(Element)ulm.getElementById(nodeId);
            for(Node n=felement.getFirstChild(); n!=null;n=n.getNextSibling()) {
                if(n.getNodeType()==Node.ELEMENT_NODE) {
                    Element e=(Element)n;
                    if(e.getAttribute("ID")!=null) {
                        v.add(e.getAttribute("ID"));
                    }
                }
            }
        }
        return v;
    }

    public String getCacheKey() {
        return this.cacheKey;
    }

    /**
     * This is outright cheating ! We're supposed to analyze the user layout tree
     * and return a key that corresponds uniqly to the composition and the sturcture of the tree.
     * Here we just return a different key wheneever anything changes. So if one was to move a
     * node back and forth, the key would always never (almost) come back to the original value,
     * even though the changes to the user layout are cyclic.
     *
     */
    private void updateCacheKey() {
        this.cacheKey=Long.toString(rnd.nextLong());
    }

    public int getLayoutId() {
        return profile.getLayoutId();
    }

    public boolean addLayoutEventListener(LayoutEventListener l) {
        return listeners.add(l);
    }
    public boolean removeLayoutEventListener(LayoutEventListener l) {
        return listeners.remove(l);
    }


    protected boolean isLayoutDirty() { return dirtyState; }
    private void markLayoutDirty() { dirtyState=true; }
    private void clearDirtyFlag() { dirtyState=false; }
}
