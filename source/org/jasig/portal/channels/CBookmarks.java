package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import org.jasig.portal.*;
import org.jasig.portal.channels.bookmarks.*;
import com.objectspace.xml.*;

/**
 * This is a channel for storing user-defined bookmarks.
 * 
 * @author Ken Weiner
 */
public class CBookmarks extends GenericPortalBean implements org.jasig.portal.IChannel                         
{ 
  // This should come from a database
  private File xmlFile = new File (getPortalBaseDir () + "source" + File.separator + "org" + File.separator + "jasig" + File.separator + "portal" + File.separator + "channels" + File.separator + "bookmarks" + File.separator + "bookmarks.xml");
  private String xmlFilePackage = "org.jasig.portal.channels.bookmarks";
  
  public String getName () {return "My Bookmarks";}
  public void initParams (Hashtable params) {};
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return true;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return true;}
  public boolean hasHelp () {return false;}  
  
  public int getDefaultDetachWidth () {return 450;}
  public int getDefaultDetachHeight () {return 400;}
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      // Open xml file -- this should eventually be retrieved from a database
      IXml xml = Xml.openDocument (xmlFilePackage, xmlFile);
      IBookmarks bm = (IBookmarks) xml.getRoot ();
      
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
      e.printStackTrace ();
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
      e.printStackTrace ();
    }
  }
    
  protected void doListBookmarks (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      // Open xml file -- this should eventually be retrieved from a database
      IXml xml = Xml.openDocument (xmlFilePackage, xmlFile);
      IBookmarks bm = (IBookmarks) xml.getRoot ();
        
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
        out.println ("    <td>[<a href=\"dispatch.jsp?method=edit&action=edit&bookmark=" + i + "\">edit</a>][<a href=\"dispatch.jsp?method=edit&action=delete&bookmark=" + i + "\">delete</a>]</td>");
        out.println ("  </tr>");
      }
      out.println ("</table>");
      out.println ("<table border=0><tr><form>");
      out.println ("<td><br><input type=button name=add value=\"Add Bookmark\" onClick=\"location=\'dispatch.jsp?method=edit&action=add\'\"></td>");
      out.println ("<td><br><input type=button name=finished value=\"Finished\" onClick=\"location=\'dispatch.jsp?method=edit&action=done\'\"></td>");
      out.println ("</form></tr></table>");
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }
  
  protected void doEditBookmark (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      int i = Integer.parseInt (req.getParameter("bookmark"));
        
      // Open xml file -- this should eventually be retrieved from a database
      IXml xml = Xml.openDocument (xmlFilePackage, xmlFile);
      IBookmarks bm = (IBookmarks) xml.getRoot ();
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
      out.println ("</form>");
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }
  
  protected void doDeleteBookmark (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      int i = Integer.parseInt(req.getParameter("bookmark"));
        
      // Open xml file -- this should eventually be retrieved from a database
      IXml xml = Xml.openDocument (xmlFilePackage, xmlFile);
      IBookmarks bm = (IBookmarks) xml.getRoot ();
      bm.removeBookmarkAt(i);
      
      // Write this to a database
      xml.saveDocument (xmlFile);
      
      res.sendRedirect ("dispatch.jsp?method=edit");
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }
  
  protected void doAddBookmark (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      // Open xml file -- this should eventually be retrieved from a database
      IXml xml = Xml.openDocument (xmlFilePackage, xmlFile);
      IBookmarks bm = (IBookmarks) xml.getRoot ();
        
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
      out.println ("<input type=hidden name=method value=\"edit\">");
      out.println ("<input type=hidden name=action value=\"save\">");
      out.println ("<input type=submit name=submit value=\"Save\">");
      out.println ("</form>");        
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }

  protected void doSaveBookmark (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    int i = 0;
    
    try
    {        
      // Open xml file -- this should eventually be retrieved from a database
      IXml xml = Xml.openDocument (xmlFilePackage, xmlFile);
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
      xml.saveDocument (xmlFile);
      res.sendRedirect ("dispatch.jsp?method=edit");
    }
    catch (Exception e)
    {
      e.printStackTrace ();
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
      e.printStackTrace ();
    }
  }  
  
  public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel has no help
  }
}