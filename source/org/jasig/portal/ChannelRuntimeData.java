/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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
import java.io.File;
import java.util.Enumeration;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import org.jasig.portal.services.LogService;
import com.oreilly.servlet.multipart.Part;

/**
 * A set of runtime data accessable by a channel.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class ChannelRuntimeData extends Hashtable implements Cloneable {
    private boolean renderingAsRoot=false;
    private BrowserInfo binfo=null;
    private String channelSubscribeId=null;
    private UPFileSpec channelUPFile;
    private String baseActionURL = null; // Not sure if this will stay
    private String httpRequestMethod=null;

    /**
     * Default empty constructor
     */
    public ChannelRuntimeData() {
      super();
      channelUPFile = new UPFileSpec();
    }

    /**
     * Create a new instance of ourself
     * Used by the CError channel
     */
    public Object clone() {
      ChannelRuntimeData crd = new ChannelRuntimeData();
      crd.channelUPFile = channelUPFile;
      crd.binfo = binfo;
      crd.channelSubscribeId=channelSubscribeId;
      crd.renderingAsRoot=renderingAsRoot;
      crd.putAll(this);
      return  crd;
    }

    /**
     * Set a UPFileSpec which will be used to produce
     * baseActionURL and workerActionURL.
     *
     * @param baURL a baseActionURL value.
     */
    public void setUPFile(UPFileSpec upfs) {
        channelUPFile = upfs;
    }

    /**
     * Set the HTTP Reqeust method.
     *
     * @param method a <code>String</code> value
     */
    public void setHttpRequestMethod(String method) {
        this.httpRequestMethod=method;
    }

    /**
     * Get HTTP request method (i.e. GET, POST)
     *
     * @return a <code>String</code> value
     */
    public String getHttpRequestMethod() {
        return this.httpRequestMethod;
    }

    /**
     * Sets the base action URL.  This was added back in for the benefit
     * of web services.  Not sure if it is going to stay this way.
     * @param baseActionURL the base action URL
     */
    public void setBaseActionURL(String baseActionURL) {
        this.baseActionURL = baseActionURL;
    }

    /**
     * Sets whether or not the channel is rendering as the root of the layout.
     * @param rar <code>true</code> if channel is rendering as the root, otherwise <code>false</code>
     */
    public void setRenderingAsRoot(boolean rar) {
        renderingAsRoot = rar;
    }

    /**
     * Setter method for browser info object.
     *
     * @param bi a browser info associated with the current request
     */
    public void setBrowserInfo(BrowserInfo bi) {
        this.binfo = bi;
    }

    /**
     * Setter method for channel subscribe Id.
     *
     * @param chanSubscribeId a <code>String</code> value, first character must be alphanumeric.
     */
    public void setChannelSubscribeId(String chanSubscribeId) {
        this.channelSubscribeId=chanSubscribeId;
    }

    /**
     * Provides information about a user-agent associated with the current request/response.
     *
     * @return a <code>BrowserInfo</code> object ecapsulating various user-agent information.
     */
    public BrowserInfo getBrowserInfo() {
        return  binfo;
    }

    /**
     * A convenience method for setting a whole set of parameters at once.
     * The values in the Map must be object arrays. If (name, value[]) is in
     * the Map, then a future call to getParameter(name) will return value[0].
     * @param params a <code>Map</code> of parameter names to parameter value arrays.
     */
    public void setParameters(Map params) {
      this.putAll(params); // copy a Map
    }

    /**
     * A convenience method for setting a whole set of parameters at once.
     * The Map should contain name-value pairs.  The name should be a String
     * and the value should be either a String or a Part.
     * If (name, value) is in the Map then a future call to getParameter(name)
     * will return value.
     * @param params a <code>Map</code> of parameter names to parameter value arrays.
     */
    public void setParametersSingleValued(Map params) {
        if (params != null) {
            java.util.Iterator iter = params.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                Object value = params.get(key);
                if (value instanceof String)
                    setParameter(key, (String)value);
                else if (value instanceof Part)
                    setParameter(key, (Part)value);
            }
        }
    }

    /**
     * Sets multi-valued parameter.
     *
     * @param pName parameter name
     * @param values an array of parameter values
     * @return an array of parameter values
     */
    public String[] setParameterValues(String pName, String[] values) {
        return  (String[])super.put(pName, values);
    }

    /**
     * Establish a parameter name-value pair.
     *
     * @param pName parameter name
     * @param value parameter value
     */
    public  void setParameter(String pName, String value) {
        String[] valueArray = new String[1];
        valueArray[0] = value;
        super.put(pName, valueArray);
    }

    public  com.oreilly.servlet.multipart.Part[] setParameterValues(String pName, com.oreilly.servlet.multipart.Part[] values) {
        return  (com.oreilly.servlet.multipart.Part[])super.put(pName, values);
    }

    public synchronized void setParameter(String key, Part value) {
        Part[] valueArray = new Part[1];
        valueArray[0] = value;
        super.put(key, valueArray);
    }

    /**
     * Returns a baseActionURL - parameters of a request coming in on the baseActionURL
     * will be placed into the ChannelRuntimeData object for channel's use.
     *
     * @return a value of URL to which parameter sequences should be appended.
     */
    public String getBaseActionURL() {
        return this.getBaseActionURL(false);
    }

    /**
     * Returns a baseActionURL - parameters of a request coming in on the baseActionURL
     * will be placed into the ChannelRuntimeData object for channel's use.
     *
     * @param idempotent a <code>boolean</code> value specifying if a given URL should be idepotent.
     * @return a value of URL to which parameter sequences should be appended.
     */
    public String getBaseActionURL(boolean idempotent) {
        // If the base action URL was explicitly set, use it 
        // peterk: we should probably introduce idepotent version of this one as well, at some point
        if (baseActionURL != null) {
          return baseActionURL;
        }

        String url=null;
        try {
            if(idempotent) {
                UPFileSpec upfs=new UPFileSpec(channelUPFile);
                upfs.setTagId(PortalSessionManager.IDEMPOTENT_URL_TAG);
                url=upfs.getUPFile();
            } else {
                url=channelUPFile.getUPFile();
            }
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR,"ChannelRuntimeData::getBaseActionURL() : unable to construct a base action URL!");
        }
        return url;
    }

    /**
     * Returns the URL to invoke one of the workers specified in PortalSessionManager.
     * Typically the channel that is invoked with the worker will have to implement an
     * interface specific for that worker.
     * @param worker - Worker string must be a UPFileSpec.xxx value.
     * @return URL to invoke the worker.
     */
    public String getBaseWorkerURL(String worker) {
        // todo: propagate the exception
        String url=null;
        try {
            url=getBaseWorkerURL(worker,false);
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR,"ChannelRuntimeData::getWorkerActionURL() : unable to construct a worker action URL for a worker \""+worker+"\".");
        }
        return url;
    }

    /**
     * Returns the URL to invoke one of the workers specified in PortalSessionManager.
     * Typically the channel that is invoked with the worker will have to implement an
     * interface specific for that worker.
     * @param worker - Worker string must be a UPFileSpec.xxx value.
     * @param idempotent a <code>boolean</code> value sepcifying if a URL should be idempotent
     * @return URL to invoke the worker.
     * @exception PortalException if an error occurs
     */
    public String getBaseWorkerURL(String worker, boolean idempotent) throws PortalException {
        String url=null;
        UPFileSpec upfs=new UPFileSpec(channelUPFile);
        upfs.setMethod(UPFileSpec.WORKER_METHOD);
        upfs.setMethodNodeId(worker);
        if(idempotent) {
            upfs.setTagId(PortalSessionManager.IDEMPOTENT_URL_TAG);
        }
                        
        url=upfs.getUPFile();
            /*
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR,"ChannelRuntimeData::getWorkerActionURL() : unable to construct a worker action URL for a worker \""+worker+"\".");
        }
            */
        return url;
    }

    /**
     * Tells whether or not the channel is rendering as the root of the layout.
     * @return <code>true</code> if channel is rendering as the root, otherwise <code>false</code>
     */
    public boolean isRenderingAsRoot() {
      return renderingAsRoot;
    }

    /**
     * Get a parameter value. If the parameter has multiple values, only the first value is returned.
     *
     * @param pName parameter name
     * @return parameter value
     */
    public String getParameter(String pName) {
        String[] value_array = this.getParameterValues(pName);
        if ((value_array != null) && (value_array.length > 0))
            return  value_array[0];
        else
            return  null;
    }

    /**
     * Obtain an <code>Object</code> parameter value. If the parameter has multiple values, only the first value is returned.
     *
     * @param pName parameter name
     * @return parameter value
     */
    public Object getObjectParameter(String pName) {
        Object[] value_array = this.getParameterValues(pName);
        if ((value_array != null) && (value_array.length > 0)) {
            return  value_array[0];
        } else {
            return  null;
        }
    }

    /**
     * Obtain all values for a given parameter.
     *
     * @param pName parameter name
     * @return an array of parameter string values
     */
    public String[] getParameterValues(String pName) {
      Object[] pars = (Object[])super.get(pName);
      if (pars instanceof String[]) {
        return  (String[])pars;
      } else {
        return  null;
      }
    }

    /**
     * Obtain all values for a given parameter as <code>Object</code>s.
     *
     * @param pName parameter name
     * @return a vector of parameter <code>Object[]</code> values
     */
    public Object[] getObjectParameterValues(String pName) {
        return  (Object[])super.get(pName);
    }


    /**
     * Get an enumeration of parameter names.
     *
     * @return an <code>Enumeration</code> of parameter names.
     */
    public Enumeration getParameterNames() {
        return  (Enumeration)super.keys();
    }

    /**
     * Get the parameters as a Map
     * @return a Map of parameter name-value pairs
     */
    public Map getParameters() {
        Map params = new java.util.HashMap(this.size());
        Enumeration e = this.getParameterNames();
        while (e.hasMoreElements()) {
          String name = (String)e.nextElement();
          String value = this.getParameter(name);
          params.put(name, value);
        }
        return params;
    }
}



