package org.jasig.portal;

import javax.servlet.http.*;
import java.util.Hashtable;
import java.util.Enumeration;


/**
 * A set of runtime data acessable by a channel.
 * Includes the following data
 *  - Channel ID (should probably be moved through  initParams() instead)
 *  - Base channel action URL
 *  - HTTP request/response
 *  - A hashtable of parameters passed to the current channel
 * 
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class RuntimeData {
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String chanID;
    private String baseActionURL;
    private Hashtable parameters=new Hashtable();
    
    public RuntimeData() {};
    public RuntimeData(HttpServletRequest req, HttpServletResponse res, String cID, String baURL,Hashtable params) {
	request=req;
	response=res;
	chanID=cID;
	baseActionURL=baURL;
	parameters=params;
    }


    public void setChannelID(String cID) { chanID=cID; }
    public void setBaseActionURL(String baURL) { baseActionURL=baURL; }
    public void setHttpRequest(HttpServletRequest req) { request=req; }
    public void setHttpResponse(HttpServletResponse res) { response=res; }
    public void setParameters(Hashtable params) { parameters=params; }


    public String getChannelID() { return chanID; }
    public String getBaseActionURL() { return baseActionURL; }
    public HttpServletRequest getHttpRequest() { return request; }
    public HttpServletResponse getHttpResponse() { return response; }
    
    public String getParameter(String pName) { 
	return (String )parameters.get(pName); 
    }
    public Enumeration getParameterNames() { 
	return parameters.keys(); 
    }

    public void setParameter(String pName,String pValue) {
	parameters.put(pName,pValue);
    }

}
