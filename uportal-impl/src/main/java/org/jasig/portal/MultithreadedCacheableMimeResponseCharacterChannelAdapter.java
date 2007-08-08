/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Internal adapter for a multithreaded character channel that is also
 * cacheable and implements IMimeResponse (capable of using DonwloadWorker)
 * @author <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version $Revision$
 * @see MultithreadedCacheableChannelAdapter
 */
public class MultithreadedCacheableMimeResponseCharacterChannelAdapter extends MultithreadedCacheableCharacterChannelAdapter
implements IMimeResponse {
    public MultithreadedCacheableMimeResponseCharacterChannelAdapter (IMultithreadedCharacterChannel channel,
    String uid) throws PortalException {
        super(channel, uid);
        if (!(channel instanceof IMultithreadedMimeResponse)) {
            throw  (new PortalException("MultithreadedCacheableMimeResponseChannelAdapter: Cannot adapt "
            + channel.getClass().getName()));
        }
    }
    public String getContentType () {
        return  ((IMultithreadedMimeResponse)channel).getContentType(uid);
    }
    public InputStream getInputStream () throws IOException {
        return  ((IMultithreadedMimeResponse)channel).getInputStream(uid);
    }
    public void downloadData (OutputStream out) throws IOException {
        ((IMultithreadedMimeResponse)channel).downloadData(out, uid);
    }
    public String getName () {
        return  ((IMultithreadedMimeResponse)channel).getName(uid);
    }
    public Map getHeaders () {
        return  ((IMultithreadedMimeResponse)channel).getHeaders(uid);
    }

    public void reportDownloadError(Exception e) {
      ((IMultithreadedMimeResponse)channel).reportDownloadError(e);
    }
}
