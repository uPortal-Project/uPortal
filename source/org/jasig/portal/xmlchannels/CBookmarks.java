package org.jasig.portal.xmlchannels;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.servlet.jsp.*;
import javax.servlet.http.*; 
import org.jasig.portal.*;
import org.apache.xalan.xslt.*;
import org.apache.xerces.dom.*;

import org.w3c.dom.*;


/**
 * A reference implementation of the Bookmarks xmlchannel.
 * The purpose of this code is to demonstrate the basic use of IXMLChannel interface.
 * <p>Bookmarks channel reads a simple XML file containing a list of bookmarks in it.
 * Depending on the request action type ("view", "edit", etc.) 
 * Bookmarks channel applies different stylesheets to different locations of the DOM structure produced by reading the bookmarks.xml file. </p>
 * <p> Note the use of a helper StylesheetSet class </p>
 * @author Peter Kharchenko
 * @version $Revision$
 */


public class CBookmarks extends GenericPortalBean implements IXMLChannel
{

    // a DOM where all the bookmark information will be contained
    protected Document bookmarksXML;

    ChannelStaticData staticData=new ChannelStaticData();
    ChannelRuntimeData runtimeData=new ChannelRuntimeData();
    
    
    // construct the URL for the location of the bookmarks.xml
    String fs=System.getProperty("file.separator");
    String uri=getPortalBaseDir () + "source" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "xmlchannels"+fs + "bookmarks.xml";

    // initialize StylesheetSet
    StylesheetSet set;
    
    // location of the stylesheet files
    String stylesheetDir=getPortalBaseDir()+"webpages"+fs+"stylesheets"+fs+"BookmarksChannel"+fs;

    // some variables to keep the state in.
    boolean editMode=false;
    boolean editBookmarkMode=false;
    boolean newBookmarkMode=false;

    // bookmark on which a current operation is being performed (such as editBookmark)
    int currentBookmark;


    public CBookmarks() {
	// initialize a stylesheet set from a file
	// take a look at the *.ssl file for the stylesheet list format. 
	// The format is a W3C-recommended default stylehseet binding
	set=new StylesheetSet(stylesheetDir+"BookmarksChannel.ssl");
	set.setMediaProps(getPortalBaseDir()+"properties"+fs+"media.properties");
    }
    

    

    
    // report static channel properties to the portal
    public ChannelSubscriptionProperties getSubscriptionProperties() {
	ChannelSubscriptionProperties csb=new ChannelSubscriptionProperties();
	// leave most properties at their default values, except a couple.
	csb.setName("Bookmarks");
	csb.setEditable(true);
	return csb;
    }

    // report runtime channel properties to the portal
    public ChannelRuntimeProperties getRuntimeProperties() {
	// channel will always render, so the default values are ok
	return new ChannelRuntimeProperties();
    }
							   

    // process Layout-level events comping from the portal
    public void receiveEvent(LayoutEvent ev) {
	if(ev.getEventNumber()==ev.EDIT_BUTTON_EVENT)
	    editMode=true;

	// or equivalently I could say
	// if(ev.getEventName().equals("editButtonEvent")) { ...
    }


    // receive ChannelStaticData from the portal
    public void setStaticData(ChannelStaticData sd) { staticData=sd;};


    // receive ChannelRuntimeData from the portal and process actions passed in it
    public void setRuntimeData(ChannelRuntimeData rd) {
	this.runtimeData=rd;
	// process actions that are passed
	// the names of these parameters are entirely up to the channel.
	// please see the eidt stylesheet for the construction of the URLs that are used to pass actions to the channel.
	// in brief, action URL is constructe by runtimeData.getBaseActionURL() + any parameters you lke
	// in this case we are parsing "runtimeData.baseActionURL() + "action=something"+"&"+...
	String action;
	if((action=runtimeData.getParameter("action"))!=null) {
	    if(action.equals("doneEditing")) { editMode=false;}
	    else if(action.equals("delete")) {
		deleteBookmark(Integer.parseInt(runtimeData.getParameter("bookmark")));
	    }
	    else if(action.equals("edit")) {
		editBookmarkMode=true;
		currentBookmark=Integer.parseInt(runtimeData.getParameter("bookmark"));
	    }
	    else if(action.equals("new")) {
		newBookmarkMode=true;
	    }
	    else if(action.equals("saveBookmark")) {
		editBookmarkMode=false;
		Element bookmark=(Element) ((getDoc()).getElementsByTagName("bookmark")).item(Integer.parseInt(runtimeData.getParameter("bookmark"))-1);
		bookmark.setAttribute("name",runtimeData.getParameter("name"));
		bookmark.setAttribute("url",runtimeData.getParameter("url"));
		bookmark.setAttribute("comments",runtimeData.getParameter("comments"));
	    }
	    else if(action.equals("addBookmark")) {
		newBookmarkMode=false;
	        Node bookmarks=((getDoc()).getElementsByTagName("bookmarks")).item(0);
		Element bookmark=getDoc().createElement("bookmark");
		bookmark.setAttribute("name",runtimeData.getParameter("name"));
		bookmark.setAttribute("url",runtimeData.getParameter("url"));
		bookmark.setAttribute("comments",runtimeData.getParameter("comments"));
		bookmarks.appendChild(bookmark);
	    }
	}
    }


    
    // output channel content to the portal
    public void renderXML(DocumentHandler out)
    {
	try {
	    if (set!=null) {
		
		// test in a order of precedence
		if(editBookmarkMode) {
		    renderEditBookmarkXML(out,currentBookmark);
		}
		else if (newBookmarkMode) {
		    renderNewBookmarkXML(out);
		}
		else if (editMode) { 
		    renderEditXML(out);
		}
		// default
		else renderViewXML(out);
	    }
	} catch (Exception e) {Logger.log(Logger.ERROR,e); }
    
    }

    // the rest are private helper functions, should be rather self-explanatory

    private void renderViewXML(DocumentHandler out) throws org.xml.sax.SAXException {

	// a block, typical for the IXMLChannel:
	//  - use the StylesheetSet to get an appropriate stylesheet
	//  - instansiation an XSLT processor
	//  - fire up the transformation

	XSLTInputSource stylesheet=set.getStylesheet("view",runtimeData.getHttpRequest());
	if(stylesheet!=null) {
	    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
	    processor.process(new XSLTInputSource(getDoc()),stylesheet,new XSLTResultTarget(out));
	} else Logger.log(Logger.ERROR,"BookmarksChannel::renderViewXML() : unable to find a stylesheet for rendering");
    }
    
    private void renderEditXML(DocumentHandler out) throws org.xml.sax.SAXException {
	XSLTInputSource stylesheet=set.getStylesheet("edit",runtimeData.getHttpRequest());
	
	if(stylesheet!=null) {
	    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
	    processor.setStylesheetParam("baseActionURL",processor.createXString(runtimeData.getBaseActionURL()));
	    processor.process(new XSLTInputSource(getDoc()),stylesheet,new XSLTResultTarget(out));
	} else Logger.log(Logger.ERROR,"BookmarksChannel::renderEditXML() : unable to find a stylesheet for rendering");
    }
    

    private void renderEditBookmarkXML(DocumentHandler out,int bookmarkNumber) throws org.xml.sax.SAXException {
	Node bookmark=((getDoc()).getElementsByTagName("bookmark")).item(bookmarkNumber-1);
	XSLTInputSource stylesheet=set.getStylesheet("editbookmark",runtimeData.getHttpRequest());
	
	if(stylesheet!=null) {
	    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
	    processor.setStylesheetParam("channelID",processor.createXString(staticData.getChannelID()));
	    processor.setStylesheetParam("bookmarkID",processor.createXString(String.valueOf(bookmarkNumber)));
	    processor.process(new XSLTInputSource(bookmark),stylesheet,new XSLTResultTarget(out));
	} else Logger.log(Logger.ERROR,"BookmarksChannel::renderEditBookmarkXML() : unable to find a stylesheet for rendering");
    }
    
    private void renderNewBookmarkXML(DocumentHandler out) throws org.xml.sax.SAXException {
	// this is interesting since there's is no real content being presented ... 
	// we know the expected structure of the information, but we don't have any data yet.
	// for now, the best thing I can think of is creating an empty template, i.e. an empty
	// bookmark and feeding it to the XSLT.
	//	Document doc=new DocumentImpl();
	Element bookmark=getDoc().createElement("bookmark");
	bookmark.setAttribute("name","");
	bookmark.setAttribute("url","");
	bookmark.setAttribute("comments","");
	
	XSLTInputSource stylesheet=set.getStylesheet("editbookmark",runtimeData.getHttpRequest());
	
	if(stylesheet!=null) {
	    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
	    processor.setStylesheetParam("channelID",processor.createXString(staticData.getChannelID()));
	    processor.setStylesheetParam("newBookmark",processor.createXString("true"));
	    processor.process(new XSLTInputSource(bookmark),stylesheet,new XSLTResultTarget(out));
	} else Logger.log(Logger.ERROR,"BookmarksChannel::renderEditBookmarkXML() : unable to find a stylesheet for rendering");
	

    }

    
    private Document getDoc() {
	try{
	    if(bookmarksXML==null) {
		org.apache.xerces.parsers.DOMParser parser=new org.apache.xerces.parsers.DOMParser();
		parser.parse(new org.xml.sax.InputSource(uri));
		bookmarksXML=parser.getDocument();
	    }
	} catch (Exception e) { Logger.log(Logger.ERROR,"BookmarksChannel::getDoc() : unable to get bookmarks from \""+uri+"\"\n"+e); }
	return bookmarksXML;
    }

    private void deleteBookmark(int bookmarkNumber) {
	Document root=getDoc();
	NodeList elements=root.getElementsByTagName("bookmark");
	Node bookmark=elements.item(bookmarkNumber-1);
	if(bookmark!=null) {
	    (bookmark.getParentNode()).removeChild(bookmark);
	} else Logger.log(Logger.ERROR,"BookmarksChannel::deleteBookmark() : attempting to remove nonexistent bookmark #"+bookmarkNumber);
    }
    
}
