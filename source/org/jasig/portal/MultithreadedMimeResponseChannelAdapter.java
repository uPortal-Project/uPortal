/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Internal adapter for a multithreaded channel that also
 * implements IMimeResponse (capable of using DonwloadWorker)
 * @author Alex Vigdor
 * @version $Revision$
 * @see MultithreadedChannelAdapter
 */

public class MultithreadedMimeResponseChannelAdapter extends MultithreadedChannelAdapter
        implements IMimeResponse {

    public MultithreadedMimeResponseChannelAdapter (IMultithreadedChannel channel,
            String uid) throws PortalException
    {
        super(channel, uid);
        if (!(channel instanceof IMultithreadedMimeResponse)) {
            throw  (new PortalException("MultithreadedMimeResponseChannelAdapter: Cannot adapt "
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



