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
  public void addChannel(int id, String title, Document doc) throws Exception;
  public void addChannel(int id, String title, Document doc, String catID[]) throws Exception;
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

