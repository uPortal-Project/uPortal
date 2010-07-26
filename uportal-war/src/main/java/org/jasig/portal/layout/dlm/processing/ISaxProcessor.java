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

package org.jasig.portal.layout.dlm.processing;

import org.xml.sax.ContentHandler;

/**
 * Represents a processor that takes part in SAX event stream modification. If
 * this interface is implemented by a processor in the configured set of fixed
 * processors or by the currently selected optional processor then SAX events
 * passing through the pipe will be routed through this processing handler.
 *
 * @author Mark Boyd
 */
public interface ISaxProcessor
{
    /**
     * Returns a ContentHandler suitable for pushing SAX events into this 
     * processor possibly to be filtered or added to by the processor and then
     * have the resulting SAX stream pushed into the passed-in ContentHandler.
     * This method is called once for each rendering cycle of the pipe. If the
     * state of the processor is such that it will have not impact on the SAX
     * event stream the processor can elect to return the passed in 
     * ContentHandler.
     *  
     * @param processor
     */
    public ContentHandler getContentHandler(ContentHandler handler);

    /**
     * Provide a key that is indicative of the content contributed to the SAX
     * layout event stream by this class allowing cached content to be reused
     * in some instances thereby alleviating the more costly XSLT
     * transformations from taking place. If there are no modifications made to
     * the SAX event stream by the processsor then it should return an empty
     * String.
     *
     * @return String
     */
    public String getCacheKey();
}
