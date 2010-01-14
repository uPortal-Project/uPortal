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
 * @deprecated IChannel rendering code will be replaced with portlet specific rendering code in a future release
 */
@Deprecated
public interface IChannelRenderer
{
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
     * This method suppose to take care of the runaway rendering threads.
     */
    void kill();

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
        Map<IChannel, Map<String, ChannelCacheEntry>> cacheTables
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
    
    /**
     * This may only be called after {@link #outputRendering(ContentHandler)} is called.
     *  
     * @return The time in milliseconds it took for the channel to render
     */
    long getRenderTime();
    
    /**
     * This may only be called after {@link #outputRendering(ContentHandler)} is called.
     * 
     * @return If cached content was returned instead of actually rendering the channel
     */
    boolean isRenderedFromCache();
}
