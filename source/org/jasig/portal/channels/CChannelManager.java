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
import java.sql.SQLException;


public class CChannelManager extends BaseChannel {
  protected static final String sslLocation = UtilitiesBean.fixURI("webpages/stylesheets/org/jasig/portal/channels/CChannelManager/CChannelManager.ssl");
  protected static final Document emptyDoc = DocumentFactory.getNewDocument();
  protected short state;
  protected static final short DEFAULT_STATE = 0;
  protected static final short CHANNEL_TYPE_STATE = 1;
  protected static final short GENERAL_SETTINGS_STATE = 2;
  protected static final short CHANNEL_DEF_STATE = 3;
  protected static final short CHANNEL_CONTROLS_STATE = 4;  
  protected static final short MODIFY_CHANNEL_STATE = 5;
  protected Document channelManagerDoc;
  protected UserSettings userSettings = new UserSettings();
  
  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
    runtimeData = rd;
    String action = runtimeData.getParameter("action");
    if (action != null) {
      if (action.equals("selectChannelType")) {
        state = CHANNEL_TYPE_STATE;
        channelManagerDoc = getChannelManagerDoc();
      } else if (action.equals("selectGeneralSettings")) {
        state = GENERAL_SETTINGS_STATE;
        channelManagerDoc = getChannelManagerDoc();              
      } else if (action.equals("channelDef")) {
        state = CHANNEL_DEF_STATE;
        channelManagerDoc = getChannelManagerDoc();              
      } else if (action.equals("selectControls")) {
        state = CHANNEL_CONTROLS_STATE;
        channelManagerDoc = getChannelManagerDoc();              
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
        break;        
      case CHANNEL_CONTROLS_STATE:
        action = "selectControls";
        break;        
      case MODIFY_CHANNEL_STATE:
        action = "selectModifyChannel";
        break;
      default:
        action = "none";
        break;
    }
    
    xslt.setStylesheetParameter("action", action);
    xslt.transform();
    
    // Remove this!!!
    try {
      if (false) System.out.println(UtilitiesBean.dom2PrettyString(channelManagerDoc));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  protected Document getChannelManagerDoc() throws PortalException {
    Document channelManagerDoc = DocumentFactory.getNewDocument();
    
    // Add the top level <manageChannels> to the document
    Element channelManagerE = channelManagerDoc.createElement("manageChannels");
    channelManagerDoc.appendChild(channelManagerE);
    
    // Add <selectChannelType> workflow step
    Document channelTypesDoc = ChannelRegistryManager.getChannelTypes();
    Element channelTypes = (Element)channelManagerDoc.importNode(channelTypesDoc.getDocumentElement(), true);
    appendWorkflowStep(channelManagerE, "selectChannelType", "Channel Types", channelTypes);
      
    // Add <selectGeneralSettings> workflow step
    appendWorkflowStep(channelManagerE, "selectGeneralSettings", "General Settings", null);
    
    // Add the channel publishing document (.cpd file)
    Element cpdE = (Element)channelManagerDoc.importNode(getCPDDoc("1").getDocumentElement(), true);
    channelManagerE.appendChild(cpdE);
 
    // Add <selectControls> workflow step
    appendWorkflowStep(channelManagerE, "selectControls", "Channel Controls", null);
     
    // Add <selectCategory> workflow step
    Document channelRegistryDoc = ChannelRegistryManager.getChannelRegistry();
    Element channelRegistry = (Element)channelManagerDoc.importNode(channelRegistryDoc.getDocumentElement(), true);
    appendWorkflowStep(channelManagerE, "selectCategory", "Categories", channelRegistry);

    // Add <selectRoles> workflow step
    appendWorkflowStep(channelManagerE, "selectRoles", "Roles", null);

    // Add <reviewChannel> workflow step
    appendWorkflowStep(channelManagerE, "reviewChannel", "Review", null);

    return channelManagerDoc;
  }
  
  protected void appendWorkflowStep(Element channelManager, String workflowElement, String stepName, Element insideElement) {
    Document doc = channelManager.getOwnerDocument();
    
    // Add this step's workflow element
    Element worflowElementE = doc.createElement(workflowElement);
    channelManager.appendChild(worflowElementE);
    
    // Add the <params>, <step>, <ID>, and <name> elements
    Element paramsE = doc.createElement("params");
    worflowElementE.appendChild(paramsE);
    Element stepE = doc.createElement("step");
    paramsE.appendChild(stepE);
    Element IDE = doc.createElement("ID");
    IDE.appendChild(doc.createTextNode("1"));
    stepE.appendChild(IDE);
    Element nameE = doc.createElement("name");
    nameE.appendChild(doc.createTextNode(stepName));
    stepE.appendChild(nameE);
    
    if (insideElement != null)
      stepE.appendChild(insideElement);
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
   *   CPD
   *   Channel XML
   *   Selected categories
   *   Channel Registry
   *   Selected Roles
   *   Channel types
   *   
   * 
   */
}


