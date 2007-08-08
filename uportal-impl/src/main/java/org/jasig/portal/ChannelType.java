/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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