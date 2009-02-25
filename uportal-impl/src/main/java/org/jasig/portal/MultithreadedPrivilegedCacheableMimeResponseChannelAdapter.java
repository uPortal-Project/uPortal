/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package  org.jasig.portal;


/**
 * Internal adapter for a multithreaded privileged channel that is also cacheable and implements IMimeResponse (capable
 * of using DonwloadWorker)
 * @author  <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version $Revision$
 * @see MultithreadedPrivilegedCacheableChannelAdapter
 */
public class MultithreadedPrivilegedCacheableMimeResponseChannelAdapter
extends MultithreadedCacheableMimeResponseChannelAdapter
implements IPrivilegedChannel {
    public MultithreadedPrivilegedCacheableMimeResponseChannelAdapter (IMultithreadedChannel channel,
    String uid) throws PortalException {
        super(channel, uid);
        if (!(channel instanceof IMultithreadedMimeResponse)) {
            throw  (new PortalException("MultithreadedPrivilegedCacheableMimeResponseChannelAdapter: Cannot adapt "
            + channel.getClass().getName()));
        }
    }
    public void setPortalControlStructures(PortalControlStructures pcs)
    throws PortalException {
        ((IMultithreadedPrivileged)channel).setPortalControlStructures(pcs,uid);
    }
}
