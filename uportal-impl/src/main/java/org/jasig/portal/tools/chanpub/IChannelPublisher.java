/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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