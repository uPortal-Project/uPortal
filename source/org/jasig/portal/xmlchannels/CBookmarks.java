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

package org.jasig.portal.xmlchannels;

import org.jasig.portal.*;

import javax.servlet.jsp.*;
import javax.servlet.http.*;

import org.apache.xalan.xslt.*;
import org.apache.xerces.dom.*;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.apache.xml.serialize.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.Hashtable;
import java.util.Enumeration;
import java.sql.*;
import java.io.*;

/**
 * A reference implementation of the Bookmarks xmlchannel.
 * The purpose of this code is to demonstrate the basic use of IXMLChannel interface.
 * <p>Bookmarks channel reads a simple XML file containing a list of bookmarks in it.
 * Depending on the request action type ("view", "edit", etc.)
 * Bookmarks channel applies different stylesheets to different locations of the DOM structure produced by reading the bookmarks.xml file. </p>
 * <p> Note the use of a helper StylesheetSet class </p>
 * <p> Exibits the same behavior as the regular CBookmarks Channel, will pull and push data
 * to and from the database, under the table name PORTAL_BOOKMARKS.
 * <p> If user's bookmarks are empty, then it will get the 'default' bookmarks from the
 * default (userid = 0) layout.
 * @author Peter Kharchenko
 * @author Steven Toth
 * @version $Revision$
 */

public class CBookmarks extends GenericPortalBean implements IChannel
{
  // a DOM where all the bookmark information will be contained
  protected Document bookmarksXML;

  ChannelStaticData staticData = new ChannelStaticData ();
  ChannelRuntimeData runtimeData = new ChannelRuntimeData ();
  private RdbmServices rdbmService = new RdbmServices ();
  private Connection con = null;

  // construct the URL for the location of the bookmarks.xml
  String fs = System.getProperty ("file.separator");

  // initialize StylesheetSet
  StylesheetSet set;

  // location of the stylesheet files
  String stylesheetDir = getPortalBaseDir () + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CBookmarks" + fs;

  // some variables to keep the state in.
  boolean editMode = false;
  boolean editBookmarkMode = false;
  boolean newBookmarkMode = false;

  // bookmark on which a current operation is being performed (such as editBookmark)
  int currentBookmark;

  public CBookmarks ()
  {
    // initialize a stylesheet set from a file
    // take a look at the *.ssl file for the stylesheet list format.
    // The format is a W3C-recommended default stylehseet binding
    set = new StylesheetSet (stylesheetDir + "CBookmarks.ssl");
    set.setMediaProps (getPortalBaseDir () + "properties" + fs + "media.properties");
  }

  // report static channel properties to the portal
  public ChannelSubscriptionProperties getSubscriptionProperties ()
  {
    ChannelSubscriptionProperties csb = new ChannelSubscriptionProperties ();
    
    // leave most properties at their default values, except a couple.
    csb.setName ("myWeb Bookmarks");
    csb.setEditable (true);
    return csb;
  }

  // report runtime channel properties to the portal
  public ChannelRuntimeProperties getRuntimeProperties ()
  {
    // channel will always render, so the default values are ok
    return new ChannelRuntimeProperties ();
  }


  // process Layout-level events comping from the portal
  public void receiveEvent (LayoutEvent ev)
  {
    if (ev.getEventNumber ()==ev.EDIT_BUTTON_EVENT)
    editMode=true;

    // or equivalently I could say
    // if(ev.getEventName().equals("editButtonEvent")) { ...
  }

  // receive ChannelStaticData from the portal
  public void setStaticData (ChannelStaticData sd)
  {
    staticData=sd;
  }
  
  protected Document getBookmarkXML ()
  {
    return  getBookmarkXML (runtimeData);
  }
  
  protected Document getBookmarkXML (ChannelRuntimeData rd)
  {
    if (bookmarksXML == null)
    {  
      //If there is no bookmarks, then go and get it from the database
      Connection con;
      String inputXML = null;
      
      try
      {
        con = this.rdbmService.getConnection ();
        String userid = GetUserID (rd.getHttpRequest ());
        ResultSet statem = con.createStatement ().executeQuery ("SELECT BOOKMARK_XML FROM PORTAL_BOOKMARKS WHERE PORTAL_USER_ID=" + userid);

        DOMParser domP = new DOMParser ();
        
        if (statem.next ())
        {
          inputXML = statem.getString ("BOOKMARK_XML");
        }
        else
        {
          statem = con.createStatement ().executeQuery ("select portal_bookmarks.bookmark_xml from portal_bookmarks , portal_users where portal_users.user_name = 'default' and portal_users.id = portal_bookmarks.portal_user_id ");
          statem.next ();
          inputXML = statem.getString ("BOOKMARK_XML");
          Statement cstate = con.createStatement ();
          cstate.executeQuery ("INSERT INTO PORTAL_BOOKMARKS VALUES ('"+userid+"','"+userid+"','"+inputXML+"')");
        }
        
        domP.parse (new InputSource (new StringReader (inputXML)));
        bookmarksXML=domP.getDocument ();
        rdbmService.releaseConnection (con);
      }
      catch (Exception e)
      {
        Logger.log (Logger.ERROR, e);
        Logger.log (Logger.ERROR, this.userID);
        
        if (inputXML != null)
        {
          Logger.log (Logger.ERROR,inputXML);
        }
      }
    }
    return bookmarksXML;
  }
  
  protected void saveXML (ChannelRuntimeData rd)
  {
    if ( bookmarksXML != null)
    {
      StringWriter outString = new StringWriter ();
      
      try
      {
        XMLSerializer xsl = new XMLSerializer (outString,new OutputFormat ( this.bookmarksXML ) );
        xsl.serialize (bookmarksXML);
        this.con = this.rdbmService.getConnection ();
        Statement statem = con.createStatement ();
        statem.executeUpdate ("UPDATE PORTAL_BOOKMARKS SET BOOKMARK_XML = '" + outString.toString () + "' WHERE PORTAL_USER_ID = " + GetUserID (rd.getHttpRequest ()));
      }
      catch (Exception e)
      {
        Logger.log (Logger.ERROR,e);
      }
    }
  }

  // receive ChannelRuntimeData from the portal and process actions passed in it
  public void setRuntimeData (ChannelRuntimeData rd)
  {
    this.runtimeData=rd;
    
    // process actions that are passed
    // the names of these parameters are entirely up to the channel.
    // please see the eidt stylesheet for the construction of the URLs that are used to pass actions to the channel.
    // in brief, action URL is constructe by runtimeData.getBaseActionURL() + any parameters you lke
    // in this case we are parsing "runtimeData.baseActionURL() + "action=something"+"&"+...
    String action;
    
    if ((action=runtimeData.getParameter ("action")) != null)
    {
      if (action.equals ("doneEditing"))
      {  
        //if editing is done, then save the XML to the database and exit edit mode.
        saveXML (rd);
        editMode=false;
      }
      else if (action.equals ("delete"))
      {
        deleteBookmark (Integer.parseInt (runtimeData.getParameter ("bookmark")));
      }
      else if (action.equals ("edit"))
      {
        editBookmarkMode=true;
        currentBookmark=Integer.parseInt (runtimeData.getParameter ("bookmark"));
      }
      else if (action.equals ("new"))
      {
        newBookmarkMode=true;
      }
      else if (action.equals ("saveBookmark"))
      {
        editBookmarkMode=false;
        Element bookmark = (Element) ((getBookmarkXML ()).getElementsByTagName ("bookmark")).item (Integer.parseInt (runtimeData.getParameter ("bookmark"))-1);

        bookmark.setAttribute ("name", runtimeData.getParameter ("name"));
        bookmark.setAttribute ("url", makeUrlSafe (runtimeData.getParameter ("url")));
        bookmark.setAttribute ("comments", runtimeData.getParameter ("comments"));
      }
      else if (action.equals ("addBookmark"))
      {
        newBookmarkMode=false;
        Node bookmarks= ((getBookmarkXML ()).getElementsByTagName ("bookmarks")).item (0);
        Element bookmark=getBookmarkXML ().createElement ("bookmark");
        bookmark.setAttribute ("name", runtimeData.getParameter ("name"));
        bookmark.setAttribute ("url", makeUrlSafe (runtimeData.getParameter ("url")));
        bookmark.setAttribute ("comments", runtimeData.getParameter ("comments"));
        bookmarks.appendChild (bookmark);
      }
    }
  }

  public static String makeUrlSafe (String url)
  {
    String safeUrl = url.toLowerCase ();
    
    if (!(safeUrl.startsWith ("http://")))
    {
      if (safeUrl.startsWith ("http:/"))
      {
        safeUrl = safeUrl.substring (0,6) + "/" + safeUrl.substring (7);
      }
      else
      {
        if (safeUrl.indexOf ('@') !=-1 )
        {
          if (!safeUrl.startsWith ("mailto:"))
          {
            safeUrl = "mailto:" + safeUrl;
          }
        }
        else
        {
          safeUrl = "http://" + safeUrl;
        }
      }
    }
    return safeUrl;
  }

  // output channel content to the portal
  public void renderXML (DocumentHandler out)
  {
    try
    {
      if (set!=null)
      {
        // test in a order of precedence
        if (editBookmarkMode)
        {
          renderEditBookmarkXML (out, currentBookmark);
        }
        else if (newBookmarkMode)
        {
          renderNewBookmarkXML (out);
        }
        else if (editMode)
        {
          renderEditXML (out);
        }
        // default
        else renderViewXML (out);
      }
    } 
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e); 
    }
  }

  // the rest are private helper functions, should be rather self-explanatory
  
  private void renderViewXML (DocumentHandler out) throws org.xml.sax.SAXException
  {
    // a block, typical for the IChannel:
    //  - use the StylesheetSet to get an appropriate stylesheet
    //  - instansiation an XSLT processor
    //  - fire up the transformation
    XSLTInputSource stylesheet=set.getStylesheet ("view",runtimeData.getHttpRequest ());

    if (stylesheet!=null)
    {
      XSLTProcessor processor = XSLTProcessorFactory.getProcessor (new org.apache.xalan.xpath.xdom.XercesLiaison ());
      processor.process (new XSLTInputSource (getBookmarkXML ()),stylesheet,new XSLTResultTarget (out));
    } 
    else 
      Logger.log (Logger.ERROR,"BookmarksChannel::renderViewXML() : unable to find a stylesheet for rendering");
  }

  private void renderEditXML (DocumentHandler out) throws org.xml.sax.SAXException
  {
    XSLTInputSource stylesheet=set.getStylesheet ("edit",runtimeData.getHttpRequest ());
    
    if (stylesheet!=null)
    {
      XSLTProcessor processor = XSLTProcessorFactory.getProcessor (new org.apache.xalan.xpath.xdom.XercesLiaison ());
      processor.setStylesheetParam ("baseActionURL",processor.createXString (runtimeData.getBaseActionURL ()));
      processor.process (new XSLTInputSource (getBookmarkXML ()),stylesheet,new XSLTResultTarget (out));
    } 
    else 
      Logger.log (Logger.ERROR,"BookmarksChannel::renderEditXML() : unable to find a stylesheet for rendering");
  }

  private void renderEditBookmarkXML (DocumentHandler out,int bookmarkNumber) throws org.xml.sax.SAXException
  {
    Node bookmark= ((getBookmarkXML ()).getElementsByTagName ("bookmark")).item (bookmarkNumber-1);
    XSLTInputSource stylesheet=set.getStylesheet ("editbookmark",runtimeData.getHttpRequest ());
 
    if (stylesheet!=null)
    {
      XSLTProcessor processor = XSLTProcessorFactory.getProcessor (new org.apache.xalan.xpath.xdom.XercesLiaison ());
      processor.setStylesheetParam ("channelID",processor.createXString (staticData.getChannelID ()));
      processor.setStylesheetParam ("bookmarkID",processor.createXString (String.valueOf (bookmarkNumber)));
      processor.process (new XSLTInputSource (bookmark),stylesheet,new XSLTResultTarget (out));
    } 
    else 
      Logger.log (Logger.ERROR,"BookmarksChannel::renderEditBookmarkXML() : unable to find a stylesheet for rendering");
  }

  private void renderNewBookmarkXML (DocumentHandler out) throws org.xml.sax.SAXException
  {
    // this is interesting since there's is no real content being presented ...
    // we know the expected structure of the information, but we don't have any data yet.
    // for now, the best thing I can think of is creating an empty template, i.e. an empty
    // bookmark and feeding it to the XSLT.
    //	Document doc=new DocumentImpl();
    Element bookmark=getBookmarkXML ().createElement ("bookmark");
    bookmark.setAttribute ("name","");
    bookmark.setAttribute ("url","");
    bookmark.setAttribute ("comments","");

    XSLTInputSource stylesheet=set.getStylesheet ("editbookmark",runtimeData.getHttpRequest ());

    if (stylesheet!=null)
    {
      XSLTProcessor processor = XSLTProcessorFactory.getProcessor (new org.apache.xalan.xpath.xdom.XercesLiaison ());
      processor.setStylesheetParam ("channelID",processor.createXString (staticData.getChannelID ()));
      processor.setStylesheetParam ("newBookmark",processor.createXString ("true"));
      processor.process (new XSLTInputSource (bookmark),stylesheet,new XSLTResultTarget (out));
    } 
    else 
      Logger.log (Logger.ERROR,"BookmarksChannel::renderEditBookmarkXML() : unable to find a stylesheet for rendering");
  }

  private void deleteBookmark (int bookmarkNumber)
  {
    Document root = this.getBookmarkXML ();
    NodeList elements = root.getElementsByTagName ("bookmark");
    Node bookmark=elements.item (bookmarkNumber - 1);
    
    if (bookmark!=null)
    {
       (bookmark.getParentNode ()).removeChild (bookmark);
    } 
    else 
      Logger.log (Logger.ERROR,"BookmarksChannel::deleteBookmark() : attempting to remove nonexistent bookmark #"+bookmarkNumber);
  }

  protected String userID=null;
  
  protected String GetUserID (HttpServletRequest req)
  {  
    //goes to the DB and gets the user ID unless is has already been collected.
    if (userID == null)
    {
      HttpSession session = req.getSession (false);
      Connection con = null;
      String username = (String)session.getAttribute ("userName");
      
      if (username ==null)
      {
        username = "guest";
      }
      
      try
      {
        con = rdbmService.getConnection ();
        Statement stmt = con.createStatement ();
        String userQuery = "SELECT ID FROM PORTAL_USERS WHERE USER_NAME = '" + username + "'";
        ResultSet rs = stmt.executeQuery (userQuery);
        
        if (rs.next ())
        {
          userID = rs.getString ("ID");
        }
        
        stmt.close ();
      }
      catch (Exception e)
      {
        Logger.log (Logger.ERROR, e);
      }
      finally
      {
        rdbmService.releaseConnection (con);
      }
    }
    
    return userID;
  }
}
