/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

import java.util.*;

/** 
 * Used to store static properties of the channel.
 * Current version gathers the following information:
 * <ul> 
 *  <li> Name </li>
 *  <li> isMinimizable </li>
 *  <li> isDetachable </li>
 *  <li> isRemovable </li>
 *  <li> isEditable </li>
 *  <li> hasHelp </li>
 *  <li> DefaultDetachWidth </li>
 *  <li> DefaultDetachHeight </li>
 * </ul>
 * @version $Revision 1.1$ 
 * @author Peter Kharchenko
 */

public class ChannelSubscriptionProperties 
{
  private String str_name;

  // I leave those as strings, just in case we'll want to use some weird format
  private String str_defaultDetachWidth;
  private String str_defaultDetachHeight;

  private boolean bool_isMinimizable;
  private boolean bool_isDetachable;
  private boolean bool_isRemovable;
  private boolean bool_isEditable;
  private boolean bool_hasHelp;

  public ChannelSubscriptionProperties() 
  {
    // set the default values here ... I've chosen something safe for now
    str_name="";
    str_defaultDetachWidth="200";
    str_defaultDetachHeight="100";
    bool_isMinimizable=true;
    bool_isDetachable=true;
    bool_isRemovable=true;
    bool_isEditable=false;
    bool_hasHelp=false;
  }

  // the set functions
  public void setName(String n) { str_name=n; }
  public void setDefaultDetachWidth(String ddw) { str_defaultDetachWidth=ddw;}
  public void setDefaultDetachHeight(String ddh) { str_defaultDetachHeight=ddh;}
  public void setMinimizable(boolean value) { bool_isMinimizable=value; }
  public void setDetachable(boolean value) { bool_isDetachable=value; }
  public void setRemovable(boolean value) { bool_isRemovable=value; }
  public void setEditable(boolean value) { bool_isEditable=value; }
  public void setHasHelp(boolean value) { bool_hasHelp=value; }

  // the get functions
  public String getName() { return str_name; }
  public String getDefaultDetachWidth() { return str_defaultDetachWidth; }
  public String getDefaultDetachHeight() { return str_defaultDetachHeight; }
  public boolean isMinimizable() {return bool_isMinimizable; } 
  public boolean isDetachable() { return bool_isDetachable; }
  public boolean isRemovable() { return bool_isRemovable; }
  public boolean isEditable() { return bool_isEditable; }
  public boolean hasHelp() { return bool_hasHelp; }

}
