/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.tools.chanpub;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jasig.portal.ChannelDefinition;

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
    public ChannelDefinition publishChannel(File filename) throws Exception;

    /**
     * Publishes the channel represented by the XML accessed via the passed in
     * InputStream object and returns the resultant ChannelDefinition object.
     *
     * @param is and InputStream containing the channel XML definition
     * @return org.jasig.portal.ChannelDefinition the published channel definition
     * @throws Exception
     */
    public ChannelDefinition publishChannel(InputStream is) throws Exception;

    public void setOverride(boolean b);

    public File[] parseCommandLine(String[] args) throws ParseException;

}