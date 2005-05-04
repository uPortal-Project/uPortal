/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.UserPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelSAXStreamFilter;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.StructureAttributesIncorporationFilter;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.StylesheetSet;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.i18n.LocaleAwareXSLT;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutManagerFactory;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.layout.node.UserLayoutFolderDescription;
import org.jasig.portal.serialize.OutputFormat;
import org.jasig.portal.serialize.XMLSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.SAX2DuplicatingFilterImpl;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;


/**
 * This user preferences component is for use with layouts based
 * on the tab-column structure.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class TabColumnPrefsState extends BaseState
{
    private static final Log log = LogFactory.getLog(TabColumnPrefsState.class);
  protected ChannelStaticData staticData;
  protected ChannelRuntimeData runtimeData;
  private static final String sslLocation = "/org/jasig/portal/channels/CUserPreferences/tab-column/tab-column.ssl";

  private IUserLayoutManager ulm;
  private PortalControlStructures pcs;
  private UserPreferences userPrefs;
  private UserProfile editedUserProfile;
  private static IUserLayoutStore ulStore = UserLayoutStoreFactory.getUserLayoutStoreImpl();
  private StylesheetSet set;

  private String action = "none";
  private String activeTab = "none";
  private String elementID = "none";
  private String newColumnId = null;

  // These can be overridden in a sub-class.
  protected static String BLANK_TAB_NAME = "My Tab"; // The tab will take on this name if left blank by the user
  protected static String SKINS_PATH = "media/org/jasig/portal/layout/tab-column/nested-tables";

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
  private static final String errorMessageLockTab = "Problem trying to lock tab";
  private static final String errorMessageUnlockTab = "Problem trying to unlock tab";
  private static final String errorMessageChangeColumnWidths = "Problem changing column widths";
  private static final String errorMessageMoveColumn = "Problem trying to move column";
  private static final String errorMessageNewColumn = "Problem trying to add a new column";
  private static final String errorMessageDeleteColumn = "Problem trying to delete column";
  private static final String errorMessageNewChannel = "Problem trying to add a new channel";
  private static final String errorMessageModChannelParams = "Problem trying to modify channel parameters";
  private static final String errorMessageMoveChannel = "Problem trying to move channel";
  private static final String errorMessageDeleteChannel = "Problem trying to delete channel";

  public TabColumnPrefsState() throws PortalException
  {
    super();
    this.internalState = new DefaultState(this);

    // initialize stylesheet set
    set = new StylesheetSet(ResourceLoader.getResourceAsURLString(this.getClass(), sslLocation));
  }

  public TabColumnPrefsState(CUserPreferences context) throws PortalException
  {
    super(context);
    this.internalState = new DefaultState(this);
    // initialize stylesheet set
    set = new StylesheetSet(ResourceLoader.getResourceAsURLString(this.getClass(), sslLocation));
  }

  public void setStaticData (ChannelStaticData sd) throws PortalException
  {
    this.staticData = sd;
    this.internalState.setStaticData(sd);
  }

  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException
  {
    this.runtimeData = rd;

    // See if a top-level action has been given
    String action=rd.getParameter("action");
    if (action != null) {
      if (action.equals("newChannel")) {
        if (!(internalState instanceof NewChannelState)) {
          internalState = new NewChannelState(this);
          internalState.setStaticData(staticData);
        }
      } else if (action.equals("manageSkins")) {
        if (!(internalState instanceof SelectSkinsState)) {
          internalState = new SelectSkinsState(this);
          internalState.setStaticData(staticData);
        }
      } else if (action.equals("resetLayout")) {
        if (!(internalState instanceof ResetLayoutState)) {
          internalState = new ResetLayoutState(this);
          internalState.setStaticData(staticData);
        }        
      } else if (action.equals("managePreferences")) {
        internalState = new DefaultState(this);
        internalState.setStaticData(staticData);
      }
    }
    internalState.setRuntimeData(rd);

    try
    {
        
      // Need this check so that we don't override the column width's we just set
      if (internalState instanceof DefaultState){
        if (((DefaultState)internalState).columnHasBeenAdded)
          return;
      }
        
      // The profile the user is currently viewing or modifying...
      editedUserProfile = context.getEditedUserProfile();
      ulm = getUserLayoutManager();
      userPrefs = context.getUserPreferencesFromStore(editedUserProfile);
    }
    catch (Exception e)
    {
      throw new GeneralRenderingException(e);
    }
  }

  public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException  {
    this.pcs = pcs;
  }

  public void renderXML(ContentHandler out) throws PortalException
  {
    if (this.internalState != null)
      this.internalState.renderXML(out);
    else
      log.error( "TabColumnPrefsState::renderXML() : No internal state!");
  }

  // Helper methods...

  private final IUserLayoutManager getUserLayoutManager() throws Exception
  {
    IUserPreferencesManager upm = context.getUserPreferencesManager();
    IUserLayoutManager lm=null;
    // If the we are editing the current user layout, get a copy of the current user layout,
    // otherwise get it from the database or other persistant storage
    if (modifyingCurrentUserLayout()) {
        // get it from the preferences manager
        lm=upm.getUserLayoutManager();
    }  else {
        // construct a new one
        lm=UserLayoutManagerFactory.getUserLayoutManager(upm.getPerson(),context.getCurrentUserPreferences().getProfile());
    }
    return lm;
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
      log.error( "TabColumnPrefsState::getAcctiveTab : Unable to retrieve active tab.");
    }

    return activeTab;
  }

  private final void setActiveTab(String activeTab) throws Exception
  {
    StructureStylesheetUserPreferences ssup = userPrefs.getStructureStylesheetUserPreferences();
    ssup.putParameterValue("activeTab", activeTab);

    // Persist structure stylesheet user preferences
    int profileId = editedUserProfile.getProfileId();
    ulStore.setStructureStylesheetUserPreferences(staticData.getPerson(), profileId, ssup);
  }

  private final void renameTab(String tabId, String tabName) throws PortalException
  {
      IUserLayoutFolderDescription tab=(IUserLayoutFolderDescription)ulm.getNode(tabId);
      if(ulm.canUpdateNode(ulm.getNode(tabId))) {
          if (tabName == null || tabName.trim().length() == 0) {
              tab.setName(BLANK_TAB_NAME);
          } else {
              tab.setName(tabName);
          }
          ulm.updateNode(tab);
      } else {
          throw new PortalException("Attempt to rename immutable tab " + tabId + "has failed");
      }
  }

  private final void moveTab(String sourceTabId, String method, String destinationTabId) throws PortalException {
      // determine root folder id
      String rootNodeId=ulm.getParentId(sourceTabId);

      if(ulm.canMoveNode(sourceTabId,rootNodeId,destinationTabId)) {
          if(method.equals("insertBefore")) {
              ulm.moveNode(sourceTabId,rootNodeId,destinationTabId);
          } else {
              ulm.moveNode(sourceTabId,rootNodeId,null);
          }
      }
  }

  /**
   * Adds a new tab to the layout.
   * @param tabName the name of the tab
   * @param method either <code>insertBefore</code> or <code>appendAfter</code>
   * @param destinationTabId the column to insert the new column before or append after (may actually be a tab)
   * @throws PortalException
   */
  private final void addTab(String tabName, String method, String destinationTabId) throws PortalException
  {

    IUserLayoutFolderDescription newTab = createFolder(tabName);
    if (tabName == null || tabName.trim().length() == 0) {
        newTab.setName(BLANK_TAB_NAME);
    }
    String siblingId=null;
    if(method.equals("insertBefore")) 
      siblingId=destinationTabId;
    ulm.addNode(newTab,ulm.getRootFolderId(),siblingId);

    // Add a new column to this tab
    IUserLayoutFolderDescription newColumn = createFolder("Column");
    ulm.addNode(newColumn, newTab.getId(), null);    
  }
  
  /**
   * This method will remove a column from the user's layout.  The column will be added into the layout
   * via the "add new column" link within the preferences channel.  Clicking on cancel after choosing to add
   * a new column will not remove the column hence the introduction of this method.
   */
  private final void removeNewColumn() {
      try {
          Document doc = this.ulm.getUserLayoutDOM();
          Node nNewColumnNode = doc.getElementById(this.newColumnId);    
          if (nNewColumnNode != null){
            Node parent = nNewColumnNode.getParentNode();
            parent.removeChild(nNewColumnNode);
            this.newColumnId = null;
          }
      } catch (Exception e){
          if (log.isDebugEnabled())
              log.debug("removeNewColumn failed to find new column with id " 
                      + this.newColumnId);
      }
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
      IUserLayoutFolderDescription newColumn = createFolder("Column");
      // Insert a column if the destination element is a tab
      if(isTab(destinationElementId)) {
          ulm.addNode(newColumn,destinationElementId,null);
      } else if(isColumn(destinationElementId)) {
          String siblingId=null;
          if(method.equals("insertBefore")) {
              siblingId=destinationElementId;
          }
          // Returns the node that was just added containing the default width of 100%
          IUserLayoutNodeDescription ulnd = ulm.addNode(newColumn,ulm.getParentId(destinationElementId),siblingId);
          // Get the current users layout
          Document doc = this.ulm.getUserLayoutDOM();
          // Keep track of the new column id incase the user clicks on cancel button
          this.newColumnId = ulnd.getId();
          Element nE = (Element)doc.getElementById(ulnd.getId());
          // Find out how many siblings this node contains
          NodeList list = nE.getParentNode().getChildNodes();
          if (list != null && list.getLength() > 0)
              this.setEvenlyAssignedColumnWidths(list);
      }
  }

  private final void changeColumnWidths(HashMap columnWidths) throws Exception
  {
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
        if (log.isDebugEnabled()) 
            log.debug("User id " + staticData.getPerson().getID() + " entered invalid column width: " + newWidth);

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
   * @throws PortalException
   */
  private final void moveColumn(String sourceId, String method, String destinationId) throws PortalException
  {
      String siblingId=null;
      if(method.equals("insertBefore")) {
          siblingId=destinationId;
      }
      ulm.moveNode(sourceId,ulm.getParentId(destinationId),siblingId);
  }

  /**
   * Moves a channel from one position in the layout to another.
   * @param sourceChannelSubscribeId the channel to move
   * @param method either <code>insertBefore</code> or <code>appendAfter</code>
   * @param destinationElementId the ID of the channel to insert the new channel before or append after
   * @throws PortalException
   */
  private final void moveChannel(String sourceChannelSubscribeId, String method, String destinationElementId) throws PortalException
  {
      if(isTab(destinationElementId)) {
          // create a new column and move channel there
          IUserLayoutNodeDescription newColumn=ulm.addNode(createFolder("Column"),destinationElementId,null);
          ulm.moveNode(sourceChannelSubscribeId,newColumn.getId(),null);
      } else if(isColumn(destinationElementId)) {
          // move the channel into the column
          ulm.moveNode(sourceChannelSubscribeId,destinationElementId,null);
      } else {
          // assume that destinationElementId is that of a sibling channel
          String siblingId=null;
          if(method.equals("insertBefore")) {
              siblingId=destinationElementId;
          }
          ulm.moveNode(sourceChannelSubscribeId,ulm.getParentId(destinationElementId),siblingId);
      }
  }

  /**
   * Adds a channel to the layout.
   * @param newChannel the channel to add
   * @param position either <code>before</code> or <code>after</code>
   * @param destinationElementId the ID of the channel to insert the new channel before or append after
   * @throws PortalException
   */
  private final void addChannel(Element newChannel, String position, String destinationElementId) throws PortalException
  {
      IUserLayoutChannelDescription channel=new UserLayoutChannelDescription(newChannel);
      if(isTab(destinationElementId)) {
          // create a new column and move channel there
          IUserLayoutNodeDescription newColumn=ulm.addNode(createFolder("Column"),destinationElementId,null);
          ulm.addNode(channel,newColumn.getId(),null);
      } else if(isColumn(destinationElementId)) {
          // move the channel into the column
          ulm.addNode(channel,destinationElementId,null);
      } else {
          // assume that destinationElementId is that of a sibling channel
          String siblingId=null;
          if(position.equals("before")) {
              siblingId=destinationElementId;
          }
          ulm.addNode(channel,ulm.getParentId(destinationElementId),siblingId);
      }

      // Make sure ChannelManager knows about the new channel
      pcs.getChannelManager().instantiateChannel(channel.getId());

      ulm.saveUserLayout();
  }

  /**
   * Adds a channel to the layout.
   * @param selectedChannelSubscribeId the channel to add
   * @param position either <code>before</code> or <code>after</code>
   * @param destinationElementId the ID of the channel to insert the new channel before or append after
   * @throws Exception
   */
  private final void addChannel(String selectedChannelSubscribeId, String position, String destinationElementId) throws Exception
  {
    Document channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());
    Element newChannel = channelRegistry.getElementById(selectedChannelSubscribeId);
    addChannel(newChannel, position, destinationElementId);
  }

 /**
   * Removes a channel element from the layout
   * @param channelSubscribeId the ID attribute of the channel to remove
   */
  private final void deleteChannel(String channelSubscribeId) throws Exception
  {
    pcs.getChannelManager().removeChannel(channelSubscribeId);
    deleteElement(channelSubscribeId);
  }

  /**
   * Removes a tab or column element from the layout.  To remove
   * a channel element, call deleteChannel().
   * @param elementId the ID attribute of the element to remove
   */
  private final void deleteElement(String elementId) throws Exception
  {
      // Need to check if we are about to delete a column, if so, need to reset other columns to appropriate width's
      Document doc = this.ulm.getUserLayoutDOM();
      Element childElement=(Element)doc.getElementById(elementId);
      // determine if this is a column
      String whatIsThis = childElement.getAttribute("name");
      if (whatIsThis != null && whatIsThis.startsWith("Column")){
        userPrefs.getStructureStylesheetUserPreferences().removeFolder(childElement.getAttribute("ID"));
        // get the id of the parent (tab)
        String tabId = ((Element)childElement.getParentNode()).getAttribute("ID");
        // Found a column .. lets remove the column selected first
        ulm.deleteNode(elementId);
        // get the updated xml document
        doc = this.ulm.getUserLayoutDOM();
        // Find out how many siblings this node contains
        NodeList list = ((Element)doc.getElementById(tabId)).getChildNodes();
        if (list != null && list.getLength() > 0)
            this.setEvenlyAssignedColumnWidths(list);
        this.saveUserPreferences();
        
      } else {
        // this is a tab, go ahead and delete it
        ulm.deleteNode(elementId);
      }
  }

  /**
 * @param list as a NodeList that contains all columns in current tab
 */
  private void setEvenlyAssignedColumnWidths(NodeList list) {
    // Simply divide the number of columns by 100 and produce an evenly numbered column widths
    int columns = list.getLength();
    int columnSize = 100 / columns;
    int remainder = 100 % columns;
    // Traverse through the columns and reset with the new caculated value
    StructureStylesheetUserPreferences ssup = userPrefs.getStructureStylesheetUserPreferences();
    for (int i=0; i < list.getLength(); i++){
        Element c = (Element) list.item(i);
        String nId = c.getAttribute("ID");
        ssup.setFolderAttributeValue(nId, "width", (i == (list.getLength() - 1) ? columnSize+remainder+"%" : columnSize+"%"));
    }            
  }

  private final void updateTabLock(String elementId, boolean locked) throws Exception
  {
      // NOTE: this method is to be removed soon.
  }
  
  /**
   * A folder is a tab if its parent element is the layout element
   * @param folderId the id of the folder in question
   * @return <code>true</code> if the folder is a tab, otherwise <code>false</code>
   */
  private final boolean isTab (String folderId) throws PortalException
  {
      // we could be a bit more careful here and actually check the type
      return ulm.getRootFolderId().equals(ulm.getParentId(folderId));
  }

  /**
   * A folder is a column if its parent is a tab element
   * @param folderId the id of the folder in question
   * @return <code>true</code> if the folder is a column, otherwise <code>false</code>
   */
  private final boolean isColumn (String folderId) throws PortalException
  {
      return isTab(ulm.getParentId(folderId));
  }

  /**
   * Creates a folder element with default attributes.  This method can be used
   * to create a tab or a column.  For tabs, pass the tab name.  For column,
   * pass an empty String since column names aren't meaningful
   * @param name the tab name for tabs and an empty string for columns
   * @return the newly created tab or column
   */
  private final IUserLayoutFolderDescription createFolder (String name)
  {
    String id = "tbd";
    IUserLayoutFolderDescription folder=new UserLayoutFolderDescription();
    folder.setName(name);
    folder.setId(id);
    folder.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
    folder.setHidden(false);
    folder.setUnremovable(false);
    folder.setImmutable(false);
    return folder;
  }

  /**
   * Finds any parameters in a channel that are determined to be overridable
   * by a user.
   * @param channelPublishId an identifier to find the selected channel within the channel registry
   * @return a list of <parameter> elements whose override attribute is set to true
   * @throws org.jasig.portal.PortalException
   */
  private final List getOverridableChannelParams(String channelPublishId) throws PortalException {
    Document channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());
    Element channel = (Element)channelRegistry.getElementById(channelPublishId.startsWith("chan") ? channelPublishId : "chan" + channelPublishId);
    List overridableParams = null;

    if (channel != null) {
      overridableParams = new ArrayList();

      NodeList params = channel.getElementsByTagName("parameter");
      for (int i = 0; i < params.getLength(); i++) {
        Element param = (Element)params.item(i);
        String override = param.getAttribute("override");
        if (override != null && override.equals("yes"))
          overridableParams.add(param);
      }
    } else {
      throw new PortalException("Channel " + channelPublishId + " is missing from the channel registry");
    }
    return overridableParams;
  }


  private void saveUserPreferences () throws PortalException
  {
    userPrefs.getStructureStylesheetUserPreferences().putParameterValue("userLayoutRoot", staticData.getChannelSubscribeId());
    IUserPreferencesManager upm = context.getUserPreferencesManager();
    if (modifyingCurrentUserLayout()) {
        upm.setNewUserLayoutAndUserPreferences(null, userPrefs);
    } else {
      try {
          ulStore.putUserPreferences(staticData.getPerson(), userPrefs);
      } catch (Exception e) {
        throw new PortalException(e);
      }
    }
  }

  private boolean modifyingCurrentUserLayout () throws PortalException
  {
      // check if we're editing the same layout (note: this relies on the layout Ids to be meaningful, which
      // is not entirely true with the current "template user layout" feature. Hopefully this will go away soon.
      return (context.getUserPreferencesManager().getCurrentProfile().getProfileId()==editedUserProfile.getProfileId() && context.getUserPreferencesManager().getCurrentProfile().isSystemProfile()==editedUserProfile.isSystemProfile());
  }

  /**
   * A sub-state of TabColumnPrefsState for visualizing the user layout
   * in tab-column form.
   */
  protected class DefaultState extends BaseState
  {
    private static final boolean printXMLToLog = false;
    private boolean columnHasBeenAdded = false;
    protected TabColumnPrefsState context;

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
            log.error( e);
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
            log.error( e);
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
            log.error( e);
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
            log.error( e);
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
            log.error( e);
            action = "error";
            errorMessage = errorMessageDeleteTab;
          }
        }
        // Lock tab
        else if (action.equals("lockTab"))
        {
          try
          {
            String tabId = runtimeData.getParameter("elementID");

            updateTabLock(tabId, true);
          }
          catch (Exception e)
          {
            log.error( e);
            action = "error";
            errorMessage = errorMessageLockTab;
          }
        }
        // Unlock tab
        else if (action.equals("unlockTab"))
        {
          try
          {
            String tabId = runtimeData.getParameter("elementID");

            updateTabLock(tabId, false);
          }
          catch (Exception e)
          {
            log.error( e);
            action = "error";
            errorMessage = errorMessageUnlockTab;
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
            log.error( e);
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
            // Get the source column if this is a one-step move, otherwise we already
            // have it stored as elementID
            String sourceId = runtimeData.getParameter("sourceID");
            if (sourceId != null)
              elementID = sourceId;

            String method = runtimeData.getParameter("method");
            String destinationId = runtimeData.getParameter("elementID");

            moveColumn(elementID, method, destinationId);
          }
          catch (Exception e)
          {
            log.error( e);
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
            columnHasBeenAdded = true;
          }
          catch (Exception e)
          {
            log.error( e);
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
            log.error( e);
            action = "error";
            errorMessage = errorMessageDeleteColumn;
          }
        }
        // Select channel
        else if (action.equals("selectChannel"))
        {
          elementID = runtimeData.getParameter("elementID");

          // Modify channel parameters
          String subAction = runtimeData.getParameter("subAction");
          if (subAction != null && subAction.equals("modifyChannelParams"))
          {
            IUserLayoutChannelDescription layoutChannel=(IUserLayoutChannelDescription)ulm.getNode(elementID);
            String channelPublishId=layoutChannel.getChannelPublishId();

            Document channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());
            Element channel = (Element)channelRegistry.getElementById("chan" + channelPublishId);
            List overridableChanParams = getOverridableChannelParams(channelPublishId);
            context.internalState = new ParametersState(context, this, overridableChanParams, channel);
            context.internalState.setStaticData(staticData);
          }
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
            // Get the source channel if this is a one-step move, otherwise we already
            // have it stored as elementID
            String sourceId = runtimeData.getParameter("sourceID");
            if (sourceId != null)
              elementID = sourceId;

            String method = runtimeData.getParameter("method");
            String destinationId = runtimeData.getParameter("elementID");

            moveChannel(elementID, method, destinationId);

            // Clear out elementId so the channel doesn't stay highlighted
            elementID = null;
          }
          catch (Exception e)
          {
            log.error( e);
            action = "error";
            errorMessage = errorMessageMoveChannel;
          }
        }
        // Delete channel
        else if (action.equals("deleteChannel"))
        {
          try
          {
            String channelSubscribeId = runtimeData.getParameter("elementID");

            deleteChannel(channelSubscribeId);
          }
          catch (Exception e)
          {
            log.error( e);
            action = "error";
            errorMessage = errorMessageDeleteChannel;
          }
        }
        // Cancel
        else if (action.equals("cancel"))
        {
          elementID = "none";
          // check to see if we added a new column
          if (columnHasBeenAdded){
            removeNewColumn();
            columnHasBeenAdded = false;
          }
        }
      }
      else
        action = "none";
    }

    public void renderXML (ContentHandler out) throws PortalException {
      try {
        // Set up chain: userLayout --> Structure Attributes Incorp. Filter --> out
        TransformerFactory tFactory = TransformerFactory.newInstance();
        if (tFactory instanceof SAXTransformerFactory) {
          SAXTransformerFactory saxTFactory = (SAXTransformerFactory)tFactory;

          // Empty transformer to do the initial dom2sax transition
          Transformer emptytr = tFactory.newTransformer();

          // Stylesheet transformer
          String xslURI = set.getStylesheetURI("default", runtimeData.getBrowserInfo());

          // for i18n
          xslURI= LocaleAwareXSLT.getLocaleAwareXslUri(xslURI, runtimeData.getLocales(), this);

          TransformerHandler th = saxTFactory.newTransformerHandler(XSLT.getTemplates(ResourceLoader.getResourceAsURLString(this.getClass(), xslURI)));
          th.setResult(new SAXResult(out));
          Transformer sstr = th.getTransformer();

          // Set the parameters
          sstr.setParameter("baseActionURL", runtimeData.getBaseActionURL());
          sstr.setParameter("activeTab", activeTab);
          sstr.setParameter("action", action);
          sstr.setParameter("elementID", elementID != null ? elementID : "none");
          sstr.setParameter("errorMessage", errorMessage);

          StructureStylesheetUserPreferences ssup = userPrefs.getStructureStylesheetUserPreferences();
          StructureAttributesIncorporationFilter saif = new StructureAttributesIncorporationFilter(th, ssup);

          // Put a duplicating filter before th
          StringWriter sw = null;
          OutputFormat outputFormat = null;
          if (printXMLToLog) {
            sw = new StringWriter();
            outputFormat = new OutputFormat();
            outputFormat.setIndenting(true);
            XMLSerializer debugSerializer = new XMLSerializer(sw, outputFormat);
            SAX2DuplicatingFilterImpl dupFilter = new SAX2DuplicatingFilterImpl(th, debugSerializer);
            dupFilter.setParent(saif);
          }

          // Incorporate channel registry document into userLayout if user is in the subscribe process
          if (action.equals("newChannel")) {
            Node channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson()).getDocumentElement();
            // start document manually
            saif.startDocument();
            // output layout
            ulm.getUserLayout(new ChannelSAXStreamFilter((ContentHandler)saif));
            emptytr.transform(new DOMSource(channelRegistry),new SAXResult(new ChannelSAXStreamFilter((ContentHandler)saif)));
            // end document manually
            saif.endDocument();
          } else {
              //if (action.equals("moveChannelHere"))
              //System.out.println(org.jasig.portal.utils.XML.serializeNode(userLayout));

              // Begin SAX chain
              //          emptytr.transform(new DOMSource(), new SAXResult(saif));
              ulm.getUserLayout((ContentHandler)saif);
          }

          // Debug piece to print out the recorded pre-structure transformation XML
          if (printXMLToLog) {
            log.debug("TablColumnPrefsState::renderXML() : XML incoming to the structure transformation :\n\n" + sw.toString() + "\n\n");
          }

        } else {
          log.error( "TablColumnPrefsState::renderXML() : Unable to obtain SAX Transformer Factory ! Check your TRAX configuration.");
        }
      } catch (Exception e) {
        log.error( e);
        throw new GeneralRenderingException(e);
      }
    }
  }

  /**
   * A sub-state of TabColumnPrefsState for resetting layout
   */
  protected class ResetLayoutState extends BaseState
  {
    protected TabColumnPrefsState context;

    public ResetLayoutState(TabColumnPrefsState context) {
      this.context = context;
    }

    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
      try {
        editedUserProfile.setLayoutId(0);
        ulStore.updateUserProfile(staticData.getPerson(), editedUserProfile);
        ulm.loadUserLayout();
      } catch (Exception e) {
        throw new PortalException(e);
      }
      // return to the default state
      BaseState df = new DefaultState(context);
      df.setStaticData(staticData);
      context.setState(df);
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
                // save user preferences
                saveUserPreferences();
            } else if (runtimeData.getParameter("submitCancel")!=null) {
                // return to the default state
                BaseState df=new DefaultState(context);
                df.setStaticData(staticData);
                context.setState(df);
            }
        }
    }

    public void renderXML (ContentHandler out) throws PortalException
    {
    	InputStream xmlStream = null;
    try {
      xmlStream = PortalSessionManager.getResourceAsStream(SKINS_PATH + "/skinList.xml");
      String currentSkin = userPrefs.getThemeStylesheetUserPreferences().getParameterValue("skin");

      XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
      xslt.setXML(xmlStream);
      xslt.setXSL(sslLocation, "skinList", runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      xslt.setStylesheetParameter("skinsPath", SKINS_PATH);
      xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
      if(currentSkin!=null)
        xslt.setStylesheetParameter("currentSkin", currentSkin);
      xslt.transform();
      } finally {
				try {
					if (xmlStream != null)
						xmlStream.close();
				} catch (IOException exception) {
					log.error("TabColumnPrefsState:renderXML()::unalbe to close InputStream "+ exception);
				}
			}
    }
  }

  /**
   * A sub-state of TabColumnPrefsState for choosing a new channel (formerly subscribe)
   */
  protected class NewChannelState extends BaseState
  {
    protected TabColumnPrefsState context;
    private String position = "none";
    private String catID = "top";

    public NewChannelState(TabColumnPrefsState context) {
      this.context = context;
    }

    public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
      runtimeData = rd;
      String action = runtimeData.getParameter("action");
      if (action != null) {
        if (action.equals("cancel")) {
          returnToDefaultState();
        } else {
          // User clicked "?"
          if (runtimeData.getParameter("channelMoreInfo") != null) {
            // Implement channel preview here!
            String selectedChannel = runtimeData.getParameter("selectedChannel");
            // Do more...
          } else if (runtimeData.getParameter("addChannel") != null) {
            // User clicked "Add"
            String selectedChannel = runtimeData.getParameter("selectedChannel");
            if (selectedChannel != null) {
              try {
                // Determine whether channel has overridable parameters
                List overridableChanParams = getOverridableChannelParams(selectedChannel);
                if (overridableChanParams.isEmpty()) {
                  addChannel(selectedChannel, position, elementID);
                  returnToDefaultState();
                } else { // present user with screen to specify subscribe-time params
                  Document channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());
                  Element channel = (Element)channelRegistry.getElementById(selectedChannel);
                  context.internalState = new ParametersState(context, this, overridableChanParams, channel, position, elementID);
                  context.internalState.setStaticData(staticData);
                }
              } catch (Exception e) {
                errorMessage = errorMessageNewChannel;
              }
            }
          } else {
            // Collect the position and element ID the first time
            String passedPosition = runtimeData.getParameter("position");
            String passedElementID = runtimeData.getParameter("elementID");
            if (passedPosition != null)
              position = passedPosition;
            if (passedElementID != null)
              elementID = passedElementID;

            // User clicked "Go"
            String selectedCategory = runtimeData.getParameter("selectedCategory");
            if (selectedCategory != null  && selectedCategory.trim().length() > 0)
              catID = selectedCategory;
          }
        }
      }
    }

    public void renderXML (ContentHandler out) throws PortalException
    {
      Document doc = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());

      XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
      xslt.setXML(doc);
      xslt.setXSL(sslLocation, "newChannel", runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
      xslt.setStylesheetParameter("elementID", elementID);
      xslt.setStylesheetParameter("position", position);
      xslt.setStylesheetParameter("catID", catID);
      xslt.setStylesheetParameter("errorMessage", errorMessage);
      xslt.transform();
    }

    private void returnToDefaultState() throws PortalException {
      // Reset global variables
      elementID = "none";
      position = "none";
      action = "none";

      BaseState defaultState = new DefaultState(context);
      defaultState.setStaticData(staticData);
      context.setState(defaultState);
    }
  }

  /**
   * A sub-state of TabColumnPrefsState for setting channel parameters
   */
  protected class ParametersState extends BaseState
  {
    protected TabColumnPrefsState context;
    protected BaseState previousState;
    private List overridableChanParams;
    private Element registryChannel;
    private String position;
    private String destinationElementId;

    private boolean error = false;

    public ParametersState(TabColumnPrefsState context, BaseState previousState, List overridableChanParams, Element registryChannel) {
      this.context = context;
      this.previousState = previousState;
      this.overridableChanParams = overridableChanParams;
      this.registryChannel = registryChannel;
    }

    public ParametersState(TabColumnPrefsState context, BaseState previousState, List overridableChanParams, Element registryChannel, String position, String destinationElementId) {
      this(context, previousState, overridableChanParams, registryChannel);
      this.position = position;
      this.destinationElementId = destinationElementId;
    }

    public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
      runtimeData = rd;
      String action = runtimeData.getParameter("uPTCUP_action");
      if (action != null) {
        if (action.equals("back")) {
          context.setState(previousState);
        } else if (action.equals("finished")) {
          applyChanges(); // Add or modify the channel
          returnToDefaultState();
        } else if (action.equals("cancel")) {
          returnToDefaultState();
        }
      }
    }

    public void renderXML (ContentHandler out) throws PortalException
    {
      XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
      xslt.setXML(getParametersDoc());
      xslt.setXSL(sslLocation, "parameters", runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
      if (error)
        xslt.setStylesheetParameter("errorMessage", errorMessage);
      xslt.transform();
    }

    private void returnToDefaultState() throws PortalException {
      // Reset global variables
        elementID = "none";
        position = "none";
        action = "none";

      BaseState defaultState = new DefaultState(context);
      defaultState.setStaticData(staticData);
      context.setState(defaultState);
    }

    private void applyChanges() {
      // Finally, add the channel to the layout or modify it if it is already there
      try {
        if (previousState instanceof NewChannelState) {
          processParams(registryChannel);
          context.addChannel(registryChannel, position, destinationElementId);
        }
        else if (previousState instanceof DefaultState) {
          updateParams((IUserLayoutChannelDescription)ulm.getNode(elementID));
        }

      } catch (Exception e) {
        error = true;
        errorMessage = errorMessageModChannelParams;
      }
    }

    private void updateParams(IUserLayoutChannelDescription cd) throws PortalException {
      // Process params
      Iterator iter = overridableChanParams.iterator();
      while (iter.hasNext()) {
        Element parameterE = (Element)iter.next();
        String paramName = parameterE.getAttribute("name");
        String paramValue = runtimeData.getParameter(paramName);
        cd.setParameterValue(paramName, paramValue);
      }
      ulm.updateNode(cd);
    }

    private void processParams(Element channel) {
      // Process params
      Iterator iter = overridableChanParams.iterator();
      while (iter.hasNext()) {
        Element parameterE = (Element)iter.next();
        String paramName = parameterE.getAttribute("name");
        String paramValue = runtimeData.getParameter(paramName);

        // Find param within channel and update it
        NodeList params = channel.getElementsByTagName("parameter");
        for (int i = 0; i < params.getLength(); i++) {
          Element paramE = (Element)params.item(i);
          if (paramE.getAttribute("name").equals(paramName)) {
            paramE.setAttribute("value", paramValue);
            break;
          }
        }
      }
    }

    private Document getParametersDoc() throws PortalException {
      Document doc = DocumentFactory.getNewDocument();

      // Top-level element
      Element userPrefParamsE = doc.createElement("userPrefParams");

      if (previousState instanceof NewChannelState)
        userPrefParamsE.appendChild(doc.importNode(registryChannel, true));
      else if (previousState instanceof DefaultState) {
        IUserLayoutNodeDescription node=ulm.getNode(elementID);
        userPrefParamsE.appendChild(node.getXML(doc));
      }

      // CPD
      Document cpd = ChannelRegistryManager.getCPD(registryChannel.getAttribute("typeID"));
      if (cpd != null)
        userPrefParamsE.appendChild(doc.importNode(cpd.getDocumentElement(), true));

      doc.appendChild(userPrefParamsE);
      return doc;
    }
  }

}
