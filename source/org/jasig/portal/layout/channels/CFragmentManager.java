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

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IPrivileged;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.layout.IAggregatedUserLayoutStore;
import org.jasig.portal.layout.ILayoutFragment;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.ALFragment;
import org.jasig.portal.layout.ALFolder;
import org.jasig.portal.layout.IALFolderDescription;
import org.jasig.portal.layout.IUserLayoutChannelDescription;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.layout.IAggregatedUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutNodeDescription;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.XSLT;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import java.util.Map;
import java.util.Iterator;

/**
 * A channel for adding new content to a layout.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class CFragmentManager extends BaseChannel implements IPrivileged {

	private static final String sslLocation = "/org/jasig/portal/channels/CFragmentManager/CFragmentManager.ssl";
	private IUserLayoutManager ulm;
	private ThemeStylesheetUserPreferences themePrefs;
	private static IAggregatedUserLayoutStore layoutStore;
	private static Map fragmentMap;
	private static ALFragment fragments[];
	private String newName;

	public CFragmentManager() throws PortalException {
		super();
		if (layoutStore == null) {
			IUserLayoutStore layoutStoreImpl =
				UserLayoutStoreFactory.getUserLayoutStoreImpl();
			if (layoutStoreImpl == null
				|| !(layoutStoreImpl instanceof IAggregatedUserLayoutStore))
				throw new PortalException("CFragmentManager: The user layout store is NULL or must implement IAggregatedUserLayoutStore!");
			layoutStore = (IAggregatedUserLayoutStore) layoutStoreImpl;
		}

	}

	private void createFoundation() throws PortalException {
		IUserLayout layout = ulm.getUserLayout();
		IUserLayoutNodeDescription tabDesc =
			ulm.createNodeDescription(IUserLayoutNodeDescription.FOLDER);
		tabDesc.setName(newName != null ? newName : "Unnamed");
		String tabId = ulm.addNode(tabDesc, layout.getRootId(), null).getId();
		if (tabId != null) {
			IUserLayoutNodeDescription columnDesc =
				ulm.createNodeDescription(IUserLayoutNodeDescription.FOLDER);
			columnDesc.setName("Fragment column");
			String columnId = ulm.addNode(tabDesc, tabId, null).getId();
		}
	}

	private void analyzeParameters( String fragmentId ) throws Exception {
		String action = runtimeData.getParameter("uPcFM_action");
		if (action != null) {
			if (ulm instanceof TransientUserLayoutManagerWrapper)
				ulm = ((TransientUserLayoutManagerWrapper)ulm).getOriginalLayoutManager();
			if (ulm instanceof IAggregatedUserLayoutManager) {
				IAggregatedUserLayoutManager alm =
					(IAggregatedUserLayoutManager) ulm;
				if (action.equals("new")) {
					String fragmentName = runtimeData.getParameter("uP_fragment_name");
					String fragmentDesc = runtimeData.getParameter("uP_fragment_desc");
					String defaultValue = IAggregatedUserLayoutManager.NEW_FRAGMENT;
					fragmentId = alm.createFragment(CommonUtils.envl(fragmentName, defaultValue),
					                                CommonUtils.envl(fragmentDesc, "The fragment"));
				} else if (action.equals("edit") && fragmentId != null) {
					if (CommonUtils.parseInt(fragmentId) > 0)
						alm.loadFragment(fragmentId);
					else
						alm.loadUserLayout();
				} else if (action.equals("save")) {
					alm.saveFragment();
				} else if (action.equals("delete")) {
					alm.deleteFragment();
				}
				themePrefs.putParameterValue("currentFragmentID", CommonUtils.envl(fragmentId, "default_layout"));
			}
		}
	}

	/**
	 * Passes portal control structure to the channel.
	 * @see PortalControlStructures
	 */
	public void setPortalControlStructures(PortalControlStructures pcs)
		throws PortalException {
		ulm = pcs.getUserPreferencesManager().getUserLayoutManager();
		themePrefs = pcs.getUserPreferencesManager().getUserPreferences().getThemeStylesheetUserPreferences();
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
			for (int i = 0; i < fragments.length; i++) {
				ALFragment fragment = fragments[i];
				String fragmentId = fragment.getId();
				ALFolder rootFolder =
					(ALFolder) fragment.getLayoutData().get(
						fragment.getRootId());
				String fragmentRootId = rootFolder.getFirstChildNodeId();
				Element fragmentNode = document.createElement("fragment");
				category.appendChild(fragmentNode);
				Element id = document.createElement("ID");
				id.appendChild(document.createTextNode(fragmentId));
				fragmentNode.appendChild(id);
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
							? ((ALFolder) fragment
								.getLayoutData()
								.get(fragmentRootId))
								.getNodeDescription()
								.getName()
							: fragment.getFunctionalName()));
				fragmentNode.appendChild(name);
				Element desc = document.createElement("description");
				desc.appendChild(
					document.createTextNode(fragment.getDescription()));
				fragmentNode.appendChild(desc);
			}
		}
		return document;
	}

	public void setStaticData(ChannelStaticData sd) throws PortalException {
		staticData = sd;
		refreshFragments();
	}

	public void refreshFragments() throws PortalException {
		if (fragmentMap == null)
			fragmentMap = layoutStore.getFragments(staticData.getPerson());
		if (fragments == null) {
			fragments = new ALFragment[fragmentMap.size()];
			int i = 0;
			for (Iterator ids = fragmentMap.keySet().iterator();
				ids.hasNext();
				i++) {
				String fragmentId = (String) ids.next();
				ILayoutFragment layoutFragment =
					layoutStore.getFragment(staticData.getPerson(), fragmentId);
				if (layoutFragment == null
					|| !(layoutFragment instanceof ALFragment))
					throw new PortalException(
						"The fragment must be "
							+ ALFragment.class.getName()
							+ " type!");
				fragments[i] = (ALFragment) layoutFragment;
			}
		}
	}

	public void renderXML(ContentHandler out) throws PortalException {

		String fragmentId =
			CommonUtils.nvl(runtimeData.getParameter("uPcFM_selectedID"));

		XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
		xslt.setXML(getFragmentList());
		xslt.setXSL(
			sslLocation,
			"fragmentManager",
			runtimeData.getBrowserInfo());
		xslt.setTarget(out);
		/*if ( catId.length() > 0 )
		 xslt.setStylesheetParameter("catID", catId );*/
		xslt.setStylesheetParameter(
			"baseActionURL",
			runtimeData.getBaseActionURL());
		xslt.transform();
	}

}