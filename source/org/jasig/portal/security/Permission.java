/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.security;

import  java.util.Date;
import  org.jasig.portal.AuthorizationException;


/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 */
public abstract class Permission {
  private String m_owner = null;

  /**
   * This constructor forces Permission objects to be created with an owner
   * @param owner
   */
  public Permission (String owner) {
    m_owner = owner;
  }

  /**
   * Returns the owner of this Permission
   * @return 
   */
  public String getOwner () {
    return  (m_owner);
  }

  /**
   * Returns the principal that this object is bound to
   * @return 
   */
  public abstract String getPrincipal () throws AuthorizationException;

  /**
   * Sets the principal that this object is bound to
   * @param principal
   */
  public abstract void setPrincipal (String principal) throws AuthorizationException;

  /**
   * Gets the activity that this Permission is associated with
   * @return 
   */
  public abstract String getActivity () throws AuthorizationException;

  /**
   * Sets the activity that this Permission is associated with
   * @param activity
   */
  public abstract void setActivity (String activity) throws AuthorizationException;

  /**
   * Gets the target that this Permission is associated with
   * @return 
   */
  public abstract String getTarget () throws AuthorizationException;

  /**
   * Gets the target that this Permission is associated with
   * @param target
   */
  public abstract void setTarget (String target) throws AuthorizationException;

  /**
   * Gets that date that this Permission should become effective on
   * @return 
   */
  public abstract Date getEffective () throws AuthorizationException;

  /**
   * Sets the date that this Permission should become effective on
   * @param effective
   */
  public abstract void setEffective (Date effective) throws AuthorizationException;

  /**
   * Gets the date that this Permission should expire on
   * @return 
   */
  public abstract Date getExpires () throws AuthorizationException;

  /**
   * Sets the date that this Permission should expire on
   * @param expires
   */
  public abstract void setExpires (Date expires) throws AuthorizationException;
}



