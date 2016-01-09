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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.beanutils.BeanPredicate;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.EqualPredicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserPreferencesManager;
import org.jasig.portal.fragment.subscribe.IUserFragmentSubscription;
import org.jasig.portal.fragment.subscribe.dao.IUserFragmentSubscriptionDao;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.layout.IStylesheetUserPreferencesService;
import org.jasig.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;
import org.jasig.portal.layout.IUserLayout;
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
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlets.favorites.FavoritesUtils;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.provider.RestrictedPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.jasig.portal.layout.node.IUserLayoutNodeDescription.LayoutNodeType.FOLDER;

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

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IUserIdentityStore userIdentityStore;
    private IUserFragmentSubscriptionDao userFragmentInfoDao;
    private IUserInstanceManager userInstanceManager;
    private IStylesheetUserPreferencesService stylesheetUserPreferencesService;
    private IUserLayoutStore userLayoutStore;
    private MessageSource messageSource;
    private IPortletWindowRegistry portletWindowRegistry;

    @Value("${org.jasig.portal.layout.dlm.remoting.addedWindowState:null}")
    private String addedPortletWindowState;

    private WindowState addedWindowState;

    @PostConstruct
    private void initAddedPortletWindowState(){
        if(addedPortletWindowState!=null && !"null".equalsIgnoreCase(addedPortletWindowState) && !addedPortletWindowState.isEmpty()){
            addedWindowState = new WindowState(addedPortletWindowState);
        }
    }

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

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
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
               if (!ulm.deleteNode(elementId)) {
                   log.info("Failed to remove element ID {} from layout root folder ID {}, delete node returned false", elementId, ulm.getRootFolderId());
                   response.sendError(HttpServletResponse.SC_FORBIDDEN);
                   return new ModelAndView("jsonView", Collections.singletonMap("error", getMessage("error.element.update", "Unable to update element", RequestContextUtils.getLocale(request))));
               }
            }

            ulm.saveUserLayout();

            return new ModelAndView("jsonView", Collections.EMPTY_MAP);

        } catch (PortalException e) {
            return handlePersistError(request, response, e);
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
            return new ModelAndView("jsonView", Collections.singletonMap("error", "Attempted to subscribe to tab with null owner ID"));
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
            NodeList nodes = (NodeList) xpath.evaluate(expression.toString(), layoutDocument, XPathConstants.NODESET);
            String sourceId = nodes.item(0).getAttributes().getNamedItem("ID").getTextContent();
            ulm.moveNode(sourceId, ulm.getParentId(destinationId), destinationId);

            ulm.saveUserLayout();

            return new ModelAndView("jsonView", Collections.singletonMap("tabId", sourceId));

        } catch (XPathExpressionException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return new ModelAndView("jsonView", Collections.singletonMap("error", "Xpath error"));
        } catch (PortalException e) {
            return handlePersistError(request, response, e);
        }

    }

    @RequestMapping(method = RequestMethod.POST, params = "action=movePortletAjax")
    public ModelAndView movePortletAjax(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @RequestParam String sourceId,
                                        @RequestParam String previousNodeId,
                                        @RequestParam String nextNodeId) {
        final Locale locale = RequestContextUtils.getLocale(request);
        if(moveElementInternal(request, sourceId, previousNodeId, "appendAfter")
                && moveElementInternal(request, sourceId, nextNodeId, "insertBefore") ) {
            return new ModelAndView("jsonView",Collections.singletonMap("response", getMessage("success.move.element", "Element moved successfully", locale)));
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView("jsonView", Collections.singletonMap("response", getMessage("error.move.element", "Error moving element.", locale)));
        }
    }

    /**
     * Move a portlet to another location on the tab.
     *
     * This deprecated method is replaced by the method/action "moveElement".
     * The code is the same, but the naming better abstracts the action. This method is here for
     * backwards compatibility with anything using the "movePortlet" action of the API.
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws PortalException
     * @deprecated - replaced by the method/action "moveElement"
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=movePortlet")
    @Deprecated
    public ModelAndView movePortlet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, PortalException {
        return moveElement(request, response);
    }

    /**
     * Move an element to another location on the tab.
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws PortalException
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=moveElement")
    public ModelAndView moveElement(HttpServletRequest request, HttpServletResponse response)
            throws IOException, PortalException {
        final Locale locale = RequestContextUtils.getLocale(request);

        // element to be moved
        String sourceId = request.getParameter("sourceID");

        // Either "insertBefore" or "appendAfter".
        String method = request.getParameter("method");

        // Target element to move the source element in front of.  This parameter
        // isn't actually relevant if we're appending the source element.
        String destinationId = request.getParameter("elementID");

        if(moveElementInternal(request, sourceId, destinationId, method)) {
            return new ModelAndView("jsonView", Collections.singletonMap("response", getMessage("success.move.element", "Element moved successfully", locale)));
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView("jsonView", Collections.singletonMap("response", getMessage("error.move.element", "Error moving element", locale)));
        }
    }

    /**
     * Change the number of columns on a specified tab.  In the event that the user is
     * decreasing the number of columns, extra columns will be stripped from the
     * right-hand side.  Any channels in these columns will be moved to the bottom of
     * the last preserved column.
     *
     * @param widths array of column widths
     * @param deleted array of deleted column IDs
     * @param acceptor not sure what this is
     * @param request HttpRequest
     * @param response HttpResponse
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
        } catch (PortalException e) {
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
        final Locale locale = RequestContextUtils.getLocale(request);

        // gather the parameters we need to move a channel
        String destinationId = request.getParameter("elementID");
        String sourceId = request.getParameter("sourceID");
        String method = request.getParameter("method");

        // If we're moving this element before another one, we need
        // to know what the target is. If there's no target, just
        // assume we're moving it to the very end of the list.
        String siblingId = null;
        if ("insertBefore".equals(method))
            siblingId = destinationId;

        try {
            // move the node as requested and save the layout
            if (!ulm.moveNode(sourceId, ulm.getParentId(destinationId), siblingId)) {
                log.warn("Failed to move tab in user layout. moveNode returned false");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return new ModelAndView("jsonView",
                        Collections.singletonMap("response", getMessage("error.move.tab",
                                "There was an issue moving the tab, please refresh the page and try again.", locale)));
            }
            ulm.saveUserLayout();
        } catch (PortalException e) {
            return handlePersistError(request, response, e);
        }

        return new ModelAndView("jsonView",
                Collections.singletonMap("response", getMessage("success.move.tab", "Tab moved successfully", locale)));
    }

    @RequestMapping(method= RequestMethod.POST , params = "action=addFavorite")
    public ModelAndView addFavorite(@RequestParam String channelId, HttpServletRequest request, HttpServletResponse response) {
        //setup
        IUserInstance ui = userInstanceManager.getUserInstance(request);

        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        IUserLayoutChannelDescription channel = new UserLayoutChannelDescription(portletDefinitionRegistry.getPortletDefinition(channelId));

        final Locale locale = RequestContextUtils.getLocale(request);

        //get favorite tab
        String favoriteTabNodeId = FavoritesUtils.getFavoriteTabNodeId(ulm.getUserLayout());

        if(favoriteTabNodeId != null) {
            //add portlet to favorite tab
            IUserLayoutNodeDescription node = addNodeToTab(ulm, channel, favoriteTabNodeId);

            if (node == null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return new ModelAndView("jsonView", Collections.singletonMap("response",
                                getMessage("error.add.portlet.in.tab", "Can''t add a new favorite", locale)));
            }

            try {
                // save the user's layout
                ulm.saveUserLayout();
            } catch (PortalException e) {
                return handlePersistError(request, response, e);
            }

            //document success for notifications
            Map<String, String> model = new HashMap<String, String>();
            final String channelTitle = channel.getTitle();
            model.put("response", getMessage("favorites.added.favorite", channelTitle,
                            "Added " + channelTitle + " as a favorite.", locale));
            model.put("newNodeId", node.getId());
            return new ModelAndView("jsonView", model);
       } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ModelAndView("jsonView", Collections.singletonMap("response",
                            getMessage("error.finding.favorite.tab", "Can''t find favorite tab", locale)));
       }
    }

    /**
     * This method removes the channelId specified from favorites. Note that even if you pass in the layout channel id, it will always remove from the favorites.
     * @param channelId The long channel ID that is used to determine which fname to remove from favorites
     * @param request
     * @param response
     * @return returns a mav object with a response attribute for noty
     * @throws IOException if it has problem reading the layout file.
     */
    @RequestMapping(method= RequestMethod.POST , params = "action=removeFavorite")
    public ModelAndView removeFavorite(@RequestParam String channelId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserPreferencesManager upm = (UserPreferencesManager) userInstanceManager.getUserInstance(request).getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();
        final Locale locale = RequestContextUtils.getLocale(request);
        IPortletDefinition portletDefinition = portletDefinitionRegistry.getPortletDefinition(channelId);

        if(portletDefinition != null &&  StringUtils.isNotBlank(portletDefinition.getFName())) {
            String functionalName = portletDefinition.getFName();
            List<IUserLayoutNodeDescription> favoritePortlets = FavoritesUtils.getFavoritePortlets(ulm.getUserLayout());

            //search for the favorite to delete
            EqualPredicate nameEqlPredicate = new EqualPredicate(functionalName);
            Object result = CollectionUtils.find(favoritePortlets, new BeanPredicate("functionalName",nameEqlPredicate));

            if(result != null && result instanceof UserLayoutChannelDescription) {
                UserLayoutChannelDescription channelDescription = (UserLayoutChannelDescription)result;
                try {
                    if (!ulm.deleteNode(channelDescription.getChannelSubscribeId())) {
                        log.warn("Error deleting the node" + channelId + "from favorites for user " + (upm.getPerson() == null ? "unknown" : upm.getPerson().getID()));
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return new ModelAndView("jsonView", Collections.singletonMap("response", getMessage("error.remove.favorite", "Can''t remove favorite", locale)));
                    }
                    // save the user's layout
                    ulm.saveUserLayout();
                } catch (PortalException e) {
                    return handlePersistError(request, response, e);
                }

                //document success for notifications
                Map<String, String> model = new HashMap<String, String>();
                model.put("response", getMessage("success.remove.portlet", "Removed from Favorites successfully", locale));
                return new ModelAndView("jsonView", model);
            }
        }
        // save the user's layout
        ulm.saveUserLayout();
        return new ModelAndView("jsonView", Collections.singletonMap("response", getMessage("error.finding.favorite", "Can''t find favorite", locale)));
    }

    /**
     * Add a new channel.
     *
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
        final Locale locale = RequestContextUtils.getLocale(request);

        // gather the parameters we need to move a channel
        String destinationId = request.getParameter("elementID");
        String sourceId = request.getParameter("channelID");
        String method = request.getParameter("position");
        String fname = request.getParameter("fname");

        if(destinationId == null) {
            String tabName = request.getParameter("tabName");
            if(tabName != null) {
                destinationId = getTabIdFromName(ulm.getUserLayout(),tabName);
            }
        }

        IPortletDefinition definition = null;
        if(sourceId != null)
            definition = portletDefinitionRegistry.getPortletDefinition(sourceId);
        else if (fname != null)
            definition = portletDefinitionRegistry.getPortletDefinitionByFname(fname);
        else {
            log.error("SourceId or fname invalid when adding a portlet");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ModelAndView("jsonView", Collections.singletonMap("error", "SourceId or fname invalid"));
        }

        IUserLayoutChannelDescription channel = new UserLayoutChannelDescription(definition);

        IUserLayoutNodeDescription node = null;
        if (isTab(ulm, destinationId)) {
            node = addNodeToTab(ulm,channel, destinationId);

        } else {
            boolean isInsert = method != null && method.equals("insertBefore");

            //If neither an insert or type folder - Can't "insert into" non-folder
            if (!(isInsert || isFolder(ulm, destinationId))) {
                log.error("Cannot insert into portlet element");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return new ModelAndView("jsonView", Collections.singletonMap("error", "Cannot insert into portlet element"));
            }

            String siblingId = isInsert ? destinationId : null;
            String target = isInsert ? ulm.getParentId(destinationId) : destinationId;

            // move the channel into the column
            node = ulm.addNode(channel, target, siblingId);
        }


        if (node == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView("jsonView", Collections.singletonMap("error", getMessage("error.add.element", "Unable to add element", locale)));
        }

        String nodeId = node.getId();

        try {
            // save the user's layout
            ulm.saveUserLayout();
            if(addedWindowState!=null){
                IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(request, channel.getFunctionalName());
                portletWindow.setWindowState(addedWindowState);
                this.portletWindowRegistry.storePortletWindow(request, portletWindow);
            }
        } catch (PortalException e) {
            return handlePersistError(request, response, e);
        }

        Map<String, String> model = new HashMap<String, String>();
        model.put("response", getMessage("success.add.portlet", "Added a new channel", locale));
        model.put("newNodeId", nodeId);
        return new ModelAndView("jsonView", model);

    }

    private IUserLayoutNodeDescription addNodeToTab(IUserLayoutManager ulm, IUserLayoutChannelDescription channel, String tabId) {
        IUserLayoutNodeDescription node = null;

        Enumeration<String> columns = ulm.getChildIds(tabId);
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
            newColumn.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
            newColumn.setHidden(false);
            newColumn.setUnremovable(false);
            newColumn.setImmutable(false);

            // add the column to our layout
            IUserLayoutNodeDescription col = ulm.addNode(newColumn, tabId, null);

            // add the channel
            node = ulm.addNode(channel, col.getId(), null);
        }

        return node;
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

        // Verify that the user has permission to add this tab
        final IAuthorizationPrincipal authPrincipal = this.getUserPrincipal(per.getUserName());
        if (!authPrincipal.hasPermission(IPermission.PORTAL_SYSTEM, IPermission.ADD_TAB_ACTIVITY, IPermission.ALL_TARGET)) {
            log.warn("Attempt to add a tab through the REST API by unauthorized user '" + per.getUserName() + "'");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView("jsonView", Collections.singletonMap("error", "Add tab disabled"));
        }

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
        } catch (PortalException e) {
            return handlePersistError(request, response, e);
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
        } catch (PortalException e) {
            return handlePersistError(request, response, e);
        }

        return new ModelAndView("jsonView", Collections.singletonMap("tabId", tabId));
    }

    /**
     * Add a new folder to the layout.
     *
     * @param request
     * @param response
     * @param targetId - id of the folder node to add the new folder to. By default, the folder will be inserted after other
     *                   existing items in the node unless a siblingId is provided.
     * @param siblingId - if set, insert new folder prior to the node with this id, otherwise simple insert at the end of the list.
     * @param attributes - if included, parse the JSON name-value pairs in the body as the attributes of the folder. These
     *                     will override the defaults.
     * e.g. :
     * {
     *      "structureAttributes" : {"display" : "row", "other" : "another" },
     *      "attributes" : {"hidden": "true", "type" : "header-top" }
     * }
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=addFolder")
    public ModelAndView addFolder(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestParam("targetId") String targetId,
                                  @RequestParam(value="siblingId", required=false) String siblingId,
                                  @RequestParam(value="display", required=false) String display,
                                  @RequestBody(required=false) Map<String, Map<String, String>> attributes) {
        IUserLayoutManager ulm = userInstanceManager.getUserInstance(request).getPreferencesManager().getUserLayoutManager();
        final Locale locale = RequestContextUtils.getLocale(request);

        if (!ulm.getNode(targetId).isAddChildAllowed()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView("jsonView", Collections.singletonMap("error", getMessage("error.add.element", "Unable to add element", locale)));
        }

        UserLayoutFolderDescription newFolder = new UserLayoutFolderDescription();
        newFolder.setHidden(false);
        newFolder.setImmutable(false);
        newFolder.setAddChildAllowed(true);
        newFolder.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);

        // Update the attributes based on the supplied JSON (optional request body name-value pairs)
        if (attributes != null && !attributes.isEmpty()) {
            setObjectAttributes(newFolder, request, attributes);
        }

        ulm.addNode(newFolder, targetId, siblingId);

        try {
            ulm.saveUserLayout();
        } catch (PortalException e) {
            return handlePersistError(request, response, e);
        }

        Map<String, Object> model = new HashMap<>();
        model.put("response", getMessage("success.add.folder", "Added a new folder", locale));
        model.put("folderId", newFolder.getId());
        model.put("immutable", newFolder.isImmutable());
        return new ModelAndView("jsonView", model);
    }

    /**
     * Attempt to map the attribute values to the given object.
     * @param node
     * @param request
     * @param attributes
     */
    private void setObjectAttributes(IUserLayoutNodeDescription node, HttpServletRequest request, Map<String, Map<String, String>> attributes) {
        // Attempt to set the object attributes
        for(String name : attributes.get("attributes").keySet()) {
            try {
              BeanUtils.setProperty(node, name, attributes.get(name));
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                log.warn("Unable to set attribute: " + name + "on object of type: " + node.getType());
            }
        }

        // Set the structure-attributes, whatever they may be
        Map<String, String> structureAttributes = attributes.get("structureAttributes");
        if (structureAttributes != null) {
            for(String name : structureAttributes.keySet()) {
                this.stylesheetUserPreferencesService.setLayoutAttribute(request,
                                                                         PreferencesScope.STRUCTURE,
                                                                         node.getId(),
                                                                         name,
                                                                         structureAttributes.get(name));
            }
        }
    }

    /**
     * Update the attributes for the node. Unrecognized attributes will log a warning, but are otherwise ignored.
     *
     * @param request
     * @param response
     * @param targetId - the id of the node whose attributes will be updated.
     * @param attributes - parse the JSON name-value pairs in the body as the attributes of the folder.
     * e.g. :
     * {
     *      "structureAttributes" : {"display" : "row", "other" : "another" },
     *      "attributes" : {"hidden": "true", "type" : "header-top" }
     * }
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=updateAttributes")
    public ModelAndView updateAttributes(HttpServletRequest request,
                                         HttpServletResponse response,
                                         @RequestParam("targetId") String targetId,
                                         @RequestBody Map<String, Map<String, String>> attributes) {
        IUserLayoutManager ulm = userInstanceManager.getUserInstance(request).getPreferencesManager().getUserLayoutManager();

        if (!ulm.getNode(targetId).isEditAllowed()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView("jsonView", Collections.singletonMap("error", getMessage("error.element.update", "Unable to update element", RequestContextUtils.getLocale(request))));
        }

        // Update the attributes based on the supplied JSON (request body name-value pairs)
        IUserLayoutNodeDescription node = ulm.getNode(targetId);
        if (node == null) {
            log.warn("[updateAttributes()] Unable to locate node with id: " + targetId);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ModelAndView("jsonView", Collections.singletonMap("error", "Unable to locate node with id: " + targetId));
        } else {
            setObjectAttributes(node, request, attributes);

            final Locale locale = RequestContextUtils.getLocale(request);
            try {
                ulm.saveUserLayout();
            } catch (PortalException e) {
                return handlePersistError(request, response, e);
            }

            Map<String, String> model = Collections.singletonMap("success", getMessage("success.element.update", "Updated element attributes", locale));
            return new ModelAndView("jsonView", model);
        }
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView("jsonView", Collections.singletonMap("error", getMessage("error.element.update", "Unable to update element", RequestContextUtils.getLocale(request))));
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
            } catch (PortalException e) {
                return handlePersistError(request, response, e);
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
            return new ModelAndView("jsonView", Collections.singletonMap("error", "Invalid node id " + elementId));
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
        } catch (PortalException e) {
            return handlePersistError(request, response, e);
        }

        return new ModelAndView("jsonView", Collections.EMPTY_MAP);

    }

    private ModelAndView handlePersistError(HttpServletRequest request, HttpServletResponse response, Exception e) {
        log.warn("Error saving layout", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new ModelAndView("jsonView", Collections.singletonMap("error",
                getMessage("error.persisting.attribute.change", "Unable to save attribute changes",
                RequestContextUtils.getLocale(request))));
    }

    protected void removeSubscription(IPerson per, String elementId, IUserLayoutManager ulm) {

        // get the fragment owner's ID from the element string
        String userIdString = StringUtils.substringBetween(elementId, Constants.FRAGMENT_ID_USER_PREFIX, Constants.FRAGMENT_ID_LAYOUT_PREFIX);
        int userId = NumberUtils.toInt(userIdString, 0);

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
     * Syntactic sugar for safely resolving a no-args message from message bundle.
     * @param key Message bundle key
     * @param defaultMessage Ready-to-present message to fall back upon.
     * @param locale desired locale
     * @return Resolved interpolated message or defaultMessage.
     */
    protected String getMessage(String key, String defaultMessage, Locale locale) {
        try {
            return messageSource.getMessage(key, new Object[] {}, defaultMessage, locale);
        } catch (Exception e) {
            // sadly, messageSource.getMessage can throw e.g. when message is ill formatted.
            log.error("Error resolving message with key {}.", key, e);
            return defaultMessage;
        }
    }

    /**
     * Syntactic sugar for safely resolving a one-arg message from message bundle.
     * @param key Message bundle key
     * @param argument dynamic value to be interpolated
     * @param defaultMessage Ready-to-present message to fall back upon.
     * @param locale desired locale
     * @return Resolved interpolated message or defaultMessage.
     */
    protected String getMessage(String key, String argument, String defaultMessage, Locale locale) {
        try {
            return messageSource.getMessage(key, new String[] {argument}, defaultMessage, locale);
        } catch (Exception e) {
            // sadly, messageSource.getMessage can throw e.g. when message is ill formatted.
            log.error("Error resolving message with key {}.", key, e);
            return defaultMessage;
        }
    }

    protected IAuthorizationPrincipal getUserPrincipal(final String userName) {
        final IEntity user = GroupService.getEntity(userName, IPerson.class);
        if (user == null) {
            return null;
        }

        final AuthorizationService authService = AuthorizationService.instance();
        return authService.newPrincipal(user);
    }

    /**
     * A folder is a column if its parent is a tab element
     *
     * @param folder the folder in question
     * @return <code>true</code> if the folder is a column, otherwise <code>false</code>
     */
    protected boolean isColumn(IUserLayoutManager ulm, String folderId) throws PortalException {
        return isTab(ulm, ulm.getParentId(folderId));
    }

    protected String getTabIdFromName(IUserLayout userLayout, String tabName) {
        @SuppressWarnings("unchecked")
        Enumeration<String> childrenOfRoot = userLayout.getChildIds(userLayout.getRootId());

        while (childrenOfRoot.hasMoreElements()) { //loop over folders that might be the favorites folder
            String nodeId = childrenOfRoot.nextElement();

            try {

                IUserLayoutNodeDescription nodeDescription = userLayout.getNodeDescription(nodeId);
                IUserLayoutNodeDescription.LayoutNodeType nodeType = nodeDescription.getType();

                if (FOLDER.equals(nodeType)
                        && nodeDescription instanceof IUserLayoutFolderDescription) {
                    IUserLayoutFolderDescription folderDescription = (IUserLayoutFolderDescription) nodeDescription;

                    if (tabName.equalsIgnoreCase(folderDescription.getName())) {
                        return folderDescription.getId();
                    }
                }
            } catch (Exception e) {
                log.error("Error getting the nodeID of the tab name " + tabName, e);
            }
        }

        log.warn("Tab " + tabName + " was searched for but not found");
        return null; //didn't find tab
    }

    /**
     * If the destination is a tab, the new element automatically goes to the end of the first column.
     *
     * Otherwise we check that the destination is a folder. If it is not and we aren't just trying to insert before it,
     * the operation fails. If we haven't failed, if the "method" param is "insertBefore", we insert before the destination
     * node, otherwise it goes to the end of that folder.
     * @return
     */
    private boolean moveElementInternal(HttpServletRequest request,
                                        String sourceId,
                                        String destinationId,
                                        String method) {
      if(StringUtils.isEmpty(destinationId)) {//shortcut for beginning and end
        return true;
      }
      IUserInstance ui = userInstanceManager.getUserInstance(request);
      UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
      IUserLayoutManager ulm = upm.getUserLayoutManager();

      if (isTab(ulm, destinationId)) {
          // if the target is a tab type node, move the element to
          // the end of the first column
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

      } else {
          boolean isInsert = method != null && method.equals("insertBefore");
          if (isFolder(ulm, destinationId)) {
              ulm.moveNode(sourceId, destinationId, null);
          } else {
              String siblingId = isInsert ? destinationId : null;

              if (!ulm.moveNode(sourceId, ulm.getParentId(destinationId), siblingId)) {
                  log.info("moveNode returned false. Aborting node movement");
                  return false;
              }
          }
      }

      try {
          ulm.saveUserLayout();
      } catch (PortalException e) {
          log.warn("Error saving layout", e);
          return false;
      }

      return true;
    }

    private boolean isFolder(IUserLayoutManager ulm, String id) {
        return ulm.getNode(id).getType().equals(IUserLayoutNodeDescription.LayoutNodeType.FOLDER);
    }
}
