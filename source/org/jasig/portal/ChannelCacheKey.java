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


/**
 * A general channel cache key class. The class includes the key iteslf, as well as key properties.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 */
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

    public void setKeyScope(int scope) { this.keyScope=keyScope; }
    /**
     * Returns a specification of the scope in which the cache to be used.
     * Possible values are : INSTANCE_KEY_SCOPE and SYSTEM_KEY_SCOPE.
     */
    public int getKeyScoe() { return keyScope; }

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
