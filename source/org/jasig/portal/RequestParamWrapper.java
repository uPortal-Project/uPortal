/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.container.services.information.PortletStateManager;
import org.jasig.portal.properties.PropertiesManager;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;

/**
 * Wraps an http request object to prevent unverified requests from
 * accessing any of the request parameters.
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class RequestParamWrapper extends HttpServletRequestWrapper {
    
    private static final Log log = LogFactory.getLog(RequestParamWrapper.class);
    
    protected Hashtable parameters;
    protected boolean request_verified;
    private static int sizeLimit = -1;

    /**
     * Creates a new <code>RequestParamWrapper</code> instance.
     *
     * @param source an <code>HttpServletRequest</code> value that's being wrapped.
     * @param request_verified a <code>boolean</code> flag that determines if the request params should be accessable.
     */
    public RequestParamWrapper(HttpServletRequest source, boolean request_verified) {
        super(source);        
        setFileUploadMaxSize();
                
        // leech all of the information from the source request
        this.request_verified = request_verified;

        parameters = new Hashtable();

        // only bother with parameter work if should be accessable
        if (request_verified) {
            // parse request body
            String contentType = source.getContentType();
            String portletAction = source.getParameter(PortletStateManager.ACTION);
            if (contentType != null && contentType.startsWith("multipart/form-data") && portletAction == null) {
                com.oreilly.servlet.multipart.Part attachmentPart;
                try {
                    MultipartParser multi = new MultipartParser(source, source.getContentLength(), true, true, "UTF-8");
                    boolean noAttachments = source.getContentLength() > sizeLimit;
                    
                    while ((attachmentPart = multi.readNextPart()) != null) {
                        String partName = attachmentPart.getName();

                        if (attachmentPart.isParam()) {
                            ParamPart parameterPart = (ParamPart)attachmentPart;
                            String paramValue = parameterPart.getStringValue();
                            if (parameters.containsKey(partName)) {

                                /* Assume they meant a multivalued tag, like a checkbox */
                                String[] oldValueArray = (String[])parameters.get(partName);
                                String[] valueArray = new String[oldValueArray.length + 1];
                                for (int i = 0; i < oldValueArray.length; i++) {
                                    valueArray[i] = oldValueArray[i];
                                }
                                valueArray[oldValueArray.length] = paramValue;
                                parameters.put(partName, valueArray);
                            } else {
                                String[] valueArray = new String[1];
                                valueArray[0] = paramValue;
                                parameters.put(partName, valueArray);
                            }
                        } else if (attachmentPart.isFile()) {
                            FilePart filePart = (FilePart)attachmentPart;
                            String filename = filePart.getFileName();

                            MultipartDataSource fileUpload = null;
                            // check if this file has exceeded the maximum allowed upload size
                            if (noAttachments){
                                fileUpload = new MultipartDataSource(filename, "Exceeded file size allowed");
                                MultipartDataSource[] valueArray = new MultipartDataSource[1];
                                valueArray[0] = fileUpload;
                                parameters.put(partName, valueArray);
                            } else if (filename != null) {
                                fileUpload = new MultipartDataSource(filePart);
                            }
                            
                            if (fileUpload != null) {
                                
                                if (parameters.containsKey(partName)) {
                                    MultipartDataSource[] oldValueArray = (MultipartDataSource[])parameters.get(partName);
                                    MultipartDataSource[] valueArray = new MultipartDataSource[oldValueArray.length + 1];
                                    for (int i = 0; i < oldValueArray.length; i++) {
                                        valueArray[i] = oldValueArray[i];
                                    }
                                    valueArray[oldValueArray.length] = fileUpload;
                                    parameters.put(partName, valueArray);
                                } else {
                                    MultipartDataSource[] valueArray = new MultipartDataSource[1];
                                    valueArray[0] = fileUpload;
                                    parameters.put(partName, valueArray);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    //was: LogService.log(LogService.ERROR, e);
                    ExceptionHelper.genericTopHandler(Errors.bug, e);
                }
            }
            // regular params
            Enumeration en = source.getParameterNames();
            if (en != null) {
                while (en.hasMoreElements()) {
                    String pName = (String)en.nextElement();
                    parameters.put(pName, source.getParameterValues(pName));
                }
            }
        }
    }

    /**
     * Overloaded method
     * @param name the parameter name
     * @return parameter
     */
    public String getParameter(String name) {
        String[] value_array = this.getParameterValues(name);
        if ((value_array != null) && (value_array.length > 0)) {
            return value_array[0];
        } else {
            return null;
        }
    }

    /**
     * Overloaded method
     * @return parameter names
     */
    public Enumeration getParameterNames() {
        return this.parameters.keys();
    }

    /**
     * Return a String[] for this parameter
     * @param name the parameter name
     * @return String[] if parameter is not an Object[]
     */
    public String[] getParameterValues(String name) {
        Object[] pars = (Object[])this.parameters.get(name);
        if (pars != null && pars instanceof String[]) {
            return (String[])this.parameters.get(name);
        } else {
            return null;
        }
    }

    /**
     * Overloaded method
     *
     * @return a <code>Map</code> value
     */
    public Map getParameterMap() {
        return parameters;
    }

    /**
     * Return the Object represented by this parameter name
     * @param name the parameter name
     * @return Object
     */
    public Object[] getObjectParameterValues(String name) {
        return (Object[])this.parameters.get(name);
    }
    
    private void setFileUploadMaxSize() {
        // Obtain file upload max size
        // This property was renamed in uPortal 2.3, so we'll check the old name if it is missing
        if (sizeLimit < 0) {
            try {
                sizeLimit = PropertiesManager.getPropertyAsInt("org.jasig.portal.RequestParamWrapper.file_upload_max_size");
            } catch (Exception e) {
                sizeLimit = PropertiesManager.getPropertyAsInt("org.jasig.portal.PortalSessionManager.File_upload_max_size");
            }
        }
    }

}