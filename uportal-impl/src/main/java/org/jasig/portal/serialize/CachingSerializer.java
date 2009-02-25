/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.serialize;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
/**
 * <code>CachingSerializer</code> interface allows low-level character interaction with a serializer.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 */
public interface CachingSerializer extends MarkupSerializer {

    /**
     * Commands serializer to start caching.
     *
     * @return <code>false</code> if the serializer is already caching.
     */
    public boolean startCaching() throws IOException ;

    /**
     * Commands serializer to stop caching.
     *
     * @return <code>false</code> if the serializer was NOT caching.
     */
    public boolean stopCaching() throws IOException ;


    /**
     * Asks serializer for an accumulated cache content.
     *
     * @return a <code>String</code> cache.
     * @exception UnsupportedEncodingException if the OutputFormat specified unsupported encoding
     */
    public String getCache() throws UnsupportedEncodingException, IOException;
    
    /**
     * Allows one to print a <code>String</code> of characters directly to the output stream.
     *
     * @param text a <code>String</code> value
     */
    public void printRawCharacters(String text) throws IOException;

    /**
     * Let the serializer know if the document has already
     * been started. 
     *
     * @param setting a <code>boolean</code> value
     */
    public void setDocumentStarted(boolean setting);

    /**
     * Flushes all the buffers
     *
     * @exception IOException if an error occurs
     */
    public void flush() throws IOException;
}
