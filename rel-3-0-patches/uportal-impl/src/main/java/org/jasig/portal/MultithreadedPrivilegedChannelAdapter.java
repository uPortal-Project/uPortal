/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Internal adapter for a multithreaded channel that is also privileged.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @version $Revision$
 * @see MultithreadedChannelAdapter
 */

public class MultithreadedPrivilegedChannelAdapter extends MultithreadedChannelAdapter implements IPrivilegedChannel {
    public MultithreadedPrivilegedChannelAdapter(IMultithreadedChannel channel, String uid) {
        super(channel,uid);
    }

    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
        ((IMultithreadedPrivileged)channel).setPortalControlStructures(pcs, uid);
    }
}
