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

import javax.servlet.http.*;

import java.util.Hashtable;
import java.util.Map;

import org.jasig.portal.security.IPerson;

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

public class ChannelRuntimeData extends Hashtable
{
    private HttpServletRequest request;
    private String baseActionURL;

    private IPerson m_person;

    public ChannelRuntimeData()
    {
	super();

	// set the default values for the parameters here
	request=null;
	baseActionURL=null;
    };


    // the set methods ...
    public void setPerson(IPerson person)
    {
      m_person = person;
    }

    public void setBaseActionURL(String baURL) { baseActionURL=baURL; }
    public void setHttpRequest(HttpServletRequest req) { request=req; }

    public void setParameters(Map params)
    {
	// copy a Map
	this.putAll(params);
    }

    public void setParameter(String pName,String pValue)
    {
	this.put(pName,pValue);
    }

    // the get methods ...
    public IPerson getPerson()
    {
      return(m_person);
    }

    public String getBaseActionURL() { return baseActionURL; }
    public HttpServletRequest getHttpRequest() { return request; }

    // Parameters are strings !
    public synchronized String setParameter (Object key, String value) {return (String) super.put (key, value);}
    public synchronized String getParameter (Object key) {return (String) super.get (key);}

    // if you need to pass objects, use this
    public synchronized Object put (Object key, Object value) {return super.put (key, value);}
    public synchronized Object get (Object key) {return super.get (key);}
}
