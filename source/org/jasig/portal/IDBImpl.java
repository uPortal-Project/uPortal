package org.jasig.portal;

/**
 * Interface by which portal talks to the database
 * @author George Lindholm
 * @version $Revision$
 */

import org.w3c.dom.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
public interface IDBImpl {
  /* UserLayout  */
  public Document getUserLayout(int userId,int profileId) throws Exception;
  public void setUserLayout(int userId,int profileId,Document layoutXML) throws Exception;

  /* UserPreferences */
  public int getUserBrowserMapping(int userId,String userAgent) throws Exception;
  public void setUserBrowserMapping(int userId,String userAgent, int profileId) throws Exception;
  public UserProfile getUserProfileById(int userId, int profileId) throws Exception;
  public Hashtable getUserProfileList(int userId) throws Exception;
  public void setUserProfile(int userId,UserProfile profile) throws Exception;
  public Document getStructureStylesheetUserPreferences(int userId,int profileId,String stylesheetName) throws Exception;
  public Document getThemeStylesheetUserPreferences(int userId,int profileId,String stylesheetName) throws Exception;
  public void setStructureStylesheetUserPreferences(int userId,int profileId, String stylesheetName, Document upXML) throws Exception;
  public void setThemeStylesheetUserPreferences(int userId, int profileId, String stylesheetName, Document upXML) throws Exception;
  public void updateUserProfile(int userId,UserProfile profile) throws Exception;
  public UserProfile addUserProfile(int userId,UserProfile profile) throws Exception;
  public void deleteUserProfile(int userId,int profileId) throws Exception;

  /* ChannelRegistry */
  public String getNextChannelId() throws Exception;
  public void addChannel(String id, String title, Document doc) throws Exception;
  public void addChannel(String id, String title, Document doc, String catID[]) throws Exception;
  public Element getRegistryXML(org.apache.xerces.dom.DocumentImpl chanDoc, Element root, String catID, String role) throws Exception;
  public void getTypesXML(Document types, Element root, String role) throws Exception;
  public void getCategoryXML(Document catsDoc, Element root, String role) throws Exception;

  /* CoreStylesheetDescription */
  public void getMimeTypeList(Hashtable list) throws Exception;
  public void getStructureStylesheetList(String mimeType, Hashtable list) throws Exception;
  public void getThemeStylesheetList(String structureStylesheetName, Hashtable list) throws Exception;
  public String[] getStructureStylesheetDescription(String stylesheetName) throws Exception;
  public String[] getThemeStylesheetDescription(String stylesheetName) throws Exception;
  public void removeStructureStylesheetDescription(String stylesheetName) throws Exception;
  public void removeThemeStylesheetDescription(String stylesheetName) throws Exception;
  public void addStructureStylesheetDescription(String xmlStylesheetName, String stylesheetURI, String stylesheetDescriptionURI, String xmlStylesheetDescriptionText) throws Exception;
  public void addThemeStylesheetDescription(String xmlStylesheetName, String stylesheetURI, String stylesheetDescriptionURI, String xmlStylesheetDescriptionText, String mimeType, Enumeration enum) throws Exception;

  /* CBookmarks */
  public Document getBookmarkXML(int userId) throws Exception;
  public void saveBookmarkXML(int userId, Document doc) throws Exception;

  /* ReferenceAuthorization */
  public boolean isUserInRole(int userId, String role) throws Exception;
  public Vector getAllRoles() throws Exception;
  public void getChannelRoles(Vector roles, int channelID) throws Exception;
  public int setChannelRoles(int channelID, Vector roles) throws Exception;
  public void getUserRoles(Vector userRoles, int userId) throws Exception;
  public void addUserRoles(int userId, Vector roles) throws Exception;
  public void removeUserRoles(int userId, Vector roles) throws Exception;

  /* Authorization */
  public String[] getUserAccountInformation(String username) throws Exception;
}

