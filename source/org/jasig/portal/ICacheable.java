/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

/**
 * An interface that a cacheable channel must implement.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @version $Revision$
 */
public interface ICacheable {
    
    /**
     * Requests the channel to generate a key uniqly describing it's current state,
     * and a description of key usage.
     */
    public ChannelCacheKey generateKey();

    /**
     * Requests the channel to verify validity of the retreived cache based on the validator object.
     */
    public boolean isCacheValid(Object validity);
}
