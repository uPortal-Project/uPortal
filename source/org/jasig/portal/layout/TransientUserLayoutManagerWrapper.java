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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelParameter;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

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

    // transient folder's subscribe id <'f'older><'t'ransient><id>
    public final static String TRANSIENT_FOLDER_ID="ft1";
    // channel subscription prefix  <'c'hannel><'t'ransient><'f'older>
    public final static String SUBSCRIBE_PREFIX = "ctf";
    
    // The original user layout manager
    private IUserLayoutManager man=null;
    
    // contains fname --> subscribe id mappings (for transient channels only)
    private Map mFnameMap = Collections.synchronizedMap(new HashMap());
    private Map mSubIdMap = Collections.synchronizedMap(new HashMap());
    // stores channel defs by subscribe id (transient channels only)
    private Map mChanMap = Collections.synchronizedMap(new HashMap());

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

    public void setUserLayout(IUserLayout userLayout) throws PortalException {
        man.setUserLayout(userLayout);
    }


    public void getUserLayout(ContentHandler ch) throws PortalException {
        man.getUserLayout(new TransientUserLayoutManagerSAXFilter(ch));
    }

    public void getUserLayout(String nodeId, ContentHandler ch) throws PortalException {
        IUserLayoutNodeDescription node = this.getNode(nodeId);
        if ( null != node ) {
          IUserLayoutNodeDescription layoutNode = man.getNode(nodeId);
          if ( layoutNode != null )
           man.getUserLayout(nodeId, new TransientUserLayoutManagerSAXFilter(ch));
          else {
             Document doc = DocumentFactory.getNewDocument();
             try{
                Element e = node.getXML(doc);
                doc.appendChild(e);
                Transformer trans=TransformerFactory.newInstance().newTransformer();
                trans.transform(new DOMSource(doc), new SAXResult(new TransientUserLayoutManagerSAXFilter(ch)));
             }
             catch( Exception e ){
                throw new PortalException("Encountered an exception trying to output user layout",e);
             }
          }
        }
    }


    public void setLayoutStore(IUserLayoutStore ls) {
        man.setLayoutStore(ls);
    }

    public Document getUserLayoutDOM() throws PortalException {
        return man.getUserLayoutDOM();
    }


    public void loadUserLayout() throws PortalException {
        man.loadUserLayout();
    }

    public void saveUserLayout() throws PortalException {
        man.saveUserLayout();
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
            // not found in layout...
            LogService.log(LogService.DEBUG,
                           "Node '" + nodeId + "' is not in layout, " +
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

    public IUserLayoutNodeDescription createNodeDescription( int nodeType ) throws PortalException {
        return man.createNodeDescription(nodeType);
    }

    public boolean addLayoutEventListener(LayoutEventListener l){
        return man.addLayoutEventListener(l);
    }
    public boolean removeLayoutEventListener(LayoutEventListener l){
        return man.removeLayoutEventListener(l);
    }


    /**
     * Given a subscribe Id, return a ChannelDefinition.
     *
     * @param subId  the subscribe id for the ChannelDefinition.
     * @return a <code>ChannelDefinition</code>
     **/
    protected ChannelDefinition getChannelDefinition( String subId )
        throws PortalException
    {
        ChannelDefinition chanDef = (ChannelDefinition)
            mChanMap.get(subId);

        if ( null == chanDef ){
            String fname = getFname(subId);

            LogService.log(LogService.DEBUG,"TransientUserLayoutManagerWrapper>>getChannelDefinition, " +
                           "attempting to get a channel definition using functional name: " + fname );
            try{
                chanDef = ChannelRegistryStoreFactory.
                    getChannelRegistryStoreImpl().getChannelDefinition(fname);
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
        return (String)mSubIdMap.get(subId);
    }


    /**
     * Given an functional name, return its subscribe id.
     *
     * @param fname  the functional name to lookup
     * @return the fname's subscribe id.
     **/
    public String getSubscribeId(String fname) throws PortalException {

        // see if a given subscribe id is already in the map
        String subId=(String)mFnameMap.get(fname);
        if(subId==null) {
            // see if a given subscribe id is already in the layout
            subId = man.getSubscribeId(fname);
        }

        // obtain a description of the transient channel and
        // assign a new transient channel id
        if ( subId == null ) {
            try {
                ChannelDefinition chanDef = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl().getChannelDefinition(fname);
                if(chanDef!=null) {
                    // assign a new id
                    subId = getNextSubscribeId();
                    mFnameMap.put(fname,subId);
                    mSubIdMap.put(subId,fname);
                    mChanMap.put(subId,chanDef);
                }
            } catch (Exception e) {
                LogService.log(LogService.ERROR,"TransientUserLayoutManagerWrapper::getSubscribeId() : an exception encountered while trying to obtain ChannelDefinition for fname \""+fname+"\" : "+e);
                subId=null;
            }
        }
        return subId;
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
        String fname = getFname(nodeId);
        if ( null == fname || fname.equals("") )
            throw new PortalException( "Could not find a transient node " +
                                       "for id: " + nodeId );

        IUserLayoutChannelDescription ulnd = new UserLayoutChannelDescription();
        try
        {
            // check cache first
            ChannelDefinition chanDef = (ChannelDefinition)mChanMap.get(nodeId);

            if ( null == chanDef ) {
                chanDef = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl().getChannelDefinition(fname);
                mChanMap.put(nodeId,chanDef);
            }

            ulnd.setId(nodeId);
            ulnd.setName(chanDef.getName());
            ulnd.setUnremovable(true);
            ulnd.setImmutable(true);
            ulnd.setHidden(false);
            ulnd.setTitle(chanDef.getTitle());
            ulnd.setDescription(chanDef.getDescription());
            ulnd.setClassName(chanDef.getJavaClass());
            ulnd.setChannelPublishId("" + chanDef.getId());
            ulnd.setChannelTypeId("" + chanDef.getTypeId());
            ulnd.setFunctionalName(chanDef.getFName());
            ulnd.setTimeout(chanDef.getTimeout());
            ulnd.setEditable(chanDef.isEditable());
            ulnd.setHasHelp(chanDef.hasHelp());
            ulnd.setHasAbout(chanDef.hasAbout());

            ChannelParameter[] parms = chanDef.getParameters();
            for ( int i=0; i<parms.length; i++ )
            {
                ChannelParameter parm = (ChannelParameter)parms[i];
                ulnd.setParameterValue(parm.getName(),parm.getValue());
                ulnd.setParameterOverride(parm.getName(),parm.getOverride());
            }

        }
        catch( Exception e )
        {
            throw new PortalException( "Failed to obtain channel definition " +
                                       "using fname: " + fname );
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


    /**
     * This filter incorporates transient channels into a layout.
     * This provides the ability for channel definitions to reside in
     * a layout w/out the owner having to actually have them
     * persisted.
     */
    class TransientUserLayoutManagerSAXFilter extends SAX2FilterImpl {

        private static final String LAYOUT="layout";
        private static final String LAYOUT_FRAGMENT="layout_fragment";
        private static final String FOLDER="folder";
        private static final String CHANNEL="channel";
        private static final String PARAMETER="parameter";

        public TransientUserLayoutManagerSAXFilter(ContentHandler handler) {
            super(handler);
        }

        public TransientUserLayoutManagerSAXFilter(XMLReader parent) {
            super(parent);
        }

        public void startElement(String uri, String localName, String qName,
                                 Attributes atts)
            throws SAXException {
            // check for root node (layout_fragment for detached
            // nodes), as that's where the transient folder/channel(s)
            // need to be added.
            String id = atts.getValue("ID");
            if ( null != id && id.equals(getRootFolderId()))
            {
                // pass root event up the chain
                super.startElement(uri,localName,qName,atts);
                // create folder off of root layout to act as
                // container for transient channels
                AttributesImpl folderAtts = new AttributesImpl();
                folderAtts.addAttribute("","ID","ID","ID",TRANSIENT_FOLDER_ID);
                folderAtts.addAttribute("","type","type","CDATA","regular" );
                folderAtts.addAttribute("","hidden","hidden","CDATA","true");
                folderAtts.addAttribute("","unremovable","unremovable","CDATA","true");
                folderAtts.addAttribute("","immutable","immutable","CDATA","true");
                folderAtts.addAttribute("","name","name","CDATA","Transient Folder");
                startElement("",FOLDER,FOLDER,folderAtts);
                return;
            }
            else if ( qName.equals(FOLDER) )
            {
                id = atts.getValue("ID");
                if ( null != id && id.equals(TRANSIENT_FOLDER_ID) )
                {
                    // pass event up the chain so it's added
                    // as a child of the root
                    super.startElement(uri,localName,qName,atts);

                    // add a channel to the transient folder
                    // implementation
                    String subscribeId = "";
                    try
                    {
                        subscribeId = getFocusedId();
                        // append channel element iff subscribeId describes
                        // a transient channel, and not a regular layout channel

                        if ( null != subscribeId && !subscribeId.equals("") && mSubIdMap.containsKey(subscribeId))
                        {
                            ChannelDefinition chanDef = getChannelDefinition(subscribeId);
                            AttributesImpl channelAttrs = new AttributesImpl();
                            channelAttrs.addAttribute("","ID","ID","ID",subscribeId);
                            channelAttrs.addAttribute("","typeID","typeID","CDATA",
                                                      "" + chanDef.getTypeId());
                            channelAttrs.addAttribute("","hidden","hidden","CDATA","false");
                            channelAttrs.addAttribute("","editable","editable","CDATA",
                                                      CommonUtils.boolToStr(chanDef.isEditable()));
                            channelAttrs.addAttribute("","unremovable","unremovable","CDATA","true");
                            channelAttrs.addAttribute("","name","name","CDATA",chanDef.getName());
                            channelAttrs.addAttribute("","description","description","CDATA",
                                                      chanDef.getDescription());
                            channelAttrs.addAttribute("","title","title","CDATA",chanDef.getTitle());
                            channelAttrs.addAttribute("","class","class","CDATA",chanDef.getJavaClass());
                            channelAttrs.addAttribute("","chanID","chanID","CDATA",
                                                      "" + chanDef.getId());
                            channelAttrs.addAttribute("","fname","fname","CDATA",chanDef.getFName());
                            channelAttrs.addAttribute("","timeout","timeout","CDATA",
                                                      "" + chanDef.getTimeout());
                            channelAttrs.addAttribute("","hasHelp","hasHelp","CDATA",
                                                      CommonUtils.boolToStr(chanDef.hasHelp()));
                            channelAttrs.addAttribute("","hasAbout","hasAbout","CDATA",
                                                      CommonUtils.boolToStr(chanDef.hasAbout()));

                            startElement("",CHANNEL,CHANNEL,channelAttrs);

                            // now add channel parameters
                            ChannelParameter[] chanParms = chanDef.getParameters();
                            for( int i=0; i<chanParms.length; i++ )
                            {
                                AttributesImpl parmAttrs = new AttributesImpl();
                                ChannelParameter parm = (ChannelParameter)chanParms[i];
                                parmAttrs.addAttribute("","name","name","CDATA",parm.getName());
                                parmAttrs.addAttribute("","value","value","CDATA",parm.getValue());

                                startElement("",PARAMETER,PARAMETER,parmAttrs);
                                endElement("",PARAMETER,PARAMETER);
                            }

                            endElement("",CHANNEL,CHANNEL);
                        }
                    }
                    catch( Exception e )
                    {
                        LogService.log(LogService.ERROR,
                                       "Could not obtain channel definition " +
                                       "from database for subscribe id: " +
                                       subscribeId + " - error is: " + e.getMessage() );
                    }

                    // now pass folder up the chain so it's closed
                    // out
                    super.endElement(uri,localName,qName);
                    return;
                }
                else
                {
                    AttributesImpl attsImpl = new AttributesImpl(atts);
                    super.startElement(uri,localName,qName,attsImpl);
                }
            } else {
                AttributesImpl attsImpl = new AttributesImpl(atts);
                super.startElement(uri,localName,qName,attsImpl);
            }
        }
    }
}

