/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.tools.chanpub;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.cli.ParseException;
import org.jasig.portal.channel.IChannelDefinition;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IChannelPublisher {

    /**
     * Publishes the channel represented by the XML located in the file
     * represented by the passed in filename and returns the resultant
     * ChannelDefinition object.
     *
     * @param filename the name of a file containing the channel XML definition
     * @return org.jasig.portal.ChannelDefinition the published channel definition
     * @throws Exception
     */
    public IChannelDefinition publishChannel(File filename) throws Exception;

    /**
     * Publishes the channel represented by the XML accessed via the passed in
     * InputStream object and returns the resultant ChannelDefinition object.
     *
     * @param is and InputStream containing the channel XML definition
     * @return org.jasig.portal.ChannelDefinition the published channel definition
     * @throws Exception
     */
    public IChannelDefinition publishChannel(InputStream is) throws Exception;

    public void setOverride(boolean b);

    public File[] parseCommandLine(String[] args) throws ParseException;

}