/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
