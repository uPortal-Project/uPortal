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
import java.sql.*;
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

    RdbmServices rdbmService;
    Connection con;
    int systemUserId = 0; // Set this somehow

    public UserPreferencesDBImpl() {
        rdbmService = new RdbmServices();
        con=null;
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

    public Integer getUserBrowserMapping(int userId,String userAgent) {
	Integer profileId=null;
        try {
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
            String sQuery = "SELECT PROFILE_ID FROM UP_USER_UA_MAP WHERE USER_ID="+userId+" AND USER_AGENT='"+userAgent+"'";
            Logger.log(Logger.DEBUG,sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            if(rs.next()) {
                profileId=new Integer(rs.getInt("PROFILE_ID"));
            } else { return null; }
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
        return profileId;
    }

    public void setUserBrowserMapping(int userId,String userAgent, int profileId) {
        try {
            con=rdbmService.getConnection();
            // remove the old mapping and add the new one
            Statement stmt=con.createStatement();
            String sQuery = "DELETE FROM UP_USER_UA_MAP WHERE USER_ID='"+userId+"' AND USER_AGENT='"+userAgent+"'";
            Logger.log(Logger.DEBUG,sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            sQuery = "INSERT INTO UP_USER_UA_MAP (USER_ID,USER_AGENT,PROFILE_ID) VALUES ("+userId+",'"+userAgent+"',"+profileId+")";
            Logger.log(Logger.DEBUG, sQuery);
            rs=stmt.executeQuery(sQuery);
         } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
    }

    public void setSystemBrowserMapping(String userAgent,int profileId) {
        this.setUserBrowserMapping(systemUserId,userAgent,profileId);
    }

    public Integer getSystemBrowserMapping(String userAgent) {
        return getUserBrowserMapping(systemUserId,userAgent);
    }

    public UserProfile getUserProfile(int userId,String userAgent) {
	Integer profileId=getUserBrowserMapping(userId,userAgent);
	if(profileId==null) return null; 
	return this.getUserProfileById(userId,profileId.intValue());
    }

    public UserProfile getSystemProfile(String userAgent) {
	Integer profileId=getSystemBrowserMapping(userAgent);
	if(profileId==null) return null;
        UserProfile up= this.getUserProfileById(systemUserId,profileId.intValue());
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
	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();
	    String sQuery = "SELECT PROFILE_ID,PROFILE_NAME,STRUCTURE_SS_NAME, THEME_SS_NAME,DESCRIPTION FROM UP_USER_PROFILES WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId;
	    Logger.log(Logger.DEBUG,sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);
	    if(rs.next()) {
		upl=new UserProfile(profileId,rs.getString("PROFILE_NAME"),rs.getString("STRUCTURE_SS_NAME"),rs.getString("THEME_SS_NAME"),rs.getString("DESCRIPTION"));
	    } else { return null; }
	} catch (Exception e) {
	    Logger.log(Logger.ERROR,e);
	} finally {
	    rdbmService.releaseConnection (con);
	}
        return upl;
    }


    public Hashtable getUserProfileList(int userId) {
        Hashtable pv=new Hashtable();
        try {
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
            String sQuery = "SELECT PROFILE_ID,PROFILE_NAME,STRUCTURE_SS_NAME, THEME_SS_NAME, DESCRIPTION FROM UP_USER_PROFILES WHERE USER_ID="+userId;
            Logger.log(Logger.DEBUG,sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            while(rs.next()) {
                UserProfile upl=new UserProfile(rs.getInt("PROFILE_ID"),rs.getString("PROFILE_NAME"),rs.getString("STRUCTURE_SS_NAME"),rs.getString("THEME_SS_NAME"),rs.getString("DESCRIPTION"));
                pv.put(upl.getProfileName(),upl);
            }
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
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
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
	    String sQuery = "UPDATE UP_USER_PROFILES SET THEME_SS_NAME='"+profile.getThemeStylesheetName()+"', STRUCTURE_SS_NAME='"+profile.getStructureStylesheetName()+"', DESCRIPTION='"+profile.getProfileDescription()+"', PROFILE_NAME='"+profile.getProfileName()+"' WHERE USER_ID = "+userId+" AND PROFILE_ID="+profile.getProfileId();
	    Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::updateUserProfile() : "+sQuery);
	    stmt.executeUpdate(sQuery);
        }
        catch (Exception e) {
            Logger.log (Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
    }

    public void updateSystemProfile(UserProfile profile) {
        this.updateUserProfile(0,profile);
    }
    public UserProfile addUserProfile(int userId,UserProfile profile) {
	// generate an id for this profile
	DBCounterImpl dbc=new DBCounterImpl();
	Integer id=dbc.getIncrementIntegerId("UP_USER_PROFILES");
	if(id==null) return null;
	profile.setProfileId(id.intValue());
	try {
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
	    String sQuery = "INSERT INTO UP_USER_PROFILES (USER_ID,PROFILE_ID,PROFILE_NAME,STRUCTURE_SS_NAME,THEME_SS_NAME,DESCRIPTION) VALUES ("+userId+","+profile.getProfileId()+",'"+profile.getProfileName()+"','"+profile.getStructureStylesheetName()+"','"+profile.getThemeStylesheetName()+"','"+profile.getProfileDescription()+"')";
	    Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::addUserProfile() : "+sQuery);
	    stmt.executeQuery(sQuery);
        }
        catch (Exception e) {
            Logger.log (Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
	return profile;
    }

    public UserProfile addSystemProfile(UserProfile profile) {
	return addUserProfile(0,profile);
    }

    public void deleteUserProfile(int userId,int profileId) {
        try {
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
            String sQuery = "DELETE FROM UP_USER_PROFILES WHERE USER_ID="+userId+" AND PROFILE_ID="+Integer.toString(profileId);
            Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::deleteUserProfile() : "+sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
        }
        catch (Exception e) {
            Logger.log (Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
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
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
            String sQuery = "SELECT USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_ID="+userId+" AND STYLESHEET_NAME='"+stylesheetName+"' AND PROFILE_ID="+profileId;
            Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::getStylesheetNames() : "+sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            String str_upXML=null;
            if(rs.next()) str_upXML=rs.getString("USER_PREFERENCES_XML");
            if(str_upXML!=null) {
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::getStylesheetUserPreferences() : "+str_upXML);
                DOMParser parser = new DOMParser ();
                parser.parse (new org.xml.sax.InputSource (new StringReader (str_upXML)));
                Document upXML=parser.getDocument();
                this.populateUserParameterPreferences(upXML,fsup);
                this.populateUserParameterChannelAttributes(upXML,fsup);
                this.populateUserParameterFolderAttributes(upXML,fsup);
            } else {
                Logger.log(Logger.DEBUG,"UserPreferencesDBInpl::getStructureStylesheetUserPreferences() : Couldn't find stylesheet preferences for userId=\""+userId+"\", profileId=\""+profileId+"\" and stylesheetName=\""+stylesheetName+"\".");
            }
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
        return fsup;
    }

    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences(int userId,int profileId,String stylesheetName) {
        ThemeStylesheetUserPreferences ssup=new ThemeStylesheetUserPreferences();
        ssup.setStylesheetName(stylesheetName);
        try {
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
            String sQuery = "SELECT USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_ID="+userId+" AND STYLESHEET_NAME='"+stylesheetName+"' AND PROFILE_ID="+profileId;
            Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::getThemeStylesheetUserPreferences() : "+sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            String str_upXML=null;
            if(rs.next()) str_upXML=rs.getString("USER_PREFERENCES_XML");
            if(str_upXML!=null) {
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::getThemeStylesheetUserPreferences() : "+str_upXML);
                DOMParser parser = new DOMParser ();
                parser.parse (new org.xml.sax.InputSource (new StringReader (str_upXML)));
                Document upXML=parser.getDocument();
                this.populateUserParameterPreferences(upXML,ssup);
                this.populateUserParameterChannelAttributes(upXML,ssup);
            } else {
                Logger.log(Logger.DEBUG,"UserPreferencesDBInpl::getThemeStylesheetUserPreferences() : Couldn't find stylesheet preferences for userId=\""+userId+"\", profileId=\""+profileId+"\" and stylesheetName=\""+stylesheetName+"\".");
            }
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
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
        StringWriter outString = new StringWriter ();
        try {
            OutputFormat format=new OutputFormat(doc);
            format.setOmitXMLDeclaration(true);
            XMLSerializer xsl = new XMLSerializer (outString,format);
            xsl.serialize (doc);
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
            // this is ugly, but we have to know wether to do INSERT or UPDATE
            String sQuery = "SELECT USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_ID="+userId+" AND STYLESHEET_NAME='"+stylesheetName+"' AND PROFILE_ID="+profileId;
            Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setStructureStylesheetUserPreferences() : "+sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            if(rs.next()) {
                sQuery = "UPDATE UP_USER_SS_PREFS SET USER_PREFERENCES_XML='"+outString.toString()+"' WHERE USER_ID = "+userId+" AND STYLESHEET_NAME='"+stylesheetName+"' AND PROFILE_ID="+profileId;
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setStructureStylesheetUserPreferences() : "+sQuery);
                stmt.executeUpdate(sQuery);
            }
            else {
                sQuery = "INSERT INTO UP_USER_SS_PREFS (USER_ID,PROFILE_ID,NAME,STYLESHEET_NAME,USER_PREFERENCES_XML) VALUES ("+userId+","+profileId+",'"+stylesheetName+"','"+outString.toString()+"')";
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setStructureStylesheetUserPreferences() : "+sQuery);
                stmt.executeQuery(sQuery);
            }
        }
        catch (Exception e) {
            Logger.log (Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
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

        // update the database
        StringWriter outString = new StringWriter ();
        try {
            OutputFormat format=new OutputFormat(doc);
            format.setOmitXMLDeclaration(true);
            XMLSerializer xsl = new XMLSerializer (outString,format);
            xsl.serialize (doc);
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
            // this is ugly, but we have to know wether to do INSERT or UPDATE
            String sQuery = "SELECT USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_ID="+userId+" AND STYLESHEET_NAME='"+stylesheetName+"' AND PROFILE_ID="+profileId;
            Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setThemeStylesheetUserPreferences() : "+sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            if(rs.next()) {
                sQuery = "UPDATE UP_USER_SS_PREFS SET USER_PREFERENCES_XML='"+outString.toString()+"' WHERE USER_ID = "+userId+" AND STYLESHEET_NAME='"+stylesheetName+"' AND PROFILE_ID="+profileId;
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setThemeStylesheetUserPreferences() : "+sQuery);
                stmt.executeUpdate(sQuery);
            }
            else {
                sQuery = "INSERT INTO UP_USER_SS_PREFS (USER_ID,PROFILE_ID,STYLESHEET_NAME,USER_PREFERENCES_XML) VALUES ("+userId+","+profileId+",'"+stylesheetName+"','"+outString.toString()+"')";
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setThemeStylesheetUserPreferences() : "+sQuery);
                stmt.executeQuery(sQuery);
            }
        }
        catch (Exception e) {
            Logger.log (Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
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
