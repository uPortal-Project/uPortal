package org.jasig.portal;


import javax.servlet.http.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;
import org.xml.sax.*;

// this class shall have the burden of squeezing content
// out of channels.

// future prospects:
//  - Wrap IChannel classes
//     this should be done by parsing through
//     HTML that IChannel can output
//
//  - more complex caching ?
//  - Validation and timeouts
//      these two are needed for smooth operation of the portal
//      sometimes channels will timeout with information retreival
//      then the content should be skipped
//

public class ChannelManager {
    private HttpServletRequest req;
    private HttpServletResponse res;
    
    private Hashtable channelTable;

    private String channelTarget;
    private Hashtable targetParams;
    
    
    public ChannelManager(){ channelTable=new Hashtable();};
    public ChannelManager(HttpServletRequest request,HttpServletResponse response) {
	this();
	this.req=request; this.res=response;
    }
    public void setReqNRes(HttpServletRequest request,HttpServletResponse response) {
	this.req=request; this.res=response;
	processRequestChannelParameters(request);
    }

    /**
     * Look through request parameters for "channelTarget" and
     * pass corresponding actions/params to the channel
     * @param the request object
     */
    private void processRequestChannelParameters(HttpServletRequest req) {
	// clear the previous settings
	channelTarget=null;
	targetParams=new Hashtable();

	if((channelTarget=req.getParameter("channelTarget"))!=null) {
	    //	    Logger.log(Logger.DEBUG,"ChannelManager::processRequestChannelParameters() : Recording parameters for channel \""+channelTarget+"\"");
	    Enumeration e=req.getParameterNames();
	    if(e!=null) {
		while(e.hasMoreElements()) {
		    String pName=(String) e.nextElement();
		    if(!pName.equals("channelTarget")) {
			targetParams.put(pName,req.getParameter(pName));
			//			Logger.log(Logger.DEBUG,"ChannelManager::processRequestChannelParameters() : recorded (\""+pName+"\",\""+req.getParameter(pName)+"\")");
		    }
		}
	    }
	}
    }

    /**
     * Output channel content.
     * @param channel ID (unique)
     * @param name of the channel class
     * @param a table of parameters
     * @param an output DocumentHandler target
     */
    public void processChannel(String chanID,String className,Hashtable params,DocumentHandler dh) {
	try{
	    // see if the channel is cached
	    IXMLChannel ch;
	    if((ch=(IXMLChannel) channelTable.get(chanID))==null) {
		ch = (org.jasig.portal.IXMLChannel) Class.forName (className).newInstance ();
		// construct a ChannelStaticData object 
		ChannelStaticData sd=new ChannelStaticData();
		sd.setChannelID(chanID);
		sd.setParameters( params);
		ch.setStaticData(sd);
		channelTable.put(chanID,ch);
	    }

	    ChannelSAXStreamFilter custodian=new ChannelSAXStreamFilter(dh);
	    
	    // set up RuntimeData for the channel
	    Hashtable chParams=new Hashtable();
	    if(chanID.equals(channelTarget)) chParams=targetParams;
	    //	    RuntimeData rd=new ChannelRuntimeData(req,res,chanID,"index.jsp?channelTarget="+chanID+"&",chParams);
	    ChannelRuntimeData rd=new ChannelRuntimeData();
	    rd.setParameters(chParams);
	    rd.setHttpRequest(req);
	    String reqURI=req.getRequestURI();
	    reqURI=reqURI.substring(reqURI.lastIndexOf("/")+1,reqURI.length());
	    rd.setBaseActionURL(reqURI+"?channelTarget="+chanID+"&");
	    ch.setRuntimeData(rd);
	    ch.renderXML(custodian);
	} catch (Exception e) { Logger.log(Logger.ERROR,e); }
    }

    /**
     * passes Layout-level event to a channel
     * @param channel ID
     * @param LayoutEvent object
     */
    public void passLayoutEvent(String chanID,LayoutEvent le) {
	IXMLChannel ch=(IXMLChannel) channelTable.get(chanID);
	if(ch!=null) {
	    ch.receiveEvent(le);
	} else Logger.log(Logger.ERROR,"ChannelManager::passLayoutEvent() : trying to pass an event to a channel that is not in cache. (cahnel=\""+chanID+"\")");
    }	
}
