/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
