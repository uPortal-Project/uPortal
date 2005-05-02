/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.LayoutEvent;
import org.jasig.portal.layout.LayoutEventListener;
import org.jasig.portal.layout.LayoutMoveEvent;
import org.jasig.portal.layout.SimpleLayout;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.layout.node.UserLayoutFolderDescription;
import org.jasig.portal.layout.node.UserLayoutNodeDescription;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;


/**
 * A proprietary SCT layout manager that provides layout control through
 * layout fragments owned by various users. Copied from SimpleLayoutManager.
 *
 * @author <a href="mailto:mboyd@sct.com">Mark Boyd</a>
 * @version 1.0  $Revision$ $Date$
 * @since uPortal 2.5
 */
public class DistributedLayoutManager implements IUserLayoutManager
{
    public static final String RCS_ID = "@(#) $Header$";
    private static final Log cLog = LogFactory.getLog(DistributedLayoutManager.class);

    // The names for marking nodes
    // added for uPortal 2.3.2 upgrade.
    private static final String ADD_COMMAND = "add";
    private static final String MOVE_COMMAND = "move";

    protected final IPerson owner;
    protected final UserProfile profile;
    protected IUserLayoutStore store=null;
    protected Set listeners=new HashSet();

    protected Document userLayoutDocument=null;

    protected static Random rnd=new Random();
    protected String cacheKey="initialKey";
    protected String rootNodeId = null;

    private boolean channelsAdded = false;
    private boolean isFragment = false;

    /** Map of read/writer lock objects; one per unique person. */
    private Map mLocks = new ConcurrentHashMap();

    private final ReadWriteLock getReadWriteLock(IPerson person)
    {
        Object key = new Integer(person.getID());

        ReadWriteLock lock = (ReadWriteLock) mLocks.get(key);

        if (null == lock)
        {
            lock = new ReentrantWriterPreferenceReadWriteLock();

            mLocks.put(key, lock);
        }

        return lock;
    }

    private void acquireReadLock(IPerson person) throws InterruptedException
    {
        getReadWriteLock(person).readLock().acquire();
    }

    private void releaseReadLock(IPerson person)
    {
        getReadWriteLock(person).readLock().release();
    }

    private void acquireWriteLock(IPerson person) throws InterruptedException
    {
        getReadWriteLock(person).writeLock().acquire();
    }

    private void releaseWriteLock(IPerson person)
    {
        getReadWriteLock(person).writeLock().release();
    }

    public DistributedLayoutManager(IPerson owner, UserProfile profile,
            IUserLayoutStore store) throws PortalException
    {
        try
        {
            if (owner == null)
            {
                throw new PortalException(
                        "A non-null owner needs to be specified.");
            }

            if (profile == null)
            {
                throw new PortalException(
                        "A non-null profile needs to be specified.");
            }

            this.owner = owner;
            this.profile = profile;
            this.setLayoutStore(store);
            this.loadUserLayout();

            // This listener determines if one or more channels have been
            // added, and sets a state variable which is reset when the
            // layout saved event is triggered.
            this.addLayoutEventListener(new LayoutEventListener()
            {
                public void channelAdded(LayoutEvent ev)
                {
                    channelsAdded = true;
                }

                public void channelUpdated(LayoutEvent ev)
                {
                    // ignore
                }

                public void channelMoved(LayoutMoveEvent ev)
                {
                    // ignore
                }

                public void channelDeleted(LayoutMoveEvent ev)
                {
                    // ignore
                }

                public void folderAdded(LayoutEvent ev)
                {
                    // ignore
                }

                public void folderUpdated(LayoutEvent ev)
                {
                    // ignore
                }

                public void folderMoved(LayoutMoveEvent ev)
                {
                    // ignore
                }

                public void folderDeleted(LayoutMoveEvent ev)
                {
                    // ignore
                }

                public void layoutLoaded()
                {
                    // ignore
                }

                public void layoutSaved()
                {
                    channelsAdded = false;
                }
            });
        } catch (Throwable e)
        {
            cLog.error("Unable to instantiate DistributedLayoutManager "
                    + "for user " + owner.getFullName(), e);
            if (e instanceof PortalException)
                throw (PortalException) e;
            else
                throw new RuntimeException(
                        "Unable to instantiate DistributedLayoutManager "
                                + "for user " + owner.getFullName(), e);
        }
    }


    // This method should be removed whenever it becomes possible
    private void setUserLayoutDOM(Document doc) {
        this.userLayoutDocument = doc;
        this.updateCacheKey();

        // determine if this is a layout fragment by looking at the root node
        // for a cp:fragment attribute.
        // changes since uP2.4.1 now has a separate nested folder that is the
        // root folder and the only child of the outermost, containing, layout 
        // element.
        //Element layout = (Element) this.userLayoutDocument
        //    .getElementById( ROOT_FOLDER_ID );
        Element layout = this.userLayoutDocument.getDocumentElement();
        Node attr = layout.getAttributeNodeNS( Constants.NS_URI,
                                               Constants.LCL_FRAGMENT_NAME );
        this.isFragment = attr != null;
    }
    // This method should be removed whenever it becomes possible
    public Document getUserLayoutDOM() {
        return this.userLayoutDocument;
    }

    public void getUserLayout(ContentHandler ch) throws PortalException {
        Document ul=this.getUserLayoutDOM();
        if(ul==null) {
            throw new PortalException("User layout has not been initialized");
        } else {
            getUserLayout(ul,ch);
        }
    }

    public void getUserLayout(String nodeId, ContentHandler ch) throws PortalException {
        Document ul=this.getUserLayoutDOM();

        if(ul==null) {
            throw new PortalException("User layout has not been initialized");
        } else {
            Node rootNode=ul.getElementById(nodeId);
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
            cLog.error("Encountered an exception trying to output "
                    + "user layout:", e);
            throw new PortalException("Encountered an exception trying to output user layout",e);
        }
    }


    public void setLayoutStore(IUserLayoutStore store) {
        this.store=store;
    }

    protected IUserLayoutStore getLayoutStore() {
        return this.store;
    }


    public synchronized void loadUserLayout() throws PortalException {
        IUserLayoutStore layoutStore = getLayoutStore();

        if(layoutStore==null) {
            throw new PortalException("Store implementation has not been set.");
        } else {
            try {
                Document uli=layoutStore.getUserLayout(this.owner,this.profile);
                
               if(uli!=null) {
                    this.setUserLayoutDOM(uli);
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
                String message = "Exception encountered while reading a layout for userId="+this.owner.getID()+", profileId="+this.profile.getProfileId();
                cLog.error(message, e);
                throw new PortalException(message ,e);
            }
        }
    }

    public synchronized void saveUserLayout() throws PortalException{
        Document uld=this.getUserLayoutDOM();
        
        if(uld==null) {
            throw new PortalException("UserLayout has not been initialized.");
        } else {
            IUserLayoutStore layoutStore = getLayoutStore();

            if(layoutStore==null) {
                throw new PortalException("Store implementation has not been set.");
            } else {
                try {
                    layoutStore.setUserLayout(this.owner,this.profile,uld,channelsAdded);
                    
                    // inform listeners
                    for(Iterator i=listeners.iterator();i.hasNext();) {
                        LayoutEventListener lel=(LayoutEventListener)i.next();
                        lel.layoutSaved();
                    }
                } catch (PortalException pe) {
                    throw pe;
                } catch (Exception e) {
                    String message = "Exception encountered while trying to save a layout for userId="+this.owner.getID()+", profileId="+this.profile.getProfileId();
                    cLog.error(message, e);
                    throw new PortalException(message,e);
                }
                
            }
        }
    }

    public IUserLayoutNodeDescription getNode( String nodeId )
        throws PortalException
    {
        if (nodeId == null)
            return null;
        
        Document uld=this.getUserLayoutDOM();

        if( uld==null )
        {
            throw new PortalException("UserLayout has not been initialized.");
        }

        // find an element with a given id
        Element element = (Element) uld.getElementById( nodeId );
        if( element == null )
        {
            throw new PortalException("Element with ID=\"" + nodeId +
                                      "\" doesn't exist." );
        }
        return UserLayoutNodeDescription.createUserLayoutNodeDescription(element);
    }


    public IUserLayoutNodeDescription addNode( IUserLayoutNodeDescription node,
                                              String parentId,
                                              String nextSiblingId )
        throws PortalException
    {
        boolean isChannel=false;
        IUserLayoutNodeDescription parent=this.getNode(parentId);
        if( canAddNode( node, parent, nextSiblingId ) )
        {
            // assign new Id
            IUserLayoutStore layoutStore = getLayoutStore();

            if(layoutStore==null) {
                throw new PortalException("Store implementation has not been set.");
            } else {
                try {
                    if(node instanceof IUserLayoutChannelDescription) {
                        isChannel=true;
                        node.setId(layoutStore.generateNewChannelSubscribeId(owner));
                    } else {
                        node.setId(layoutStore.generateNewFolderId(owner));
                    }
                } catch (PortalException pe) {
                    throw pe;
                } catch (Exception e) {
                    throw new PortalException("Exception encountered while generating new usre layout node Id for userId="+this.owner.getID());
                }
            }

            Document uld=this.userLayoutDocument;
            Element childElement=node.getXML(this.getUserLayoutDOM());
            Element parentElement=(Element)uld.getElementById(parentId);
            if(nextSiblingId==null) {
                parentElement.appendChild(childElement);
            } else {
                Node nextSibling=uld.getElementById(nextSiblingId);
                parentElement.insertBefore(childElement,nextSibling);
            }
            // register element id
            /* mrb DOM3 change
            ((IPortalDocument) uld).putIdentifier(node.getId(),childElement);
            */
            childElement.setIdAttribute(Constants.ATT_ID, true);
            childElement.setAttribute(Constants.ATT_ID, node.getId());
            this.updateCacheKey();

            // push into the user's real layout that gets persisted.
            HandlerUtils.createPlfNodeAndPath( childElement,
                                               isChannel, owner );

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

    public boolean moveNode( String nodeId,
                             String parentId,
                             String nextSiblingId )
        throws PortalException
    {
        IUserLayoutNodeDescription parent=this.getNode(parentId);
        IUserLayoutNodeDescription node=this.getNode(nodeId);
        String oldParentNodeId=getParentId(nodeId);
        if(canMoveNode(node,parent,nextSiblingId)) {
            // must be a folder
            Document uld=this.getUserLayoutDOM();
            Element childElement=(Element)uld.getElementById(nodeId);
            Element parentElement=(Element)uld.getElementById(parentId);
            if(nextSiblingId==null) {
                parentElement.appendChild(childElement);
            } else {
                Node nextSibling=uld.getElementById(nextSiblingId);
                parentElement.insertBefore(childElement,nextSibling);
            }
            this.updateCacheKey();

            // propagate the change into the PLF
            Element oldParent = (Element) uld.getElementById(oldParentNodeId);
            TabColumnPrefsHandler.moveElement( childElement,
                                               oldParent,
                                               owner );
            // inform the listeners
            boolean isChannel=false;
            if(node instanceof IUserLayoutChannelDescription) {
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

    public boolean deleteNode( String nodeId )
        throws PortalException {
        if(canDeleteNode(nodeId)) {
            IUserLayoutNodeDescription nodeDescription=this.getNode(nodeId);
            String parentNodeId=this.getParentId(nodeId);

            Document uld=this.getUserLayoutDOM();
            Element childElement=(Element)uld.getElementById(nodeId);
            Node parent=childElement.getParentNode();
            if(parent!=null) {
                parent.removeChild(childElement);
            } else {
                throw new PortalException("Node \""+nodeId+"\" has a NULL parent !");
            }
            this.updateCacheKey();

            // now push into the PLF
            TabColumnPrefsHandler.deleteNode( childElement, (Element) parent,
                                              owner );
            // inform the listeners
            boolean isChannel=false;
            if(nodeDescription instanceof IUserLayoutChannelDescription) {
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

    public synchronized boolean updateNode( IUserLayoutNodeDescription node )
        throws PortalException
    {
        boolean isChannel=false;

        if( canUpdateNode( node ) )
        {
            // normally here, one would determine what has changed
            // but we'll just make sure that the node type has not
            // changed and then regenerate the node Element from scratch,
            // and attach any children it might have had to it.

            String nodeId = node.getId();
            String nextSiblingId = getNextSiblingId( nodeId );
            Element nextSibling = null;

            if( nextSiblingId != null )
            {
                Document uld = this.userLayoutDocument;
                nextSibling = uld.getElementById( nextSiblingId );
            }

            IUserLayoutNodeDescription oldNode = getNode( nodeId );

            if( oldNode instanceof IUserLayoutChannelDescription )
            {
                IUserLayoutChannelDescription oldChannel=(IUserLayoutChannelDescription) oldNode;
                if( node instanceof IUserLayoutChannelDescription )
                {
                    isChannel = true;
                    Document uld = this.userLayoutDocument;
                    // generate new XML Element

                    Element newChannelElement = node.getXML(uld);
                    Element oldChannelElement = (Element) uld.getElementById( nodeId );
                    Node parent = oldChannelElement.getParentNode();
                    parent.removeChild( oldChannelElement );
                    parent.insertBefore( newChannelElement, nextSibling );
                    // register new child instead
                    /* mrb DOM3 change
                    ((IPortalDocument) uld).putIdentifier( node.getId(), newChannelElement );
                    */
                    newChannelElement.setIdAttribute(Constants.ATT_ID, true);

                    // inform the listeners
                    LayoutEvent ev=new LayoutEvent( this, node);
                    for( Iterator i=listeners.iterator(); i.hasNext(); )
                    {
                        LayoutEventListener lel=(LayoutEventListener)i.next();
                        lel.channelUpdated( ev );
                    }
                    pushChanDiffsIntoPlf( newChannelElement,
                                          (IUserLayoutChannelDescription) node,
                                          oldChannel );
                }
                else
                {
                    throw new PortalException("Change channel to folder is not allowed by updateNode() method!");
                }
            }
            else
            {
                 // must be a folder
                UserLayoutFolderDescription oldFolder=(UserLayoutFolderDescription) oldNode;
                if (oldFolder.getId().equals(getRootFolderId()))
                    throw new PortalException("Update of root node is not currently allowed!");
                    
                if( node instanceof IUserLayoutFolderDescription )
                {
                    Document uld=this.userLayoutDocument;
                    // generate new XML Element
                    Element newFolderElement=node.getXML(uld);
                    Element oldFolderElement=(Element)uld.getElementById(nodeId);
                    Node parent=oldFolderElement.getParentNode();

                    // move children
                    Vector children=new Vector();
                    for( Node n=oldFolderElement.getFirstChild();
                        n!=null; n=n.getNextSibling() )
                    {
                        children.add(n);
                    }

                    for( int i=0; i<children.size(); i++ )
                    {
                        newFolderElement.appendChild((Node)children.get(i));
                    }

                    // replace the actual node
                    parent.removeChild(oldFolderElement);
                    parent.insertBefore(newFolderElement,nextSibling);
                    // register new child instead
                    /* mrb DOM3 change
                    ((IPortalDocument) uld).putIdentifier( node.getId(), newFolderElement );
                    */
                    newFolderElement.setIdAttribute(Constants.ATT_ID, true);

                    // inform the listeners
                    LayoutEvent ev=new LayoutEvent(this,node);
                    for(Iterator i=listeners.iterator();i.hasNext();) {
                        LayoutEventListener lel=(LayoutEventListener)i.next();
                        lel.folderUpdated(ev);
                    }
                    pushFolderDiffsIntoPlf( newFolderElement,
                                            (UserLayoutFolderDescription) node,
                                            oldFolder );
                }
            }
            this.updateCacheKey();
            return true;
        }
        else
        {
            return false;
        }
    }

    private void pushFolderDiffsIntoPlf( Element element,
                                         UserLayoutFolderDescription newF,
                                         UserLayoutFolderDescription oldF )
        throws PortalException
    {
        // currently the only elements that we expect users to change are:
        // deleteAllowed, moveAllowed, editAllowed, and addChildAllowed (for
        // fragment owners); name (for regular users).

        if ( ! newF.getName().equals( oldF.getName() ) )
            TabColumnPrefsHandler.editAttribute( element,
                                                 Constants.ATT_NAME,
                                                 owner );

        if ( newF.isMoveAllowed()     !=  oldF.isMoveAllowed() ||
             newF.isEditAllowed()     !=  oldF.isEditAllowed() ||
             newF.isAddChildAllowed() !=  oldF.isAddChildAllowed() ||
             newF.isDeleteAllowed()   !=  oldF.isDeleteAllowed() )
            TabColumnPrefsHandler.changeRestrictions( element,
                                                      newF.isMoveAllowed(),
                                                      newF.isEditAllowed(),
                                                      newF.isAddChildAllowed(),
                                                      newF.isDeleteAllowed(),
                                                      owner );
    }

    private void pushChanDiffsIntoPlf( Element element,
                                       IUserLayoutChannelDescription newChan,
                                       IUserLayoutChannelDescription oldChan )
        throws PortalException
    {
        // currently the only elements that we expect users to change are:
        // deleteAllowed and moveAllowed (for fragment owners); and child
        // property elements.

        if ( newChan.isMoveAllowed()     !=  oldChan.isMoveAllowed() ||
             newChan.isDeleteAllowed()   !=  oldChan.isDeleteAllowed() )
            TabColumnPrefsHandler.changeRestrictions( element,
                                                      newChan.isMoveAllowed(),
                                                      false,
                                                      false,
                                                      newChan.isDeleteAllowed(),
                                                      owner );
        // now push changed param children into PLF
        Element plfChan = TabColumnPrefsHandler.getPlfChannel( element,
                                                               owner );
        Document root = plfChan.getOwnerDocument();
        
        // TODO figure out how the line below is supposed to take place in 
        // the latest codebase via the interface rather than the implemention
        // class.
        UserLayoutChannelDescription newChanDef = (UserLayoutChannelDescription) newChan;
        newChanDef.addParameterChildren( plfChan, root );
    }

    public boolean canAddNode( IUserLayoutNodeDescription node,
                               String parentId,
                               String nextSiblingId )
        throws PortalException
    {
        return this.canAddNode(node,this.getNode(parentId),nextSiblingId);
    }

    protected boolean canAddNode( IUserLayoutNodeDescription node,
                                  IUserLayoutNodeDescription parent,
                                  String nextSiblingId )
        throws PortalException
    {
        // make sure sibling exists and is a child of nodeId
        if(nextSiblingId!=null) {
            IUserLayoutNodeDescription sibling=getNode(nextSiblingId);
            if(sibling==null) {
                throw new PortalException("Unable to find a sibling node with id=\""+nextSiblingId+"\"");
            }
            if(!parent.getId().equals(getParentId(nextSiblingId))) {
                throw new PortalException("Given sibling (\""+nextSiblingId+"\") is not a child of a given parentId (\""+parent.getId()+"\")");
            }
        }

        if ( parent == null ||
             ! node.isMoveAllowed() )
            return false;

        if ( parent instanceof IUserLayoutFolderDescription &&
             ! ( (IUserLayoutFolderDescription) parent).isAddChildAllowed() )
            return false;

        if ( nextSiblingId == null ) // end of list targeted
            return true;

        // so lets see if we can place it at the end of the sibling list and
        // hop left until we get into the correct position.

        Enumeration sibIds = getVisibleChildIds( parent.getId() );
        List sibs = Collections.list(sibIds);

        if ( sibs.size() == 0 ) // last node in list so should be ok
            return true;

        // reverse scan so that as changes are made the order of the, as yet,
        // unprocessed nodes is not altered.
        for( int idx = sibs.size() - 1;
             idx >= 0;
             idx-- )
        {
            IUserLayoutNodeDescription prev = getNode((String) sibs.get(idx));

            if ( ! MovementRules.canHopLeft( node, prev ) )
                return false;
            if ( prev.getId().equals( nextSiblingId ) )
                return true;
        }
        return false; // oops never found the sib
    }

    public boolean canMoveNode( String nodeId,
                                String parentId,
                                String nextSiblingId )
        throws PortalException
    {
        return this.canMoveNode( this.getNode( nodeId ),
                                 this.getNode( parentId ),
                                 nextSiblingId );
    }

    protected boolean canMoveNode( IUserLayoutNodeDescription node,
                                   IUserLayoutNodeDescription parent,
                                   String nextSiblingId )
        throws PortalException
    {
        // are we moving to a new parent?
        if ( ! getParentId( node.getId() ).equals( parent.getId() ) )
            return node.isMoveAllowed() &&
                canAddNode( node, parent, nextSiblingId );

        // same parent. which direction are we moving?
        Document uld = this.getUserLayoutDOM();
        Element parentE = (Element) uld.getElementById( parent.getId() );
        Element child = (Element) parentE.getFirstChild();
        int idx = 0;
        int nodeIdx = -1;
        int sibIdx = -1;

        while ( child != null )
        {
            String id = child.getAttribute( Constants.ATT_ID );
            if ( id.equals( node.getId() ) )
                nodeIdx = idx;
            if ( id.equals( nextSiblingId ) )
                sibIdx = idx;
            idx++;
            child = (Element) child.getNextSibling();
        }
        if ( nodeIdx == -1 ||     // couldn't find node
             ( nextSiblingId != null &&
               sibIdx == -1 ) )   // couldn't find sibling
            return false;

        if ( nodeIdx < sibIdx || // moving right
             sibIdx == -1 )      // appending to end
            return canMoveRight( node.getId(), nextSiblingId );
        else
            return canMoveLeft( node.getId(), nextSiblingId );
    }

    private boolean canMoveRight( String nodeId, String targetNextSibId )
        throws PortalException
    {
        IUserLayoutNodeDescription node = getNode( nodeId );
        Enumeration sibIds = getVisibleChildIds( getParentId( nodeId ) );
        List sibs = Collections.list(sibIds);

        for ( int idx = sibs.indexOf( nodeId ) + 1;
              idx > 0 && idx < sibs.size();
              idx++ )
        {
            String nextSibId = (String) sibs.get( idx );
            IUserLayoutNodeDescription next = getNode( nextSibId );

            if ( nextSibId != null &&
                 next.getId().equals( targetNextSibId ) )
                return true;
            else if ( ! MovementRules.canHopRight( node, next ) )
                return false;
        }

        if ( targetNextSibId == null ) // made it to end of sib list and
            return true;               // that is the desired location
        return false; // oops never found the sib. Should never happen.
    }

    private boolean canMoveLeft( String nodeId, String targetNextSibId )
        throws PortalException
    {
        IUserLayoutNodeDescription node = getNode( nodeId );
        Enumeration sibIds = getVisibleChildIds( getParentId( nodeId ) );
        List sibs = Collections.list(sibIds);

        for ( int idx = sibs.indexOf( nodeId ) - 1;
              idx >= 0;
              idx-- )
        {
            String prevSibId = (String) sibs.get( idx );
            IUserLayoutNodeDescription prev = getNode( prevSibId );

            if ( ! MovementRules.canHopLeft( node, prev ) )
                return false;
            if ( targetNextSibId != null &&
                 prev.getId().equals( targetNextSibId ) )
                return true;
        }
        return false; // oops never found the sib
    }

    public boolean canDeleteNode(String nodeId) throws PortalException {
        return canDeleteNode(this.getNode(nodeId));
    }

    /**
       Returns true if the node exists in the underlying
       DOM model and it does not contain a 'deleteAllowed' attribute with a
       value of 'false'.
     */
    protected boolean canDeleteNode( IUserLayoutNodeDescription node )
        throws PortalException
    {
        if ( node == null )
            return false;

        return node.isDeleteAllowed();
    }

    public boolean canUpdateNode( String nodeId )
        throws PortalException
    {
        return canUpdateNode( this.getNode( nodeId ) );
    }

    /**
       Returns true if the node is a folder node and edits on the folder are
       allowed or if the folder is a channel.
     */
    public boolean canUpdateNode( IUserLayoutNodeDescription node )
    {
        if ( node == null )
            return false;

        if ( node instanceof UserLayoutFolderDescription )
            return isFragment ||
                ( ( UserLayoutFolderDescription ) node ).isEditAllowed();
        return true;
    }

    public void markAddTargets(IUserLayoutNodeDescription node) {
        // we use the old prefs channel for now so ignore this.
        // TODO add for conversion to integrated modes theme
        
        // get all folders
        this.updateCacheKey();
    }


    public void markMoveTargets(String nodeId) throws PortalException {
        IUserLayoutNodeDescription node=getNode(nodeId);
        this.updateCacheKey();
    }

    public String getParentId(String nodeId) throws PortalException {
        Document uld=this.getUserLayoutDOM();
        Element nelement=(Element)uld.getElementById(nodeId);
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
        Document uld=this.getUserLayoutDOM();
        Element nelement=(Element)uld.getElementById(nodeId);
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
        Document uld=this.getUserLayoutDOM();
        Element nelement=(Element)uld.getElementById(nodeId);
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

    public Enumeration getChildIds(String nodeId) throws PortalException {
        return getChildIds( nodeId, false );
    }

    private Enumeration getVisibleChildIds(String nodeId)
        throws PortalException
    {
        return getChildIds( nodeId, true );
    }

    private Enumeration getChildIds( String nodeId,
                              boolean visibleOnly)
        throws PortalException
    {
        Vector v=new Vector();
        IUserLayoutNodeDescription node=getNode(nodeId);
        if(node instanceof UserLayoutFolderDescription) {
            Document uld=this.getUserLayoutDOM();
            Element felement=(Element)uld.getElementById(nodeId);
            for(Node n=felement.getFirstChild(); n!=null;n=n.getNextSibling()) {
                if( n.getNodeType()==Node.ELEMENT_NODE &&
                    ( visibleOnly == false ||
                      ( visibleOnly == true &&
                        ((Element) n).getAttribute( Constants.ATT_HIDDEN )
                        .equals("false") ) ) )
                {
                    Element e=(Element)n;
                    if(e.getAttribute("ID")!=null)
                    {
                        v.add(e.getAttribute("ID"));
                    }
                }
            }
        }
        return v.elements();
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

    /**
     * Returns the subscribe ID of a channel having the passed in functional
     * name or null if it can't find such a channel in the layout.
     */
    public String getSubscribeId(String fname) {
        try
        {
                // mrb JAXP1.3 upgrade change for standardization
                // Element fnameNode = (Element) XPathAPI.selectSingleNode(this.getUserLayoutDOM(),"//channel[@fname=\'"+fname+"\']");
                String expression = "//channel[@fname=\'"+fname+"\']";
                XPathFactory fac = XPathFactory.newInstance();
                XPath xpath = fac.newXPath();
                Element fnameNode = (Element) xpath.evaluate(expression, this
                        .getUserLayoutDOM(), XPathConstants.NODE);
                if(fnameNode!=null) {
                    return fnameNode.getAttribute("ID");
                } else {
                    return null;
                }
        } catch (XPathExpressionException e)
        {
            cLog.error("Encountered the exception, while trying to identify " +
                    "subscribe channel id for the fname=\""+fname+"\".", e);
            return null;
        }
    }

    public boolean addLayoutEventListener(LayoutEventListener l) {
        return listeners.add(l);
    }
    public boolean removeLayoutEventListener(LayoutEventListener l) {
        return listeners.remove(l);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.IUserLayoutManager#getUserLayout()
     */
    public IUserLayout getUserLayout() throws PortalException
    {
        // Copied from SimpleLayoutManager since our layouts are regular
        // simple layouts, ie Documents.
        return new SimpleLayout(String.valueOf(profile.getLayoutId()), this.userLayoutDocument);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.IUserLayoutManager#setUserLayout(org.jasig.portal.layout.IUserLayout)
     */
    public void setUserLayout(IUserLayout userLayout) throws PortalException
    {
        // Temporary until we use IUserLayout for real
        Document doc = DocumentFactory.getNewDocument();
        try {
            userLayout.writeTo(doc);
        } catch (PortalException pe) {
        }
        this.userLayoutDocument=doc;
        //this.markedUserLayout=null;
        this.updateCacheKey();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.IUserLayoutManager#getRootFolderId()
     */
    public String getRootFolderId()
    {
        try
        {
            if (rootNodeId == null)
            {
                // mrb JAXP1.3 upgrade change for standardization
                // Element rootNode = (Element)
                // XPathAPI.selectSingleNode(this.getUserLayoutDOM(),"//layout/folder");
                String expression = "//layout/folder";
                XPathFactory fac = XPathFactory.newInstance();
                XPath xpath = fac.newXPath();
                Element rootNode = (Element) xpath.evaluate(expression, this
                        .getUserLayoutDOM(), XPathConstants.NODE);
                rootNodeId = rootNode.getAttribute("ID");
            }
        } catch (XPathExpressionException e)
        {
            cLog.error(e);
        }
        return rootNodeId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.IUserLayoutManager#getDepth(java.lang.String)
     */
    public int getDepth(String nodeId) throws PortalException
    {
        // can't see what it calling this anywhere so ignoring for now.
        // TODO waiting to hear back from peter/michael
        return 0;
    }

    /* Return an implementation of IUserLayoutNodeDescription appropriate for
     * the type of node indicated. Currently, the only two types supported are
     * IUserLayoutNodeDescription.FOLDER and IUserLayoutNodeDescription.CHANNEL.
     * 
     * @see org.jasig.portal.layout.IUserLayoutManager#createNodeDescription(int)
     */
    public IUserLayoutNodeDescription createNodeDescription(int nodeType) throws PortalException
    {
        if (nodeType == IUserLayoutNodeDescription.FOLDER)
        {
            return new UserLayoutFolderDescription();
        }
        else
        {
            return new UserLayoutChannelDescription();
        }
    }
}
