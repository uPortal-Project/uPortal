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

package org.jasig.portal.layout.dlm;

import java.util.*;
import java.util.Map.Entry;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathConstants;

import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.PortalException;
import org.jasig.portal.events.IPortalLayoutEventFactory;
import org.jasig.portal.layout.IFolderLocalNameResolver;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.PortletSubscribeIdResolver;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription.LayoutNodeType;
import org.jasig.portal.layout.node.UserLayoutFolderDescription;
import org.jasig.portal.layout.simple.SimpleLayout;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.AdminEvaluator;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.provider.AuthorizationImpl;
import org.jasig.portal.spring.locator.PortletDefinitionRegistryLocator;
import org.jasig.portal.spring.locator.UserIdentityStoreLocator;
import org.jasig.portal.xml.XmlUtilities;
import org.jasig.portal.xml.xpath.XPathOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A layout manager that provides layout control through
 * layout fragments that are derived from regular portal user accounts.
 *
 * @author Mark Boyd
 * @version 1.0  $Revision$ $Date$
 * @since uPortal 2.5
 */
public class DistributedLayoutManager implements IUserLayoutManager, IFolderLocalNameResolver, InitializingBean
{
    public static final String RCS_ID = "@(#) $Header$";
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private XmlUtilities xmlUtilities;
    private ILayoutCachingService layoutCachingService;
    private IUserLayoutStore distributedLayoutStore;
    private XPathOperations xpathOperations;
    private IPortalLayoutEventFactory portalEventFactory;
    private IAuthorizationService authorizationService;
    
    protected final IPerson owner;
    protected final IUserProfile profile;

    /**
     * Holds the bean name of the configured folder label policy if any that is 
     * defined in the dlm context configuration.  
     */
    static final String FOLDER_LABEL_POLICY = "FolderLabelPolicy";
    
    /**
     * Documents what folder types are gracefully degraded.
     * This is private because it's an implementation detail of DLM, but it's called out here because
     * it matters if you, say, invent a new folder type.
     */
    private static final String[] FOLDER_TYPES_GRACEFULLY_DEGRADED = {
            IUserLayoutFolderDescription.HEADER_TYPE,
            IUserLayoutFolderDescription.FOOTER_TYPE,
            IUserLayoutFolderDescription.SIDEBAR_TYPE };


    /**
     * Converts the folder types gracefully degraded documented immediately previously to a useful Set.
     */
    private static final Set<String> FOLDER_TYPES_GRACEFULLY_DEGRADED_SET =
            new HashSet<String>(Arrays.asList( FOLDER_TYPES_GRACEFULLY_DEGRADED));

    protected final static Random rnd=new Random();
    protected String cacheKey="initialKey";
    protected String rootNodeId = null;

    private boolean channelsAdded = false;
    private boolean isFragmentOwner = false;

    public DistributedLayoutManager(IPerson owner, IUserProfile profile) throws PortalException
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
        
        // cache the relatively lightwieght userprofile for use in 
        // in layout PLF loading
        owner.setAttribute(IUserProfile.USER_PROFILE, profile);
        
        this.owner = owner;
        this.profile = profile;
    }
    
    @Autowired
    public void setAuthorizationService(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Autowired
    public void setXpathOperations(XPathOperations xpathOperations) {
        this.xpathOperations = xpathOperations;
    }

    @Autowired
    public void setXmlUtilities(XmlUtilities xmlUtilities) {
        this.xmlUtilities = xmlUtilities;
    }

    @Autowired
    public void setLayoutCachingService(ILayoutCachingService layoutCachingService) {
        this.layoutCachingService = layoutCachingService;
    }

    @Autowired
    public void setDistributedLayoutStore(IUserLayoutStore distributedLayoutStore) {
        this.distributedLayoutStore = distributedLayoutStore;
    }

    @Autowired
    public void setPortalEventFactory(IPortalLayoutEventFactory portalEventFactory) {
        this.portalEventFactory = portalEventFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {



        // Ensure a new layout gets loaded whenever a user logs in except for guest users
        if (!owner.isGuest()) {
            // logs the full owner, profile objects just once per user session at DLM init
            // subsequent log messages reference by ID
            logger.trace("Initting DLM for non-guest user {} with profile {}, clearing cached layout.",
                    owner, profile);

            this.layoutCachingService.removeCachedLayout(owner, profile);
        } else {
            logger.trace("Initting DLM for guest user {} with profile {}, *not* clearing cached layout.",
                    owner, profile);
        }

        
        this.loadUserLayout();
        
        // verify that we have the minimum layout necessary to render the
        // portal and reset it if we do not.
        this.getRootFolderId();

        // This listener determines if one or more channels have been
        // added, and sets a state variable which is reset when the
        // layout saved event is triggered.
//        this.addLayoutEventListener(new LayoutEventListenerAdapter()
//        {
//            @Override
//            public void channelAdded(LayoutEvent ev) {
//                channelsAdded = true;
//            }
//            @Override
//            public void layoutSaved() {
//                channelsAdded = false;
//            }
//        });
    }

    private void setUserLayoutDOM(DistributedUserLayout userLayout) {

        this.layoutCachingService.cacheLayout(owner, profile, userLayout);
        this.updateCacheKey();

        // determine if this is a layout fragment by looking at the root node
        // for a cp:fragment attribute.
        Element layout = userLayout.getLayout().getDocumentElement();
        Node attr = layout.getAttributeNodeNS( Constants.NS_URI,
                                               Constants.LCL_FRAGMENT_NAME );
        this.isFragmentOwner = attr != null;
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public Document getUserLayoutDOM()
    {
        final DistributedUserLayout userLayout = getDistributedUserLayout();
        return userLayout.getLayout();
    }

    protected DistributedUserLayout getDistributedUserLayout() {
        DistributedUserLayout userLayout = this.layoutCachingService.getCachedLayout(owner, profile);
        if ( null == userLayout )
        {
            logger.debug("Loading layout from store for owner user id {}.", owner.getID());

            userLayout = this.distributedLayoutStore.getUserLayout(this.owner,this.profile);
            
            final Document userLayoutDocument = userLayout.getLayout();

            // DistributedLayoutManager shall gracefully remove channels 
            // that the user isn't authorized to render from folders of type 
            // 'header' and 'footer'.
            IAuthorizationPrincipal principal = authorizationService.newPrincipal(owner.getUserName(), IPerson.class);
            NodeList nodes = userLayoutDocument.getElementsByTagName("folder");
            for (int i=0; i < nodes.getLength(); i++) {
          	  Element fd = (Element) nodes.item(i);
          	  String type = fd.getAttribute("type");
          	  if (type != null && FOLDER_TYPES_GRACEFULLY_DEGRADED_SET.contains(type)) {
          		  // Here's where we do the work...

          		logger.trace("Examining folder of type {} of user {} for non-authorized channels.",
                        type, owner.getID() );


          		  NodeList channels = fd.getElementsByTagName("channel");
          		  for (int j=0; j < channels.getLength(); j++) {
          			  Element ch = (Element) channels.item(j);
          			  try {
          				  String chanId = ch.getAttribute("chanID");
          				  if (!principal.canRender(chanId)) {
          					  fd.removeChild(ch);
          					  logger.debug("Removing channel {} from folder of type {} of user {} " +
                                      "because user not authorized to render it.",
                                      ch, type, owner.getID());

          				  }
          			  } catch (Throwable t) {
          				  // Log this...
          				  logger.warn("Unable to analyze channel element {}.", ch, t);
          			  }
          		  }
          	  } else {
                  logger.trace("Not cleaning up folder of type {} because its type is not among {}", type,
                          FOLDER_TYPES_GRACEFULLY_DEGRADED_SET);
              }
            }
            
            setUserLayoutDOM( userLayout );
        }
        return userLayout;
    }
    
    @Override
    public XMLEventReader getUserLayoutReader() {
        Document ul = this.getUserLayoutDOM();
        if (ul == null) {
            throw new PortalException("User layout has not been initialized for " + owner.getAttribute(IPerson.USERNAME));
        }
        
        final XMLInputFactory xmlInputFactory = this.xmlUtilities.getXmlInputFactory();
        
        final DOMSource layoutSource = new DOMSource(ul);
        try {
            return xmlInputFactory.createXMLEventReader(layoutSource);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create Layout XMLStreamReader for user: " + owner.getAttribute(IPerson.USERNAME), e);
        }
    }

    /**
     * Instantiates an empty transformer to generate SAX events for the layout.
     * 
     * @return Transformer
     * @throws PortalException
     */
    private Transformer getEmptyTransformer() throws PortalException
    {
        Transformer xfrmr = null;
        try 
        {
            xfrmr = TransformerFactory.newInstance().newTransformer();
        } 
        catch (Exception e) 
        {
            throw new PortalException("Unable to instantiate transformer.", e);
        }
        return xfrmr;
    }

    public synchronized void loadUserLayout() throws PortalException {
        this.loadUserLayout(false);
    }

    public synchronized void loadUserLayout(boolean reload) throws PortalException {
        Document uli= null;
        try {
            //Clear the loaded document first if this is a forced reload
            if (reload) {
                this.layoutCachingService.removeCachedLayout(owner, profile);
            }
            
            uli=getUserLayoutDOM();
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
    }

    public synchronized void saveUserLayout() throws PortalException{

        logger.trace("Saving user layout for owner {}, profile {}", owner.getID(), profile.getProfileId());

        Document uld=this.getUserLayoutDOM();
        
        if(uld==null) {
            throw new PortalException("UserLayout has not been initialized for " 
                    + owner.getAttribute(IPerson.USERNAME) + ".");
        }
        try {
            this.distributedLayoutStore.setUserLayout(this.owner,this.profile,uld,channelsAdded);
        } catch (Exception e) {
            throw new PortalException("Exception encountered while " +
                    "saving layout for userId=" + this.owner.getID() +
                    ", profileId=" + this.profile.getProfileId(),e);
        }

        this.channelsAdded = false;
    }
    

    @Override
    public Set<String> getAllSubscribedChannels() {
        final Document uld = this.getUserLayoutDOM();

        if (uld == null) {
            throw new PortalException("UserLayout has not been initialized for " + owner.getAttribute(IPerson.USERNAME));
        }
        
        final NodeList channelElements = uld.getElementsByTagName(CHANNEL);
        
        final Set<String> allSubscribedChannels = new LinkedHashSet<String>(channelElements.getLength());
        for (int nodeIndex = 0; nodeIndex < channelElements.getLength(); nodeIndex++) {
            final Element channelElement = (Element)channelElements.item(nodeIndex);
            final String subscribeId = channelElement.getAttribute("ID");
            allSubscribedChannels.add(subscribeId);
        }
        
        return allSubscribedChannels;
    }

    public IUserLayoutNodeDescription getNode( String nodeId )
        throws PortalException
    {
        logger.trace("Getting node {} in layout for user {} in profile {}",
                nodeId, owner.getID(), profile.getProfileId());

        if (nodeId == null)
            return null;
        
        Document uld=this.getUserLayoutDOM();

        if( uld==null )
            throw new PortalException("UserLayout has not been initialized for " 
                    + owner.getAttribute(IPerson.USERNAME) + ".");

        // find an element with a given id
        Element element = uld.getElementById( nodeId );
        if( element == null )
        {
            logger.debug("Could not find layout node with id {} in user {}'s profile {}",
                    nodeId, owner.getID(), profile.getProfileId());
            throw new PortalException("Element with ID=\"" + nodeId +
                                      "\" doesn't exist for " 
                    + owner.getAttribute(IPerson.USERNAME) + "." );
        }
        // instantiate the node description
        IUserLayoutNodeDescription desc = createNodeDescription(element);
        if (nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX)
                && desc instanceof ChannelDescription)
        {
            FragmentChannelInfo info = this.distributedLayoutStore.getFragmentChannelInfo(nodeId);
            ((ChannelDescription)desc).setFragmentChannelInfo(info);
            logger.trace("nodeId {} started with {} so applied fragment channel info {}.", nodeId,
                    Constants.FRAGMENT_ID_USER_PREFIX, info);
        }

        logger.trace("Got node [{}] for nodeId {} for user {} in profile {}",
                desc, nodeId, owner.getID(), profile.getProfileId());
        return desc;
    }

    public IUserLayoutNodeDescription addNode( IUserLayoutNodeDescription node,
                                              String parentId,
                                              String nextSiblingId )
        throws PortalException
    {

        logger.trace("Adding node {} with parentId {} and nextSiblingId {} for {} in {}.",
                node, parentId, nextSiblingId, owner.getID(), profile.getProfileId());

        boolean isChannel=false;
        IUserLayoutNodeDescription parent=this.getNode(parentId);
        if( canAddNode( node, parent, nextSiblingId ) )
        {
            // assign new Id
            try {
                if(node instanceof IUserLayoutChannelDescription) {
                    isChannel=true;
                    node.setId(this.distributedLayoutStore.generateNewChannelSubscribeId(owner));
                } else {
                    node.setId(this.distributedLayoutStore.generateNewFolderId(owner));
                }
            } catch (Exception e) {
                    throw new PortalException("Exception encountered while " +
                            "generating new user layout node Id for  for " 
                        + owner.getAttribute(IPerson.USERNAME), e);
            }

            Document uld=getUserLayoutDOM();
            Element childElement=node.getXML(uld);
            Element parentElement= uld.getElementById(parentId);
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

            // fire event
            final int layoutId = this.getLayoutId();
            if(isChannel) {
                this.channelsAdded = true;
                final String fname = ((IUserLayoutChannelDescription)node).getFunctionalName();
                this.portalEventFactory.publishPortletAddedToLayoutPortalEvent(this, this.owner, layoutId, parent.getId(), fname);
            } 
            else {
                this.portalEventFactory.publishFolderAddedToLayoutPortalEvent(this, this.owner, layoutId, node.getId());
            }

            logger.trace("Added {} with parentId {} and nextSiblingId {}.", node, parentId, nextSiblingId);
            
            return node;

        } else {
            logger.debug("Can't add node {} w/ proposed parent {} and nextSibling {} for user {} in profile {}.",
                    node, parentId, nextSiblingId, owner.getID(), profile.getProfileId());
        }
        return null;
    }

    public boolean moveNode( String nodeId,
                             String parentId,
                             String nextSiblingId )
        throws PortalException
    {

        logger.trace("Attempting to move user {}'s nodeId {} " +
                "to have parentId {} and nextSiblingId {} in profile {}",
                owner.getID(), nodeId, parentId, nextSiblingId, profile.getProfileId());

        IUserLayoutNodeDescription parent=this.getNode(parentId);
        IUserLayoutNodeDescription node=this.getNode(nodeId);
        String oldParentNodeId=getParentId(nodeId);
        if(canMoveNode(node,parent,nextSiblingId)) {
            // must be a folder
            Document uld=this.getUserLayoutDOM();
            Element childElement = uld.getElementById(nodeId);
            Element parentElement = uld.getElementById(parentId);
            if(nextSiblingId==null) {
                parentElement.appendChild(childElement);
            } else {
                Node nextSibling=uld.getElementById(nextSiblingId);
                parentElement.insertBefore(childElement,nextSibling);
            }
            this.updateCacheKey();

            // propagate the change into the PLF
            Element oldParent = uld.getElementById(oldParentNodeId);
            TabColumnPrefsHandler.moveElement( childElement,
                                               oldParent,
                                               owner );
            // fire event
            final int layoutId = this.getLayoutId();
            if (node instanceof IUserLayoutChannelDescription) {
                this.channelsAdded = true;
                final String fname = ((IUserLayoutChannelDescription)node).getFunctionalName();
                this.portalEventFactory.publishPortletMovedInLayoutPortalEvent(this, this.owner, layoutId, oldParentNodeId, parent.getId(), fname);
            } 
            else {
                this.portalEventFactory.publishFolderMovedInLayoutPortalEvent(this, this.owner, layoutId, oldParentNodeId, parent.getId());
            }

            logger.trace("Moved user {}'s node {} to new parentId {} and nextSiblingId {}. " +
                    "From parentId {} in profile {}.",
                    owner.getID(), node, parentId, nextSiblingId, oldParentNodeId, profile.getProfileId());

            return true;
        } else {
            logger.debug("Can't move node {} to have parentId {} and nextSiblingId {} in profile {} for user {}.",
                    node, parentId, nextSiblingId, profile.getProfileId(), owner.getID());
        }
        return false;
    }

    public boolean deleteNode( String nodeId )
        throws PortalException {

        logger.trace("Attempting to delete node {} in context of user {} and profile {}.",
                nodeId, owner.getID(), profile.getProfileId());

        if(canDeleteNode(nodeId)) {
            IUserLayoutNodeDescription nodeDescription=this.getNode(nodeId);
            String parentNodeId=this.getParentId(nodeId);

            Document uld=this.getUserLayoutDOM();
            Element ilfNode = uld.getElementById(nodeId);
            Node parent=ilfNode.getParentNode();
            if(parent!=null) {
                parent.removeChild(ilfNode);
            } else {
                throw new PortalException("Node \""+nodeId +
                        "\" has a NULL parent for layout of " 
                    + owner.getAttribute(IPerson.USERNAME) + ".");
            }
            this.updateCacheKey();

            // now push into the PLF
            TabColumnPrefsHandler.deleteNode( ilfNode, (Element) parent,
                                              owner );
            // inform the listeners
            final int layoutId = this.getLayoutId();
            if (nodeDescription instanceof IUserLayoutChannelDescription) {
                final IUserLayoutChannelDescription userLayoutChannelDescription = (IUserLayoutChannelDescription)nodeDescription;
                this.portalEventFactory.publishPortletDeletedFromLayoutPortalEvent(this, this.owner, layoutId, parentNodeId, userLayoutChannelDescription.getFunctionalName());
            }
            else {
                this.portalEventFactory.publishFolderDeletedFromLayoutPortalEvent(this, this.owner, layoutId, parentNodeId, nodeDescription.getId(), nodeDescription.getName());
            }

            logger.trace("Successfully deleted node {} in context of user {} and profile {}.",
                    nodeId, owner.getID(), profile.getProfileId());
            return true;
        }

        logger.debug("Requested delete of node {} for owner {} and profile {} but delete not allowed.",
                nodeId, owner.getID(), profile.getProfileId());
        return false;
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
        // no trace log here because would be redundant with contextualized trace logging below.

        if( canUpdateNode( node ) )
        {
            String nodeId = node.getId();
            IUserLayoutNodeDescription oldNode = getNode( nodeId );

            logger.trace("Attempting to updateNode {} from old node {} for user {} in profile {}.",
                    node, oldNode, owner.getID(), profile.getProfileId());

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
                } else {
                    logger.warn("Existing node with id {} is a folder {} " +
                            "but proposed new node {} is a channel so doing nothing.",
                            nodeId, oldFolderDesc, node);
                    // TODO: really ought to return false here to reflect doing nothing.
                    // NOT returning false to stay true to existing (bugged?) behavior.
                }
            }
            this.updateCacheKey();
            return true;
        } else {
            logger.debug("Cannot update node {} for user {} in profile {}.",
                    node, owner.getID(), profile.getProfileId());
        }
        return false;
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

        logger.trace("entering updateFolderNode with id {} from {} to {}.",
                nodeId, oldFolderDesc, newFolderDesc);

        Element ilfNode = (Element) getUserLayoutDOM().getElementById(nodeId);
        List<ILayoutProcessingAction> pendingActions 
            = new ArrayList<ILayoutProcessingAction>();

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
        if (isFragmentOwner
                && (newFolderDesc.isDeleteAllowed() != 
                    oldFolderDesc.isDeleteAllowed()
                 || newFolderDesc.isEditAllowed() != 
                     oldFolderDesc.isEditAllowed() 
                 || newFolderDesc.isAddChildAllowed() != 
                     oldFolderDesc.isAddChildAllowed() 
                 || newFolderDesc.isMoveAllowed() != 
                    oldFolderDesc.isMoveAllowed()))
        {
            LPAEditRestriction newEditRestriction = new LPAEditRestriction(owner, ilfNode,
                    newFolderDesc.isMoveAllowed(),
                    newFolderDesc.isDeleteAllowed(),
                    newFolderDesc.isEditAllowed(),
                    newFolderDesc.isAddChildAllowed());

            pendingActions.add(newEditRestriction);

            logger.trace("Owner {} is a fragment owner and delete, edit, add, or move " +
                    "restrictions are changing in updating node {} from {} to {} " +
                    "so added edit restrictions {} to pendingActions.",
                    owner.getID(), nodeId, oldFolderDesc, newFolderDesc, newEditRestriction);
        }
        
        // ATT: Name
        updateNodeAttribute(ilfNode, nodeId, Constants.ATT_NAME, newFolderDesc
                .getName(), oldFolderDesc.getName(), pendingActions);

        logger.trace("Performing {} in updating folder node {} from {} to {}",
                pendingActions, nodeId, oldFolderDesc, newFolderDesc);

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
            String attName, String newVal, String oldVal, 
            List<ILayoutProcessingAction> pendingActions) 
    throws PortalException
    {
        // no trace logging here because would be redundant with guaranteed at least one contextualized log statement
        // below

        if (newVal == null && oldVal != null ||
                newVal != null && oldVal == null ||
                (newVal != null && oldVal != null &&
                 ! newVal.equals(oldVal)))
        {
            boolean isIncorporated = 
                nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX); 
            if (isIncorporated)
            {
                /*
                 * Is a change to this attribute allowed?
                 */
                FragmentNodeInfo fragNodeInf = this.distributedLayoutStore.getFragmentNodeInfo(nodeId);
                if (fragNodeInf == null )
                {
                    LPAChangeAttribute attributeChangeAction = new LPAChangeAttribute(nodeId, attName,
                            newVal, owner, ilfNode, isFragmentOwner);

                    logger.warn("Node with id {} (incorporated since started with {}) is " +
                            "not present in the layout store.  Deleted in the fragment owner layout " +
                            "but user was logged in and so is editing a " +
                            "no-longer-in-underlying-fragment incorporated node? " +
                            "Anyway, added {} to pending actions," +
                            " to change attribute named {} from value {} to value {}.",
                            nodeId, Constants.FRAGMENT_ID_USER_PREFIX, attributeChangeAction, attName,
                            oldVal, newVal);

                    /*
                     * null should only happen if a node was deleted in the
                     * fragment and a user happened to already be logged in and
                     * edited an attribute on that node.
                     */ 
                    pendingActions.add(attributeChangeAction);
                }
                else if (! fragNodeInf.canOverrideAttributes())
                {
                    logger.warn("User {} in profile {} attempted updating node attribute {} " +
                            "on node {} from value {} to value {} " +
                            "but is not permitted to override attributes.",
                            owner.getID(), profile.getProfileId(), attName, nodeId, oldVal, newVal );

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
                    LPAChangeAttribute attributeChange = new LPAChangeAttribute(nodeId, attName,
                            newVal, owner, ilfNode, isFragmentOwner);

                    /*
                     * If we get here we can override and the value is 
                     * different than that in the fragment so make the change.
                     */

                    logger.trace("User {} in profile {} is permitted to change " +
                            "attribute named {} of nodeId {} " +
                            "from value {} to value {} so queued change {}.",
                            owner.getID(), profile.getProfileId(), attName, nodeId,
                            oldVal, newVal, attributeChange);

                    pendingActions.add(attributeChange);
                }
                else 
                {
                    LPAResetAttribute attributeResetRequest = new LPAResetAttribute(nodeId, attName,
                            fragNodeInf.getAttributeValue(attName), owner, ilfNode);

                    logger.trace("User {} in profile {} is changing attribute named {} on nodeId {} " +
                            "from value {} to value {} " +
                            "which happens to match the value in the underlying fragment, " +
                            "so translated request to an attribute reset request {}.",
                            owner.getID(), profile.getProfileId(), attName, nodeId,
                            oldVal, newVal, attributeResetRequest);
                    /*
                     * The new value matches that in the fragment. 
                     */
                    pendingActions.add(attributeResetRequest);
                }
            }
            else
            {
                LPAChangeAttribute changeAttribute = new LPAChangeAttribute(nodeId, attName,
                        newVal, owner, ilfNode, isFragmentOwner);

                logger.trace("nodeId {} did not start with {} (owned by user {}) " +
                        "so authorizing change request {}.",
                        nodeId, Constants.FRAGMENT_ID_USER_PREFIX, owner.getID(), changeAttribute);

                /*
                 * Node owned by user so no checking needed. Just change it.
                 */
                pendingActions.add(changeAttribute);
            }
        } else {
            logger.trace("updateNodeAttribute: {} already has attribute {} with desired value {} so do nothing.",
                    nodeId, attName, newVal);
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
        Element ilfNode = (Element) getUserLayoutDOM().getElementById(nodeId);
        List<ILayoutProcessingAction> pendingActions 
            = new ArrayList<ILayoutProcessingAction>();
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
        if (isFragmentOwner
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
            fragChanInf = this.distributedLayoutStore.getFragmentChannelInfo(nodeId);
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
                        IPortletDefinitionParameter cp = 
                            (IPortletDefinitionParameter) pubParms.get(name);
                        
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
                     * definition.
                     */
                    IPortletDefinitionParameter cp = 
                        (IPortletDefinitionParameter) pubParms.get(name);
                    
                    if (cp != null && cp.getValue().equals(newVal))
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
        	IPortletDefinitionRegistry registry = PortletDefinitionRegistryLocator.getPortletDefinitionRegistry();
            IPortletDefinition def = registry.getPortletDefinition(channelPublishId);

            Map publishedChannelParametersMap = def.getParametersAsUnmodifiableMap();

            logger.trace("getPublishedParametersMap( chanPubId {} ) returning {}.",
                    channelPublishId, publishedChannelParametersMap);

            return publishedChannelParametersMap;
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

        boolean canAddNode = canAddNode(node,this.getNode(parentId),nextSiblingId);

        logger.trace("canAddNode(node {}, parentId {}, nextSiblingId {}) returning {}.",
                node, parentId, nextSiblingId, canAddNode);

        return canAddNode;

    }

    protected boolean canAddNode( IUserLayoutNodeDescription node,
                                  IUserLayoutNodeDescription parent,
                                  String nextSiblingId )
        throws PortalException
    {
        if (parent == null) {
            logger.warn("canAddNode( node {} parent {} nextSiblingId {}) returning false because parent null.",
                    node, parent, nextSiblingId);
            return false;
        }

        // make sure sibling exists and is a child of nodeId
        if(nextSiblingId!=null && ! nextSiblingId.equals("")) {
            IUserLayoutNodeDescription sibling=getNode(nextSiblingId);
            if(sibling==null) {

                logger.error("Error determining whether can add node {} to parent {} with nextSiblingId {}: " +
                        "Could not find node id matching that nextSiblingId.",
                        node, parent, nextSiblingId);

                throw new PortalException("Unable to find a sibling node " +
                        "with id=\""+nextSiblingId+"\".  Occurred " +
                            "in layout for " 
                            + owner.getAttribute(IPerson.USERNAME) + ".");
            }
            if(!parent.getId().equals(getParentId(nextSiblingId))) {

                logger.error("Error determining whether can add node {} to parent {} with nextSiblingId {}: " +
                        "desired next sibling doesn't share this parent.",
                        node, parent, nextSiblingId);

                throw new PortalException("Given sibling (\""+nextSiblingId
                        +"\") is not a child of a given parentId (\""
                        +parent.getId()+"\"). Occurred " +
                            "in layout for " 
                            + owner.getAttribute(IPerson.USERNAME) + ".");
            }
        }

        if ( ! node.isMoveAllowed() ) {
            logger.trace("canAddNode( {}, parent {}, nextSiblingId {}) returning false: move disallowed on node.",
                    node, parent, nextSiblingId);
            return false;
        }

        if ( parent instanceof IUserLayoutFolderDescription &&
             ! ( (IUserLayoutFolderDescription) parent).isAddChildAllowed() ) {
            logger.trace("canAddNode( {}, parent {}, nextSiblingId {}) returning false: " +
                    "addChild disallowed on proposed parent.",
                    node, parent, nextSiblingId);
            return false;
        }

        if ( nextSiblingId == null || nextSiblingId.equals("")) { // end of list targeted

            logger.trace("canAddNode( {}, parent {}, nextSiblingId {}) returning true: (no competing siblings).",
                    node, parent, nextSiblingId);

            return true;
        }

        // so lets see if we can place it at the end of the sibling list and
        // hop left until we get into the correct position.

        Enumeration sibIds = getVisibleChildIds( parent.getId() );
        List sibs = Collections.list(sibIds);

        if ( sibs.size() == 0 ) { // last node in list so should be ok

            logger.trace("canAddNode( {}, parent {}, nextSiblingId {}) returning true: " +
                    "(no *visible* competing sublings).",
                    node, parent, nextSiblingId);

            return true;
        }

        // reverse scan so that as changes are made the order of the, as yet,
        // unprocessed nodes is not altered.
        for( int idx = sibs.size() - 1;
             idx >= 0;
             idx-- )
        {
            IUserLayoutNodeDescription prev = getNode((String) sibs.get(idx));

            if ( ! MovementRules.canHopLeft( node, prev ) ) {

                logger.debug("canAddNode( node {}, parent {}, nextSiblingId {}) returning false: " +
                        "(can't have that nextSiblingId because node cannot hop left over {}.",
                        node, parent, nextSiblingId, prev);

                return false;
            }
            if ( prev.getId().equals( nextSiblingId ) ) {

                logger.trace("canAddNode( {}, parent {}, nextSiblingId {}) returning true: " +
                        "(leftward hop permissable across all hopped siblings among {}.",
                        node, parent, nextSiblingId, sibs);

                return true;
            }
        }

        logger.warn("canAddNode( {}, parent {}, nextSiblingId {}) returning false: " +
               "Did not find the desired next sibling for {} in {}.",
                node, parent, nextSiblingId, owner, profile);
        return false; // oops never found the sib
    }

    public boolean canMoveNode( String nodeId,
                                String parentId,
                                String nextSiblingId )
        throws PortalException
    {

        boolean canMoveNode = canMoveNode( this.getNode( nodeId ),
                this.getNode( parentId ),
                nextSiblingId );

        logger.trace("canMoveNode(nodeId {}, parentId {}, nextSiblingId {}) returning {}.",
                nodeId, parentId, nextSiblingId, canMoveNode);

        return canMoveNode;


    }

    protected boolean canMoveNode( IUserLayoutNodeDescription node,
                                   IUserLayoutNodeDescription parent,
                                   String nextSiblingId )
        throws PortalException
    {
        // are we moving to a new parent?
        if ( ! getParentId( node.getId() ).equals( parent.getId() ) ) {

            if (! node.isMoveAllowed()) {
                logger.trace("canMoveNode (node {}, parent {}, nextSiblingId {}) returning false: " +
                        "Node does not allow moves but move to a new parent is proposed.",
                        node, parent, nextSiblingId);
                return false;
            }

            if ( canAddNode( node, parent, nextSiblingId)) {

                logger.trace("canMoveNode( node {}, parent {}, nextSiblingId {}) returning true: " +
                    "Node allows move and proposed new parent allows add.",
                        node, parent, nextSiblingId);
                return true;
            }

            logger.trace("canMoveNode( node {}, parent {}, nextSiblingId {}) returning false: " +
                "Node allows move but proposed new parent does not allow add.",
                    node, parent, nextSiblingId);

            return false;
        }

        // same parent. which direction are we moving?
        Document uld = this.getUserLayoutDOM();
        Element parentE = uld.getElementById( parent.getId() );
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

        if (nodeIdx == -1) { // could not find node to move
            logger.debug("canMoveNode( node {}, parent {}, nextSiblingId {}) returning false: " +
                "Could not find node in user {} 's layout under parent.",
                    node, parent, nextSiblingId, owner.getID());
            return false;
        }

        if ( nextSiblingId != null &&
                sibIdx == -1 ) { // could not find sibling

            logger.debug("canMoveNode( node {}, parent {}, nextSiblingId {}) returning false: " +
                    "Could not find sibling in user {} 's layout under parent.",
                    node, parent, nextSiblingId, owner.getID());
            return false;
        }

        if (nodeIdx < sibIdx) { // move right

            boolean canMoveRight = canMoveRight( node.getId(), nextSiblingId );

            if (canMoveRight) {
                logger.trace("canMoveNode( node {}, parent {}, nextSiblingId {}) returning true: " +
                   "User {} is allowed this rightward move.",
                        node, parent, nextSiblingId, owner.getID());
                return true;
            } else {
                logger.trace("canMoveNode( node {}, parent {}, nextSiblingId {}) returning false: " +
                   "User {} is not allowed this rightward move.",
                        node, parent, nextSiblingId, owner.getID());
                return false;
            }

        }

        if (sibIdx == -1) { // append to end of siblings
            boolean canMoveRight = canMoveRight( node.getId(), nextSiblingId );

            if (canMoveRight) {
                logger.trace("canMoveNode( node {}, parent {}, nextSiblingId {}) returning true: " +
                        "{} is allowed this append to end.",
                        node, parent, nextSiblingId, owner);
                return true;
            } else {
                logger.trace("canMoveNode( node {}, parent {}, nextSiblingId {}) returning false: " +
                        "User {} is not allowed this append to end.",
                        node, parent, nextSiblingId, owner.getID());
                return false;
            }
        }

        if (canMoveLeft( node.getId(), nextSiblingId )) {
            logger.trace("canMoveNode( node {}, parent {}, nextSiblingId {}) returning true: " +
                    "{} is allowed this leftward move.",
                    node, parent, nextSiblingId, owner);
            return true;
        } else {
            logger.trace("canMoveNode( node {}, parent {}, nextSiblingId {}) returning false: " +
                    "User {} is not allowed this leftward move.",
                    node, parent, nextSiblingId, owner.getID());
            return false;
        }

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
                 next.getId().equals( targetNextSibId ) ) {
                logger.trace("canMoveRight( nodeId {}, targetNextSibId {}) returning true");
                return true;
            }
            else if ( ! MovementRules.canHopRight( node, next ) ) {
                logger.trace("canMoveRight( nodeId {}, targetNextSibId {}) returning false bc movement rule.",
                        nodeId, targetNextSibId);
                return false;
            }
        }

        if ( targetNextSibId == null ) {
            logger.trace("canMoveRight(nodeId {}, targetNextSibId {}) returning true because " +
                "made it to the end of the sibling list and that is the desired location.",
                    nodeId, targetNextSibId);

            return true;
        }

        logger.error("Something went wrong in canMoveRight(nodeId {}, targetNextSibId {}): " +
            "Never found the sibling.  This should never happen.",
                nodeId, targetNextSibId);
        return false;
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

            if ( ! MovementRules.canHopLeft( node, prev ) ) {
                logger.trace("canMoveLeft( nodeId {}, targetNextSibId {}) returning false per movement rule.",
                        nodeId, targetNextSibId);
                return false;
            }
            if ( targetNextSibId != null &&
                 prev.getId().equals( targetNextSibId ) ) {

                logger.trace("canMoveLeft( nodeId {}, targetNextSibId {}) returning true.");
                return true;
            }
        }

        logger.debug("canMoveLeft( nodeId {}, targetNextSibId {}) returning false because next sib not found.",
                nodeId, targetNextSibId);
        return false; // oops never found the sib
    }

    public boolean canDeleteNode(String nodeId) throws PortalException {

        boolean canDeleteNode = canDeleteNode(this.getNode(nodeId));

        logger.trace("canDeleteNode( nodeId {} ) returning {}.",
                nodeId, canDeleteNode);

        return canDeleteNode;
    }

    /**
       Returns true if the node exists in the underlying
       DOM model and it does not contain a 'deleteAllowed' attribute with a
       value of 'false'.
     */
    protected boolean canDeleteNode( IUserLayoutNodeDescription node )
        throws PortalException
    {
        if ( node == null ) {
            logger.debug("canDeleteNode() returning false on a null node.");
            return false;
        }

        boolean canDeleteNode = node.isDeleteAllowed();

        logger.trace("canDeleteNode( node {} ) returning {}.",
                node, canDeleteNode);

        return canDeleteNode;
    }

    public boolean canUpdateNode( String nodeId )
        throws PortalException
    {
        boolean canUpdateNode = canUpdateNode( this.getNode( nodeId ) );

        logger.trace("canUpdateNode( nodeId {} ) returning {}.",
                nodeId, canUpdateNode);

        return canUpdateNode;
    }

    /**
     * Returns true if we are dealing with a fragment layout or if editing of
     * attributes is allowed, or the node is a channel since ad-hoc parameters
     * can always be added.
     */
    public boolean canUpdateNode( IUserLayoutNodeDescription node )
    {
        if ( node == null ) {
            logger.debug("User {} cannot update a null node (in profile {}).",
                    owner.getID(), profile.getProfileId());
            return false;
        }

        if (isFragmentOwner) {
            logger.trace("User {} can update node {} under profile {} because isFragmentOwner.",
                    owner.getID(), node, profile.getProfileId());
            return true;
        }

        if (node.isEditAllowed()) {
            logger.trace("{} can update node {} under profile {} because that node reports isEditAllowed true.",
                    owner, node, profile);
            return true;
        }

        if (node instanceof IUserLayoutChannelDescription) {
            logger.trace("User {} can update node {} under profile {} because tha node is a channel.",
                    owner.getID(), node, profile.getProfileId());
            return true;
        }

        logger.trace("User {} cannot update node {} under profile {}.",
                owner.getID(), node, profile.getProfileId());

        return false;
    }

    /**
     * Unsupported operation in DLM. This feature is handled by pluggable 
     * processors in the DLM processing pipe. See properties/dlmContext.xml.
     */
    public void markAddTargets(IUserLayoutNodeDescription node) {
        throw new UnsupportedOperationException("Use an appropriate " +
                "processor for adding targets.");
    }

    /**
     * Unsupported operation in DLM. This feature is handled by pluggable 
     * processors in the DLM processing pipe. See properties/dlmContext.xml.
     */
    public void markMoveTargets(String nodeId) throws PortalException {
            throw new UnsupportedOperationException("Use an appropriate " +
            "processor for adding targets.");
    }


    public String getParentId(String nodeId) throws PortalException {
        Document uld=this.getUserLayoutDOM();
        Element nelement = uld.getElementById(nodeId);
        if(nelement!=null) {
            Node parent=nelement.getParentNode();
            if(parent!=null) {
                if(parent.getNodeType()!=Node.ELEMENT_NODE) {
                    logger.error("Failed to getParentId( nodeId {} ) because nodeId identifies a non-element.",
                            nodeId);
                    throw new PortalException("Node with id=\""+nodeId+"\" is attached to something other than an element node.");
                }
                Element e=(Element) parent;
                String parentId = e.getAttribute("ID");
                logger.trace("getParentId( nodeId {} ) returning {}.",
                        nodeId, parentId);
                return parentId;
            }

            logger.warn("getParentId( nodeId {}) returning null because that node has no parent.",
                    nodeId);
            return null;
        }

        logger.error("Failing to getParentId( nodeId {}) because no node with that node ID exists.",
                nodeId);
        throw new PortalException("Node with id=\""+nodeId+
                "\" doesn't exist. Occurred in layout for " 
                + owner.getAttribute(IPerson.USERNAME) + ".");
    }

    public String getNextSiblingId(String nodeId) throws PortalException {
        Document uld=this.getUserLayoutDOM();
        Element nelement = uld.getElementById(nodeId);
        if(nelement!=null) {
            Node nsibling=nelement.getNextSibling();
            // scroll to the next element node
            while(nsibling!=null && nsibling.getNodeType()!=Node.ELEMENT_NODE){
                nsibling=nsibling.getNextSibling();
            }
            if(nsibling!=null) {
                Element e=(Element) nsibling;
                String nextSiblingId = e.getAttribute("ID");
                logger.trace("getNextSiblingId( nodeId {} ) returning {}.",
                        nodeId, nextSiblingId);
                return nextSiblingId;
            }
            logger.trace("getNextSiblingId( nodeId {} ) returning null because no next sibling.",
                    nodeId);
            return null;
        }

        logger.error("Failed to getNextSiblingId( nodeId {} ) because that node does not exist.",
                nodeId);
        throw new PortalException("Node with id=\""+nodeId+
                "\" doesn't exist. Occurred " +
                "in layout for " 
                + owner.getAttribute(IPerson.USERNAME) + ".");
    }

    public String getPreviousSiblingId(String nodeId) throws PortalException {
        Document uld=this.getUserLayoutDOM();
        Element nelement = uld.getElementById(nodeId);
        if(nelement!=null) {
            Node nsibling=nelement.getPreviousSibling();
            // scroll to the next element node
            while(nsibling!=null && nsibling.getNodeType()!=Node.ELEMENT_NODE){
                nsibling=nsibling.getNextSibling();
            }
            if(nsibling!=null) {
                Element e=(Element) nsibling;
                String previousSiblingId = e.getAttribute("ID");
                logger.trace("getPreviousSibling( nodeId {} ) returning {}.",
                        nodeId, previousSiblingId);
                return previousSiblingId;
            }
            logger.trace("getPreviousSibling( nodeId {} ) returning null because no previous sibling.",
                    nodeId);
            return null;
        }

        logger.error("Failed to getPreviousSibling{ nodeId {} ) because that nodeId doesn't exist.",
                nodeId);
        throw new PortalException("Node with id=\""+nodeId+
                "\" doesn't exist. Occurred in layout for " 
                + owner.getAttribute(IPerson.USERNAME) + ".");
    }

    public Enumeration<String> getChildIds(String nodeId) throws PortalException {
        return getChildIds( nodeId, false );
    }

    private Enumeration<String> getVisibleChildIds(String nodeId)
        throws PortalException
    {
        return getChildIds( nodeId, true );
    }

    private Enumeration<String> getChildIds( String nodeId,
                              boolean visibleOnly)
        throws PortalException
    {
        Vector<String> v=new Vector<String>();
        IUserLayoutNodeDescription node=getNode(nodeId);
        if(node instanceof IUserLayoutFolderDescription) {
            Document uld=this.getUserLayoutDOM();
            Element felement = uld.getElementById(nodeId);
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

        logger.trace("getChildIds( nodeId {}, visibleOnly {}) returning {}.",
                nodeId, visibleOnly, v.elements());

        return v.elements();
    }

    public String getCacheKey() {
        return this.cacheKey;
    }

    /**
     * This is outright cheating ! We're supposed to analyze the user layout tree
     * and return a key that corresponds uniqly to the composition and the stucture of the tree.
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
    @Override
    public String getSubscribeId(String fname) {
    	final Document userLayout = this.getUserLayoutDOM();
        return new PortletSubscribeIdResolver(fname).traverseDocument(userLayout);
    }
    
    public String getSubscribeId(String parentFolderId, String fname) {
    	final Map<String, String> variables = new HashMap<String, String>();
    	variables.put("parentFolderId", parentFolderId);
    	variables.put("fname", fname);
    	
    	final Document userLayout = this.getUserLayoutDOM();
    	final Element fnameNode = this.xpathOperations.evaluate("//folder[@ID=$parentFolderId]/descendant::channel[@fname=$fname]", variables, userLayout, XPathConstants.NODE);

        if (fnameNode != null) {

            String subscribeId = fnameNode.getAttribute("ID");

            logger.trace("getSubscribeId( parentFolderId {}, fname {}) returning {} for user {} in profile {}.",
                    parentFolderId, fname, subscribeId, owner.getID(), profile.getProfileId());

			return subscribeId;

		} else {
            logger.trace("Could not find a subscribeId for fname {} within parent folder {}.",
                    fname, parentFolderId);
        }
    	
    	return null;
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.IUserLayoutManager#getUserLayout()
     */
    public IUserLayout getUserLayout() throws PortalException
    {
        // Copied from SimpleLayoutManager since our layouts are regular
        // simple layouts, ie Documents.
        return new SimpleLayout(this.getDistributedUserLayout(), String.valueOf(profile.getLayoutId()), this.cacheKey);
    }

    /* Returns the ID attribute of the root folder of the layout. This folder 
     * is defined to be the single child of the top most "layout" Element.
     * 
     * @see org.jasig.portal.layout.IUserLayoutManager#getRootFolderId()
     * @see org.jasig.portal.layout.dlm.RootLocator
     */
    public String getRootFolderId()
    {
        if (rootNodeId == null) {
            Document layout = getUserLayoutDOM();
            
            Element rootNode = this.xpathOperations.evaluate("//layout/folder", layout, XPathConstants.NODE);
            if (rootNode == null || !rootNode.getAttribute(Constants.ATT_TYPE).equals(Constants.ROOT_FOLDER_ID)) {
                logger.error("Unable to locate root node in layout of {}. Resetting corrupted layout.",
                        owner.getAttribute(IPerson.USERNAME));

                resetLayout((String) null);
                
                rootNode = this.xpathOperations.evaluate("//layout/folder", layout, XPathConstants.NODE);
                if (rootNode == null || !rootNode.getAttribute(Constants.ATT_TYPE).equals(Constants.ROOT_FOLDER_ID)) {
                    throw new PortalException("Corrupted layout detected for " + owner.getAttribute(IPerson.USERNAME)
                            + " and resetting layout failed.");
                }
            }
            rootNodeId = rootNode.getAttribute("ID");
        }

        logger.trace("getRootFolderId() returning {} for user {} in profile {}.",
                rootNodeId, owner.getID(), profile.getProfileId());

        return rootNodeId;
    }

    /*
     * (non-Javadoc)
     * 
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
     * IUserLayoutNodeDescription.FOLDER and LayoutNodeType.PORTLET.
     * 
     * @see org.jasig.portal.layout.IUserLayoutManager#createNodeDescription(int)
     */
    @Override
    public IUserLayoutNodeDescription createNodeDescription(LayoutNodeType nodeType) throws PortalException
    {
        if (nodeType == LayoutNodeType.FOLDER)
        {
            return new UserLayoutFolderDescription();
        }
        return new ChannelDescription();
    }

    /**
     * Resets the layout of the user with the specified user id if the current
     * user is an administrator or a member of any administrative sub-group.
     * Has no effect if these requirements are not met.
     * 
     * @return true if layout was reset, false otherwise.
     * 
     * @param loginId
     */
    public boolean resetLayout(String loginId)
    {
        boolean resetSuccess = false;
        boolean resetCurrentUserLayout = (null == loginId);
        
        if (resetCurrentUserLayout || 
                AdminEvaluator.isAdmin(owner))
        {
            logger.debug("resetLayout() for {} 's layout requested by user {}.",
                    loginId, owner.getID());

            int portalID = IPerson.UNDEFINED_ID;
            IPerson person = null;
            
            if (resetCurrentUserLayout ||
                    loginId.equals(owner.getAttribute(IPerson.USERNAME)))
            {
                person = owner;
                portalID = owner.getID();
            }
            else
            {
                // need to get the portal id
                person = PersonFactory.createPerson();
                person.setAttribute(IPerson.USERNAME, loginId);

                try
                {
                    IUserIdentityStore userStore = UserIdentityStoreLocator.getUserIdentityStore();
                    portalID = userStore.getPortalUID(person);
                    person.setID(portalID);
                } 
                catch (Exception e)
                {
                    // ignore since the store will log the problem
                }
            }
            if (portalID != IPerson.UNDEFINED_ID)
            {
                resetSuccess = resetLayout(person);
            }
        } 
        else
        {
            logger.error("Non-administrator user {} requested layout reset for user {}. No-op.",
                    owner, loginId);
        }
        return resetSuccess;
    }

    /**
     * Resets the layout of the specified user.
     */
    private boolean resetLayout(IPerson person)
    {
        final String userName = person.getUserName();
        if (PersonFactory.GUEST_USERNAME.equals(userName)) {
            throw new IllegalArgumentException("CANNOT RESET LAYOUT FOR A GUEST USER: " + person);
        }
        logger.warn("Resetting user layout for: {}.", userName, new Throwable());
        
        boolean layoutWasReset = false;
        
        /*
         * is the person being reset a fragment owner? Can't use the
         * isFramentOwner variable in this class since we could be resetting 
         * another user's layout.
         */
        if (this.distributedLayoutStore.isFragmentOwner(person))
        {
            // set template user override so reload of layout comes from
            // fragment template user
            person.setAttribute( 
                    org.jasig.portal.Constants.TEMPLATE_USER_NAME_ATT, 
                    FragmentDefinition.getDefaultLayoutOwnerId() );
        }
        IUserIdentityStore userStore = UserIdentityStoreLocator.getUserIdentityStore();

        try
        {
            userStore.removePortalUID( person.getID() );
            userStore.getPortalUID( person, true );

            // see if the current user was the one to reset their layout and if
            // so we need to refresh our local copy of their layout
            if (person == owner)
            {
                this.layoutCachingService.removeCachedLayout(person, profile);
                updateCacheKey();
                getUserLayoutDOM();
            }
            //if (isFragmentOwner)
            //{
            //    
            //    store.updateOwnerLayout(person);
            //}
            layoutWasReset = true;
        }
        catch( Exception e )
        {
            logger.error("Unable to reset layout for {}.",
                    person.getAttribute(IPerson.USERNAME), e);
        }
        return layoutWasReset;
    }

    public IUserLayoutNodeDescription createNodeDescription(Element node) throws PortalException
    {
        String type = node.getNodeName();
        if(type.equals(Constants.ELM_CHANNEL)) 
        {
            return new ChannelDescription(node);
        }
        else if (type.equals(Constants.ELM_FOLDER))
        {
            return new UserLayoutFolderDescription(node);
        }
        else
        {
            throw new PortalException("Given XML Element is neither a folder nor a channel!");
        }
    }

    /**
     * Return a map of channel identifiers to functional names, for those 
     * channels that have functional names.
     */
    public Map getChannelFunctionalNameMap() throws PortalException
    {
        Document layout = getUserLayoutDOM();
        
        /*
         * NodeLists are known not to be thread safe but the layout is 
         * hierarchical and this is the simples way to obtain all of the nested
         * channels. Furthermore, since this method is only called by jndi
         * initialization once in a user's session and hence should be just
         * fine. Furthermore, this NodeList is not that of the children of
         * a node in the layout so it is unlikely that it will change.
         */
        NodeList channelNodes = layout.getElementsByTagName("channel");
        Map<String, String> map = new HashMap<String, String>();
        
        // Parse through the channels and populate the set
        for (int i = 0; i < channelNodes.getLength(); i++) {
            // Attempt to get the fname and instance ID from the channel
            Element chan = (Element) channelNodes.item(i);
            String id = chan.getAttribute("ID");
            String fname = chan.getAttribute("fname");
            if (!id.equals("") && !fname.equals(""))
            {
                map.put(id, fname);
            }
        }

        logger.trace("getChannelFunctionalNameMap() returning {} for user {} in profile {}.",
                map, owner.getID(), profile.getProfileId());

        return map;
    }
    
    /**
     * Returns the IPerson that is the owner of this layout manager instance.
     * @return IPerson object
     */
    IPerson getOwner()
    {
        return owner;
    }
 
    /**
     * Returns a resolver for local names. This layout manager supports this
     * feature itself and hence returns itself as the interface.
     * 
     * @return
     */
    public IFolderLocalNameResolver getFolderNameResolver()
    {
       return this; 
    }
    
    /**
     * Returns the localized name of a folder node or null if none is available.
     * This method also implements enforcement of user label overrides to 
     * fragment folders purging those overrides if they are no longer allowed
     * or needed. 
     */
    public String getFolderLabel(String nodeId)
    {
        IUserLayoutNodeDescription ndesc = getNode(nodeId);
        if (!(ndesc instanceof IUserLayoutFolderDescription)) {
            logger.warn("nodeId {} identifies a folder ({}) so can't getFolderLabel().",
                    nodeId, ndesc);
            return null;
        }
        
        IUserLayoutFolderDescription desc 
            = (IUserLayoutFolderDescription) ndesc; 
        boolean editAllowed = desc.isEditAllowed();
        String label = desc.getName();
        // assume user owned to begin with which means plfId equals nodeId
        String plfId = nodeId; 

        if (nodeId.startsWith(
                org.jasig.portal.layout.dlm.Constants.FRAGMENT_ID_USER_PREFIX))
        {
            Document plf = RDBMDistributedLayoutStore.getPLF( owner );
            Element plfNode = plf.getElementById( nodeId );
            if (plfNode != null)
                plfId = plfNode.getAttribute(Constants.ATT_PLF_ID);
            else
                plfId = null; // no user mods exist for this node
        }

        logger.trace("getFolderLabel( nodeId {} ) returning {} for user id {} in profile {}.",
                nodeId, label, owner.getID(), profile.getProfileId());
    
        return label;
    }
}
