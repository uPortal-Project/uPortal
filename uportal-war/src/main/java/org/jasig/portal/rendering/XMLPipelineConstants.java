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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;

/**
 * Constants used throughout the uPortal XML based rendering pipeline
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XMLPipelineConstants {
    /**
     * Represents a <folder> layout element
     */
    public static final QName FOLDER = new QName("folder");
    /**
     * Represents <channel> layout element
     */
    public static final QName CHANNEL = new QName("channel");
    /**
     * Represents <parameter> layout element
     */
    public static final QName PARAMETER = new QName("parameter");
    /**
     * Represents an ID element attribute
     */
    public static final QName ID_ATTR_NAME = new QName("ID");
    
    /**
     * The shared {@link XMLEventFactory} to use
     */
    public static final XMLEventFactory XML_EVENT_FACTORY = XMLEventFactory.newFactory();
}
