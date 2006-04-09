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
 * @author mark.boyd@sungardhe.com
 */
public interface ISaxProcessor
{
    /**
     * Returns a ContentHandler suitable for pushing SAX events into this 
     * processor possibly to be filtered or added to by the processor and then
     * have the resulting SAX stream pushed into the ContentHandler registered
     * via setExitContentHandler().
     *
     */
    public ContentHandler getEntryContentHandler();

    /**
     * Set the ContentHandler that will receive resulting SAX events that have 
     * pass through and potentially been modified by this processor.
     *  
     * @param processor
     */
    public void setExitContentHandler(ContentHandler handler);

    /**
     * Provide a key that is indicative of the content contributed to the SAX
     * layout event stream by this class allowing cached content to be reused
     * in some instances thereby alleviating the more costly XSLT 
     * transformations from taking place. If there are no modifications made to
     * the SAX event stream by the processsor then it should return an empty
     * String.
     * 
     * @return
     */
    public String getCacheKey();
}
