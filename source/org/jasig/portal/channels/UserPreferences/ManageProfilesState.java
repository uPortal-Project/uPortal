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

package org.jasig.portal.channels.UserPreferences;

import org.jasig.portal.*;
import org.xml.sax.DocumentHandler;
import java.util.*;
import javax.servlet.http.*;
import org.w3c.dom.*;
import org.apache.xalan.xslt.*;

/** <p>CUserPreferences state for managing profiles</p>
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */


class ManageProfilesState extends BaseState {
    protected IUserPreferencesDB updb;
    protected Hashtable userProfileList;
    protected Hashtable systemProfileList;
    protected ChannelRuntimeData runtimeData;
    
    public ManageProfilesState(CUserPreferences context) {
	super(context);
    }
    
    public void setPortalControlStructures(PortalControlStructures pcs) {
	UserLayoutManager ulm=pcs.getUserLayoutManager();
	if(updb==null) updb=new UserPreferencesDBImpl();
	if(updb!=null) {
	    if(userProfileList==null)
		userProfileList=updb.getUserProfileList(ulm.getPerson().getID());
	    if(systemProfileList==null)
		    systemProfileList=updb.getSystemProfileList();
	}
    }
    
    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
	this.runtimeData=rd;
	// local action processing
	String action=runtimeData.getParameter("action");
	if(action!=null) {
	    if(action.equals("edit")) {
		String profileName=runtimeData.getParameter("profileName");
		String profileType=runtimeData.getParameter("profileType");
		boolean systemProfile=false;
		if(profileType.equals("system")) systemProfile=true;
		
		// initialize internal edit state
		
	    }
	    
	}
	
    }
    
    public void renderXML(DocumentHandler out) throws PortalException {
	
	if(updb==null) updb=new UserPreferencesDBImpl();
	if(updb==null) throw new ResourceMissingException("","User preference database","Unable to obtain the list of user profiles, since the user preference database is currently down");
	    if((userProfileList==null)||(systemProfileList==null)) {
		if(userProfileList==null)
		    userProfileList=updb.getUserProfileList(context.getUserLayoutManager().getPerson().getID());
		if(systemProfileList==null)
		    systemProfileList=updb.getSystemProfileList();
	    }
	    
	    Document doc = new org.apache.xerces.dom.DocumentImpl();
	    Element edEl=doc.createElement("profiles");
	    doc.appendChild(edEl);
	    // fill out user-defined profiles
	    Element uEl=doc.createElement("user");
	    for(Enumeration upe=userProfileList.elements(); upe.hasMoreElements(); ) {
		UserProfile p=(UserProfile) upe.nextElement();
		Element pEl=doc.createElement("profile");
		pEl.setAttribute("name",p.getProfileName());
		Element dEl=doc.createElement("description");
		dEl.appendChild(doc.createTextNode(p.getProfileDescription()));
		pEl.appendChild(dEl);
		uEl.appendChild(pEl);
	    }
	    edEl.appendChild(uEl);
	    // fill out system-defined profiles
	    Element sEl=doc.createElement("system");
	    for(Enumeration spe=systemProfileList.elements(); spe.hasMoreElements(); ) {
		UserProfile p=(UserProfile) spe.nextElement();
		Element pEl=doc.createElement("profile");
		pEl.setAttribute("name",p.getProfileName());
		Element dEl=doc.createElement("description");
		dEl.appendChild(doc.createTextNode(p.getProfileDescription()));
		pEl.appendChild(dEl);
		sEl.appendChild(pEl);
	    }
	    edEl.appendChild(sEl);
	    
	    // find the stylesheet and transform
	    StylesheetSet set=context.getStylesheetSet();
	    if(set==null) 
		throw new GeneralRenderingException("Unable to determine the stylesheet list");
	    String xslURI=set.getStylesheetURI("profileList", runtimeData.getHttpRequest()); 	    
	    
	    XSLTResultTarget xmlResult = new XSLTResultTarget(out);

	    UserProfile currentProfile=context.getCurrentUserPreferences().getProfile();

	    Hashtable params=new Hashtable();

	    params.put("baseActionURL", runtimeData.getBaseActionURL());        
	    params.put("profileName", currentProfile.getProfileName());
	    if(currentProfile.isSystemProfile()) params.put("profileType","system");
	    else params.put("profileType","user");
	    
	    if (xslURI != null) {
		try {
		    org.jasig.portal.utils.XSLT.transform(xmlResult,doc,xslURI,params);
		} catch (org.xml.sax.SAXException e) {
		    throw new GeneralRenderingException("Unable to complete transformation");
		} catch (java.io.IOException i) {
		    throw new GeneralRenderingException("IOException has been encountered");
		}
	    } else throw new ResourceMissingException("","stylesheet","Unable to find stylesheet to display content for this media");
    }
}
