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

    public UserPreferencesDBImpl() {
        rdbmService = new RdbmServices();
        con=null;
    }

    public UserPreferences getUserPreferences(String userName, String media) {
        UserPreferences up=null;
        String[] stylesheetNames=this.getStylesheetNames(userName,media);
        if(stylesheetNames!=null) {
            up=new UserPreferences(media);
            up.setStructureStylesheetUserPreferences(getStructureStylesheetUserPreferences(userName,stylesheetNames[0]));
            up.setThemeStylesheetUserPreferences(getThemeStylesheetUserPreferences(userName,stylesheetNames[1]));
            up.setCoreCSSStylesheetUserPreferences(getCSSStylesheetUserPreferences(userName,stylesheetNames[2]));
        }
        return up;
    }

    public void putUserPreferences(String userName, UserPreferences up) {
        String[] stylesheetNames=new String[3];
        StructureStylesheetUserPreferences fsup=up.getStructureStylesheetUserPreferences();
        ThemeStylesheetUserPreferences ssup=up.getThemeStylesheetUserPreferences();
        CoreCSSStylesheetUserPreferences cssup=up.getCoreCSSStylesheetUserPreferences();
        stylesheetNames[0]=fsup.getStylesheetName();
        stylesheetNames[1]=ssup.getStylesheetName();
        stylesheetNames[2]=cssup.getStylesheetName();
        String media=up.getMedia();
        this.setStylesheetNames(stylesheetNames,userName,media);
        this.setStructureStylesheetUserPreferences(userName,fsup);
        this.setThemeStylesheetUserPreferences(userName,ssup);
        this.setCSSStylesheetUserPreferences(userName,cssup);
    }

    public String getStructureStylesheetName(String userName, String media)  {
        return (this.getStylesheetNames(userName,media))[0];
    }

    public String getThemeStylesheetName(String userName, String media)  {
        return (this.getStylesheetNames(userName,media))[1];
    }
    public String getCSSStylesheetName(String userName, String media)  {
        return (this.getStylesheetNames(userName,media))[2];
    }

    private String[] getStylesheetNames(String userName,String media) {
        String[] stylesheetNames=new String[3];
        try {
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
            String sQuery = "SELECT STRUCTURE_STYLESHEET_NAME, THEME_STYLESHEET_NAME, CSS_STYLESHEET_NAME  FROM UP_USER_PREFS WHERE USER_NAME='"+userName+"' AND MEDIA='"+media+"'";
            Logger.log(Logger.DEBUG,sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            if(rs.next()) {
                stylesheetNames[0]=rs.getString("STRUCTURE_STYLESHEET_NAME");
                stylesheetNames[1]=rs.getString("THEME_STYLESHEET_NAME");
                stylesheetNames[2]=rs.getString("CSS_STYLESHEET_NAME");
        } else { return null; }
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
        return stylesheetNames;
    }

    public void setStructureStylesheetName(String stylesheetName,String userName, String media) {
        String[] stylesheetNames= new String[3];
        stylesheetNames[0]=stylesheetName;
        this.setStylesheetNames(stylesheetNames,userName,media);
    }

    public void setThemeStylesheetName(String stylesheetName,String userName, String media) {
        String[] stylesheetNames= new String[3];
        stylesheetNames[1]=stylesheetName;
        this.setStylesheetNames(stylesheetNames,userName,media);
    }

    public void setCSSStylesheetName(String stylesheetName,String userName, String media) {
        String[] stylesheetNames= new String[3];
        stylesheetNames[2]=stylesheetName;
        this.setStylesheetNames(stylesheetNames,userName,media);
    }

    private void setStylesheetNames(String stylesheetNames[],String userName, String media) {
        try {
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
            // determien wether to do an insert or an update
            // note that stylesheet names passed as "null" will be substituted for with the current database entries when an UPDATE is done
            String sQuery = "SELECT STRUCTURE_STYLESHEET_NAME, THEME_STYLESHEET_NAME, CSS_STYLESHEET_NAME  FROM UP_USER_PREFS WHERE USER_NAME='"+userName+"' AND MEDIA='"+media+"'";
            Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setStylesheetNames() : "+sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            if(rs.next()) {
                if(stylesheetNames[0]==null) stylesheetNames[0]=rs.getString("STRUCTURE_STYLESHEET_NAME");
                if(stylesheetNames[1]==null) stylesheetNames[1]=rs.getString("THEME_STYLESHEET_NAME");
                if(stylesheetNames[2]==null) stylesheetNames[2]=rs.getString("CSS_STYLESHEET_NAME");

                sQuery = "UPDATE UP_USER_PREFS SET STRUCTURE_STYLESHEET_NAME='" + stylesheetNames[0] + "', THEME_STYLESHEET_NAME='" + stylesheetNames[1] + "', CSS_STYLESHEET_NAME='" + stylesheetNames[2] + "' WHERE USER_NAME='" + userName + "' AND MEDIA='" + media + "'";
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setStylesheetNames() : "+sQuery);
                stmt.executeUpdate(sQuery);
            }
            else if(stylesheetNames[0]!=null && stylesheetNames[1]!=null && stylesheetNames[2]!=null) {
                sQuery = "INSERT INTO UP_USER_PREFS (USER_NAME, MEDIA, STRUCTURE_STYLESHEET_NAME, THEME_STYLESHEET_NAME, CSS_STYLESHEET_NAME) VALUES ('"+userName+"','"+media+"','"+stylesheetNames[0]+"','"+stylesheetNames[1]+"','"+stylesheetNames[2]+"')";
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setStylesheetNames() : "+sQuery);
                stmt.executeQuery(sQuery);
            } else {
                Logger.log(Logger.ERROR,"UserPreferencesDBInpl::setStylesheetNames() : Trying to initialize UserPreferences with a null stylesheet name !");
            }
        }
        catch (Exception e) {
            Logger.log (Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }

    }

    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences(String userName,String stylesheetName) {
        StructureStylesheetUserPreferences fsup=new StructureStylesheetUserPreferences();
        fsup.setStylesheetName(stylesheetName);
        try {
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
            String sQuery = "SELECT USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_NAME='"+userName+"' AND STYLESHEET_NAME='"+stylesheetName+"'";
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
                Logger.log(Logger.DEBUG,"UserPreferencesDBInpl::getStructureStylesheetUserPreferences() : Couldn't find stylesheet preferences for userName=\""+userName+"\" and stylesheetName=\""+stylesheetName+"\".");
            }
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
        return fsup;
    }

    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences(String userName,String stylesheetName) {
        ThemeStylesheetUserPreferences ssup=new ThemeStylesheetUserPreferences();
        ssup.setStylesheetName(stylesheetName);
        try {
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
            String sQuery = "SELECT USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_NAME='"+userName+"' AND STYLESHEET_NAME='"+stylesheetName+"'";
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
                Logger.log(Logger.DEBUG,"UserPreferencesDBInpl::getThemeStylesheetUserPreferences() : Couldn't find stylesheet preferences for userName=\""+userName+"\" and stylesheetName=\""+stylesheetName+"\".");
            }
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
        return ssup;
    }

    public CoreCSSStylesheetUserPreferences getCSSStylesheetUserPreferences(String userName,String stylesheetName) {
        CoreCSSStylesheetUserPreferences cssup=new CoreCSSStylesheetUserPreferences();
        cssup.setStylesheetName(stylesheetName);
        try {
            con=rdbmService.getConnection();
            Statement stmt=con.createStatement();
            String sQuery = "SELECT USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_NAME='"+userName+"' AND STYLESHEET_NAME='"+stylesheetName+"'";
            Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::getCSSStylesheetUserPreferences() : "+sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            String str_upXML=null;
            if(rs.next()) str_upXML=rs.getString("USER_PREFERENCES_XML");
            if(str_upXML!=null) {
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::getCSSStylesheetUserPreferences() : "+str_upXML);
                DOMParser parser = new DOMParser ();
                parser.parse (new org.xml.sax.InputSource (new StringReader (str_upXML)));
                Document upXML=parser.getDocument();
                this.populateUserParameterPreferences(upXML,cssup);
            } else {
                Logger.log(Logger.DEBUG,"UserPreferencesDBInpl::getCSSStylesheetUserPrefernces() : Couldn't find stylesheet preferences for userName=\""+userName+"\" and stylesheetName=\""+stylesheetName+"\".");
            }
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
        return cssup;
    }

    public void setStructureStylesheetUserPreferences(String userName,StructureStylesheetUserPreferences fsup) {
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
            String sQuery = "SELECT USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_NAME='"+userName+"' AND STYLESHEET_NAME='"+stylesheetName+"'";
            Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setStructureStylesheetUserPreferences() : "+sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            if(rs.next()) {
                sQuery = "UPDATE UP_USER_SS_PREFS SET USER_PREFERENCES_XML='"+outString.toString()+"' WHERE USER_NAME = '"+userName+"' AND STYLESHEET_NAME='"+stylesheetName+"'";
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setStructureStylesheetUserPreferences() : "+sQuery);
                stmt.executeUpdate(sQuery);
            }
            else {
                sQuery = "INSERT INTO UP_USER_SS_PREFS (USER_NAME,STYLESHEET_NAME,USER_PREFERENCES_XML) VALUES ('"+userName+"','"+stylesheetName+"','"+outString.toString()+"')";
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

    public void setThemeStylesheetUserPreferences(String userName, ThemeStylesheetUserPreferences ssup) {
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
            String sQuery = "SELECT USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_NAME='"+userName+"' AND STYLESHEET_NAME='"+stylesheetName+"'";
            Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setThemeStylesheetUserPreferences() : "+sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            if(rs.next()) {
                sQuery = "UPDATE UP_USER_SS_PREFS SET USER_PREFERENCES_XML='"+outString.toString()+"' WHERE USER_NAME = '"+userName+"' AND STYLESHEET_NAME='"+stylesheetName+"'";
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setThemeStylesheetUserPreferences() : "+sQuery);
                stmt.executeUpdate(sQuery);
            }
            else {
                sQuery = "INSERT INTO UP_USER_SS_PREFS (USER_NAME,STYLESHEET_NAME,USER_PREFERENCES_XML) VALUES ('"+userName+"','"+stylesheetName+"','"+outString.toString()+"')";
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


    public void setCSSStylesheetUserPreferences(String userName, CoreCSSStylesheetUserPreferences cssup) {
        String stylesheetName=cssup.getStylesheetName();
        // construct a DOM tree
        Document doc = new org.apache.xerces.dom.DocumentImpl();
        Element spEl = doc.createElement("stylesheetuserpreferences");
        spEl.appendChild(constructParametersElement(cssup,doc));
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
            String sQuery = "SELECT USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_NAME='"+userName+"' AND STYLESHEET_NAME='"+stylesheetName+"'";
            Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setCSSStylesheetUserPreferences() : "+sQuery);
            ResultSet rs=stmt.executeQuery(sQuery);
            if(rs.next()) {
                sQuery = "UPDATE UP_USER_SS_PREFS SET USER_PREFERENCES_XML='"+outString.toString()+"' WHERE USER_NAME = '"+userName+"' AND STYLESHEET_NAME='"+stylesheetName+"'";
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setCSSStylesheetUserPreferences() : "+sQuery);
                stmt.executeUpdate(sQuery);
            }
            else {
                sQuery = "INSERT INTO UP_USER_SS_PREFS (USER_NAME,STYLESHEET_NAME,USER_PREFERENCES_XML) VALUES ('"+userName+"','"+stylesheetName+"','"+outString.toString()+"')";
                Logger.log(Logger.DEBUG,"UserPreferencesDBImpl::setCSSStylesheetUserPreferences() : "+sQuery);
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
                up.addChannelAttribute(attributeName,null);
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
                up.addFolderAttribute(attributeName,null);
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
        List l=up.getChannelAttributeNames();
        for(int i=0;i<l.size();i++) {
            String attributeName=(String) l.get(i);
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
        List l=up.getFolderAttributeNames();
        for(int i=0;i<l.size();i++) {
            String attributeName=(String) l.get(i);
            Element attributeEl=doc.createElement("attribute");
            attributeEl.setAttribute("name",attributeName);
            for(Enumeration e=up.getCategories();e.hasMoreElements();) {
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
