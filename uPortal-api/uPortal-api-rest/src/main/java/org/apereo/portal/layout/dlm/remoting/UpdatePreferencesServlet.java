/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout.dlm.remoting;

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
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.IUserIdentityStore;
import org.apereo.portal.PortalException;
import org.apereo.portal.UserPreferencesManager;
import org.apereo.portal.groups.IEntity;
import org.apereo.portal.layout.IStylesheetUserPreferencesService;
import org.apereo.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;
import org.apereo.portal.layout.IUserLayout;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.IUserLayoutStore;
import org.apereo.portal.layout.PortletSubscribeIdResolver;
import org.apereo.portal.layout.dlm.MissingPortletDefinition;
import org.apereo.portal.layout.dlm.UserPrefsHandler;
import org.apereo.portal.layout.node.IUserLayoutChannelDescription;
import org.apereo.portal.layout.node.IUserLayoutFolderDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.layout.node.UserLayoutChannelDescription;
import org.apereo.portal.layout.node.UserLayoutFolderDescription;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.portlets.favorites.FavoritesUtils;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.PermissionHelper;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.apereo.portal.utils.personalize.IPersonalizer;
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
import org.w3c.dom.Element;

/** Provides targets for AJAX preference setting calls. */
@Controller
@RequestMapping("/layout")
public class UpdatePreferencesServlet {

    private static final String TAB_GROUP_PARAMETER = "tabGroup"; // matches incoming JS
    private static final String TAB_GROUP_DEFAULT =
            "DEFAULT_TABGROUP"; // matches default in structure transform

    private static final String CLASSIC_COLUMNS_WIDTH_USER_PREFERENCE_NAME = "width";
    private static final String FLEX_COLUMNS_COUNT_USER_PREFERENCE_NAME = "flexColumns";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IAuthorizationService authorizationService;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IUserIdentityStore userIdentityStore;
    private IUserInstanceManager userInstanceManager;
    private IStylesheetUserPreferencesService stylesheetUserPreferencesService;
    private IUserLayoutStore userLayoutStore;
    private MessageSource messageSource;
    private IPortletWindowRegistry portletWindowRegistry;
    private FavoritesUtils favoritesUtils;
    private IPersonalizer personalizer;

    @Value("${org.apereo.portal.layout.dlm.remoting.addedWindowState:null}")
    private String addedPortletWindowState;

    private WindowState addedWindowState;

    @PostConstruct
    private void initAddedPortletWindowState() {
        if (addedPortletWindowState != null
                && !"null".equalsIgnoreCase(addedPortletWindowState)
                && !addedPortletWindowState.isEmpty()) {
            addedWindowState = new WindowState(addedPortletWindowState);
        }
    }

    @Autowired
    public void setAuthorizationService(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Autowired
    public void setUserLayoutStore(IUserLayoutStore userLayoutStore) {
        this.userLayoutStore = userLayoutStore;
    }

    @Autowired
    public void setStylesheetUserPreferencesService(
            IStylesheetUserPreferencesService stylesheetUserPreferencesService) {
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

    @Autowired
    public void setFavoritesUtils(FavoritesUtils favoritesUtils) {
        this.favoritesUtils = favoritesUtils;
    }

    @Autowired
    public void setPersonalizer(IPersonalizer personalizer) {
        this.personalizer = personalizer;
    }

    /** Default name given to newly created tabs. */
    protected static final String DEFAULT_TAB_NAME = "New Tab"; // TODO: Requires i18n!

    /**
     * Remove an element from the layout.
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=removeElement")
    public ModelAndView removeElement(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);
        IPerson per = getPerson(ui, response);

        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        try {

            // Delete the requested element node.  This code is the same for
            // all node types, so we can just have a generic action.
            String elementId = request.getParameter("elementID");
            if (!ulm.deleteNode(elementId)) {
                logger.info(
                        "Failed to remove element ID {} from layout root folder ID {}, delete node returned false",
                        elementId,
                        ulm.getRootFolderId());
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return new ModelAndView(
                        "jsonView",
                        Collections.singletonMap(
                                "error",
                                getMessage(
                                        "error.element.update",
                                        "Unable to update element",
                                        RequestContextUtils.getLocale(request))));
            }

            ulm.saveUserLayout();

            return new ModelAndView("jsonView", Collections.emptyMap());

        } catch (PortalException e) {
            return handlePersistError(request, response, e);
        }
    }

    /**
     * Remove the first element with the provided fname from the layout.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param fname fname of the portlet to remove from the layout
     * @return json response
     * @throws IOException if the person cannot be retrieved
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=removeByFName")
    public ModelAndView removeByFName(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "fname") String fname)
            throws IOException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);

        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        try {
            String elementId =
                    ulm.getUserLayout().findNodeId(new PortletSubscribeIdResolver(fname));
            if (elementId != null) {
                // Delete the requested element node.  This code is the same for
                // all node types, so we can just have a generic action.
                if (!ulm.deleteNode(elementId)) {
                    logger.info(
                            "Failed to remove element ID {} from layout root folder ID {}, delete node returned false",
                            elementId,
                            ulm.getRootFolderId());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return new ModelAndView(
                            "jsonView",
                            Collections.singletonMap(
                                    "error",
                                    getMessage(
                                            "error.element.update",
                                            "Unable to update element",
                                            RequestContextUtils.getLocale(request))));
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }

            ulm.saveUserLayout();

            return new ModelAndView("jsonView", Collections.emptyMap());

        } catch (PortalException e) {
            return handlePersistError(request, response, e);
        }
    }

    /**
     * Moves the portlet either before nextNodeId or after previousNodeId as appropriate.
     *
     * @param request HttpRequest
     * @param response HttpResponse
     * @param sourceId nodeId to move
     * @param previousNodeId if nextNodeId is not blank, moves portlet to end of list previousNodeId
     *     is in
     * @param nextNodeId nodeId to insert sourceId before.
     * @return JSON containing a success message or an error message
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=movePortletAjax")
    public ModelAndView movePortletAjax(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam String sourceId,
            @RequestParam String previousNodeId,
            @RequestParam String nextNodeId) {
        final Locale locale = RequestContextUtils.getLocale(request);
        boolean success = false;
        if (StringUtils.isNotBlank(nextNodeId)) {
            success = moveElementInternal(request, sourceId, nextNodeId, "insertBefore");
        } else {
            success = moveElementInternal(request, sourceId, previousNodeId, "appendAfter");
        }
        if (success) {
            return new ModelAndView(
                    "jsonView",
                    Collections.singletonMap(
                            "response",
                            getMessage(
                                    "success.move.element", "Element moved successfully", locale)));
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView(
                    "jsonView",
                    Collections.singletonMap(
                            "response",
                            getMessage("error.move.element", "Error moving element.", locale)));
        }
    }

    /**
     * Move a portlet to another location on the tab.
     *
     * <p>This deprecated method is replaced by the method/action "moveElement". The code is the
     * same, but the naming better abstracts the action. This method is here for backwards
     * compatibility with anything using the "movePortlet" action of the API.
     *
     * <p>Used by Respondr UI when moving portlets around in content area. uPortal 4.2 and prior
     * behavior:
     *
     * <ul>
     *   <li>If destination is a tab either adds to end of 1st column or if no columns, creates one
     *       and adds it. AFAIK this was not actually used by the UI.
     *   <li>If target is a column (2 down from root), portlet always added to end of column. Used
     *       by UI to drop portlet into empty column (UI did insertBefore with elementId=columnId)
     *   <li>If method=insertBefore does insert before elementId (always a portlet in 4.2).
     *   <li>If method=appendAfter does append at end of parent(elementId), result of which is a
     *       column. Used by UI to add to end of column (elementId is last portlet in column).
     * </ul>
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws PortalException
     * @deprecated - replaced by the method/action "moveElement"
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=movePortlet")
    @Deprecated
    public ModelAndView movePortlet(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "sourceID") String sourceId,
            @RequestParam String method,
            @RequestParam(value = "elementID") String destinationId)
            throws IOException, PortalException {
        return moveElement(request, response, sourceId, method, destinationId);
    }

    /**
     * Move an element to another location on the tab.
     *
     * <p>Used by Respondr UI when moving portlets around in content area. Will be made more generic
     * to support ngPortal UI which supports arbitrary nesting of folders. When that code is merged
     * in, the behavior of this method will need to change slightly (make sure movePortlet behavior
     * doesn't change though). Current behavior:
     *
     * <ul>
     *   <li>If destination is a tab either adds to end of 1st column or if no columns, creates one
     *       and adds it. AFAIK this was not actually used by the UI.
     *   <li>If target is a column (2 down from root), portlet always added to end of column. Used
     *       by UI to drop portlet into empty column (UI did insertBefore with elementId=columnId)
     *   <li>If method=insertBefore does insert before elementId (always a portlet in 4.2).
     *   <li>If method=appendAfter does append at end of parent(elementId), result of which is a
     *       column. Used by UI to add to end of column (elementId is last portlet in column).
     * </ul>
     *
     * @param request
     * @param response
     * @param sourceId id of the element to move
     * @param method insertBefore or appendAfter
     * @param destinationId Id of element. If a tab, sourceID added to end of a folder/column in the
     *     tab. If a folder, sourceID added to the end of the folder. Otherwise sourceID added
     *     before elementID.
     * @throws IOException
     * @throws PortalException
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=moveElement")
    public ModelAndView moveElement(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "sourceID") String sourceId,
            @RequestParam String method,
            @RequestParam(value = "elementID") String destinationId)
            throws IOException, PortalException {
        final Locale locale = RequestContextUtils.getLocale(request);

        if (moveElementInternal(request, sourceId, destinationId, method)) {
            return new ModelAndView(
                    "jsonView",
                    Collections.singletonMap(
                            "response",
                            getMessage(
                                    "success.move.element", "Element moved successfully", locale)));
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView(
                    "jsonView",
                    Collections.singletonMap(
                            "response",
                            getMessage("error.move.element", "Error moving element", locale)));
        }
    }

    /**
     * Change the number of columns on a specified tab. In the event that the user is decreasing the
     * number of columns, extra columns will be stripped from the right-hand side. Any channels in
     * these columns will be moved to the bottom of the last preserved column.
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
    public ModelAndView changeColumns(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("tabId") String tabId,
            @RequestParam("widths[]") String[] widths,
            @RequestParam(value = "deleted[]", required = false) String[] deleted,
            @RequestParam(value = "acceptor", required = false) String acceptor)
            throws IOException, PortalException {

        final IUserInstance ui = userInstanceManager.getUserInstance(request);
        final IPerson person = getPerson(ui, response);

        final UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        final IUserLayoutManager ulm = upm.getUserLayoutManager();

        final int newColumnCount = widths.length;

        // build a list of the current columns for this tab
        final Enumeration<String> columns = ulm.getChildIds(tabId);
        final List<String> columnList = new ArrayList<>();
        while (columns.hasMoreElements()) {
            columnList.add(columns.nextElement());
        }
        final int oldColumnCount = columnList.size();

        final Map<String, Object> model = new HashMap<>();

        // if the new layout has more columns
        if (newColumnCount > oldColumnCount) {
            final List<String> newColumnIds = new ArrayList<>();
            for (int i = columnList.size(); i < newColumnCount; i++) {

                // create new column element
                final IUserLayoutFolderDescription newColumn = new UserLayoutFolderDescription();
                newColumn.setName("Column");
                newColumn.setId("tbd");
                newColumn.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
                newColumn.setHidden(false);
                newColumn.setUnremovable(false);
                newColumn.setImmutable(false);

                // add the column to our layout
                final IUserLayoutNodeDescription node = ulm.addNode(newColumn, tabId, null);
                newColumnIds.add(node.getId());

                model.put("newColumnIds", newColumnIds);
                columnList.add(node.getId());
            }

        }

        // if the new layout has fewer columns
        else if (deleted != null && deleted.length > 0) {

            for (String columnId : deleted) {

                // move all channels in the current column to the last valid column
                final Enumeration channels = ulm.getChildIds(columnId);
                while (channels.hasMoreElements()) {
                    ulm.addNode(ulm.getNode((String) channels.nextElement()), acceptor, null);
                }

                // delete the column from the user's layout
                ulm.deleteNode(columnId);

                columnList.remove(columnId);
            }
        }

        /*
         * Now that the number of columns is established, the appearance and behavior of the columns
         * on the page is determined by Structure Stylesheet User Preferences.  "Classic" uPortal
         * columns use a 'width' attribute, while the new(er) columns based on CSS Flex use a
         * 'flexColumns' attribute.
         */
        resetColumnStylesheetUserPreferences(
                request, person, ulm, columnList); // Clear previous selections

        // Choose a column layout strategy...
        boolean useFlexStrategy = false; // default is "classic"
        if (columnList.size() == 1) {
            int firstColumnWidthAsInt = Integer.parseInt(widths[0]);
            if (firstColumnWidthAsInt > 100) {
                // A single column with a width parameter creater than 100(%)
                // signals that we want the flexColumns strategy.
                useFlexStrategy = true;
            }
        }

        // Update Structure Stylesheet User Preferences based on the selected strategy
        if (useFlexStrategy) {
            updateColumnStylesheetUserPreferencesFlex(request, person, ulm, columnList, widths);
        } else {
            updateColumnStylesheetUserPreferencesClassic(request, person, ulm, columnList, widths);
        }

        try {
            ulm.saveUserLayout();
        } catch (PortalException e) {
            logger.warn("Error saving layout", e);
        }

        return new ModelAndView("jsonView", model);
    }

    /**
     * Move a tab left or right.
     *
     * @param sourceId node ID of tab to move
     * @param method insertBefore or appendAfter. If appendAfter, tab is added as last tab (parent
     *     of destinationId).
     * @param destinationId insertBefore: node ID of tab to move sourceId before. insertAfter: node
     *     ID of another tab
     * @param request
     * @param response
     * @throws PortalException
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=moveTab")
    public ModelAndView moveTab(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "sourceID") String sourceId,
            @RequestParam String method,
            @RequestParam(value = "elementID") String destinationId)
            throws IOException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);

        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();
        final Locale locale = RequestContextUtils.getLocale(request);

        // If we're moving this element before another one, we need
        // to know what the target is. If there's no target, just
        // assume we're moving it to the very end of the list.
        String siblingId = null;
        if ("insertBefore".equals(method)) siblingId = destinationId;

        try {
            // move the node as requested and save the layout
            if (!ulm.moveNode(sourceId, ulm.getParentId(destinationId), siblingId)) {
                logger.warn("Failed to move tab in user layout. moveNode returned false");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return new ModelAndView(
                        "jsonView",
                        Collections.singletonMap(
                                "response",
                                getMessage(
                                        "error.move.tab",
                                        "There was an issue moving the tab, please refresh the page and try again.",
                                        locale)));
            }
            ulm.saveUserLayout();
        } catch (PortalException e) {
            return handlePersistError(request, response, e);
        }

        return new ModelAndView(
                "jsonView",
                Collections.singletonMap(
                        "response",
                        getMessage("success.move.tab", "Tab moved successfully", locale)));
    }

    @RequestMapping(method = RequestMethod.POST, params = "action=addFavorite")
    public ModelAndView addFavorite(
            @RequestParam String channelId,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        final IUserInstance ui = userInstanceManager.getUserInstance(request);
        final IPerson person = getPerson(ui, response);
        final IPortletDefinition pdef = portletDefinitionRegistry.getPortletDefinition(channelId);
        final Locale locale = RequestContextUtils.getLocale(request);

        final IAuthorizationPrincipal authPrincipal = this.getUserPrincipal(person.getUserName());
        final String targetString = PermissionHelper.permissionTargetIdForPortletDefinition(pdef);
        if (!authPrincipal.hasPermission(
                IPermission.PORTAL_SYSTEM, IPermission.PORTLET_FAVORITE_ACTIVITY, targetString)) {
            logger.warn(
                    "Unauthorized attempt to favorite portlet '{}' through the REST API by user '{}'",
                    pdef.getFName(),
                    person.getUserName());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView(
                    "jsonView",
                    Collections.singletonMap(
                            "response",
                            getMessage(
                                    "error.favorite.not.permitted",
                                    "Favorite not permitted",
                                    locale)));
        }

        final UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        final IUserLayoutManager ulm = upm.getUserLayoutManager();

        final IUserLayoutChannelDescription channel =
                new UserLayoutChannelDescription(person, pdef, request.getSession(), personalizer);

        // get favorite tab
        final String favoriteTabNodeId = favoritesUtils.getFavoriteTabNodeId(ulm.getUserLayout());

        if (favoriteTabNodeId != null) {
            removeOrphanedFavoritesFromLayout(ulm);

            // add new favorite portlet to favorite tab
            final IUserLayoutNodeDescription node = addNodeToTab(ulm, channel, favoriteTabNodeId);

            if (node == null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return new ModelAndView(
                        "jsonView",
                        Collections.singletonMap(
                                "response",
                                getMessage(
                                        "error.add.portlet.in.tab",
                                        "Can''t add a new favorite",
                                        locale)));
            }

            try {
                // save the user's layout
                ulm.saveUserLayout();
            } catch (PortalException e) {
                return handlePersistError(request, response, e);
            }

            // document success for notifications
            final Map<String, String> model = new HashMap<>();
            final String channelTitle = channel.getTitle();
            model.put(
                    "response",
                    getMessage(
                            "favorites.added.favorite",
                            channelTitle,
                            "Added " + channelTitle + " as a favorite.",
                            locale));
            model.put("newNodeId", node.getId());
            return new ModelAndView("jsonView", model);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ModelAndView(
                    "jsonView",
                    Collections.singletonMap(
                            "response",
                            getMessage(
                                    "error.finding.favorite.tab",
                                    "Can''t find favorite tab",
                                    locale)));
        }
    }

    /**
     * This method removes the channelId specified from favorites. Note that even if you pass in the
     * layout channel id, it will always remove from the favorites.
     *
     * @param channelId The long channel ID that is used to determine which fname to remove from
     *     favorites
     * @param request
     * @param response
     * @return returns a mav object with a response attribute for noty
     * @throws IOException if it has problem reading the layout file.
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=removeFavorite")
    public ModelAndView removeFavorite(
            @RequestParam String channelId,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        UserPreferencesManager upm =
                (UserPreferencesManager)
                        userInstanceManager.getUserInstance(request).getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();
        final Locale locale = RequestContextUtils.getLocale(request);
        IPortletDefinition portletDefinition =
                portletDefinitionRegistry.getPortletDefinition(channelId);

        if (portletDefinition != null && StringUtils.isNotBlank(portletDefinition.getFName())) {
            String functionalName = portletDefinition.getFName();
            List<IUserLayoutNodeDescription> favoritePortlets =
                    favoritesUtils.getFavoritePortletLayoutNodes(ulm.getUserLayout());

            // Retrieve favorites matching functional name from list of favorites
            List<IUserLayoutChannelDescription> favoritesToDelete =
                    FavoritesUtils.getDuplicateFavoritesByFNameToDelete(
                            favoritePortlets, functionalName);

            String resp = new String();

            removeOrphanedFavoritesFromLayout(ulm, favoritePortlets);

            boolean saveNeeded = false;
            for (IUserLayoutChannelDescription deleteNode : favoritesToDelete) {
                if (deleteNode != null) {
                    try {
                        if (!ulm.deleteNode(deleteNode.getChannelSubscribeId())) {

                            logger.warn(
                                    "Error deleting the node {} from favorites for user {}",
                                    channelId,
                                    (upm.getPerson() == null
                                            ? "unknown"
                                            : upm.getPerson().getID()));

                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            // load fail message
                            resp =
                                    getMessage(
                                            "error.remove.favorite",
                                            "Can''t remove favorite",
                                            locale);
                        } else {
                            saveNeeded = true;
                            // load success message
                        }
                    } catch (PortalException e) {
                        return handlePersistError(request, response, e);
                    }
                }
                if (saveNeeded) {
                    try {
                        ulm.saveUserLayout();
                    } catch (PortalException e) {
                        return handlePersistError(request, response, e);
                    }
                    resp =
                            getMessage(
                                    "success.remove.portlet",
                                    "Removed from Favorites successfully",
                                    locale);
                }
            }

            return new ModelAndView("jsonView", Collections.singletonMap("response", resp));
        }

        return new ModelAndView(
                "jsonView",
                Collections.singletonMap(
                        "response",
                        getMessage("error.finding.favorite", "Can''t find favorite", locale)));
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
    public ModelAndView addPortlet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, PortalException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();
        final Locale locale = RequestContextUtils.getLocale(request);

        // gather the parameters we need to move a channel
        String destinationId = request.getParameter("elementID");
        String sourceId = request.getParameter("channelID");
        String method = request.getParameter("position");
        String fname = request.getParameter("fname");

        if (destinationId == null) {
            String tabName = request.getParameter("tabName");
            if (tabName != null) {
                destinationId = getTabIdFromName(ulm.getUserLayout(), tabName);
            }
        }

        IPortletDefinition definition;
        if (sourceId != null) definition = portletDefinitionRegistry.getPortletDefinition(sourceId);
        else if (fname != null)
            definition = portletDefinitionRegistry.getPortletDefinitionByFname(fname);
        else {
            logger.error("SourceId or fname invalid when adding a portlet");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ModelAndView(
                    "jsonView", Collections.singletonMap("error", "SourceId or fname invalid"));
        }

        IUserLayoutChannelDescription channel =
                new UserLayoutChannelDescription(
                        getPerson(ui, response), definition, request.getSession(), personalizer);

        IUserLayoutNodeDescription node;
        if (isTab(ulm, destinationId)) {
            node = addNodeToTab(ulm, channel, destinationId);

        } else {
            boolean isInsert = method != null && method.equals("insertBefore");

            // If neither an insert or type folder - Can't "insert into" non-folder
            if (!(isInsert || isFolder(ulm, destinationId))) {
                logger.error("Cannot insert into portlet element");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return new ModelAndView(
                        "jsonView",
                        Collections.singletonMap("error", "Cannot insert into portlet element"));
            }

            String siblingId = isInsert ? destinationId : null;
            String target = isInsert ? ulm.getParentId(destinationId) : destinationId;

            // move the channel into the column
            node = ulm.addNode(channel, target, siblingId);
        }

        if (node == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView(
                    "jsonView",
                    Collections.singletonMap(
                            "error",
                            getMessage("error.add.element", "Unable to add element", locale)));
        }

        String nodeId = node.getId();

        try {
            // save the user's layout
            ulm.saveUserLayout();
            if (addedWindowState != null) {
                IPortletWindow portletWindow =
                        this.portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(
                                request, channel.getFunctionalName());
                portletWindow.setWindowState(addedWindowState);
                this.portletWindowRegistry.storePortletWindow(request, portletWindow);
            }
        } catch (PortalException e) {
            return handlePersistError(request, response, e);
        }

        Map<String, String> model = new HashMap<>();
        model.put("response", getMessage("success.add.portlet", "Added a new channel", locale));
        model.put("newNodeId", nodeId);
        return new ModelAndView("jsonView", model);
    }

    /**
     * Update the user's preferred skin.
     *
     * @param request HTTP Request
     * @param skinName name of the Skin
     * @throws IOException
     * @throws PortalException
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=chooseSkin")
    public ModelAndView chooseSkin(HttpServletRequest request, @RequestParam String skinName)
            throws IOException {

        this.stylesheetUserPreferencesService.setStylesheetParameter(
                request, PreferencesScope.THEME, "skin", skinName);

        return new ModelAndView("jsonView", Collections.emptyMap());
    }

    /**
     * Add a new tab to the layout. The new tab will be appended to the end of the list and named
     * with the BLANK_TAB_NAME variable.
     *
     * @param request
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=addTab")
    public ModelAndView addTab(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("widths[]") String[] widths)
            throws IOException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);
        IPerson person = getPerson(ui, response);
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        // Verify that the user has permission to add this tab
        final IAuthorizationPrincipal authPrincipal = this.getUserPrincipal(person.getUserName());
        if (!authPrincipal.hasPermission(
                IPermission.PORTAL_SYSTEM, IPermission.ADD_TAB_ACTIVITY, IPermission.ALL_TARGET)) {
            logger.warn(
                    "Attempt to add a tab through the REST API by unauthorized user '"
                            + person.getUserName()
                            + "'");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView(
                    "jsonView", Collections.singletonMap("error", "Add tab disabled"));
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
            newColumn.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
            newColumn.setHidden(false);
            newColumn.setUnremovable(false);
            newColumn.setImmutable(false);

            // add the column to our layout
            ulm.addNode(newColumn, tabId, null);

            this.stylesheetUserPreferencesService.setLayoutAttribute(
                    request,
                    PreferencesScope.STRUCTURE,
                    newColumn.getId(),
                    CLASSIC_COLUMNS_WIDTH_USER_PREFERENCE_NAME,
                    width + "%");
            try {
                // This sets the column attribute in memory but doesn't persist it.  Comment says
                // saves changes "prior to persisting"
                Element folder = ulm.getUserLayoutDOM().getElementById(newColumn.getId());
                UserPrefsHandler.setUserPreference(
                        folder, CLASSIC_COLUMNS_WIDTH_USER_PREFERENCE_NAME, person);
            } catch (Exception e) {
                logger.error("Error saving new column widths", e);
            }
        }

        // ## 'tabGroup' value (optional feature)
        // Set the 'tabGroup' attribute on the folder element that describes
        // this new tab;  use the currently active tabGroup.
        if (request.getParameter(TAB_GROUP_PARAMETER) != null) {

            String tabGroup = request.getParameter(TAB_GROUP_PARAMETER).trim();
            if (logger.isDebugEnabled()) {
                logger.debug(TAB_GROUP_PARAMETER + "=" + tabGroup);
            }

            if (!TAB_GROUP_DEFAULT.equals(tabGroup) && tabGroup.length() != 0) {
                // Persists SSUP values to the database
                this.stylesheetUserPreferencesService.setLayoutAttribute(
                        request, PreferencesScope.STRUCTURE, tabId, TAB_GROUP_PARAMETER, tabGroup);
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
     * @param targetId - id of the folder node to add the new folder to. By default, the folder will
     *     be inserted after other existing items in the node unless a siblingId is provided.
     * @param siblingId - if set, insert new folder prior to the node with this id, otherwise simple
     *     insert at the end of the list.
     * @param attributes - if included, parse the JSON name-value pairs in the body as the
     *     attributes of the folder. These will override the defaults. e.g. : {
     *     "structureAttributes" : {"display" : "row", "other" : "another" }, "attributes" :
     *     {"hidden": "true", "type" : "header-top" } }
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=addFolder")
    public ModelAndView addFolder(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("targetId") String targetId,
            @RequestParam(value = "siblingId", required = false) String siblingId,
            @RequestBody(required = false) Map<String, Map<String, String>> attributes) {
        IUserLayoutManager ulm =
                userInstanceManager
                        .getUserInstance(request)
                        .getPreferencesManager()
                        .getUserLayoutManager();
        final Locale locale = RequestContextUtils.getLocale(request);

        if (!ulm.getNode(targetId).isAddChildAllowed()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView(
                    "jsonView",
                    Collections.singletonMap(
                            "error",
                            getMessage("error.add.element", "Unable to add element", locale)));
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
     * Update the attributes for the node. Unrecognized attributes will log a warning, but are
     * otherwise ignored.
     *
     * @param request
     * @param response
     * @param targetId - the id of the node whose attributes will be updated.
     * @param attributes - parse the JSON name-value pairs in the body as the attributes of the
     *     folder. e.g. : { "structureAttributes" : {"display" : "row", "other" : "another" },
     *     "attributes" : {"hidden": "true", "type" : "header-top" } }
     */
    @RequestMapping(method = RequestMethod.POST, params = "action=updateAttributes")
    public ModelAndView updateAttributes(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("targetId") String targetId,
            @RequestBody Map<String, Map<String, String>> attributes) {
        IUserLayoutManager ulm =
                userInstanceManager
                        .getUserInstance(request)
                        .getPreferencesManager()
                        .getUserLayoutManager();

        if (!ulm.getNode(targetId).isEditAllowed()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView(
                    "jsonView",
                    Collections.singletonMap(
                            "error",
                            getMessage(
                                    "error.element.update",
                                    "Unable to update element",
                                    RequestContextUtils.getLocale(request))));
        }

        // Update the attributes based on the supplied JSON (request body name-value pairs)
        IUserLayoutNodeDescription node = ulm.getNode(targetId);
        if (node == null) {
            logger.warn("[updateAttributes()] Unable to locate node with id: " + targetId);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ModelAndView(
                    "jsonView",
                    Collections.singletonMap(
                            "error", "Unable to locate node with id: " + targetId));
        } else {
            setObjectAttributes(node, request, attributes);

            final Locale locale = RequestContextUtils.getLocale(request);
            try {
                ulm.saveUserLayout();
            } catch (PortalException e) {
                return handlePersistError(request, response, e);
            }

            Map<String, String> model =
                    Collections.singletonMap(
                            "success",
                            getMessage(
                                    "success.element.update",
                                    "Updated element attributes",
                                    locale));
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
    public ModelAndView renameTab(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        // element ID of the tab to be renamed
        String tabId = request.getParameter("tabId");
        IUserLayoutFolderDescription tab = (IUserLayoutFolderDescription) ulm.getNode(tabId);

        // desired new name
        String tabName = request.getParameter("tabName");

        if (!ulm.canUpdateNode(tab)) {
            logger.warn("Attempting to rename an immutable tab");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView(
                    "jsonView",
                    Collections.singletonMap(
                            "error",
                            getMessage(
                                    "error.element.update",
                                    "Unable to update element",
                                    RequestContextUtils.getLocale(request))));
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

            // TODO why do we have to do this, shouldn't modifying the layout be enough to trigger a
            // full re-render (layout's cache key changes)
            this.stylesheetUserPreferencesService.setLayoutAttribute(
                    request, PreferencesScope.STRUCTURE, tabId, "name", tabName);
        }

        Map<String, String> model = Collections.singletonMap("message", "saved new tab name");
        return new ModelAndView("jsonView", model);
    }

    @RequestMapping(method = RequestMethod.POST, params = "action=updatePermissions")
    public ModelAndView updatePermissions(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        IUserInstance ui = userInstanceManager.getUserInstance(request);
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        String elementId = request.getParameter("elementID");
        IUserLayoutNodeDescription node = ulm.getNode(elementId);

        if (node == null) {
            logger.warn("Failed to locate node for permissions update");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return new ModelAndView(
                    "jsonView", Collections.singletonMap("error", "Invalid node id " + elementId));
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

        return new ModelAndView("jsonView", Collections.emptyMap());
    }

    private void resetColumnStylesheetUserPreferences(
            HttpServletRequest request,
            IPerson person,
            IUserLayoutManager ulm,
            List<String> columnList) {

        for (String columnId : columnList) {
            // Remove pre-existing "width" user preference...
            stylesheetUserPreferencesService.removeLayoutAttribute(
                    request,
                    PreferencesScope.STRUCTURE,
                    columnId,
                    CLASSIC_COLUMNS_WIDTH_USER_PREFERENCE_NAME);
            // Remove pre-existing "flexColumns" user preference...
            stylesheetUserPreferencesService.removeLayoutAttribute(
                    request,
                    PreferencesScope.STRUCTURE,
                    columnId,
                    FLEX_COLUMNS_COUNT_USER_PREFERENCE_NAME);
        }
    }

    private void updateColumnStylesheetUserPreferencesFlex(
            HttpServletRequest request,
            IPerson person,
            IUserLayoutManager ulm,
            List<String> columnList,
            String[] widths) {

        final int flexColumnsIntValue = Integer.parseInt(widths[0]) - 100;
        String flexColumns; // Allowable values are 6, 4, 3, and 2
        switch (flexColumnsIntValue) {
            case 6:
                flexColumns = "6";
                break;
            case 4:
                flexColumns = "4";
                break;
            case 3:
                flexColumns = "3";
                break;
            default:
                // Rather than risk a disallowed value getting into the DB,
                // we'll convert anything besides 6, 4, or 3 into a 2.
                flexColumns = "2";
                break;
        }
        for (String columnId : columnList) {
            stylesheetUserPreferencesService.setLayoutAttribute(
                    request,
                    PreferencesScope.STRUCTURE,
                    columnId,
                    FLEX_COLUMNS_COUNT_USER_PREFERENCE_NAME,
                    flexColumns);
            try {
                // For fragment-based column nodes, signal to DLM that it will need
                // to replay the user's selection of the "flexColumns" attribute...
                final Element folder = ulm.getUserLayoutDOM().getElementById(columnId);
                UserPrefsHandler.setUserPreference(
                        folder, FLEX_COLUMNS_COUNT_USER_PREFERENCE_NAME, person);
            } catch (Exception e) {
                logger.error("Error saving new column layout", e);
            }
        }
    }

    private void updateColumnStylesheetUserPreferencesClassic(
            HttpServletRequest request,
            IPerson person,
            IUserLayoutManager ulm,
            List<String> columnList,
            String[] widths) {

        int count = 0;
        for (String columnId : columnList) {
            stylesheetUserPreferencesService.setLayoutAttribute(
                    request,
                    PreferencesScope.STRUCTURE,
                    columnId,
                    CLASSIC_COLUMNS_WIDTH_USER_PREFERENCE_NAME,
                    widths[count] + "%");
            try {
                // For fragment-based column nodes, signal to DLM that it will need
                // to replay the user's selection of the "width" attribute...
                final Element folder = ulm.getUserLayoutDOM().getElementById(columnId);
                UserPrefsHandler.setUserPreference(
                        folder, CLASSIC_COLUMNS_WIDTH_USER_PREFERENCE_NAME, person);
            } catch (Exception e) {
                logger.error("Error saving new column layout", e);
            }
            ++count;
        }
    }

    private IUserLayoutNodeDescription addNodeToTab(
            IUserLayoutManager ulm, IUserLayoutChannelDescription channel, String tabId) {
        IUserLayoutNodeDescription node = null;

        Enumeration<String> columns = ulm.getChildIds(tabId);
        if (columns.hasMoreElements()) {
            while (columns.hasMoreElements()) {
                // attempt to add this channel to the column
                node = ulm.addNode(channel, columns.nextElement(), null);
                // if it couldn't be added to this column, go on and try the next
                // one.  otherwise, we're set.
                if (node != null) break;
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
     * Attempt to map the attribute values to the given object.
     *
     * @param node
     * @param request
     * @param attributes
     */
    private void setObjectAttributes(
            IUserLayoutNodeDescription node,
            HttpServletRequest request,
            Map<String, Map<String, String>> attributes) {
        // Attempt to set the object attributes
        for (String name : attributes.get("attributes").keySet()) {
            try {
                BeanUtils.setProperty(node, name, attributes.get(name));
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.warn(
                        "Unable to set attribute: "
                                + name
                                + "on object of type: "
                                + node.getType());
            }
        }

        // Set the structure-attributes, whatever they may be
        Map<String, String> structureAttributes = attributes.get("structureAttributes");
        if (structureAttributes != null) {
            for (String name : structureAttributes.keySet()) {
                this.stylesheetUserPreferencesService.setLayoutAttribute(
                        request,
                        PreferencesScope.STRUCTURE,
                        node.getId(),
                        name,
                        structureAttributes.get(name));
            }
        }
    }

    private ModelAndView handlePersistError(
            HttpServletRequest request, HttpServletResponse response, Exception e) {
        logger.warn("Error saving layout", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new ModelAndView(
                "jsonView",
                Collections.singletonMap(
                        "error",
                        getMessage(
                                "error.persisting.attribute.change",
                                "Unable to save attribute changes",
                                RequestContextUtils.getLocale(request))));
    }

    /**
     * A folder is a tab if its parent element is the layout element
     *
     * @param ulm User Layout Manager
     * @param folderId the folder in question
     * @return <code>true</code> if the folder is a tab, otherwise <code>false</code>
     */
    protected boolean isTab(IUserLayoutManager ulm, String folderId) throws PortalException {
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
     *
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
            logger.error("Error resolving message with key {}.", key, e);
            return defaultMessage;
        }
    }

    /**
     * Syntactic sugar for safely resolving a one-arg message from message bundle.
     *
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
            logger.error("Error resolving message with key {}.", key, e);
            return defaultMessage;
        }
    }

    private IAuthorizationPrincipal getUserPrincipal(final String userName) {
        final IEntity user = this.personEntityService.getPersonEntity(userName);
        if (user == null) {
            return null;
        }

        return authorizationService.newPrincipal(user);
    }

    private String getTabIdFromName(IUserLayout userLayout, String tabName) {
        @SuppressWarnings("unchecked")
        Enumeration<String> childrenOfRoot = userLayout.getChildIds(userLayout.getRootId());

        while (childrenOfRoot
                .hasMoreElements()) { // loop over folders that might be the favorites folder
            String nodeId = childrenOfRoot.nextElement();

            try {

                IUserLayoutNodeDescription nodeDescription = userLayout.getNodeDescription(nodeId);
                IUserLayoutNodeDescription.LayoutNodeType nodeType = nodeDescription.getType();

                if (IUserLayoutNodeDescription.LayoutNodeType.FOLDER.equals(nodeType)
                        && nodeDescription instanceof IUserLayoutFolderDescription) {
                    IUserLayoutFolderDescription folderDescription =
                            (IUserLayoutFolderDescription) nodeDescription;

                    if (tabName.equalsIgnoreCase(folderDescription.getName())) {
                        return folderDescription.getId();
                    }
                }
            } catch (Exception e) {
                logger.error("Error getting the nodeID of the tab name " + tabName, e);
            }
        }

        logger.warn("Tab " + tabName + " was searched for but not found");
        return null; // didn't find tab
    }

    /**
     * Moves the source element.
     *
     * <p>- If the destination is a tab, the new element automatically goes to the end of the first
     * column or in a new column. - If the destination is a folder, the element is added to the end
     * of the folder. - Otherwise, the element is inserted before the destination (the destination
     * can't be a tab or folder so it must be a portlet).
     *
     * @return true if the element was moved and saved.
     */
    private boolean moveElementInternal(
            HttpServletRequest request, String sourceId, String destinationId, String method) {

        logger.debug(
                "moveElementInternal invoked for sourceId={}, destinationId={}, method={}",
                sourceId,
                destinationId,
                method);

        if (StringUtils.isEmpty(destinationId)) { // shortcut for beginning and end
            return true;
        }
        IUserInstance ui = userInstanceManager.getUserInstance(request);
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        boolean success = false;
        if (isTab(ulm, destinationId)) {
            // If the target is a tab type node, move the element to the end of the first column.
            // TODO Try to insert it into the first available column if multiple columns
            Enumeration<String> columns = ulm.getChildIds(destinationId);
            if (columns.hasMoreElements()) {
                success = attemptNodeMove(ulm, sourceId, columns.nextElement(), null);
            } else {
                // Attempt to create a new column

                IUserLayoutFolderDescription newColumn = new UserLayoutFolderDescription();
                newColumn.setName("Column");
                newColumn.setId("tbd");
                newColumn.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
                newColumn.setHidden(false);
                newColumn.setUnremovable(false);
                newColumn.setImmutable(false);

                // add the column to our layout
                IUserLayoutNodeDescription col = ulm.addNode(newColumn, destinationId, null);

                // If column was created (might not if the tab had addChild=false), move the
                // channel.
                if (col != null) {
                    success = attemptNodeMove(ulm, sourceId, col.getId(), null);
                } else {
                    logger.info(
                            "Unable to move item into existing columns on tab {} and unable to create new column",
                            destinationId);
                }
            }

        } else {
            // If destination is a column, attempt to move into end of column
            if (isFolder(ulm, destinationId)) {
                success = attemptNodeMove(ulm, sourceId, destinationId, null);
            } else {
                // If insertBefore move to prior to node else to end of folder containing node
                success =
                        attemptNodeMove(
                                ulm,
                                sourceId,
                                ulm.getParentId(destinationId),
                                "insertBefore".equals(method) ? destinationId : null);
            }
        }

        try {
            if (success) {
                ulm.saveUserLayout();
            }
        } catch (PortalException e) {
            logger.warn("Error saving layout", e);
            return false;
        }

        return success;
    }

    private boolean attemptNodeMove(
            IUserLayoutManager ulm, String sourceId, String destinationId, String beforeNode) {
        boolean success = ulm.moveNode(sourceId, destinationId, beforeNode);
        if (!success) {
            logger.warn(
                    "moveNode returned false for sourceId={}, destinationId={}, method={};  "
                            + "Aborting node movement",
                    sourceId,
                    destinationId,
                    beforeNode);
        }
        return success;
    }

    private boolean isFolder(IUserLayoutManager ulm, String id) {
        return ulm.getNode(id).getType().equals(IUserLayoutNodeDescription.LayoutNodeType.FOLDER);
    }

    private void removeOrphanedFavoritesFromLayout(IUserLayoutManager ulm) {
        List<IUserLayoutNodeDescription> favoritesPortlets =
                favoritesUtils.getFavoritePortletLayoutNodes(ulm.getUserLayout());
        removeOrphanedFavoritesFromLayout(ulm, favoritesPortlets);
    }

    private void removeOrphanedFavoritesFromLayout(
            IUserLayoutManager ulm, List<IUserLayoutNodeDescription> favoritesPortlets) {
        // remove orphaned favorites (meaning they are favorites for portlets that have been
        // removed/deleted) ...keep in mind that ulm.saveLayout() will need to be called
        // in order to actually save the changes to the layout
        for (IUserLayoutNodeDescription node : favoritesPortlets) {
            if (node instanceof IUserLayoutChannelDescription) {
                IUserLayoutChannelDescription channelDescription =
                        (IUserLayoutChannelDescription) node;
                if (channelDescription
                        .getChannelPublishId()
                        .equals(MissingPortletDefinition.CHANNEL_ID)) {
                    ulm.deleteNode(channelDescription.getChannelSubscribeId());
                }
            }
        }
    }

    // for unit testing begin

    public interface IPersonEntityService {
        IEntity getPersonEntity(String userName);
    }

    public class GroupServiceBasedPersonEntityService implements IPersonEntityService {
        @Override
        public IEntity getPersonEntity(String userName) {
            return GroupService.getEntity(userName, IPerson.class);
        }
    }

    private IPersonEntityService personEntityService = new GroupServiceBasedPersonEntityService();

    public void setPersonEntityService(IPersonEntityService personEntityService) {
        this.personEntityService = personEntityService;
    }

    // for unit testing end
}
