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

 package  org.jasig.portal.channels.groupsmanager;


/**
 * <p>Title: uPortal</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Columbia University</p>
 * @author Don Fracapane
 * @version 2.0
 */
/**
 * Defines the interface for an Initial Groups Context object to be used by CGroupssManager
 */
public interface IInitialGroupContext {

   /**
    * Returns the ownerID
    * @return
    */
   public String getOwnerID ();

   /**
    * Returns the ownerType.
    * @return
    */
   public String getOwnerType ();

   /**
    * Returns the groupID
    * @return
    */
   public String getGroupID ();

   /**
    * Returns the ordinal used for display positioning.
    * @return
    */
   public int getOrdinal ();

   /**
    * Returns expanded
    * @return
    */
   public boolean isExpanded ();

   /**
    * Returns the dateCreated
    * @return
    */
   public java.sql.Timestamp getDateCreated ();

   /**
    * Sets the groupID
    * @param group
    */
   public void setGroupID (String group);

   /**
    * Sets the ownerType
    * @param ownerType
    */
   public void setOwnerType (String ownerType);

   /**
    * Sets the ordinal
    * @param ordinal
    */
   public void setOrdinal (int ordinal);

   /**
    * Sets expanded
    * @param boolean expanded
    */
   public void setExpanded (boolean expanded);

   /**
    * put your documentation comment here
    * @param String expanded
    */
   public void setExpanded (String expanded);

   /**
    * Makes the changes persistent.
    */
   public void update () throws ChainedException;
}
