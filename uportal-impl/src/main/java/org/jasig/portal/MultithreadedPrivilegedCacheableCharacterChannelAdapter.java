/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

/**
 * Internal adapter for a multithreaded character channel that is also both privileged and cacheable.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}, <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version $Revision$
 * @see MultithreadedChannelAdapter
 */
public class MultithreadedPrivilegedCacheableCharacterChannelAdapter extends MultithreadedCacheableCharacterChannelAdapter
implements IPrivilegedChannel {
    public MultithreadedPrivilegedCacheableCharacterChannelAdapter(IMultithreadedCharacterChannel channel, String uid) {
        super(channel, uid);
    }
    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
        ((IMultithreadedPrivileged)channel).setPortalControlStructures(pcs, uid);
    }
}
