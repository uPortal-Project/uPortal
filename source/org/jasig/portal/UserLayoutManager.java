/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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


package  org.jasig.portal;

import  org.jasig.portal.services.LogService;
import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.jndi.JNDIManager;
import  org.jasig.portal.jndi.PortalNamingException;
import  org.jasig.portal.utils.BooleanLock;
import  java.sql.*;
import  org.w3c.dom.*;
import  javax.servlet.*;
import  javax.servlet.jsp.*;
import  javax.servlet.http.*;
import  java.io.*;
import  java.util.*;
import  java.text.*;
import  java.net.*;


/**
 * UserLayoutManager is responsible for keeping: user id, user layout, user preferences
 * and stylesheet descriptions.
 * For method descriptions please see {@link IUserLayoutManager}.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 */
public class UserLayoutManager implements IUserLayoutManager {

    private Document uLayoutXML;
    private UserPreferences complete_up;
    // caching of stylesheet descriptions is recommended
    // if they'll take up too much space, we can take them
    // out, but cache stylesheet URIs, mime type and serializer name.
    // Those are used in every rendering cycle.
    private ThemeStylesheetDescription tsd;
    private StructureStylesheetDescription ssd;
    private boolean unmapped_user_agent = false;
    IPerson m_person;

    BooleanLock layout_write_lock=new BooleanLock(true);

    /**
     * Constructor does the following
     *  1. Read layout.properties
     *  2. read userLayout from the database
     *  @param the servlet request object
     *  @param person object
     */
    public UserLayoutManager (HttpServletRequest req, IPerson person) {
        uLayoutXML = null;
        try {
            m_person = person;
            // load user preferences
            // Should obtain implementation in a different way!!
            IUserPreferencesStore updb = UserPreferencesStoreFactory.getUserPreferencesStoreImpl();
            // determine user profile
            String userAgent = req.getHeader("User-Agent");
            UserProfile upl = updb.getUserProfile(m_person, userAgent);
            if (upl == null) {
                upl = updb.getSystemProfile(userAgent);
            }
            if (upl != null) {
                // read uLayoutXML
                uLayoutXML = UserLayoutStoreFactory.getUserLayoutStoreImpl().getUserLayout(m_person, upl.getProfileId());
                if (uLayoutXML == null) {
                    LogService.instance().log(LogService.ERROR, "UserLayoutManager::UserLayoutManager() : unable to retreive userLayout for user=\"" +
                               m_person.getID() + "\", profile=\"" + upl.getProfileName() + "\".");
                }
                complete_up=updb.getUserPreferences(m_person, upl);
                try {
                  // Initialize the JNDI context for this user
                  JNDIManager.initializeUserContext(uLayoutXML, req.getSession(), m_person);
                }
                catch(PortalNamingException pne) {
                  LogService.instance().log(LogService.ERROR, "UserLayoutManager(): Could not properly initialize user context", pne);
                }
                // set dirty flag on the layout
                layout_write_lock.setValue(true);
            }
            else {
                // there is no user-defined mapping for this particular browser.
                // user should be redirected to a browser-registration page.
                unmapped_user_agent = true;
                LogService.instance().log(LogService.DEBUG, "UserLayoutManager::UserLayoutManager() : unable to find a profile for user \"" + m_person.getID()
                           + "\" and userAgent=\"" + userAgent + "\".");
            }
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        }
    }

    /**
     * A simpler constructor, that only initialises the person object.
     * Needed for ancestors.
     * @param person an <code>IPerson</code> object.
     */
    public UserLayoutManager(IPerson person) {
        m_person=person;
    }

    /* This function processes request parameters related to
     * setting Structure/Theme stylesheet parameters and attributes.
     * (uP_sparam, uP_tparam, uP_sfattr, uP_scattr uP_tcattr)
     * It also processes layout root requests (uP_root)
     * @param req current <code>HttpServletRequest</code>
     */
    public void processUserPreferencesParameters (HttpServletRequest req) {
        // layout root setting
        String root;
        if ((root = req.getParameter("uP_root")) != null) {
            // If a channel specifies "me" as its root, set the root
            // to the channel's instance Id
            if (root.equals("me")) {
                String chanInstanceId = null;
                String servletPath = req.getServletPath();
                String searchFor = "/channel/";
                int chanIdBegIndex = servletPath.indexOf(searchFor) + searchFor.length();
                if (chanIdBegIndex != -1) {
                    int chanIdEndIndex = servletPath.indexOf("/", chanIdBegIndex);
                    root = servletPath.substring(chanIdBegIndex, chanIdEndIndex);
                }
            }
            complete_up.getStructureStylesheetUserPreferences().putParameterValue("userLayoutRoot", root);
        }
        // other params
        String[] sparams = req.getParameterValues("uP_sparam");
        if (sparams != null) {
            for (int i = 0; i < sparams.length; i++) {
                String pValue = req.getParameter(sparams[i]);
                complete_up.getStructureStylesheetUserPreferences().putParameterValue(sparams[i], pValue);
                LogService.instance().log(LogService.DEBUG, "UserLayoutManager::processUserPreferencesParameters() : setting sparam \"" + sparams[i]
                           + "\"=\"" + pValue + "\".");
            }
        }
        String[] tparams = req.getParameterValues("uP_tparam");
        if (tparams != null) {
            for (int i = 0; i < tparams.length; i++) {
                String pValue = req.getParameter(tparams[i]);
                complete_up.getThemeStylesheetUserPreferences().putParameterValue(tparams[i], pValue);
                LogService.instance().log(LogService.DEBUG, "UserLayoutManager::processUserPreferencesParameters() : setting tparam \"" + tparams[i]
                           + "\"=\"" + pValue + "\".");
            }
        }
        // attribute processing
        // structure transformation
        String[] sfattrs = req.getParameterValues("uP_sfattr");
        if (sfattrs != null) {
            for (int i = 0; i < sfattrs.length; i++) {
                String aName = sfattrs[i];
                String[] aNode = req.getParameterValues(aName + "_folderId");
                if (aNode != null && aNode.length > 0) {
                    for (int j = 0; j < aNode.length; j++) {
                        String aValue = req.getParameter(aName + "_" + aNode[j] + "_value");
                        complete_up.getStructureStylesheetUserPreferences().setFolderAttributeValue(aNode[j], aName, aValue);
                        LogService.instance().log(LogService.DEBUG, "UserLayoutManager::processUserPreferencesParameters() : setting sfattr \"" + aName
                                   + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
                    }
                }
            }
        }
        String[] scattrs = req.getParameterValues("uP_scattr");
        if (scattrs != null) {
            for (int i = 0; i < scattrs.length; i++) {
                String aName = scattrs[i];
                String[] aNode = req.getParameterValues(aName + "_channelId");
                if (aNode != null && aNode.length > 0) {
                    for (int j = 0; j < aNode.length; j++) {
                        String aValue = req.getParameter(aName + "_" + aNode[j] + "_value");
                        complete_up.getStructureStylesheetUserPreferences().setChannelAttributeValue(aNode[j], aName, aValue);
                        LogService.instance().log(LogService.DEBUG, "UserLayoutManager::processUserPreferencesParameters() : setting scattr \"" + aName
                                   + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
                    }
                }
            }
        }
        // theme stylesheet attributes
        String[] tcattrs = req.getParameterValues("uP_tcattr");
        if (tcattrs != null) {
            for (int i = 0; i < tcattrs.length; i++) {
                String aName = tcattrs[i];
                String[] aNode = req.getParameterValues(aName + "_channelId");
                if (aNode != null && aNode.length > 0) {
                    for (int j = 0; j < aNode.length; j++) {
                        String aValue = req.getParameter(aName + "_" + aNode[j] + "_value");
                        complete_up.getThemeStylesheetUserPreferences().setChannelAttributeValue(aNode[j], aName, aValue);
                        LogService.instance().log(LogService.DEBUG, "UserLayoutManager::processUserPreferencesParameters() : setting tcattr \"" + aName
                                   + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
                    }
                }
            }
        }
    }

    /**
     * Returns current person object
     * @return current <code>IPerson</code>
     */
    public IPerson getPerson () {
        return  (m_person);
    }

    /**
     * Returns a global channel Id given a channel instance Id
     * @param channelInstanceId instance id of a channel
     * @return channel global id
     */
    public String getChannelGlobalId (String channelInstanceId) {
        // Get the channel node from the user's layout
        Node channelNode = getUserLayoutNode(channelInstanceId);
        if (channelNode == null) {
            return  (null);
        }
        // Get the global channel Id from the channel node
        Node channelIdNode = channelNode.getAttributes().getNamedItem("chanID");
        if (channelIdNode == null) {
            return  (null);
        }
        // Return the channel's global Id
        return  (channelIdNode.getNodeValue());
    }

    /**
     * put your documentation comment here
     * @return
     */
    public boolean isUserAgentUnmapped () {
        return  unmapped_user_agent;
    }

    /*
     * Resets both user layout and user preferences.
     * Note that if any of the two are "null", old values will be used.
     */
    public void setNewUserLayoutAndUserPreferences (Document newLayout, UserPreferences newPreferences) throws PortalException {
        if (newPreferences != null) {
            // Should obtain implementation in a different way!!
            IUserPreferencesStore updb = UserPreferencesStoreFactory.getUserPreferencesStoreImpl();
            updb.putUserPreferences(m_person, newPreferences);
            complete_up=newPreferences;
        }
        synchronized(layout_write_lock) {
            if (newLayout != null) {
                uLayoutXML = newLayout;
                layout_write_lock.setValue(true);
                try {
                    UserLayoutStoreFactory.getUserLayoutStoreImpl().setUserLayout(m_person, complete_up.getProfile().getProfileId(), uLayoutXML);
                } catch (Exception e) {
                    LogService.instance().log(LogService.ERROR, e);
                    throw  new GeneralRenderingException(e.getMessage());
                }
            }
        }
    }

    /**
     * put your documentation comment here
     * @return
     */
    public Document getUserLayoutCopy () {
        return  UtilitiesBean.cloneDocument((org.apache.xerces.dom.DocumentImpl)uLayoutXML);
    }

    public UserPreferences getUserPreferencesCopy () {
        return  new UserPreferences(this.getUserPreferences());
    }


    public UserProfile getCurrentProfile () {
        return  this.getUserPreferences().getProfile();
    }

    public ThemeStylesheetDescription getThemeStylesheetDescription () {
        if (this.tsd == null) {
            ICoreStylesheetDescriptionStore csddb = CoreStylesheetDescriptionStoreFactory.getCoreStylesheetDescriptionStoreImpl();
            tsd = csddb.getThemeStylesheetDescription(this.getCurrentProfile().getThemeStylesheetId());
        }
        return  tsd;
    }

    public StructureStylesheetDescription getStructureStylesheetDescription () {
        if (this.ssd == null) {
            ICoreStylesheetDescriptionStore csddb = CoreStylesheetDescriptionStoreFactory.getCoreStylesheetDescriptionStoreImpl();
            ssd = csddb.getStructureStylesheetDescription(this.getCurrentProfile().getStructureStylesheetId());
        }
        return  ssd;
    }

    public Node getUserLayoutNode (String elementId) {
        return  uLayoutXML.getElementById(elementId);
    }
    public Document getUserLayout () {
        return  uLayoutXML;
    }

    public UserPreferences getUserPreferences() {
        return complete_up;
    }

    /**
     * helper function that allows to determine the name of a channel or
     *  folder in the current user layout given their Id.
     * @param nodeId
     * @return
     */
    public String getNodeName (String nodeId) {
        Element node = uLayoutXML.getElementById(nodeId);
        if (node != null) {
            return  node.getAttribute("name");
        }
        else
            return  null;
    }

    public boolean removeChannel (String channelId) throws PortalException {
        // warning .. the channel should also be removed from uLayoutXML
        Element channel = uLayoutXML.getElementById(channelId);
        if (channel != null) {
            boolean rval=true;
            synchronized(layout_write_lock) {
                if(!this.deleteNode(channel)) {
                    // unable to remove channel due to unremovable/immutable restrictionsn
                    LogService.instance().log(LogService.INFO,"UserLayoutManager::removeChannlel() : unable to remove a channel \""+channelId+"\"");
                    rval=false;
                } else {
                    layout_write_lock.setValue(true);
                    // channel has been removed from the userLayoutXML .. persist the layout ?
                    // NOTE: this shouldn't be done every time a channel is removed. A separate portal event should initiate save
                    // (or, alternatively, an incremental update should be done on the UserLayoutStore())
                    try {
                        /*
                          The following patch has been kindly contributed by Neil Blake <nd_blake@NICKEL.LAURENTIAN.CA>.
                        */
                        UserLayoutStoreFactory.getUserLayoutStoreImpl().setUserLayout(m_person, complete_up.getProfile().getProfileId(), uLayoutXML);
                        /* end of patch */
                    } catch (Exception e) {
                        LogService.instance().log(LogService.ERROR,"UserLayoutManager::removeChannle() : database operation resulted in an exception "+e);
                        throw new GeneralRenderingException("Unable to save layout changes.");
                    }
                    //	    LogService.instance().log(LogService.INFO,"UserLayoutManager::removeChannlel() : removed a channel \""+channelId+"\"");
                }
            }
            return rval;
        } else {
            LogService.instance().log(LogService.ERROR, "UserLayoutManager::removeChannel() : unable to find a channel with Id=" + channelId);
            return false;
        }
    }

    /**
     * Returns user layout write lock
     *
     * @return an <code>Object</code> lock
     */
    public BooleanLock getUserLayoutWriteLock() {
        return layout_write_lock;
    }

    /**
     * Returns a child with a particular tagname
     * @param node parent node
     * @param tagName child's tag name
     * @return child that matches the tag name
     */
    private static Element getChildByTagName (Node node, String tagName) {
        if (node == null)
            return  null;
        NodeList children = node.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element)child;
                if ((el.getTagName()).equals(tagName))
                    return  el;
            }
        }
        return  null;
    }

    /**
     * Determines if the node or any of it's parents are marked
     * as "unremovable".
     * @param node the node to be tested
     */
    public static boolean isUnremovable (Node node) {
        if (getUnremovableParent(node) != null)
            return  true;
        else
            return  false;
    }

    /**
     * Determines if the node or any of it's parents are marked as immutables
     * @param node the node to be tested
     * @param root the root node of the layout tree
     */
    public static boolean isImmutable (Node node) {
        if (getImmutableParent(node) != null)
            return  true;
        else
            return  false;
    }

    /**
     * Returns first parent of the node (or the node itself) that's marked
     * as "unremovable". Note that if the node itself is marked as
     * "unremovable", the method will return the node itself.
     * @param node node from which to move up the tree
     */
    public static Node getUnremovableParent (Node node) {
        if (node == null)
            return  null;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String r = ((Element)node).getAttribute("unremovable");
            if (r != null) {
                if (r.equals("true"))
                    return  node;
            }
        }
        return  getUnremovableParent(node.getParentNode());
    }

    /**
     * Returns first parent of the node (or the node itself) that's marked
     * as "immutable". Note that if the node itself is marked as
     * "ummutable", the method will return the node itself.
     * @param node node from which to move up the tree
     */
    public static Node getImmutableParent (Node node) {
        if (node == null)
            return  null;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String r = ((Element)node).getAttribute("immutable");
            if (r != null) {
                if (r.equals("true"))
                    return  node;
            }
        }
        return  getUnremovableParent(node.getParentNode());
    }

    /**
     * Returns true if a node has any unremovable children.
     * This function does a depth-first traversal down the user layout, so it's rather expensive.
     *
     * @param node a <code>Node</code> current node
     * @return a <code>boolean</code> true if there are any unremovable children of this node
     */
    public static  boolean hasUnremovableChildren (Node node) {
        NodeList nl = node.getChildNodes();
        if (nl != null)
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    String r = ((Element)n).getAttribute("unremovable");
                    if (r != null) {
                        if (r.equals("true")) {
                            return  true;
                        }
                    }
                    if (hasUnremovableChildren(n))
                        return  true;
                }
            }
        return  false;
    }

    /**
     * Removes a channel or a folder from the userLayout structure
     * @param node the node to be removed
     * @return removal has been successfull
     */
    public static boolean deleteNode (Node node) {
        // first of all check if this is an Element node
        if (node == null || node.getNodeType() != Node.ELEMENT_NODE)
            return  false;
        // check if the node is removable
        if (isUnremovable(node))
            return  false;
        // see if any of the parent nodes marked as immutable
        if (isImmutable(node.getParentNode()))
            return  false;
        // see if any of the node children are marked as unremovable
        if (hasUnremovableChildren(node))
            return  false;
        // all checks out, delete the node
        if (node.getParentNode() != null) {
            (node.getParentNode()).removeChild(node);
            return  true;
        } else {
            LogService.instance().log(LogService.ERROR,"UserLayoutManager::deleteNode() : trying to remove a root node ?!?");
            return false;
        }
    }

    /**
     * Checks if a particular node is a descendent of some other node.
     * Note that if both ancestor and node point at the same node, true
     * will be returned.
     * @param node the node to be checked
     * @param ancestor potential ancestor
     * @return true if node is an descendent of ancestor
     */
    private static boolean isDescendentOf (Node ancestor, Node node) {
        if (node == null)
            return  false;
        if (node == ancestor)
            return  true;
        else
            return  isDescendentOf(ancestor, node.getParentNode());
    }

    /**
     * Moves node from one location in the userLayout tree to another
     * @param node the node to be moved
     * @param target the node to which it should be appended.
     * @param sibiling a sibiling before which the node should be inserted under the target node (can be null)
     * @return move has been successfull
     */
    public static boolean moveNode (Node node, Node target, Node sibiling) {
        // make sure this is an element node
        if (node == null || node.getNodeType() != Node.ELEMENT_NODE)
            return  false;
        if (target == null || target.getNodeType() != Node.ELEMENT_NODE)
            return  false;
        // source node checks
        // see if the source is a descendent of an immutable node
        if (isImmutable(node.getParentNode()))
            return  false;
        // see if the source is a descendent of some unremovable node
        Node unrp = getUnremovableParent(node.getParentNode());
        if (unrp != null) {
            // make sure the target node is a descendent of the same unremovable
            // node as well.
            if (!isDescendentOf(unrp, target))
                return  false;
        }
        // target node checks
        // check if the target is unremovable or immutable
        if (isUnremovable(target) || isImmutable(target))
            return  false;
        // everything checks out, do the move
        if (sibiling != null && sibiling.getParentNode() == target) {
            target.insertBefore(node, sibiling);
        }
        else {
            target.appendChild(node);
        }
        return  true;
    }
}



