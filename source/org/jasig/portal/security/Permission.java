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
 */


package  org.jasig.portal.security;

import java.util.Date;


/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 * @deprecated As of uPortal 2.0, replaced by {@link IPermission}
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
   * @return owner of this Permission
   */
  public String getOwner () {
    return  (m_owner);
  }

  /**
   * Returns the principal that this object is bound to
   * @return principal that this object is bound to
   */
  public abstract String getPrincipal ();

  /**
   * Sets the principal that this object is bound to
   * @param principal
   */
  public abstract void setPrincipal (String principal);

  /**
   * Gets the activity that this Permission is associated with
   * @return activity that this Permission is associated with
   */
  public abstract String getActivity ();

  /**
   * Sets the activity that this Permission is associated with
   * @param activity
   */
  public abstract void setActivity (String activity);

  /**
   * Gets the target that this Permission is associated with
   * @return target that this Permission is associated with
   */
  public abstract String getTarget ();

  /**
   * Gets the target that this Permission is associated with
   * @param target
   */
  public abstract void setTarget (String target);


    /**
     * Returns permission type.
     *
     * @return a <code>String</code> value
     */
    public abstract String getType ();

 
    /**
     * Sets permission type.
     *
     * @param type a <code>String</code> value
     */
    public abstract void setType (String type);

  /**
   * Gets that date that this Permission should become effective on
   * @return date that this Permission should become effective on
   */
  public abstract Date getEffective ();

  /**
   * Sets the date that this Permission should become effective on
   * @param effective
   */
  public abstract void setEffective (Date effective);

  /**
   * Gets the date that this Permission should expire on
   * @return date that this Permission should expire on
   */
  public abstract Date getExpires ();

  /**
   * Sets the date that this Permission should expire on
   * @param expires
   */
  public abstract void setExpires (Date expires);
}



