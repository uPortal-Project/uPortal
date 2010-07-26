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
 * A general channel cache key class. The class includes the key iteslf, as well as key properties.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class ChannelCacheKey {
    /**
     * Specifies that the cache is specific to the instance of the channel that generated it.
     * This is the default scope. This scope should be used if the screen rendering at the 
     * current state involves any user-specific information.
     */
    public static final int INSTANCE_KEY_SCOPE=0;
    /**
     * Specifies that the cache is accessable by all instances of that channel class.
     * In construction a session-wide key, make sure to include static data information in there.
     */
    public static final int SYSTEM_KEY_SCOPE=1;

    String key; // the actual key
    int keyScope; // scope in which the cache to be used
    Object keyValidity; // validity object

    public ChannelCacheKey() {
	key=null; keyValidity=null;
	keyScope=INSTANCE_KEY_SCOPE;
    }

    public void setKey(String key) {
	this.key=key;
    }
    /**
     * Returns a key uniqly describing the channel state.
     */
    public String getKey() { return key; }

    public void setKeyScope(int scope) { this.keyScope=scope; }
    /**
     * Returns a specification of the scope in which the cache to be used.
     * Possible values are : INSTANCE_KEY_SCOPE and SYSTEM_KEY_SCOPE.
     */
    public int getKeyScope() { return keyScope; }

    public void setKeyValidity(Object validity) {
	this.keyValidity=validity; 
    }
    /**
     * Returns an object that can be used by the channel to verify cache validity.
     * In general, cache validators allow to strengthen the key, by allowing for non-exact
     * checks. A good example is a cache validity condition involving expiration time-stamp.
     */
    public Object getKeyValidity() { return keyValidity; }
}
