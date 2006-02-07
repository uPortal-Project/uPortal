/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.channels.DLMUserPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelParameter;
import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelSAXStreamFilter;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.StructureAttributesIncorporationFilter;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.StylesheetSet;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.layout.node.UserLayoutFolderDescription;
import org.jasig.portal.layout.UserLayoutManagerFactory;
import org.jasig.portal.layout.dlm.ChannelDescription;
import org.jasig.portal.layout.dlm.Constants;
import org.jasig.portal.layout.dlm.UserPrefsHandler;
import org.jasig.portal.security.IPerson;
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
import org.xml.sax.ContentHandler;


/**
 * This user preferences component is for use with layouts based
 * on the tab-column structure.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class TabColumnPrefsState extends BaseState
{
    private static final Log log = LogFactory.getLog(TabColumnPrefsState.class);
  protected ChannelStaticData staticData;
  protected ChannelRuntimeData runtimeData;
  private static final String sslLocation = "/org/jasig/portal/channels/DLMUserPreferences/tab-column/tab-column.ssl";
  private static final String bundleBaseLocation = 
        "/org/jasig/portal/channels/DLMUserPreferences/tab-column/";

  private IUserLayoutManager ulm;
  private PortalControlStructures pcs;
  private UserPreferences userPrefs;
  private UserProfile editedUserProfile;
  private static IUserLayoutStore ulStore = UserLayoutStoreFactory.getUserLayoutStoreImpl();
  private StylesheetSet set;

  private String action = "none";
  private String activeTab = "none";
  private String elementID = "none";

  // These can be overridden in a sub-class.
  protected static String BLANK_TAB_NAME = "My Tab"; // The tab will take on this name if left blank by the user
  protected static String SKIN_LIST_FILE = "media/org/jasig/portal/layout/tab-column/nested-tables/skinList.xml";
  /**
   * Configuration Object to read resource bundle property values for I18N
   */
  
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

  public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
    this.pcs = pcs;
  }

  public void renderXML(ContentHandler out) throws PortalException
  {
    if (this.internalState != null)
      this.internalState.renderXML(out);
    else
      log.error( "No internal state!");
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
      log.error( "Unable to retrieve active tab.");
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
      	
          throw new PortalException("attempt.to.rename.immutable.tab" +tabId);
      }
      saveLayout(false);
  }

    private boolean getRestriction( String val )
    {
        // This seems kind of backwards but is correct. The checkboxes are
        // labeled "X Allowed" and if checked the values here are "true". If
        // not checked then no value is submitted for that checkbox resulting
        // in a null value here.

        if ( val == null ||
             ! val.equals( "true" ) )
            return false;
        return true;
    }
    private final void changeRestrictions( String id,
                                           String moveAllowed,
                                           String editAllowed,
                                           String addChildAllowed,
                                           String deleteAllowed )
        throws Exception
    {
        IUserLayoutNodeDescription node = ulm.getNode(id);

        node.setDeleteAllowed( getRestriction( deleteAllowed ) );
        node.setMoveAllowed( getRestriction( moveAllowed ) );

        if( node instanceof UserLayoutFolderDescription )
        {
            UserLayoutFolderDescription fldr = null;
            fldr = (UserLayoutFolderDescription) node;
            fldr.setEditAllowed( getRestriction( editAllowed ) );
            fldr.setAddChildAllowed( getRestriction( addChildAllowed ) );
        }
        ulm.updateNode(node);
        saveLayout(false);
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
          saveLayout(false);
      }
  }

  /**
   * Adds a new tab to the layout.
   * @param tabName the name of the tab
   * @param method either <code>insertBefore</code> or <code>appendAfter</code>
   * @param destinationTabId the column to insert the new column before or append after (may actually be a tab)
   * @throws PortalException
   */
  private final void addTab(String tabName,
                            String method,
                            String destinationTabId)
      throws PortalException
  {
    IUserLayoutFolderDescription newTab = createFolder(tabName);

    if (tabName == null || tabName.trim().length() == 0) {
        newTab.setName(BLANK_TAB_NAME);
    }
    String siblingId=null;
    if(method.equals("insertBefore")) 
      siblingId=destinationTabId;
    ulm.addNode(newTab,ulm.getRootFolderId(),siblingId);

    saveLayout(false);
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
          ulm.addNode(newColumn,ulm.getParentId(destinationElementId),siblingId);
      }
      saveLayout(false);     
  }

  private final void changeColumnWidths(HashMap columnWidths) throws Exception
  {
    // Must get from store because the one in memory is comtaminated with stylesheet params
    // that shouldn't get persisted
    //UserPreferences userPrefsFromStore = context.getUserPreferencesFromStore(context.getCurrentUserPreferences().getProfile());
    //StructureStylesheetUserPreferences ssup = userPrefsFromStore.getStructureStylesheetUserPreferences();
    StructureStylesheetUserPreferences ssup = userPrefs.getStructureStylesheetUserPreferences();

    /*
      userPrefs is loaded directly from the database not copied from the
      UserPreferencesManager's value. As such it does not contain current
      session values like the structure stylesheet parameter userLayoutRoot
      which tells the rendering engine the node id that should be displayed
      in focused mode. While in preferences this node id is the node id of the
      preferences channel. That is a session value and should not be preserved.
      But the UserPreferencesManager's structure stylesheet user preferences
      object is being replaced by the version that we have here so that column
      width changes take immediate affect. Therefore, after persisting them we
      need to push the value of the userLayoutRoot node back into the
      preferences now held by the UserPreferencesManager so that the
      preferences channel remains in focus after changing the column widths.
     */
    String focusedNode = context.getUserPreferencesManager()
        .getUserPreferences().getStructureStylesheetUserPreferences()
        .getParameterValue( "userLayoutRoot" );
    String activeTab = context.getUserPreferencesManager()
        .getUserPreferences().getStructureStylesheetUserPreferences()
        .getParameterValue( "activeTab" );
    
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
      {
        ssup.setFolderAttributeValue(folderId, "width", newWidth);
        Element folder = ulm.getUserLayoutDOM().getElementById( folderId );
        UserPrefsHandler.setUserPreference( folder, "width",
                                            staticData.getPerson() );
      }
      else
        if (log.isDebugEnabled()) 
            log.debug("User id " + staticData.getPerson().getID() + " entered invalid column width: " + newWidth);
      	
    }

    // Persist structure stylesheet user preferences
    saveUserPreferences();
    saveLayout(false);
    
    // now push the focused node value back into new rendering prefs object.
    ssup.putParameterValue( "userLayoutRoot", focusedNode );
    
    if ( activeTab != null )
        ssup.putParameterValue( "activeTab", activeTab );
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
      saveLayout(false);
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
      saveLayout(false);
  }

  /**
   * Adds a channel to the layout.
   * @param channel the channel to add
   * @param position either <code>before</code> or <code>after</code>
   * @param destinationElementId the ID of the channel to insert the new channel before or append after
   * @throws PortalException
   */
  private final void addChannel(IUserLayoutChannelDescription channel, 
          String position, String destinationElementId) throws PortalException
    {
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
      saveLayout(true);
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
      addChannel(channel, position, destinationElementId);
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
    deleteElement(channelSubscribeId);
  }

  /**
   * Removes a tab or column element from the layout.  To remove
   * a channel element, call deleteChannel().
   * @param elementId the ID attribute of the element to remove
   */
  private final void deleteElement(String elementId) throws Exception
  {
      ulm.deleteNode(elementId);
      saveLayout(false);
  }

  private final void updateTabLock(String elementId, boolean locked) throws Exception
  {
      // NOTE: this method is to be removed soon.
      /*
    Element element = userLayout.getElementById(elementId);
    if(locked)
    {
      element.setAttribute("unremovable", "true");
      element.setAttribute("immutable", "true");
    }
    else
    {
      element.setAttribute("unremovable", "false");
      element.setAttribute("immutable", "false");
    }
    saveLayout(false);
      */
  }
  /**
   * A folder is a tab if its parent element is the layout element
   * @param folder the folder in question
   * @return <code>true</code> if the folder is a tab, otherwise <code>false</code>
   */
  private final boolean isTab (String folderId) throws PortalException
  {
      // we could be a bit more careful here and actually check the type
      return ulm.getRootFolderId().equals(ulm.getParentId(folderId));
  }

  /**
   * A folder is a column if its parent is a tab element
   * @param folder the folder in question
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
  private final List getOverridableChannelParams(String channelPublishId)
            throws PortalException
    {
        List overridableParams = null;
        ChannelParameter parms[] = getChannelDefParams(channelPublishId);

        if (parms != null && parms.length > 0)
        {
            overridableParams = new ArrayList();
            for (int p = 0; p < parms.length; p++)
            {
                if (parms[p].getOverride())
                    overridableParams.add(parms[p]);
            }
        }
        return overridableParams;
    }
  
  /**
   * For a channel already located in the user's layout this method finds any
   * parameters that are overridable by the user.
   * 
   * @param channelDesc
   *            the IUserLayoutChannelDescription representing the channel 
   *            in the user's layout.
   * @return a list of Channel<parameter>elements whose override attribute is set to
   *         true
   * @throws org.jasig.portal.PortalException
   */
    private final List getOverridableChannelParams(
            IUserLayoutChannelDescription channelDesc) throws PortalException
    {
        // get params in channel description in the layout which can be a super
        // set of those defined in the publish-time definition due to the
        // addition of fragment or user added adhoc values. Then pass through
        // the definition adding overrideable parms to the list and removing
        // the corresponding value from the description map. Any remaining in
        // the description map represent adhoc ones and should be added to the
        // list.

        List overridableParams = null;
        Map descParms = new HashMap(channelDesc.getParameterMap());
        ChannelParameter parms[] = getChannelDefParams(channelDesc
                .getChannelPublishId());

        if (parms != null && parms.length > 0)
        {
            overridableParams = new ArrayList();
            for (int p = 0; p < parms.length; p++)
            {
                if (parms[p].getOverride())
                    overridableParams.add(parms[p]);
                descParms.remove(parms[p].getName());
            }
        }
        if (descParms.size() > 0)
        {
            if (overridableParams == null)
                overridableParams = new ArrayList();

            // description map is a map of name value pairs. need to convert to
            // ChannelParameter instances.
            for(Iterator i = descParms.entrySet().iterator(); i.hasNext();)
            {
                Map.Entry e = (Entry) i.next();
                String name = (String) e.getKey();
                String value = (String) e.getValue();
                ChannelParameter parm = new ChannelParameter(name, value, true);
                overridableParams.add(parm);
            }
        }
        return overridableParams;
    }

    /**
     * Return the list of publish-time-specified channel parameters.
     * 
     * @param id
     * @return
     * @throws PortalException
     */
    private ChannelParameter[] getChannelDefParams(String id) 
    throws PortalException
    {
        IChannelRegistryStore crs = ChannelRegistryStoreFactory
                .getChannelRegistryStoreImpl();
        id = id.startsWith("chan") ? id.substring(4) : id;
        int pubId = Integer.parseInt(id);
        ChannelDefinition def = null;
        try
        {
            def = crs.getChannelDefinition(pubId);
        } catch (Exception e)
        {
            throw new PortalException("unable.to.load.channel.definition "
                    + id, e);
        }
        return def.getParameters();
    }

  private void saveLayout (boolean channelsAdded) throws PortalException
  {
          ulm.saveUserLayout();
  }

  private void saveUserPreferences () throws PortalException
  {
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
            log.error(e, e);
            action = "error";
            errorMessage = errorMessageSetActiveTab;
          }
        }
        // Change tab restrictions
        else if (action.equals("changePermissions" ) )
        {
          try
          {
            String elementId = runtimeData.getParameter("elementID");
            String moveAllowed = runtimeData.getParameter("moveAllowed");
            String editAllowed = runtimeData.getParameter("editAllowed");
            String addChildAllowed = runtimeData.getParameter("addChildAllowed");
            String deleteAllowed = runtimeData.getParameter("deleteAllowed");
            changeRestrictions( elementId, moveAllowed, editAllowed,
                                addChildAllowed, deleteAllowed );
          }
          catch (Exception e)
          {
            log.error(e, e);
            action = "error";
            errorMessage = errorMessageRenameTab;
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
            log.error(e, e);
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
            log.error(e, e);
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
            log.error(e, e);
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
            log.error(e, e);
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
            log.error(e, e);
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
            log.error(e, e);
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
            log.error(e, e);
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
            log.error(e, e);
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
            log.error(e, e);
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
            log.error(e, e);
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
            List overridableChanParams = getOverridableChannelParams(layoutChannel);
            context.internalState = new ParametersState(context, this, overridableChanParams, layoutChannel);
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
            log.error(e, e);
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
            log.error(e, e);
            action = "error";
            errorMessage = errorMessageDeleteChannel;
          }
        }
        // Cancel
        else if (action.equals("cancel"))
        {
          elementID = "none";
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

          // removed for DLM version of prefs channel.
          // use the ResourceBundle approach that the community is going to
          //xslURI= LocaleAwareXSLT.getLocaleAwareXslUri(xslURI, runtimeData.getLocales(), this);
          String urlString = ResourceLoader.getResourceAsURLString(this.getClass(), xslURI);
          ResourceBundle bundle = ResourceBundle.getBundle(bundleBaseLocation+"default",
                  runtimeData.getLocales()[0]);

          Templates templates = XSLT.getTemplates(urlString, bundle);
          TransformerHandler th = saxTFactory.newTransformerHandler(templates);
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
          	
            log.debug("incoming.xml.for.transformation "+
                            sw.toString());
          }

        } else {
          log.error("unable.to.obtain.SAX.Transformer");
        }
      } catch (Exception e) {
        log.error(e, e);
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
        IPerson user  = staticData.getPerson();
        editedUserProfile.setLayoutId(0);
        ulStore.updateUserProfile(user, editedUserProfile);
        user.setAttribute( Constants.PLF, null );
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
                // save user preferences ?
                saveUserPreferences();
                // reset state
                BaseState df=new DefaultState(context);
                df.setStaticData(staticData);
                context.setState(df);
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
      xmlStream = PortalSessionManager.getResourceAsStream(SKIN_LIST_FILE);
      String currentSkin = userPrefs.getThemeStylesheetUserPreferences().getParameterValue("skin");

      XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
      ResourceBundle bundle = ResourceBundle.getBundle(bundleBaseLocation+"skinlist",
              runtimeData.getLocales()[0]);
      xslt.setResourceBundle(bundle);
      xslt.setXML(xmlStream);
      xslt.setXSL(sslLocation, "skinList", runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
      if(currentSkin!=null)
        xslt.setStylesheetParameter("currentSkin", currentSkin);
      xslt.transform();
     } finally {
       try {
         if (xmlStream != null)
           xmlStream.close();
       } catch (IOException exception) {
					log.error("TabColumnPrefsState:renderXML()::unalbe to close InputStream ", exception);
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
                if (overridableChanParams == null || overridableChanParams.isEmpty()) {
                  addChannel(selectedChannel, position, elementID);
                  returnToDefaultState();
                } else { // present user with screen to specify subscribe-time params
                  Document channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());
                  Element channel = channelRegistry.getElementById(selectedChannel);
                  /*
                   * The next line is tightly coupled to DLM which is ok since 
                   * this is the DLM version of the prefs channel. But if we 
                   * rework the ILayoutManager interface to create an 
                   * implementation independant approach to instantiating 
                   * channel descriptions we wouldn't have to tighly couple.
                   */
                  ChannelDescription newChannel = new ChannelDescription(channel);
                  context.internalState = new ParametersState(context, this, overridableChanParams, newChannel, position, elementID);
                  context.internalState.setStaticData(staticData);
                }
              } catch (Exception e) {
                  log.error("Problem occurred adding Channel.", e);
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
      ResourceBundle bundle = ResourceBundle.getBundle(bundleBaseLocation+"newChannel",
              runtimeData.getLocales()[0]);
      xslt.setResourceBundle(bundle);
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
    private IUserLayoutChannelDescription channelDesc;
    private String position;
    private String destinationElementId;

    private boolean error = false;

    public ParametersState(TabColumnPrefsState context, BaseState previousState, List overridableChanParams, IUserLayoutChannelDescription channelDesc) {
      this.context = context;
      this.previousState = previousState;
      this.overridableChanParams = overridableChanParams;
      this.channelDesc = channelDesc;
    }

    public ParametersState(TabColumnPrefsState context, BaseState previousState, List overridableChanParams, IUserLayoutChannelDescription channelDesc, String position, String destinationElementId) {
      this(context, previousState, overridableChanParams, channelDesc);
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
        try
        {
      XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
      ResourceBundle bundle = ResourceBundle.getBundle(bundleBaseLocation+"parameters",
              runtimeData.getLocales()[0]);
      xslt.setResourceBundle(bundle);
      xslt.setXML(getParametersDoc());
      xslt.setXSL(sslLocation, "parameters", runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
      if (error)
        xslt.setStylesheetParameter("errorMessage", errorMessage);
      xslt.transform();
        }
        catch(Exception e)
        {
            log.error(e, e);
        }
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
            processParams();
          context.addChannel(channelDesc, position, destinationElementId);
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
          ChannelParameter parm = (ChannelParameter)iter.next();
          String paramValue = runtimeData.getParameter(parm.getName());
          cd.setParameterValue(parm.getName(), paramValue);
      }
      ulm.updateNode(cd);
      context.saveLayout(false);
    }

    private void processParams() {
        // Process params by passing through the overrideable parms list and
        // pushing the submitted value for that param into the description
        Iterator iter = overridableChanParams.iterator();
        while (iter.hasNext()) {
          ChannelParameter parm = (ChannelParameter)iter.next();
          String paramValue = runtimeData.getParameter(parm.getName());
          channelDesc.setParameterValue(parm.getName(), paramValue);
        }
      }

    private Document getParametersDoc() throws PortalException {
      Document doc = DocumentFactory.getNewDocument();

      // Top-level element
      Element userPrefParamsE = doc.createElement("userPrefParams");

      if (previousState instanceof NewChannelState)
          userPrefParamsE.appendChild(channelDesc.getXML(doc));
      else if (previousState instanceof DefaultState) {
        IUserLayoutNodeDescription node=ulm.getNode(elementID);
        userPrefParamsE.appendChild(node.getXML(doc));
      }

      // CPD
      Document cpd = ChannelRegistryManager.getCPD(channelDesc.getChannelTypeId());
      if (cpd != null)
        userPrefParamsE.appendChild(doc.importNode(cpd.getDocumentElement(), true));

      doc.appendChild(userPrefParamsE);
      return doc;
    }
  }

}
