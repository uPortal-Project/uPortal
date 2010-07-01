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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserIdentityStoreFactory;
import org.jasig.portal.UserPreferencesManager;
import org.jasig.portal.UserProfile;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.fragment.subscribe.IUserFragmentSubscription;
import org.jasig.portal.fragment.subscribe.dao.IUserFragmentSubscriptionDao;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.layout.dlm.Constants;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.UserPrefsHandler;
import org.jasig.portal.layout.dlm.providers.AttributeEvaluator;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.layout.node.UserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.provider.RestrictedPerson;
import org.jasig.portal.url.PortalHttpServletRequestWrapper;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
public class UpdatePreferencesServlet implements InitializingBean {

	protected final Log log = LogFactory.getLog(getClass());

	private IUserLayoutStore ulStore = UserLayoutStoreFactory
			.getUserLayoutStoreImpl();

    private IUserIdentityStore userStore;

    @Autowired(required = true)
    public void setUserIdentityStore(IUserIdentityStore userStore) {
        this.userStore = userStore;
    }
    
    private IUserFragmentSubscriptionDao userFragmentInfoDao;
    
    @Autowired(required = true)
    public void setUserFragmentInfoDao(IUserFragmentSubscriptionDao userFragmentInfoDao) {
        this.userFragmentInfoDao = userFragmentInfoDao;
    }
    
    private IUserInstanceManager userInstanceManager;
    
    @Autowired(required = true)
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

	// default tab name
	protected final static String BLANK_TAB_NAME = "New Tab";
	protected final static String ACTIVE_TAB_PARAM = "activeTab";

    @RequestMapping(method = RequestMethod.POST)
	public void updatePreferences(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	    IUserInstance ui = null;
		IPerson per = null;
		UserPreferencesManager upm = null;
		IUserLayoutManager ulm = null;

		// make sure the user has a current session
		if (request.getSession(false) == null) {
			log.warn("Attempting to use AJAX preferences as GUEST user");
			printError(response, "Your session has timed out.  Please log in again to make changes to your layout.");
			return;
		}

		try {
            
			// Retrieve the user's UserInstance object
			ui = userInstanceManager.getUserInstance(request);

			// Retrieve the user's IPerson object
			per = ui.getPerson();

			// ensure that the user is currently logged in
			if (per.isGuest()) {
				log.warn("Attempting to use AJAX preferences as GUEST user");
				printError(response, "Your session has timed out.  Please log in again to make changes to your layout.");
				return;
			}

			// Retrieve the preferences manager
			upm = (UserPreferencesManager) ui.getPreferencesManager();

			// Retrieve the layout manager
			ulm = upm.getUserLayoutManager();

		} catch (RuntimeException e) {
			log.error(e, e);
			printError(response, "An unknown error occurred.");
			return;
		}

		try {

			// get the requested preferences action
			String action = request.getParameter("action");

			// perform the requested action
			if (action == null) {

				log.warn("preferences servlet called with no action parameter");

			} else if (action.equals("movePortletHere")) {

				moveChannel(per, upm, ulm, request, response);

			} else if (action.equals("changeColumns")) {

				changeColumns(per, upm, ulm, request, response);

			} else if (action.equals("updateColumnWidths")) {

				updateColumnWidths(per, upm, ulm, request, response);

			} else if (action.equals("addChannel")) {

				addChannel(per, upm, ulm, userInstanceManager, request, response);

			} else if (action.equals("renameTab")) {

				renameTab(per, upm, ulm, request, response);

			} else if (action.equals("addTab")) {

				addTab(per, upm, ulm, request, response);

			} else if (action.equals("moveTabHere")) {

				moveTab(per, upm, ulm, request, response);

			} else if (action.equals("chooseSkin")) {
				
				chooseSkin(per, upm, ulm, request, response);

            } else if (action.equals("subscribeToTabs")) {
                subscribeToTab(request, response, per, upm, ulm,
                        userFragmentInfoDao);

			} else if (action.equals("removeElement")) {
			    removeElement(request, response, per, upm, ulm);
			}

		} catch (RuntimeException e) {
			log.error(e, e);
			printError(response, "An unknown error occurred.");
			return;
		}

	}

    private void removeElement(HttpServletRequest request,
            HttpServletResponse response, IPerson per,
            UserPreferencesManager upm, IUserLayoutManager ulm)
            throws IOException {
        
        // if the element ID starts with the fragment prefix and is a folder, 
        // attempt first to treat it as a pulled fragment subscription
        String elementId = request.getParameter("elementID");
        if (elementId != null && elementId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX) && 
                ulm.getNode( elementId ) instanceof org.jasig.portal.layout.node.UserLayoutFolderDescription) {
            
            // get the fragment owner's ID from the element string
            String userIdString = StringUtils.substringBetween(elementId, Constants.FRAGMENT_ID_USER_PREFIX, Constants.FRAGMENT_ID_LAYOUT_PREFIX);
            int userId = NumberUtils.toInt(userIdString,0);
            
            // construct a new person object reqpresenting the fragment owner
            RestrictedPerson fragmentOwner = PersonFactory.createRestrictedPerson();
            fragmentOwner.setID(userId);
            fragmentOwner.setUserName(userStore.getPortalUserName(userId));
            
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
            
        } else {
            // Delete the requested element node.  This code is the same for 
            // all node types, so we can just have a generic action.
           ulm.deleteNode(elementId);
        }
        try {
            saveUserLayoutPreservingTab(ulm, upm, per);
        } catch (Exception e) {
            log.warn("Error saving layout", e);
        }

        printSuccess(response, "Removed element", null);
    }
	   /**
     * sets the fragment owner user name, and inactivates the subscription in the
     * database and removes it from the in-memory person attributes
     * @param per the logged in user
     * @param subscriptionAttributes fragment owner subscription attributes
     * @param fragmentOwner the fragment owner
     * @param userName the fragment owner username
     */
    private void removeSubscription(IPerson per,
            List<Object> subscriptionAttributes,
            RestrictedPerson fragmentOwner, String userName) {
        fragmentOwner.setUserName(userName);
        updateSubciptionInfo(per, userFragmentInfoDao, fragmentOwner,false);    
        subscriptionAttributes.remove(fragmentOwner.getUserName());
        per.setAttribute("fragmentOwner", subscriptionAttributes);
    }


    /**
     * Subscribes a user to a pre-formatted tab. The steps are:
     * 1.  Tests to see whether the sourceID is already applicable to the person.  If true, no further action is required
     *     since the a fragment may only be subscribed to once
     * 2.  Adds the attribute to the person "fragmentOwner" attribute list 
     * 3.  Adds the new attribute to the user's fragmentInfo if it is new, or updates the active flag if it already exists for the user in the database
     * 4.  Forces a reload of the user preferences so that the new fragment will be pushed when the page refreshes
     * 5.  Moves the new tab to the last tab position for the user and sets it to the selected tab position
     * @param request the http servlet request must contain parameter "sourceID" with the value of a fragment owner name
     * @param response the http servlet response
     * @param per the logged in person
     * @param upm the user preferences manager
     * @param ulm the user layout manager
     * @param userFragmentInfoDao
     */
    @SuppressWarnings("unchecked")
    private void subscribeToTab(HttpServletRequest request,
            HttpServletResponse response, IPerson per,
            UserPreferencesManager upm, IUserLayoutManager ulm,
            IUserFragmentSubscriptionDao userFragmentInfoDao) {
        String subscriptionAttr = request.getParameter("sourceID");
        if (StringUtils.isNotBlank(subscriptionAttr)) { 
                List<Object> subscriptionAttributes = getAttributeList(per);
                subscriptionAttributes.add(subscriptionAttr);
                per.setAttribute("fragmentOwner", subscriptionAttributes);

            
                RestrictedPerson fragmentOwner = PersonFactory
                        .createRestrictedPerson();
                fragmentOwner.setUserName(subscriptionAttr);
                updateSubciptionInfo(per, userFragmentInfoDao, fragmentOwner,true);
                UserProfile currentProfile = upm.getUserPreferences()
                        .getProfile();
                int profileID = currentProfile.getProfileId();
                int structID = currentProfile.getStructureStylesheetId();
                // get the active tab number from the store so that we can
                // preserve it
                StructureStylesheetUserPreferences ssup = null;
                try {
                    ssup = ulStore.getStructureStylesheetUserPreferences(per,
                            profileID, structID);
                    StructureStylesheetUserPreferences origSsup = upm
                            .getUserPreferences()
                            .getStructureStylesheetUserPreferences();

                    String currentTab = ssup
                            .getParameterValue(ACTIVE_TAB_PARAM);

                    ssup.putParameterValue(ACTIVE_TAB_PARAM, currentTab);
                    upm.getUserPreferences()
                            .setStructureStylesheetUserPreferences(ssup);
                    ulm.loadUserLayout(true);

                    moveSubscribedTab(per, upm, ulm, fragmentOwner, request,
                            response);
                } catch (Exception e) {
                    log.warn("Error subscribing to fragment owned by "
                            + subscriptionAttr, e);
                }

        }
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
    private void moveSubscribedTab(IPerson per, UserPreferencesManager upm,
            IUserLayoutManager ulm, IPerson fragmentOwner, HttpServletRequest request,
            HttpServletResponse response) throws PortalException, IOException, Exception {

        // gather the parameters we need to move a channel
        String destinationId = request.getParameter("elementID");
        String subscriptionAttr = request.getParameter("sourceID");
        String method = request.getParameter("method");
        String tabPosition = request.getParameter("tabPosition");
        int uid = userStore.getPortalUID(fragmentOwner, false);
        Document userLayout = ulStore.getUserLayout(per, upm.getUserPreferences().getProfile());
        NodeList  nodes = null;
        try
        {
                StringBuilder expression = new StringBuilder("//folder[@type='root']/folder[starts-with(@ID,'")
                                           .append(Constants.FRAGMENT_ID_USER_PREFIX)
                                           .append(uid)
                                           .append("')]");
                XPathFactory fac = XPathFactory.newInstance();
                XPath xpath = fac.newXPath();
                nodes = (NodeList) xpath.evaluate(expression.toString(), userLayout,  XPathConstants.NODESET);
                // If we're moving this element before another one, we need
                // to know what the target is. If there's no target, just
                // assume we're moving it to the very end of the list.
                String siblingId = null;
                if (method.equals("insertBefore"))
                    siblingId = destinationId;

                // move the node as requested and save the layout
                for (int i = 0; i < nodes.getLength(); i++) {
                    String sourceId = nodes.item(i).getAttributes().getNamedItem("ID").getTextContent();
                    ulm.moveNode(sourceId, ulm.getParentId(destinationId), siblingId);
                }

                    StructureStylesheetUserPreferences ssup = upm.getUserPreferences()
                            .getStructureStylesheetUserPreferences();

                    try {
                        String currentTab = ssup.getParameterValue(ACTIVE_TAB_PARAM);
                        UserProfile currentProfile = upm.getUserPreferences().getProfile();
                        int profileID = currentProfile.getProfileId();
                        int structID = currentProfile.getStructureStylesheetId();
                        // get the active tab number from the store so that we can preserve it
                        String defaultTab = ulStore.getStructureStylesheetUserPreferences(per, profileID, structID).getParameterValue(ACTIVE_TAB_PARAM);
                        // set the active tab to previously recorded value
                        if (defaultTab.equals(currentTab)) {
                            ssup.putParameterValue(ACTIVE_TAB_PARAM, tabPosition);
                        }
                        else {
                            ssup.putParameterValue(ACTIVE_TAB_PARAM, defaultTab);
                        }
                        // This is a brute force save of the new attributes.  It requires access to the layout store. -SAB
                        ulStore.setStructureStylesheetUserPreferences(per, profileID, ssup);
                    } catch (Exception e) {
                        log.error(e);
                    }

                    ulm.saveUserLayout();

                    // reset the active tab for viewing (not default)
                    ssup.putParameterValue(ACTIVE_TAB_PARAM, tabPosition);

                    printSuccess(response, "Saved new tab position", null);

                
        } catch (XPathExpressionException e) {
            log.error("Error searching for fragments owned by "+ subscriptionAttr, e);
        }


        
    }
    
    /**
     * Update the user fragment info. If the record does not exist in the database
     * it will be created, if the active flag is set to true.  If the record
     * already exists in the database, the active flag will be updated.
     * @param per
     * @param userFragmentInfoDao
     * @param fragmentOwner
     * @param activeFlag
     */
    private void updateSubciptionInfo(IPerson per,
            IUserFragmentSubscriptionDao userFragmentInfoDao,
            RestrictedPerson fragmentOwner, boolean activeFlag) {
        IUserFragmentSubscription userFragmentInfo = userFragmentInfoDao
                .getUserFragmentInfo(per, fragmentOwner);
        if (userFragmentInfo == null) {
            if (activeFlag) {
                userFragmentInfo = userFragmentInfoDao.createUserFragmentInfo(per, fragmentOwner);
            }
        } else {
            userFragmentInfo.setActive(activeFlag);
            userFragmentInfoDao
                    .updateUserFragmentInfo(userFragmentInfo);
        }
    }


    private List<Object> getAttributeList(IPerson per) {
        Object[] subscriptionAttributeArray = per.getAttributeValues("fragmentOwner");
        List<Object> subscriptionAttributes = new ArrayList();
        if (subscriptionAttributeArray !=  null) {
            for (Object attr : subscriptionAttributeArray ){
                subscriptionAttributes.add(attr);
            }
        }
        return subscriptionAttributes;
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
	private void moveChannel(IPerson per, UserPreferencesManager upm, IUserLayoutManager ulm,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, PortalException {

		// portlet to be moved
		String sourceId = request.getParameter("sourceID");

		// Either "insertBefore" or "appendAfter".
		String method = request.getParameter("method");

		// Target element to move the source element in front of.  This parameter
		// isn't actually relevant if we're appending the source element.
		String destinationId = request.getParameter("elementID");

		
		IUserLayoutNodeDescription node = null;
		if (isTab(ulm, destinationId)) {
			// if the target is a tab type node, move the portlet to 
			// the end of the first column
			Enumeration columns = ulm.getChildIds(destinationId);
			if (columns.hasMoreElements()) {
				ulm.moveNode(sourceId, (String) columns.nextElement(), null);
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
			saveUserLayoutPreservingTab(ulm, upm, per);
		} catch (Exception e) {
			log.warn("Error saving layout", e);
		}

		printSuccess(response, "Saved new channel location", null);

	}
	
	/**
	 * Change the number of columns on a specified tab.  In the event that the user is
	 * decresasing the number of columns, extra columns will be stripped from the 
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
	private void changeColumns(IPerson per, UserPreferencesManager upm,
			IUserLayoutManager ulm, HttpServletRequest request,
			HttpServletResponse response) throws IOException, PortalException {

		String[] newcolumns = request.getParameterValues("columns[]");
		int columnNumber = newcolumns.length;
		String tabId = request.getParameter("tabId");
        if (tabId ==  null) tabId = (String)request.getAttribute("tabId");
		Enumeration columns = ulm.getChildIds(tabId);
		List<String> columnList = new ArrayList<String>();
		while (columns.hasMoreElements()) {
			columnList.add((String) columns.nextElement());
		}
		List<String> newColumns = new ArrayList<String>();

		if (columnNumber > columnList.size()) {
			for (int i = columnList.size(); i < columnNumber; i++) {

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
				newColumns.add(node.getId());

			}
		} else if (columnNumber < columnList.size()) {
			String lastColumn = columnList.get(columnNumber - 1);
			for (int i = columnNumber; i < columnList.size(); i++) {
				String columnId = columnList.get(i);

				// move all channels in the current column to the last valid column
				Enumeration channels = ulm.getChildIds(columnId);
				while (channels.hasMoreElements()) {
					ulm.addNode(ulm.getNode((String) channels.nextElement()),
							lastColumn, null);
				}

				// delete the column from the user's layout
				ulm.deleteNode(columnId);

			}
		}

		int count = 0;
		columns = ulm.getChildIds(tabId);
		StructureStylesheetUserPreferences ssup = upm.getUserPreferences()
		.getStructureStylesheetUserPreferences();
		while (columns.hasMoreElements()) {
			String columnId = (String) columns.nextElement();
			ssup.setFolderAttributeValue(columnId, "width", newcolumns[count] + "%");
			Element folder = ulm.getUserLayoutDOM().getElementById(columnId);
			try {
				// This sets the column attribute in memory but doesn't persist it.  Comment says saves changes "prior to persisting"
				UserPrefsHandler.setUserPreference(folder, "width", per);
		        saveSSUPPreservingTab(upm, per, ssup);
			} catch (Exception e) {
				log.error("Error saving new column widths", e);
			}
			count++;
		}


		
		try {
			// save the user's layout
			saveUserLayoutPreservingTab(ulm, upm, per);
		} catch (Exception e) {
			log.warn("Error saving layout", e);
		}

		// construct XML representing all the IDs of the resulting columns
		StringBuffer buf = new StringBuffer();
		if (newColumns.size() > 0) {
			buf.append("<newColumns>");
			for (Iterator iter = newColumns.iterator(); iter.hasNext();) {
				buf.append("<id>" + iter.next() + "</id>");
			}
			buf.append("</newColumns>");
		}

		printSuccess(response, "Saved new column widths", buf.toString());

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
	private void moveTab(IPerson per, UserPreferencesManager upm,
			IUserLayoutManager ulm, HttpServletRequest request,
			HttpServletResponse response) throws PortalException, IOException {

		// gather the parameters we need to move a channel
		String destinationId = request.getParameter("elementID");
		String sourceId = request.getParameter("sourceID");
		String method = request.getParameter("method");
		String tabPosition = request.getParameter("tabPosition");

		// If we're moving this element before another one, we need
		// to know what the target is. If there's no target, just
		// assume we're moving it to the very end of the list.
		String siblingId = null;
		if (method.equals("insertBefore"))
			siblingId = destinationId;

		// move the node as requested and save the layout
		ulm.moveNode(sourceId, ulm.getParentId(destinationId), siblingId);

		StructureStylesheetUserPreferences ssup = upm.getUserPreferences()
				.getStructureStylesheetUserPreferences();

		try {
            String currentTab = ssup.getParameterValue(ACTIVE_TAB_PARAM);
            UserProfile currentProfile = upm.getUserPreferences().getProfile();
            int profileID = currentProfile.getProfileId();
            int structID = currentProfile.getStructureStylesheetId();
            // get the active tab number from the store so that we can preserve it
            String defaultTab = ulStore.getStructureStylesheetUserPreferences(per, profileID, structID).getParameterValue(ACTIVE_TAB_PARAM);
            // set the active tab to previously recorded value
            if (defaultTab.equals(currentTab)) {
                ssup.putParameterValue(ACTIVE_TAB_PARAM, tabPosition);
            }
            else {
                ssup.putParameterValue(ACTIVE_TAB_PARAM, defaultTab);
            }
            // This is a brute force save of the new attributes.  It requires access to the layout store. -SAB
            ulStore.setStructureStylesheetUserPreferences(per, profileID, ssup);
		} catch (Exception e) {
			log.error(e, e);
		}

		ulm.saveUserLayout();

        // reset the active tab for viewing (not default)
		ssup.putParameterValue(ACTIVE_TAB_PARAM, tabPosition);

		printSuccess(response, "Saved new tab position", null);

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
	private void addChannel(IPerson per, UserPreferencesManager upm,
			IUserLayoutManager ulm, IUserInstanceManager userInstanceManager, HttpServletRequest request,
			HttpServletResponse response) throws IOException, PortalException {

		// gather the parameters we need to move a channel
		String destinationId = request.getParameter("elementID");
		int sourceId = Integer.parseInt(request.getParameter("channelID"));
		String method = request.getParameter("position");

		IChannelRegistryStore channelRegistryStore = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();
		IChannelDefinition definition = channelRegistryStore.getChannelDefinition(sourceId);
		
        IUserLayoutChannelDescription channel = new UserLayoutChannelDescription(definition);
		for (IChannelParameter param : definition.getParameters()) {
		    if (param.getOverride()) {
                String paramValue = request.getParameter(param.getName());
                if (paramValue != null) {
                    channel.setParameterValue(param.getName(), param.getValue());
                }
		    }
		}

		IUserLayoutNodeDescription node = null;
		if (isTab(ulm, destinationId)) {
			Enumeration columns = ulm.getChildIds(destinationId);
			if (columns.hasMoreElements()) {
				while (columns.hasMoreElements()) {
					// attempt to add this channel to the column
					node = ulm.addNode(channel, (String) columns.nextElement(),
							null);
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

		// instantiate the channel in the user's layout
		final HttpSession session = request.getSession(false);
        ChannelManager cm = new ChannelManager(upm, session);
		cm.instantiateChannel(new PortalHttpServletRequestWrapper(request, response, userInstanceManager), response, channel.getId());

		try {
			// save the user's layout
			saveUserLayoutPreservingTab(ulm, upm, per);
		} catch (Exception e) {
			log.warn("Error saving layout", e);
		}

		printSuccess(response, "Added new channel", "<newNodeId>" + nodeId
				+ "</newNodeId>");

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
	private void chooseSkin(IPerson per, UserPreferencesManager upm,
			IUserLayoutManager ulm, HttpServletRequest request,
			HttpServletResponse response) throws IOException, PortalException {

		String skinName = request.getParameter("skinName");
        ThemeStylesheetUserPreferences themePrefs = upm.getUserPreferences().getThemeStylesheetUserPreferences();
        themePrefs.putParameterValue("skin",skinName);
		try {
			ulStore.setThemeStylesheetUserPreferences(per, upm
					.getUserPreferences().getProfile().getProfileId(), themePrefs);
		} catch (Exception e) {
			log.error("Error storing user skin preferences", e);
		}

		printSuccess(response, "Updated Skin", null);
}
	/**
	 * Set all columns on a given tab to have the same width.
	 * 
	 * @param per
	 * @param upm
	 * @param ulm
	 * @param tabId
	 * @throws PortalException
	 */
	private void equalizeColumnWidths(IPerson per, UserPreferencesManager upm,
			IUserLayoutManager ulm, String tabId) throws PortalException {

		// get the total number of columns
		Enumeration columns = ulm.getChildIds(tabId);
		int count = 0;
		while (columns.hasMoreElements()) {
			columns.nextElement();
			count++;
		}

		// set the new width for each column to be equal
		int width = 100 / count;
		String widthString = width + "%";

		StructureStylesheetUserPreferences ssup = upm.getUserPreferences().getStructureStylesheetUserPreferences();
        UserProfile currentProfile = upm.getUserPreferences().getProfile();
		columns = ulm.getChildIds(tabId);
		String currentTab = ssup.getParameterValue( ACTIVE_TAB_PARAM );
		try {
			while (columns.hasMoreElements()) {
				String columnId = (String) columns.nextElement();
				ssup.setFolderAttributeValue(columnId, "width", widthString);
				Element folder = ulm.getUserLayoutDOM().getElementById(columnId);
				// This sets the column attribute in memory but doesn't persist it.  Comment says saves changes "prior to persisting"
				UserPrefsHandler.setUserPreference(folder, "width", per);
                
				count++;
			}

	        saveSSUPPreservingTab(upm, per, ssup);

		} catch (Exception e) {
			log.error("Error saving new column widths", e);
		}

	}
	
	/**
	 * Set the column widths of a specified tab to the user's requested widths.
	 * 
	 * @param per
	 * @param upm
	 * @param ulm
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws PortalException
	 */
	private void updateColumnWidths(IPerson per, UserPreferencesManager upm,
			IUserLayoutManager ulm, HttpServletRequest request,
			HttpServletResponse response) throws IOException, PortalException {

		String[] columnIds = request.getParameterValues("columnIds");
		String[] columnWidths = request.getParameterValues("columnWidths");

		StructureStylesheetUserPreferences ssup = upm.getUserPreferences()
				.getStructureStylesheetUserPreferences();
		String currentTab = ssup.getParameterValue( ACTIVE_TAB_PARAM );

		for (int i = 0; i < columnIds.length; i++) {
			ssup
					.setFolderAttributeValue(columnIds[i], "width",
							columnWidths[i]);
			Element folder = ulm.getUserLayoutDOM()
					.getElementById(columnIds[i]);
			try {
				// This sets the column attribute in memory but doesn't persist it.  Comment says saves changes "prior to persisting"
				UserPrefsHandler.setUserPreference(folder, "width", per);
				
		        saveSSUPPreservingTab(upm, per, ssup);

			} catch (Exception e) {
				log.error("Error saving new column widths", e);
			}
		}

		printSuccess(response, "Added new channel", null);

	}

	/**
	 * Add a new tab to the layout.  The new tab will be appended to the end of the
	 * list and named with the BLANK_TAB_NAME variable.
	 * 
	 * @param ulm
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws PortalException
	 */
	private void addTab(IPerson per, UserPreferencesManager upm,
			IUserLayoutManager ulm, HttpServletRequest request,
			HttpServletResponse response) throws IOException, PortalException {

		// construct a brand new tab
		String id = "tbd";
        String tabName = request.getParameter("tabName");
        if (StringUtils.isBlank(tabName)) tabName = BLANK_TAB_NAME;
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
			saveUserLayoutPreservingTab(ulm, upm, per);
		} catch (Exception e) {
			log.warn("Error saving layout", e);
		}

		// get the id of the newly added tab
		String nodeId = newTab.getId();

		// pre-populate this new tab with one column
		IUserLayoutFolderDescription newColumn = new UserLayoutFolderDescription();
		newColumn.setName("Column");
		newColumn.setId("tbd");
		newColumn.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
		newColumn.setHidden(false);
		newColumn.setUnremovable(false);
		newColumn.setImmutable(false);
		ulm.addNode(newColumn, nodeId, null);

		try {
			// save the user's layout
			saveUserLayoutPreservingTab(ulm, upm, per);
		} catch (Exception e) {
			log.warn("Error saving layout", e);
		}

        request.setAttribute("tabId", nodeId);
        changeColumns(per, upm, ulm, request, response);

	}

	/**
	 * Rename a specified tab.
	 * 
	 * @param ulm
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws PortalException
	 */
	private void renameTab(IPerson per, UserPreferencesManager upm,
			IUserLayoutManager ulm, HttpServletRequest request,
			HttpServletResponse response) throws IOException, PortalException {

		// element ID of the tab to be renamed
		String tabId = request.getParameter("tabId");

		// desired new name
		String tabName = request.getParameter("tabName");

		// rename the tab
		IUserLayoutFolderDescription tab = (IUserLayoutFolderDescription) ulm
				.getNode(tabId);
		if (ulm.canUpdateNode(ulm.getNode(tabId))) {
			if (tabName == null || tabName.trim().length() == 0) {
				tab.setName(BLANK_TAB_NAME);
			} else {
				tab.setName(tabName);
			}
			ulm.updateNode(tab);
			try {
				// save the user's layout
				saveUserLayoutPreservingTab(ulm, upm, per);
			} catch (Exception e) {
				log.warn("Error saving layout", e);
			}

		} else {
			throw new PortalException("attempt.to.rename.immutable.tab" + tabId);
		}
		
		StructureStylesheetUserPreferences ssup = upm.getUserPreferences()
			.getStructureStylesheetUserPreferences();
		ssup.setFolderAttributeValue(tabId, "name", tabName);



		printSuccess(response, "Saved new tab name", null);

	}

	private String getDefaultTab(UserPreferencesManager upm, IPerson per) throws Exception {
	    UserProfile currentProfile = upm.getUserPreferences().getProfile();
	    int profileID = currentProfile.getProfileId();
	    int structID = currentProfile.getStructureStylesheetId();
	    String defaultTab = ulStore.getStructureStylesheetUserPreferences(per, profileID, structID)
	                              .getParameterValue( ACTIVE_TAB_PARAM );
	    return defaultTab;
	}

	/**
	 * Save the user's layout while preserving the current in-storage default
	 * tab.
	 * 
	 * @param ulm
	 * @param upm
	 * @param per
	 * @throws Exception
	 */
	private void saveUserLayoutPreservingTab(IUserLayoutManager ulm, UserPreferencesManager upm, IPerson per) throws Exception {
		StructureStylesheetUserPreferences ssup = upm.getUserPreferences().getStructureStylesheetUserPreferences();
		String currentTab = ssup.getParameterValue( ACTIVE_TAB_PARAM );
	    
		// get the active tab number from the store so that we can preserve it
	    String defaultTab = getDefaultTab(upm, per);
	    // set the active tab to previously recorded value
	    ssup.putParameterValue( ACTIVE_TAB_PARAM, defaultTab );

	    // save the user's layout
		ulm.saveUserLayout();
		
		// set the current active tab back to the previous value
	    ssup.putParameterValue( ACTIVE_TAB_PARAM, currentTab );
		
	}
	
	/**
	 * Save the user's structure stylesheet while preserving the current
	 * in-storage default tab.
	 * 
	 * @param upm
	 * @param per
	 * @param ssup
	 * @throws Exception
	 */
	private void saveSSUPPreservingTab(UserPreferencesManager upm, IPerson per, StructureStylesheetUserPreferences ssup) throws Exception {
		int profileId = upm.getUserPreferences().getProfile().getProfileId();
		String currentTab = ssup.getParameterValue( ACTIVE_TAB_PARAM );
        // get the active tab number from the store so that we can preserve it
        String defaultTab = getDefaultTab(upm, per);
        // set the active tab to previously recorded value
        ssup.putParameterValue( ACTIVE_TAB_PARAM, defaultTab );
        
		// This is a brute force save of the new attributes.  It requires access to the layout store. -SAB
		ulStore.setStructureStylesheetUserPreferences(per, profileId, ssup);

	    // set active tab in current preferences back to "current" tab
	    ssup.putParameterValue( ACTIVE_TAB_PARAM, currentTab );

	}

	/**
	 * A folder is a tab if its parent element is the layout element
	 * @param folder the folder in question
	 * @return <code>true</code> if the folder is a tab, otherwise <code>false</code>
	 */
	private final boolean isTab(IUserLayoutManager ulm, String folderId)
			throws PortalException {
		// we could be a bit more careful here and actually check the type
		return ulm.getRootFolderId().equals(ulm.getParentId(folderId));
	}

	/**
	 * A folder is a column if its parent is a tab element
	 * @param folder the folder in question
	 * @return <code>true</code> if the folder is a column, otherwise <code>false</code>
	 */
	private final boolean isColumn(IUserLayoutManager ulm, String folderId)
			throws PortalException {
		return isTab(ulm, ulm.getParentId(folderId));
	}

	/**
	 * Print an XML success response.
	 * 
	 * @param response
	 * @param message	A descriptive message of the saved change.
	 * @param data	Any extra data the method needs to send back for AJAX processing.
	 * @throws IOException
	 */
	private void printSuccess(HttpServletResponse response, String message,
			String data) throws IOException {
		response.setContentType("text/xml");
		response.getWriter()
				.print("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		response.getWriter().print("<response>");
		response.getWriter().print("<status>");
		response.getWriter().print("<success>true</success>");
		response.getWriter().print("<message>" + message + "</message>");
		response.getWriter().print("</status>");
		if (data != null)
			response.getWriter().print(data);
		response.getWriter().print("</response>");
	}

	private void printError(HttpServletResponse response, String message) throws IOException {
		response.setContentType("text/xml");
		response.getWriter()
				.print("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		response.getWriter().print("<response>");
		response.getWriter().print("<status>");
		response.getWriter().print("<success>false</success>");
		response.getWriter().print("<message>" + message + "</message>");
		response.getWriter().print("</status>");
		response.getWriter().print("</response>");
	}

    @Override
    public void afterPropertiesSet() throws Exception {
        ulStore = UserLayoutStoreFactory.getUserLayoutStoreImpl();
    }

}
