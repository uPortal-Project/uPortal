/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal;

/**
 * A channel type.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class ChannelType {

  private int channelTypeId;
  private String javaClass;
  private String name;
  private String descr;
  private String cpdUri;

  /**
   * Constructs a channel type.
   * @param channelTypeId the channel type ID
   * @param javaClass the fully-qualifed java class name of the channel
   * @param name the name of the channel type
   * @param descr the description of the channel type
   * @param cpdUri the path to the channel publishing document, an XML file ending in .cpd
   * @throws java.lang.Exception
   */
  public ChannelType(int channelTypeId, String javaClass, String name, String descr, String cpdUri) {
    this.channelTypeId = channelTypeId;
    this.javaClass = javaClass;
    this.name = name;
    this.descr = descr;
    this.cpdUri = cpdUri;
  }

  // Getter methods
  public int getChannelTypeId() { return channelTypeId; }
  public String getJavaClass() { return javaClass; }
  public String getName() { return name; }
  public String getDescription() { return descr; }
  public String getCpdUri() { return cpdUri; }

  // Setter methods
  public void setChannelTypeId(int channelTypeId) { this.channelTypeId = channelTypeId; }
  public void setJavaClass(String javaClass) { this.javaClass = javaClass; }
  public void setName(String name) { this.name = name; }
  public void setDescription(String descr) { this.descr = descr; }
  public void setCpdUri(String cpdUri) { this.cpdUri = cpdUri; }

}