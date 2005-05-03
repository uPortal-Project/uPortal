/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.UserPreferences;

import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.StructureStylesheetDescription;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.StylesheetSet;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutManagerFactory;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/** <p>Manages User Layout and user stylesheet preferences </p>
 * This is a general UserPreference component. A structure/theme
 * stylesheet is expected to replace this component with its own
 * class to make layout/stylesheet preference management more
 * intuitive.
 * @author Ken Weiner, kweiner@unicon.net
 * @author Peter Kharchenko, pkharchenko@unicon.net
 * @version $Revision$
 */
class GPreferencesState extends BaseState {
    private static final Log log = LogFactory.getLog(GPreferencesState.class);
    private UserProfile profile;
    protected ChannelRuntimeData runtimeData;
    private UserPreferences up = null;
    private IUserLayoutManager ulm=null;
    ThemeStylesheetDescription tsd = null;
    StructureStylesheetDescription ssd = null;
    protected IUserLayoutStore ulsdb = UserLayoutStoreFactory.getUserLayoutStoreImpl();
    // these state variables are kept for the use by the internalStates

    // just a way to refer to the layout element since it doesn't have an ID attribute
    private String folderID = IUserLayout.ROOT_NODE_NAME;
    private boolean modified = false;             // becomes true after user makes changes to layout


    /**
     * Check if the user layout or preferences have been modified.
     *
     * @return a <code>boolean</code> value
     */
    protected boolean isModified() {
        return  modified;
    }

    /**
     * Set modification state
     *
     * @param mod a <code>boolean</code> value
     */
    protected void setModified(boolean mod) {
        this.modified = mod;
    }


    /**
     * Set current folderId.
     *
     * @param id a <code>String</code> value
     */
    protected void setFolderID(String id) {
        this.folderID = id;
        if (folderID == null)
            folderID = IUserLayout.ROOT_NODE_NAME;
    }

    /**
     * Obtain current folderId
     *
     * @return a <code>String</code> value
     */
    protected String getFolderID() {
        if (folderID == null)
            folderID = IUserLayout.ROOT_NODE_NAME;
        return  this.folderID;
    }


    protected String getLayoutRootID() {
        return  IUserLayout.ROOT_NODE_NAME;
    }

  public GPreferencesState() {
    super();
    this.internalState = new GBrowseState(this);
    ulsdb = UserLayoutStoreFactory.getUserLayoutStoreImpl();
  }

    /**
     * Creates a new <code>GPreferencesState</code> for a given context.
     *
     * @param context a <code>CUserPreferences</code> value
     */
    public GPreferencesState(CUserPreferences context) {
    super(context);
    this.internalState = new GBrowseState(this);
  }


  public GPreferencesState(CUserPreferences context, UserProfile p) {
    this.profile = p;
    // initialize in a browse state
    this.internalState = new GBrowseState(this);
  }

  protected IUserLayoutManager getUserLayoutManager() throws PortalException {
      if(this.ulm==null) {
          IUserPreferencesManager upm = context.getUserPreferencesManager();
          // If the we are editing the current user layout, get a copy of the current user layout,
          // otherwise get it from the database or other persistant storage
          if (modifyingCurrentUserLayout()) {
              // get it from the preferences manager
              this.ulm=upm.getUserLayoutManager();
          }  else {
              // construct a new one
              this.ulm=UserLayoutManagerFactory.getUserLayoutManager(upm.getPerson(),context.getCurrentUserPreferences().getProfile());
          }
      }
      return this.ulm;
  }

  private boolean modifyingCurrentUserLayout () throws PortalException {
      // check if we're editing the same layout (note: this relies on the layout Ids to be meaningful, which
      // is not entirely true with the current "template user layout" feature. Hopefully this will go away soon.
      //      return (context.getUserPreferencesManager().getCurrentProfile().getProfileId()==context.getEditedUserProfile().getProfileId() && context.getUserPreferencesManager().getCurrentProfile().isSystemProfile()==context.getEditedUserProfile().isSystemProfile());
      return (context.getUserPreferencesManager().getCurrentProfile().getLayoutId()==context.getEditedUserProfile().getLayoutId());
  }



  protected IUserPreferencesManager getUserPreferencesManager() {
    return  context.getUserPreferencesManager();
  }


  protected UserPreferences getUserPreferences() throws ResourceMissingException, PortalException {
    if (up == null) {
      // load UserPreferences from the DB
      try {
        up = ulsdb.getUserPreferences(context.getUserPreferencesManager().getPerson(), this.getProfile());
        up.synchronizeWithUserLayoutXML(this.getUserLayoutManager().getUserLayoutDOM());
      } catch (Exception e) {
        throw new PortalException(e);
      }
    }
    return  up;
  }


  protected ThemeStylesheetDescription getThemeStylesheetDescription() throws PortalException{
    if (tsd == null) {
      ThemeStylesheetUserPreferences ssup = up.getThemeStylesheetUserPreferences();
      try {
        tsd = ulsdb.getThemeStylesheetDescription(ssup.getStylesheetId());
      } catch (Exception e) {
        throw new PortalException(e);
      }
    }
    return  tsd;
  }

  protected StructureStylesheetDescription getStructureStylesheetDescription() throws ResourceMissingException, PortalException {
    if (ssd == null) {
      StructureStylesheetUserPreferences fsup = this.getUserPreferences().getStructureStylesheetUserPreferences();
      try {
        ssd = ulsdb.getStructureStylesheetDescription(fsup.getStylesheetId());
      } catch (Exception e) {
        throw new PortalException(e);
      }
    }
    return  ssd;
  }


  public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
    if (this.internalState != null) {
      this.internalState.setPortalControlStructures(pcs);
    }
  }


  protected StylesheetSet getStylesheetSet() {
    return  context.getStylesheetSet();
  }


  protected UserProfile getProfile() {
    if (profile == null)
      profile = context.getUserPreferencesManager().getCurrentProfile();
    return  profile;
  }

  public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
    this.runtimeData = rd;
    String action = runtimeData.getParameter("action");
    if (action != null) {
      if (action.equals("submitEditChoice")) {
        String manageTarget = runtimeData.getParameter("userPreferencesAction");
        if (manageTarget.equals("layout")) {
          this.folderID = this.getLayoutRootID();
          // browse mode
          GBrowseState bstate = new GBrowseState(this);
          bstate.setRuntimeData(rd);
          this.internalState = bstate;
        } else if (manageTarget.equals("gpref")) {
          // invoke gpref mode
          GGlobalPrefsState pstate = new GGlobalPrefsState(this);
          pstate.setRuntimeData(rd);
          this.internalState = pstate;
        }
      }
    }

    if (this.internalState != null) {
        this.internalState.setRuntimeData(rd);
    }
  }


  public void renderXML(ContentHandler out) throws PortalException {
      if (this.internalState != null) {
          this.internalState.renderXML(out);
      }
      else {
          log.error( "CUserPreferences.GPreferencesState::renderXML() : no internal state !");
      }
  }

  protected class GEditLayoutItemState extends BaseState {
    protected GPreferencesState context;
    private String editElementID;


    public GEditLayoutItemState(GPreferencesState context) {
      this.context = context;
    }


    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
      String action = runtimeData.getParameter("action");
      if (action != null) {
        if (action.equals("editElement")) {
          editElementID = runtimeData.getParameter("folderID");
        }
        else if (action.equals("submitEditValues")) {
          String submit = runtimeData.getParameter("submit");
          if (submit.equals("Cancel")) {
            // return to the browse state
            IPrivilegedChannel bstate = new GBrowseState(context);
            bstate.setRuntimeData(rd);
            context.setState(bstate);
          }
          else if (submit.equals("Save")) {
            prepareSaveEditedItem();
          }
        }
      }
    }

    private void prepareSaveEditedItem() throws PortalException {
      // update node name
      IUserLayoutNodeDescription node=getUserLayoutManager().getNode(editElementID);
      node.setName(runtimeData.getParameter("name"));
      getUserLayoutManager().updateNode(node);

      // reset the name
      if (node instanceof IUserLayoutChannelDescription) {
        // target is a channel
        StructureStylesheetUserPreferences ssup = context.getUserPreferences().getStructureStylesheetUserPreferences();
        for (Enumeration ce = ssup.getChannelAttributeNames(); ce.hasMoreElements();) {
          String atName = (String)ce.nextElement();
          String atValue = runtimeData.getParameter(atName);
          if (atValue.equals(context.getStructureStylesheetDescription().getChannelAttributeDefaultValue(atName))) {
              atValue = null;
          }

          ssup.setChannelAttributeValue(editElementID, atName, atValue);
        }
        ThemeStylesheetUserPreferences tsup = context.getUserPreferences().getThemeStylesheetUserPreferences();
        for (Enumeration ca = tsup.getChannelAttributeNames(); ca.hasMoreElements();) {
          String atName = (String)ca.nextElement();
          String atValue = runtimeData.getParameter(atName);
          if (atValue.equals(context.getThemeStylesheetDescription().getChannelAttributeDefaultValue(atName))) {
            atValue = null;
          }
          tsup.setChannelAttributeValue(editElementID, atName, atValue);
        }
      } else {
        // target is a folder
        StructureStylesheetUserPreferences ssup = context.getUserPreferences().getStructureStylesheetUserPreferences();
        for (Enumeration fe = ssup.getFolderAttributeNames(); fe.hasMoreElements();) {
          String atName = (String)fe.nextElement();
          String atValue = runtimeData.getParameter(atName);
          if (atValue.equals(context.getStructureStylesheetDescription().getFolderAttributeDefaultValue(atName))) {
            atValue = null;
          }
          ssup.setFolderAttributeValue(editElementID, atName, atValue);
        }
      }
      context.setModified(true);
      // get back to browse mode
      IPrivilegedChannel bstate = new GBrowseState(context);
      bstate.setRuntimeData(runtimeData);
      context.setState(bstate);
    }

    public void renderXML(ContentHandler out) throws PortalException {
        IUserLayoutNodeDescription node=getUserLayoutManager().getNode(editElementID);
        String elType="folder";
        if(node instanceof IUserLayoutChannelDescription) {
            elType="channel";
        }

      // construct the descriptive XML
      Document doc = DocumentFactory.getNewDocument();
      Element edEl = doc.createElement("editelement");
      Element typeEl = doc.createElement("type");
      if (elType.equals("folder")) {
        typeEl.appendChild(doc.createTextNode("folder"));
      } else {
        typeEl.appendChild(doc.createTextNode("channel"));
      }

      edEl.appendChild(typeEl);
      Element nameEl = doc.createElement("name");
      nameEl.appendChild(doc.createTextNode(node.getName()));
      edEl.appendChild(nameEl);
      // determine element type
      if (elType.equals("folder")) {
        // target is a folder
        StructureStylesheetUserPreferences ssup = context.getUserPreferences().getStructureStylesheetUserPreferences();
        Element saEl = doc.createElement("structureattributes");
        for (Enumeration fe = ssup.getFolderAttributeNames(); fe.hasMoreElements();) {
          Element atEl = doc.createElement("attribute");
          Element atNameEl = doc.createElement("name");
          String atName = (String)fe.nextElement();
          atNameEl.appendChild(doc.createTextNode(atName));
          atEl.appendChild(atNameEl);
          Element valueEl = doc.createElement("value");
          String value = ssup.getFolderAttributeValue(editElementID, atName);
          if (value == null) {
            // set the default value
            value = context.getStructureStylesheetDescription().getFolderAttributeDefaultValue(atName);
          }
          valueEl.appendChild(doc.createTextNode(value));
          atEl.appendChild(valueEl);
          Element descrEl = doc.createElement("description");
          descrEl.appendChild(doc.createTextNode(context.getStructureStylesheetDescription().getFolderAttributeWordDescription(atName)));
          atEl.appendChild(descrEl);
          saEl.appendChild(atEl);
        }
        edEl.appendChild(saEl);
      } else if (elType.equals("channel")) {
        // target is a channel
        StructureStylesheetUserPreferences ssup = context.getUserPreferences().getStructureStylesheetUserPreferences();
        Element saEl = doc.createElement("structureattributes");
        for (Enumeration ce = ssup.getChannelAttributeNames(); ce.hasMoreElements();) {
          Element atEl = doc.createElement("attribute");
          Element atNameEl = doc.createElement("name");
          String atName = (String)ce.nextElement();
          atNameEl.appendChild(doc.createTextNode(atName));
          atEl.appendChild(atNameEl);
          Element valueEl = doc.createElement("value");
          String value = ssup.getChannelAttributeValue(editElementID, atName);
          if (value == null) {
            value = context.getStructureStylesheetDescription().getChannelAttributeDefaultValue(atName);
          }
          valueEl.appendChild(doc.createTextNode(value));
          atEl.appendChild(valueEl);
          Element descrEl = doc.createElement("description");
          descrEl.appendChild(doc.createTextNode(context.getStructureStylesheetDescription().getChannelAttributeWordDescription(atName)));
          atEl.appendChild(descrEl);
          saEl.appendChild(atEl);
        }
        edEl.appendChild(saEl);
        ThemeStylesheetUserPreferences tsup = context.getUserPreferences().getThemeStylesheetUserPreferences();
        Element taEl = doc.createElement("themeattributes");
        for (Enumeration ce = tsup.getChannelAttributeNames(); ce.hasMoreElements();) {
          Element atEl = doc.createElement("attribute");
          Element atNameEl = doc.createElement("name");
          String atName = (String)ce.nextElement();
          atNameEl.appendChild(doc.createTextNode(atName));
          atEl.appendChild(atNameEl);
          Element valueEl = doc.createElement("value");
          String value = tsup.getChannelAttributeValue(editElementID, atName);
          if (value == null) {
            value = context.getThemeStylesheetDescription().getChannelAttributeDefaultValue(atName);
          }
          valueEl.appendChild(doc.createTextNode(value));
          atEl.appendChild(valueEl);
          Element descrEl = doc.createElement("description");
          descrEl.appendChild(doc.createTextNode(context.getThemeStylesheetDescription().getChannelAttributeWordDescription(atName)));
          atEl.appendChild(descrEl);
          taEl.appendChild(atEl);
        }
        edEl.appendChild(taEl);
      }
      doc.appendChild(edEl);
      // debug printout of the prepared xml
      try {
        StringWriter outString = new StringWriter();
        /* TODO: This should be reviewed at some point to see if we can use the
         * DOM3 LS capability and hence a standard way of doing this rather
         * than using an internal implementation class.
         */
        OutputFormat format = new OutputFormat();
        format.setOmitXMLDeclaration(true);
        format.setIndenting(true);
        XMLSerializer xsl = new XMLSerializer(outString, format);        
        xsl.serialize(doc);
        log.debug(outString.toString());
      } catch (Exception e) {
        log.debug(e, e);
      }
      StylesheetSet set = context.getStylesheetSet();
      if (set == null) {
          throw  new GeneralRenderingException("Unable to determine the stylesheet list");
      }
      String xslURI = set.getStylesheetURI("editItem", runtimeData.getBrowserInfo());
      if (xslURI != null) {
        XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
        xslt.setXML(doc);
        xslt.setXSL(this.getClass().getResource(xslURI).toString());
        xslt.setTarget(out);
        xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
        xslt.transform();
      } else {
          throw  new ResourceMissingException("", "stylesheet", "Unable to find stylesheet to display content for this media");
      }
    }
  }

  protected class GGlobalPrefsState extends BaseState {
    ChannelRuntimeData runtimeData;
    protected GPreferencesState context;


    public GGlobalPrefsState(GPreferencesState context) {
      this.context = context;
    }

    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
      this.runtimeData = rd;
      // internal state handling
      String action = runtimeData.getParameter("action");
      if (action != null) {
        if (action.equals("submitEditValues")) {
          String submit = runtimeData.getParameter("submit");
          if (submit == null || submit.equals("Save")) {
            prepareSaveEditGPrefs();
          }
          else if (submit.equals("Cancel")) {
            IPrivilegedChannel bstate = new GBrowseState(context);
            bstate.setRuntimeData(runtimeData);
            context.setState(bstate);
          }
        }
      }
    }


    public void renderXML(ContentHandler out) throws PortalException {
      // construct gpref XML
      Document doc = DocumentFactory.getNewDocument();
      Element edEl = doc.createElement("gpref");
      StructureStylesheetUserPreferences ssup = context.getUserPreferences().getStructureStylesheetUserPreferences();
      Element spEl = doc.createElement("structureparameters");
      for (Enumeration e = context.getStructureStylesheetDescription().getStylesheetParameterNames(); e.hasMoreElements();) {
        Element atEl = doc.createElement("parameter");
        Element atNameEl = doc.createElement("name");
        String atName = (String)e.nextElement();
        atNameEl.appendChild(doc.createTextNode(atName));
        atEl.appendChild(atNameEl);
        Element valueEl = doc.createElement("value");
        String value = ssup.getParameterValue(atName);
        if (value == null) {
          // set the default value
          value = context.getStructureStylesheetDescription().getStylesheetParameterDefaultValue(atName);
        }
        valueEl.appendChild(doc.createTextNode(value));
        atEl.appendChild(valueEl);
        Element descrEl = doc.createElement("description");
        descrEl.appendChild(doc.createTextNode(context.getStructureStylesheetDescription().getStylesheetParameterWordDescription(atName)));
        atEl.appendChild(descrEl);
        spEl.appendChild(atEl);
      }
      edEl.appendChild(spEl);
      ThemeStylesheetUserPreferences tsup = context.getUserPreferences().getThemeStylesheetUserPreferences();
      Element tpEl = doc.createElement("themeparameters");
      for (Enumeration e = context.getThemeStylesheetDescription().getStylesheetParameterNames(); e.hasMoreElements();) {
        Element atEl = doc.createElement("parameter");
        Element atNameEl = doc.createElement("name");
        String atName = (String)e.nextElement();
        atNameEl.appendChild(doc.createTextNode(atName));
        atEl.appendChild(atNameEl);
        Element valueEl = doc.createElement("value");
        String value = tsup.getParameterValue(atName);
        if (value == null) {
          // set the default value
          value = context.getThemeStylesheetDescription().getStylesheetParameterDefaultValue(atName);
        }
        valueEl.appendChild(doc.createTextNode(value));
        atEl.appendChild(valueEl);
        Element descrEl = doc.createElement("description");
        descrEl.appendChild(doc.createTextNode(context.getThemeStylesheetDescription().getStylesheetParameterWordDescription(atName)));
        atEl.appendChild(descrEl);
        tpEl.appendChild(atEl);
      }
      edEl.appendChild(tpEl);
      doc.appendChild(edEl);
      // debug printout of the prepared xml
      try {
        StringWriter outString = new StringWriter();
        /* TODO: This should be reviewed at some point to see if we can use the
         * DOM3 LS capability and hence a standard way of doing this rather
         * than using an internal implementation class.
         */
        OutputFormat format = new OutputFormat();
        format.setOmitXMLDeclaration(true);
        format.setIndenting(true);
        XMLSerializer serializer = new XMLSerializer(outString, format);        
        serializer.serialize(doc);
        log.debug(outString.toString());
      } catch (Exception e) {
        log.debug(e, e);
      }
      StylesheetSet set = context.getStylesheetSet();
      if (set == null) {
          throw  new GeneralRenderingException("Unable to determine the stylesheet list");
      }
      String xslURI = set.getStylesheetURI("editGPrefs", runtimeData.getBrowserInfo());
      if (xslURI != null) {
        XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
        xslt.setXML(doc);
        xslt.setXSL(this.getClass().getResource(xslURI).toString());
        xslt.setTarget(out);
        xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
        xslt.transform();
      } else {
          throw  new ResourceMissingException("", "stylesheet", "Unable to find stylesheet to display content for this media");
      }
    }

    private void prepareSaveEditGPrefs() throws PortalException {
      StructureStylesheetUserPreferences ssup = context.getUserPreferences().getStructureStylesheetUserPreferences();
      for (Enumeration e = context.getStructureStylesheetDescription().getStylesheetParameterNames(); e.hasMoreElements();) {
        String parName = (String)e.nextElement();
        String value = runtimeData.getParameter(parName);
        if (value == null) {
          ssup.putParameterValue(parName, context.getStructureStylesheetDescription().getStylesheetParameterDefaultValue(parName));
        } else {
          ssup.putParameterValue(parName, value);
          //		    log.debug("CUserPreferences.GGlobalPrefsState::prepareSaveEditGPrefs() : setting sparameter "+parName+"=\""+value+"\".");
        }
      }
      ThemeStylesheetUserPreferences tsup = context.getUserPreferences().getThemeStylesheetUserPreferences();
      for (Enumeration e = context.getThemeStylesheetDescription().getStylesheetParameterNames(); e.hasMoreElements();) {
        String parName = (String)e.nextElement();
        String value = runtimeData.getParameter(parName);
        if (value == null) {
          tsup.putParameterValue(parName, context.getThemeStylesheetDescription().getStylesheetParameterDefaultValue(parName));
        } else {
          tsup.putParameterValue(parName, value);
          //		    log.debug("CUserPreferences.GGlobalPrefsState::prepareSaveEditGPrefs() : setting tparameter "+parName+"=\""+value+"\".");
        }
      }
      context.setModified(true);
      IPrivilegedChannel bstate = new GBrowseState(context);
      bstate.setRuntimeData(runtimeData);
      context.setState(bstate);
    }
  }

  protected class GBrowseState extends BaseState {
    ChannelRuntimeData runtimeData;
    protected GPreferencesState context;


    public GBrowseState (GPreferencesState context) {
      this.context = context;
    }


    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
      this.runtimeData = rd;
      // internal state handling
      String action = runtimeData.getParameter("action");
      if (action != null) {
        if (action.equals("browse")) {
          String runtimeFolderID = runtimeData.getParameter("folderID");
          if (runtimeFolderID != null) {
            context.setFolderID(runtimeFolderID);
          }
        } else if (action.equals("move")) {
          IPrivilegedChannel mts = new GMoveToState(context);
          mts.setRuntimeData(rd);
          context.setState(mts);
        } else if (action.equals("reorder"))
          prepareReorder();
        else if (action.equals("saveChanges")) {
          prepareSaveChanges();
        } else if (action.equals("editElement")) {
          IPrivilegedChannel eli = new GEditLayoutItemState(context);
          eli.setRuntimeData(rd);
          context.setState(eli);
        }
      }
    }


    public void renderXML(ContentHandler out) throws PortalException {
      StylesheetSet set = context.getStylesheetSet();
      if (set == null) {
          throw  new GeneralRenderingException("Unable to determine the stylesheet list");
      }
      String xslURI = null;
      xslURI = set.getStylesheetURI("browse", runtimeData.getBrowserInfo());
      Hashtable params = new Hashtable();
      params.put("folderID", context.getFolderID());
      params.put("modified", new Boolean(context.isModified()));
      params.put("baseActionURL", runtimeData.getBaseActionURL());
      params.put("profileName", context.getProfile().getProfileName());
      if (context.getProfile().isSystemProfile()) {
        params.put("profileType", "system");
      } else {
        params.put("profileType", "user");
      }
      if (xslURI != null) {
        XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
        xslt.setXML(context.getUserLayoutManager().getUserLayoutDOM());
        xslt.setXSL(this.getClass().getResource(xslURI).toString());
        xslt.setTarget(out);
        xslt.setStylesheetParameters(params);
        xslt.transform();
      }
      else {
          throw  new ResourceMissingException("", "stylesheet", "Unable to find stylesheet to display content for this media");
      }
    }

    private void prepareSaveChanges() throws PortalException {
      context.setFolderID(context.getLayoutRootID());
      context.setModified(false);
      // relate changes back to the UserPreferencesManager if the profile that's being
      // edited is the current profile.
      // changes in userLayoutXML are always related back to the UserPreferencesManager.
      // (unless profile-specific layouts will be introduced)
      if (context.getUserPreferencesManager().getCurrentProfile() == context.getProfile()) {
          context.getUserPreferencesManager().setNewUserLayoutAndUserPreferences(context.getUserLayoutManager(), context.getUserPreferences());
      } else {
        // do a database save on the preferences
        try {
            // persist layout
            context.getUserLayoutManager().saveUserLayout();
            ulsdb.putUserPreferences(context.getUserPreferencesManager().getPerson(), context.getUserPreferences());
        } catch (Exception e) {
            throw new PortalException(e);
        }
      }
    }

    private void prepareReorder() throws PortalException {
      String folderID = runtimeData.getParameter("elementID");                  // the folder or channel ID
      String direction = runtimeData.getParameter("dir");       // "up" or "down"

      IUserLayoutManager lm=context.getUserLayoutManager();
      if (direction.equals("up")) {
          String prevSiblingId=lm.getPreviousSiblingId(folderID);
          if(prevSiblingId!=null) {
              lm.moveNode(folderID,lm.getParentId(folderID),prevSiblingId);
          }
          //        for (prev = element.getPreviousSibling(); prev != null && prev.getNodeType() != Node.ELEMENT_NODE && (!prev.getNodeName().equals("channel")  || !prev.getNodeName().equals("folder")); prev = prev.getPreviousSibling());
      } else if (direction.equals("down")) {
          String nextSiblingId=lm.getNextSiblingId(folderID);
          if(nextSiblingId!=null) {
              lm.moveNode(folderID,lm.getParentId(folderID),nextSiblingId);
          }
          //        for(next = element.getNextSibling(); next != null && next.getNodeType() != Node.ELEMENT_NODE && (!next.getNodeName().equals("channel") || !next.getNodeName().equals("folder")); next = next.getNextSibling());
      }
      context.setModified(true);
    }
  }

  protected class GMoveToState extends BaseState {
    private String[] moveIDs = null;            // contains the IDs of channels/folders to be moved
    protected ChannelRuntimeData runtimeData;
    protected GPreferencesState context;


    public GMoveToState(GPreferencesState context) {
      this.context = context;
    }

    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
      this.runtimeData = rd;
      String action = runtimeData.getParameter("action");
      if (action != null) {
        if (action.equals("cancel")) {
        //			prepareCancel ();
        } else if (action.equals("move"))
          prepareMove();
        else if (action.equals("moveTo"))
          prepareMoveTo();
      }
    }


    public void renderXML (ContentHandler out) throws PortalException {
      StylesheetSet set = context.getStylesheetSet();
      if (set == null)
        throw  new GeneralRenderingException("Unable to determine the stylesheet list");
      String xslURI = set.getStylesheetURI("moveTo", runtimeData.getBrowserInfo());
      if (xslURI != null) {
        XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
        xslt.setXML(context.getUserLayoutManager().getUserLayoutDOM());
        xslt.setXSL(this.getClass().getResource(xslURI).toString());
        xslt.setTarget(out);
        xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
        xslt.transform();
      }
      else
        throw  new ResourceMissingException("", "stylesheet", "Unable to find stylesheet to display content for this media");
    }


    private void prepareMove () {
      // getParameterValues() should be a method in ChannelRuntimeData.
      // For now, I'll use the request object -- ask Peter about this!
      moveIDs = runtimeData.getParameterValues("move");
    }

    private void prepareMoveTo () throws PortalException {
      String destinationID = runtimeData.getParameter("destination");
      for (int i = 0; i < moveIDs.length; i++) {
          getUserLayoutManager().moveNode(moveIDs[i],destinationID,null);
      }
      context.setModified(true);
      IPrivilegedChannel bstate = new GBrowseState(context);
      bstate.setRuntimeData(runtimeData);
      context.setState(bstate);
    }
  }
}



