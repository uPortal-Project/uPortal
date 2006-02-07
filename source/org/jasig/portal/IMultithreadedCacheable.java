/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;


/**
 * A cacheable interface for the multithreaded channels
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @see ICacheable
 * @see IMultithreadedChannel
 * 
 * @deprecated Use the IChannel* interfaces instead or write a portlet. For more information see: 
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IMultithreaded+Interfaces
 */

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
