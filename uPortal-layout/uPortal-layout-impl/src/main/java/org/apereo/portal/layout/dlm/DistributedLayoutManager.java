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
package org.apereo.portal.layout.dlm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.IUserIdentityStore;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.PortalException;
import org.apereo.portal.events.IPortalLayoutEventFactory;
import org.apereo.portal.layout.IUserLayout;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.IUserLayoutStore;
import org.apereo.portal.layout.PortletSubscribeIdResolver;
import org.apereo.portal.layout.SimpleLayout;
import org.apereo.portal.layout.node.IUserLayoutChannelDescription;
import org.apereo.portal.layout.node.IUserLayoutFolderDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription.LayoutNodeType;
import org.apereo.portal.layout.node.UserLayoutFolderDescription;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.security.AdminEvaluator;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.PersonFactory;
import org.apereo.portal.spring.locator.PortletDefinitionRegistryLocator;
import org.apereo.portal.xml.XmlUtilities;
import org.apereo.portal.xml.xpath.XPathOperations;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A layout manager that provides layout control through layout fragments that are derived from
 * regular portal user accounts.
 *
 * @since 2.5
 */
public class DistributedLayoutManager implements IUserLayoutManager, InitializingBean {

    private static final Log LOG = LogFactory.getLog(DistributedLayoutManager.class);

    private XmlUtilities xmlUtilities;
    private ILayoutCachingService layoutCachingService;
    private IUserLayoutStore distributedLayoutStore;
    private XPathOperations xpathOperations;
    private IPortalLayoutEventFactory portalEventFactory;
    private IAuthorizationService authorizationService;
    private IUserIdentityStore userIdentityStore;

    protected final IPerson owner;
    protected final IUserProfile profile;

    protected static final Random rnd = new Random();
    protected String cacheKey = null; // Must be "updated" prior to use
    protected String rootNodeId = null;

    private boolean channelsAdded = false;
    private boolean isFragmentOwner = false;

    public DistributedLayoutManager(IPerson owner, IUserProfile profile) throws PortalException {

        if (owner == null) {
            throw new PortalException(
                    "Unable to instantiate DistributedLayoutManager. "
                            + "A non-null owner must to be specified.");
        }
        if (profile == null) {
            throw new PortalException(
                    "Unable to instantiate DistributedLayoutManager for "
                            + owner.getUserName()
                            + ". A "
                            + "non-null profile must to be specified.");
        }

        // cache the relatively lightweight userprofile for use in layout PLF loading
        owner.setAttribute(IUserProfile.USER_PROFILE, profile);

        this.owner = owner;
        this.profile = profile;

        // Must always initialize the cacheKey to a generated value
        this.updateCacheKey();
    }

    @Autowired
    public void setAuthorizationService(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Autowired
    public void setUserIdentityStore(IUserIdentityStore userIdentityStore) {
        this.userIdentityStore = userIdentityStore;
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
            this.layoutCachingService.removeCachedLayout(owner, profile);
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
        Node attr = layout.getAttributeNodeNS(Constants.NS_URI, Constants.LCL_FRAGMENT_NAME);
        this.isFragmentOwner = attr != null;
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public Document getUserLayoutDOM() {
        final DistributedUserLayout userLayout = getDistributedUserLayout();
        return userLayout.getLayout();
    }

    protected DistributedUserLayout getDistributedUserLayout() {
        DistributedUserLayout userLayout =
                this.layoutCachingService.getCachedLayout(owner, profile);
        if (null == userLayout) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Load from store for " + owner.getUserName());
            }
            userLayout = this.distributedLayoutStore.getUserLayout(this.owner, this.profile);

            final Document userLayoutDocument = userLayout.getLayout();

            // DistributedLayoutManager shall gracefully remove channels
            // that the user isn't authorized to render from folders of type
            // 'header' and 'footer'.
            IAuthorizationPrincipal principal =
                    authorizationService.newPrincipal(owner.getUserName(), IPerson.class);
            NodeList nodes = userLayoutDocument.getElementsByTagName("folder");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element fd = (Element) nodes.item(i);
                String type = fd.getAttribute("type");
                if (type != null
                        && (type.equals("header")
                                || type.equals("footer")
                                || type.equals("sidebar"))) {
                    // Here's where we do the work...
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                                "RDBMUserLayoutStore examining the '"
                                        + type
                                        + "' folder of user '"
                                        + owner.getUserName()
                                        + "' for non-authorized channels.");
                    }
                    NodeList channels = fd.getElementsByTagName("channel");
                    for (int j = 0; j < channels.getLength(); j++) {
                        Element ch = (Element) channels.item(j);
                        try {
                            String chanId = ch.getAttribute("chanID");
                            if (!principal.canRender(chanId)) {
                                fd.removeChild(ch);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(
                                            "RDBMUserLayoutStore removing channel '"
                                                    + ch.getAttribute("fname")
                                                    + "' from the header or footer of user '"
                                                    + owner.getUserName()
                                                    + "' because he/she isn't authorized to render it.");
                                }
                            }
                        } catch (Throwable t) {
                            // Log this...
                            LOG.warn(
                                    "RDBMUserLayoutStore was unable to analyze channel element with Id="
                                            + ch.getAttribute("chanID"),
                                    t);
                        }
                    }
                }
            }

            setUserLayoutDOM(userLayout);
        }
        return userLayout;
    }

    @Override
    public XMLEventReader getUserLayoutReader() {
        Document ul = this.getUserLayoutDOM();
        if (ul == null) {
            throw new PortalException(
                    "User layout has not been initialized for " + owner.getUserName());
        }

        final XMLInputFactory xmlInputFactory = this.xmlUtilities.getXmlInputFactory();

        final DOMSource layoutSource = new DOMSource(ul);
        try {
            return xmlInputFactory.createXMLEventReader(layoutSource);
        } catch (XMLStreamException e) {
            throw new RuntimeException(
                    "Failed to create Layout XMLStreamReader for user: " + owner.getUserName(), e);
        }
    }

    @Override
    public synchronized void loadUserLayout() throws PortalException {
        this.loadUserLayout(false);
    }

    @Override
    public synchronized void loadUserLayout(boolean reload) throws PortalException {
        Document uli = null;
        try {
            // Clear the loaded document first if this is a forced reload
            if (reload) {
                this.layoutCachingService.removeCachedLayout(owner, profile);
            }

            uli = getUserLayoutDOM();
        } catch (Exception e) {
            throw new PortalException(
                    "Exception encountered while "
                            + "reading a layout for userId="
                            + this.owner.getID()
                            + ", profileId="
                            + this.profile.getProfileId(),
                    e);
        }
        if (uli == null) {
            throw new PortalException(
                    "Null user layout returned "
                            + "for ownerId=\""
                            + owner.getID()
                            + "\", profileId=\""
                            + profile.getProfileId()
                            + "\", layoutId=\""
                            + profile.getLayoutId()
                            + "\"");
        }
    }

    @Override
    public synchronized void saveUserLayout() throws PortalException {
        Document uld = this.getUserLayoutDOM();

        if (uld == null) {
            throw new PortalException(
                    "UserLayout has not been initialized for " + owner.getUserName() + ".");
        }
        try {
            this.distributedLayoutStore.setUserLayout(this.owner, this.profile, uld, channelsAdded);
        } catch (Exception e) {
            throw new PortalException(
                    "Exception encountered while "
                            + "saving layout for userId="
                            + this.owner.getID()
                            + ", profileId="
                            + this.profile.getProfileId(),
                    e);
        }

        this.channelsAdded = false;
    }

    @Override
    public Set<String> getAllSubscribedChannels() {
        final Document uld = this.getUserLayoutDOM();

        if (uld == null) {
            throw new PortalException(
                    "UserLayout has not been initialized for " + owner.getUserName());
        }

        final NodeList channelElements = uld.getElementsByTagName(CHANNEL);

        final Set<String> allSubscribedChannels =
                new LinkedHashSet<String>(channelElements.getLength());
        for (int nodeIndex = 0; nodeIndex < channelElements.getLength(); nodeIndex++) {
            final Element channelElement = (Element) channelElements.item(nodeIndex);
            final String subscribeId = channelElement.getAttribute("ID");
            allSubscribedChannels.add(subscribeId);
        }

        return allSubscribedChannels;
    }

    @Override
    public IUserLayoutNodeDescription getNode(String nodeId) throws PortalException {
        if (nodeId == null) return null;

        Document uld = this.getUserLayoutDOM();

        if (uld == null)
            throw new PortalException(
                    "UserLayout has not been initialized for " + owner.getUserName() + ".");

        // find an element with a given id
        Element element = uld.getElementById(nodeId);
        if (element == null) {
            throw new PortalException(
                    "Element with ID=\""
                            + nodeId
                            + "\" doesn't exist for "
                            + owner.getUserName()
                            + ".");
        }
        // instantiate the node description
        final IUserLayoutNodeDescription desc = createNodeDescription(element);
        return desc;
    }

    @Override
    public IUserLayoutNodeDescription addNode(
            IUserLayoutNodeDescription node, String parentId, String nextSiblingId)
            throws PortalException {
        boolean isChannel = false;
        IUserLayoutNodeDescription parent = this.getNode(parentId);
        if (canAddNode(node, parent, nextSiblingId)) {
            // assign new Id
            try {
                if (node instanceof IUserLayoutChannelDescription) {
                    isChannel = true;
                    node.setId(this.distributedLayoutStore.generateNewChannelSubscribeId(owner));
                } else {
                    node.setId(this.distributedLayoutStore.generateNewFolderId(owner));
                }
            } catch (Exception e) {
                throw new PortalException(
                        "Exception encountered while "
                                + "generating new user layout node Id for  for "
                                + owner.getUserName(),
                        e);
            }

            Document uld = getUserLayoutDOM();
            Element childElement = node.getXML(uld);
            Element parentElement = uld.getElementById(parentId);
            if (nextSiblingId == null) {
                parentElement.appendChild(childElement);
            } else {
                Node nextSibling = uld.getElementById(nextSiblingId);
                parentElement.insertBefore(childElement, nextSibling);
            }
            // register element id
            childElement.setIdAttribute(Constants.ATT_ID, true);
            childElement.setAttribute(Constants.ATT_ID, node.getId());
            this.updateCacheKey();

            // push into the user's real layout that gets persisted.
            HandlerUtils.createPlfNodeAndPath(childElement, isChannel, owner);

            // fire event
            final int layoutId = this.getLayoutId();
            if (isChannel) {
                this.channelsAdded = true;
                final String fname = ((IUserLayoutChannelDescription) node).getFunctionalName();
                this.portalEventFactory.publishPortletAddedToLayoutPortalEvent(
                        this, this.owner, layoutId, parent.getId(), fname);
            } else {
                this.portalEventFactory.publishFolderAddedToLayoutPortalEvent(
                        this, this.owner, layoutId, node.getId());
            }

            return node;
        }
        return null;
    }

    @Override
    public boolean moveNode(String nodeId, String parentId, String nextSiblingId)
            throws PortalException {
        IUserLayoutNodeDescription parent = this.getNode(parentId);
        IUserLayoutNodeDescription node = this.getNode(nodeId);
        String oldParentNodeId = getParentId(nodeId);
        if (canMoveNode(node, parent, nextSiblingId)) {
            // must be a folder
            Document uld = this.getUserLayoutDOM();
            Element childElement = uld.getElementById(nodeId);
            Element parentElement = uld.getElementById(parentId);
            if (nextSiblingId == null) {
                parentElement.appendChild(childElement);
            } else {
                Node nextSibling = uld.getElementById(nextSiblingId);
                parentElement.insertBefore(childElement, nextSibling);
            }
            this.updateCacheKey();

            // propagate the change into the PLF
            Element oldParent = uld.getElementById(oldParentNodeId);
            TabColumnPrefsHandler.moveElement(childElement, oldParent, owner);
            // fire event
            final int layoutId = this.getLayoutId();
            if (node instanceof IUserLayoutChannelDescription) {
                this.channelsAdded = true;
                final String fname = ((IUserLayoutChannelDescription) node).getFunctionalName();
                this.portalEventFactory.publishPortletMovedInLayoutPortalEvent(
                        this, this.owner, layoutId, oldParentNodeId, parent.getId(), fname);
            } else {
                this.portalEventFactory.publishFolderMovedInLayoutPortalEvent(
                        this, this.owner, layoutId, oldParentNodeId, parent.getId());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteNode(String nodeId) throws PortalException {
        if (canDeleteNode(nodeId)) {
            IUserLayoutNodeDescription nodeDescription = this.getNode(nodeId);
            String parentNodeId = this.getParentId(nodeId);

            Document uld = this.getUserLayoutDOM();
            Element ilfNode = uld.getElementById(nodeId);
            Node parent = ilfNode.getParentNode();
            if (parent != null) {
                parent.removeChild(ilfNode);
            } else {
                throw new PortalException(
                        "Node \""
                                + nodeId
                                + "\" has a NULL parent for layout of "
                                + owner.getUserName()
                                + ".");
            }
            this.updateCacheKey();

            // now push into the PLF
            TabColumnPrefsHandler.deleteNode(ilfNode, owner);
            // inform the listeners
            final int layoutId = this.getLayoutId();
            if (nodeDescription instanceof IUserLayoutChannelDescription) {
                final IUserLayoutChannelDescription userLayoutChannelDescription =
                        (IUserLayoutChannelDescription) nodeDescription;
                this.portalEventFactory.publishPortletDeletedFromLayoutPortalEvent(
                        this,
                        this.owner,
                        layoutId,
                        parentNodeId,
                        userLayoutChannelDescription.getFunctionalName());
            } else {
                this.portalEventFactory.publishFolderDeletedFromLayoutPortalEvent(
                        this,
                        this.owner,
                        layoutId,
                        parentNodeId,
                        nodeDescription.getId(),
                        nodeDescription.getName());
            }

            return true;
        }
        return false;
    }

    /**
     * Handles pushing changes made to the passed-in node into the user's layout. If the node is an
     * ILF node then the change is recorded via directives in the PLF if such changes are allowed by
     * the owning fragment. If the node is a user owned node then the changes are applied directly
     * to the corresponding node in the PLF.
     */
    @Override
    public synchronized boolean updateNode(IUserLayoutNodeDescription node) throws PortalException {
        if (canUpdateNode(node)) {
            String nodeId = node.getId();
            IUserLayoutNodeDescription oldNode = getNode(nodeId);

            if (oldNode instanceof IUserLayoutChannelDescription) {
                IUserLayoutChannelDescription oldChanDesc = (IUserLayoutChannelDescription) oldNode;
                if (!(node instanceof IUserLayoutChannelDescription)) {
                    throw new PortalException(
                            "Change channel to folder is "
                                    + "not allowed by updateNode() method! Occurred "
                                    + "in layout for "
                                    + owner.getUserName()
                                    + ".");
                }
                IUserLayoutChannelDescription newChanDesc = (IUserLayoutChannelDescription) node;
                updateChannelNode(nodeId, newChanDesc, oldChanDesc);
            } else {
                // must be a folder
                IUserLayoutFolderDescription oldFolderDesc = (IUserLayoutFolderDescription) oldNode;
                if (oldFolderDesc.getId().equals(getRootFolderId()))
                    throw new PortalException("Update of root node is not currently allowed!");

                if (node instanceof IUserLayoutFolderDescription) {
                    IUserLayoutFolderDescription newFolderDesc =
                            (IUserLayoutFolderDescription) node;
                    updateFolderNode(nodeId, newFolderDesc, oldFolderDesc);
                }
            }
            this.updateCacheKey();
            return true;
        }
        return false;
    }

    /**
     * Compares the new folder description object with the old folder description object to
     * determine what items were changed and if those changes are allowed. Once all changes are
     * verified as being allowed changes then they are pushed into both the ILF and the PLF as
     * appropriate. No changes are made until we determine that all changes are allowed.
     *
     * @param nodeId
     * @param newFolderDesc
     * @param oldFolderDesc
     * @throws PortalException
     */
    private void updateFolderNode(
            String nodeId,
            IUserLayoutFolderDescription newFolderDesc,
            IUserLayoutFolderDescription oldFolderDesc)
            throws PortalException {
        Element ilfNode = (Element) getUserLayoutDOM().getElementById(nodeId);
        List<ILayoutProcessingAction> pendingActions = new ArrayList<ILayoutProcessingAction>();

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
                && (newFolderDesc.isDeleteAllowed() != oldFolderDesc.isDeleteAllowed()
                        || newFolderDesc.isEditAllowed() != oldFolderDesc.isEditAllowed()
                        || newFolderDesc.isAddChildAllowed() != oldFolderDesc.isAddChildAllowed()
                        || newFolderDesc.isMoveAllowed() != oldFolderDesc.isMoveAllowed())) {
            pendingActions.add(
                    new LPAEditRestriction(
                            owner,
                            ilfNode,
                            newFolderDesc.isMoveAllowed(),
                            newFolderDesc.isDeleteAllowed(),
                            newFolderDesc.isEditAllowed(),
                            newFolderDesc.isAddChildAllowed()));
        }

        // ATT: Name
        updateNodeAttribute(
                ilfNode,
                nodeId,
                Constants.ATT_NAME,
                newFolderDesc.getName(),
                oldFolderDesc.getName(),
                pendingActions);

        /*
         * if we make it to this point then all edits made are allowed so
         * process the actions to push the edits into the layout
         */
        for (Iterator itr = pendingActions.iterator(); itr.hasNext(); ) {
            ILayoutProcessingAction action = (ILayoutProcessingAction) itr.next();
            action.perform();
        }
    }

    /**
     * Handles checking for updates to a named attribute, verifying such change is allowed, and
     * generates an action object to make that change.
     *
     * @param ilfNode the node in the viewed layout
     * @param nodeId the id of the ilfNode
     * @param attName the attribute to be checked
     * @param newVal the attribute's new value
     * @param oldVal the attribute's old value
     * @param pendingActions the set of actions for adding an action
     * @throws PortalException if the change is not allowed
     */
    private void updateNodeAttribute(
            Element ilfNode,
            String nodeId,
            String attName,
            String newVal,
            String oldVal,
            List<ILayoutProcessingAction> pendingActions)
            throws PortalException {
        if (newVal == null && oldVal != null
                || newVal != null && oldVal == null
                || (newVal != null && oldVal != null && !newVal.equals(oldVal))) {
            boolean isIncorporated = nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX);
            if (isIncorporated) {
                /*
                 * Is a change to this attribute allowed?
                 */
                FragmentNodeInfo fragNodeInf =
                        this.distributedLayoutStore.getFragmentNodeInfo(nodeId);
                if (fragNodeInf == null) {
                    /*
                     * null should only happen if a node was deleted in the
                     * fragment and a user happened to already be logged in and
                     * edited an attribute on that node.
                     */
                    pendingActions.add(
                            new LPAChangeAttribute(nodeId, attName, newVal, owner, ilfNode));
                } else if (!fragNodeInf.canOverrideAttributes()) {
                    /*
                     * It isn't overrideable.
                     */
                    throw new PortalException(
                            "Layout element '"
                                    + fragNodeInf.getAttributeValue(attName)
                                    + "' does not allow overriding attribute '"
                                    + attName
                                    + "'.");
                } else if (!fragNodeInf.getAttributeValue(attName).equals(newVal)) {
                    /*
                     * If we get here we can override and the value is
                     * different than that in the fragment so make the change.
                     */
                    pendingActions.add(
                            new LPAChangeAttribute(nodeId, attName, newVal, owner, ilfNode));
                } else {
                    /*
                     * The new value matches that in the fragment.
                     */
                    pendingActions.add(
                            new LPAResetAttribute(
                                    nodeId,
                                    attName,
                                    fragNodeInf.getAttributeValue(attName),
                                    owner,
                                    ilfNode));
                }
            } else {
                /*
                 * Node owned by user so no checking needed. Just change it.
                 */
                pendingActions.add(new LPAChangeAttribute(nodeId, attName, newVal, owner, ilfNode));
            }
        }
    }
    /**
     * Compares the new channel description object with the old channel description object to
     * determine what items were changed and if those changes are allowed. Once all changes are
     * verified as being allowed changes then they are pushed into both the ILF and the PLF as
     * appropriate. No changes are made until we determine that all changes are allowed.
     *
     * @param nodeId
     * @param newChanDesc
     * @param oldChanDesc
     * @throws PortalException
     */
    private void updateChannelNode(
            String nodeId,
            IUserLayoutChannelDescription newChanDesc,
            IUserLayoutChannelDescription oldChanDesc)
            throws PortalException {
        Element ilfNode = (Element) getUserLayoutDOM().getElementById(nodeId);
        List<ILayoutProcessingAction> pendingActions = new ArrayList<ILayoutProcessingAction>();
        boolean isIncorporated = nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX);

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
                && (newChanDesc.isDeleteAllowed() != oldChanDesc.isDeleteAllowed()
                        || newChanDesc.isEditAllowed() != oldChanDesc.isEditAllowed()
                        || newChanDesc.isMoveAllowed() != oldChanDesc.isMoveAllowed())) {
            pendingActions.add(
                    new LPAEditRestriction(
                            owner,
                            ilfNode,
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
        Map pubParms = getPublishedChannelParametersMap(newChanDesc.getChannelPublishId());

        if (isIncorporated)
            fragChanInf = this.distributedLayoutStore.getFragmentChannelInfo(nodeId);
        Map oldParms = new HashMap(oldChanDesc.getParameterMap());
        for (Iterator itr = newChanDesc.getParameterMap().entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry e = (Entry) itr.next();
            String name = (String) e.getKey();
            String newVal = (String) e.getValue();
            String oldVal = (String) oldParms.remove(name);

            if (oldVal == null) {
                /*
                 * not in old description so this is a new ad-hoc parameter
                 */
                pendingActions.add(new LPAAddParameter(nodeId, name, newVal, owner, ilfNode));
            } else if (!oldVal.equals(newVal)) {
                if (isIncorporated) {
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

                    if (fragValue == null) {
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
                            pendingActions.add(
                                    new LPARemoveParameter(nodeId, name, owner, ilfNode));
                        else
                            /*
                             * value doesn't match that of published chanel so
                             * we need change any existing parameter spec or add
                             * a new one if it doesn't exist.
                             */
                            pendingActions.add(
                                    new LPAChangeParameter(nodeId, name, newVal, owner, ilfNode));
                    } else if (!fragValue.equals(newVal)) {
                        /*
                         * so fragment does specify and user value is different
                         * so change any existing parameter spec or add a new
                         * one if it doesn't exist.
                         */
                        pendingActions.add(
                                new LPAChangeParameter(nodeId, name, newVal, owner, ilfNode));
                    } else {
                        /*
                         * new val same as fragment value so don't persist.
                         * remove any parameter spec if it exists.
                         */
                        pendingActions.add(
                                new LPAResetParameter(nodeId, name, fragValue, owner, ilfNode));
                    }
                } else // not incorporated from a fragment
                {
                    /*
                     * see if the value specified matches that of the channel
                     * definition.
                     */
                    IPortletDefinitionParameter cp =
                            (IPortletDefinitionParameter) pubParms.get(name);

                    if (cp != null && cp.getValue().equals(newVal))
                        pendingActions.add(new LPARemoveParameter(nodeId, name, owner, ilfNode));
                    else
                        pendingActions.add(
                                new LPAChangeParameter(nodeId, name, newVal, owner, ilfNode));
                }
            }
        }
        /*
         * So any parameters remaining in the oldParms map at this point didn't
         * match those in the new channel description which means that they were
         * removed. So remove any parameter spec if it exists.
         */
        for (Iterator itr = oldParms.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry e = (Entry) itr.next();
            String name = (String) e.getKey();
            pendingActions.add(new LPARemoveParameter(nodeId, name, owner, ilfNode));
        }
        /*
         * if we make it to this point then all edits made are allowed so
         * process the actions to push the edits into the layout
         */
        for (Iterator itr = pendingActions.iterator(); itr.hasNext(); ) {
            ILayoutProcessingAction action = (ILayoutProcessingAction) itr.next();
            action.perform();
        }
    }
    /**
     * Return a map parameter names to channel parameter objects representing the parameters
     * specified at publish time for the channel with the passed-in publish id.
     *
     * @param channelPublishId
     * @return
     * @throws PortalException
     */
    private Map getPublishedChannelParametersMap(String channelPublishId) throws PortalException {
        try {
            IPortletDefinitionRegistry registry =
                    PortletDefinitionRegistryLocator.getPortletDefinitionRegistry();
            IPortletDefinition def = registry.getPortletDefinition(channelPublishId);
            return def.getParametersAsUnmodifiableMap();
        } catch (Exception e) {
            throw new PortalException("Unable to acquire channel definition.", e);
        }
    }

    @Override
    public boolean canAddNode(
            IUserLayoutNodeDescription node, String parentId, String nextSiblingId)
            throws PortalException {
        return this.canAddNode(node, this.getNode(parentId), nextSiblingId);
    }

    protected boolean canAddNode(
            IUserLayoutNodeDescription node,
            IUserLayoutNodeDescription parent,
            String nextSiblingId)
            throws PortalException {
        // make sure sibling exists and is a child of nodeId
        if (nextSiblingId != null && !nextSiblingId.equals("")) {
            IUserLayoutNodeDescription sibling = getNode(nextSiblingId);
            if (sibling == null) {
                throw new PortalException(
                        "Unable to find a sibling node "
                                + "with id=\""
                                + nextSiblingId
                                + "\".  Occurred "
                                + "in layout for "
                                + owner.getUserName()
                                + ".");
            }
            if (!parent.getId().equals(getParentId(nextSiblingId))) {
                throw new PortalException(
                        "Given sibling (\""
                                + nextSiblingId
                                + "\") is not a child of a given parentId (\""
                                + parent.getId()
                                + "\"). Occurred "
                                + "in layout for "
                                + owner.getUserName()
                                + ".");
            }
        }

        // todo if isFragmentOwner should probably verify both node and parent are part of the
        // same layout fragment as the fragment owner to insure a misbehaving front-end doesn't
        // do an improper operation.

        if (parent == null || !(node.isMoveAllowed() || isFragmentOwner)) return false;

        if (parent instanceof IUserLayoutFolderDescription
                && !(((IUserLayoutFolderDescription) parent).isAddChildAllowed())
                && !isFragmentOwner) return false;

        if (nextSiblingId == null || nextSiblingId.equals("")) // end of list targeted
        return true;

        // so lets see if we can place it at the end of the sibling list and
        // hop left until we get into the correct position.

        Enumeration sibIds = getVisibleChildIds(parent.getId());
        List sibs = Collections.list(sibIds);

        if (sibs.size() == 0) // last node in list so should be ok
        return true;

        // reverse scan so that as changes are made the order of the, as yet,
        // unprocessed nodes is not altered.
        for (int idx = sibs.size() - 1; idx >= 0; idx--) {
            IUserLayoutNodeDescription prev = getNode((String) sibs.get(idx));

            if (!isFragmentOwner && !MovementRules.canHopLeft(node, prev)) return false;
            if (prev.getId().equals(nextSiblingId)) return true;
        }
        return false; // oops never found the sib
    }

    @Override
    public boolean canMoveNode(String nodeId, String parentId, String nextSiblingId)
            throws PortalException {
        return this.canMoveNode(this.getNode(nodeId), this.getNode(parentId), nextSiblingId);
    }

    protected boolean canMoveNode(
            IUserLayoutNodeDescription node,
            IUserLayoutNodeDescription parent,
            String nextSiblingId)
            throws PortalException {
        // todo if isFragmentOwner should probably verify both node and parent are part of the
        // same layout fragment as the fragment owner to insure a misbehaving front-end doesn't
        // do an improper operation.

        // are we moving to a new parent?
        if (!getParentId(node.getId()).equals(parent.getId()))
            return (isFragmentOwner || node.isMoveAllowed())
                    && canAddNode(node, parent, nextSiblingId);

        // same parent. which direction are we moving?
        Document uld = this.getUserLayoutDOM();
        Element parentE = uld.getElementById(parent.getId());
        Element child = (Element) parentE.getFirstChild();
        int idx = 0;
        int nodeIdx = -1;
        int sibIdx = -1;

        while (child != null) {
            String id = child.getAttribute(Constants.ATT_ID);
            if (id.equals(node.getId())) nodeIdx = idx;
            if (id.equals(nextSiblingId)) sibIdx = idx;
            idx++;
            child = (Element) child.getNextSibling();
        }
        if (nodeIdx == -1
                || // couldn't find node
                (nextSiblingId != null && sibIdx == -1)) // couldn't find sibling
        return false;

        if (nodeIdx < sibIdx
                || // moving right
                sibIdx == -1) // appending to end
        return canMoveRight(node.getId(), nextSiblingId);
        return canMoveLeft(node.getId(), nextSiblingId);
    }

    private boolean canMoveRight(String nodeId, String targetNextSibId) throws PortalException {
        IUserLayoutNodeDescription node = getNode(nodeId);
        Enumeration sibIds = getVisibleChildIds(getParentId(nodeId));
        List sibs = Collections.list(sibIds);

        for (int idx = sibs.indexOf(nodeId) + 1; idx > 0 && idx < sibs.size(); idx++) {
            String nextSibId = (String) sibs.get(idx);
            IUserLayoutNodeDescription next = getNode(nextSibId);

            if (nextSibId != null && next.getId().equals(targetNextSibId)) return true;
            else if (!isFragmentOwner && !MovementRules.canHopRight(node, next)) return false;
        }

        if (targetNextSibId == null) // made it to end of sib list and
        return true; // that is the desired location
        return false; // oops never found the sib. Should never happen.
    }

    private boolean canMoveLeft(String nodeId, String targetNextSibId) throws PortalException {
        IUserLayoutNodeDescription node = getNode(nodeId);
        Enumeration sibIds = getVisibleChildIds(getParentId(nodeId));
        List sibs = Collections.list(sibIds);

        for (int idx = sibs.indexOf(nodeId) - 1; idx >= 0; idx--) {
            String prevSibId = (String) sibs.get(idx);
            IUserLayoutNodeDescription prev = getNode(prevSibId);

            if (!isFragmentOwner && !MovementRules.canHopLeft(node, prev)) return false;
            if (targetNextSibId != null && prev.getId().equals(targetNextSibId)) return true;
        }
        return false; // oops never found the sib
    }

    @Override
    public boolean canDeleteNode(String nodeId) throws PortalException {
        return canDeleteNode(this.getNode(nodeId));
    }

    /**
     * Returns true if the node exists in the underlying DOM model and it does not contain a
     * 'deleteAllowed' attribute with a value of 'false'.
     */
    protected boolean canDeleteNode(IUserLayoutNodeDescription node) throws PortalException {
        if (node == null) return false;

        // todo if isFragmentOwner should probably verify node is part of the
        // same layout fragment as the fragment owner to insure a misbehaving front-end doesn't
        // do an improper operation.

        return isFragmentOwner || node.isDeleteAllowed();
    }

    /**
     * Returns true if we are dealing with a fragment layout or if editing of attributes is allowed,
     * or the node is a channel since ad-hoc parameters can always be added.
     */
    @Override
    public boolean canUpdateNode(IUserLayoutNodeDescription node) {
        if (node == null) return false;

        return isFragmentOwner
                || node.isEditAllowed()
                || node instanceof IUserLayoutChannelDescription;
    }

    /**
     * Unsupported operation in DLM. This feature is handled by pluggable processors in the DLM
     * processing pipe. See properties/dlmContext.xml.
     */
    @Override
    public void markAddTargets(IUserLayoutNodeDescription node) {
        throw new UnsupportedOperationException(
                "Use an appropriate " + "processor for adding targets.");
    }

    /**
     * Unsupported operation in DLM. This feature is handled by pluggable processors in the DLM
     * processing pipe. See properties/dlmContext.xml.
     */
    @Override
    public void markMoveTargets(String nodeId) throws PortalException {
        throw new UnsupportedOperationException(
                "Use an appropriate " + "processor for adding targets.");
    }

    @Override
    public String getParentId(String nodeId) throws PortalException {
        Document uld = this.getUserLayoutDOM();
        Element nelement = uld.getElementById(nodeId);
        if (nelement != null) {
            Node parent = nelement.getParentNode();
            if (parent != null) {
                if (parent.getNodeType() != Node.ELEMENT_NODE) {
                    throw new PortalException(
                            "Node with id=\""
                                    + nodeId
                                    + "\" is attached to something other then an element node.");
                }
                Element e = (Element) parent;
                return e.getAttribute("ID");
            }
            return null;
        }
        throw new PortalException(
                "Node with id=\""
                        + nodeId
                        + "\" doesn't exist. Occurred in layout for "
                        + owner.getUserName()
                        + ".");
    }

    @Override
    public String getNextSiblingId(String nodeId) throws PortalException {
        Document uld = this.getUserLayoutDOM();
        Element nelement = uld.getElementById(nodeId);
        if (nelement != null) {
            Node nsibling = nelement.getNextSibling();
            // scroll to the next element node
            while (nsibling != null && nsibling.getNodeType() != Node.ELEMENT_NODE) {
                nsibling = nsibling.getNextSibling();
            }
            if (nsibling != null) {
                Element e = (Element) nsibling;
                return e.getAttribute("ID");
            }
            return null;
        }
        throw new PortalException(
                "Node with id=\""
                        + nodeId
                        + "\" doesn't exist. Occurred "
                        + "in layout for "
                        + owner.getUserName()
                        + ".");
    }

    @Override
    public String getPreviousSiblingId(String nodeId) throws PortalException {
        Document uld = this.getUserLayoutDOM();
        Element nelement = uld.getElementById(nodeId);
        if (nelement != null) {
            Node nsibling = nelement.getPreviousSibling();
            // scroll to the next element node
            while (nsibling != null && nsibling.getNodeType() != Node.ELEMENT_NODE) {
                nsibling = nsibling.getNextSibling();
            }
            if (nsibling != null) {
                Element e = (Element) nsibling;
                return e.getAttribute("ID");
            }
            return null;
        }
        throw new PortalException(
                "Node with id=\""
                        + nodeId
                        + "\" doesn't exist. Occurred in layout for "
                        + owner.getUserName()
                        + ".");
    }

    @Override
    public Enumeration<String> getChildIds(String nodeId) throws PortalException {
        return getChildIds(nodeId, false);
    }

    private Enumeration<String> getVisibleChildIds(String nodeId) throws PortalException {
        return getChildIds(nodeId, true);
    }

    private Enumeration<String> getChildIds(String nodeId, boolean visibleOnly)
            throws PortalException {
        Vector<String> v = new Vector<String>();
        IUserLayoutNodeDescription node = getNode(nodeId);
        if (node instanceof IUserLayoutFolderDescription) {
            Document uld = this.getUserLayoutDOM();
            Element felement = uld.getElementById(nodeId);
            for (Node n = felement.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE
                        && (visibleOnly == false
                                || (visibleOnly == true
                                        && ((Element) n)
                                                .getAttribute(Constants.ATT_HIDDEN)
                                                .equals("false")))) {
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
    public String getCacheKey() {
        return this.cacheKey;
    }

    /**
     * This is outright cheating! We're supposed to analyze the user layout tree and return a key
     * that corresponds uniquely to the composition and the structure of the tree. Here we just
     * return a different key whenever anything changes. So if one was to move a node back and
     * forth, the key would always never (almost) come back to the original value, even though the
     * changes to the user layout are cyclic.
     */
    private void updateCacheKey() {
        this.cacheKey = Long.toString(rnd.nextLong());
    }

    @Override
    public int getLayoutId() {
        return profile.getLayoutId();
    }

    /**
     * Returns the subscribe ID of a channel having the passed in functional name or null if it
     * can't find such a channel in the layout.
     */
    @Override
    public String getSubscribeId(String fname) {
        final Document userLayout = this.getUserLayoutDOM();
        return new PortletSubscribeIdResolver(fname).traverseDocument(userLayout);
    }

    @Override
    public String getSubscribeId(String parentFolderId, String fname) {
        final Map<String, String> variables = new HashMap<String, String>();
        variables.put("parentFolderId", parentFolderId);
        variables.put("fname", fname);

        final Document userLayout = this.getUserLayoutDOM();
        final Element fnameNode =
                this.xpathOperations.evaluate(
                        "//folder[@ID=$parentFolderId]/descendant::channel[@fname=$fname]",
                        variables,
                        userLayout,
                        XPathConstants.NODE);
        if (fnameNode != null) {
            return fnameNode.getAttribute("ID");
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.IUserLayoutManager#getUserLayout()
     */
    @Override
    public IUserLayout getUserLayout() throws PortalException {
        // Copied from SimpleLayoutManager since our layouts are regular
        // simple layouts, ie Documents.
        return new SimpleLayout(
                this.getDistributedUserLayout(), String.valueOf(profile.getLayoutId()));
    }

    /* Returns the ID attribute of the root folder of the layout. This folder
     * is defined to be the single child of the top most "layout" Element.
     *
     * @see org.apereo.portal.layout.IUserLayoutManager#getRootFolderId()
     * @see org.apereo.portal.layout.dlm.RootLocator
     */
    @Override
    public String getRootFolderId() {
        if (rootNodeId == null) {
            Document layout = getUserLayoutDOM();

            Element rootNode =
                    this.xpathOperations.evaluate("//layout/folder", layout, XPathConstants.NODE);
            if (rootNode == null
                    || !rootNode.getAttribute(Constants.ATT_TYPE)
                            .equals(Constants.ROOT_FOLDER_ID)) {
                LOG.error(
                        "Unable to locate root node in layout of "
                                + owner.getUserName()
                                + ". Resetting corrupted layout.");
                resetLayout((String) null);

                rootNode =
                        this.xpathOperations.evaluate(
                                "//layout/folder", layout, XPathConstants.NODE);
                if (rootNode == null
                        || !rootNode.getAttribute(Constants.ATT_TYPE)
                                .equals(Constants.ROOT_FOLDER_ID)) {
                    throw new PortalException(
                            "Corrupted layout detected for "
                                    + owner.getUserName()
                                    + " and resetting layout failed.");
                }
            }
            rootNodeId = rootNode.getAttribute("ID");
        }
        return rootNodeId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apereo.portal.layout.IUserLayoutManager#getDepth(java.lang.String)
     */
    @Override
    public int getDepth(String nodeId) throws PortalException {
        // can't see what it calling this anywhere so ignoring for now.
        // TODO waiting to hear back from peter/michael
        return 0;
    }

    /* Return an implementation of IUserLayoutNodeDescription appropriate for
     * the type of node indicated. Currently, the only two types supported are
     * IUserLayoutNodeDescription.FOLDER and LayoutNodeType.PORTLET.
     *
     * @see org.apereo.portal.layout.IUserLayoutManager#createNodeDescription(int)
     */
    @Override
    public IUserLayoutNodeDescription createNodeDescription(LayoutNodeType nodeType)
            throws PortalException {
        if (nodeType == LayoutNodeType.FOLDER) {
            return new UserLayoutFolderDescription();
        }
        return new ChannelDescription();
    }

    /**
     * Resets the layout of the user with the specified user id if the current user is an
     * administrator or a member of any administrative sub-group. Has no effect if these
     * requirements are not met.
     *
     * @return true if layout was reset, false otherwise.
     * @param loginId
     */
    public boolean resetLayout(String loginId) {
        boolean resetSuccess = false;
        boolean resetCurrentUserLayout = (null == loginId);

        if (resetCurrentUserLayout || (!resetCurrentUserLayout && AdminEvaluator.isAdmin(owner))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Reset layout requested for user with id " + loginId + ".");
            }
            int portalID = IPerson.UNDEFINED_ID;
            IPerson person;

            if (resetCurrentUserLayout || loginId.equals(owner.getUserName())) {
                person = owner;
                portalID = owner.getID();
            } else {
                // Need to get the portal id
                person = PersonFactory.createPerson();
                person.setAttribute(IPerson.USERNAME, loginId);

                try {
                    portalID = userIdentityStore.getPortalUID(person);
                    person.setID(portalID);
                } catch (Exception e) {
                    // ignore since the store will log the problem
                }
            }
            if (portalID != IPerson.UNDEFINED_ID) {
                resetSuccess = resetLayout(person);
            }
        } else {
            LOG.error(
                    "Layout reset requested for user "
                            + loginId
                            + " by "
                            + owner.getID()
                            + " who is not an administrative user.");
        }
        return resetSuccess;
    }

    /** Resets the layout of the specified user. */
    private boolean resetLayout(IPerson person) {
        final String userName = person.getUserName();
        if (PersonFactory.getGuestUsernames().contains(userName)) {
            throw new IllegalArgumentException("CANNOT RESET LAYOUT FOR A GUEST USER: " + person);
        }
        LOG.warn("Resetting user layout for: " + userName, new Throwable());

        boolean layoutWasReset = false;

        try {
            userIdentityStore.removePortalUID(person.getID());
            userIdentityStore.getPortalUID(person, true);

            // see if the current user was the one to reset their layout and if
            // so we need to refresh our local copy of their layout
            if (person == owner) {
                this.layoutCachingService.removeCachedLayout(person, profile);
                updateCacheKey();
                getUserLayoutDOM();
            }
            // if (isFragmentOwner)
            // {
            //
            //    store.updateOwnerLayout(person);
            // }
            layoutWasReset = true;
        } catch (Exception e) {
            LOG.error("Unable to reset layout for " + person.getUserName() + ".", e);
        }
        return layoutWasReset;
    }

    public IUserLayoutNodeDescription createNodeDescription(Element node) throws PortalException {
        String type = node.getNodeName();
        if (type.equals(Constants.ELM_CHANNEL)) {
            return new ChannelDescription(node);
        } else if (type.equals(Constants.ELM_FOLDER)) {
            return new UserLayoutFolderDescription(node);
        } else {
            throw new PortalException("Given XML Element is not a channel!");
        }
    }
}
