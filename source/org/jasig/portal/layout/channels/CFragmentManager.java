/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.layout.channels;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.IServant;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerServantFactory;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IPrivileged;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.layout.ALFolder;
import org.jasig.portal.layout.ALNode;
import org.jasig.portal.layout.ALFragment;
import org.jasig.portal.layout.IAggregatedUserLayoutManager;
import org.jasig.portal.layout.ILayoutFragment;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutNodeDescription;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;


/**
 * A channel for adding new content to a layout.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class CFragmentManager extends BaseChannel implements IPrivileged {

	private static final String sslLocation = "/org/jasig/portal/channels/CFragmentManager/CFragmentManager.ssl";
	private IAggregatedUserLayoutManager alm;
	private ThemeStylesheetUserPreferences themePrefs;
	private Map fragments;
	private boolean servantRender = false;
	private String servantFragmentId;
	private static Map servants = new HashMap();
	private final static int MAX_SERVANTS = 50; 

	public CFragmentManager() throws PortalException {
		super();
	}
	
	private class ServantSAXFilter extends SAX2FilterImpl {

		 public ServantSAXFilter(ContentHandler ch) throws PortalException {
			super(ch);
		 }
		   
		 public void startElement (String uri, String localName, String qName,  Attributes atts) throws SAXException {
		  try {	
			super.startElement(uri,localName,qName,atts);
			if(qName.equals("groupServant")) {
			  if ( servantRender ) {
			   getGroupServant().renderXML(contentHandler);
			  } 
			}	
			
		  } catch ( Exception e ) {
		  	  throw new SAXException(e.toString());	
		  }
		 }	

	}

    private synchronized IGroupMember[] getGroupMembers(String fragmentId) throws PortalException {
       Enumeration groupsEnum = alm.getPublishGroups(fragmentId);	
       ArrayList groups = new ArrayList(); 
       while ( groupsEnum.hasMoreElements() ) {
       	String groupKey = (String) groupsEnum.nextElement();
       	IGroupMember member = GroupService.findGroup(groupKey); 
       	if ( member != null )
       	 groups.add(member);  
       }
		IGroupMember[] members = new IGroupMember[groups.size()];
        for ( int i = 0; i < members.length; i++ ) {
          	members[i] = (IGroupMember) groups.get(i);
        }
        return members;
    }   


    private String getServantKey() {
      return CommonUtils.envl(servantFragmentId,"new"); 	
    }

	/**
		 * Produces a group servant
		 * @return the group servant
		 */
	private synchronized IServant getGroupServant() throws PortalException {
		if ( servants.size() > MAX_SERVANTS ) servants.clear();	
		IServant groupServant = (IServant) servants.get(getServantKey());
		if ( groupServant == null ) {
				 try {
				  // create the appropriate servant
				  if ( servantFragmentId != null && CommonUtils.parseInt(servantFragmentId) > 0  ) {
					groupServant = CGroupsManagerServantFactory.getGroupsServantforSelection(staticData,
													"Please select groups or people who should have access to this fragment:",
													GroupService.EVERYONE,true,true,getGroupMembers(servantFragmentId));	
				  } else 
					groupServant = CGroupsManagerServantFactory.getGroupsServantforSelection(staticData,
								"Please select groups or people who should have access to this fragment:",
								GroupService.EVERYONE);		
								
				  if ( groupServant != null ) 
				     servants.put(getServantKey(),groupServant);	
				  									
				} catch (Exception e) {
					throw new PortalException(e);
				  }
		}	
		 groupServant.setRuntimeData((ChannelRuntimeData)runtimeData.clone());		  
		 return groupServant;
    }

	private String createFolder( ALFragment fragment ) throws PortalException {
		IUserLayoutNodeDescription folderDesc = alm.createNodeDescription(IUserLayoutNodeDescription.FOLDER);
		folderDesc.setName("Fragment column");
		return alm.addNode(folderDesc, getFragmentRootId(fragment.getId()), null).getId();
	}

	private void analyzeParameters( XSLT xslt ) throws PortalException {
		
		String fragmentId = CommonUtils.nvl(runtimeData.getParameter("uPcFM_selectedID"));
		String action = CommonUtils.nvl(runtimeData.getParameter("uPcFM_action"));
		  
				if (action.equals("save_new")) {
					String fragmentName = runtimeData.getParameter("fragment_name");
					String funcName = runtimeData.getParameter("fragment_fname");
					String fragmentDesc = runtimeData.getParameter("fragment_desc");
					String fragmentType = runtimeData.getParameter("fragment_type");
					String fragmentFolder = runtimeData.getParameter("fragment_add_folder");
					boolean isPushedFragment = ("pushed".equals(fragmentType))?true:false;
					fragmentId = alm.createFragment(CommonUtils.nvl(funcName),CommonUtils.nvl(fragmentDesc),CommonUtils.nvl(fragmentName));
					ALFragment newFragment = (ALFragment) alm.getFragment(fragmentId);
					if ( newFragment != null ) { 
					  if ( isPushedFragment ) 
					    newFragment.setPushedFragment(); 
					  else
					    newFragment.setPulledFragment();
					  // Saving the changes in the database  
					  alm.saveFragment(newFragment);
					  // Updating the fragments map
					  fragments.put(fragmentId,newFragment); 
					  // Check if we need to create an additional folder on the fragment root
					  if ( "true".equals(fragmentFolder) ) {
					  	alm.loadFragment(fragmentId);
					  	createFolder(newFragment);
					  	alm.saveFragment();
					  }
					}     
				} else if (action.equals("save")) {
					String funcName = runtimeData.getParameter("fragment_fname");
					String fragmentName = runtimeData.getParameter("fragment_name");
					String fragmentDesc = runtimeData.getParameter("fragment_desc");
					String fragmentType = runtimeData.getParameter("fragment_type");
					boolean isPushedFragment = ("pushed".equals(fragmentType))?true:false;
					ALFragment fragment = (ALFragment) fragments.get(fragmentId);
				    if ( fragment != null ) { 
					   if ( isPushedFragment ) 
					     fragment.setPushedFragment(); 
					   else
					     fragment.setPulledFragment();
					   fragment.setFunctionalName(CommonUtils.nvl(funcName));
					   fragment.setDescription(CommonUtils.nvl(fragmentDesc));  
					   String fragmentRootId = getFragmentRootId(fragmentId);
					   ALNode fragmentRoot = fragment.getNode(fragmentRootId);
					   fragmentRoot.getNodeDescription().setName(fragmentName);
					   // Saving the changes in the database  
					   alm.saveFragment(fragment);					
					}     
				} else if (action.equals("delete")) {
					if (CommonUtils.parseInt(fragmentId) > 0) {
					  alm.deleteFragment(fragmentId);
					  // Updating the fragments map
					  fragments.remove(fragmentId);  
					  fragmentId = (fragments != null && fragments.isEmpty())?(String) fragments.keySet().toArray()[0]:"";
					} else
					   new PortalException ( "Invalid fragment ID="+fragmentId);
				} else if (action.equals("publish")) {
					servantRender = true; 
					servantFragmentId = fragmentId;
				}  else if (action.equals("publish_finish")) {
					IGroupMember[] gms = (IGroupMember[]) getGroupServant().getResults();
					for ( int i = 0; i < gms.length; i++ )
				      System.out.println ( "group key: " + gms[i].getKey());
		            servantRender = false; 
	               }
				
				
		           if ( getGroupServant().isFinished() ) {
					IGroupMember[] gms = (IGroupMember[]) getGroupServant().getResults();
					if ( gms != null && "Done".equals(runtimeData.getParameter("grpCommand")))
					  alm.setPublishGroups(gms,servantFragmentId);
		           	servants.remove(getServantKey());
					servantRender = false;
		           } 	 
				
				   boolean noAction = action.equals("");
				   if ( servantRender && noAction ) {
				     fragmentId = servantFragmentId;
				     action = "publish";	
				   }
				   
				   // Loading the default layout if the fragment is loaded in the theme
				   if ( noAction && alm.isFragmentLoaded() )
				      alm.loadUserLayout();
				
				xslt.setStylesheetParameter("uPcFM_selectedID",fragmentId);
			    xslt.setStylesheetParameter("uPcFM_action",action);	
		
	}
	
	
	private String getFragmentRootId( String fragmentId ) throws PortalException {
	  if ( fragments != null && !fragments.isEmpty() ) {
		ALFragment fragment = (ALFragment) fragments.get(fragmentId);
		ALFolder rootFolder = (ALFolder) fragment.getNode(fragment.getRootId());
		return rootFolder.getFirstChildNodeId();	
	  }
	    return null;
	}

	/**
	 * Passes portal control structure to the channel.
	 * @see PortalControlStructures
	 */
	public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
	    IUserLayoutManager ulm = pcs.getUserPreferencesManager().getUserLayoutManager();
		if (ulm instanceof TransientUserLayoutManagerWrapper)
		  ulm = ((TransientUserLayoutManagerWrapper)ulm).getOriginalLayoutManager();
	    if (ulm instanceof IAggregatedUserLayoutManager)
		  alm = (IAggregatedUserLayoutManager) ulm;
		themePrefs = pcs.getUserPreferencesManager().getUserPreferences().getThemeStylesheetUserPreferences();	
		// Refresh the fragment list
		refreshFragments();
	}

	private Document getFragmentList() throws PortalException {
		Document document = DocumentFactory.getNewDocument();
		Element fragmentsNode = document.createElement("fragments");
		document.appendChild(fragmentsNode);
		//fragmentsNode.appendChild(getGroupsXML(document));
		Element category = document.createElement("category");
		category.setAttribute("name", "Fragments");
		category.setAttribute("expanded", "true");
		fragmentsNode.appendChild(category);
		boolean updateList = false;
		if (fragments != null) {
			for ( Iterator ids = fragments.keySet().iterator(); ids.hasNext(); ) {
				String fragmentId = (String) ids.next();
				ALFragment fragment = (ALFragment) fragments.get(fragmentId);
				String fragmentRootId = getFragmentRootId(fragmentId);
				// if the fragment root ID is NULL then the fragment must be deleted
				// since it does not have any content
				if ( fragmentRootId == null ) {
					alm.deleteFragment(fragmentId);
				    if ( !updateList ) 
				      updateList = true;
				    continue;
				}
				Element fragmentNode = document.createElement("fragment");
				category.appendChild(fragmentNode);
				Element id = document.createElement("ID");
				id.appendChild(document.createTextNode(fragmentId));
				fragmentNode.appendChild(id);
				Element rootId = document.createElement("rootNodeID");
				rootId.appendChild(document.createTextNode(fragmentRootId));
				rootId.setAttribute("immutable",fragment.getNode(fragmentRootId).getNodeDescription().isImmutable()?"Y":"N");
				fragmentNode.appendChild(rootId);
				Element type = document.createElement("type");
				type.appendChild(
					document.createTextNode(
						fragment.isPushedFragment() ? "pushed" : "pulled"));
				fragmentNode.appendChild(type);
				Element fname = document.createElement("fname");
				fname.appendChild(
					document.createTextNode(fragment.getFunctionalName()));
				fragmentNode.appendChild(fname);
				Element name = document.createElement("name");
				name.appendChild(
					document.createTextNode(
						fragmentRootId != null
						? ((ALNode) fragment.getNode(fragmentRootId)).getNodeDescription().getName()
						: fragment.getFunctionalName()));
				fragmentNode.appendChild(name);
				Element desc = document.createElement("description");
				desc.appendChild(document.createTextNode(fragment.getDescription()));
				fragmentNode.appendChild(desc);
			}
			
			// If there were any fragments withno rootID and these fragments were deleted - need to update the fragment list
			if ( updateList )
			  refreshFragments();
		}
		//System.out.println ( org.jasig.portal.utils.XML.serializeNode(document));
		return document;
	}

	public void setStaticData(ChannelStaticData sd) throws PortalException {
		staticData = sd;
	}
	public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
	  runtimeData = rd;	
	}

	public void refreshFragments() throws PortalException {
		  Set fragmentIds = alm.getFragments();
		  fragments = new HashMap();
			for (Iterator ids = fragmentIds.iterator(); ids.hasNext(); ) {
				String fragmentId = (String) ids.next();
				ILayoutFragment layoutFragment = alm.getFragment(fragmentId);
				if (layoutFragment == null || !(layoutFragment instanceof ALFragment))
					throw new PortalException("The fragment must be "+ALFragment.class.getName()+" type!");
				fragments.put(fragmentId,layoutFragment);
			}
		
	}

	public void renderXML(ContentHandler out) throws PortalException {
		
		
		XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
		
		analyzeParameters(xslt);
		
		xslt.setXML(getFragmentList());
		xslt.setXSL(sslLocation,"fragmentManager",runtimeData.getBrowserInfo());
		xslt.setTarget(new ServantSAXFilter(out));
		xslt.setStylesheetParameter("baseActionURL",runtimeData.getBaseActionURL());
	    
	    
		/*if ( servantRender ) {
		  xslt.setStylesheetParameter("action", "selectGroupsButtons");  
		}*/ 
		
		xslt.transform();    
	}

}