/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
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
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelParameter;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.LayoutEvent;
import org.jasig.portal.layout.LayoutEventListener;
import org.jasig.portal.layout.LayoutMoveEvent;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.UserLayoutFolderDescription;
import org.jasig.portal.layout.simple.SimpleLayout;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * A layout manager that provides layout control through
 * layout fragments that are derived from regular portal user accounts.
 *
 * @author <a href="mailto:mboyd@sct.com">Mark Boyd</a>
 * @version 1.0  $Revision$ $Date$
 * @since uPortal 2.5
 */
public class DistributedLayoutManager implements IUserLayoutManager
{
    public static final String RCS_ID = "@(#) $Header$";
    private static final Log LOG = LogFactory.getLog(DistributedLayoutManager.class);

    protected final IPerson owner;
    protected final UserProfile profile;
    protected RDBMDistributedLayoutStore store=null;
    protected Set listeners=new HashSet();

    protected Document userLayoutDocument=null;

    protected static Random rnd=new Random();
    protected String cacheKey="initialKey";
    protected String rootNodeId = null;

    private boolean channelsAdded = false;
    private boolean isFragment = false;

    private IUserLayoutNodeDescription newNodeDescription = null;

    public DistributedLayoutManager(IPerson owner, UserProfile profile,
            IUserLayoutStore store) throws PortalException
    {
        if (owner == null)
        {
            throw new PortalException(
                    "Unable to instantiate DistributedLayoutManager. "
                            + "A non-null owner must to be specified.");
        }

        if (profile == null)
        {
            throw new PortalException(
                    "Unable to instantiate DistributedLayoutManager for "
                            + owner.getAttribute(IPerson.USERNAME) + ". A "
                            + "non-null profile must to be specified.");
        }
        try
        {

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
            throw new PortalException(
                    "Unable to instantiate DistributedLayoutManager for " 
                        + owner.getAttribute(IPerson.USERNAME)+".", e);
        }
    }

    private void setUserLayoutDOM(Document doc) {
        this.userLayoutDocument = doc;
        this.updateCacheKey();

        // determine if this is a layout fragment by looking at the layout node
        // for a dlm:fragment attribute.
        Element layout = this.userLayoutDocument.getDocumentElement();
        Node attr = layout.getAttributeNodeNS( Constants.NS_URI,
                                               Constants.LCL_FRAGMENT_NAME );
        this.isFragment = attr != null;
    }

    public Document getUserLayoutDOM() {
        return this.userLayoutDocument;
    }

    public void getUserLayout(ContentHandler ch) throws PortalException {
        Document ul=this.getUserLayoutDOM();
        if(ul==null) {
            throw new PortalException("User layout has not been initialized for " 
                        + owner.getAttribute(IPerson.USERNAME)+".");
        } else {
            getUserLayout(ul,ch);
        }
    }

    public void getUserLayout(String nodeId, ContentHandler ch) throws PortalException {
        Document ul=this.getUserLayoutDOM();

        if(ul==null) {
            throw new PortalException("User layout has not been initialized for " 
                        + owner.getAttribute(IPerson.USERNAME)+".");
        } else {
            Node rootNode=ul.getElementById(nodeId);
            if(rootNode==null) {
                throw new PortalException("A requested root node (with id=\"" 
                        + nodeId + "\") is not in the user layout for " 
                        + owner.getAttribute(IPerson.USERNAME)+".");
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
            LOG.error("Encountered an exception trying to output "
                    + "user layout for " + owner.getAttribute(IPerson.USERNAME)
                    + ".", e);
            throw new PortalException("Encountered an exception trying to " 
                    + "output user layout for " 
                    + owner.getAttribute(IPerson.USERNAME) + ".",e);
        }
    }


    public void setLayoutStore(IUserLayoutStore store) {
        this.store=(RDBMDistributedLayoutStore) store;
    }

    protected IUserLayoutStore getLayoutStore() {
        return this.store;
    }


    public synchronized void loadUserLayout() throws PortalException {
        IUserLayoutStore layoutStore = getLayoutStore();

        if(layoutStore==null) {
            throw new PortalException("Store implementation has not been " 
                    + "set for " 
                    + owner.getAttribute(IPerson.USERNAME) + ".");
        } else {
            Document uli= null;
            try {
                uli=layoutStore.getUserLayout(this.owner,this.profile);
            } catch (Exception e) {
                throw new PortalException("Exception encountered while " +
                        "reading a layout for userId=" + this.owner.getID() +
                        ", profileId=" + this.profile.getProfileId() ,e);
            }
            if(uli == null) {
                throw new PortalException("Null user layout returned " +
                        "for ownerId=\"" + owner.getID() + 
                        "\", profileId=\"" + profile.getProfileId() 
                        + "\", layoutId=\"" + profile.getLayoutId() + "\"");
            }
            try {
               if(uli!=null) {
                    this.setUserLayoutDOM(uli);
                    // inform listeners
                    for(Iterator i=listeners.iterator();i.hasNext();) {
                        LayoutEventListener lel=(LayoutEventListener)i.next();
                        lel.layoutLoaded();
                    }
                }
            } catch (Exception e) {
                   throw new PortalException("Exception encountered contacting " +
                           "layout listeners of layout for userId=" +
                           this.owner.getID() + ", profileId=" + 
                           this.profile.getProfileId() ,e);
            }
        }
    }

    public synchronized void saveUserLayout() throws PortalException{
        Document uld=this.getUserLayoutDOM();
        
        if(uld==null) {
            throw new PortalException("UserLayout has not been initialized for " 
                    + owner.getAttribute(IPerson.USERNAME) + ".");
        } else {
            IUserLayoutStore layoutStore = getLayoutStore();

            if(layoutStore==null) {
                throw new PortalException("Store implementation has not been set for " 
                    + owner.getAttribute(IPerson.USERNAME) + ".");
            } else {
                try {
                    layoutStore.setUserLayout(this.owner,this.profile,uld,channelsAdded);
                } catch (Exception e) {
                    throw new PortalException("Exception encountered while " +
                            "saving layout for userId=" + this.owner.getID() +
                            ", profileId=" + this.profile.getProfileId(),e);
                }
                try // inform listeners
                {
                    for(Iterator i=listeners.iterator();i.hasNext();) {
                        LayoutEventListener lel=(LayoutEventListener)i.next();
                        lel.layoutSaved();
                    }
                } catch (Exception e) {
                    throw new PortalException("Exception encountered contacting " +
                            "layout listeners of layout for userId=" +
                            this.owner.getID() + ", profileId=" + 
                            this.profile.getProfileId() ,e);
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
            throw new PortalException("UserLayout has not been initialized for " 
                    + owner.getAttribute(IPerson.USERNAME) + ".");

        // find an element with a given id
        Element element = (Element) uld.getElementById( nodeId );
        if( element == null )
        {
            throw new PortalException("Element with ID=\"" + nodeId +
                                      "\" doesn't exist for " 
                    + owner.getAttribute(IPerson.USERNAME) + "." );
        }
        IUserLayoutNodeDescription desc =
            ChannelDescription.createUserLayoutNodeDescription(element);
        if (nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX)
                && desc instanceof ChannelDescription)
        {
            FragmentChannelInfo info = store.getFragmentChannelInfo(nodeId);
            ((ChannelDescription)desc).setFragmentChannelInfo(info);
        }
        return desc;
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
                throw new PortalException("Store implementation has not been set for " 
                    + owner.getAttribute(IPerson.USERNAME) + ".");
            } else {
                try {
                    if(node instanceof IUserLayoutChannelDescription) {
                        isChannel=true;
                        node.setId(layoutStore.generateNewChannelSubscribeId(owner));
                    } else {
                        node.setId(layoutStore.generateNewFolderId(owner));
                    }
                } catch (Exception e) {
                    throw new PortalException("Exception encountered while " +
                            "generating new user layout node Id for  for " 
                            + owner.getAttribute(IPerson.USERNAME));
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
                throw new PortalException("Node \""+nodeId +
                        "\" has a NULL parent for layout of " 
                    + owner.getAttribute(IPerson.USERNAME) + ".");
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
    
    /**
     * Handles pushing changes made to the passed-in node into the user's layout.
     * If the node is an ILF node then the change is recorded via directives in
     * the PLF if such changes are allowed by the owning fragment. If the node
     * is a user owned node then the changes are applied directly to the corresponding node
     * in the PLF. 
     */
    public synchronized boolean updateNode( IUserLayoutNodeDescription node )
        throws PortalException
    {
        if( canUpdateNode( node ) )
        {
            String nodeId = node.getId();
            IUserLayoutNodeDescription oldNode = getNode( nodeId );

            if( oldNode instanceof IUserLayoutChannelDescription )
            {
                IUserLayoutChannelDescription oldChanDesc = (IUserLayoutChannelDescription) oldNode;
                if (!(node instanceof IUserLayoutChannelDescription))
                {
                    throw new PortalException("Change channel to folder is "
                            + "not allowed by updateNode() method! Occurred "
                            + "in layout for "
                            + owner.getAttribute(IPerson.USERNAME) + ".");
                }
                IUserLayoutChannelDescription newChanDesc = 
                    (IUserLayoutChannelDescription) node;
                updateChannelNode(nodeId, newChanDesc, oldChanDesc);
                // inform the listeners
                LayoutEvent ev = new LayoutEvent(this, node);
                for (Iterator i = listeners.iterator(); i.hasNext();)
                {
                    LayoutEventListener lel = (LayoutEventListener) i.next();
                    lel.channelUpdated(ev);
                }
            }
            else
            {
                 // must be a folder
                IUserLayoutFolderDescription oldFolderDesc=(IUserLayoutFolderDescription) oldNode;
                if (oldFolderDesc.getId().equals(getRootFolderId()))
                    throw new PortalException("Update of root node is not currently allowed!");
                    
                if( node instanceof IUserLayoutFolderDescription )
                {
                    IUserLayoutFolderDescription newFolderDesc=(IUserLayoutFolderDescription) node;
                    updateFolderNode(nodeId, newFolderDesc, oldFolderDesc);

                    // inform the listeners
                    LayoutEvent ev=new LayoutEvent(this,node);
                    for(Iterator i=listeners.iterator();i.hasNext();) {
                        LayoutEventListener lel=(LayoutEventListener)i.next();
                        lel.folderUpdated(ev);
                    }
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

    /**
     * Compares the new folder description object with the old folder 
     * description object to determine what items were changed and if those
     * changes are allowed. Once all changes are verified as being allowed
     * changes then they are pushed into both the ILF and the PLF as
     * appropriate. No changes are made until we determine that all changes are
     * allowed.
     * 
     * @param nodeId
     * @param newFolderDesc
     * @param oldFolderDesc
     * @throws PortalException
     */
    private void updateFolderNode(String nodeId,
            IUserLayoutFolderDescription newFolderDesc,
            IUserLayoutFolderDescription oldFolderDesc)
    throws PortalException
    {
        Element ilfNode = 
            (Element) userLayoutDocument.getElementById(nodeId);
        List pendingActions = new ArrayList();

        /*
         * see what structure attributes changed if any and see if allowed.
         * 
         * CHANNEL ATTRIBUTES that currently can be EDITED in DLM are:
         * name - in both fragments and regular layouts
         * dlm:moveAllowed - only on fragments 
         * dlm:editAllowed - only on fragments 
         * dlm:deleteAllowed - only on fragments 
         * dlm:addChildAllowed - only on fragments
         */

        // ATT: DLM Restrictions
        if (isFragment
                && (newFolderDesc.isDeleteAllowed() != 
                    oldFolderDesc.isDeleteAllowed()
                 || newFolderDesc.isEditAllowed() != 
                     oldFolderDesc.isEditAllowed() 
                 || newFolderDesc.isAddChildAllowed() != 
                     oldFolderDesc.isAddChildAllowed() 
                 || newFolderDesc.isMoveAllowed() != 
                    oldFolderDesc.isMoveAllowed()))
        {
            pendingActions.add(new LPAEditRestriction(owner, ilfNode,
                    newFolderDesc.isMoveAllowed(), 
                    newFolderDesc.isDeleteAllowed(), 
                    newFolderDesc.isEditAllowed(), 
                    newFolderDesc.isAddChildAllowed()));
        }
        
        // ATT: Name
        updateNodeAttribute(ilfNode, nodeId, Constants.ATT_NAME, newFolderDesc
                .getName(), oldFolderDesc.getName(), pendingActions);
        
        /*
         * if we make it to this point then all edits made are allowed so
         * process the actions to push the edits into the layout
         */
        for(Iterator itr = pendingActions.iterator(); itr.hasNext();)
        {
            ILayoutProcessingAction action = 
                (ILayoutProcessingAction) itr.next();
            action.perform();
        }
    }

    /**
     * Handles checking for updates to a named attribute, verifying such change
     * is allowed, and generates an action object to make that change.
     * 
     * @param ilfNode the node in the viewed layout
     * @param nodeId the id of the ilfNode
     * @param attName the attribute to be checked
     * @param newVal the attribute's new value
     * @param oldVal the attribute's old value
     * @param pendingActions the set of actions for adding an action 
     * @throws PortalException if the change is not allowed
     */
    private void updateNodeAttribute(Element ilfNode, String nodeId, 
            String attName, String newVal, String oldVal, List pendingActions) 
    throws PortalException
    {
        if (! newVal.equals(oldVal))
        {
            boolean isIncorporated = 
                nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX); 
            if (isIncorporated)
            {
                /*
                 * Is a change to this attribute allowed?
                 */
                FragmentNodeInfo fragNodeInf = store.getFragmentNodeInfo(nodeId);
                if (fragNodeInf == null )
                {
                    /*
                     * null should only happen if a node was deleted in the
                     * fragment and a user happened to already be logged in and
                     * edited an attribute on that node.
                     */ 
                    pendingActions.add(new LPAChangeAttribute(nodeId, attName,
                            newVal, owner, ilfNode));
                }
                else if (! fragNodeInf.canOverrideAttributes())
                {
                    /*
                     * It isn't overrideable.
                     */
                    throw new PortalException("Layout element '" 
                            + fragNodeInf.getAttributeValue(attName)
                            + "' does not allow overriding attribute '" 
                            + attName + "'.");
                }
                else if (! fragNodeInf.getAttributeValue(attName)
                        .equals(newVal))
                {
                    /*
                     * If we get here we can override and the value is 
                     * different than that in the fragment so make the change.
                     */
                    pendingActions.add(new LPAChangeAttribute(nodeId, attName,
                            newVal, owner, ilfNode));
                }
                else 
                {
                    /*
                     * The new value matches that in the fragment. 
                     */
                    pendingActions.add(new LPAResetAttribute(nodeId, attName,
                            fragNodeInf.getAttributeValue(attName), owner,
                            ilfNode));
                }
            }
            else
            {
                /*
                 * Node owned by user so no checking needed. Just change it.
                 */
                pendingActions.add(new LPAChangeAttribute(nodeId, attName,
                        newVal, owner, ilfNode));
            }
        }
    }
    /**
     * Compares the new channel description object with the old channel
     * description object to determine what items were changed and if those
     * changes are allowed. Once all changes are verified as being allowed
     * changes then they are pushed into both the ILF and the PLF as
     * appropriate. No changes are made until we determine that all changes are
     * allowed.
     * 
     * @param nodeId
     * @param newChanDesc
     * @param oldChanDesc
     * @throws PortalException
     */
    private void updateChannelNode(String nodeId,
            IUserLayoutChannelDescription newChanDesc,
            IUserLayoutChannelDescription oldChanDesc)
    throws PortalException
    {
        Element ilfNode = 
            (Element) userLayoutDocument.getElementById(nodeId);
        List pendingActions = new ArrayList();
        boolean isIncorporated = 
            nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX); 

        /*
         * see what structure attributes changed if any and see if allowed.
         * 
         * CHANNEL ATTRIBUTES that currently can be EDITED in DLM are:
         * dlm:moveAllowed - only on fragments 
         * dlm:editAllowed - only on fragments 
         * dlm:deleteAllowed - only on fragments 
         */

        // ATT: DLM Restrictions
        if (isFragment
                && (newChanDesc.isDeleteAllowed() != 
                    oldChanDesc.isDeleteAllowed()
                 || newChanDesc.isEditAllowed() != 
                    oldChanDesc.isEditAllowed() 
                 || newChanDesc.isMoveAllowed() != 
                    oldChanDesc.isMoveAllowed()))
        {
            pendingActions.add(new LPAEditRestriction(owner, ilfNode,
                    newChanDesc.isMoveAllowed(), 
                    newChanDesc.isDeleteAllowed(), 
                    newChanDesc.isEditAllowed(), 
                    newChanDesc.isAddChildAllowed()));
        }
        
        // ATT: other? if other attributes should be editable in DLM on channels
        // we can add calls like this to enable such support.
        //  updateNodeAttribute(ilfNode, nodeId, "hidden", 
        //     newChanDesc.getName(), oldChanDesc.getName(), pendingActions);

        /*
         * now we loop through all parameters in the new channel description and
         * see if there is a corresponding parameter in the old channel
         * description and see if the change is allowed. For each allowed change
         * we add an object that will make such a change once all changes have
         * been approved. As we find matches in the old channel description we
         * remove those parameters. Then any left there after processing those
         * of the new channel description indicate parameters that were removed.
         */
        FragmentChannelInfo fragChanInf = null;
        Map pubParms = getPublishedChannelParametersMap(
                newChanDesc.getChannelPublishId());
        
        if (isIncorporated)
            fragChanInf = store.getFragmentChannelInfo(nodeId);
        Map oldParms = new HashMap(oldChanDesc.getParameterMap());
        for (Iterator itr = newChanDesc.getParameterMap().entrySet()
                .iterator(); itr.hasNext();)
        {
            Map.Entry e = (Entry) itr.next();
            String name = (String) e.getKey();
            String newVal = (String) e.getValue();
            String oldVal = (String) oldParms.remove(name);

            if (oldVal == null)
            {
                /*
                 * not in old description so this is a new ad-hoc parameter
                 */
                pendingActions.add(new LPAAddParameter
                        (nodeId, name, newVal, owner, ilfNode));
            } else if (!oldVal.equals(newVal)) 
            {
                /*
                 * changing value, is it allowed by the channel and by the
                 * fragment if this came from a fragment?
                 */
                if (!oldChanDesc.canOverrideParameter(name))
                    throw new PortalException("This instance of "
                            + oldChanDesc.getTitle() 
                            + " does not allow overriding parameter " 
                            + name);
                if (isIncorporated )
                {
                    /*
                     * if the fragment does not have a value for this parm then
                     * this is an ad-hoc value and we need a directive to
                     * persist the user's desired value. if the frament does
                     * have a value and it is the same as the new value then we
                     * can remove the override since it won't accomplish
                     * anything. if the fragment does have a value and it is
                     * different then we need the directive to persist the
                     * user's desired value.
                     */
                    String fragValue = fragChanInf.getParameterValue(name);

                    if (fragValue == null)
                    {
                        /*
                         * so fragment doesn't override. See if the value
                         * specified matches that of the channel definition
                         */
                        ChannelParameter cp = 
                            (ChannelParameter) pubParms.get(name);
                        
                        if (cp != null && cp.getValue().equals(newVal))
                            /*
                             * new value matches that of published channel to
                             * remove any user parameter spec since not needed
                             */
                            pendingActions.add(new LPARemoveParameter
                                    (nodeId, name, owner, ilfNode));
                        else
                            /*
                             * value doesn't match that of published chanel so
                             * we need change any existing parameter spec or add
                             * a new one if it doesn't exist.
                             */
                            pendingActions.add(new LPAChangeParameter
                                    (nodeId, name, newVal, owner, ilfNode));
                    } else if (!fragValue.equals(newVal))
                    {
                        /*
                         * so fragment does specify and user value is different
                         * so change any existing parameter spec or add a new
                         * one if it doesn't exist.
                         */
                        pendingActions.add(new LPAChangeParameter
                                (nodeId, name, newVal, owner, ilfNode));
                    } else
                    {
                        /*
                         * new val same as fragment value so don't persist.
                         * remove any parameter spec if it exists.
                         */
                        pendingActions.add(new LPAResetParameter
                                (nodeId, name, fragValue, owner, ilfNode));
                    }
                }
                else // not incorporated from a fragment 
                {
                    /*
                     * see if the value specified matches that of the channel
                     * definition
                     */
                    ChannelParameter cp = 
                        (ChannelParameter) pubParms.get(name);
                    
                    if (cp.getValue().equals(newVal))
                        pendingActions.add(new LPARemoveParameter
                                (nodeId, name, owner, ilfNode));
                    else
                        pendingActions.add(new LPAChangeParameter
                                (nodeId, name, newVal, owner, ilfNode));
                }
            }
        }
        /*
         * So any parameters remaining in the oldParms map at this point didn't
         * match those in the new channel description which means that they were
         * removed. So remove any parameter spec if it exists.
         */
        for (Iterator itr = oldParms.entrySet().iterator(); itr
                .hasNext();)
        {
            Map.Entry e = (Entry) itr.next();
            String name = (String) e.getKey();
            pendingActions.add(new LPARemoveParameter
                    (nodeId, name, owner, ilfNode));
        }
        /*
         * if we make it to this point then all edits made are allowed so
         * process the actions to push the edits into the layout
         */
        for(Iterator itr = pendingActions.iterator(); itr.hasNext();)
        {
            ILayoutProcessingAction action = 
                (ILayoutProcessingAction) itr.next();
            action.perform();
        }
    }
    /**
     * Return a map parameter names to channel parameter objects representing
     * the parameters specified at publish time for the channel with the
     * passed-in publish id.
     * 
     * @param channelPublishId
     * @return
     * @throws PortalException
     */
    private Map getPublishedChannelParametersMap(String channelPublishId)
            throws PortalException
    {
        try
        {
            IChannelRegistryStore crs = ChannelRegistryStoreFactory
                .getChannelRegistryStoreImpl();
            int pubId = Integer.parseInt(channelPublishId);
            ChannelDefinition def = crs.getChannelDefinition(pubId);
            return def.getParametersAsUnmodifiableMap();
        } catch (Exception e)
        {
            throw new PortalException("Unable to acquire channel definition.",
                    e);
        }
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
                throw new PortalException("Unable to find a sibling node " +
                        "with id=\""+nextSiblingId+"\".  Occurred " +
                            "in layout for " 
                            + owner.getAttribute(IPerson.USERNAME) + ".");
            }
            if(!parent.getId().equals(getParentId(nextSiblingId))) {
                throw new PortalException("Given sibling (\""+nextSiblingId
                        +"\") is not a child of a given parentId (\""
                        +parent.getId()+"\"). Occurred " +
                            "in layout for " 
                            + owner.getAttribute(IPerson.USERNAME) + ".");
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
     * Returns true if we are dealing with a fragment layout or if editing of
     * attributes is allowed, or the node is a channel since ad-hoc parameters
     * can always be added.
     */
    public boolean canUpdateNode( IUserLayoutNodeDescription node )
    {
        if ( node == null )
            return false;

        return isFragment || node.isEditAllowed()
                || node instanceof IUserLayoutChannelDescription;
    }

    /**
     * The structure of add targets where a node is to fall after a sibling is:
     * 
     * &lt;addTarget parentID="someID" nextID="newFollowingSiblingID"/&gt;
     * 
     * For targets at the end of the sibling list the structure is:
     * 
     * &lt;addTarget parentID="someID" nextID=""/&gt;
     */
    public void markAddTargets(IUserLayoutNodeDescription node) {
        
        // we use the old prefs channel for now so ignore this.
        // TODO add for conversion to integrated modes theme
        
        // get all folders
        // so where can we add 
        this.updateCacheKey();
    }


    public void markMoveTargets(String nodeId) throws PortalException {
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
            throw new PortalException("Node with id=\""+nodeId+
                    "\" doesn't exist. Occurred in layout for " 
                    + owner.getAttribute(IPerson.USERNAME) + ".");
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
            throw new PortalException("Node with id=\""+nodeId+
                    "\" doesn't exist. Occurred " +
                            "in layout for " 
                            + owner.getAttribute(IPerson.USERNAME) + ".");
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
            throw new PortalException("Node with id=\""+nodeId+
                    "\" doesn't exist. Occurred in layout for " 
                            + owner.getAttribute(IPerson.USERNAME) + ".");
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
        if(node instanceof IUserLayoutFolderDescription) {
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
            LOG.error("Encountered exception while trying to identify " +
                    "subscribe channel id for the fname=\""+fname+"\"" +
                            " in layout of" 
                            + owner.getAttribute(IPerson.USERNAME) + ".", e);
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
        String expression = "//layout/folder";
        try
        {
            if (rootNodeId == null)
            {
                XPathFactory fac = XPathFactory.newInstance();
                XPath xpath = fac.newXPath();
                Element rootNode = (Element) xpath.evaluate(expression, this
                        .getUserLayoutDOM(), XPathConstants.NODE);
                rootNodeId = rootNode.getAttribute("ID");
            }
        } catch (XPathExpressionException e)
        {
            LOG.error("Unable to locate node with path " + expression + 
                    " in layout of" 
                            + owner.getAttribute(IPerson.USERNAME) + ".", e);
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
            return new ChannelDescription();
        }
    }

    /**
     * Handle layout specific parameters posted with the request.
     */
    public void processLayoutParameters(IPerson person, UserPreferences userPrefs, HttpServletRequest req) throws PortalException
    {
        try
        {
            String newNodeId = null;

            // Sending the theme stylesheets parameters based on the user
            // security context
            ThemeStylesheetUserPreferences themePrefs = userPrefs
                    .getThemeStylesheetUserPreferences();
            StructureStylesheetUserPreferences structPrefs = userPrefs
                    .getStructureStylesheetUserPreferences();

            String authenticated = String.valueOf(person.getSecurityContext()
                    .isAuthenticated());
            structPrefs.putParameterValue("authenticated", authenticated);
            /*
            String userName = person.getFullName();
            if (userName != null && userName.trim().length() > 0)
                themePrefs.putParameterValue("userName", userName);
            try
            {
                if (ChannelStaticData.getAuthorizationPrincipal(person)
                        .canPublish())
                {
                    themePrefs.putParameterValue("authorizedFragmentPublisher",
                            "true");
                    themePrefs.putParameterValue("authorizedChannelPublisher",
                            "true");
                }
            } catch (Exception e)
            {
                LOG.error("Exception determining publish rights for " + person,
                        e);
            }
            */
            
            String[] values;

            if ((values = req.getParameterValues("uP_request_move_targets")) != null)
            {
                if (values[0].trim().length() == 0)
                    values[0] = null;
                this.markMoveTargets(values[0]);
            } else
            {
                this.markMoveTargets(null);
            }

            if ((values = req.getParameterValues("uP_request_add_targets")) != null)
            {
                String value;
                int nodeType = values[0].equals("folder")
                        ? IUserLayoutNodeDescription.FOLDER
                        : IUserLayoutNodeDescription.CHANNEL;
                IUserLayoutNodeDescription nodeDesc = this
                        .createNodeDescription(nodeType);
                nodeDesc.setName("Unnamed");
                if (nodeType == IUserLayoutNodeDescription.CHANNEL
                        && (value = req.getParameter("channelPublishID")) != null)
                {
                    String contentPublishId = value.trim();
                    if (contentPublishId.length() > 0)
                    {
                        ((IUserLayoutChannelDescription) nodeDesc)
                                .setChannelPublishId(contentPublishId);
                        themePrefs.putParameterValue("channelPublishID",
                                contentPublishId);
                    }
                }
                /*
                else if (nodeType == IUserLayoutNodeDescription.FOLDER
                        && (value = req.getParameter("fragmentPublishID")) != null)
                {
                    String contentPublishId = value.trim();
                    String fragmentRootId = CommonUtils.nvl(req
                            .getParameter("fragmentRootID"));
                    if (contentPublishId.length() > 0
                            && fragmentRootId.length() > 0)
                    {
                        IALFolderDescription folderDesc = (IALFolderDescription) nodeDesc;
                        folderDesc.setFragmentId(contentPublishId);
                        folderDesc.setFragmentNodeId(fragmentRootId);
                    }
                    //themePrefs.putParameterValue("uP_fragmentPublishID",contentPublishId);
                }
                */
                newNodeDescription = nodeDesc;
                this.markAddTargets(newNodeDescription);
            } else
            {
                this.markAddTargets(null);
            }

            if ((values = req.getParameterValues("uP_add_target")) != null)
            {
                String[] values1, values2;
                String value = null;
                values1 = req.getParameterValues("targetNextID");
                if (values1 != null && values1.length > 0)
                    value = values1[0];
                if ((values2 = req.getParameterValues("targetParentID")) != null)
                {
                    if (newNodeDescription != null)
                    {
                        if (CommonUtils.nvl(value).trim().length() == 0)
                            value = null;

                        // Adding a new node
                        newNodeId = this.addNode(newNodeDescription,
                                values2[0], value).getId();
                        /*
                        // if the new node is a fragment being added - we need
                        // to re-load the layout
                        if (newNodeDescription instanceof IALFolderDescription)
                        {
                            IALFolderDescription folderDesc = (IALFolderDescription) newNodeDescription;
                            if (folderDesc.getFragmentNodeId() != null)
                            {
                                this.saveUserLayout();
                                this.loadUserLayout();
                            }
                        }
                        */
                    }
                }
                newNodeDescription = null;
            }

            if ((values = req.getParameterValues("uP_move_target")) != null)
            {
                String[] values1, values2;
                String value = null;
                values1 = req.getParameterValues("targetNextID");
                if (values1 != null && values1.length > 0)
                    value = values1[0];
                if ((values2 = req.getParameterValues("targetParentID")) != null)
                {
                    if (CommonUtils.nvl(value).trim().length() == 0)
                        value = null;
                    this.moveNode(values[0], values2[0], value);
                }
            }

            if ((values = req.getParameterValues("uP_rename_target")) != null)
            {
                String[] values1;
                if ((values1 = req.getParameterValues("uP_target_name")) != null)
                {
                    IUserLayoutNodeDescription nodeDesc = this
                            .getNode(values[0]);
                    if (nodeDesc != null)
                    {
                        String oldName = nodeDesc.getName();
                        nodeDesc.setName(values1[0]);
                        if (!this.updateNode(nodeDesc))
                            nodeDesc.setName(oldName);
                    }
                }
            }

            if ((values = req.getParameterValues("uP_remove_target")) != null)
            {
                for (int i = 0; i < values.length; i++)
                {
                    this.deleteNode(values[i]);
                }
            }

            String param = req.getParameter("uP_cancel_targets");
            if (param != null && param.equals("true"))
            {
                this.markAddTargets(null);
                this.markMoveTargets(null);
                newNodeDescription = null;
            }

            param = req.getParameter("uP_reload_layout");
            if (param != null && param.equals("true"))
            {
                this.loadUserLayout();
            }

            /*
             param = req.getParameter("uPcFM_action");
             if ( param != null ) {
             String fragmentId = req.getParameter("uP_fragmentID");
             if ( param.equals("edit") && fragmentId != null ) {
             if ( CommonUtils.parseInt(fragmentId) > 0 )
             this.loadFragment(fragmentId);
             else
             this.loadUserLayout();
             } else if ( param.equals("save") ) {
             this.saveFragment();
             }
             }
             */

            // If we have created a new node we need to let the structure XSL know about it
            structPrefs.putParameterValue("newNodeID", CommonUtils
                    .nvl(newNodeId));
            /*
            // Sending the parameter indicating whether the layout or the fragment 
            // is loaded in the preferences mode
            structPrefs.putParameterValue("current_structure", this
                    .isFragmentLoaded() ? "fragment" : "layout");
            */
        } catch (Exception e)
        {
            throw new PortalException(e);
        }
    }
}
