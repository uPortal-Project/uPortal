package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import org.jasig.portal.*;
import org.jasig.portal.channels.bookmarks.*;
import com.objectspace.xml.*;
import java.sql.*;

/**
 * This is a channel for storing user-defined bookmarks.
 * 
 * @author M. Barton
 * @version $Revision$
 */

public class CBookmarks extends GenericPortalBean implements org.jasig.portal.IChannel                         
{ 
  ChannelConfig chConfig = null;
  private String xmlFilePackage = "org.jasig.portal.channels.bookmarks";
  private RdbmServices rdbmService = new RdbmServices ();
  private Connection con = null;
  private File dtdFile = new File (getPortalBaseDir () + "webpages" + File.separator + "dtd" + File.separator + "bookmarks.dtd");
  
  protected void doAddBookmark (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
	  try
	  {
	    // Retrieve bookmarkXml
  	  IBookmarks bm = getBookmarkData(req);
  	  	
	    out.println ("<form action=\"dispatch.jsp\">");
	    out.println ("<table>");
	    out.println ("  <tr>");
	    out.println ("    <th>Name</th>");
	    out.println ("    <th>URL</th>");
	    out.println ("    <th>Comments</th>");
	    out.println ("  </tr>");
	    out.println ("  <tr>");
	    out.println ("    <td><input type=text name=name value=\"\" size=20></td>");
	    out.println ("    <td><input type=text name=url value=\"\" size=40></td>");
	    out.println ("    <td><input type=text name=comments value=\"\" size=40></td>");
	    out.println ("  </tr>");
	    out.println ("</table>");
	    out.println ("<input type=\"hidden\" name=\"method\" value=\"edit\">");
	    out.println ("<input type=\"hidden\" name=\"action\" value=\"save\">");
	    out.println ("<input type=\"submit\" name=\"submit\" value=\"Save\">");
      out.println ("<input type=\"hidden\" name=\"channelID\" value=\"" + chConfig.getChannelID () + "\">");
	    out.println ("</form>");        
	  }
	  catch (Exception e)
	  {
	    Logger.log (Logger.ERROR, e);
	  }
  }
  
  protected void doDeleteBookmark (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
	  try
	  {
	    int i = Integer.parseInt(req.getParameter("bookmark"));
  		
	    // Open xml file -- this should eventually be retrieved from a database
	    //IXml xml = Xml.openDocument (xmlFilePackage, xmlFile);
	    //IBookmarks bm = (IBookmarks) xml.getRoot ();

	    // Retrieve bookmarkXml
	    IXml xml = getBookmarkXml(req);
	    IBookmarks bm = (IBookmarks) xml.getRoot ();
  	    //IBookmarks bm = getBookmarkData(req);
  	  
	    bm.removeBookmarkAt(i);
  	  
	    // Write this to a database
	    //xml.saveDocument (xmlFile);
  	  
	    // Store bookmarkXml in session
	    // Write bookmarkXml to database
	    HttpSession session = req.getSession (false);
	    session.setAttribute ("bookmarkXml", xml);
	    saveBookMarkXml(req);	  
  	  
	    res.sendRedirect (DispatchBean.buildURL ("edit", chConfig));
	  }
	  catch (Exception e)
	  {
	    Logger.log (Logger.ERROR, e);
	  }
  }
  
  protected void doEditBookmark (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
	  try
	  {
	    int i = Integer.parseInt (req.getParameter("bookmark"));
  		
	    // Open xml file -- this should eventually be retrieved from a database
	    //IXml xml = Xml.openDocument (xmlFilePackage, xmlFile);
	    //IBookmarks bm = (IBookmarks) xml.getRoot ();
	    // Retrieve bookmarkXml
  	    IBookmarks bm = getBookmarkData(req);
  	  
	    IBookmark bookmark = bm.getBookmarkAt(i);
	    String sName = bookmark.getAttribute("name");
	    String sUrl = bookmark.getAttribute("url");
	    String sComments = bookmark.getAttribute("comments");
  		
	    out.println ("<form action=\"dispatch.jsp\">");
	    out.println ("<table>");
	    out.println ("  <tr>");
	    out.println ("    <th>Name</th>");
	    out.println ("    <th>URL</th>");
	    out.println ("    <th>Comments</th>");
	    out.println ("  </tr>");
	    out.println ("  <tr>");
	    out.println ("    <td><input type=text name=name value=\"" + sName + "\" size=20></td>");
	    out.println ("    <td><input type=text name=url value=\"" + sUrl + "\" size=40></td>");
	    out.println ("    <td><input type=text name=comments value=\"" + sComments + "\" size=40></td>");
	    out.println ("  </tr>");
	    out.println ("</table>");
	    out.println ("<input type=hidden name=bookmark value=\""+ i + "\">");
	    out.println ("<input type=hidden name=method value=\"edit\">");
	    out.println ("<input type=hidden name=action value=\"save\">");
	    out.println ("<input type=submit name=submit value=\"Save\">");
      out.println ("<input type=\"hidden\" name=\"channelID\" value=\"" + chConfig.getChannelID () + "\">");
	    out.println ("</form>");
	  }
	  catch (Exception e)
	  {
	    Logger.log (Logger.ERROR, e);
	  }
  }
  
  protected void doFinishedEditing (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
	  try
	  {    
	    HttpSession session = req.getSession (false);
	    DispatchBean dispatchBean = (DispatchBean) session.getAttribute ("dispatchBean");
	    dispatchBean.finish (req, res);
	  }
	  catch (Exception e)
	  {
	    Logger.log (Logger.ERROR, e);
	  }
  }
  
  protected void doListBookmarks (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
	  try
	  {
	    // Open xml file -- this should eventually be retrieved from a database
	    //IXml xml = Xml.openDocument (xmlFilePackage, xmlFile);
	    //IBookmarks bm = (IBookmarks) xml.getRoot ();
	    // Retrieve bookmarkXml
  	    IBookmarks bm = getBookmarkData(req);	  
  		
	    // Get Bookmarks
	    IBookmark[] bookmarks = bm.getBookmarks ();
  		
	    out.println ("<table>");
	    out.println ("  <tr>");
	    out.println ("    <th>Name</th>");
	    out.println ("    <th>URL</th>");
	    out.println ("    <th>Comments</th>");
	    out.println ("  </tr>");
  		
	    for (int i = 0; i < bookmarks.length; i++)
	    {
		    out.println ("  <tr bgcolor=#eeeeee>");
		    out.println ("    <td>" + bookmarks[i].getAttribute ("name") + "</td>");
		    out.println ("    <td>" + bookmarks[i].getAttribute ("url") + "</td>");
		    out.println ("    <td>" + bookmarks[i].getAttribute ("comments") + "</td>");
		    out.println ("    <td>[<a href=\"" + DispatchBean.buildURL("edit", chConfig) + "&action=edit&bookmark=" + i + "\">edit</a>][<a href=\"" + DispatchBean.buildURL("edit", chConfig) + "&action=delete&bookmark=" + i + "\">delete</a>]</td>");
		    //out.println ("    <td>[<a href=\"dispatch.jsp?method=edit&action=edit&bookmark=" + i + "\">edit</a>][<a href=\"dispatch.jsp?method=edit&action=delete&bookmark=" + i + "\">delete</a>]</td>");
		    out.println ("  </tr>");
	    }
	    
	    out.println ("</table>");
	    out.println ("<table border=0><tr><form>");
	    out.println ("<td><br><input type=button name=add value=\"Add Bookmark\" onClick=\"location=\'" + DispatchBean.buildURL ("edit", chConfig) + "&action=add\'\"></td>");
	    out.println ("<td><br><input type=button name=finished value=\"Finished\" onClick=\"location=\'" + DispatchBean.buildURL("edit", chConfig) + "&action=done\'\"></td>");
	    out.println ("</form></tr></table>");
	  }
	  catch (Exception e)
	  {
	    Logger.log (Logger.ERROR, e);
	  }
  }
  
  protected void doSaveBookmark (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
	  int i = 0;
  	
	  try
	  {        
	    // Open xml file -- this should eventually be retrieved from a database
	    //IXml xml = Xml.openDocument (xmlFilePackage, xmlFile);
	    //IBookmarks bm = (IBookmarks) xml.getRoot ();
	    // Retrieve bookmarkXml
	    IXml xml = getBookmarkXml(req);
	    IBookmarks bm = (IBookmarks) xml.getRoot ();
  	   
	    String sName = req.getParameter ("name");
	    String sUrl = req.getParameter ("url");
	    String sComments = req.getParameter ("comments");
  	  
	    // Are we coming from adding or editing?
	    if (req.getParameter("bookmark") == null)
	    {
		  // We are adding
		  IBookmark bookmark = Factory.newBookmark();
		  bookmark.setAttribute("name", sName);
		  bookmark.setAttribute("url", sUrl);
		  bookmark.setAttribute("comments", sComments);
		  bm.addBookmark(bookmark);
	    }
	    else
	    {
		  // We are editing
		  i = Integer.parseInt(req.getParameter("bookmark"));
		  IBookmark bookmark = bm.getBookmarkAt(i);
		  bookmark.setAttribute("name", sName);
		  bookmark.setAttribute("url", sUrl);
		  bookmark.setAttribute("comments", sComments);
	    }
  	  
	    // Write this to a database
	    //xml.saveDocument (xmlFile);
  	  
	    // Write bookmarkXml to database
	    HttpSession session = req.getSession (false);
	    session.setAttribute ("bookmarkXml", xml);
	    saveBookMarkXml(req);
  		
	    res.sendRedirect (DispatchBean.buildURL ("edit", chConfig));
	  }
	  catch (Exception e)
	  {
	    Logger.log (Logger.ERROR, e);
	  }
  }
  
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
	  try
	  {
	    String sAction = req.getParameter("action");

	    if (sAction == null)
		    doListBookmarks (req, res, out);
	    else if (sAction.equals("edit"))
		    doEditBookmark (req, res, out);
	    else if (sAction.equals ("delete"))
		    doDeleteBookmark (req, res, out);
	    else if (sAction.equals ("add"))
		    doAddBookmark (req, res, out);
	    else if (sAction.equals ("save"))
		    doSaveBookmark (req, res, out);
	    else if (sAction.equals ("done"))
		    doFinishedEditing (req, res, out);
	  }
	  catch (Exception e)
	  {
	    Logger.log (Logger.ERROR, e);
	  }
  }
  
  /**
  * This method was created in VisualAge.
  * @return org.jasig.portal.channels.bookmarks.IBookmarks
 	  getBookmarkData(HttpServletRequest req)  does just what the method name says
 	  calls getBookmarkXml(req) and returns a bookmarks object
  */
  public IBookmarks getBookmarkData(HttpServletRequest req) {

	  IBookmarks bm = null;
  	
	  try {
		  //Logger.log (Logger.DEBUG, "Inside getBookmarkData, before call to getBookmarkXml");
		  IXml xml = getBookmarkXml(req);
		  bm = (IBookmarks) xml.getRoot ();
		  //Logger.log (Logger.DEBUG, "after call to getBookmarkXml");
	  }
	  catch (Exception e) {
	    e.printStackTrace ();
	  }
  	
	  return bm;
  }
  
  /**
	checks the session for bookmark_xml for user
		if bookmark_xml exists in session
			return bookmark_xml

		if bookmark_xml doesn't exist return nothing
			else return user's bookmark_xml
   **/
   
  private IXml getBookmarkXml (HttpServletRequest req)
  {
	  IXml bookmarkXml = null;

	  // getBookMarkXML from session
	  //bookMarkXml = getBookmarkXmlFromSession(req);
	  //if (!bookMarkXml.equals(null)) return bookMarkXml;

	  //Logger.log (Logger.DEBUG, "before call to database");
  	
	  // if no bookmark XML in session, get bookmark XML from database
	  bookmarkXml = getBookmarkXmlFromDatabase(req);
	  //Logger.log (Logger.DEBUG, "after call to database");

	  return bookmarkXml;
  }
  
  /**
	* get ID associated with User
	* query portal_bookmarks table portal_bookmarks.ID = portal_users.ID
	* if bookmark_xml doesn't exist return an empty bookmarks channel
	* else return user's bookmark_xml
	*
	* needs better processing of exceptions.
	* consider creation of PortalExceptionHandler method
	*
   **/
  private IXml getBookmarkXmlFromDatabase (HttpServletRequest req)
  {
	  IXml bookmarkXml = null;
	  String id = null;
  	
	  //consider putting this code in getUserIdFromDatabase method
	  String sUserName = getUserName(req).equals(null)?"guest":getUserName(req);
	  //Logger.log (Logger.DEBUG, sUserName);
  	
	  try
	  {
		  con = rdbmService.getConnection ();
		  Statement stmt = con.createStatement();

		  String userQuery = "SELECT ID FROM PORTAL_USERS WHERE USER_NAME = '" + sUserName + "'" ;
		  Logger.log (Logger.DEBUG, userQuery);
		  debug(userQuery);
  		
		  ResultSet rs = stmt.executeQuery (userQuery);
		  if (rs.next ())
		  {
		    id = rs.getString ("ID");
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

	  // get the users bookmark_xml or return an empty bookmark channel if no user data found	
	  try
	  {
		  con = rdbmService.getConnection ();
		  Statement stmt = con.createStatement();
  		
		  String sQuery = "SELECT BOOKMARK_XML FROM PORTAL_BOOKMARKS WHERE PORTAL_USER_ID=" + id;
		  Logger.log (Logger.DEBUG, sQuery);
		  debug(sQuery);
  		
		  ResultSet rs = stmt.executeQuery (sQuery);
		  if (rs.next ())
		  {
		    String sBookmarkXml = rs.getString ("BOOKMARK_XML");
		    String xmlFilePackage = "org.jasig.portal.channels.bookmarks";
		    bookmarkXml = Xml.openDocument (xmlFilePackage, new StringReader (sBookmarkXml));
		  }
		  else {
		    //no record in database, return blank bookmarkXml
		    bookmarkXml = Xml.newDocument (xmlFilePackage, dtdFile,"bookmarks");
		  }
  				
		  stmt.close ();

	    return bookmarkXml;
	  }
	  catch (Exception e)
	  {
	    Logger.log (Logger.ERROR, e);
	  }
	  finally
	  {
	    rdbmService.releaseConnection (con);
	  }
	  return null;
  }
  
  /**
checks the session for bookmark_xml for user
if bookmark_xml exists in session
	return bookmark_xml
else
	get ID associated with User
	query portal_bookmarks table portal_bookmarks.ID = portal_users.ID
		if bookmark_xml doesn't exist return nothing
		else return user's bookmark_xml

parse bookmarkXml data and return   	 


   */
  private IXml getBookmarkXmlFromSession (HttpServletRequest req)
  {
	  HttpSession session = req.getSession (false);
	  IXml bookmarkXml = (IXml) session.getAttribute ("bookmarkXml");
	  return bookmarkXml;
  }
  
  public int getDefaultDetachHeight () {return 400;}  
  public int getDefaultDetachWidth () {return 450;}  
  public String getName () {return "My Bookmarks";}
  
  /**
   * Gets the username from the session
   * @param the servlet request object
   * @return the username
   */
  public String getUserName (HttpServletRequest req)
  {
	  HttpSession session = req.getSession (false);
	  return (String) session.getAttribute ("userName");
  }
  
  public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
	  // This channel has no help
  } 
  
  public void init (ChannelConfig chConfig) {this.chConfig = chConfig;}  
  public boolean isDetachable () {return true;}  
  public boolean isEditable () {return true;}  
  public boolean isMinimizable () {return true;}  
  public boolean isRemovable () {return true;}
  public boolean hasHelp () {return false;}
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
	  try 
	  {
	    // Retrieve bookmarkXml
   	    IBookmarks bm = getBookmarkData(req);
  	  
	    // Get Bookmarks
	    IBookmark[] bookmarks = bm.getBookmarks ();
  	  
	    out.println ("<table width=100%>");
  		
	    for (int i = 0; i < bookmarks.length; i++)
	    {
		  out.println ("  <tr bgcolor=#eeeeee>");
		  out.println ("    <td><a href=\"" + bookmarks[i].getAttribute ("url") + "\">" + bookmarks[i].getAttribute ("name") + "</a></td>");
		  out.println ("    <td>" + bookmarks[i].getAttribute ("comments") + "</td>");
		  out.println ("  </tr>");
	    }
  	  
	    out.println ("</table>");
  	  
	  }
	  catch (Exception e)
	  {
	    Logger.log (Logger.ERROR, e);
	  }
  }
  
  /**
  	Save the bookmark XML to the database
  **/
  private boolean saveBookMarkXml (HttpServletRequest req)
  {
	  // getBookMarkXML from session
	  IXml bookmarkXml = getBookmarkXmlFromSession(req);
  	
	  //Logger.log (Logger.DEBUG, "Inside saveBookMarkXML");
	  try {
		  ByteArrayOutputStream outputBookmarkXml = new ByteArrayOutputStream();
		  bookmarkXml.saveDocument(outputBookmarkXml);
  		
		  //Logger.log (Logger.DEBUG, "before saveBookMarkXmlToDatabase");
		  if (bookmarkXml != null) 
			  return saveBookMarkXmlToDatabase(req, outputBookmarkXml.toString());
	}
	catch (Exception e)
	{	
	  Logger.log (Logger.ERROR, "Problem in savBookMarkXml" + e);
	}
	return false;
  }  
  /**
	* get ID associated with User
	* attempt update of users bookmark xml
	* if attempt fails
	*	insert new record into database with users bookmark xml
   **/
  private boolean saveBookMarkXmlToDatabase (HttpServletRequest req, String bookmarkXml)
  {
	  String id = null;
	  // condsider creation of getUserID method
	  String sUserName = getUserName(req).equals(null)?"guest":getUserName(req);
	  //Logger.log (Logger.DEBUG, "before if try to save toDatabase");
	  try
	  {
		  con = rdbmService.getConnection ();
		  Statement stmt = con.createStatement();

		  String userQuery = "SELECT ID FROM PORTAL_USERS WHERE USER_NAME = '" + sUserName + "'";
		  //Logger.log (Logger.DEBUG, userQuery);
		  debug(userQuery);
  		
		  ResultSet rs = stmt.executeQuery (userQuery);
		  if (rs.next ())
		  {
		    id = rs.getString ("ID");
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

  	
	  try
	  {
		  con = rdbmService.getConnection ();
		  Statement stmt = con.createStatement();
		  String sUpdate = "UPDATE PORTAL_BOOKMARKS SET BOOKMARK_XML = '"+ bookmarkXml  + "' WHERE PORTAL_USER_ID=" + id ;
		  Logger.log (Logger.DEBUG, sUpdate);
		  debug(sUpdate);
  		
		  int goodUpdate = stmt.executeUpdate (sUpdate);
		  if (goodUpdate == 0) {
			  sUpdate = "INSERT INTO PORTAL_BOOKMARKS (PORTAL_USER_ID, BOOKMARK_XML) VALUES('"+ id +"','" + bookmarkXml + "')";
			  Logger.log (Logger.DEBUG, sUpdate);
			  goodUpdate = stmt.executeUpdate (sUpdate);	
		  }
		  // should verify that the executeUpdate was successful

		  stmt.close ();

	    return true;
	  }
	  catch (Exception e)
	  {
	    Logger.log (Logger.ERROR, e);
	  }
	  finally
	  {
	    rdbmService.releaseConnection (con);
	  }
	  return true;
  }  
} 