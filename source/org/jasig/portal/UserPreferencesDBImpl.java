/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.apache.xerces.parsers.*;

import org.apache.xml.serialize.*;

/**
 * Reference implementation of IUserPreferencesDB
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */
public class UserPreferencesDBImpl implements IUserPreferencesDB {

    int systemUserId = 0; // Set this somehow

    public UserPreferencesDBImpl() {
    }


    public UserPreferences getUserPreferences(int userId, UserProfile profile) {
        int profileId=profile.getProfileId();
        UserPreferences up=new UserPreferences(profile);
        up.setStructureStylesheetUserPreferences(getStructureStylesheetUserPreferences(userId,profileId,profile.getStructureStylesheetName()));
        up.setThemeStylesheetUserPreferences(getThemeStylesheetUserPreferences(userId,profileId,profile.getThemeStylesheetName()));
        return up;
    }

    public UserPreferences getUserPreferences(int userId, int profileId) {
        UserPreferences up=null;
        UserProfile profile=this.getUserProfileById(userId,profileId);
        if(profile!=null) {
            up=getUserPreferences(userId,profile);
        }
        return up;
    }

    public int getUserBrowserMapping(int userId,String userAgent) {
        try {
          IDBImpl dbImpl = new DBImpl();
          return dbImpl.getUserBrowserMapping(userId, userAgent);
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
        return 0;
    }

    public void setUserBrowserMapping(int userId,String userAgent, int profileId) {
        try {
          IDBImpl dbImpl = new DBImpl();
          dbImpl.setUserBrowserMapping(userId, userAgent, profileId);
         } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
    }

    public void setSystemBrowserMapping(String userAgent,int profileId) {
        this.setUserBrowserMapping(systemUserId,userAgent,profileId);
    }

    public int getSystemBrowserMapping(String userAgent) {
        return getUserBrowserMapping(systemUserId,userAgent);
    }

    public UserProfile getUserProfile(int userId,String userAgent) {
        int profileId=getUserBrowserMapping(userId,userAgent);
        if(profileId==0) return null;
        return this.getUserProfileById(userId,profileId);
    }

    public UserProfile getSystemProfile(String userAgent) {
        int profileId=getSystemBrowserMapping(userAgent);
        if(profileId==0) return null;
        UserProfile up= this.getUserProfileById(systemUserId,profileId);
        up.setSystemProfile(true);
        return up;
    }


    public UserProfile getSystemProfileById(int profileId) {
        UserProfile up=this.getUserProfileById(systemUserId,profileId);
        up.setSystemProfile(true);
        return up;
    }

    public UserProfile getUserProfileById(int userId, int profileId) {
        UserProfile upl=null;
        try {
          IDBImpl dbImpl = new DBImpl();
          upl = dbImpl.getUserProfileById(userId, profileId);
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
        return upl;
    }


    public Hashtable getUserProfileList(int userId) {
        Hashtable pv=null;
        try {
          IDBImpl dbImpl = new DBImpl();
          pv = dbImpl.getUserProfileList(userId);
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
        return pv;
    }

    public Hashtable getSystemProfileList() {
        Hashtable pl=this.getUserProfileList(0);
        for(Enumeration e=pl.elements(); e.hasMoreElements(); ) {
            UserProfile up=(UserProfile) e.nextElement();
            up.setSystemProfile(true);
        }
        return pl;
    }

    public void updateUserProfile(int userId,UserProfile profile) {
        try {
          IDBImpl dbImpl = new DBImpl();
          dbImpl.updateUserProfile(userId, profile);
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
     }

    public void updateSystemProfile(UserProfile profile) {
        this.updateUserProfile(0,profile);
    }
    public UserProfile addUserProfile(int userId,UserProfile profile) {
      IDBImpl dbImpl = new DBImpl();
        try {
          profile = dbImpl.addUserProfile(userId, profile);
        } catch (Exception e) {
            Logger.log (Logger.ERROR,e);
        }
        return profile;
    }

    public UserProfile addSystemProfile(UserProfile profile) {
        return addUserProfile(0,profile);
    }

    public void deleteUserProfile(int userId,int profileId) {
      try {
        IDBImpl dbImpl = new DBImpl();
        dbImpl.deleteUserProfile(userId, profileId);
        }
        catch (Exception e) {
            Logger.log (Logger.ERROR,e);
        }
    }

    public void deleteSystemProfile(int profileId) {
        this.deleteUserProfile(0,profileId);
    }

    public void putUserPreferences(int userId, UserPreferences up) {
        // store profile
        UserProfile profile=up.getProfile();
        this.updateUserProfile(userId,profile);

        this.setStructureStylesheetUserPreferences(userId,profile.getProfileId(),up.getStructureStylesheetUserPreferences());
        this.setThemeStylesheetUserPreferences(userId,profile.getProfileId(),up.getThemeStylesheetUserPreferences());
    }

    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences(int userId,int profileId,String stylesheetName) {
        StructureStylesheetUserPreferences fsup=new StructureStylesheetUserPreferences();
        fsup.setStylesheetName(stylesheetName);
        try {
            IDBImpl dbImpl = new DBImpl();

            Document upXML=dbImpl.getStructureStylesheetUserPreferences(userId, profileId, stylesheetName);
            if(upXML!=null) {
                this.populateUserParameterPreferences(upXML,fsup);
                this.populateUserParameterChannelAttributes(upXML,fsup);
                this.populateUserParameterFolderAttributes(upXML,fsup);
            } else {
                Logger.log(Logger.DEBUG,"UserPreferencesDBInpl::getStructureStylesheetUserPreferences() : Couldn't find stylesheet preferences for userId=\""+userId+"\", profileId=\""+profileId+"\" and stylesheetName=\""+stylesheetName+"\".");
            }
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
        return fsup;
    }

    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences(int userId,int profileId,String stylesheetName) {
        ThemeStylesheetUserPreferences ssup=new ThemeStylesheetUserPreferences();
        ssup.setStylesheetName(stylesheetName);
        try {
            IDBImpl dbImpl = new DBImpl();
            Document upXML = dbImpl.getThemeStylesheetUserPreferences(userId, profileId, stylesheetName);
            if(upXML!=null) {
                this.populateUserParameterPreferences(upXML,ssup);
                this.populateUserParameterChannelAttributes(upXML,ssup);
            } else {
                Logger.log(Logger.DEBUG,"UserPreferencesDBInpl::getThemeStylesheetUserPreferences() : Couldn't find stylesheet preferences for userId=\""+userId+"\", profileId=\""+profileId+"\" and stylesheetName=\""+stylesheetName+"\".");
            }
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
        return ssup;
    }

    public void setStructureStylesheetUserPreferences(int userId,int profileId,StructureStylesheetUserPreferences fsup) {
        String stylesheetName=fsup.getStylesheetName();
        // construct a DOM tree
        Document doc = new org.apache.xerces.dom.DocumentImpl();
        Element spEl = doc.createElement("stylesheetuserpreferences");
        spEl.appendChild(constructParametersElement(fsup,doc));
        spEl.appendChild(constructChannelAttributesElement(fsup,doc));
        spEl.appendChild(constructFolderAttributesElement(fsup,doc));
        doc.appendChild(spEl);

        // update the database
        try {
            IDBImpl dbImpl = new DBImpl();
            dbImpl.setStructureStylesheetUserPreferences(userId, profileId, stylesheetName, doc);
        }
        catch (Exception e) {
            Logger.log (Logger.ERROR,e);
        }
    }

    public void setThemeStylesheetUserPreferences(int userId, int profileId,ThemeStylesheetUserPreferences ssup) {
        String stylesheetName=ssup.getStylesheetName();
        // construct a DOM tree
        Document doc = new org.apache.xerces.dom.DocumentImpl();
        Element spEl = doc.createElement("stylesheetuserpreferences");
        spEl.appendChild(constructParametersElement(ssup,doc));
        spEl.appendChild(constructChannelAttributesElement(ssup,doc));
        doc.appendChild(spEl);

        IDBImpl dbImpl = new DBImpl();

        // update the database
        try {
          dbImpl.setThemeStylesheetUserPreferences(userId, profileId, stylesheetName, doc);
        }
        catch (Exception e) {
            Logger.log (Logger.ERROR,e);
        }
    }


    private void populateUserParameterPreferences(Document upXML,StylesheetUserPreferences up) {
        NodeList parametersNodes=upXML.getElementsByTagName("parameters");
        // just get the first matching node. Since this XML is portal generated, we trust it.
        Element parametersNode=(Element) parametersNodes.item(0);
        if(parametersNode!=null) {
            NodeList elements=parametersNode.getElementsByTagName("parameter");
            for(int i=elements.getLength()-1;i>=0;i--) {
                Element parameterElement=(Element) elements.item(i);
                up.putParameterValue(parameterElement.getAttribute("name"),this.getTextChildNodeValue(parameterElement));
                //		Logger.log(Logger.DEBUG,"UserPreferencesDBInpl::populateUserParameterPreferences() : adding paramtere name=\""+parameterElement.getAttribute("name")+"\" value=\""+this.getTextChildNodeValue(parameterElement)+"\".");
            }
        }
    }


    private void populateUserParameterChannelAttributes(Document upXML,ThemeStylesheetUserPreferences up) {
        NodeList attributesNodes=upXML.getElementsByTagName("channelattributes");
        // just get the first matching node. Since this XML is portal generated, we trust it.
        Element attributesNode=(Element) attributesNodes.item(0);
        if(attributesNode!=null) {
            NodeList attributeElements=attributesNode.getElementsByTagName("attribute");
            for(int i=attributeElements.getLength()-1;i>=0;i--) {
                // process a particular attribute
                Element attributeElement=(Element) attributeElements.item(i);
                String attributeName=attributeElement.getAttribute("name");
                String attributeDefaultValue=attributeElement.getAttribute("defaultvalue");
                up.addChannelAttribute(attributeName,attributeDefaultValue);
                NodeList channelElements=attributeElement.getElementsByTagName("channel");
                for(int j=channelElements.getLength()-1;j>=0;j--) {
                    Element channelElement=(Element) channelElements.item(j);
                    up.setChannelAttributeValue(channelElement.getAttribute("channelid"),attributeName,channelElement.getAttribute("value"));
                    //		    Logger.log(Logger.DEBUG,"UserPreferencesDBInpl::populateUserParameterChannelAttributes() : adding channel attribute attributeName=\""+attributeName+"\" channelID=\""+channelElement.getAttribute("channelid")+"\" attributeValue=\""+channelElement.getAttribute("value")+"\".");
                }
            }
        }
    }

    private void populateUserParameterFolderAttributes(Document upXML,StructureStylesheetUserPreferences up) {
        NodeList attributesNodes=upXML.getElementsByTagName("folderattributes");
        // just get the first matching node. Since this XML is portal generated, we trust it.
        Element attributesNode=(Element) attributesNodes.item(0);
        if(attributesNode!=null) {
            NodeList attributeElements=attributesNode.getElementsByTagName("attribute");
            for(int i=attributeElements.getLength()-1;i>=0;i--) {
                // process a particular attribute
                Element attributeElement=(Element) attributeElements.item(i);
                String attributeName=attributeElement.getAttribute("name");
                String attributeDefaultValue=attributeElement.getAttribute("defaultvalue");
                up.addFolderAttribute(attributeName,attributeDefaultValue);
                NodeList folderElements=attributeElement.getElementsByTagName("folder");
                for(int j=folderElements.getLength()-1;j>=0;j--) {
                    Element folderElement=(Element) folderElements.item(j);
                    up.setFolderAttributeValue(folderElement.getAttribute("folderid"),attributeName,folderElement.getAttribute("value"));
                    //		    Logger.log(Logger.DEBUG,"UserPreferencesDBInpl::populateUserParameterFolderAttributes() : adding folder attribute attributeName=\""+attributeName+"\" folderID=\""+folderElement.getAttribute("folderid")+"\" attributeValue=\""+folderElement.getAttribute("value")+"\".");
                }
            }
        }
    }

    private Element constructParametersElement(StylesheetUserPreferences up,Document doc) {
        Element parametersEl = doc.createElement("parameters");
        Hashtable pv=up.getParameterValues();
        for (Enumeration e = pv.keys() ; e.hasMoreElements() ;) {
            String parameterName=(String) e.nextElement();
            Element parameterEl=doc.createElement("parameter");
            parameterEl.setAttribute("name",parameterName);
            parameterEl.appendChild(doc.createTextNode((String) pv.get(parameterName)));
            parametersEl.appendChild(parameterEl);
        }
        return parametersEl;
    }

    private Element constructChannelAttributesElement(ThemeStylesheetUserPreferences up,Document doc) {
        Element attributesEl = doc.createElement("channelattributes");
        for(Enumeration ae=up.getChannelAttributeNames();ae.hasMoreElements();) {
            String attributeName=(String) ae.nextElement();
            Element attributeEl=doc.createElement("attribute");
            attributeEl.setAttribute("name",attributeName);
            for(Enumeration e=up.getChannels();e.hasMoreElements();) {
                String channelID=(String) e.nextElement();
                Element channelEl=doc.createElement("channel");
                channelEl.setAttribute("channelid",channelID);
                channelEl.setAttribute("value",up.getChannelAttributeValue(channelID,attributeName));
                attributeEl.appendChild(channelEl);
            }
            attributesEl.appendChild(attributeEl);
        }
        return attributesEl;
    }

    private Element constructFolderAttributesElement(StructureStylesheetUserPreferences up,Document doc) {
        Element attributesEl = doc.createElement("folderattributes");
        for(Enumeration ae=up.getFolderAttributeNames();ae.hasMoreElements();) {
            String attributeName=(String) ae.nextElement();
            Element attributeEl=doc.createElement("attribute");
            attributeEl.setAttribute("name",attributeName);
            for(Enumeration e=up.getFolders();e.hasMoreElements();) {
                String folderID=(String) e.nextElement();
                Element folderEl=doc.createElement("folder");
                folderEl.setAttribute("folderid",folderID);
                folderEl.setAttribute("value",up.getFolderAttributeValue(folderID,attributeName));
                attributeEl.appendChild(folderEl);
            }
            attributesEl.appendChild(attributeEl);
        }
        return attributesEl;
    }

    private String getTextChildNodeValue(Node node) {
        if(node==null) return null;
        NodeList children=node.getChildNodes();
        for(int i=children.getLength()-1;i>=0;i--) {
            Node child=children.item(i);
            if(child.getNodeType()==Node.TEXT_NODE) return child.getNodeValue();
        }
        return null;
    }
}
