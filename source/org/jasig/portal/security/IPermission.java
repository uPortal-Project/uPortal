package org.jasig.portal.security;

/**
 * Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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

import java.util.Date;
import org.jasig.portal.AuthorizationException;


/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 * @author Dan Ellentuck
 * @version $Revision$ 
 */
public interface IPermission {

  /**
   * Gets the activity associated with this <code>IPermission</code>.
   * @return String
   */
  public String getActivity ();

  /**
   * Gets that date that this <code>IPermission</code> should become effective on.
   * @return 
   */
  public Date getEffective ();

  /**
   * Gets the date that this <code>IPermission</code> should expire on.
   * @return 
   */
  public Date getExpires ();

  /**
   * Returns the owner of this <code>IPermission</code>.
   * @return 
   */
  public String getOwner ();

  /**
   * Gets the target associated with this <code>IPermission</code>.
   * @return 
   */
  public String getTarget ();
  
  /**
   * Returns the <code>Permission</code> type.
   */
  public String getType ();
  
  /**
   * Sets the activity associated with this <code>IPermission</code>.
   * @param activity String
   */
  public void setActivity (String activity);
  
  /**
   * Sets the date that this <code>IPermission</code> should become effective on.
   * @param effective java.util.Date
   */
  public void setEffective (Date effective);
  
  /**
   * Sets the date that this <code>IPermission</code> should expire on.
   * @param expires java.util.Date
   */
  public void setExpires (Date expires);
 
  /**
   * Sets the target associated with this <code>IPermission</code>.
   * @param target
   */
  public void setTarget (String target);
  
  /**
   * Sets the <code>IPermission</code> type.
   * @param type String
   */
  public abstract void setType (String type);

  /**
   * Returns a String representing the <code>IAuthorizationPrincipal</code> associated
   * with this <code>IPermission</code>.
   * @return 
   */
  public String getPrincipal();

  /**
   * Sets the principal String representing the <code>IAuthorizationPrincipal</code> 
   * associated with this <code>IPermission</code>.
   * @param newPrincipal String
   */
  public void setPrincipal (String newPrincipal);
}
