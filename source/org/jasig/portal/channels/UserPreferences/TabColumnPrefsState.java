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

package org.jasig.portal.channels.UserPreferences;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.UserLayoutManager;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.PortalException;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.Logger;
import org.jasig.portal.GenericPortalBean;
import org.jasig.portal.UtilitiesBean;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.IUserPreferencesStore;
import org.jasig.portal.RDBMUserPreferencesStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.DocumentHandler;
import java.util.Hashtable;
import java.net.URL;

/**
 * This user preferences component is for use with layouts based
 * on the tab-column structure.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
class TabColumnPrefsState extends BaseState
{
  protected ChannelStaticData staticData;
  protected ChannelRuntimeData runtimeData;
  private static final String sslLocation = UtilitiesBean.fixURI("webpages/stylesheets/org/jasig/portal/channels/CUserPreferences/tab-column/tab-column.ssl");
  private Document userLayout;
  private IUserPreferencesStore upStore = new RDBMUserPreferencesStore();

  // Here are all the possible error messages for this channel. Maybe these should be moved to
  // a properties file or static parameters.
  private String errorMessage = "Nothing is wrong!";
  private static final String errorMessageSetActiveTab = "Problem trying to set the active tab";
  private static final String errorMessageRenameTab = "Problem trying to rename tab";
  private static final String errorMessageMoveTab = "Problem trying to move the active tab";
  private static final String errorMessageAddTab = "Problem trying to add a new tab";
  private static final String errorMessageDeleteTab = "Problem trying to delete tab";
  private static final String errorMessageMoveColumn = "Problem trying to move column";
  private static final String errorMessageDeleteColumn = "Problem trying to delete column";

  public TabColumnPrefsState(CUserPreferences context)
  {
    super(context);
    this.internalState = new DefaultState(this);
  }

  public void setStaticData (ChannelStaticData sd) throws PortalException
  {
    this.staticData = sd;
    this.internalState.setStaticData(sd);
  }

  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException
  {
    this.runtimeData = rd;
    this.internalState.setRuntimeData(rd);

    try
    {
      userLayout = getUserLayout();
    }
    catch (Exception e)
    {
      throw new GeneralRenderingException(e.getMessage());
    }
  }

  public void renderXML(DocumentHandler out) throws PortalException
  {
    if (this.internalState != null)
      this.internalState.renderXML(out);
    else
      Logger.log(Logger.ERROR, "TabColumnPrefsState::renderXML() : No internal state!");
  }

  // Helper methods...

  private Document getUserLayout() throws Exception
  {
    // Get the profile currently being used
    UserLayoutManager ulm = context.getUserLayoutManager();
    int currentProfileId = ulm.getCurrentProfile().getProfileId();

    // Get the profile associated with the layout currently being modified
    UserPreferences currentUserPrefs = context.getCurrentUserPreferences();
    int editedProfileId = currentUserPrefs.getProfile().getProfileId();

    // If the we are editing the current profile, get a copy of the current user layout,
    // otherwise get it from the database or other persistant storage
    Document userLayout = null;
    if (currentProfileId == editedProfileId)
      userLayout = ulm.getUserLayoutCopy();
    else
      userLayout = GenericPortalBean.getUserLayoutStore().getUserLayout(ulm.getPerson().getID(), editedProfileId);

    return userLayout;
  }

  private String getActiveTab()
  {
    String activeTab = "none";

    try
    {
      // Get the profile associated with the layout currently being modified
      UserPreferences userPrefsFromStore = context.getUserPreferencesFromStore(context.getCurrentUserPreferences().getProfile());
      userPrefsFromStore = new GPreferencesState(context).getUserPreferences();
      activeTab = userPrefsFromStore.getStructureStylesheetUserPreferences().getParameterValue("activeTab");
      System.out.println("activeTabFromDB="+activeTab);
    }
    catch (Exception e)
    {
      Logger.log(Logger.ERROR, "TabColumnPrefsState::getAcctiveTab : Unable to retrieve active tab.");
    }

    return activeTab;
  }

  private void setActiveTab(String activeTab) throws Exception
  {
    UserPreferences userPrefsFromStore = context.getUserPreferencesFromStore(context.getCurrentUserPreferences().getProfile());
    StructureStylesheetUserPreferences ssup = userPrefsFromStore.getStructureStylesheetUserPreferences();
    ssup.putParameterValue("activeTab", activeTab);

    // Persist user preferences
    int userId = staticData.getPerson().getID();
    int profileId = context.getCurrentUserPreferences().getProfile().getProfileId();
    upStore.setStructureStylesheetUserPreferences(userId, profileId, ssup);
  }

  private void renameTab(String tabId, String tabName) throws Exception
  {
    Element tab = userLayout.getElementById(tabId);
    tab.setAttribute("name", tabName);
    saveLayout();
  }

  private void moveTab(String tabFromId, String method, String tabToId) throws Exception
  {
    Element tabFrom = userLayout.getElementById(tabFromId);
    Element tabTo = userLayout.getElementById(tabToId);
    Element layout = userLayout.getDocumentElement();
    layout.removeChild(tabFrom);

    if (method.equals("insertBefore"))
      layout.insertBefore(tabFrom, tabTo);
    else // method = "appendAfter"
      layout.appendChild(tabFrom);

    saveLayout();
  }

  private void addTab(String tabName, String method, String tabToId) throws Exception
  {
    Element layout = userLayout.getDocumentElement();
    Document doc = layout.getOwnerDocument();
    Element newTab = doc.createElement("folder");
    newTab.setAttribute("name", tabName);
    newTab.setAttribute("ID", String.valueOf(GenericPortalBean.getUserLayoutStore().getNextStructFolderId(staticData.getPerson().getID())));
    newTab.setAttribute("type", "regular");
    newTab.setAttribute("hidden", "false");
    newTab.setAttribute("unremovable", "false");
    newTab.setAttribute("immutable", "false");
    Node tabTo = userLayout.getElementById(tabToId);

    if (method.equals("insertBefore"))
      layout.insertBefore(newTab, tabTo);
    else // method = "appendAfter"
      layout.appendChild(newTab);

    saveLayout();
  }

  private void moveColumn(String sourceId, String method, String destinationId) throws Exception
  {
    Element layout = userLayout.getDocumentElement();
    Document doc = layout.getOwnerDocument();

    Element source = userLayout.getElementById(sourceId);
    Element destination = userLayout.getElementById(destinationId);
    Element sourceColumn = source;
    Element destinationColumn = destination;

    // Check if source is a tab (its parent is the layout element)
    boolean sourceIsATab = source.getParentNode().getNodeName().equals("layout");

    // If source is a tab, create a column, move the tab's children channels to this column,
    // and use this new column as the source
    if (sourceIsATab)
    {
      sourceColumn = doc.createElement("folder");
      sourceColumn.setAttribute("name", "");
      sourceColumn.setAttribute("ID", String.valueOf(GenericPortalBean.getUserLayoutStore().getNextStructFolderId(staticData.getPerson().getID())));
      sourceColumn.setAttribute("type", "regular");
      sourceColumn.setAttribute("hidden", "false");
      sourceColumn.setAttribute("unremovable", "false");
      sourceColumn.setAttribute("immutable", "false");

      NodeList channels = source.getElementsByTagName("channel");
      int numChannels = channels.getLength();
      for (int nodeIndex = 0; nodeIndex < numChannels; nodeIndex++)
      {
        Node channel = channels.item(0); // The index is 0 because after each move, the channel positions move up a notch
        boolean moveSuccessful = context.getUserLayoutManager().moveNode(channel, sourceColumn, null);
        // Need to deal with case when move isn't successful!!!
      }

      source.appendChild(sourceColumn);
    }

    // Check if destination is a tab (its parent is the layout element)
    boolean destinationIsATab = destination.getParentNode().getNodeName().equals("layout");

    // If destination is a tab, create a column, move the tab's children channels to this column,
    // and use this new column as the destination
    if (destinationIsATab)
    {
      destinationColumn = doc.createElement("folder");
      destinationColumn.setAttribute("name", "");
      destinationColumn.setAttribute("ID", String.valueOf(GenericPortalBean.getUserLayoutStore().getNextStructFolderId(staticData.getPerson().getID())));
      destinationColumn.setAttribute("type", "regular");
      destinationColumn.setAttribute("hidden", "false");
      destinationColumn.setAttribute("unremovable", "false");
      destinationColumn.setAttribute("immutable", "false");

      NodeList channels = destination.getElementsByTagName("channel");
      int numChannels = channels.getLength();
      for (int nodeIndex = 0; nodeIndex < numChannels; nodeIndex++)
      {
        Node channel = channels.item(0); // The index is 0 because after each move, the channel positions move up a notch
        boolean moveSuccessful = context.getUserLayoutManager().moveNode(channel, destinationColumn, null);
        // Need to deal with case when move isn't successful!!!
      }

      destination.appendChild(destinationColumn);
    }

    // Move the source column before the destination column or at the end
    Node targetTab = destinationColumn.getParentNode();
    Node siblingColumn = method.equals("insertBefore") ? destinationColumn : null;
    context.getUserLayoutManager().moveNode(sourceColumn, targetTab, siblingColumn);

    // Delete the source column from its tab
    //sourceColumn.getParentNode().removeChild(sourceColumn);

    // And insert before the destination column or at the end
    //if (method.equals("insertBefore"))
    //  destinationColumn.getParentNode().insertBefore(sourceColumn, destinationColumn);
    //else // method equals "appendAfter"
    //  destinationColumn.getParentNode().appendChild(sourceColumn);

    saveLayout();
  }

  private void deleteElement(String elementId) throws Exception
  {
    Element element = userLayout.getElementById(elementId);
    element.getParentNode().removeChild(element);
    saveLayout();
  }

  private void saveLayout () throws PortalException
  {
    // Persist user preferences
    UserLayoutManager ulm = context.getUserLayoutManager();
    ulm.setNewUserLayoutAndUserPreferences(userLayout, null);
  }

  /**
   * A sub-state of TabColumnPrefsState for visualizing the user layout
   * in tab-column form.
   */
  protected class DefaultState extends BaseState
  {
    protected TabColumnPrefsState context;

    private String action = "none";
    private String activeTab = "none";
    private String elementID = "none";

    public DefaultState(TabColumnPrefsState context)
    {
      this.context = context;
    }

    public void setRuntimeData (ChannelRuntimeData rd) throws PortalException
    {
      runtimeData = rd;

      // If the user hasn't clicked on a tab, get persisted active tab
      if (activeTab.equals("none"))
        activeTab = getActiveTab();

      action = runtimeData.getParameter("action");

      if (action != null)
      {
        // Select tab
        if (action.equals("selectTab"))
          activeTab = runtimeData.getParameter("activeTab");
        // Set active tab
        else if (action.equals("setActiveTab"))
        {
          try
          {
            setActiveTab(activeTab);
          }
          catch (Exception e)
          {
            Logger.log(Logger.ERROR, e);
            action = "error";
            errorMessage = errorMessageSetActiveTab;
          }
        }
        // Rename tab
        else if (action.equals("renameTab"))
        {
          String tabId = runtimeData.getParameter("elementID");
          String tabName = runtimeData.getParameter("tabName");

          System.out.println(tabId);
          System.out.println(tabName);

          try
          {
            renameTab(tabId, tabName);
          }
          catch (Exception e)
          {
            Logger.log(Logger.ERROR, e);
            action = "error";
            errorMessage = errorMessageRenameTab;
          }
        }
        // Move tab
        else if (action.equals("moveTab"))
        {
          String methodAndID = runtimeData.getParameter("method_ID");
          String tabFromId = runtimeData.getParameter("elementID");
          int indexOf_ = methodAndID.indexOf("_");
          String method = methodAndID.substring(0, indexOf_); // insertBefore or appendAfter
          String tabToId = methodAndID.substring(indexOf_ + 1);

          try
          {
            moveTab(tabFromId, method, tabToId);
          }
          catch (Exception e)
          {
            Logger.log(Logger.ERROR, e);
            action = "error";
            errorMessage = errorMessageMoveTab;
          }
        }
        // Add tab
        else if (action.equals("addTab"))
        {
          String tabName = runtimeData.getParameter("tabName");
          String methodAndID = runtimeData.getParameter("method_ID");
          int indexOf_ = methodAndID.indexOf("_");
          String method = methodAndID.substring(0, indexOf_); // insertBefore or appendAfter
          String tabToId = methodAndID.substring(indexOf_ + 1);

          try
          {
            addTab(tabName, method, tabToId);
          }
          catch (Exception e)
          {
            Logger.log(Logger.ERROR, e);
            action = "error";
            errorMessage = errorMessageAddTab;
          }
        }
        // Delete tab
        else if (action.equals("deleteTab"))
        {
          String tabId = runtimeData.getParameter("elementID");

          try
          {
            deleteElement(tabId);
          }
          catch (Exception e)
          {
            Logger.log(Logger.ERROR, e);
            action = "error";
            errorMessage = errorMessageDeleteTab;
          }
        }
        // Select column
        else if (action.equals("selectColumn"))
          elementID = runtimeData.getParameter("elementID");
        // Move column
        else if (action.equals("moveColumn"))
        {
          String activeTabParam = runtimeData.getParameter("activeTab");
          if (activeTabParam != null)
            activeTab = activeTabParam;
        }
        // Move column here
        else if (action.equals("moveColumnHere"))
        {
          String method = runtimeData.getParameter("method");
          String destinationId = runtimeData.getParameter("elementID");

          try
          {
            moveColumn(elementID, method, destinationId);
          }
          catch (Exception e)
          {
            Logger.log(Logger.ERROR, e);
            action = "error";
            errorMessage = errorMessageMoveColumn;
          }
        }
        // Delete column
        else if (action.equals("deleteColumn"))
        {
          String columnId = runtimeData.getParameter("elementID");

          try
          {
            deleteElement(columnId);
          }
          catch (Exception e)
          {
            Logger.log(Logger.ERROR, e);
            action = "error";
            errorMessage = errorMessageDeleteColumn;
          }
        }
        // Select channel
        else if (action.equals("selectChannel"))
        {
          elementID = runtimeData.getParameter("elementID");
        }
       }
      else
        action = "none";
    }


    public void renderXML (DocumentHandler out) throws PortalException
    {
      Hashtable ssParams = new Hashtable(5);
      ssParams.put("baseActionURL", runtimeData.getBaseActionURL());
      ssParams.put("activeTab", activeTab);
      ssParams.put("action", action);
      ssParams.put("elementID", elementID);
      ssParams.put("errorMessage", errorMessage);
      String media = runtimeData.getMedia();

      try
      {
        //System.out.println(UtilitiesBean.dom2PrettyString(userLayout));
        XSLT.transform(userLayout, new URL(sslLocation), out, ssParams, media);
      }
      catch (Exception e)
      {
        throw new GeneralRenderingException(e.getMessage());
      }
    }
  }
}
