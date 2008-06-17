/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Internal adapter for a multithreaded character channel that is also privileged.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}, <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version $Revision$
 * @see MultithreadedCharacterChannelAdapter
 */
public class MultithreadedPrivilegedCharacterChannelAdapter extends MultithreadedCharacterChannelAdapter
implements IPrivilegedChannel {
    public MultithreadedPrivilegedCharacterChannelAdapter(IMultithreadedCharacterChannel channel, String uid) {
        super(channel, uid);
    }
    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
        ((IMultithreadedPrivileged)channel).setPortalControlStructures(pcs,uid);
    }
}
