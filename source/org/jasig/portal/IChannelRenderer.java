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

package org.jasig.portal;

import java.util.Map;

import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.SetCheckInSemaphore;
import org.xml.sax.ContentHandler;


/**
 * <p>The <code>IChannelRenderer</code> defines channel rendering interface.
 * The process of channel rendering can be implemented in different ways
 * including in serial form, in parallel form, or a mixture of the two. This
 * interface allows different implementation to use different implementation
 * policies.</p>
 *
 * <p>The channel renderer interaction model is as follows, in order of
 * invocation:</p>
 *
 * <li>first <code>startRendering</code></li>
 * <li>then <code>completeRendering</code></li>
 * <li>and optionally <code>outputRendering</code></li>
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 *
 * @version $Revision$
 **/
public interface IChannelRenderer
{
    /** <p> Class version identifier.</p> */
    public final static String RCS_ID = "@(#) $Header$";

    /** <p>Channel rendering was successful.</p> */
    public static int RENDERING_SUCCESSFUL = 0;

    /** <p>Channel rendering failed.</p> */
    public static int RENDERING_FAILED = 1;

    /** <p>Channel rendering timed-out.</p> */
    public static int RENDERING_TIMED_OUT = 2;

    /**
     * <p>Starts the channel rendering process.</p>
     **/
    void startRendering();

    /**
     * <p>Starts the channel rendering process.</p>
     *
     *
     * @param groupSemaphore semaphore to use for a group of channels
     *
     * @param groupRenderingKey group rendering key
     **/
    void startRendering(
        SetCheckInSemaphore groupSemaphore,
        Object groupRenderingKey
        );

    /**
     * <p>Complete the channel rendering.</p>
     *
     * @return status code of the channel rendering process
     *
     * @throws Throwable
     **/
    int completeRendering()
        throws Throwable;

    /**
     * <p>Cancels the rendering job.
     **/
    void cancelRendering();

    /**
     * <p>Returns the channel rendering character set.</p>
     *
     * @return string representation of the channel rendering characters
     **/
    String getCharacters();

    /**
     * <p>Returns the channel rendering buffer.</p>
     *
     * @return channel rendering buffer
     **/
    SAX2BufferImpl getBuffer();

    /**
     * <p>Sets the character cache for the channel renderer.</p>
     *
     * @param chars character cache for the channel renderer
     **/
    void setCharacterCache(
        String chars
        );

    /**
     * <p>Enables or disables character caching for the channel renderer.</p>
     *
     * @param setting character caching setting
     **/
    void setCharacterCacheable(
        boolean setting
        );

    /**
     * <p>Sets the cache tables for the channel renderer.</p>
     *
     * @param cacheTables cache table for the channel renderer
     **/
    void setCacheTables(
        Map cacheTables
        );

    /**
     * <p>Sets the timeout value for the channel renderer.</p>
     *
     * @param value milliseconds of timeout for the channel renderer
     **/
    void setTimeout(
        long value
        );

    /**
     * </p>Places the channel rendering output in the specified content
     * handler.</p>
     *
     * @param out content handler for the channel rendering information
     *
     * @return status code
     *
     * @throws Throwable if an error occurs
     **/
    int outputRendering(
        ContentHandler out
        )
        throws Throwable;
}
