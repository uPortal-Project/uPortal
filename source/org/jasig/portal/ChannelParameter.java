/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * <p>Title: ChannelParameter class</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic</p>
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.0
 */

/**
   * Describes a channel definition parameter
   * A channel can have zero or more parameters.
   */

  public class ChannelParameter {
    String name;
    String value;
    boolean override;
    String descr;

    public ChannelParameter(String name, String value, String override) {
      this(name, value, RDBMServices.dbFlag(override));
    }

    public ChannelParameter(String name, String value, boolean override) {
      this.name = name;
      this.value = value;
      this.override = override;
    }

    // Getter methods
    public String getName() { return name; }
    public String getValue() { return value; }
    public boolean getOverride() { return override; }
    public String getDescription() {return descr; }

    // Setter methods
    public void setName(String name) { this.name = name; }
    public void setValue(String value) { this.value = value; }
    public void setOverride(boolean override) { this.override = override; }
    public void setDescription(String descr) { this.descr = descr; }
  }