/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.serialize;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
/**
 * <code>CachingSerializer</code> interface allows low-level character interaction with a serializer.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 */
public interface CachingSerializer {

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
