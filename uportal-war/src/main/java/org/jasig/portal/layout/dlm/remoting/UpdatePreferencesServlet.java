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

package org.jasig.portal.layout.dlm.remoting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserPreferencesManager;
import org.jasig.portal.fragment.subscribe.IUserFragmentSubscription;
import org.jasig.portal.fragment.subscribe.dao.IUserFragmentSubscriptionDao;
import org.jasig.portal.layout.IStylesheetUserPreferencesService;
import org.jasig.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.dlm.Constants;
import org.jasig.portal.layout.dlm.DistributedUserLayout;
import org.jasig.portal.layout.dlm.UserPrefsHandler;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.layout.node.UserLayoutFolderDescription;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.provider.RestrictedPerson;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Provides targets for AJAX preference setting calls.
 * 
 * @author jennifer.bourey@yale.edu
 * @version $Revision$ $Date$
 */
@Controller
@RequestMapping("/layout")
public class UpdatePreferencesServlet {

    private static final String TAB_GROUP_PARAMETER = "tabGroup";  // matches incoming JS
    private static final String TAB_GROUP_DEFAULT = "DEFAULT_TABGROUP";  // matches default in structure transform

    protected final Log log = LogFactory.getLog(getClass());
	
	private IPortletDefinitionRegistry portletDefinitionRegistry;
	private IUserIdentityStore userIdentityStore;
	private IUserFragmentSubscriptionDao userFragmentInfoDao;
	private IUserInstanceManager userInstanceManager;
	private IStylesheetUserPreferencesService stylesheetUserPreferencesService;
	private IUserLayoutStore userLayoutStore;

	@Autowired
    public void setUserLayoutStore(IUserLayoutStore userLayoutStore) {
        this.userLayoutStore = userLayoutStore;
    }

    @Autowired
	public void setStylesheetUserPreferencesService(IStylesheetUserPreferencesService stylesheetUserPreferencesService) {
        this.stylesheetUserPreferencesService = stylesheetUserPreferencesService;
    }

    @Autowired
	public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
	    this.portletDefinitionRegistry = portletDefinitionRegistry;
	}

    @Autowired
    public void setUserIdentityStore(IUserIdentityStore userStore) {
        this.userIdentityStore = userStore;
    }
    
    @Autowired
    public void setUserFragmentInfoDao(IUserFragmentSubscriptionDao userFragmentInfoDao) {
        this.userFragmentInfoDao = userFragmentInfoDao;
    }
    
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

	// default tab name
	protected static final String DEFAULT_TAB_NAME = "New Tab";

	/**
	 * Remove an element from the layout.
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
    @RequestMapping(method = RequestMethod.POST, params = "action=removeElement")
    public ModelAndView removeElement(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);
        IPerson per = getPerson(ui, response);

        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        try {
            
            // if the element ID starts with the fragment prefix and is a folder, 
            // attempt first to treat it as a pulled fragment subscription
            String elementId = request.getParameter("elementID");
            if (elementId != null && elementId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX) && 
                    ulm.getNode( elementId ) instanceof org.jasig.portal.layout.node.UserLayoutFolderDescription) {
                
                removeSubscription(per, elementId, ulm);
                
            } else {
                // Delete the requested element node.  This code is the same for 
                // all node types, so we can just have a generic action.
               ulm.deleteNode(elementId);
            }

            ulm.saveUserLayout();

            return new ModelAndView("jsonView", Collections.EMPTY_MAP);
            
        } catch (Exception e) {
            log.warn("Failed to remove element from layout", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

    /**
     * Subscribe a user to a pre-formatted tab (pulled DLM fragment).
     * 
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=subscribeToTab")
    public ModelAndView subscribeToTab(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);
        IPerson per = getPerson(ui, response);

        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        // Get the fragment owner's name from the request and construct 
        // an IPerson object representing that user
        String fragmentOwnerName = request.getParameter("sourceID");
        if (StringUtils.isBlank(fragmentOwnerName)) {
            log.warn("Attempted to subscribe to tab with null owner ID");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        RestrictedPerson fragmentOwner = PersonFactory.createRestrictedPerson();
        fragmentOwner.setUserName(fragmentOwnerName);

        // Mark the currently-authenticated user as subscribed to this fragment.
        // If an inactivated fragment registration already exists, update it
        // as an active subscription.  Otherwise, create a new fragment
        // subscription.
        IUserFragmentSubscription userFragmentInfo = userFragmentInfoDao
            .getUserFragmentInfo(per, fragmentOwner);
        if (userFragmentInfo == null) {
            userFragmentInfo = userFragmentInfoDao.createUserFragmentInfo(per,
                    fragmentOwner);
        } else {
            userFragmentInfo.setActive(true);
            userFragmentInfoDao.updateUserFragmentInfo(userFragmentInfo);
        }
        
        try {
            // reload user layout and stylesheet to incorporate new DLM fragment
            ulm.loadUserLayout(true);

            // get the target node this new tab should be moved after
            String destinationId = request.getParameter("elementID");

            // get the user layout for the currently-authenticated user
            int uid = userIdentityStore.getPortalUID(fragmentOwner, false);
            final DistributedUserLayout userLayout = userLayoutStore.getUserLayout(per, upm.getUserProfile());
            Document layoutDocument = userLayout.getLayout();

            // attempt to find the new subscribed tab in the layout so we can
            // move it
            StringBuilder expression = new StringBuilder("//folder[@type='root']/folder[starts-with(@ID,'")
                                       .append(Constants.FRAGMENT_ID_USER_PREFIX)
                                       .append(uid)
                                       .append("')]");
            XPathFactory fac = XPathFactory.newInstance();
            XPath xpath = fac.newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(expression.toString(), layoutDocument,  XPathConstants.NODESET);
            String sourceId = nodes.item(0).getAttributes().getNamedItem("ID").getTextContent();
            ulm.moveNode(sourceId, ulm.getParentId(destinationId), destinationId);

            ulm.saveUserLayout();

            return new ModelAndView("jsonView", Collections.singletonMap("tabId", sourceId));
            
        } catch (Exception e) {
            log.warn("Error subscribing to fragment owned by "
                    + fragmentOwnerName, e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

    }
    
	/**
	 * Move a portlet to another location on the tab.
	 * 
	 * @param ulm
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws PortalException
	 */
    @RequestMapping(method = RequestMethod.POST, params = "action=movePortlet")
	public ModelAndView movePortlet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, PortalException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);
        IPerson per = getPerson(ui, response);

        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

		// portlet to be moved
		String sourceId = request.getParameter("sourceID");

		// Either "insertBefore" or "appendAfter".
		String method = request.getParameter("method");

		// Target element to move the source element in front of.  This parameter
		// isn't actually relevant if we're appending the source element.
		String destinationId = request.getParameter("elementID");

		
		if (isTab(ulm, destinationId)) {
			// if the target is a tab type node, move the portlet to 
			// the end of the first column
		    @SuppressWarnings("unchecked")
			Enumeration<String> columns = ulm.getChildIds(destinationId);
			if (columns.hasMoreElements()) {
				ulm.moveNode(sourceId, columns.nextElement(), null);
			} else {

				IUserLayoutFolderDescription newColumn = new UserLayoutFolderDescription();
				newColumn.setName("Column");
				newColumn.setId("tbd");
				newColumn
						.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
				newColumn.setHidden(false);
				newColumn.setUnremovable(false);
				newColumn.setImmutable(false);

				// add the column to our layout
				IUserLayoutNodeDescription col = ulm.addNode(newColumn,
						destinationId, null);

				// move the channel
				ulm.moveNode(sourceId, col.getId(), null);
			}

		} else if (ulm.getRootFolderId().equals(
			// if the target is a column type node, we need to just move the portlet
			// to the end of the column
			ulm.getParentId(ulm.getParentId(destinationId)))) {
			ulm.moveNode(sourceId, destinationId, null);

		} else {
			// If we're moving this element before another one, we need
			// to know what the target is. If there's no target, just
			// assume we're moving it to the very end of the column.
			String siblingId = null;
			if (method.equals("insertBefore"))
				siblingId = destinationId;

			// move the node as requested and save the layout
			ulm.moveNode(sourceId, ulm.getParentId(destinationId), siblingId);
		}

		try {
			// save the user's layout
            ulm.saveUserLayout();
		} catch (Exception e) {
			log.warn("Error saving layout", e);
		}

        return new ModelAndView("jsonView", Collections.EMPTY_MAP);

	}
	
	/**
	 * Change the number of columns on a specified tab.  In the event that the user is
	 * decreasing the number of columns, extra columns will be stripped from the 
	 * right-hand side.  Any channels in these columns will be moved to the bottom of
	 * the last preserved column.
	 * 
	 * @param per
	 * @param upm
	 * @param ulm
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws PortalException
	 */
    @RequestMapping(method = RequestMethod.POST, params = "action=changeColumns")
	public ModelAndView changeColumns(HttpServletRequest request,
			HttpServletResponse response, @RequestParam("tabId") String tabId, 
			@RequestParam("widths[]") String[] widths, 
			@RequestParam(value = "deleted[]", required = false) String[] deleted, 
			@RequestParam(value = "acceptor", required = false) String acceptor) throws IOException, PortalException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);
        IPerson per = getPerson(ui, response);

        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

		int newColumnCount = widths.length;
        
		// build a list of the current columns for this tab
        @SuppressWarnings("unchecked")
		Enumeration<String> columns = ulm.getChildIds(tabId);
		List<String> columnList = new ArrayList<String>();
		while (columns.hasMoreElements()) {
			columnList.add(columns.nextElement());
		}
        int oldColumnCount = columnList.size();
        
        Map<String, Object> model = new HashMap<String, Object>();

        // if the new layout has more columns 
		if (newColumnCount > oldColumnCount) {
	        List<String> newColumnIds = new ArrayList<String>();
			for (int i = columnList.size(); i < newColumnCount; i++) {

				// create new column element
				IUserLayoutFolderDescription newColumn = new UserLayoutFolderDescription();
				newColumn.setName("Column");
				newColumn.setId("tbd");
				newColumn
						.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
				newColumn.setHidden(false);
				newColumn.setUnremovable(false);
				newColumn.setImmutable(false);

				// add the column to our layout
				IUserLayoutNodeDescription node = ulm.addNode(newColumn, tabId,
						null);
				newColumnIds.add(node.getId());
				
	            model.put("newColumnIds", newColumnIds);
				columnList.add(node.getId());

			}

		} 
		
		// if the new layout has fewer columns
		else if (deleted != null && deleted.length > 0) {
		    
	        if (columnList.size() != widths.length + deleted.length) {
	            // TODO: error?
	        }
	        
		    for (String columnId : deleted) {
		        
                // move all channels in the current column to the last valid column
                @SuppressWarnings("unchecked")
                Enumeration channels = ulm.getChildIds(columnId);
                while (channels.hasMoreElements()) {
                    ulm.addNode(ulm.getNode((String) channels.nextElement()),
                            acceptor, null);
                }

                // delete the column from the user's layout
                ulm.deleteNode(columnId); 
                
                columnList.remove(columnId);
            }
		}

		int count = 0;
		for (String columnId : columnList) {
		    this.stylesheetUserPreferencesService.setLayoutAttribute(request, PreferencesScope.STRUCTURE, columnId, "width", widths[count] + "%");
			try {
				// This sets the column attribute in memory but doesn't persist it.  Comment says saves changes "prior to persisting"
	            Element folder = ulm.getUserLayoutDOM().getElementById(columnId);
				UserPrefsHandler.setUserPreference(folder, "width", per);
			} catch (Exception e) {
				log.error("Error saving new column widths", e);
			}
			count++;
		}
		
		try {
		    ulm.saveUserLayout();
		} catch (Exception e) {
			log.warn("Error saving layout", e);
		}

        return new ModelAndView("jsonView", model);

	}

	/**
	 * Move a tab left or right.
	 * 
	 * @param per
	 * @param upm
	 * @param ulm
	 * @param request
	 * @param response
	 * @throws PortalException
	 * @throws IOException
	 */
    @RequestMapping(method = RequestMethod.POST, params = "action=moveTab")
	public ModelAndView moveTab(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);

        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

		// gather the parameters we need to move a channel
		String destinationId = request.getParameter("elementID");
		String sourceId = request.getParameter("sourceID");
		String method = request.getParameter("method");

		// If we're moving this element before another one, we need
		// to know what the target is. If there's no target, just
		// assume we're moving it to the very end of the list.
		String siblingId = null;
		if (method.equals("insertBefore"))
			siblingId = destinationId;

		// move the node as requested and save the layout
		ulm.moveNode(sourceId, ulm.getParentId(destinationId), siblingId);

		try {
            ulm.saveUserLayout();
		} catch (Exception e) {
			log.warn("Failed to move tab in user layout", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		return new ModelAndView("jsonView", Collections.EMPTY_MAP);

	}

	/**
	 * Add a new channel.
	 * 
	 * @param per
	 * @param upm
	 * @param ulm
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws PortalException
	 */
    @RequestMapping(method = RequestMethod.POST, params = "action=addPortlet")
	public ModelAndView addPortlet(HttpServletRequest request, HttpServletResponse response) throws IOException, PortalException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);

        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

		// gather the parameters we need to move a channel
		String destinationId = request.getParameter("elementID");
		String sourceId = request.getParameter("channelID");
		String method = request.getParameter("position");

		IPortletDefinition definition = portletDefinitionRegistry.getPortletDefinition(sourceId);
		
        IUserLayoutChannelDescription channel = new UserLayoutChannelDescription(definition);

		IUserLayoutNodeDescription node = null;
		if (isTab(ulm, destinationId)) {
            @SuppressWarnings("unchecked")
			Enumeration<String> columns = ulm.getChildIds(destinationId);
			if (columns.hasMoreElements()) {
				while (columns.hasMoreElements()) {
					// attempt to add this channel to the column
					node = ulm.addNode(channel, columns.nextElement(), null);
					// if it couldn't be added to this column, go on and try the next
					// one.  otherwise, we're set.
					if (node != null)
						break;
				}
			} else {

				IUserLayoutFolderDescription newColumn = new UserLayoutFolderDescription();
				newColumn.setName("Column");
				newColumn.setId("tbd");
				newColumn
						.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
				newColumn.setHidden(false);
				newColumn.setUnremovable(false);
				newColumn.setImmutable(false);

				// add the column to our layout
				IUserLayoutNodeDescription col = ulm.addNode(newColumn,
						destinationId, null);

				// add the channel
				node = ulm.addNode(channel, col.getId(), null);
			}

		} else if (isColumn(ulm, destinationId)) {
			// move the channel into the column
			node = ulm.addNode(channel, destinationId, null);
		} else {
			// If we're moving this element before another one, we need
			// to know what the target is. If there's no target, just
			// assume we're moving it to the very end of the column.
			String siblingId = null;
			if (method.equals("insertBefore"))
				siblingId = destinationId;

			// move the node as requested and save the layout
			node = ulm.addNode(channel, ulm.getParentId(destinationId),
					siblingId);
		}

		String nodeId = node.getId();

		try {
			// save the user's layout
            ulm.saveUserLayout();
		} catch (Exception e) {
			log.warn("Error saving layout", e);
		}

		Map<String, String> model = new HashMap<String, String>();
		model.put("response", "Added new channel");
		model.put("newNodeId", nodeId);
		return new ModelAndView("jsonView", model);

	}

	/**
	 * Update the user's preferred skin.
	 * 
	 * @param per
	 * @param upm
	 * @param ulm
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws PortalException
	 */
    @RequestMapping(method = RequestMethod.POST, params="action=chooseSkin")
	public ModelAndView chooseSkin(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String skinName = request.getParameter("skinName");
		this.stylesheetUserPreferencesService.setStylesheetParameter(request, PreferencesScope.THEME, "skin", skinName);
		
		return new ModelAndView("jsonView", Collections.EMPTY_MAP);
	}


	/**
	 * Add a new tab to the layout.  The new tab will be appended to the end of the
	 * list and named with the BLANK_TAB_NAME variable.
	 * 
	 * @param request
	 * @throws IOException 
	 */
    @RequestMapping(method = RequestMethod.POST, params="action=addTab")
	public ModelAndView addTab(HttpServletRequest request, HttpServletResponse response, @RequestParam("widths[]") String[] widths) throws IOException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);
        IPerson per = getPerson(ui, response);
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

		// construct a brand new tab
		String id = "tbd";
        String tabName = request.getParameter("tabName");
        if (StringUtils.isBlank(tabName)) tabName = DEFAULT_TAB_NAME;
		IUserLayoutFolderDescription newTab = new UserLayoutFolderDescription();
		newTab.setName(tabName);
		newTab.setId(id);
		newTab.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
		newTab.setHidden(false);
		newTab.setUnremovable(false);
		newTab.setImmutable(false);

		// add the tab to the layout
		ulm.addNode(newTab, ulm.getRootFolderId(), null);

		try {
			// save the user's layout
            ulm.saveUserLayout();
		} catch (Exception e) {
			log.warn("Error saving layout", e);
		}

		// get the id of the newly added tab
		String tabId = newTab.getId();

        for (String width : widths) {

            // create new column element
            IUserLayoutFolderDescription newColumn = new UserLayoutFolderDescription();
            newColumn.setName("Column");
            newColumn.setId("tbd");
            newColumn
                    .setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
            newColumn.setHidden(false);
            newColumn.setUnremovable(false);
            newColumn.setImmutable(false);

            // add the column to our layout
            ulm.addNode(newColumn, tabId, null);
            
            this.stylesheetUserPreferencesService.setLayoutAttribute(request, PreferencesScope.STRUCTURE, newColumn.getId(), "width", width + "%");
            try {
                // This sets the column attribute in memory but doesn't persist it.  Comment says saves changes "prior to persisting"
                Element folder = ulm.getUserLayoutDOM().getElementById(newColumn.getId());
                UserPrefsHandler.setUserPreference(folder, "width", per);
            } catch (Exception e) {
                log.error("Error saving new column widths", e);
            }

        }

        // ## 'tabGroup' value (optional feature)
        // Set the 'tabGroup' attribute on the folder element that describes 
        // this new tab;  use the currently active tabGroup.
        if (request.getParameter(TAB_GROUP_PARAMETER)!= null) {

            String tabGroup = request.getParameter(TAB_GROUP_PARAMETER).trim();
            if (log.isDebugEnabled()) {
                log.debug(TAB_GROUP_PARAMETER + "=" + tabGroup);
            }

            if (!TAB_GROUP_DEFAULT.equals(tabGroup) && tabGroup.length() != 0) {
                // Persists SSUP values to the database
                this.stylesheetUserPreferencesService.setLayoutAttribute(request, PreferencesScope.STRUCTURE, tabId, TAB_GROUP_PARAMETER , tabGroup);
            }

        }

        try {
            // save the user's layout
            ulm.saveUserLayout();
        } catch (Exception e) {
            log.warn("Error saving layout", e);
        }

		return new ModelAndView("jsonView", Collections.singletonMap("tabId", tabId));
	}

	/**
	 * Rename a specified tab.
	 * 
	 * @param request
	 * @throws IOException 
	 */
    @RequestMapping(method = RequestMethod.POST, params = "action=renameTab")
	public ModelAndView renameTab(HttpServletRequest request, HttpServletResponse response) throws IOException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);
        IPerson per = getPerson(ui, response);
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

		// element ID of the tab to be renamed
		String tabId = request.getParameter("tabId");
        IUserLayoutFolderDescription tab = (IUserLayoutFolderDescription) ulm
            .getNode(tabId);

		// desired new name
		String tabName = request.getParameter("tabName");

		if (!ulm.canUpdateNode(tab)) {
		    log.warn("Attempting to rename an immutable tab");
		    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		    return null;
		}
		
		/*
		 * Update the tab and save the layout
		 */
	    tab.setName(StringUtils.isBlank(tabName) ? DEFAULT_TAB_NAME : tabName);
		final boolean updated = ulm.updateNode(tab);
		
		if (updated) {
    		try {
    			// save the user's layout
                ulm.saveUserLayout();
    		} catch (Exception e) {
    			log.warn("Error saving layout", e);
    		}
    
    		//TODO why do we have to do this, shouldn't modifying the layout be enough to trigger a full re-render (layout's cache key changes)
    		this.stylesheetUserPreferencesService.setLayoutAttribute(request, PreferencesScope.STRUCTURE, tabId, "name", tabName);
		}

        Map<String, String> model = Collections.singletonMap("message", "saved new tab name");
        return new ModelAndView("jsonView", model);

	}

    @RequestMapping(method = RequestMethod.POST, params = "action=updatePermissions")
    public ModelAndView updatePermissions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        IUserInstance ui = userInstanceManager.getUserInstance(request);
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        String elementId = request.getParameter("elementID");
        IUserLayoutNodeDescription node = ulm.getNode(elementId);
        
        if (node == null){
            log.warn("Failed to locate node for permissions update");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        
        String deletable = request.getParameter("deletable");
        if (!StringUtils.isBlank(deletable)) {
            node.setDeleteAllowed(Boolean.valueOf(deletable));
        }

        String movable = request.getParameter("movable");
        if (!StringUtils.isBlank(movable)) {
            node.setMoveAllowed(Boolean.valueOf(movable));
        }

        String editable = request.getParameter("editable");
        if (!StringUtils.isBlank(editable)) {
            node.setEditAllowed(Boolean.valueOf(editable));
        }
        
        String canAddChildren = request.getParameter("addChildAllowed");
        if (!StringUtils.isBlank(canAddChildren)) {
            node.setAddChildAllowed(Boolean.valueOf(canAddChildren));
        }
        
        ulm.updateNode(node);
        
        try {
            // save the user's layout
            ulm.saveUserLayout();
        } catch (Exception e) {
            log.warn("Error saving layout", e);
        }

        return new ModelAndView("jsonView", Collections.EMPTY_MAP);

    }
    
    protected void removeSubscription(IPerson per, String elementId, IUserLayoutManager ulm) {
        
        // get the fragment owner's ID from the element string
        String userIdString = StringUtils.substringBetween(elementId, Constants.FRAGMENT_ID_USER_PREFIX, Constants.FRAGMENT_ID_LAYOUT_PREFIX);
        int userId = NumberUtils.toInt(userIdString,0);
        
        // construct a new person object representing the fragment owner
        RestrictedPerson fragmentOwner = PersonFactory.createRestrictedPerson();
        fragmentOwner.setID(userId);
        fragmentOwner.setUserName(userIdentityStore.getPortalUserName(userId));
        
        // attempt to find a subscription for this fragment
        IUserFragmentSubscription subscription = userFragmentInfoDao.getUserFragmentInfo(per, fragmentOwner);
        
        // if a subscription was found, remove it's registration
        if (subscription != null) {
            userFragmentInfoDao.deleteUserFragmentInfo(subscription);
            ulm.loadUserLayout(true);
        } 
        
        // otherwise, delete the node
        else {
            ulm.deleteNode(elementId);
        }
    
    }
	
	/**
	 * A folder is a tab if its parent element is the layout element
	 * 
	 * @param folder the folder in question
	 * @return <code>true</code> if the folder is a tab, otherwise <code>false</code>
	 */
	protected boolean isTab(IUserLayoutManager ulm, String folderId)
			throws PortalException {
		// we could be a bit more careful here and actually check the type
		return ulm.getRootFolderId().equals(ulm.getParentId(folderId));
	}
	
	protected IPerson getPerson(IUserInstance ui, HttpServletResponse response) throws IOException {
        IPerson per = ui.getPerson();
        if (per.isGuest()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        
        return per;

	}

	/**
	 * A folder is a column if its parent is a tab element
	 * 
	 * @param folder the folder in question
	 * @return <code>true</code> if the folder is a column, otherwise <code>false</code>
	 */
	protected boolean isColumn(IUserLayoutManager ulm, String folderId)
			throws PortalException {
		return isTab(ulm, ulm.getParentId(folderId));
	}
}
