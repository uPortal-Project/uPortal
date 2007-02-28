package org.jasig.portal.layout.dlm.remoting;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.PortalException;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.UserInstance;
import org.jasig.portal.UserInstanceManager;
import org.jasig.portal.UserPreferencesManager;
import org.jasig.portal.layout.IUserLayoutManager;
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

	protected static String BLANK_TAB_NAME = "New Tab"; // The tab will take on

	// this name if left
	// blank by the user

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			// Retrieve the user's UserInstance object
			UserInstance ui = UserInstanceManager.getUserInstance(request);

			// Retrieve the user's IPerson object
			IPerson per = ui.getPerson();

			// Retrieve the preferences manager
			UserPreferencesManager upm = (UserPreferencesManager) ui
					.getPreferencesManager();

			// Retrieve the layout manager
			IUserLayoutManager ulm = upm.getUserLayoutManager();

			String action = request.getParameter("action");

			// handle channel move requests
			if (action == null) {

				log.warn("preferences servlet called with no action parameter");

			} else if (action.equals("movePortletHere")) {

				movePortlet(ulm, request, response);
				
			} else if (action.equals("moveColumnHere")) {
				
				moveColumn(ulm, request, response);

			} else if (action.equals("addColumn")) {

				addColumn(per, upm, ulm, request, response);
				
			} else if (action.equals("addChannel")) {

				addPortlet(per, upm, ulm, request, response);

			} else if (action.equals("renameTab")) {

				renameTab(ulm, request, response);
				
			} else if (action.equals("addTab")) {

				addTab(ulm, request, response);

			} else if (action.equals("moveTabHere")) {
				
				moveTab(ulm, request, response);

			} else if (action.equals("removeElement")) {

				String elementId = request.getParameter("elementID");
				ulm.deleteNode(elementId);
				ulm.saveUserLayout();
				
				response.setContentType("text/xml");
				response.getWriter().print(
						"<?xml version=\"1.0\" encoding=\"utf-8\"?>");
				response.getWriter().print(
						"<status>success</status>");

			}

		} catch (PortalException e) {
			log.error(e);
		}

	}

	public void movePortlet(IUserLayoutManager ulm, HttpServletRequest request, HttpServletResponse response) throws IOException, PortalException {
		// gather the parameters we need to move a channel
		String destinationId = request.getParameter("elementID");
		String sourceId = request.getParameter("sourceID");
		String method = request.getParameter("method");

		if (ulm.getRootFolderId().equals(
				ulm.getParentId(ulm.getParentId(destinationId)))) {
			// move the channel into the column
			ulm.moveNode(sourceId, destinationId, null);
		} else {
			// If we're moving this element before another one, we need
			// to know what the target is. If there's no target, just
			// assume we're moving it to the very end of the column.
			String siblingId = null;
			if (method.equals("insertBefore"))
				siblingId = destinationId;

			// move the node as requested and save the layout
			ulm.moveNode(sourceId, ulm.getParentId(destinationId),
					siblingId);
		}
		ulm.saveUserLayout();

		response.setContentType("text/xml");
		response.getWriter().print(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		response.getWriter().print(
				"<status>success</status>");

	}

	public void moveColumn(IUserLayoutManager ulm, HttpServletRequest request, HttpServletResponse response) throws IOException, PortalException {

		// gather the parameters we need to move a channel
		String destinationId = request.getParameter("elementID");
		String sourceId = request.getParameter("sourceID");
		String method = request.getParameter("method");

		// If we're moving this element before another one, we need
		// to know what the target is. If there's no target, just
		// assume we're moving it to the very end of the column.
		String siblingId = null;
		if (method.equals("insertBefore"))
			siblingId = destinationId;

		// move the node as requested and save the layout
		ulm.moveNode(sourceId, ulm.getParentId(destinationId),
				siblingId);
		ulm.saveUserLayout();

		response.setContentType("text/xml");
		response.getWriter().print(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		response.getWriter().print(
				"<status>success</status>");

}

	public void moveTab(IUserLayoutManager ulm, HttpServletRequest request, HttpServletResponse response) throws IOException, PortalException {

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
		ulm.moveNode(sourceId, ulm.getParentId(destinationId),
				siblingId);
		ulm.saveUserLayout();

		response.setContentType("text/xml");
		response.getWriter().print(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		response.getWriter().print(
				"<status>success</status>");

	}

	public void addPortlet(IPerson per, UserPreferencesManager upm, IUserLayoutManager ulm, HttpServletRequest request, HttpServletResponse response) throws IOException, PortalException {

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
			IUserLayoutNodeDescription tab = ulm.getNode(destinationId);
			Enumeration columns = ulm.getChildIds(destinationId);
			if (columns.hasMoreElements()) {
				node = ulm.addNode(channel, (String) columns.nextElement(), null);
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
				IUserLayoutNodeDescription col = ulm.addNode(newColumn, destinationId, null);
			
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
			node = ulm.addNode(channel, ulm
					.getParentId(destinationId), siblingId);
		}

		String nodeId = node.getId();

		// instantiate the channel in the user's layout
		ChannelManager cm = new ChannelManager(upm);
		cm.instantiateChannel(channel.getId());

		// save the user layout
		ulm.saveUserLayout();

		response.setContentType("text/xml");
		response.getWriter().print(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		response.getWriter().print(
				"<response>" + nodeId + "</response>");

	}

	public void addColumn(IPerson per, UserPreferencesManager upm, IUserLayoutManager ulm, HttpServletRequest request, HttpServletResponse response) throws IOException, PortalException {

		String destinationId = request.getParameter("elementID");

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
		IUserLayoutNodeDescription node = ulm.addNode(newColumn, ulm
				.getParentId(destinationId), null);
		String nodeId = node.getId();
		ulm.saveUserLayout();

		adjustColumnWidths(per, upm, ulm, ulm.getParentId(destinationId));
		
		response.setContentType("text/xml");
		response.getWriter().print(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		response.getWriter().print(
				"<response>" + nodeId + "</response>");

	}
	
	public void adjustColumnWidths(IPerson per, UserPreferencesManager upm, IUserLayoutManager ulm, String tabId) throws PortalException {

		Enumeration columns = ulm.getChildIds(tabId);
		int count = 0;
		while (columns.hasMoreElements()) {
			columns.nextElement();
			count++;
		}
		int width = 100 / count;
		String widthString = width + "%";
		
		StructureStylesheetUserPreferences ssup = upm.getUserPreferences().getStructureStylesheetUserPreferences();
		columns = ulm.getChildIds(tabId);
		while (columns.hasMoreElements()) {
			String columnId = (String) columns.nextElement();
	        ssup.setFolderAttributeValue(columnId, "width", widthString);
	        Element folder = ulm.getUserLayoutDOM().getElementById( columnId );
	        try {
				UserPrefsHandler.setUserPreference( folder, "width", per );
			} catch (Exception e) {
				log.error("Error saving new column widths", e);
			}
			count++;
		}

	}

	public void addTab(IUserLayoutManager ulm, HttpServletRequest request, HttpServletResponse response) throws IOException, PortalException {
		String tabName = BLANK_TAB_NAME;

		String id = "tbd";
		IUserLayoutFolderDescription newTab = new UserLayoutFolderDescription();
		newTab.setName(tabName);
		newTab.setId(id);
		newTab.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
		newTab.setHidden(false);
		newTab.setUnremovable(false);
		newTab.setImmutable(false);

		ulm.addNode(newTab, ulm.getRootFolderId(), null);
		ulm.saveUserLayout();
		String nodeId = newTab.getId();

		// create new column element
		IUserLayoutFolderDescription newColumn = new UserLayoutFolderDescription();
		newColumn.setName("Column");
		newColumn.setId("tbd");
		newColumn
				.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
		newColumn.setHidden(false);
		newColumn.setUnremovable(false);
		newColumn.setImmutable(false);

		ulm.addNode(newColumn, nodeId, null);

		ulm.saveUserLayout();

		response.setContentType("text/xml");
		response.getWriter().print(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		response.getWriter().print("<response>");
		response.getWriter().print("<tabId>" + nodeId + "</tabId>");
		response.getWriter().print("</response>");
	}

	public void renameTab(IUserLayoutManager ulm, HttpServletRequest request, HttpServletResponse response) throws IOException, PortalException {
		String tabId = request.getParameter("tabId");
		String tabName = request.getParameter("tabName");

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
			throw new PortalException("attempt.to.rename.immutable.tab"
					+ tabId);
		}

		response.setContentType("text/xml");
		response.getWriter().print(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		response.getWriter().print(
				"<status>success</status>");

	}

	  /**
	   * A folder is a tab if its parent element is the layout element
	   * @param folder the folder in question
	   * @return <code>true</code> if the folder is a tab, otherwise <code>false</code>
	   */
	  private final boolean isTab (IUserLayoutManager ulm, String folderId) throws PortalException
	  {
	      // we could be a bit more careful here and actually check the type
	      return ulm.getRootFolderId().equals(ulm.getParentId(folderId));
	  }

	  /**
	   * A folder is a column if its parent is a tab element
	   * @param folder the folder in question
	   * @return <code>true</code> if the folder is a column, otherwise <code>false</code>
	   */
	  private final boolean isColumn (IUserLayoutManager ulm, String folderId) throws PortalException
	  {
	      return isTab(ulm, ulm.getParentId(folderId));
	  }



}
