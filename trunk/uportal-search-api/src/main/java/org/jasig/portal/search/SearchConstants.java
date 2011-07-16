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

package org.jasig.portal.search;

import javax.xml.namespace.QName;

/**
 * Constants related to the portal search API
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class SearchConstants {
    /**
     * Namespace used by uPortal search events
     */
    public static final String NAMESPACE = "https://source.jasig.org/schemas/uportal/search";

    /**
     * Local SearchRequest event name
     */
    public static final String SEARCH_REQUEST_LOCAL_NAME = "SearchRequest";
    /**
     * SearchRequest QName
     */
    public static final QName SEARCH_REQUEST_QNAME = new QName(NAMESPACE, SEARCH_REQUEST_LOCAL_NAME);
    /**
     * Same as {@link QName#toString()} on {@link #SEARCH_REQUEST_QNAME} but hard coded to allow use in annotations 
     */
    public static final String SEARCH_REQUEST_QNAME_STRING = "{" + NAMESPACE + "}" + SEARCH_REQUEST_LOCAL_NAME;
    
    /**
     * Local SearchRequest event name
     */
    public static final String SEARCH_RESULTS_LOCAL_NAME = "SearchResults";
    /**
     * SearchResponse QName
     */
    public static final QName SEARCH_RESULTS_QNAME = new QName(NAMESPACE, SEARCH_RESULTS_LOCAL_NAME);
    /**
     * Same as {@link QName#toString()} on {@link #SEARCH_RESULTS_QNAME} but hard coded to allow use in annotations 
     */
    public static final String SEARCH_RESULTS_QNAME_STRING = "{" + NAMESPACE + "}" + SEARCH_RESULTS_LOCAL_NAME;
    
    private SearchConstants() {
    }
}
