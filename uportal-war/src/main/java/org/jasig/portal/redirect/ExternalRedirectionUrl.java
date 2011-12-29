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
package org.jasig.portal.redirect;

/**
 * ExternalRedirectionUrl represents a non-portlet URL target for the redirection
 * service. 
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
public class ExternalRedirectionUrl extends AbstractRedirectionUrl {

    private String url;

    /**
     * String representation of an absolute URL.  This url cannot be relative
     * and should not include any parameters in the string.
     * 
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the string representation of an absolute URL.  This url cannot be relative
     * and should not include any parameters in the string.
     * 
     * @return
     */
    public void setUrl(String url) {
        this.url = url;
    }

}
