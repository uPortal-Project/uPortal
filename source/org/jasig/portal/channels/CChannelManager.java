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

package org.jasig.portal.channels;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.UtilitiesBean;
import org.jasig.portal.PortalException;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.factories.DocumentFactory;
import org.xml.sax.DocumentHandler;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.sql.SQLException;

/**
 * <p>Manages channels, replaces CPublisher</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CChannelManager extends BaseChannel {
  protected static final String sslLocation = UtilitiesBean.fixURI("webpages/stylesheets/org/jasig/portal/channels/CChannelManager/CChannelManager.ssl");
  protected static final Document emptyDoc = DocumentFactory.getNewDocument();
  protected short state;
  protected static final short DEFAULT_STATE = 0;
  protected static final short CHANNEL_TYPE_STATE = 1;
  protected static final short GENERAL_SETTINGS_STATE = 2;
  protected static final short CHANNEL_DEF_STATE = 3;
  protected static final short CHANNEL_CONTROLS_STATE = 4;
  protected static final short CHANNEL_CATEGORIES_STATE = 5;
  protected static final short CHANNEL_ROLES_STATE = 6;
  protected static final short CHANNEL_REVIEW_STATE = 7;
  protected static final short MODIFY_CHANNEL_STATE = 8;
  protected String action;
  protected String stepID;
  protected Document channelManagerDoc;
  protected ChannelDefinition channelDef = new ChannelDefinition();
  protected UserSettings userSettings = new UserSettings();
  protected CategorySettings categorySettings = new CategorySettings();
  protected RoleSettings roleSettings = new RoleSettings();

  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
    runtimeData = rd;
    action = runtimeData.getParameter("uPCM_action");
    captureChanges(); // Keep after "action = " because action might change inside captureChanges
    if (action != null) {
      if (action.equals("selectChannelType")) {

        state = CHANNEL_TYPE_STATE;
        Workflow workflow = new Workflow();

        // Add channel types and channel def
        WorkflowSection chanTypeSection = new WorkflowSection("selectChannelType");
        WorkflowStep step = new WorkflowStep("1", "Channel Type");
        step.addDataElement(ChannelRegistryManager.getChannelTypes().getDocumentElement());
        step.addDataElement(channelDef.toXML());
        chanTypeSection.addStep(step);
        workflow.setChannelTypesSection(chanTypeSection);

        // Add CPD document if channel is "generic"
        String channelTypeID = channelDef.getTypeID();
        if (channelTypeID != null && !channelTypeID.equals("-1")) {
          CPDWorkflowSection cpdSection = new CPDWorkflowSection(channelDef.getTypeID());
          workflow.setCPDSection(cpdSection);
        }

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("selectGeneralSettings")) {

        state = GENERAL_SETTINGS_STATE;
        Workflow workflow = new Workflow();

        // Add General Settings section
        WorkflowSection gsSection = new WorkflowSection("selectGeneralSettings");
        workflow.setGeneralSettingsSection(gsSection);
        WorkflowStep step = new WorkflowStep("1", "General Settings");
        step.addDataElement(channelDef.toXML());
        gsSection.addStep(step);

        // Add CPD document
        CPDWorkflowSection section = new CPDWorkflowSection(channelDef.getTypeID());
        workflow.setCPDSection(section);

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("channelDef")) {

        state = CHANNEL_DEF_STATE;
        Workflow workflow = new Workflow();

        // Add CPD document
        CPDWorkflowSection cpdSection = new CPDWorkflowSection(channelDef.getTypeID());
        cpdSection.addToStep(channelDef.toXML(), fixStepID(stepID));
        workflow.setCPDSection(cpdSection);

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("selectControls")) {

        state = CHANNEL_CONTROLS_STATE;
        Workflow workflow = new Workflow();

        // Add CPD document
        CPDWorkflowSection section = new CPDWorkflowSection(channelDef.getTypeID());
        workflow.setCPDSection(section);

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("selectCategories")) {

        state = CHANNEL_CATEGORIES_STATE;
        Workflow workflow = new Workflow();

        // Add CPD document
        CPDWorkflowSection cpdSection = new CPDWorkflowSection(channelDef.getTypeID());
        workflow.setCPDSection(cpdSection);

        // Add channel registry
        WorkflowSection catSection = new WorkflowSection("selectCategories");
        workflow.setCategoriesSection(catSection);
        WorkflowStep step = new WorkflowStep("1", "Categories");
        step.addDataElement(ChannelRegistryManager.getChannelRegistry().getDocumentElement());
        // Add user settings with previously chosen categories
        step.addDataElement(categorySettings.toXML());
        catSection.addStep(step);

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("selectRoles")) {

        state = CHANNEL_ROLES_STATE;
        Workflow workflow = new Workflow();

        // Add CPD document
        CPDWorkflowSection cpdSection = new CPDWorkflowSection(channelDef.getTypeID());
        workflow.setCPDSection(cpdSection);

        // Add roles
        WorkflowSection roleSection = new WorkflowSection("selectRoles");
        workflow.setRolesSection(roleSection);
        WorkflowStep step = new WorkflowStep("1", "Roles");
        step.addDataElement(getRoles().getDocumentElement());
        // Add user settings with previously chosen roles
        step.addDataElement(roleSettings.toXML());
        roleSection.addStep(step);

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("reviewChannel")) {

        state = CHANNEL_REVIEW_STATE;
        Workflow workflow = new Workflow();

        WorkflowSection reviewSection = new WorkflowSection("reviewChannel");
        workflow.setReviewSection(reviewSection);
        WorkflowStep step = new WorkflowStep("1", "Review");

        // Channel Definition
        step.addDataElement(channelDef.toXML());
        // Selected categories
        // Channel registry
        step.addDataElement(ChannelRegistryManager.getChannelRegistry().getDocumentElement());
        // Selected roles
        step.addDataElement(roleSettings.toXML());
        // Channel types
        step.addDataElement(ChannelRegistryManager.getChannelTypes().getDocumentElement());

        reviewSection.addStep(step);

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("selectModifyChannel")) {

        state = MODIFY_CHANNEL_STATE;
        channelManagerDoc = getChannelManagerDoc(userSettings);
      } else if (action.equals("changePage")) {
        String newPage = runtimeData.getParameter("newPage");
        if (newPage != null) {
          userSettings.setCurrentPage(newPage);
          channelManagerDoc = getChannelManagerDoc(userSettings);
        }
      } else if (action.equals("changeRecordsPerPage")) {
        String recordsPerPage = runtimeData.getParameter("recordsPerPage");
        if (recordsPerPage != null) {
          userSettings.setRecordsPerPage(recordsPerPage);
          channelManagerDoc = getChannelManagerDoc(userSettings);
        }
      } else if (action.equals("filterByCategory")) {
        String filterByID = runtimeData.getParameter("newCategory");
        if (filterByID != null) {
          userSettings.setFilterByID(filterByID);
          channelManagerDoc = getChannelManagerDoc(userSettings);
        }
      }
    }

    if (action == null || action.equals("cancel")) {
      state = DEFAULT_STATE;
      channelManagerDoc = emptyDoc;
    }
  }

  public void renderXML (DocumentHandler out) throws PortalException {
    XSLT xslt = new XSLT();
    xslt.setXML(channelManagerDoc);
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());

    String action = null;
    switch (state) {
      case DEFAULT_STATE:
        action = "none";
        break;
      case CHANNEL_TYPE_STATE:
        action = "selectChannelType";
        break;
      case GENERAL_SETTINGS_STATE:
        action = "selectGeneralSettings";
        break;
      case CHANNEL_DEF_STATE:
        action = "channelDef";
        xslt.setStylesheetParameter("stepID", fixStepID(stepID));
        break;
      case CHANNEL_CONTROLS_STATE:
        action = "selectControls";
        break;
      case CHANNEL_CATEGORIES_STATE:
        action = "selectCategories";
        break;
      case CHANNEL_ROLES_STATE:
        action = "selectRoles";
        break;
      case CHANNEL_REVIEW_STATE:
        action = "reviewChannel";
        break;
      case MODIFY_CHANNEL_STATE:
        action = "selectModifyChannel";
        break;
      default:
        action = "none";
        break;
    }

    xslt.setStylesheetParameter("action", action);
    // Temporary mediaPath param - makes it easier for Justin and I to work together
    xslt.setStylesheetParameter("mediaPath", "media/org/jasig/portal/channels/CChannelManager");
    xslt.transform();

    // Remove this!!!
    try {
      if (false) {
        System.out.println("-----------------------------------------------");
        System.out.println("baseActionURL=" + runtimeData.getBaseActionURL());
        System.out.println("action=" + action);
        System.out.println("stepID=" + stepID);
        System.out.println("fixedStepID=" + fixStepID(stepID));
        System.out.println(UtilitiesBean.dom2PrettyString(channelManagerDoc));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String fixStepID (String stepID) {
    if (stepID == null) {
      stepID = "1";
    } else {
      try {
        Integer.parseInt(stepID);
      }
      catch (java.lang.NumberFormatException nfe) {
        stepID = "1";
      }
    }
    return stepID;
  }

  protected void captureChanges() {
    String capture = runtimeData.getParameter("uPCM_capture");
    if (capture != null) {
      // Channel types
      if (capture.equals("selectChannelType")) {
        String typeID = runtimeData.getParameter("ID");
        if (typeID != null)
          channelDef.setTypeID(typeID);
        else
          action = "selectChannelType";
      // General Settings (name and timeout)
      } else if (capture.equals("selectGeneralSettings")) {
        String name = runtimeData.getParameter("name");
        String timeout = runtimeData.getParameter("timeout");
        if (name != null)
          channelDef.setName(name);
        if (timeout != null)
          channelDef.setTimeout(timeout);
      // CPD parameters
      } else if (capture.equals("channelDef")) {
        stepID = runtimeData.getParameter("uPCM_step");
        Iterator iter = ((java.util.Hashtable)runtimeData).keySet().iterator();
        while (iter.hasNext()) {
          String name = (String)iter.next();

          // Ignore parameters whose name starts with "uPCM_"
          if (name.startsWith("uPCM_"))
            continue;

          String value = runtimeData.getParameter(name);
          String modType = "unknown"; // ???? what should we do here ????
          channelDef.addParameter(name, value, modType);
        }
      // Channel controls
      } else if (capture.equals("selectControls")) {
        String minimizable = runtimeData.getParameter("minimizable");
        channelDef.setMinimizable(minimizable != null ? "true" : "false");
        String editable = runtimeData.getParameter("editable");
        channelDef.setEditable(editable != null ? "true" : "false");
        String hasHelp = runtimeData.getParameter("hasHelp");
        channelDef.setHasHelp(hasHelp != null ? "true" : "false");
        String hasAbout = runtimeData.getParameter("hasAbout");
        channelDef.setHasAbout(hasAbout != null ? "true" : "false");
        String printable = runtimeData.getParameter("printable");
        channelDef.setPrintable(printable != null ? "true" : "false");
        String removable = runtimeData.getParameter("removable");
        channelDef.setRemovable(removable != null ? "true" : "false");
        String detachable = runtimeData.getParameter("detachable");
        channelDef.setDetachable(detachable != null ? "true" : "false");
      // Categories
      } else if (capture.equals("selectCategories")) {
        String selectedCategory = runtimeData.getParameter("selectedCategory");
        if (selectedCategory != null && selectedCategory.trim().length() > 0) {
          if (runtimeData.getParameter("uPCM_browse") != null)
            categorySettings.setBrowsingCategory(selectedCategory);
          else // runtimeData.getParameter("uPCM_select") != null
            categorySettings.addSelectedCategory(selectedCategory);
        }
      }
    }
  }

  protected Document getChannelManagerDoc(UserSettings userSettings) throws PortalException {
    Document channelManagerDoc = DocumentFactory.getNewDocument();

    // Add the top level <manageChannels> to the document
    Element channelManager = channelManagerDoc.createElement("manageChannels");
    channelManagerDoc.appendChild(channelManager);

    // Get the channel registry
    Document channelRegistryDoc = ChannelRegistryManager.getChannelRegistry();

    // Set the registry ID attribute to "-1"
    Element registry = channelRegistryDoc.getDocumentElement();
    registry.setAttribute("ID", "-1");

    // Add the <registry> to <manageChannels>
    Element channelRegistry = (Element)channelManagerDoc.importNode(channelRegistryDoc.getDocumentElement(), true);
    channelManager.appendChild(channelRegistry);

    // Add a <userSettings> fragment to <manageChannels>
    appendUserSettings(channelManager, userSettings);

    return channelManagerDoc;
  }

  protected static void appendUserSettings(Element channelManager, UserSettings userSettings) {
    Document doc = channelManager.getOwnerDocument();
    Element userSettingsE = doc.createElement("userSettings");
    Element modifyView = doc.createElement("modifyView");
    userSettingsE.appendChild(modifyView);
    Element recordsPerPageE = doc.createElement("recordsPerPage");
    recordsPerPageE.appendChild(doc.createTextNode(userSettings.getRecordsPerPage()));
    modifyView.appendChild(recordsPerPageE);
    Element currentPageE = doc.createElement("currentPage");
    currentPageE.appendChild(doc.createTextNode(userSettings.getCurrentPage()));
    modifyView.appendChild(currentPageE);
    Element filterByIDE = doc.createElement("filterByID");
    filterByIDE.appendChild(doc.createTextNode(userSettings.getFilterByID()));
    modifyView.appendChild(filterByIDE);
    channelManager.appendChild(userSettingsE);
  }

  protected static Document getCPDDoc(String chanTypeID) throws PortalException {
    Element channelTypes = ChannelRegistryManager.getChannelTypes().getDocumentElement();

    // Look for channel type element matching the channel type ID
    Element chanType = null;
    for (Node n = channelTypes.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("channelType")) {
        chanType = (Element)n;
        if (chanTypeID.equals(chanType.getAttribute("ID")))
          break;
      }
    }

    // Find the cpd-uri within this element
    String cpdUri = null;
    for (Node n = chanType.getLastChild(); n != null; n = n.getPreviousSibling()) {
      if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("cpd-uri")) {
        // Found the <cpd-uri> element, now get its value
        for (Node m = n.getFirstChild(); m != null; m = m.getNextSibling()) {
          if (m instanceof Text)
            cpdUri = m.getNodeValue();
        }
        break;
      }
    }

    Document cpdDoc = null;
    if (cpdUri != null) {
      try {
        org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
        parser.parse(UtilitiesBean.fixURI(cpdUri));
        cpdDoc = parser.getDocument();
      } catch (java.io.IOException ioe) {
        throw new ResourceMissingException(cpdUri, "Channel publishing document", ioe.getMessage());
      } catch (org.xml.sax.SAXException se) {
        throw new GeneralRenderingException("Unable to parse CPD file: " + se.getMessage());
      }
    }
    return cpdDoc;
  }

  // This method needs some caching!!!
  protected static Document getRoles() {
    Document roleDoc = DocumentFactory.getNewDocument();
    org.jasig.portal.security.IAuthorization authorization = new org.jasig.portal.security.provider.ReferenceAuthorizationFactory().getAuthorization();
    java.util.Vector vRoles = authorization.getAllRoles();
    Element rolesE = roleDoc.createElement("roles");
    for (int i = 0; i < vRoles.size(); i++) {
      Element roleE = roleDoc.createElement("role");
      Element nameE = roleDoc.createElement("name");
      nameE.appendChild(roleDoc.createTextNode(((org.jasig.portal.security.IRole)vRoles.elementAt(i)).getRoleTitle()));
      roleE.appendChild(nameE);
      Element descriptionE = roleDoc.createElement("description");
      descriptionE.appendChild(roleDoc.createTextNode((String)((org.jasig.portal.security.IRole)vRoles.elementAt(i)).getAttribute("description")));
      roleE.appendChild(descriptionE);
      rolesE.appendChild(roleE);
    }
    roleDoc.appendChild(rolesE);
    return  roleDoc;
  }

  // This method is just for testing and will be removed...
  public static void main(String[] args) throws Exception {
    UtilitiesBean.setPortalBaseDir("D:\\Projects\\JA-SIG\\uPortal2\\");
    org.jasig.portal.IChannelRegistryStore chanReg = org.jasig.portal.RdbmServices.getChannelRegistryStoreImpl();

    // Getting the channel types...
    Document channelTypes = chanReg.getChannelTypesXML();
    //System.out.println(UtilitiesBean.dom2PrettyString(channelTypes));

    // Getting the channel registry...
    Document channelRegistry = chanReg.getChannelRegistryXML();
    //System.out.println(UtilitiesBean.dom2PrettyString(channelRegistry));

    // Getting the CPDDoc...
    Document cpdDoc = getCPDDoc("3");
    System.out.println(UtilitiesBean.dom2PrettyString(cpdDoc));
  }

  /**
   * Keeps track of page settings for MODIFY_CHANNEL_STATE
   */
  protected class UserSettings {
    private String recordsPerPage;
    private String currentPage;
    private String filterByID;

    protected UserSettings() {
      recordsPerPage = "5";
      currentPage = "1";
      filterByID = "-1";
    }

    // Accessor methods
    protected String getRecordsPerPage() { return recordsPerPage; }
    protected String getCurrentPage() { return currentPage; }
    protected String getFilterByID() { return filterByID; }

    protected void setRecordsPerPage(String recordsPerPage) { this.recordsPerPage = recordsPerPage; }
    protected void setCurrentPage(String currentPage) { this.currentPage = currentPage; }
    protected void setFilterByID(String filterByID) { this.filterByID = filterByID; }
  }


  /**
   * <p>This Workflow class represents the collection of workflow sections and can
   * produce an XML version of itself for passing to the XSLT stylesheets. When a
   * particular section is not explicitly set, a minimal XML fragment will still
   * be included so that the channel can render the workflow sections at the top.</p>
   */
  protected class Workflow {
    protected WorkflowSection channelTypesSection;
    protected WorkflowSection generalSettingsSection;
    protected WorkflowSection cpdSection;
    protected WorkflowSection controlsSection;
    protected WorkflowSection categoriesSection;
    protected WorkflowSection rolesSection;
    protected WorkflowSection reviewSection;

    protected void setChannelTypesSection(WorkflowSection channelTypesSection) { this.channelTypesSection = channelTypesSection; }
    protected void setGeneralSettingsSection(WorkflowSection generalSettingsSection) { this.generalSettingsSection = generalSettingsSection; }
    protected void setCPDSection(WorkflowSection cpdSection) { this.cpdSection = cpdSection; }
    protected void setControlsSection(WorkflowSection controlsSection) { this.controlsSection = controlsSection; }
    protected void setCategoriesSection(WorkflowSection categoriesSection) { this.categoriesSection = categoriesSection; }
    protected void setRolesSection(WorkflowSection rolesSection) { this.rolesSection = rolesSection; }
    protected void setReviewSection(WorkflowSection reviewSection) { this.reviewSection = reviewSection; }

    protected Document toXML() {
      Document doc = DocumentFactory.getNewDocument();

      // Add the top level <manageChannels> to the document
      Element channelManagerE = doc.createElement("manageChannels");
      doc.appendChild(channelManagerE);

      // Add all the sections
      addSection(channelTypesSection, "selectChannelType", "Channel Type", channelManagerE);
      addSection(generalSettingsSection, "selectGeneralSettings", "General Settings", channelManagerE);

      // This should only happen in the first state during the publishing of a new channel
      if (cpdSection != null)
        channelManagerE.appendChild(cpdSection.toXML(doc));

      addSection(controlsSection, "selectControls", "Channel Controls", channelManagerE);
      addSection(categoriesSection, "selectCategories", "Categories", channelManagerE);
      addSection(rolesSection, "selectRoles", "Roles", channelManagerE);
      addSection(reviewSection, "reviewChannel", "Review", channelManagerE);

      return doc;
    }

    private void addSection (WorkflowSection section, String sectionElementName, String stepTitle, Element e) {
      // For sections that haven't been set, add an empty one with one step
      if (section == null) {
        section = new WorkflowSection(sectionElementName);
        section.addStep(new WorkflowStep("1", stepTitle));
      }
      e.appendChild(section.toXML(e.getOwnerDocument()));
    }
  }

  protected class WorkflowSection {
    protected String name;
    protected List steps;

    protected WorkflowSection() {
    }

    protected WorkflowSection(String name) {
      this.name = name;
      steps = new ArrayList();
    }

    protected void setName(String name) { this.name = name; }
    protected void addStep(WorkflowStep step) {
      steps.add(step);
    }

    protected Element toXML(Document doc) {
      // Add this step's workflow element
      Element sectionE = doc.createElement(name);

      // Add the <params> element
      Element paramsE = doc.createElement("params");
      sectionE.appendChild(paramsE);

      // Add all the <step> elements
      Iterator iter = steps.iterator();
      while (iter.hasNext()) {
        WorkflowStep step = (WorkflowStep)iter.next();
        paramsE.appendChild(step.toXML(doc));
      }

      return sectionE;
    }
  }

  protected class CPDWorkflowSection extends WorkflowSection {
    protected Document cpdDoc;

    protected CPDWorkflowSection (String chanTypeID) throws PortalException {
      super();
      cpdDoc = getCPDDoc(chanTypeID);
    }

    protected void addToStep(Element element, String stepID) {
      for (Node n1 = cpdDoc.getDocumentElement().getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
        if (n1.getNodeType() == Node.ELEMENT_NODE && n1.getNodeName().equals("params")) {
          for (Node n2 = n1.getFirstChild(); n2 != null; n2 = n2.getNextSibling()) {
            if (n2.getNodeType() == Node.ELEMENT_NODE && n2.getNodeName().equals("step")) {
              for (Node n3 = n2.getFirstChild(); n3 != null; n3 = n3.getNextSibling()) {
                if (n3.getNodeType() == Node.ELEMENT_NODE && n3.getNodeName().equals("ID")) {
                  for (Node n4 = n3.getFirstChild(); n4 != null; n4 = n4.getNextSibling()) {
                    if (n4.getNodeType() == Node.TEXT_NODE) {
                      String ID = n4.getNodeValue();
                      if (ID.equals(stepID)) {
                        n2.appendChild(cpdDoc.importNode(element, true));
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    protected Element toXML(Document doc) {
      return (Element)doc.importNode(cpdDoc.getDocumentElement(), true);
    }
  }

  protected class WorkflowStep {
    protected String ID;
    protected String name;
    protected List dataElements;

    protected WorkflowStep(String ID, String name) {
      this.ID = ID;
      this.name = name;
      this.dataElements = new ArrayList();
    }

    // Accessor methods
    protected String getID() { return ID; }
    protected String getName() { return name; }

    protected void setID(String ID) { this.ID = ID; }
    protected void setName(String name) { this.name = name; }
    protected void addDataElement(Element dataElement) { this.dataElements.add(dataElement); }

    protected Element toXML(Document doc) {
      Element stepE = doc.createElement("step");
      Element IDE = doc.createElement("ID");
      IDE.appendChild(doc.createTextNode(ID));
      stepE.appendChild(IDE);
      Element nameE = doc.createElement("name");
      nameE.appendChild(doc.createTextNode(name));
      stepE.appendChild(nameE);

      Iterator iter = dataElements.iterator();
      while (iter.hasNext()) {
        stepE.appendChild(doc.importNode((Element)iter.next(), true));
      }

      return stepE;
    }
  }

  protected class ChannelDefinition {
    protected String ID;
    protected String typeID;
    protected String name;
    protected String timeout;
    protected String fname;
    protected String javaClass;
    protected String minimizable;
    protected String editable;
    protected String hasHelp;
    protected String hasAbout;
    protected String printable;
    protected String removable;
    protected String detachable;
    protected List parameters;

    protected class Parameter {
      protected String name;
      protected String value;
      protected String modType; // Need to make this part of parameter table

      protected Parameter(String name, String value, String modType) {
        this.name = name;
        this.value = value;
        this.modType = modType;
      }

      protected String getName() { return name; }
      protected String getValue() { return value; }
      protected String getModType() { return modType; }
      protected void setName(String name) { this.name = name; }
      protected void setValue(String value) { this.value = value; }
      protected void setModType(String modType) { this.modType = modType; }
    }

    protected ChannelDefinition() {
      parameters = new ArrayList();
    }

    protected String getTypeID() { return typeID; }
    protected void setTypeID(String typeID) { this.typeID = typeID; }
    protected void setName(String name) { this.name = name; }
    protected void setTimeout(String timeout) { this.timeout = timeout; }
    protected void setMinimizable(String minimizable) { this.minimizable = minimizable; }
    protected void setEditable(String editable) { this.editable = editable; }
    protected void setHasHelp(String hasHelp) { this.hasHelp = hasHelp; }
    protected void setHasAbout(String hasAbout) { this.hasAbout = hasAbout; }
    protected void setPrintable(String printable) { this.printable = printable; }
    protected void setRemovable(String removable) { this.removable = removable; }
    protected void setDetachable(String detachable) { this.detachable = detachable; }

    private void setAttribute(Element e, String attName, String attVal) {
      // Only set the attribute if it has a non-null value
      if (attVal != null)
        e.setAttribute(attName, attVal);
    }

    protected void addParameter(String name, String value, String modType) {
      parameters.add(new Parameter(name, value, modType));
    }

    protected Element toXML() {
      Element channelE = emptyDoc.createElement("channel");
      setAttribute(channelE, "ID", ID);
      setAttribute(channelE, "typeID", typeID); // Need to officially make this part of channel def
      setAttribute(channelE, "name", name);
      setAttribute(channelE, "fname", fname);
      setAttribute(channelE, "class", javaClass);
      setAttribute(channelE, "timeout", timeout);
      setAttribute(channelE, "minimizable", minimizable);
      setAttribute(channelE, "editable", editable);
      setAttribute(channelE, "printable", printable);
      setAttribute(channelE, "removable", removable);
      setAttribute(channelE, "detachable", detachable);
      setAttribute(channelE, "hasAbout", hasAbout);
      setAttribute(channelE, "hasHelp", hasHelp);

      Iterator iter = parameters.iterator();
      while (iter.hasNext()) {
        Parameter param = (Parameter)iter.next();
        Element parameterE = emptyDoc.createElement("parameter");
        parameterE.setAttribute("name", param.getName());
        parameterE.setAttribute("value", param.getValue());
        parameterE.setAttribute("modType", param.getModType());
        channelE.appendChild(parameterE);
      }
      return channelE;
    }
  }

  protected class CategorySettings {
    protected String browsingCategory;
    protected List selectedCategories;

    protected CategorySettings() {
      browsingCategory = "top";
      selectedCategories = new ArrayList();
    }

    protected void setBrowsingCategory(String browsingCategory) { this.browsingCategory = browsingCategory; }
    protected void addSelectedCategory(String selectedCategory) {
      selectedCategories.add(selectedCategory);
    }

    protected Element toXML() {
      Element userSettingsE = emptyDoc.createElement("userSettings");
      Element browsingCategoryE = emptyDoc.createElement("browsingCategory");
      browsingCategoryE.appendChild(emptyDoc.createTextNode(browsingCategory));
      userSettingsE.appendChild(browsingCategoryE);

      // Add selected categories if there are any
      if (selectedCategories.size() > 0) {
        Element selectedCategoriesE = emptyDoc.createElement("selectedCategories");
        Iterator iter = selectedCategories.iterator();
        while (iter.hasNext()) {
          Element selectedCategoryE = emptyDoc.createElement("selectedCategory");
          selectedCategoryE.appendChild(emptyDoc.createTextNode((String)iter.next()));
          selectedCategoriesE.appendChild(selectedCategoryE);
        }
        userSettingsE.appendChild(selectedCategoriesE);
      }

      return userSettingsE;
    }
  }

  protected class RoleSettings {
    protected List selectedRoles;

    protected RoleSettings() {
      selectedRoles = new ArrayList();
    }

    protected void addSelectedRole(String selectedRole) {
      selectedRoles.add(selectedRole);
    }

    protected Element toXML() {
      Element userSettingsE = emptyDoc.createElement("userSettings");

      // Add selected roles if there are any
      if (selectedRoles.size() > 0) {
        Element selectedRolesE = emptyDoc.createElement("selectedRoles");
        Iterator iter = selectedRoles.iterator();
        while (iter.hasNext()) {
          Element selectedRoleE = emptyDoc.createElement("selectedRole");
          selectedRoleE.appendChild(emptyDoc.createTextNode((String)iter.next()));
          selectedRolesE.appendChild(selectedRoleE);
        }
        userSettingsE.appendChild(selectedRolesE);
      }

      return userSettingsE;
    }
  }

  /*
   * Channel Types
   *   Channel types
   * General settings
   *   CPD
   *   Channel XML
   * CPD steps
   *   CPD
   *   Channel XML
   * Channel controls
   *   CPD
   *   Channel XML
   * Categories
   *   CPD
   *   Channel Registry
   *   Browsed category/selected category
   * Roles
   *   CPD
   *   Roles
   * Review
   *   Channel XML
   *   Selected categories
   *   Channel Registry
   *   Selected Roles
   *   Channel types
   *
   *
   */
}


