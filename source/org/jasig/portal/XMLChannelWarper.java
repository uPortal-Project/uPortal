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
 * @version 0.1
 * @author Peter Kharchenko
 */


public class XMLChannelWarper implements IChannel
{
    IXMLChannel ch;
    String chanID;
    
    public XMLChannelWarper(IXMLChannel xmlChannel) { ch=xmlChannel; }
    
    public void init (ChannelConfig chConfig) {
	ChannelStaticData sd=new ChannelStaticData();
	sd.setChannelID(chConfig.getChannelID());
	sd.setParameters(chConfig);
	ch.setStaticData(sd);
	
	chanID=chConfig.getChannelID();
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
	// IXMLChannels don't conform to dispatch.jsp stuff, hence the redirect
	try {
	    res.sendRedirect(getJSP(req));
	} catch (Exception e) { Logger.log(Logger.ERROR,e); }
	//	render(req,res,out);
    }
    public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out) {
	ch.receiveEvent(new LayoutEvent(LayoutEvent.HELP_BUTTON_EVENT));
	try {
	    res.sendRedirect(getJSP(req));
	} catch (Exception e) { Logger.log(Logger.ERROR,e); }

	//	render(req,res,out);
    }

    private String getJSP(HttpServletRequest req)
    {
	String reqURL=req.getRequestURI();
	String jspfile=reqURL.substring(reqURL.lastIndexOf('/')+1,reqURL.length());
	if(jspfile.equals("") || jspfile.equals("dispatch.jsp")) jspfile="index.jsp";
	if(jspfile.equals("detach.jsp")) {
	    //reconstruct URL parameters
	    jspfile=req.getRequestURI()+"?";
	    for (Enumeration e = req.getParameterNames() ; e.hasMoreElements() ;) {
		String pName=(String) e.nextElement(); 
		String pValue= req.getParameter(pName);
		jspfile+=pName+"="+pValue+"&";
	    }
	}
	else jspfile+='?';
	Logger.log(Logger.DEBUG,"XMLChannelWarper::getJSP() : jspfile=\""+jspfile+"\"");
	return jspfile;
    }
}



