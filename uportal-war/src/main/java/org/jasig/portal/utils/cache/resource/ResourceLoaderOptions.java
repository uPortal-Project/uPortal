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

package org.jasig.portal.utils.cache.resource;

import java.security.MessageDigest;

/**
 * Options when loading a resource using {@link CachingResourceLoader}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ResourceLoaderOptions {
    /**
     * @return true if the input stream should be run through a {@link MessageDigest} during loading. If null the default will be used.
     */
    public Boolean isDigestInput();
    
    /**
     * @return The {@link MessageDigest} algorithm to use. If null the default will be used.
     */
    public String getDigestAlgorithm();
    
    /**
     * @return The interval in ms between checking for updates to the resource. If null the default will be used. If less than 1 checking will always be done.
     */
    public Long getCheckInterval();
}
