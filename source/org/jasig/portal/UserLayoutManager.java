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

import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.jndi.JNDIManager;
import  java.sql.*;
import  org.w3c.dom.*;
import  org.apache.xalan.xpath.*;
import  org.apache.xalan.xslt.*;
import  org.apache.xml.serialize.*;
import  org.w3c.dom.*;
import  javax.servlet.*;
import  javax.servlet.jsp.*;
import  javax.servlet.http.*;
import  java.io.*;
import  java.util.*;
import  java.text.*;
import  java.net.*;


/**
 * UserLayoutManager participates in all operations associated with the
 * user layout and user preferences.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class UserLayoutManager {
  private Document uLayoutXML;
  private UserPreferences complete_up;
  // caching of stylesheet descriptions is recommended
  // if they'll take up too much space, we can take them
  // out, but cache stylesheet URIs, mime type and serializer name.
  // Those are used in every rendering cycle.
  private ThemeStylesheetDescription tsd;
  private StructureStylesheetDescription ssd;
  private boolean unmapped_user_agent = false;
  private IPerson m_person;

  /**
   * Constructor does the following
   *  1. Read layout.properties
   *  2. read userLayout from the database
   *  @param the servlet request object
   *  @param person object
   */
  public UserLayoutManager (HttpServletRequest req, IPerson person) {
    String fs = System.getProperty("file.separator");
    String propertiesDir = GenericPortalBean.getPortalBaseDir() + "properties" + fs;
    int guestId = 1;            // belongs in a properties file
    uLayoutXML = null;
    try {
      m_person = person;
      // load user preferences
      // Should obtain implementation in a different way!!
      IUserPreferencesStore updb = RdbmServices.getUserPreferencesStoreImpl();
      // determine user profile
      String userAgent = req.getHeader("User-Agent");
      UserProfile upl = updb.getUserProfile(m_person.getID(), userAgent);
      if (upl == null) {
        upl = updb.getSystemProfile(userAgent);
      }
      if (upl != null) {
        // read uLayoutXML
        uLayoutXML = GenericPortalBean.getUserLayoutStore().getUserLayout(m_person.getID(), upl.getProfileId());
        if (uLayoutXML == null) {
          Logger.log(Logger.ERROR, "UserLayoutManager::UserLayoutManager() : unable to retreive userLayout for user=\"" + 
              m_person.getID() + "\", profile=\"" + upl.getProfileName() + "\".");
        }
        this.setCurrentUserPreferences(updb.getUserPreferences(m_person.getID(), upl));
        // Initialize the JNDI context for this user
        JNDIManager.initializeUserContext(uLayoutXML, req.getSession(), m_person);
      } 
      else {
        // there is no user-defined mapping for this particular browser.
        // user should be redirected to a browser-registration page.
        unmapped_user_agent = true;
        Logger.log(Logger.DEBUG, "UserLayoutManager::UserLayoutManager() : unable to find a profile for user \"" + m_person.getID()
            + "\" and userAgent=\"" + userAgent + "\".");
      }
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }

  /* This function processes request parameters related to
   * setting Structure/Theme stylesheet parameters and attributes.
   * (uP_sparam, uP_tparam, uP_sfattr, uP_scattr uP_tcattr)
   * It also processes layout root requests (uP_root)
   */
  public void processUserPreferencesParameters (HttpServletRequest req) {
    // layout root setting
    String root;
    if ((root = req.getParameter("uP_root")) != null) {
      // If a channel specifies "me" as its root, set the root
      // to the channel's instance ID
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
        Logger.log(Logger.DEBUG, "UserLayoutManager::processUserPreferencesParameters() : setting sparam \"" + sparams[i]
            + "\"=\"" + pValue + "\".");
      }
    }
    String[] tparams = req.getParameterValues("uP_tparam");
    if (tparams != null) {
      for (int i = 0; i < tparams.length; i++) {
        String pValue = req.getParameter(tparams[i]);
        complete_up.getThemeStylesheetUserPreferences().putParameterValue(tparams[i], pValue);
        Logger.log(Logger.DEBUG, "UserLayoutManager::processUserPreferencesParameters() : setting tparam \"" + tparams[i]
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
            Logger.log(Logger.DEBUG, "UserLayoutManager::processUserPreferencesParameters() : setting sfattr \"" + aName
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
            Logger.log(Logger.DEBUG, "UserLayoutManager::processUserPreferencesParameters() : setting scattr \"" + aName
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
            Logger.log(Logger.DEBUG, "UserLayoutManager::processUserPreferencesParameters() : setting tcattr \"" + aName
                + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
          }
        }
      }
    }
  }

  /**
   * put your documentation comment here
   * @return
   */
  public IPerson getPerson () {
    return  (m_person);
  }

  /**
   * Returns a global channel ID given a channel instance ID
   * @param channelInstanceID
   * @return
   */
  public String getChannelGlobalID (String channelInstanceID) {
    // Get the channel node from the user's layout
    Node channelNode = getNode(channelInstanceID);
    if (channelNode == null) {
      return  (null);
    }
    // Get the global channel ID from the channel node
    Node channelIDNode = channelNode.getAttributes().getNamedItem("chanID");
    if (channelIDNode == null) {
      return  (null);
    }
    // Return the channel's global ID
    return  (channelIDNode.getNodeValue());
  }

  /**
   * put your documentation comment here
   * @return
   */
  public boolean userAgentUnmapped () {
    return  unmapped_user_agent;
  }

  /**
   * put your documentation comment here
   * @return
   */
  public UserPreferences getCompleteCurrentUserPreferences () {
    return  complete_up;
  }

  /**
   * put your documentation comment here
   * @param current_up
   */
  public void setCurrentUserPreferences (UserPreferences current_up) {
    if (current_up != null)
      complete_up = current_up;
  }

  /*
   * Resets both user layout and user preferences.
   * Note that if any of the two are "null", old values will be used.
   */
  public void setNewUserLayoutAndUserPreferences (Document newLayout, UserPreferences newPreferences) throws PortalException {
    if (newPreferences != null) {
      // Should obtain implementation in a different way!!
      IUserPreferencesStore updb = RdbmServices.getUserPreferencesStoreImpl();
      updb.putUserPreferences(m_person.getID(), newPreferences);
      this.setCurrentUserPreferences(newPreferences);
    }
    if (newLayout != null) {
      uLayoutXML = newLayout;
      try {
        GenericPortalBean.getUserLayoutStore().setUserLayout(m_person.getID(), complete_up.getProfile().getProfileId(), 
            uLayoutXML);
      } catch (Exception e) {
        Logger.log(Logger.ERROR, e);
        throw  new GeneralRenderingException(e.getMessage());
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

  /**
   * put your documentation comment here
   * @return
   */
  public UserPreferences getUserPreferencesCopy () {
    return  new UserPreferences(this.getUserPreferences());
  }

  /**
   * put your documentation comment here
   * @return
   */
  private UserPreferences getUserPreferences () {
    //        return up;
    return  complete_up;
  }

  /**
   * put your documentation comment here
   * @return
   */
  public UserProfile getCurrentProfile () {
    return  this.getUserPreferences().getProfile();
  }

  /**
   * put your documentation comment here
   * @return
   */
  private ThemeStylesheetDescription getThemeStylesheetDescription () {
    if (this.tsd == null) {
      ICoreStylesheetDescriptionStore csddb = RdbmServices.getCoreStylesheetDescriptionImpl();
      tsd = csddb.getThemeStylesheetDescription(this.getCurrentProfile().getThemeStylesheetId());
    }
    return  tsd;
  }

  /**
   * put your documentation comment here
   * @return
   */
  private StructureStylesheetDescription getStructureStylesheetDescription () {
    if (this.ssd == null) {
      ICoreStylesheetDescriptionStore csddb = RdbmServices.getCoreStylesheetDescriptionImpl();
      ssd = csddb.getStructureStylesheetDescription(this.getCurrentProfile().getStructureStylesheetId());
    }
    return  ssd;
  }

  /**
   * Returns structure stylesheet defined by the user profile
   * @return
   */
  public String getStructureStylesheet () {
    return  (UtilitiesBean.fixURI(this.getStructureStylesheetDescription().getStylesheetURI()));
  }

  /**
   * Returns theme stylesheet defined by the user profile
   * @return
   */
  public String getThemeStylesheet () {
    return  (UtilitiesBean.fixURI(this.getThemeStylesheetDescription().getStylesheetURI()));
  }

  /**
   * returns the mime type defined by the theme stylesheet
   * in the user profile
   * @return
   */
  public String getMimeType () {
    return  this.getThemeStylesheetDescription().getMimeType();
  }

  /**
   * returns a serializer defined by the theme stylesheet
   * in the user profile
   * @return
   */
  public String getSerializerName () {
    return  this.getThemeStylesheetDescription().getSerializerName();
  }

  /**
   * put your documentation comment here
   * @param elementID
   * @return
   */
  public Node getNode (String elementID) {
    return  uLayoutXML.getElementById(elementID);
  }

  /**
   * put your documentation comment here
   * @return root node of the user layout
   */
  public Node getRoot () {
    return  uLayoutXML;
  }

  /**
   * helper function that allows to determine the name of a channel or
   *  folder in the current user layout given their ID.
   * @param nodeID
   * @return
   */
  public String getNodeName (String nodeID) {
    Element node = uLayoutXML.getElementById(nodeID);
    if (node != null) {
      return  node.getAttribute("name");
    } 
    else 
      return  null;
  }

  /**
   * put your documentation comment here
   * @param str_ID
   */
  public void removeChannel (String str_ID) throws PortalException {
    // warning .. the channel should also be removed from uLayoutXML
    Element channel = uLayoutXML.getElementById(str_ID);
    if (channel != null) {
	if(!this.deleteNode(channel)) {
	    // unable to remove channel due to unremovable/immutable restrictionsn
	    Logger.log(Logger.INFO,"UserLayoutManager::removeChannlel() : unable to remove a channel \""+str_ID+"\"");
	} else {
	    // channel has been removed from the userLayoutXML .. persist the layout ?
	    // NOTE: this shouldn't be done every time a channel is removed. A separate portal event should initiate save
	    // (or, alternatively, an incremental update should be done on the UserLayoutStore())
	    try {
		GenericPortalBean.getUserLayoutStore().setUserLayout(m_person.getID(), complete_up.getProfile().getProfileId(), uLayoutXML);
	    } catch (Exception e) {
		Logger.log(Logger.ERROR,"UserLayoutManager::removeChannle() : database operation resulted in an exception "+e);
		throw new GeneralRenderingException("Unable to save layout changes.");
	    }
	    //	    Logger.log(Logger.INFO,"UserLayoutManager::removeChannlel() : removed a channel \""+str_ID+"\"");
	}	
    }
    else
	Logger.log(Logger.ERROR, "UserLayoutManager::removeChannel() : unable to find a channel with ID=" + str_ID);
  }

  /**
   * put your documentation comment here
   * @param node
   * @param tagName
   * @return
   */
  private Element getChildByTagName (Node node, String tagName) {
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
  public boolean isUnremovable (Node node) {
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
  public boolean isImmutable (Node node) {
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
  public Node getUnremovableParent (Node node) {
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
  public Node getImmutableParent (Node node) {
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
  public boolean hasUnremovableChildren (Node node) {
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
  public boolean deleteNode (Node node) {
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
	Logger.log(Logger.ERROR,"UserLayoutManager::deleteNode() : trying to remove a root node ?!?");
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
  private boolean isDescendentOf (Node ancestor, Node node) {
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
  public boolean moveNode (Node node, Node target, Node sibiling) {
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



