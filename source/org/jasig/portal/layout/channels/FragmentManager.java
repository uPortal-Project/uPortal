/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package org.jasig.portal.layout.channels;

import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;


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
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


/**
 * An abstract class containing the basic business-logic and components for
 * CFragmentManager and CContentSubscriber
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public abstract class FragmentManager extends BaseChannel implements IPrivileged {

	protected IAggregatedUserLayoutManager alm;
	protected ThemeStylesheetUserPreferences themePrefs;
	protected Map fragments;
	
	
	protected String getFragmentRootId( String fragmentId ) throws PortalException {
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
		themePrefs = pcs.getUserPreferencesManager().getUserPreferences().getThemeStylesheetUserPreferences();	
	    IUserLayoutManager ulm = pcs.getUserPreferencesManager().getUserLayoutManager();
		if (ulm instanceof TransientUserLayoutManagerWrapper)
		  ulm = ((TransientUserLayoutManagerWrapper)ulm).getOriginalLayoutManager();
	    if (ulm instanceof IAggregatedUserLayoutManager)
		  alm = (IAggregatedUserLayoutManager) ulm;	
	}

    protected void getFragmentList ( Document document ) throws PortalException {
       getFragmentList(document,document);
    }
 
	protected void getFragmentList ( Document document, Node node ) throws PortalException {
		Element fragmentsNode = document.createElement("fragments");
		node.appendChild(fragmentsNode);
		Element category = document.createElement("category");
		category.setAttribute("name", "Fragments");
		category.setAttribute("view", "expanded");
		category.setAttribute("ID", "fragcat.1");
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
				fragmentNode.setAttribute("ID",fragmentId);
				category.appendChild(fragmentNode);
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
			  refreshFragmentMap();
		}
		
	}
	
	protected void refreshFragmentMap() throws PortalException {
		    Collection fragmentIds = getFragments();
		    fragments = new HashMap();
			for (Iterator ids = fragmentIds.iterator(); ids.hasNext(); ) {
					   String fragmentId = (String) ids.next();
					   ILayoutFragment layoutFragment = alm.getFragment(fragmentId);
					   if (layoutFragment == null || !(layoutFragment instanceof ALFragment))
						   throw new PortalException("The fragment must be "+ALFragment.class.getName()+" type!");
					   fragments.put(fragmentId,layoutFragment);
			}	
	}

	protected abstract Collection getFragments() throws PortalException;
	
	protected abstract void analyzeParameters( XSLT xslt ) throws PortalException;		


}