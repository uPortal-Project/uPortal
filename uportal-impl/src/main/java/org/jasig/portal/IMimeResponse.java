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

package  org.jasig.portal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * An interface that a channel that wants to download MIME files must implement.
 * @author Shridar Venkatesh, svenkatesh@interactivebusiness.com
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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

    /**
     * Let the channel know that there were problems with the download
     *
     * @param e
     */
    public void reportDownloadError(Exception e);
}
