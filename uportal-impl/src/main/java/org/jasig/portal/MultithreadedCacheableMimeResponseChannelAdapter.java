/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package  org.jasig.portal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Internal adapter for a multithreaded channel that is also cacheable and
 * implements IMimeResponse (capable of using DonwloadWorker)
 * @author Alex Vigdor
 * @version $Revision$
 * @see MultithreadedCacheableChannelAdapter
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class MultithreadedCacheableMimeResponseChannelAdapter extends MultithreadedCacheableChannelAdapter
        implements IMimeResponse {

    public MultithreadedCacheableMimeResponseChannelAdapter (IMultithreadedChannel channel,
            String uid) throws PortalException
    {
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



