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

package org.jasig.portal.rendering;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.utils.cache.CacheKey;

/**
 * Source of additional {@link Attribute}s used by the {@link StAXAttributeIncorporationComponent}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface AttributeSource {
    /**
     * Called for each {@link StartElement} event. Any returned attributes will
     * be added to the event
     */
    public Iterator<Attribute> getAdditionalAttributes(HttpServletRequest request, HttpServletResponse response, StartElement event);
    
    /**
     * Get a cache key representing the state of the additional attributes for the request.
     */
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response);
}