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
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import org.apache.xalan.xpath.*;
import org.apache.xalan.xslt.*;
import org.apache.xml.serialize.*;

/**
 * Wraps an IXMLChannel into an IChannel interface,
 * serving as a middleman in all further interactions.
 * @version $Revision$
 * @author Peter Kharchenko
 */
public class XMLChannelWrapper implements IChannel
{
    private IXMLChannel ch;
    private String chanID;

    public XMLChannelWrapper(IXMLChannel xmlChannel)
    {
      ch=xmlChannel;
    }

    public IXMLChannel getXMLChannel()
    {
      return ch;
    }

    public void init (ChannelConfig chConfig) {
	ChannelStaticData sd=new ChannelStaticData();
	sd.setChannelID(chConfig.getChannelID());
	sd.setParameters(chConfig);
	ch.setStaticData(sd);

	chanID=chConfig.getChannelID();
    }

    public Vector getParameters()
    {
      ChannelSubscriptionProperties csp;
      if((csp=ch.getSubscriptionProperties())!=null)
	  return csp.getParameterFields();
      else return null;
    }

    public String getName () {
	return ch.getSubscriptionProperties().getName();
    }
    public boolean isMinimizable () {
	return ch.getSubscriptionProperties().isMinimizable();
    }
    public boolean isDetachable () {
	return ch.getSubscriptionProperties().isDetachable();
    }
    public boolean isRemovable () {
	return ch.getSubscriptionProperties().isRemovable();
    }
    public boolean isEditable () {
	return ch.getSubscriptionProperties().isEditable();
    }
    public boolean hasHelp () {
	return ch.getSubscriptionProperties().hasHelp();
    }

    public int getDefaultDetachWidth () {
	return Integer.parseInt(ch.getSubscriptionProperties().getDefaultDetachWidth());
    }
    public int getDefaultDetachHeight () {
	return Integer.parseInt(ch.getSubscriptionProperties().getDefaultDetachHeight());
    }

    public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out) {
	ChannelRuntimeData rd=new ChannelRuntimeData();
	rd.setHttpRequest(req);

	// determine BaseActionURL
	// here it is of the form "file.jsp?channelTarget=channelID&..."
	// so we need to determine what jsp file is being used
	// the same jsp is used for forwarding when LayoutEvents occurs

	rd.setBaseActionURL(getJSP(req)+"channelTarget="+chanID+"&");
	// get the action parameters passed to the channel
	String channelTarget=null;
	if((channelTarget=req.getParameter("channelTarget"))!=null && (channelTarget.equals(chanID))) {
	    Enumeration e=req.getParameterNames();
	    if(e!=null) {
		while(e.hasMoreElements()) {
		    String pName=(String) e.nextElement();
		    if(!pName.equals("channelTarget")) {
			rd.setParameter(pName,req.getParameter(pName));
		    }
		}
	    }
	}
	ch.setRuntimeData(rd);

	HTMLSerializer htmlSerializer= new HTMLSerializer(out,new OutputFormat("HTML","UTF-8",true));
	ch.renderXML(htmlSerializer);
    }


    public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out) {
	ch.receiveEvent(new LayoutEvent(LayoutEvent.EDIT_BUTTON_EVENT));
	render(req,res,out);
    }
    public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out) {
	ch.receiveEvent(new LayoutEvent(LayoutEvent.HELP_BUTTON_EVENT));
	render(req,res,out);
    }


    private String getJSP(HttpServletRequest req)
    {
	String reqURL=req.getRequestURI();
	String jspfile=reqURL.substring(reqURL.lastIndexOf('/')+1,reqURL.length());
	// it used to be that XML channels were always rendered through main index.jsp,
	// now they are allowed to render under other .jsp, such as dispatch.jsp.
	//	if(jspfile.equals("") || jspfile.equals("dispatch.jsp")) jspfile="index.jsp";
	if(jspfile.equals("")) jspfile="index.jsp";
	else if(jspfile.equals("detach.jsp")) {
	    //reconstruct URL parameters
	    jspfile=req.getRequestURI()+"?";
	    for (Enumeration e = req.getParameterNames() ; e.hasMoreElements() ;) {
		String pName=(String) e.nextElement();
		String pValue= req.getParameter(pName);
		jspfile+=pName+"="+pValue+"&";
	    }
	}
	else if(jspfile.equals("dispatch.jsp")) {
	    //dispatch jsp needs to carry two parameters : method and channelID
	    jspfile+="?method="+req.getParameter("method")+"&channelID="+req.getParameter("channelID")+"&";
	}
	else jspfile+='?';
	Logger.log(Logger.DEBUG,"XMLChannelWrapper::getJSP() : jspfile=\""+jspfile+"\"");
	return jspfile;
    }
}


