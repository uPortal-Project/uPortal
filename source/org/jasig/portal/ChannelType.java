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
 * A channel type references a particular java class that implements
 * the IChannel interface.  It also references a channel publishing document
 * that describes the parameters that must be fed to the channel.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ChannelType {

  private int id;
  private String javaClass;
  private String name;
  private String descr;
  private String cpdUri;

  /**
   * Constructs a channel type.
   * @param id the channel type ID
   */
  public ChannelType(int id) {
    this.id = id;
  }

  // Getter methods
  public int getId() { return id; }
  public String getJavaClass() { return javaClass; }
  public String getName() { return name; }
  public String getDescription() { return descr; }
  public String getCpdUri() { return cpdUri; }

  // Setter methods
  public void setJavaClass(String javaClass) { this.javaClass = javaClass; }
  public void setName(String name) { this.name = name; }
  public void setDescription(String descr) { this.descr = descr; }
  public void setCpdUri(String cpdUri) { this.cpdUri = cpdUri; }

}