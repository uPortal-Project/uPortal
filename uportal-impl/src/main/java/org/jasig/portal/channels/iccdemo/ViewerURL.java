/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.iccdemo;

/**
 * A simple proxy class that CViewer binds in its jndi context.
 * Class allows outsiders to invoke URL change.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class ViewerURL {
    private CViewer viewerChannel;
    public ViewerURL(CViewer channel) {
        this.viewerChannel=channel;
    }

    public void setNewURL(String newURL) {
        viewerChannel.changeURL(newURL);
    }
}
