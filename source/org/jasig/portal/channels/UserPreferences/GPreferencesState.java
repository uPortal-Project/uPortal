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


package  org.jasig.portal.channels.UserPreferences;

import  org.jasig.portal.*;
import  org.jasig.portal.utils.XSLT;
import  org.jasig.portal.services.LogService;
import  org.w3c.dom.Document;
import  org.w3c.dom.Element;
import  org.w3c.dom.Node;
import  org.xml.sax.ContentHandler;
import  java.io.StringWriter;
import  java.util.Enumeration;
import  java.util.Hashtable;
import  java.net.URL;


/** <p>Manages User Layout and user stylesheet preferences </p>
 * This is a general UserPreference component. A structure/theme
 * stylesheet is expected to replace this component with its own
 * class to make layout/stylesheet preference management more
 * intuitive.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */
class GPreferencesState extends BaseState {
    private UserProfile profile;
    protected ChannelRuntimeData runtimeData;
    private UserPreferences up = null;
    private Document userLayoutXML = null;
    ThemeStylesheetDescription tsd = null;
    StructureStylesheetDescription ssd = null;
    protected IUserLayoutStore ulsdb = UserLayoutStoreFactory.getUserLayoutStoreImpl();
    // these state variables are kept for the use by the internalStates

    // just a way to refer to the layout element since it doesn't have an ID attribute
    private static final String layoutID = "top";                
    private String folderID = layoutID;
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
            folderID = layoutID;
    }

    /**
     * Obtain current folderId
     *
     * @return a <code>String</code> value
     */
    protected String getFolderID() {
        if (folderID == null)
            folderID = layoutID;
        return  this.folderID;
    }

    
    protected String getLayoutRootID() {
        return  layoutID;
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

  protected Document getUserLayoutXML() throws PortalException {
    if (userLayoutXML == null) {
      // get the layout from the database
      try {
        userLayoutXML = UserLayoutStoreFactory.getUserLayoutStoreImpl().getUserLayout(context.getUserLayoutManager().getPerson(), context.getCurrentUserPreferences().getProfile());
      } catch (Exception e) {
        LogService.instance().log(LogService.ERROR, e);
        throw  new GeneralRenderingException(e.getMessage());
      }
    }
    return  userLayoutXML;
  }


  protected IUserLayoutManager getUserLayoutManager() {
    return  context.getUserLayoutManager();
  }


  protected UserPreferences getUserPreferences() throws ResourceMissingException, PortalException {
    if (up == null) {
      // load UserPreferences from the DB
      try {
        up = ulsdb.getUserPreferences(context.getUserLayoutManager().getPerson(), this.getProfile());
        up.synchronizeWithUserLayoutXML(this.getUserLayoutXML());
      } catch (Exception e) {
        throw new PortalException(e.getMessage(), e);
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
        throw new PortalException(e.getMessage(), e);
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
        throw new PortalException(e.getMessage(), e);
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
      profile = context.getUserLayoutManager().getCurrentProfile();
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
          LogService.instance().log(LogService.ERROR, "CUserPreferences.GPreferencesState::renderXML() : no internal state !");
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
      Element target = context.getUserLayoutXML().getElementById(editElementID);
      String elType = target.getTagName();
      // reset the name
      target.setAttribute("name", runtimeData.getParameter("name"));
      if (elType.equals("folder")) {
        // target is a folder
        StructureStylesheetUserPreferences ssup = context.getUserPreferences().getStructureStylesheetUserPreferences();
        for (Enumeration fe = ssup.getFolderAttributeNames(); fe.hasMoreElements();) {
          String atName = (String)fe.nextElement();
          String atValue = runtimeData.getParameter(atName);
          if (atValue.equals(context.getStructureStylesheetDescription().getFolderAttributeDefaultValue(atName)))
            atValue = null;
          ssup.setFolderAttributeValue(editElementID, atName, atValue);
        }
      } else if (elType.equals("channel")) {
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
          if (atValue.equals(context.getThemeStylesheetDescription().getChannelAttributeDefaultValue(atName)))
            atValue = null;
          tsup.setChannelAttributeValue(editElementID, atName, atValue);
        }
      }
      context.setModified(true);
      // get back to browse mode
      IPrivilegedChannel bstate = new GBrowseState(context);
      bstate.setRuntimeData(runtimeData);
      context.setState(bstate);
    }

    public void renderXML(ContentHandler out) throws PortalException {
      Element target = context.getUserLayoutXML().getElementById(editElementID);
      String elType = target.getTagName();
      // construct the descriptive XML
      Document doc = new org.apache.xerces.dom.DocumentImpl();
      Element edEl = doc.createElement("editelement");
      Element typeEl = doc.createElement("type");
      if (elType.equals("folder")) {
        typeEl.appendChild(doc.createTextNode("folder"));
      } else {
        typeEl.appendChild(doc.createTextNode("channel"));
      }

      edEl.appendChild(typeEl);
      Element nameEl = doc.createElement("name");
      nameEl.appendChild(doc.createTextNode(target.getAttribute("name")));
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
        org.apache.xml.serialize.OutputFormat format = new org.apache.xml.serialize.OutputFormat();
        format.setOmitXMLDeclaration(true);
        format.setIndenting(true);
        org.apache.xml.serialize.XMLSerializer xsl = new org.apache.xml.serialize.XMLSerializer(outString, format);
        xsl.serialize(doc);
        LogService.instance().log(LogService.DEBUG, outString.toString());
      } catch (Exception e) {
        LogService.instance().log(LogService.DEBUG, e);
      }
      StylesheetSet set = context.getStylesheetSet();
      if (set == null) {
          throw  new GeneralRenderingException("Unable to determine the stylesheet list");
      }
      String xslURI = set.getStylesheetURI("editItem", runtimeData.getBrowserInfo());
      if (xslURI != null) {
        XSLT xslt = new XSLT(this);
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
      Document doc = new org.apache.xerces.dom.DocumentImpl();
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
        org.apache.xml.serialize.OutputFormat format = new org.apache.xml.serialize.OutputFormat();
        format.setOmitXMLDeclaration(true);
        format.setIndenting(true);
        org.apache.xml.serialize.XMLSerializer xsl = new org.apache.xml.serialize.XMLSerializer(outString, format);
        xsl.serialize(doc);
        LogService.instance().log(LogService.DEBUG, outString.toString());
      } catch (Exception e) {
        LogService.instance().log(LogService.DEBUG, e);
      }
      StylesheetSet set = context.getStylesheetSet();
      if (set == null) {
          throw  new GeneralRenderingException("Unable to determine the stylesheet list");
      }
      String xslURI = set.getStylesheetURI("editGPrefs", runtimeData.getBrowserInfo());
      if (xslURI != null) {
        XSLT xslt = new XSLT(this);
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
          //		    LogService.instance().log(LogService.DEBUG,"CUserPreferences.GGlobalPrefsState::prepareSaveEditGPrefs() : setting sparameter "+parName+"=\""+value+"\".");
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
          //		    LogService.instance().log(LogService.DEBUG,"CUserPreferences.GGlobalPrefsState::prepareSaveEditGPrefs() : setting tparameter "+parName+"=\""+value+"\".");
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
        XSLT xslt = new XSLT(this);
        xslt.setXML(context.getUserLayoutXML());
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
      // relate changes back to the UserLayoutManager if the profile that's being
      // edited is the current profile.
      // changes in userLayoutXML are always related back to the UserLayoutManager.
      // (unless profile-specific layouts will be introduced)
      if (context.getUserLayoutManager().getCurrentProfile() == context.getProfile()) {
        context.getUserLayoutManager().setNewUserLayoutAndUserPreferences(context.getUserLayoutXML(), context.getUserPreferences(), false);
      } else {
        // do a database save on the preferences
        try {
          ulsdb.putUserPreferences(context.getUserLayoutManager().getPerson(), context.getUserPreferences());
        } catch (Exception e) {
          throw new PortalException(e.getMessage(), e);
        }
        context.getUserLayoutManager().setNewUserLayoutAndUserPreferences(context.getUserLayoutXML(), null, false);
      }
    }

    private void prepareReorder() throws PortalException {
      String folderID = runtimeData.getParameter("elementID");                  // the folder or channel ID
      String direction = runtimeData.getParameter("dir");       // "up" or "down"
      Node element = context.getUserLayoutXML().getElementById(folderID);
      Node parent = element.getParentNode();
      if (direction.equals("up")) {
        Node prev;
        // Goto the previous channel or folder element
        for (prev = element.getPreviousSibling(); prev != null && prev.getNodeType() != Node.ELEMENT_NODE && (!prev.getNodeName().equals("channel")
            || !prev.getNodeName().equals("folder")); prev = prev.getPreviousSibling());
        parent.insertBefore(element, prev);
      }
      else if (direction.equals("down")) {
        Node next;
        // Goto the next channel or folder element
        for(next = element.getNextSibling(); next != null && next.getNodeType() != Node.ELEMENT_NODE && (!next.getNodeName().equals("channel") || !next.getNodeName().equals("folder")); next = next.getNextSibling());
        parent.insertBefore(next, element);
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
        XSLT xslt = new XSLT(this);
        xslt.setXML(context.getUserLayoutXML());
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
      Node destination = null;
      if (destinationID == null) {
        LogService.instance().log(LogService.ERROR, "CUserPreferences::prepareMove() : received a null destinationID !");
      } else {
          if (destinationID.equals(context.getLayoutRootID())) {
              destination = context.getUserLayoutXML().getDocumentElement();        // the layout element
          } else {
              destination = context.getUserLayoutXML().getElementById(destinationID);
          }
          if (destination == null) {
              LogService.instance().log(LogService.ERROR, "CUserPreferences::prepareMove() : destinationID=\"" + destinationID + "\" results in an empty node !");
          } else {
              for (int i = 0; i < moveIDs.length; i++) {
                  Node relocating = context.getUserLayoutXML().getElementById(moveIDs[i]);
                  destination.insertBefore(relocating, null);         // adds to end of children nodes
              }
              context.setModified(true);
              IPrivilegedChannel bstate = new GBrowseState(context);
              bstate.setRuntimeData(runtimeData);
              context.setState(bstate);
          }
      }
    }
  }
}



