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
import org.jasig.portal.IUserLayoutManager;
import org.jasig.portal.UserLayoutManager;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.StructureAttributesIncorporationFilter;
import org.jasig.portal.PortalException;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.UtilitiesBean;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.SmartCache;
import org.jasig.portal.services.LogService;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.IUserPreferencesStore;
import org.jasig.portal.RdbmServices;
import org.jasig.portal.StylesheetSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.DocumentHandler;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Enumeration;
import java.net.URL;

/**
 * This user preferences component is for use with layouts based
 * on the tab-column structure.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
final class TabColumnPrefsState extends BaseState
{
  protected ChannelStaticData staticData;
  protected ChannelRuntimeData runtimeData;
  private static final String sslLocation = UtilitiesBean.fixURI("webpages/stylesheets/org/jasig/portal/channels/CUserPreferences/tab-column/tab-column.ssl");
  private Document userLayout;
  private UserPreferences userPrefs;
  private UserProfile editedUserProfile;
  private static IUserLayoutStore ulStore = RdbmServices.getUserLayoutStoreImpl();
  private static IUserPreferencesStore upStore = RdbmServices.getUserPreferencesStoreImpl();
  private StylesheetSet set;
  private static SmartCache channelRegistryCache = new SmartCache(60); // One minute

  // Here are all the possible error messages for this channel. Maybe these should be moved to
  // a properties file or static parameters.  Actually, the error handling written so far isn't
  // very good and should be improved.  For example, there needs to be a way to let a user know that
  // he/she couldn't remove a tab because it was set as unremovable.
  private String errorMessage = "Nothing is wrong!";
  private static final String errorMessageSetActiveTab = "Problem trying to set the active tab";
  private static final String errorMessageRenameTab = "Problem trying to rename tab";
  private static final String errorMessageMoveTab = "Problem trying to move the tab";
  private static final String errorMessageAddTab = "Problem trying to add a new tab";
  private static final String errorMessageDeleteTab = "Problem trying to delete tab";
  private static final String errorMessageChangeColumnWidths = "Problem changing column widths";
  private static final String errorMessageMoveColumn = "Problem trying to move column";
  private static final String errorMessageNewColumn = "Problem trying to add a new column";
  private static final String errorMessageDeleteColumn = "Problem trying to delete column";
  private static final String errorMessageMoveChannel = "Problem trying to move channel";
  private static final String errorMessageNewChannel = "Problem trying to create a new channel";
  private static final String errorMessageDeleteChannel = "Problem trying to delete channel";

  public TabColumnPrefsState() throws PortalException
  {
    super();
    this.internalState = new DefaultState(this);
    
    // initialize stylesheet set
    set = new StylesheetSet(sslLocation);
  }

  public TabColumnPrefsState(CUserPreferences context) throws PortalException
  {
    super(context);
    this.internalState = new DefaultState(this);
    // initialize stylesheet set
    set = new StylesheetSet(sslLocation);
  }

  public void setStaticData (ChannelStaticData sd) throws PortalException
  {
    this.staticData = sd;
    this.internalState.setStaticData(sd);
  }

  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException
  {
    this.runtimeData = rd;

    // see if a top-level action has been given
    String action=rd.getParameter("action");
    if(action!=null && action.equals("manageSkins")) {
        // switch the internal state
        internalState=new SelectSkinsState(this);
        internalState.setStaticData(staticData);
    } else if(action!=null && action.equals("managePreferences")) {
        internalState=new DefaultState(this);
        internalState.setStaticData(staticData);
    }
    this.internalState.setRuntimeData(rd);

    try
    {
      // The profile the user is currently viewing or modifying...
      editedUserProfile = context.getEditedUserProfile();
      userLayout = getUserLayout();
      userPrefs = context.getUserPreferencesFromStore(editedUserProfile);
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
      LogService.instance().log(LogService.ERROR, "TabColumnPrefsState::renderXML() : No internal state!");
  }
  
  // Helper methods...

  private final Document getUserLayout() throws Exception
  {
    IUserLayoutManager ulm = context.getUserLayoutManager();
    // If the we are editing the current profile, get a copy of the current user layout,
    // otherwise get it from the database or other persistant storage
    Document userLayout = null;
    if (modifyingCurrentProfile())
      userLayout = ulm.getUserLayoutCopy();
    else
      userLayout = ulStore.getUserLayout(ulm.getPerson().getID(), context.getCurrentUserPreferences().getProfile().getProfileId());

    return userLayout;
  }
  
  private final Document getChannelRegistry()
  {
    Document channelRegistry = (Document)channelRegistryCache.get("channelRegistry");
    if (channelRegistry == null)
    {
      // Channel registry has expired, so get it and cache it
      channelRegistry = RdbmServices.getChannelRegistryStoreImpl().getRegistryXML(null, null);   

      if (channelRegistry != null)
      {
        channelRegistryCache.put("channelRegistry", channelRegistry);
        LogService.instance().log(LogService.INFO, "Caching channel registry in TabColumnPrefsState channel");
      }
    }
    return channelRegistry;
  }
  
  private final String getActiveTab()
  {
    String activeTab = "none";

    try
    {
      // Get the profile associated with the layout currently being modified
      UserPreferences userPrefsFromStore = context.getUserPreferencesFromStore(context.getCurrentUserPreferences().getProfile());
      activeTab = userPrefsFromStore.getStructureStylesheetUserPreferences().getParameterValue("activeTab");
    }
    catch (Exception e)
    {
      LogService.instance().log(LogService.ERROR, "TabColumnPrefsState::getAcctiveTab : Unable to retrieve active tab.");
    }

    return activeTab;
  }

  private final void setActiveTab(String activeTab) throws Exception
  {
    // Must get from store because the one in memory is comtaminated with stylesheet params
    // that shouldn't get persisted
    //UserPreferences userPrefsFromStore = context.getUserPreferencesFromStore(context.getCurrentUserPreferences().getProfile());
    //StructureStylesheetUserPreferences ssup = userPrefsFromStore.getStructureStylesheetUserPreferences();
    StructureStylesheetUserPreferences ssup = userPrefs.getStructureStylesheetUserPreferences();
    ssup.putParameterValue("activeTab", activeTab);

    // Persist structure stylesheet user preferences
    int userId = staticData.getPerson().getID();
    int profileId = editedUserProfile.getProfileId();
    upStore.setStructureStylesheetUserPreferences(userId, profileId, ssup);
  }

  private final void renameTab(String tabId, String tabName) throws Exception
  {
    Element tab = userLayout.getElementById(tabId);

    if (tab.getAttribute("immutable").equals("false"))
      tab.setAttribute("name", tabName);
    else
      throw new Exception("Attempt to rename immutable tab " + tabId + "has failed");

    saveLayout();
  }

  private final void moveTab(String sourceTabId, String method, String destinationTabId) throws Exception
  {
    Element sourceTab = userLayout.getElementById(sourceTabId);
    Element destinationTab = userLayout.getElementById(destinationTabId);
    Element layout = userLayout.getDocumentElement();
    Node siblingTab = method.equals("insertBefore") ? destinationTab : null;
    UserLayoutManager.moveNode(sourceTab, layout, siblingTab);

    saveLayout();
  }

  /**
   * Adds a new tab to the layout.
   * @param tabName the name of the tab
   * @param method either <code>insertBefore</code> or <code>appendAfter</code>
   * @param destinationTabId the column to insert the new column before or append after (may actually be a tab)
   * @throws Exception
   */
  private final void addTab(String tabName, String method, String destinationTabId) throws Exception
  {
    Element newTab = createFolder(tabName);
    Element destinationTab = userLayout.getElementById(destinationTabId);
    Node parent = destinationTab.getParentNode();
    Element siblingTab = method.equals("insertBefore") ? destinationTab : null;
    UserLayoutManager.moveNode(newTab, parent, siblingTab);

    saveLayout();
  }

  /**
   * Adds a new column into the layout.  Before the column is added,
   * a check is done to see whether the destination element is a tab.  If it is,
   * a new column is inserted first.
   * @param method either <code>insertBefore</code> or <code>appendAfter</code>
   * @param destinationElementId the column to insert the new column before or append after (may actually be a tab)
   * @throws Exception
   */
  private final void addColumn(String method, String destinationElementId) throws Exception
  {
    Element newColumn = createFolder("");
    Element destinationFolder = userLayout.getElementById(destinationElementId);

    // Insert a column if the destination element is a tab
    if (isTab(destinationFolder))
    {
      Element aColumn = createFolder("");
      UserLayoutManager.moveNode(aColumn, destinationFolder, null);
      destinationFolder = aColumn;
    }

    Node parent = destinationFolder.getParentNode();
    Node siblingFolder = method.equals("insertBefore") ? destinationFolder : null;
    UserLayoutManager.moveNode(newColumn, parent, siblingFolder);

    saveLayout();
  }

  private final void changeColumnWidths(HashMap columnWidths) throws Exception
  {
    // Must get from store because the one in memory is comtaminated with stylesheet params
    // that shouldn't get persisted
    //UserPreferences userPrefsFromStore = context.getUserPreferencesFromStore(context.getCurrentUserPreferences().getProfile());
    //StructureStylesheetUserPreferences ssup = userPrefsFromStore.getStructureStylesheetUserPreferences();
    StructureStylesheetUserPreferences ssup = userPrefs.getStructureStylesheetUserPreferences();
    java.util.Set sColWidths = columnWidths.keySet();
    java.util.Iterator iterator = sColWidths.iterator();
    while(iterator.hasNext())
    {
      String folderId = (String)iterator.next();
      String newWidth = (String)columnWidths.get(folderId);

      // Only accept widths that are either percentages or integers (fixed widths)
      boolean widthIsValid = true;
      try
      {
        Integer.parseInt(newWidth.endsWith("%") ? newWidth.substring(0, newWidth.indexOf("%")) : newWidth);
      }
      catch (java.lang.NumberFormatException nfe)
      {
        widthIsValid = false;
      }

      if (widthIsValid)
        ssup.setFolderAttributeValue(folderId, "width", newWidth);
      else
        LogService.instance().log(LogService.DEBUG, "User id " + staticData.getPerson().getID() + " entered invalid column width: " + newWidth);

    }

    // Persist structure stylesheet user preferences
    saveUserPreferences();
  }

  /**
   * Moves a column from one position in the layout to another.  Before the move is performed,
   * a check is done to see whether the source and/or destination elements are tabs.  If either
   * is a tab, a new column is inserted between it and the channels that it contains before the
   * move is carried out.
   * @param sourceId the column to move (may actually be a tab)
   * @param method either <code>insertBefore</code> or <code>appendAfter</code>
   * @param destinationId the column to insert the new column before or append after (may actually be a tab)
   * @throws Exception
   */
  private final void moveColumn(String sourceId, String method, String destinationId) throws Exception
  {
    Element layout = userLayout.getDocumentElement();
    Document doc = layout.getOwnerDocument();

    Element source = userLayout.getElementById(sourceId);
    Element destination = userLayout.getElementById(destinationId);
    Element sourceColumn = source;
    Element destinationColumn = destination;

    // If source is a tab, create a column, move the tab's children channels to this column,
    // and use this new column as the source
    if (isTab(source))
    {
      sourceColumn = createFolder("");
      NodeList channels = source.getElementsByTagName("channel");
      int numChannels = channels.getLength();
      for (int nodeIndex = 0; nodeIndex < numChannels; nodeIndex++)
      {
        Node channel = channels.item(0); // The index is 0 because after each move, the channel positions move up a notch
        boolean moveSuccessful = UserLayoutManager.moveNode(channel, sourceColumn, null);
        // Not done yet: Need to deal with case when move isn't successful!!!
      }

      source.appendChild(sourceColumn);
    }

    // If destination is a tab, create a column, move the tab's children channels to this column,
    // and use this new column as the destination
    if (isTab(destination))
    {
      destinationColumn = createFolder("");
      NodeList channels = destination.getElementsByTagName("channel");
      int numChannels = channels.getLength();
      for (int nodeIndex = 0; nodeIndex < numChannels; nodeIndex++)
      {
        Node channel = channels.item(0); // The index is 0 because after each move, the channel positions move up a notch
        boolean moveSuccessful = UserLayoutManager.moveNode(channel, destinationColumn, null);
        // Not done yet: Need to deal with case when move isn't successful!!!
      }

      destination.appendChild(destinationColumn);
    }

    // Move the source column before the destination column or at the end
    Node targetTab = destinationColumn.getParentNode();
    Node siblingColumn = method.equals("insertBefore") ? destinationColumn : null;
    UserLayoutManager.moveNode(sourceColumn, targetTab, siblingColumn);

    saveLayout();
  }

  /**
   * Moves a channel from one position in the layout to another.
   * @param sourceChannelId the channel to move
   * @param method either <code>insertBefore</code> or <code>appendAfter</code>
   * @param destinationElementId the ID of the channel to insert the new channel before or append after
   * @throws Exception
   */
  private final void moveChannel(String sourceChannelId, String method, String destinationElementId) throws Exception
  {
    Element layout = userLayout.getDocumentElement();

    Element sourceChannel = userLayout.getElementById(sourceChannelId);
    Element destinationElement = userLayout.getElementById(destinationElementId);

    // The destination element might be an empty tab or a column
    if (isTab(destinationElement))
    {
      // Create a new column in this tab and move the source channel there
      Element newColumn = createFolder("");
      Node destinationTab = userLayout.getElementById(destinationElementId);
      UserLayoutManager.moveNode(newColumn, destinationTab, null);
      UserLayoutManager.moveNode(sourceChannel, newColumn, null);
    }
    else if (isColumn(destinationElement))
    {
      // Move the source channel into the destination column
      UserLayoutManager.moveNode(sourceChannel, destinationElement, null);
    }
    else
    {
      // Move the source channel before the destination channel or at the end
      Node targetColumn = destinationElement.getParentNode();
      Node siblingChannel = method.equals("insertBefore") ? destinationElement : null;
      UserLayoutManager.moveNode(sourceChannel, targetColumn, siblingChannel);
    }

    saveLayout();
  }

  /**
   * Adds a channel to the layout.
   * @param selectedChannelId the channel to move from the channel registry
   * @param position either <code>before</code> or <code>after</code>
   * @param destinationElementId the ID of the channel to insert the new channel before or append after
   * @throws Exception
   */
  private final void addChannel(String selectedChannelId, String position, String destinationElementId) throws Exception
  {
    Element layout = userLayout.getDocumentElement();
    
    Document channelRegistry = getChannelRegistry();
	  Element newChannel = (Element)(userLayout.importNode(channelRegistry.getElementById(selectedChannelId), true));
    String instanceId = ulStore.getNextStructChannelId(staticData.getPerson().getID());
    newChannel.setAttribute("ID", instanceId);
    
    Element destinationElement = userLayout.getElementById(destinationElementId);

    // The destination element might be an empty tab or a column
    if (isTab(destinationElement))
    {
      // Create a new column in this tab and move the source channel there
      Element newColumn = createFolder("");
      Node destinationTab = userLayout.getElementById(destinationElementId);
      UserLayoutManager.moveNode(newColumn, destinationTab, null);
      UserLayoutManager.moveNode(newChannel, newColumn, null);
    }
    else if (isColumn(destinationElement))
    {
      // Move the source channel into the destination column
      UserLayoutManager.moveNode(newChannel, destinationElement, null);
    }
    else
    {
      // Move the source channel before the destination channel or at the end
      Node targetColumn = destinationElement.getParentNode();
      Node siblingChannel = position.equals("before") ? destinationElement : null;
      UserLayoutManager.moveNode(newChannel, targetColumn, siblingChannel);
    }
    
    // Remove the <registry> XML from the user layout before saving
    for (Node n = layout.getLastChild(); n != null && n.getNodeType() == Node.ELEMENT_NODE; n = n.getPreviousSibling())
    {
      if (n.getNodeName().equals("registry") && n.getParentNode().equals(layout))
        layout.removeChild(n);
    }
    saveLayout();
  }
  
  /**
   * Removes a tab, column, or channel element from the layout
   * @param elementId the ID attribute of the element to remove
   */
  private final void deleteElement(String elementId) throws Exception
  {
    Element element = userLayout.getElementById(elementId);
    // for some reason I am getting a null here for any newly added element
    // I remember some other people mentioning this problem
    boolean deleteSuccessful = UserLayoutManager.deleteNode(element);
    if (deleteSuccessful)
      saveLayout();
    else
      throw new Exception("Element " + elementId + " cannot be removed because it is either unremovable or it or one of its parent elements is immutable.");
  }

  /**
   * A folder is a tab if its parent element is the layout element
   * @param folder the folder in question
   * @return <code>true</code> if the folder is a tab, otherwise <code>false</code>
   */
  private final boolean isTab (Element folder)
  {
    return folder.getParentNode().getNodeName().equals("layout");
  }

  /**
   * A folder is a column if its parent is a tab element
   * @param folder the folder in question
   * @return <code>true</code> if the folder is a column, otherwise <code>false</code>
   */
  private final boolean isColumn (Element folder)
  {
    return isTab((Element)folder.getParentNode());
  }

  /**
   * Creates a folder element with default attributes.  This method can be used
   * to create a tab or a column.  For tabs, pass the tab name.  For column,
   * pass an empty String since column names aren't meaningful
   * @param name the tab name for tabs and an empty string for columns
   * @return the newly created tab or column
   */
  private final Element createFolder (String name) throws Exception
  {
    String ID = String.valueOf(ulStore.getNextStructFolderId(staticData.getPerson().getID()));
    Element layout = userLayout.getDocumentElement();
    Document doc = layout.getOwnerDocument();
    Element folder = doc.createElement("folder");
    folder.setAttribute("name", name);
    folder.setAttribute("ID", ID);
    folder.setAttribute("type", "regular");
    folder.setAttribute("hidden", "false");
    folder.setAttribute("unremovable", "false");
    folder.setAttribute("immutable", "false");

    // This is Xerces-dependent, but it is the only way to get things to work at the moment
    // Without this line, it is not possible to access this new folder with the getElementById() method
    ((org.apache.xerces.dom.DocumentImpl)doc).putIdentifier(ID, folder);

    return folder;
  }

  private void saveLayout () throws PortalException
  {
    // Persist user layout
    // Needs to check if we're modifying the current layout!
    IUserLayoutManager ulm = context.getUserLayoutManager();
    ulm.setNewUserLayoutAndUserPreferences(userLayout, null);
  }

  private void saveUserPreferences () throws PortalException
  {
    IUserLayoutManager ulm = context.getUserLayoutManager();
    if (modifyingCurrentProfile())
      ulm.setNewUserLayoutAndUserPreferences(null, userPrefs);
    else
      upStore.putUserPreferences(staticData.getPerson().getID(), userPrefs);
  }

  private boolean modifyingCurrentProfile () throws PortalException
  {
    // If the we are editing the current profile, return true, otherwise false
    boolean b = context.getUserLayoutManager().getCurrentProfile().equals(editedUserProfile);
    System.out.println("editingCurrentProfile="+b);
    System.out.println("current="+context.getUserLayoutManager().getCurrentProfile().getProfileId());
    System.out.println("edited="+editedUserProfile.getProfileId());
    return context.getUserLayoutManager().getCurrentProfile().equals(editedUserProfile);
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
    private String position = "none";
    private String catID = "none";

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
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageSetActiveTab;
          }
        }
        // Rename tab
        else if (action.equals("renameTab"))
        {
          try
          {
            String tabId = runtimeData.getParameter("elementID");
            String tabName = runtimeData.getParameter("tabName");

            renameTab(tabId, tabName);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageRenameTab;
          }
        }
        // Move tab
        else if (action.equals("moveTab"))
        {
          try
          {
            String methodAndID = runtimeData.getParameter("method_ID");

            if (methodAndID != null)
            {
              String sourceTabId = runtimeData.getParameter("elementID");
              int indexOf_ = methodAndID.indexOf("_");
              String method = methodAndID.substring(0, indexOf_); // insertBefore or appendAfter
              String destinationTabId = methodAndID.substring(indexOf_ + 1);

              moveTab(sourceTabId, method, destinationTabId);
            }
            else
              action = "selectTab";
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageMoveTab;
          }
        }
        // Add tab
        else if (action.equals("addTab"))
        {
          try
          {
            String tabName = runtimeData.getParameter("tabName");
            String methodAndID = runtimeData.getParameter("method_ID");

            if (methodAndID != null)
            {
              int indexOf_ = methodAndID.indexOf("_");
              String method = methodAndID.substring(0, indexOf_); // insertBefore or appendAfter
              String destinationTabId = methodAndID.substring(indexOf_ + 1);

              addTab(tabName, method, destinationTabId);
            }
            else
              action = "newTab";
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageAddTab;
          }
        }
        // Delete tab
        else if (action.equals("deleteTab"))
        {
          try
          {
            String tabId = runtimeData.getParameter("elementID");

            deleteElement(tabId);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageDeleteTab;
          }
        }
        // Select column
        else if (action.equals("selectColumn"))
          elementID = runtimeData.getParameter("elementID");
        // Change column width(s)
        else if (action.equals("columnWidth"))
        {
          try
          {
            HashMap columnWidths = new HashMap();
            Enumeration eParams = runtimeData.getParameterNames();
            while (eParams.hasMoreElements())
            {
              String param = (String)eParams.nextElement();
              String prefix = "columnWidth_";

              if (param.startsWith(prefix))
              {
                String folderId = param.substring(prefix.length());
                String newWidth = runtimeData.getParameter(prefix + folderId);
                columnWidths.put(folderId, newWidth);
              }
            }

            changeColumnWidths(columnWidths);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageChangeColumnWidths;
          }
        }
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
          try
          {
            String method = runtimeData.getParameter("method");
            String destinationId = runtimeData.getParameter("elementID");

            moveColumn(elementID, method, destinationId);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageMoveColumn;
          }
        }
        // New column
        else if (action.equals("newColumn"))
        {
          try
          {
            String method = runtimeData.getParameter("method");
            elementID = runtimeData.getParameter("elementID");
            String destinationColumnId = elementID;

            addColumn(method, destinationColumnId);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageNewColumn;
          }
        }
        // Add column
        else if (action.equals("addColumn"))
        {
          // Currently not implemented...
          // We need to assign widths to columns.
          // The action addColumn isn't in the stylesheet yet.
        }
        // Delete column
        else if (action.equals("deleteColumn"))
        {
          try
          {
            String columnId = runtimeData.getParameter("elementID");

            deleteElement(columnId);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageDeleteColumn;
          }
        }
        // Select channel
        else if (action.equals("selectChannel"))
        {
          elementID = runtimeData.getParameter("elementID");
        }
        // Move channel
        else if (action.equals("moveChannel"))
        {
          String activeTabParam = runtimeData.getParameter("activeTab");
          if (activeTabParam != null)
            activeTab = activeTabParam;
        }
        // Move channel here
        else if (action.equals("moveChannelHere"))
        {
          try
          {
            String method = runtimeData.getParameter("method");
            String destinationId = runtimeData.getParameter("elementID");

            moveChannel(elementID, method, destinationId);

            // Clear out elementId so the channel doesn't stay highlighted
            elementID = null;
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageMoveChannel;
          }
        }
        // New channel
        else if (action.equals("newChannel"))
        {
          try
          {
            // User clicked "?"
            if (runtimeData.getParameter("channelMoreInfo") != null)
            {
              
            }
            // User clicked "Add"
            else if (runtimeData.getParameter("addChannel") != null)
            {
              String selectedChannel = runtimeData.getParameter("selectedChannel");              
              addChannel(selectedChannel, position, elementID);
              elementID = "none";
              position = "none";
              action = "none";
            }
            // User clicked "Go"
            else
            {
              String passedPosition = runtimeData.getParameter("position");
              String passedElementID = runtimeData.getParameter("elementID");
              if (passedPosition != null)
                position = passedPosition;
              if (passedElementID != null)
                elementID = passedElementID;
              
              String selectedCategory = runtimeData.getParameter("selectedCategory");
              if (selectedCategory != null)
                catID = selectedCategory;
              else
                catID= "top";
            }            
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageNewChannel;
          }
        }        
        // Delete channel
        else if (action.equals("deleteChannel"))
        {
          try
          {
            String channelId = runtimeData.getParameter("elementID");

            deleteElement(channelId);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageDeleteChannel;
          }
        }
        // Cancel
        else if (action.equals("cancel"))
        {
          elementID = "none";
          position = "none";
          catID = "none";
        }
      }
      else
        action = "none";
    }


    public void renderXML (DocumentHandler out) throws PortalException
    {
      try
      {
        // Set up chain: userLayout --> Structure Attributes Incorp. Filter --> out
        // Currently this code assumes the use of Xalan as does much of the framework
        org.apache.xalan.xslt.XSLTProcessor processor = org.apache.xalan.xslt.XSLTProcessorFactory.getProcessor();
        String xslURI = set.getStylesheetURI("default",runtimeData.getBrowserInfo());
        org.apache.xalan.xslt.StylesheetRoot ssRoot = XSLT.getStylesheetRoot(xslURI);
        processor.setStylesheet(ssRoot);
        processor.setDocumentHandler(out);
        processor.setStylesheetParam("baseActionURL", processor.createXString(runtimeData.getBaseActionURL()));
        processor.setStylesheetParam("activeTab", processor.createXString(activeTab));
        processor.setStylesheetParam("action", processor.createXString(action));
        processor.setStylesheetParam("elementID", processor.createXString(elementID));     
        processor.setStylesheetParam("position", processor.createXString(position)); // For subscribe     
        processor.setStylesheetParam("catID", processor.createXString(catID)); // For subscribe
        processor.setStylesheetParam("errorMessage", processor.createXString(errorMessage));

        StructureStylesheetUserPreferences ssup = userPrefs.getStructureStylesheetUserPreferences();
        StructureAttributesIncorporationFilter saif = new StructureAttributesIncorporationFilter(processor, ssup);

        // Incorporate channel registry document into userLayout if user is in the subscribe process
        if (action.equals("newChannel"))
        {
          Node channelRegistry = getChannelRegistry().getDocumentElement();
          userLayout.getDocumentElement().appendChild(userLayout.importNode(channelRegistry, true));            
        }
        
        // Begin SAX chain
        UtilitiesBean.node2SAX(userLayout, saif);

        //if (action.equals("newChannel"))
        //  System.out.println(UtilitiesBean.dom2PrettyString(userLayout));
      }
      catch (Exception e)
      {
        throw new GeneralRenderingException(e.getMessage());
      }
    }
  }
  /**
   * A sub-state of TabColumnPrefsState for selecting skins
   */
  protected class SelectSkinsState extends BaseState
  {
      protected TabColumnPrefsState context;

      public SelectSkinsState(TabColumnPrefsState context) {
          this.context = context;
      }

      public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
          runtimeData = rd;
          String action = runtimeData.getParameter("action");
          if (action != null) {
              if (runtimeData.getParameter("submitSave")!=null) {
                  // save
                  String skinName = runtimeData.getParameter("skinName");
                  userPrefs.getThemeStylesheetUserPreferences().putParameterValue("skin",skinName);
                  // save user preferences ?
                  saveUserPreferences();
                  // reset state
                  BaseState df=new DefaultState(context);
                  df.setStaticData(staticData);
                  df.setRuntimeData(runtimeData);
                  context.setState(df);
              } else if (runtimeData.getParameter("submitCancel")!=null) {
                  // return to the default state
                  BaseState df=new DefaultState(context);
                  df.setStaticData(staticData);
                  df.setRuntimeData(runtimeData);
                  context.setState(df);
              }
          }
      }

      public void renderXML (DocumentHandler out) throws PortalException
      {
          String currentSkin = userPrefs.getThemeStylesheetUserPreferences().getParameterValue("skin");
          Document doc = new org.apache.xerces.dom.DocumentImpl();

          Hashtable params = new Hashtable(2);
          params.put("baseActionURL", runtimeData.getBaseActionURL());
          if(currentSkin!=null)
              params.put("currentSkin", currentSkin);

          String xslURI=set.getStylesheetURI("skinList",runtimeData.getBrowserInfo());

          try {
              XSLT.transform(doc, new URL(UtilitiesBean.fixURI(xslURI)), out, params);
          } catch (Exception e) {
              throw new GeneralRenderingException(e.getMessage());
          }

      }
  }
}
