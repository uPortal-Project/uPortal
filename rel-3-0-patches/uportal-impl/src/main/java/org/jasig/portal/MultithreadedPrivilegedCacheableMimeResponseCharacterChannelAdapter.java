/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

/**
 * Internal adapter for a multithreaded privileged character channel that is
 * also cacheable and implements IMimeResponse (capable of using DonwloadWorker)
 * @author  <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version $Revision$
 * @see MultithreadedPrivilegedCacheableChannelAdapter
 */
public class MultithreadedPrivilegedCacheableMimeResponseCharacterChannelAdapter
extends MultithreadedCacheableMimeResponseCharacterChannelAdapter
implements IPrivilegedChannel {
    public MultithreadedPrivilegedCacheableMimeResponseCharacterChannelAdapter (IMultithreadedCharacterChannel channel,
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
