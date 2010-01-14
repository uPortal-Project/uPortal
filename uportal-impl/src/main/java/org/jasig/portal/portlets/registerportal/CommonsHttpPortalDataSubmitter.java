/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlets.registerportal;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CommonsHttpPortalDataSubmitter implements IPortalDataSubmitter {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String submitUrl = null;
    private Set<String> ignoreProperties;
    
    public CommonsHttpPortalDataSubmitter() {
        this.ignoreProperties = new HashSet<String>();
        this.ignoreProperties.add("class");
        this.ignoreProperties.add("dataToSubmit");
    }
    
    /**
     * @param submitUrl URL to POST registration to
     */
    public void setSubmitUrl(String submitUrl) {
        this.submitUrl = submitUrl;
    }

    /**
     * @param ignoreProperties Properties on {@link PortalRegistrationData} to ignore when submitting
     */
    public void setIgnoreProperties(Set<String> ignoreProperties) {
        this.ignoreProperties = ignoreProperties;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataSubmitter#submitPortalData(org.jasig.portal.portlets.registerportal.PortalRegistrationData)
     */
    public boolean submitPortalData(PortalRegistrationData portalRegistrationData) {
        final HttpClient client = new HttpClient();
        final HttpClientParams httpClientParams = client.getParams();
        httpClientParams.setSoTimeout(5000);
        
        String postUrl = this.submitUrl;
        for (int redirectCounter = 0; redirectCounter < 10; redirectCounter++) {
            final PostMethod post = new PostMethod(postUrl);
            try {
                this.addParameters(portalRegistrationData, post);
                
                Integer result = null;
                try {
                    result = client.executeMethod(post);
                    
                    if (result == 200) {
                        this.logger.info("Portal registration data successfully submitted to " + this.submitUrl);
                        return true;
                    }
                    else if (result >= 300 && result <= 399) {
                        final Header newLocation = post.getResponseHeader("location");
                        postUrl = newLocation.getValue();
                        this.logger.info("Handling redirect to " + postUrl);
                        continue;
                    }
                }
                catch (HttpException e) {
                    this.logger.warn("Portal registration data failed to submit due to a HTTP exception", e);
                    return false;
                }
                catch (IOException e) {
                    this.logger.warn("Portal registration data failed to submit due to an IO exception", e);
                    return false;
                }
                
                this.logger.warn("Portal registration data failed to submit with return code " + result);
                return false;
            }
            finally {
                post.releaseConnection();
            }
        }
        
        this.logger.warn("Portal registration data failed to submit due to too many redirects");
        return false;
    }

    /**
     * Add parameters to the post from the registration data
     */
    protected void addParameters(PortalRegistrationData portalRegistrationData, final PostMethod post) {
        //Add static parameters
        final PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(portalRegistrationData.getClass());
        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            final String name = propertyDescriptor.getName();
            
            //Skip ignored properties
            if (this.ignoreProperties != null && this.ignoreProperties.contains(name)) {
                continue;
            }
            
            //Get the read method, skipping the property if it is null
            final Method readMethod = propertyDescriptor.getReadMethod();
            if (readMethod == null) {
                continue;
            }

            //Read the value
            Object value;
            try {
                value = readMethod.invoke(portalRegistrationData);
            }
            catch (Exception e) {
                this.logger.info("Failed to read property " + name + " and will skip it. From bean " + portalRegistrationData);
                continue;
            }
            
            if (value instanceof Map) {
                final Map<?, ?> dataMap = (Map<?, ?>)value;
                for (final Map.Entry<?, ?> dataEntry : dataMap.entrySet()) {
                    final String dataKey = String.valueOf(dataEntry.getKey());
                    final Object dataValue = dataEntry.getValue();
                    
                    if (dataValue instanceof Map) {
                        final Map<?, ?> valueMap = (Map<?, ?>)dataValue;
                        for (final Map.Entry<?, ?> valueEntry : valueMap.entrySet()) {
                            final String valueKey = String.valueOf(valueEntry.getKey());
                            final Object valueValue = valueEntry.getValue();
                            if (valueValue != null) {
                                post.addParameter(dataKey + "_" + valueKey, String.valueOf(valueValue));
                            }
                        }
                    }
                    else {
                        if (dataValue != null) {
                            post.addParameter(dataKey, String.valueOf(dataValue));
                        }
                    }
                }
            }
            else if (value != null) {
                post.addParameter(name, String.valueOf(value));
            }
        }
    }
}
