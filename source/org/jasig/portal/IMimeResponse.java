/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or withoutu
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

package  org.jasig.portal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * An interface that a channel that wants to download MIME files must implement.
 * @author Shridar Venkatesh, svenkatesh@interactivebusiness.com
 * @version $Revision$
 */
public interface IMimeResponse {

    /**
     * Returns the MIME type of the content.
     */
    public java.lang.String getContentType();

    /**
     * Returns the MIME content in the form of an input stream.
     * Returns null if the code needs the OutputStream object
     */
    public java.io.InputStream getInputStream() throws IOException;

    /**
     * Pass the OutputStream object to the download code if it needs special handling
     * (like outputting a Zip file).
     */
    public void downloadData(OutputStream out) throws IOException;

    /**
     * Returns the name of the MIME file.
     */
    public java.lang.String getName();

    /**
     * Returns a list of header values that can be set in the HttpResponse.
     * Returns null if no headers need to be set.
     */
    public Map getHeaders();

}
