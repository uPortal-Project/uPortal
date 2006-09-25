/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.collections.iterators.IteratorEnumeration;
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
    
    final protected Map parameters = new Hashtable();
    protected boolean request_verified;
    private static int sizeLimit = -1;

    /**
     * Default value for the upload size limit.
     * This value will be used when we are unable to load the corresponding property.
     */
    private static final int DEFAULT_SIZE_LIMIT = 3000000;
    
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
        
        // only bother with parameter work if should be accessable
        if (request_verified) {
            //Determine if this is a request for a portlet
            boolean isPortletRequest = source.getParameterMap().containsKey(PortletStateManager.ACTION);
                        
            // parse request body
            String contentType = source.getContentType();
            if (contentType != null && contentType.startsWith("multipart/form-data") && !isPortletRequest) {
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
        final Iterator keyItr = this.getParameterMap().keySet().iterator();
        return new IteratorEnumeration(keyItr);
    }

    /**
     * Return a String[] for this parameter
     * @param name the parameter name
     * @return String[] if parameter is not an Object[]
     */
    public String[] getParameterValues(String name) {
        Object[] pars = (Object[])this.getParameterMap().get(name);
        if (pars != null && pars instanceof String[]) {
            return (String[])this.getParameterMap().get(name);
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
		if (parameters.isEmpty()) {
			return super.getParameterMap();
		}
		
		Map mergedParams = new Hashtable(super.getParameterMap());
		Iterator it = parameters.keySet().iterator();
		while (it.hasNext()) {
			String pName = (String) it.next();
			mergedParams.put(pName, parameters.get(pName));
		}
		return mergedParams;
    }

    /**
     * Return the Object represented by this parameter name
     * @param name the parameter name
     * @return Object
     */
    public Object[] getObjectParameterValues(String name) {
        return (Object[])this.getParameterMap().get(name);
    }
    
    private void setFileUploadMaxSize() {
        // Obtain file upload max size
        // This property was renamed in uPortal 2.3, so we'll check the old name if it is missing
        if (sizeLimit < 0) {
            try {
                sizeLimit = PropertiesManager.getPropertyAsInt("org.jasig.portal.RequestParamWrapper.file_upload_max_size");
            } catch (Exception e) {
                // if the old property name also isn't set, fall back on the default value.
                sizeLimit = PropertiesManager.getPropertyAsInt("org.jasig.portal.PortalSessionManager.File_upload_max_size", DEFAULT_SIZE_LIMIT);
            }
        }
    }

}