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

import java.sql.*;
import org.w3c.dom.*;

import org.apache.xalan.xpath.*;
import org.apache.xalan.xslt.*;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;

/**
 * UserLayoutManager participates in all operations associated with the
 * user layout and user preferences.
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class UserLayoutManager extends GenericPortalBean {
    private Document uLayoutXML;

    // this one will contain user Layout XML with attrubutes for the
    // first (structure) transformation
    private Document argumentedUserLayoutXML;

    private MediaManager mediaM;
    
    private UserPreferences up;
    private UserPreferences complete_up;
    


    /**
     * Constructor does the following
     *  1. Read layout.properties
     *  2. read userLayout from the database
     *  @param the servlet request object
     *  @param user name
     */

  public UserLayoutManager (HttpServletRequest req, String sUserName)
  {
    String sLayoutDtd = "userLayout.dtd";
    String sPathToLayoutDtd = null;
    boolean bPropsLoaded = false;

    String fs = System.getProperty ("file.separator");
    String propertiesDir = getPortalBaseDir () + "properties" + fs;
    MediaManager mediaM = new MediaManager (propertiesDir + "media.properties", propertiesDir + "mime.properties", propertiesDir + "serializer.properties");
    
    String str_uLayoutXML = null;
    uLayoutXML = null;

    /*      ICoreStylesheetDescriptionDB csddb=new CoreStylesheetDescriptionDBImpl();
      csddb.removeStructureStylesheetDescription("Tab and Column layout");
      csddb.removeThemeStylesheetDescription("Nested tables");
      csddb.removeCSSStylesheetDescription("general CSS");
      csddb.addStructureStylesheetDescription("webpages/stylesheets/org/jasig/portal/LayoutBean/uLayout2sLayout.sdf","webpages/stylesheets/org/jasig/portal/LayoutBean/uLayout2sLayout.xsl");
      csddb.addThemeStylesheetDescription("webpages/stylesheets/org/jasig/portal/LayoutBean/sLayout2html_full.sdf","webpages/stylesheets/org/jasig/portal/LayoutBean/sLayout2html_full.xsl");
      csddb.addCSSStylesheetDescription("webpages/stylesheets/general.sdf","webpages/stylesheets/general.css");*/
//      StructureStylesheetDescription fssd=csddb.getStructureStylesheetDescription("Tab and Column layout");
//       Hashtable list=csddb.getStructureStylesheetList("netscape");
//       for (Enumeration e = list.keys() ; e.hasMoreElements() ;) {
//   	Logger.log(Logger.DEBUG,(String) e.nextElement());
//       }
//      Hashtable list=csddb.getThemeStylesheetList("Tab and Column layout");
//      for (Enumeration e = list.keys() ; e.hasMoreElements() ;) {
//  	Logger.log(Logger.DEBUG,(String) e.nextElement());
//      }
//      list=csddb.getCSSStylesheetList("Nested tables");
//      for (Enumeration e = list.keys() ; e.hasMoreElements() ;) {
//  	Logger.log(Logger.DEBUG,(String) e.nextElement());
//      }


    /*    IUserPreferencesDB updb=new UserPreferencesDBImpl();
	  StructureStylesheetUserPreferences fsup= updb.getStructureStylesheetUserPreferences("guest","Tab and Column layout");
    //updb.setStructureStylesheetUserPreferences("guest","Tab and Column layout",fsup);
    Logger.log(Logger.DEBUG,"first stylesheet name = "+updb.getStructureStylesheetName("guest","netscape"));
    Logger.log(Logger.DEBUG,"second stylesheet name = "+updb.getThemeStylesheetName("guest","netscape"));
    Logger.log(Logger.DEBUG,"CSS stylesheet name = "+updb.getCSSStylesheetName("guest","netscape"));
    updb.setStructureStylesheetName("Something other then tabs","guest","netscape");
    updb.setThemeStylesheetName("UnNested tables","guest","netscape");
    updb.setCSSStylesheetName("specific.CSS","guest","netscape");
    Logger.log(Logger.DEBUG,"first stylesheet name = "+updb.getStructureStylesheetName("guest","netscape"));
    Logger.log(Logger.DEBUG,"second stylesheet name = "+updb.getThemeStylesheetName("guest","netscape"));
    Logger.log(Logger.DEBUG,"CSS stylesheet name = "+updb.getCSSStylesheetName("guest","netscape")); 
    updb.setStructureStylesheetName("Tab and Column layout","guest","netscape");
    updb.setThemeStylesheetName("Nested tables","guest","netscape");
    updb.setCSSStylesheetName("general.CSS","guest","netscape");
    
    updb.setStructureStylesheetName("Tab and Column layout","guest2","netscape");

    updb.getUserPreferences("guest","netscape"); */

    try {
	if (!bPropsLoaded) {
	    File layoutPropsFile = new File (getPortalBaseDir () + "properties" + File.separator + "layout.properties");
	    Properties layoutProps = new Properties ();
	    layoutProps.load (new FileInputStream (layoutPropsFile));
	    sPathToLayoutDtd = layoutProps.getProperty ("pathToUserLayoutDTD");
	    bPropsLoaded = true;
	}
	
	// read uLayoutXML
	
	HttpSession session = req.getSession (false);
	uLayoutXML = (Document) session.getAttribute ("userLayoutXML");
	
	if (uLayoutXML == null) {
	    if (sUserName == null)
		sUserName = "guest";
	    
	    IUserLayoutDB uldb=new UserLayoutDBImpl();
	    
	    str_uLayoutXML = uldb.getUserLayout(sUserName,"netscape");
	    
	    if(str_uLayoutXML!=null) {
		// Tack on the full path to user_layout.dtd
		//peterk: should be done on the SAX level or not at all
		int iInsertBefore = str_uLayoutXML.indexOf (sLayoutDtd);
		
		if (iInsertBefore != -1) 
		    str_uLayoutXML = str_uLayoutXML.substring (0, iInsertBefore) + sPathToLayoutDtd + str_uLayoutXML.substring (iInsertBefore);
		
		// read in the layout DOM
		// note that we really do need to have a DOM structure here in order to introduce
		// persistent changes on the level of userLayout.
		//org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
		org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser ();
		
		// set parser features
		parser.setFeature ("http://apache.org/xml/features/validation/dynamic", true);
		
		parser.parse (new org.xml.sax.InputSource (new StringReader (str_uLayoutXML)));
		uLayoutXML = parser.getDocument ();
		session.setAttribute ("userLayoutXML", uLayoutXML);
	    }
	}
	

	// load user preferences
	IUserPreferencesDB updb=new UserPreferencesDBImpl();
	UserPreferences temp_up=updb.getUserPreferences(sUserName,mediaM.getMedia(req)); 
	if(temp_up==null) temp_up=updb.getUserPreferences(sUserName,mediaM.getDefaultMedia()); 
	this.setCurrentUserPreferences(temp_up,req.getSession (false));

    } catch (Exception e) { Logger.log(Logger.ERROR,e);}
  }
    
    
    private void synchUserPreferencesWithLayout(UserPreferences someup) {

	StructureStylesheetUserPreferences fsup=someup.getStructureStylesheetUserPreferences();	
	ThemeStylesheetUserPreferences ssup=someup.getThemeStylesheetUserPreferences();

	// make a list of channels in the XML Layout
	NodeList channelNodes=uLayoutXML.getElementsByTagName("channel");
	HashSet channelSet=new HashSet();
	for(int i=0;i<channelNodes.getLength();i++) {
	    Element el=(Element) channelNodes.item(i);
	    if(el!=null) {
		String chID=el.getAttribute("ID");
		if(!fsup.hasChannel(chID)) {
		    fsup.addChannel(chID);
		    Logger.log(Logger.DEBUG,"UserLayoutManager::synchUserPreferencesWithLayout() : StructureStylesheetUserPreferences were missing a channel="+chID);
		}
		if(!ssup.hasChannel(chID)) {
		    ssup.addChannel(chID);
		    Logger.log(Logger.DEBUG,"UserLayoutManager::synchUserPreferencesWithLayout() : ThemeStylesheetUserPreferences were missing a channel="+chID);
		}
		channelSet.add(chID);
	    }

	}
	
	// make a list of categories in the XML Layout
	NodeList categoryNodes=uLayoutXML.getElementsByTagName("category");
	HashSet categorySet=new HashSet();
	for(int i=0;i<categoryNodes.getLength();i++) {
	    Element el=(Element) categoryNodes.item(i);
	    if(el!=null) {
		String caID=el.getAttribute("ID");
		if(!fsup.hasCategory(caID)) {
		    fsup.addCategory(caID);
		    Logger.log(Logger.DEBUG,"UserLayoutManager::synchUserPreferencesWithLayout() : StructureStylesheetUserPreferences were missing a category="+caID);
		}
		categorySet.add(caID);
	    }
	}
	
	// cross check
	for(Enumeration e=fsup.getChannels();e.hasMoreElements();) {
	    String chID=(String)e.nextElement();
	    if(!channelSet.contains(chID)) {
		fsup.removeChannel(chID);
		Logger.log(Logger.DEBUG,"UserLayoutManager::synchUserPreferencesWithLayout() : StructureStylesheetUserPreferences had a non-existent channel="+chID);
	    }
	}

	for(Enumeration e=fsup.getCategories();e.hasMoreElements();) {
	    String caID=(String)e.nextElement();
	    if(!categorySet.contains(caID)) {
		fsup.removeCategory(caID);
		Logger.log(Logger.DEBUG,"UserLayoutManager::synchUserPreferencesWithLayout() : StructureStylesheetUserPreferences had a non-existent category="+caID);
	    }
	}

	for(Enumeration e=ssup.getChannels();e.hasMoreElements();) {
	    String chID=(String)e.nextElement();
	    if(!channelSet.contains(chID)) {
		ssup.removeChannel(chID);
		Logger.log(Logger.DEBUG,"UserLayoutManager::synchUserPreferencesWithLayout() : ThemeStylesheetUserPreferences had a non-existent channel="+chID);
	    }
	}
	someup.setStructureStylesheetUserPreferences(fsup);	
	someup.setThemeStylesheetUserPreferences(ssup);

    }

    public UserPreferences getCompleteCurrentUserPreferences() {
	return complete_up;
    }

    public void setCurrentUserPreferences(UserPreferences current_up,HttpSession session) {
	up=current_up;
	// load stylesheet description files and fix user preferences
	ICoreStylesheetDescriptionDB csddb=new CoreStylesheetDescriptionDBImpl();	
	
	// clean up
	StructureStylesheetUserPreferences fsup=up.getStructureStylesheetUserPreferences();	
	StructureStylesheetDescription fssd=csddb.getStructureStylesheetDescription(fsup.getStylesheetName());
	if(fssd==null) {
	    // assign a default stylesheet instead
	    
	} else {
	    fsup.synchronizeWithDescription(fssd);
	}
	
	ThemeStylesheetUserPreferences ssup=up.getThemeStylesheetUserPreferences();
	ThemeStylesheetDescription sssd=csddb.getThemeStylesheetDescription(ssup.getStylesheetName());
	if(sssd==null) {
	    // assign a default stylesheet instead
	} else {
	    ssup.synchronizeWithDescription(sssd);
	}
	
	CoreCSSStylesheetUserPreferences cssup=up.getCoreCSSStylesheetUserPreferences();
	CoreCSSStylesheetDescription csssd=csddb.getCSSStylesheetDescription(cssup.getStylesheetName());
	if(csssd==null) {
	    // assign a default stylesheet instead
	} else {
	    cssup.synchronizeWithDescription(csssd);
	}
	
	// in case something was reset to default
	up.setStructureStylesheetUserPreferences(fsup);
	up.setThemeStylesheetUserPreferences(ssup);
	up.setCoreCSSStylesheetUserPreferences(cssup);


	// now generate "filled-out copies" 
	complete_up=new UserPreferences(up);
	// syncronize up with layout
	synchUserPreferencesWithLayout(complete_up);
	StructureStylesheetUserPreferences complete_fsup=complete_up.getStructureStylesheetUserPreferences();	
	ThemeStylesheetUserPreferences complete_ssup=complete_up.getThemeStylesheetUserPreferences();
	CoreCSSStylesheetUserPreferences complete_cssup=complete_up.getCoreCSSStylesheetUserPreferences();
	complete_fsup.completeWithDescriptionInformation(fssd);
	complete_ssup.completeWithDescriptionInformation(sssd);
	complete_cssup.completeWithDescriptionInformation(csssd);
	complete_up.setStructureStylesheetUserPreferences(complete_fsup);
	complete_up.setThemeStylesheetUserPreferences(complete_ssup);
	complete_up.setCoreCSSStylesheetUserPreferences(complete_cssup);

	// complete user preferences are used to:
	//  1. fill out userLayoutXML with attributes required for the first transform
	//  2. contruct a filter that will fill out attributes required for the second transform
	//  3. revamp the CSS stylesheet to include user parameters
	
	argumentedUserLayoutXML=(Document) uLayoutXML.cloneNode(true);
	
	// deal with category attributes first
	NodeList categoryElements=argumentedUserLayoutXML.getElementsByTagName("category");
	if(categoryElements==null) Logger.log(Logger.DEBUG,"UserLayoutManager::setCurrentUserPreferences() : empty list of category elements obtained!");
	List cl=complete_fsup.getCategoryAttributeNames();
	for(int j=0;j<cl.size();j++) {
	    for(int i=categoryElements.getLength()-1;i>=0;i--) {
		Element categoryElement=(Element) categoryElements.item(i);
		categoryElement.setAttribute((String) cl.get(j),complete_fsup.getCategoryAttributeValue(categoryElement.getAttribute("ID"),(String) cl.get(j)));
		Logger.log(Logger.DEBUG,"UserLayoutManager::setCurrentUserPreferences() : added attribute "+(String) cl.get(j)+"="+complete_fsup.getCategoryAttributeValue(categoryElement.getAttribute("ID"),(String) cl.get(j))+" for a category "+categoryElement.getAttribute("ID"));
	    }
	}
	// channel attributes
	NodeList channelElements=argumentedUserLayoutXML.getElementsByTagName("channel");
	if(channelElements==null) Logger.log(Logger.DEBUG,"UserLayoutManager::setCurrentUserPreferences() : empty list of channel elements obtained!");
	List chl=complete_fsup.getChannelAttributeNames();
	for(int j=0;j<chl.size();j++) {
	    for(int i=channelElements.getLength()-1;i>=0;i--) {
		Element channelElement=(Element) channelElements.item(i);
		channelElement.setAttribute((String) chl.get(j),complete_fsup.getChannelAttributeValue(channelElement.getAttribute("ID"),(String) chl.get(j)));
		Logger.log(Logger.DEBUG,"UserLayoutManager::setCurrentUserPreferences() : added attribute "+(String) chl.get(j)+"="+complete_fsup.getChannelAttributeValue(channelElement.getAttribute("ID"),(String) chl.get(j))+" for a channel "+channelElement.getAttribute("ID"));
	    }
	}
    }



  public Node getNode (String elementID) 
  {
    return argumentedUserLayoutXML.getElementById (elementID);
  }
  
  public Node getRoot () 
  {
    return argumentedUserLayoutXML;
  }


  public void minimizeChannel (String str_ID) 
  {
    Element channel = argumentedUserLayoutXML.getElementById (str_ID);
    
    if (channel != null) 
    {
      if (channel.getAttribute ("minimized").equals ("false"))
        channel.setAttribute ("minimized", "true");
      else 
        channel.setAttribute ("minimized", "false");
    } 
    else 
      Logger.log (Logger.ERROR, "UserLayoutManager::minimizeChannel() : unable to find a channel with ID=" + str_ID);
  }

  public void removeChannel (String str_ID) 
  {
      // warning .. the channel should also be removed from uLayoutXML
    Element channel = argumentedUserLayoutXML.getElementById (str_ID);
    
    if (channel != null) 
    {
      Node parent = channel.getParentNode ();
      
      if (parent != null) 
        parent.removeChild (channel);
      else 
        Logger.log (Logger.ERROR, "UserLayoutManager::removeChannel() : attempt to remove a root node !");
    } 
    else Logger.log (Logger.ERROR, "UserLayoutManager::removeChannel() : unable to find a channel with ID="+str_ID);
  }

    private Element getChildByTagName(Node node,String tagName) {
	if(node==null) return null;
	NodeList children=node.getChildNodes();
	for(int i=children.getLength()-1;i>=0;i--) {
	    Node child=children.item(i);
	    if(child.getNodeType()==Node.ELEMENT_NODE) {
		Element el=(Element) child;
		if((el.getTagName()).equals(tagName))
		    return el;
	    }
	}
	return null;
    }
}
