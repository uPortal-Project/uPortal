package org.jasig.portal.channels;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.UtilitiesBean;
import org.jasig.portal.PortalException;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.factories.DocumentFactory;
import org.xml.sax.DocumentHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.sql.SQLException;


public class CChannelManager extends BaseChannel {
  protected static final String sslLocation = UtilitiesBean.fixURI("webpages/stylesheets/org/jasig/portal/channels/CChannelManager/CChannelManager.ssl");
  protected static final Document emptyDoc = DocumentFactory.getNewDocument();
  protected short state;
  protected static final short DEFAULT_STATE = 0;
  protected static final short MODIFY_CHANNEL_STATE = 1;
  protected Document channelManagerDoc;
  protected UserSettings userSettings = new UserSettings();
  
  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
    runtimeData = rd;
    String action = runtimeData.getParameter("action");
    if (action != null) {
      if (action.equals("selectModifyChannel")) {
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
    
    switch (state) {
      case DEFAULT_STATE:
        xslt.setStylesheetParameter("action", "none");
        break;
      case MODIFY_CHANNEL_STATE:
        xslt.setStylesheetParameter("action", "selectModifyChannel");
        break;
      default:
        xslt.setStylesheetParameter("action", "none");
        break;
    }
    
    xslt.transform();
    
    // Remove this!!!
    try {
      if (false) System.out.println(UtilitiesBean.dom2PrettyString(channelManagerDoc));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  protected Document getChannelManagerDoc(UserSettings userSettings) throws PortalException {
    Document channelManagerDoc = DocumentFactory.getNewDocument();

    // Get the channel registry
    Document channelRegistryDoc = ChannelRegistryManager.getChannelRegistry();

    // Set the registry ID attribute to "-1"
    Element registry = channelRegistryDoc.getDocumentElement();
    registry.setAttribute("ID", "-1");

    // Add the top level <channelManager> to the document
    Element channelManager = channelManagerDoc.createElement("channelManager");
    channelManagerDoc.appendChild(channelManager);

    // Add the <registry> to the <channelManager>
    Element channelRegistry = (Element)channelManagerDoc.importNode(channelRegistryDoc.getDocumentElement(), true);
    channelManager.appendChild(channelRegistry);

    // Add a <userSettings> fragment to the <channelManager>
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
}


