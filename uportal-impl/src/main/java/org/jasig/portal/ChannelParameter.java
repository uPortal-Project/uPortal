/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Represents the channel parameters that are part of a ChannelDefinition.  
 * ChannelParameters define named parameters along with values for those
 * parameters and an indication of whether it is permissible to override those
 * default values.
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$ $Date$
 */

/**
   * Describes a channel definition parameter.
   * A channel can have zero or more parameters.
   */
  public class ChannelParameter {
    
    /** The name of the parameter. */
    String name;
    
    /** A default value for the parameter. */
    String value;
    
    /** True if the default value may be overridden. */
    boolean override;
    
    /** A description of the parameter. */
    String descr;

    /**
     * Instantiate a ChannelParameter with a particular name, default value,
     * and indication of whether it can be overridden.
     * @param name name of the channel parameter.
     * @param value default value for the parameter.
     * @param override true if the default value may be overridden.
     */
    public ChannelParameter(String name, String value, boolean override) {
      this.name = name;
      this.value = value;
      this.override = override;
    }

    // Getter methods
    
    /**
     * Get the name of the channel parameter.
     * @return the name of the channel parameter.
     */
    public String getName() { return this.name; }
    
    /**
     * Get the default value of the channel parameter.
     * @return the default value for this channel parameter.
     */
    public String getValue() { return this.value; }
    
    /**
     * Get whether the value of this channel parameter may be overridden.
     * @return true if value may be overridden, false otherwise.
     */
    public boolean getOverride() { return this.override; }
    
    /**
     * Get a description of this channel parameter.
     * @return a description of this channel parameter.
     */
    public String getDescription() {return this.descr; }

    // Setter methods
    
    /**
     * Set the name of the channel parameter.
     * @param name the name of the channel parameter
     */
    public void setName(String name) { this.name = name; }
    
    /**
     * Set the default value for this channel parameter.
     * @param value the default value for this channel parameter.
     */
    public void setValue(String value) { this.value = value; }
    
    /**
     * Set whether this channel parameter may be overridden.
     * @param override true if the channel parameter may be overridden.
     */
    public void setOverride(boolean override) { this.override = override; }
    
    /**
     * Set the description of this channel parameter.
     * @param descr description of this channel parameter.
     */
    public void setDescription(String descr) { this.descr = descr; }
  }