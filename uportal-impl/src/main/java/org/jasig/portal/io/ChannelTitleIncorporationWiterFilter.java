/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.io;

import java.io.Writer;

import org.jasig.portal.ChannelManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;

/**
 * Provides for streaming token replacement with a character stream.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ChannelTitleIncorporationWiterFilter extends AbstractTokenReplacementFilter {
    public static final String TITLE_TOKEN_PREFIX = "UP:CHANNEL_TITLE-{";
    public static final int MAX_CHANNEL_ID_LENGTH = 32;
    public static final String TITLE_TOKEN_SUFFIX = "}";
    
    private final ChannelManager channelManager;
    private final IUserLayoutManager userLayoutManager;

    /**
     * @param wrappedWriter Writer to delegate writing to.
     * @param channelManager Used to load the dynamic channel title.
     * @param userLayoutManager Used to access the default title if no dynamic title is provided.
     */
    public ChannelTitleIncorporationWiterFilter(Writer wrappedWriter, ChannelManager channelManager, IUserLayoutManager userLayoutManager) {
        super(wrappedWriter, TITLE_TOKEN_PREFIX, MAX_CHANNEL_ID_LENGTH, TITLE_TOKEN_SUFFIX);
        this.channelManager = channelManager;
        this.userLayoutManager = userLayoutManager;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.AbstractTokenReplacementFilter#replaceToken(java.lang.String)
     */
    @Override
    protected String replaceToken(String channelId) {
        final IUserLayoutChannelDescription channelNode = (IUserLayoutChannelDescription)this.userLayoutManager.getNode(channelId);
        final boolean disableDynamicTitle = Boolean.valueOf(channelNode.getParameterValue("disableDynamicTitle"));
        
        String title = null;
        if (!disableDynamicTitle) {
            title = this.channelManager.getChannelTitle(channelId);
        }
        
        if (title == null) {
            title = channelNode.getTitle();
        }
        
        return title;
    }
}
