package org.jasig.portal;

import javax.servlet.http.*;
import java.util.Hashtable;
import java.util.Map;


/**
 * A set of runtime data acessable by a channel.
 * Includes the following data
 * <ul>
 *  <li>Base channel action URL</li>
 *  <li> HTTP request</li>
 *  <li>A hashtable of parameters passed to the current channel</li>
 * 
 * @author Peter Kharchenko
 * @version $Revision$
 */


public class ChannelRuntimeData extends Hashtable{


    private HttpServletRequest request;
    private String baseActionURL;
    

    public ChannelRuntimeData() { 
	super();
    
	// set the default values for the parameters here
	request=null;
	baseActionURL=null;
    };


    // the set methods ...

    public void setBaseActionURL(String baURL) { baseActionURL=baURL; }
    public void setHttpRequest(HttpServletRequest req) { request=req; }

    public void setParameters(Map params) {
	// copy a Map 
	this.putAll(params);
    };

    public void setParameter(String pName,String pValue) {
	this.put(pName,pValue);
    }

    
    // the get methods ...
    public String getBaseActionURL() { return baseActionURL; }
    public HttpServletRequest getHttpRequest() { return request; }

    // Parameters are strings !
    public synchronized String setParameter (Object key, String value) {return (String) super.put (key, value);}
    public synchronized String getParameter (Object key) {return (String) super.get (key);}
    

    // if you need to pass objects, use this
    public synchronized Object put (Object key, Object value) {return super.put (key, value);}
    public synchronized Object get (Object key) {return super.get (key);}
}    
