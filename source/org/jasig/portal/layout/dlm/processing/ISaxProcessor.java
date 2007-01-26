/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
