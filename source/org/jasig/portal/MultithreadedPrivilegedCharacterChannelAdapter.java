package org.jasig.portal;
/**
 * Internal adapter for a multithreaded character channel that is also privileged.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 * @see MultithreadedCharacterChannelAdapter
 */
public class MultithreadedPrivilegedCharacterChannelAdapter extends MultithreadedCharacterChannelAdapter
implements IPrivilegedChannel {
    public MultithreadedPrivilegedCharacterChannelAdapter(IMultithreadedCharacterChannel channel, String uid) {
        super(channel, uid);
    }
    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
        ((IPrivileged)channel).setPortalControlStructures(pcs);
    }
}
