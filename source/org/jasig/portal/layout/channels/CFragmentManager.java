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
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IPrivileged;
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
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

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

	public CFragmentManager() throws PortalException {
		super();
	}

	private String createFolder( ALFragment fragment ) throws PortalException {
		IUserLayoutNodeDescription folderDesc = alm.createNodeDescription(IUserLayoutNodeDescription.FOLDER);
		folderDesc.setName("Fragment column");
		return alm.addNode(folderDesc, getFragmentRootId(fragment.getId()), null).getId();
	}

	private String analyzeParameters( XSLT xslt ) throws PortalException {
		String fragmentId = CommonUtils.nvl(runtimeData.getParameter("uPcFM_selectedID"));
		String action = CommonUtils.nvl(runtimeData.getParameter("uPcFM_action"));
		
				if (action.equals("new")) {
					String fragmentName = runtimeData.getParameter("fragment_name");
					String funcName = runtimeData.getParameter("fragment_fname");
					String fragmentDesc = runtimeData.getParameter("fragment_desc");
					String fragmentType = runtimeData.getParameter("fragment_type");
					String fragmentFolder = runtimeData.getParameter("fragment_add_folder");
					boolean isPushedFragment = ("true".equals(fragmentType))?true:false;
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
					boolean isPushedFragment = ("true".equals(fragmentType))?true:false;
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
				} else if (action.equals("properties")) {
					 
				}
				
				xslt.setStylesheetParameter("uPcFM_selectedID",fragmentId);
			    xslt.setStylesheetParameter("uPcFM_action",action);	
		
		return fragmentId;
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
		Element category = document.createElement("category");
		category.setAttribute("name", "Fragments");
		category.setAttribute("expanded", "true");
		fragmentsNode.appendChild(category);
		if (fragments != null) {
			for ( Iterator ids = fragments.keySet().iterator(); ids.hasNext(); ) {
				String fragmentId = (String) ids.next();
				ALFragment fragment = (ALFragment) fragments.get(fragmentId);
				String fragmentRootId = getFragmentRootId(fragmentId);
				Element fragmentNode = document.createElement("fragment");
				category.appendChild(fragmentNode);
				Element id = document.createElement("ID");
				id.appendChild(document.createTextNode(fragmentId));
				fragmentNode.appendChild(id);
				Element rootId = document.createElement("rootNodeID");
				rootId.appendChild(document.createTextNode(fragmentRootId));
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
		}
		return document;
	}

	public void setStaticData(ChannelStaticData sd) throws PortalException {
		staticData = sd;
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
		
		String fragmentId = analyzeParameters(xslt);
		xslt.setXML(getFragmentList());
		
		xslt.setXSL(
			sslLocation,
			"fragmentManager",
			runtimeData.getBrowserInfo());
		xslt.setTarget(out);
		
		xslt.setStylesheetParameter(
			"baseActionURL",
			runtimeData.getBaseActionURL());
		xslt.transform();
	}

}