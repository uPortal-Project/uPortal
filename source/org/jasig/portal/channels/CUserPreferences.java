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

package org.jasig.portal.channels;

import org.jasig.portal.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.w3c.dom.*;
import org.apache.xalan.xslt.*;
import org.xml.sax.DocumentHandler;
import java.io.*;
import java.util.*;

/** <p>Manages User Layout, user preferences and profiles </p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */
public class CUserPreferences implements ISpecialChannel
{
    
    UserLayoutManager ulm;
    
    ChannelStaticData staticData = null;
    ChannelRuntimeData runtimeData = null;
    StylesheetSet set = null;
    
    private static final String fs = File.separator;
    private static final String portalBaseDir = GenericPortalBean.getPortalBaseDir ();
    String stylesheetDir = portalBaseDir + fs + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CUserPreferences";
    
    // Various modes
    private static final int BROWSE = 0;
    private static final int EDITLAYOUTITEM = 1;
    private static final int ATTRIBUTEHELP = 2;
    private static final int EDITGPREF = 3;
    private static final int MOVE = 4;
    
    private int mode = BROWSE;

    private UserPreferences up=null;
    private Document userLayoutXML = null;
    private Document inputDoc;
    private String action = null;
    private String folderID = null; 

    ThemeStylesheetDescription tsd=null;
    StructureStylesheetDescription ssd=null;

    private String editElementID = null;
    private String[] moveIDs = null; // contains the IDs of channels/folders to be moved
    private boolean modified = false; // becomes true after user makes changes to layout
    
    private static final String layoutID = "top"; // just a way to refer to the layout element since it doesn't have an ID attribute
    
    /** Constructs a CUserPrefrences.
     */
    public CUserPreferences ()
    {
	this.staticData = new ChannelStaticData ();
	this.runtimeData = new ChannelRuntimeData ();
	this.set = new StylesheetSet (stylesheetDir + fs + "CUserPreferences.ssl");
	this.set.setMediaProps (portalBaseDir + fs + "properties" + fs + "media.properties");
    }
    
    
    public void setPortalControlStructures(PortalControlStructures pcs) {
	ulm=pcs.getUserLayoutManager();
    }
    
    
    /** Returns static channel properties to the portal
     * @return handle to subscription properties
     */
    public ChannelSubscriptionProperties getSubscriptionProperties ()
    {
	ChannelSubscriptionProperties csb = new ChannelSubscriptionProperties ();
	
	// Properties which are not specifically set here will assume default
	// values as determined by ChannelSubscriptionProperties
	csb.setName ("User Preferences");
	return csb;
    }
    
    /** Returns channel runtime properties
     * @return handle to runtime properties
     */
    public ChannelRuntimeProperties getRuntimeProperties ()
    {
	// Channel will always render, so the default values are ok
	return new ChannelRuntimeProperties ();
    }
    
    /** Processes layout-level events coming from the portal
     * @param ev a portal layout event
     */
    public void receiveEvent (LayoutEvent ev)
    {
	// no events for this channel
    }
    
    /** Receive static channel data from the portal
     * @param sd static channel data
     */
    public void setStaticData (ChannelStaticData sd)
    { 
	this.staticData = sd;
    }
    
    /** Receives channel runtime data from the portal and processes actions 
     * passed to it.  The names of these parameters are entirely up to the channel. 
     * @param rd handle to channel runtime data
     */
    public void setRuntimeData (ChannelRuntimeData rd)
    {
	this.runtimeData = rd;    
	
	if(userLayoutXML==null) {
	    userLayoutXML=ulm.getUserLayoutCopy();
	}
	
	if(up==null) {
	    up=ulm.getUserPreferencesCopy();
	    ICoreStylesheetDescriptionDB csddb=new CoreStylesheetDescriptionDBImpl();
	    StructureStylesheetUserPreferences fsup=up.getStructureStylesheetUserPreferences();	
	    ssd=csddb.getStructureStylesheetDescription(fsup.getStylesheetName());
	    if(ssd==null) {
		Logger.log(Logger.ERROR,"CUserPreferences::setRuntimeData() : structure stylesheet description for a stylesheet \""+fsup.getStylesheetName()+"\" is null");
	    }
	    
	    ThemeStylesheetUserPreferences ssup=up.getThemeStylesheetUserPreferences();
	    tsd=csddb.getThemeStylesheetDescription(ssup.getStylesheetName());
	    if(tsd==null) {
		Logger.log(Logger.ERROR,"CUserPreferences::setRuntimeData() : theme stylesheet description for a stylesheet \""+fsup.getStylesheetName()+"\" is null");
	    }

	}
	
	action = runtimeData.getParameter ("action");
	
	if(mode==BROWSE || mode==MOVE ) {	
	    if (action != null) {
		if (action.equals ("browse"))
		    prepareBrowse ();
		else if (action.equals ("move"))
		    prepareMove ();
		else if (action.equals ("moveTo"))
		    prepareMoveTo ();
		else if (action.equals ("reorder"))
		    prepareReorder ();
		else if (action.equals ("saveChanges"))
		    prepareSaveChanges ();
		else if (action.equals ("editElement"))
		    prepareEditElement();
		else if(action.equals("submitEditChoice")) {
		    String manageTarget=runtimeData.getParameter("manageTarget");
		    if(manageTarget.equals("layout")) {
			folderID="top";
			mode=BROWSE;
		    } else if(manageTarget.equals("gpref")) {
			mode=EDITGPREF;
		    }
		}
	    }
	} else if (mode==EDITLAYOUTITEM) {
	    if (action != null) {
		if (action.equals ("browse"))
		    prepareBrowse ();
		else if (action.equals ("submitEditValues")) {
		    String submit=runtimeData.getParameter("submit");
		    if(submit.equals("Cancel"))
			mode=BROWSE;
		    if(submit.equals("Save"))
			prepareSaveEditedItem();
		}
	    }
	} else if (mode==EDITGPREF) {
	    Logger.log(Logger.DEBUG,"mode=EDITGPREF, action="+action+" submit="+runtimeData.getParameter("submit")+".");
	    if (action != null) {
		if (action.equals ("browse"))
		    prepareBrowse ();
		else if (action.equals ("submitEditValues")) {
		    String submit=runtimeData.getParameter("submit");
		    if(submit.equals("Cancel"))
			mode=BROWSE;
		    if(submit.equals("Save"))
			prepareSaveEditGPrefs();
		} else if(action.equals("submitEditChoice")) {
		    String manageTarget=runtimeData.getParameter("manageTarget");
		    if(manageTarget.equals("layout")) {
			folderID="top";
			mode=BROWSE;
		    } else if(manageTarget.equals("gpref")) {
			mode=EDITGPREF;
		    }
		}
		
	    }
	}
        
	if (folderID == null)
	    folderID = this.layoutID;
    }
    
    /** Output channel content to the portal
     * @param out a sax document handler
   */
    public void renderXML (DocumentHandler out)  {
	try {
	    switch (mode) {
	    case BROWSE:
		processBrowseXML (out);
		break;
	    case MOVE:
		processMoveXML (out);
		break;
	    case EDITLAYOUTITEM:
		processEditElementXML(out);
		break;
	    case EDITGPREF:
		processEditGPrefXML(out);
		break;
	    default:
		processBrowseXML (out);
		break;
	    }
	} 
	catch (Exception e) {
	    Logger.log (Logger.ERROR, e); 
	}
    }
    
    private void processBrowseXML (DocumentHandler out) throws org.xml.sax.SAXException {
	XSLTInputSource xmlSource = new XSLTInputSource (userLayoutXML);
	XSLTInputSource xslSource = set.getStylesheet("browse", runtimeData.getHttpRequest());
	XSLTResultTarget xmlResult = new XSLTResultTarget(out);
	
	if (xslSource != null) {
	    XSLTProcessor processor = XSLTProcessorFactory.getProcessor (new org.apache.xalan.xpath.xdom.XercesLiaison ());
	    processor.setStylesheetParam("baseActionURL", processor.createXString (runtimeData.getBaseActionURL()));        
	    processor.setStylesheetParam("folderID", processor.createXString (folderID));
	    processor.setStylesheetParam("modified", processor.createXBoolean (modified));        
	    processor.setStylesheetParam("profileName", processor.createXString (up.getMedia()));        
	    processor.process (xmlSource, xslSource, xmlResult);
	}
	else 
      Logger.log(Logger.ERROR, "org.jasig.portal.channels.CUserPreferences: unable to find a stylesheet for rendering");
    }

    private void processMoveXML (DocumentHandler out) throws org.xml.sax.SAXException {
	XSLTInputSource xmlSource = new XSLTInputSource (userLayoutXML);
	XSLTInputSource xslSource = set.getStylesheet("moveTo", runtimeData.getHttpRequest());
	XSLTResultTarget xmlResult = new XSLTResultTarget(out);
	
	if (xslSource != null) {
	    XSLTProcessor processor = XSLTProcessorFactory.getProcessor (new org.apache.xalan.xpath.xdom.XercesLiaison ());
	    processor.setStylesheetParam("baseActionURL", processor.createXString (runtimeData.getBaseActionURL()));        
	    processor.setStylesheetParam("folderID", processor.createXString (folderID));  
	    processor.setStylesheetParam("modified", processor.createXBoolean (modified));        
	    processor.process (xmlSource, xslSource, xmlResult);
	}
	else 
      Logger.log(Logger.ERROR, "org.jasig.portal.channels.CUserPreferences: unable to find a stylesheet for rendering");
    }
    

    private void prepareEditElement() {

	mode = EDITLAYOUTITEM;
	editElementID=runtimeData.getParameter("folderID");
    }

    private void prepareEditGPrefs() {
	mode = EDITGPREF;
    }

    private void processEditGPrefXML(DocumentHandler out) throws org.xml.sax.SAXException {
	// construct gpref XML
	Document doc = new org.apache.xerces.dom.DocumentImpl();
	Element edEl=doc.createElement("gpref");

	StructureStylesheetUserPreferences ssup=up.getStructureStylesheetUserPreferences();
	Element spEl=doc.createElement("structureparameters");
	for(Enumeration e=ssd.getStylesheetParameterNames(); e.hasMoreElements(); ) {
	    Element atEl=doc.createElement("parameter");
	    Element atNameEl=doc.createElement("name");
	    String atName=(String) e.nextElement();
	    atNameEl.appendChild(doc.createTextNode(atName));
	    atEl.appendChild(atNameEl);
	    
	    Element valueEl=doc.createElement("value");
	    String value=ssup.getParameterValue(atName);
	    if(value==null) {
		// set the default value
		value=ssd.getStylesheetParameterDefaultValue(atName);
	    }
	    
	    valueEl.appendChild(doc.createTextNode(value));
	    atEl.appendChild(valueEl);
	    Element descrEl=doc.createElement("description");
	    descrEl.appendChild(doc.createTextNode(ssd.getStylesheetParameterWordDescription(atName)));
	    atEl.appendChild(descrEl);
	    
	    
	    
	    spEl.appendChild(atEl);
	}
	edEl.appendChild(spEl);
	
	ThemeStylesheetUserPreferences tsup=up.getThemeStylesheetUserPreferences();
	Element tpEl=doc.createElement("themeparameters");
	for(Enumeration e=tsd.getStylesheetParameterNames(); e.hasMoreElements(); ) {
	    Element atEl=doc.createElement("parameter");
	    Element atNameEl=doc.createElement("name");
	    String atName=(String) e.nextElement();
	    atNameEl.appendChild(doc.createTextNode(atName));
	    atEl.appendChild(atNameEl);
	    
	    Element valueEl=doc.createElement("value");
	    String value=tsup.getParameterValue(atName);
	    if(value==null) {
		// set the default value
		value=tsd.getStylesheetParameterDefaultValue(atName);
	    }
	    
	    valueEl.appendChild(doc.createTextNode(value));
	    atEl.appendChild(valueEl);
	    Element descrEl=doc.createElement("description");
	    descrEl.appendChild(doc.createTextNode(tsd.getStylesheetParameterWordDescription(atName)));
	    atEl.appendChild(descrEl);
	    
	    
	    
	    tpEl.appendChild(atEl);
	}
	edEl.appendChild(tpEl);


	doc.appendChild(edEl);

	try {
	    StringWriter outString = new StringWriter ();
	    org.apache.xml.serialize.OutputFormat format=new org.apache.xml.serialize.OutputFormat();
	    format.setOmitXMLDeclaration(true);
	    format.setIndenting(true);
	    org.apache.xml.serialize.XMLSerializer xsl = new org.apache.xml.serialize.XMLSerializer (outString,format);
	    xsl.serialize (doc);
	    Logger.log(Logger.DEBUG,outString.toString());
	} catch (Exception e) {
	    Logger.log(Logger.DEBUG,e);
	}

	XSLTInputSource xmlSource = new XSLTInputSource (doc);
	XSLTInputSource xslSource = set.getStylesheet("editGPrefs", runtimeData.getHttpRequest());
	if(xslSource==null) {
	    Logger.log(Logger.ERROR,"CUserPreferences::processEditGPrefXML() : unable to locate editGPrefs stylesheet");
	}
	XSLTResultTarget xmlResult = new XSLTResultTarget(out);
	
	
	XSLTProcessor processor = XSLTProcessorFactory.getProcessor (new org.apache.xalan.xpath.xdom.XercesLiaison ());
	processor.setStylesheetParam("baseActionURL", processor.createXString (runtimeData.getBaseActionURL()));        
	processor.setStylesheetParam("profileName", processor.createXString (up.getMedia()));        
	processor.process (xmlSource, xslSource, xmlResult);

    }

    private void processEditElementXML(DocumentHandler out) throws org.xml.sax.SAXException {

	// find the element to be edited
	//	Logger.log(Logger.DEBUG,"CUserPreferences::processEditElementXML() : editElementID = \""+editElementID+"\".");
	Element target=userLayoutXML.getElementById(editElementID);
	String elType=target.getTagName();

	// construct the descriptive XML
	Document doc = new org.apache.xerces.dom.DocumentImpl();
	Element edEl=doc.createElement("editelement");
	Element typeEl=doc.createElement("type");
	
	if(elType.equals("folder"))
	    typeEl.appendChild(doc.createTextNode("folder"));
	else
	    typeEl.appendChild(doc.createTextNode("channel"));

	edEl.appendChild(typeEl);

	Element nameEl=doc.createElement("name");
	nameEl.appendChild(doc.createTextNode(target.getAttribute("name")));
	edEl.appendChild(nameEl);
	
	// determine element type


	if(elType.equals("folder")) {
	    // target is a folder
	    StructureStylesheetUserPreferences ssup=up.getStructureStylesheetUserPreferences();
	    Element saEl=doc.createElement("structureattributes");
	    List al=ssup.getFolderAttributeNames();
	    for(int i=0;i<al.size();i++) {
		Element atEl=doc.createElement("attribute");
		Element atNameEl=doc.createElement("name");
		String atName=(String) al.get(i);
		atNameEl.appendChild(doc.createTextNode(atName));
		atEl.appendChild(atNameEl);
		
		Element valueEl=doc.createElement("value");
		String value=ssup.getFolderAttributeValue(editElementID,atName);
		if(value==null) {
		    // set the default value
		    value=ssd.getFolderAttributeDefaultValue(atName);
		}

		valueEl.appendChild(doc.createTextNode(value));
		atEl.appendChild(valueEl);
		Element descrEl=doc.createElement("description");
		descrEl.appendChild(doc.createTextNode(ssd.getFolderAttributeWordDescription(atName)));
		atEl.appendChild(descrEl);


		
		saEl.appendChild(atEl);
	    }
	    edEl.appendChild(saEl);

	    
	} else if(elType.equals("channel")) {
	    // target is a channel
	    StructureStylesheetUserPreferences ssup=up.getStructureStylesheetUserPreferences();
	    Element saEl=doc.createElement("structureattributes");
	    List al=ssup.getChannelAttributeNames();
	    for(int i=0;i<al.size();i++) {
		Element atEl=doc.createElement("attribute");
		Element atNameEl=doc.createElement("name");
		String atName=(String) al.get(i);
		atNameEl.appendChild(doc.createTextNode(atName));
		atEl.appendChild(atNameEl);
		
		Element valueEl=doc.createElement("value");
		String value=ssup.getChannelAttributeValue(editElementID,atName);
		if(value==null) {
		    value=ssd.getChannelAttributeDefaultValue(atName);
		}

		valueEl.appendChild(doc.createTextNode(value));
		atEl.appendChild(valueEl);
		
		Element descrEl=doc.createElement("description");
		descrEl.appendChild(doc.createTextNode(ssd.getChannelAttributeWordDescription(atName)));
		atEl.appendChild(descrEl);

		saEl.appendChild(atEl);
	    }
	    edEl.appendChild(saEl);

	    ThemeStylesheetUserPreferences tsup=up.getThemeStylesheetUserPreferences();
	    Element taEl=doc.createElement("themeattributes");
	    al=tsup.getChannelAttributeNames();
	    for(int i=0;i<al.size();i++) {
		Element atEl=doc.createElement("attribute");
		Element atNameEl=doc.createElement("name");
		String atName=(String) al.get(i);
		atNameEl.appendChild(doc.createTextNode(atName));
		atEl.appendChild(atNameEl);
		
		Element valueEl=doc.createElement("value");

		String value=tsup.getChannelAttributeValue(editElementID,atName);
		if(value==null) {
		    value=tsd.getChannelAttributeDefaultValue(atName);
		}

		valueEl.appendChild(doc.createTextNode(value));
		atEl.appendChild(valueEl);
		
		Element descrEl=doc.createElement("description");
		descrEl.appendChild(doc.createTextNode(tsd.getChannelAttributeWordDescription(atName)));
		atEl.appendChild(descrEl);

		taEl.appendChild(atEl);
	    }
	    edEl.appendChild(taEl);
	    

	}


	doc.appendChild(edEl);


	// debug printout of the prepared xml
	try {
	StringWriter outString = new StringWriter ();
        org.apache.xml.serialize.OutputFormat format=new org.apache.xml.serialize.OutputFormat();
        format.setOmitXMLDeclaration(true);
        format.setIndenting(true);
        org.apache.xml.serialize.XMLSerializer xsl = new org.apache.xml.serialize.XMLSerializer (outString,format);
        xsl.serialize (doc);
        Logger.log(Logger.DEBUG,outString.toString());
	} catch (Exception e) {
	    Logger.log(Logger.DEBUG,e);
	}

	XSLTInputSource xmlSource = new XSLTInputSource (doc);
	XSLTInputSource xslSource = set.getStylesheet("editItem", runtimeData.getHttpRequest());
	if(xslSource==null) {
	    Logger.log(Logger.ERROR,"CUserPreferences::processEditElementXML() : unable to locate editItem stylesheet");
	}
	XSLTResultTarget xmlResult = new XSLTResultTarget(out);
	
	
	XSLTProcessor processor = XSLTProcessorFactory.getProcessor (new org.apache.xalan.xpath.xdom.XercesLiaison ());
	processor.setStylesheetParam("baseActionURL", processor.createXString (runtimeData.getBaseActionURL()));
	processor.process (xmlSource, xslSource, xmlResult);

    }

    private void prepareSaveEditGPrefs() {
	StructureStylesheetUserPreferences ssup=up.getStructureStylesheetUserPreferences();
	for(Enumeration e=ssd.getStylesheetParameterNames(); e.hasMoreElements(); ) {
	    String atName=(String) e.nextElement();
	    String value=runtimeData.getParameter(atName);
	    if(value.equals(ssd.getStylesheetParameterDefaultValue(atName))) {
		ssup.deleteParameter(atName);
	    } else 
		ssup.putParameterValue(atName,value);
	}	

	ThemeStylesheetUserPreferences tsup=up.getThemeStylesheetUserPreferences();

	for(Enumeration e=tsd.getStylesheetParameterNames(); e.hasMoreElements(); ) {
	    String atName=(String) e.nextElement();
	    String value=runtimeData.getParameter(atName);
	    if(value.equals(tsd.getStylesheetParameterDefaultValue(atName))) {
		tsup.deleteParameter(atName);
	    } else 
		tsup.putParameterValue(atName,value);
	}	

	modified=true;
	// get back to browse mode
	mode=BROWSE;

    }

    private void prepareSaveEditedItem() {
	Element target=userLayoutXML.getElementById(editElementID);
	String elType=target.getTagName();

	// reset the name
	target.setAttribute("name",runtimeData.getParameter("name"));
	


	if(elType.equals("folder")) {
	    // target is a folder
	    StructureStylesheetUserPreferences ssup=up.getStructureStylesheetUserPreferences();
	    List al=ssup.getFolderAttributeNames();
	    for(int i=0;i<al.size();i++) {
		String atName=(String) al.get(i);
		String atValue=runtimeData.getParameter(atName);
		if(atValue.equals(ssd.getFolderAttributeDefaultValue(atName)))
		    atValue=null;
		ssup.setFolderAttributeValue(editElementID,atName,atValue);
	    }
	} else if(elType.equals("channel")) {
	    // target is a channel
	    StructureStylesheetUserPreferences ssup=up.getStructureStylesheetUserPreferences();
	    List al=ssup.getChannelAttributeNames();
	    for(int i=0;i<al.size();i++) {
		String atName=(String) al.get(i);
		String atValue=runtimeData.getParameter(atName);
		if(atValue.equals(ssd.getChannelAttributeDefaultValue(atName)))
		    atValue=null;
		ssup.setChannelAttributeValue(editElementID,atName,atValue);
	    }

	    ThemeStylesheetUserPreferences tsup=up.getThemeStylesheetUserPreferences();
	    al=tsup.getChannelAttributeNames();
	    for(int i=0;i<al.size();i++) {
		String atName=(String) al.get(i);
		String atValue=runtimeData.getParameter(atName);
		if(atValue.equals(tsd.getChannelAttributeDefaultValue(atName)))
		    atValue=null;
		tsup.setChannelAttributeValue(editElementID,atName,atValue);
	    }
	}

	modified=true;
	// get back to browse mode
	mode=BROWSE;
    }


    private void prepareBrowse () {
	mode = BROWSE;
	String runtimeFolderID = runtimeData.getParameter ("folderID");
	
	if (runtimeFolderID != null)
	    folderID = runtimeFolderID;
    }
    
    private void prepareMove () {
	mode = MOVE;
	// getParameterValues() should be a method in ChannelRuntimeData.
	// For now, I'll use the request object -- ask Peter about this!
	HttpServletRequest req = runtimeData.getHttpRequest ();
	moveIDs = req.getParameterValues ("move");    
    }
    
    private void prepareMoveTo () {
	mode = BROWSE;
	String destinationID = runtimeData.getParameter ("destination");
	Node destination = null;
	
	if (destinationID == null) {
	    Logger.log(Logger.ERROR,"CUserPreferences::prepareMove() : received a null destinationID !");
	} else {
	    if (destinationID.equals (this.layoutID))
		destination = userLayoutXML.getDocumentElement (); // the layout element
	    else
		destination = userLayoutXML.getElementById (destinationID);
	    
	    if(destination==null) {
		Logger.log(Logger.ERROR,"CUserPreferences::prepareMove() : destinationID=\""+destinationID+"\" results in an empty node !"); 
	    } else {
		for (int i = 0; i < moveIDs.length; i++) {
		    Node relocating = userLayoutXML.getElementById (moveIDs[i]);
		    destination.insertBefore (relocating, null); // adds to end of children nodes
		}
		modified = true;
	    }
	}
    }
    
  private void prepareReorder () {
      mode = BROWSE;
      String folderID = runtimeData.getParameter ("elementID"); // the folder or channel ID
      String direction = runtimeData.getParameter ("dir"); // "up" or "down"
   
      Node element = userLayoutXML.getElementById (folderID);
      Node parent = element.getParentNode ();
    
      if (direction.equals ("up")) {
	  Node prev;
	  // Goto the previous channel or folder element
	  for (prev = element.getPreviousSibling (); prev != null && prev.getNodeType () != Node.ELEMENT_NODE && (!prev.getNodeName ().equals ("channel") || !prev.getNodeName ().equals ("folder")); prev = prev.getPreviousSibling ());
	  parent.insertBefore (element, prev);
      } else if (direction.equals ("down")) {
	  Node next;
	  // Goto the next channel or folder element
	  for (next = element.getNextSibling (); next != null && next.getNodeType () != Node.ELEMENT_NODE && (!next.getNodeName ().equals ("channel") || !next.getNodeName ().equals ("folder")); next = next.getNextSibling ());
	  parent.insertBefore (next, element);    
      }
      
      modified = true;
  }
    
    private void prepareSaveChanges () {
	// write code to persist the userLayoutXML to the session
	// and the database (remember, as the user interacts with this
	// channel, changes are only made to a copy of the userLayoutXML
	// until this method is called)
	
	folderID = this.layoutID;
	modified = false;
	ulm.setNewUserLayoutAndUserPreferences(userLayoutXML,up);
    }
}
