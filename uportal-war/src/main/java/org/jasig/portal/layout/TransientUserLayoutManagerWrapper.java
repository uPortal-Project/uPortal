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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLEventReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription.LayoutNodeType;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.spring.locator.PortletDefinitionRegistryLocator;
import org.w3c.dom.Document;

/**
 * Wraps {@link IUserLayoutManager} interface to provide ability to
 * incorporate channels into a user layout that are not part of
 * their layout structure (persistent).
 *
 * The channels are incorporated upon request (functional name)
 * and remain part of the layout structure only as long as they
 * are the target channel.
 *
 * @author <a href="mailto:kstacks@sct.com">Keith Stacks</a>
 * @version $Revision$
 */
public class TransientUserLayoutManagerWrapper implements IUserLayoutManager {

    private static final Log log = LogFactory.getLog(TransientUserLayoutManagerWrapper.class);
    
    // transient folder's subscribe id <'f'older><'t'ransient><id>
    public final static String TRANSIENT_FOLDER_ID="ft1";
    // channel subscription prefix  <'c'hannel><'t'ransient><'f'older>
    public final static String SUBSCRIBE_PREFIX = "ctf";
    
    // The original user layout manager
    private IUserLayoutManager man=null;
    
    // contains fname --> subscribe id mappings (for transient channels only)
    private Map<String, String> mFnameMap = Collections.synchronizedMap(new HashMap<String, String>());
    // contains subscribe id --> fname mappings (for transient channels only)
    private Map<String, String> mSubIdMap = Collections.synchronizedMap(new HashMap<String, String>());
    // stores channel defs by subscribe id (transient channels only)
    private Map<String, IPortletDefinition> mChanMap = Collections.synchronizedMap(new HashMap<String, IPortletDefinition>());

    // current root/focused subscribe id
    private String mFocusedId = "";
    // subscription id counter for generating subscribe ids
    private int mSubId = 0;

    public TransientUserLayoutManagerWrapper(IUserLayoutManager manager) throws PortalException {
        this.man=manager;
        if(man==null) {
            throw new PortalException("Cannot wrap a null IUserLayoutManager !");
        }
    }

    public IUserLayoutManager getOriginalLayoutManager() throws PortalException {
           return man;
    }

    public void setOriginalLayoutManager(IUserLayoutManager man ) throws PortalException {
           this.man = man;
    }

    public IUserLayout getUserLayout() throws PortalException {
        return man.getUserLayout();
    }

    @Override
    public XMLEventReader getUserLayoutReader() {
        final XMLEventReader userLayoutReader = man.getUserLayoutReader();
        return new TransientUserLayoutXMLEventReader(this, userLayoutReader);
    }

    public Document getUserLayoutDOM() throws PortalException {
        return man.getUserLayoutDOM();
    }


    public void loadUserLayout() throws PortalException {
        man.loadUserLayout();
    }
    
    public void loadUserLayout(boolean reload) throws PortalException {
        man.loadUserLayout(reload);
    }

    public void saveUserLayout() throws PortalException {
        man.saveUserLayout();
    }
    
    @Override
    public Set<String> getAllSubscribedChannels() {
        final Set<String> allSubscribedChannels = new LinkedHashSet<String>(man.getAllSubscribedChannels());
        
        for (final String subscribeId : mSubIdMap.keySet()) {
            allSubscribedChannels.add(subscribeId);
        }
        
        return allSubscribedChannels;
    }

    public IUserLayoutNodeDescription getNode(String nodeId) throws PortalException {
        // check to see if it's in the layout first, if not then
        // build it..
        IUserLayoutNodeDescription ulnd = null;

        // assume that not finding it in the implementation
        // means that it may be a requested (transient) node.
        try {
            ulnd = man.getNode(nodeId);
        } catch( PortalException pe ) {
            if (log.isDebugEnabled())
                log.debug("Node '" + nodeId + "' is not in layout, " +
                           "checking for a transient node...");
        }

        if ( null == ulnd ) {
            // if the requested node hasn't been returned yet, it's
            // likely it's a transient node that isn't actually part of
            // the layout
            ulnd = getTransientNode( nodeId );
        }

        return ulnd;
    }

    public IUserLayoutNodeDescription addNode(IUserLayoutNodeDescription node,String parentId,String nextSiblingId) throws PortalException {
        return man.addNode(node,parentId,nextSiblingId);
    }

    public boolean moveNode(String nodeId, String parentId, String nextSiblingId) throws PortalException {
        // allow all moves, except for those related to the transient channels and folders
        if(nodeId!=null && (!mSubIdMap.containsKey(nodeId)) && (!nodeId.equals(TRANSIENT_FOLDER_ID))) {
            return man.moveNode(nodeId, parentId, nextSiblingId);
        } else {
            return false;
        }
    }

    public boolean deleteNode(String nodeId) throws PortalException {
        // allow all deletions, except for those related to the transient channels and folders
        if(nodeId!=null && (!mSubIdMap.containsKey(nodeId)) && (!nodeId.equals(TRANSIENT_FOLDER_ID))) {
            return man.deleteNode(nodeId);
        } else {
            return false;
        }
    }

    public boolean updateNode(IUserLayoutNodeDescription node) throws PortalException {
        // allow all updates, except for those related to the transient channels and folders
        String nodeId=node.getId();
        if(nodeId!=null && (!mSubIdMap.containsKey(nodeId)) && (!nodeId.equals(TRANSIENT_FOLDER_ID))) {
            return man.updateNode(node);
        } else {
            return false;
        }
    }


    public boolean canAddNode(IUserLayoutNodeDescription node, String parentId, String nextSiblingId) throws PortalException {
        return man.canAddNode(node, parentId, nextSiblingId);
    }

    public boolean canMoveNode(String nodeId, String parentId, String nextSiblingId) throws PortalException {
        // allow all moves, except for those related to the transient channels and folders
        if(nodeId!=null && (!mSubIdMap.containsKey(nodeId)) && (!nodeId.equals(TRANSIENT_FOLDER_ID))) {
            return man.canMoveNode(nodeId, parentId, nextSiblingId);
        } else {
            return false;
        }
    }

    public boolean canDeleteNode(String nodeId) throws PortalException {
        // allow all deletions, except for those related to the transient channels and folders
        if(nodeId!=null && (!mSubIdMap.containsKey(nodeId)) && (!nodeId.equals(TRANSIENT_FOLDER_ID))) {
            return man.canDeleteNode(nodeId);
        } else {
            return false;
        }
    }

    public boolean canUpdateNode(IUserLayoutNodeDescription node) throws PortalException {
        // allow all updates, except for those related to the transient channels and folders
        String nodeId=node.getId();
        if(nodeId!=null && (!mSubIdMap.containsKey(nodeId)) && (!nodeId.equals(TRANSIENT_FOLDER_ID))) {
            return man.canUpdateNode(node);
        } else {
            return false;
        }
    }

    public void markAddTargets(IUserLayoutNodeDescription node) throws PortalException {
        man.markAddTargets(node);
    }

    public void markMoveTargets(String nodeId) throws PortalException {
        man.markMoveTargets(nodeId);
    }

    public String getParentId(String nodeId) throws PortalException {
        if (mChanMap.containsKey(nodeId) || TRANSIENT_FOLDER_ID.equals(nodeId)) {
            return null;
        }

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
        // we don't need to worry about extending the base cache key here,
        // because the transient channels are always rendered in a focused
        // mode, that means that the user preference attributes will be
        // sufficient to describe the layout state.
        // In general, however one would need to append that focused channel
        // subscribe id and fname (both are required for global scope use).
        return man.getCacheKey();
    }

    public int getLayoutId() {
        return man.getLayoutId();
    }

    public String getRootFolderId(){
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


    public IUserLayoutNodeDescription createNodeDescription( LayoutNodeType nodeType ) throws PortalException {
        return man.createNodeDescription(nodeType);
    }


    /**
     * Given a subscribe Id, return a ChannelDefinition.
     *
     * @param subId  the subscribe id for the ChannelDefinition.
     * @return a <code>ChannelDefinition</code>
     **/
    protected IPortletDefinition getChannelDefinition( String subId )
        throws PortalException
    {
        IPortletDefinition chanDef = mChanMap.get(subId);

        if ( null == chanDef ){
            String fname = getFname(subId);
            if (log.isDebugEnabled())
                log.debug("TransientUserLayoutManagerWrapper>>getChannelDefinition, " +
                           "attempting to get a channel definition using functional name: " + fname );
            try{
            	chanDef = PortletDefinitionRegistryLocator.getPortletDefinitionRegistry().getPortletDefinitionByFname(fname);
            }
            catch( Exception e ){
                throw new PortalException( "Failed to get channel information " +
                                           "for subscribeId: " + subId );
            }
            mChanMap.put(subId,chanDef);
        }

        return chanDef;
    }


    /**
     * Given a subscribe Id, return its functional name.
     *
     * @param subId  the subscribe id to lookup
     * @return the subscribe id's functional name
     **/
    public String getFname( String subId )
    {
        return mSubIdMap.get(subId);
    }
    
    public boolean isTransientChannel(String subId) {
        return mSubIdMap.containsKey(subId);
    }


    /**
     * Given an functional name, return its subscribe id.
     *
     * @param fname  the functional name to lookup
     * @return the fname's subscribe id.
     **/
    public String getSubscribeId(String fname) throws PortalException {

        // see if a given subscribe id is already in the map
        String subId=mFnameMap.get(fname);
        if(subId==null) {
            // see if a given subscribe id is already in the layout
            subId = man.getSubscribeId(fname);
        }

        // obtain a description of the transient channel and
        // assign a new transient channel id
        if ( subId == null ) {
            try {
            	IPortletDefinition chanDef = PortletDefinitionRegistryLocator.getPortletDefinitionRegistry().getPortletDefinitionByFname(fname);
                if(chanDef!=null) {
                    // assign a new id
                    subId = getNextSubscribeId();
                    mFnameMap.put(fname,subId);
                    mSubIdMap.put(subId,fname);
                    mChanMap.put(subId,chanDef);
                }
            } catch (Exception e) {
                log.error("TransientUserLayoutManagerWrapper::getSubscribeId() : " +
                        "an exception encountered while trying to obtain " +
                        "ChannelDefinition for fname \""+fname+"\" : "+e);
                subId=null;
            }
        }
        return subId;
    }
    
    /* (non-Javadoc)
	 * @see org.jasig.portal.layout.IUserLayoutManager#getSubscribeId(java.lang.String, java.lang.String)
	 */
	@Override
	public String getSubscribeId(String parentFolderId, String fname) {
		return this.man.getSubscribeId(parentFolderId, fname);
	}

	/**
     * Get the current focused layout subscribe id.
     **/
    public String getFocusedId()
    {
        return mFocusedId;
    }

    /**
     * Set the current focused layout subscribe id.
     *
     * @param subscribeId  Id to be set as focused.
     **/
    public void setFocusedId(String subscribeId)
    {
        mFocusedId = subscribeId;
    }


    /**
     * Return an IUserLayoutChannelDescription by way of nodeId
     *
     * @param nodeId  the node (subscribe) id to get the channel for.
     * @return a <code>IUserLayoutNodeDescription</code>
     **/
    private IUserLayoutChannelDescription getTransientNode(String nodeId)
        throws PortalException
    {
        // get fname from subscribe id
        final String fname = getFname(nodeId);
        if (null == fname || fname.equals("")) {
            return null;
        }

        try {
            // check cache first
            IPortletDefinition chanDef = mChanMap.get(nodeId);

            if (null == chanDef) {
            	chanDef = PortletDefinitionRegistryLocator.getPortletDefinitionRegistry().getPortletDefinitionByFname(fname);
                mChanMap.put(nodeId, chanDef);
            }

            return createUserLayoutChannelDescription(nodeId, chanDef);

        }
        catch (Exception e) {
            throw new PortalException("Failed to obtain channel definition using fname: " + fname);
        }
    }
    
    protected IUserLayoutChannelDescription createUserLayoutChannelDescription(String nodeId, IPortletDefinition chanDef) {
        IUserLayoutChannelDescription ulnd = new UserLayoutChannelDescription();

        ulnd.setId(nodeId);
        ulnd.setName(chanDef.getName());
        ulnd.setUnremovable(true);
        ulnd.setImmutable(true);
        ulnd.setHidden(false);
        ulnd.setTitle(chanDef.getTitle());
        ulnd.setDescription(chanDef.getDescription());
        ulnd.setChannelPublishId("" + chanDef.getPortletDefinitionId().getStringId());
        ulnd.setChannelTypeId("" + chanDef.getType().getId());
        ulnd.setFunctionalName(chanDef.getFName());
        ulnd.setTimeout(chanDef.getTimeout());
        
        Set<IPortletDefinitionParameter> parms = chanDef.getParameters();
        for ( IPortletDefinitionParameter parm : parms )
        {
            ulnd.setParameterValue(parm.getName(),parm.getValue());
        }
        
        return ulnd;
    }


    /**
     * Return the next sequential subscription id.
     *
     * @return a subscribe id
     **/
    private synchronized String getNextSubscribeId()
    {
        mSubId ++;
        return SUBSCRIBE_PREFIX + mSubId;
    }
}

