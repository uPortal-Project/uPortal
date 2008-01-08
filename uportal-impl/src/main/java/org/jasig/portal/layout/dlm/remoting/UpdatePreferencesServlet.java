package org.jasig.portal.layout.dlm.remoting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.IUserInstance;
import org.jasig.portal.PortalException;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserInstanceManager;
import org.jasig.portal.UserPreferencesManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.layout.dlm.UserPrefsHandler;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.layout.node.UserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Provides targets for AJAX preference setting calls.
 * 
 * @author jennifer.bourey@yale.edu
 * @version $Revision$ $Date$
 */
public class UpdatePreferencesServlet extends HttpServlet {

	Log log = LogFactory.getLog(getClass());

	private static IUserLayoutStore ulStore = UserLayoutStoreFactory
			.getUserLayoutStoreImpl();

	// default tab name
	protected static String BLANK_TAB_NAME = "New Tab";

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	    IUserInstance ui = null;
		IPerson per = null;
		UserPreferencesManager upm = null;
		IUserLayoutManager ulm = null;

		try {
			// Retrieve the user's UserInstance object
			ui = UserInstanceManager.getUserInstance(request);

			// Retrieve the user's IPerson object
			per = ui.getPerson();

			// Retrieve the preferences manager
			upm = (UserPreferencesManager) ui.getPreferencesManager();

			// Retrieve the layout manager
			ulm = upm.getUserLayoutManager();

		} catch (NullPointerException e1) {
			// alert the user that his/her session has timed out
			log
					.debug(
							"User encountered session timeout while attempting AJAX preferences action",
							e1);
			response
					.sendError(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Your session has timed out.  Please log in again to make changes to your layout.");
			return;
		} catch (PortalException e1) {
			response
					.sendError(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Your session has timed out.  Please log in again to make changes to your layout.");
			return;
		}

		try {

			// get the requested preferences action
			String action = request.getParameter("action");

			// perform the requested action
			if (action == null) {

				log.warn("preferences servlet called with no action parameter");

			} else if (action.equals("movePortletHere")) {

				moveChannel(ulm, request, response);

			} else if (action.equals("changeColumns")) {

				changeColumns(per, upm, ulm, request, response);

			} else if (action.equals("updateColumnWidths")) {

				updateColumnWidths(per, upm, ulm, request, response);

			} else if (action.equals("addChannel")) {

				addChannel(per, upm, ulm, request, response);

			} else if (action.equals("renameTab")) {

				renameTab(per, upm, ulm, request, response);

			} else if (action.equals("addTab")) {

				addTab(ulm, request, response);

			} else if (action.equals("moveTabHere")) {

				moveTab(per, upm, ulm, request, response);

			} else if (action.equals("chooseSkin")) {
				
				chooseSkin(per, upm, ulm, request, response);
				
			} else if (action.equals("removeElement")) {

				// Delete the requested element node.  This code is the same for 
				// all node types, so we can just have a generic action.
				String elementId = request.getParameter("elementID");
				ulm.deleteNode(elementId);
				ulm.saveUserLayout();

				printSuccess(response, "Removed element", null);

			}

		} catch (PortalException e) {
			log.error(e);
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
	private void moveChannel(IUserLayoutManager ulm,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, PortalException {

		// portlet to be moved
		String sourceId = request.getParameter("sourceID");

		// Either "insertBefore" or "appendAfter".
		String method = request.getParameter("method");

		// Target element to move the source element in front of.  This parameter
		// isn't actually relevant if we're appending the source element.
		String destinationId = request.getParameter("elementID");

		// if the target is a column type node, we need to just move the portlet
		// to the end of the column
		if (ulm.getRootFolderId().equals(
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

		// save the user's layout
		ulm.saveUserLayout();

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

		int columnNumber = Integer.parseInt(request
				.getParameter("columnNumber"));
		String tabId = request.getParameter("tabId");
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

		// set all the columns to have equal width
		equalizeColumnWidths(per, upm, ulm, tabId);

		// save the layout changes
		ulm.saveUserLayout();

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
		ssup.putParameterValue("activeTab", "1");

		try {
			// This is a brute force save of the new attributes.  It requires access to the layout store. -SAB
			ulStore.setStructureStylesheetUserPreferences(per, upm
					.getUserPreferences().getProfile().getProfileId(), ssup);
		} catch (Exception e) {
			log.error(e);
		}

		ssup.putParameterValue("activeTab", tabPosition);

		ulm.saveUserLayout();

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
			IUserLayoutManager ulm, HttpServletRequest request,
			HttpServletResponse response) throws IOException, PortalException {

		// gather the parameters we need to move a channel
		String destinationId = request.getParameter("elementID");
		String sourceId = request.getParameter("channelID");
		String method = request.getParameter("position");

		Document channelRegistry = ChannelRegistryManager
				.getChannelRegistry(per);
		Element newChannel = channelRegistry.getElementById(sourceId);
		NodeList params = newChannel.getElementsByTagName("parameter");
		for (int i = 0; i < params.getLength(); i++) {
			Element param = (Element) params.item(i);
			String override = param.getAttribute("override");
			if (override != null && override.equals("yes")) {
				String paramValue = request.getParameter(param
						.getAttribute("name"));
				if (paramValue != null)
					param.setAttribute("value", paramValue);
			}
		}

		// move the node as requested and save the layout
		IUserLayoutChannelDescription channel = new UserLayoutChannelDescription(
				newChannel);

		IUserLayoutNodeDescription node = null;
		if (isTab(ulm, destinationId)) {
			Enumeration columns = ulm.getChildIds(destinationId);
			if (columns.hasMoreElements()) {
				node = ulm.addNode(channel, (String) columns.nextElement(),
						null);
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
		cm.instantiateChannel(request, response, channel.getId());

		// save the user layout
		ulm.saveUserLayout();

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

		StructureStylesheetUserPreferences ssup = upm.getUserPreferences()
				.getStructureStylesheetUserPreferences();
		columns = ulm.getChildIds(tabId);
		while (columns.hasMoreElements()) {
			String columnId = (String) columns.nextElement();
			ssup.setFolderAttributeValue(columnId, "width", widthString);
			Element folder = ulm.getUserLayoutDOM().getElementById(columnId);
			try {
				// This sets the column attribute in memory but doesn't persist it.  Comment says saves changes "prior to persisting"
				UserPrefsHandler.setUserPreference(folder, "width", per);
				// This is a brute force save of the new attributes.  It requires access to the layout store. -SAB
				ulStore
						.setStructureStylesheetUserPreferences(per, upm
								.getUserPreferences().getProfile()
								.getProfileId(), ssup);
			} catch (Exception e) {
				log.error("Error saving new column widths", e);
			}
			count++;
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

		for (int i = 0; i < columnIds.length; i++) {
			ssup
					.setFolderAttributeValue(columnIds[i], "width",
							columnWidths[i]);
			Element folder = ulm.getUserLayoutDOM()
					.getElementById(columnIds[i]);
			try {
				// This sets the column attribute in memory but doesn't persist it.  Comment says saves changes "prior to persisting"
				UserPrefsHandler.setUserPreference(folder, "width", per);
				// This is a brute force save of the new attributes.  It requires access to the layout store. -SAB
				ulStore
						.setStructureStylesheetUserPreferences(per, upm
								.getUserPreferences().getProfile()
								.getProfileId(), ssup);
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
	private void addTab(IUserLayoutManager ulm, HttpServletRequest request,
			HttpServletResponse response) throws IOException, PortalException {

		// construct a brand new tab
		String id = "tbd";
		String tabName = BLANK_TAB_NAME;
		IUserLayoutFolderDescription newTab = new UserLayoutFolderDescription();
		newTab.setName(tabName);
		newTab.setId(id);
		newTab.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
		newTab.setHidden(false);
		newTab.setUnremovable(false);
		newTab.setImmutable(false);

		// add the tab to the layout
		ulm.addNode(newTab, ulm.getRootFolderId(), null);
		ulm.saveUserLayout();

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

		// save the changes to the layout
		ulm.saveUserLayout();

		printSuccess(response, "Added new tab", "<newNodeId>" + nodeId
				+ "</newNodeId>");

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
			ulm.saveUserLayout();

		} else {
			throw new PortalException("attempt.to.rename.immutable.tab" + tabId);
		}
		
		StructureStylesheetUserPreferences ssup = upm.getUserPreferences()
			.getStructureStylesheetUserPreferences();
		ssup.setFolderAttributeValue(tabId, "name", tabName);



		printSuccess(response, "Saved new tab name", null);

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

}
