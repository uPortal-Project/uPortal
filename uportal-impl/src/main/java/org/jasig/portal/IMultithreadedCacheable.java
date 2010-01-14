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


/**
 * A cacheable interface for the multithreaded channels
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @see ICacheable
 * @see IMultithreadedChannel
 * 
 * @deprecated Use the IChannel* interfaces instead or write a portlet. For more information see: 
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IMultithreaded+Interfaces
 */

@Deprecated
public interface IMultithreadedCacheable {
    
    /**
     * Requests the channel to generate a key uniqly describing it's current state,
     * and a description of key usage.
     */
    public ChannelCacheKey generateKey(String uid);

    /**
     * Requests the channel to verify validity of the retreived cache based on the validator object.
     */
    public boolean isCacheValid(Object validity,String uid);
}
