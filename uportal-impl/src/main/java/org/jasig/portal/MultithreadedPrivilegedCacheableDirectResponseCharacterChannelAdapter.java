/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net </a>
 * @version $Revision$
 */
public class MultithreadedPrivilegedCacheableDirectResponseCharacterChannelAdapter extends
        MultithreadedPrivilegedCacheableCharacterChannelAdapter implements IDirectResponse {

    public MultithreadedPrivilegedCacheableDirectResponseCharacterChannelAdapter(
            IMultithreadedCharacterChannel channel, String uid) throws PortalException {

        super(channel, uid);

        if (!(channel instanceof IMultithreadedDirectResponse)) {
            throw (new PortalException(
                    "MultithreadedPrivilegedCacheableDirectResponseCharacterChannelAdapter: Cannot adapt "
                            + channel.getClass().getName()));
        }
    }

    /**
     * @see org.jasig.portal.IMultithreadedDirectResponse#setResponse(String, javax.servlet.http.HttpServletResponse)
     */
    public void setResponse(HttpServletResponse response) {
        ((IMultithreadedDirectResponse) channel).setResponse(uid, response);
    }
}