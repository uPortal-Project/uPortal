/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Internal adapter for a multithreaded character channel that is also privileged.
 * @author  <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version $Revision$
 * @see MultithreadedChannelAdapter
 */
public class MultithreadedPrivilegedMimeResponseCharacterChannelAdapter extends
MultithreadedMimeResponseCharacterChannelAdapter implements IPrivilegedChannel {
    public MultithreadedPrivilegedMimeResponseCharacterChannelAdapter(IMultithreadedCharacterChannel channel, String uid)
    throws PortalException {
        super(channel, uid);
    }
    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
        ((IMultithreadedPrivileged)channel).setPortalControlStructures(pcs,uid);
    }
}
