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
import  java.sql.Timestamp;


/**
 * An implementation of IInitialGroupContext
 */
public class InitialGroupContextImpl
      implements IInitialGroupContext {
   private String ownerID;
   private String ownerType;
   private String groupID;
   private int ordinal;
   private boolean expanded = false;
   private java.sql.Timestamp dateCreated;

   /**
    * put your documentation comment here
    */
   public InitialGroupContextImpl () {
   }

   /**
    * Constructor
    * @param owner
    * @param ownerType
    * @param group
    * @param ord
    * @param expanded
    * @param created
    */
   public InitialGroupContextImpl (String owner, String ownerType, String group, int ord,
         boolean expanded, Timestamp created) {
      super();
      ownerID = owner;
      setOwnerType(ownerType);
      setGroupID(group);
      setOrdinal(ord);
      setExpanded(expanded);
      dateCreated = created;
   }

   /**
    * Returns an IInitialGroupContextStore for the underlying implementation.
    * @return IInitialGroupContextStore
    * @exception ChainedException
    * @todo  return an IInitialGroupContextStore based upon an entry in
    *  portal.properties
    */
   private IInitialGroupContextStore getFactory () throws ChainedException {
      return  RDBMInitialGroupContextStore.singleton();
   }

   /**
    * Returns the ownerID
    * @return String
    */
   public String getOwnerID () {
      return  ownerID;
   }

   /**
    * Returns the ownerType (g=group, p=person)
    * @return String
    */
   public String getOwnerType () {
      return  ownerType;
   }

   /**
    * Sets the ownerType
    * @param ownerTyp
    */
   public void setOwnerType (String ownerTyp) {
      ownerType = ownerTyp;
   }

   /**
    * Returns the groupID
    * @return String
    */
   public String getGroupID () {
      return  groupID;
   }

   /**
    * Sets the groupID
    * @param group
    */
   public void setGroupID (String group) {
      groupID = group;
   }

   /**
    * Returns the ordinal
    * @return int
    */
   public int getOrdinal () {
      return  ordinal;
   }

   /**
    * Set the ordinal
    * @param ord
    */
   public void setOrdinal (int ord) {
      ordinal = ord;
   }

   /**
    * Returns expanded
    * @return boolean
    */
   public boolean isExpanded () {
      return  expanded;
   }

   /**
    * Returns dateCreated
    * @return java.sql.Timestamp
    */
   public java.sql.Timestamp getDateCreated () {
      return  dateCreated;
   }

   /**
    * Sets expanded
    * @param newExpanded
    */
   public void setExpanded (boolean newExpanded) {
      expanded = newExpanded;
   }

   /**
    * Sets expanded
    * @param newExpanded
    */
   public void setExpanded (String newExpanded) {
      if (newExpanded != null && (newExpanded.equals("y") || newExpanded.equals("1"))) {
         expanded = true;
      }
      else {
         expanded = false;
      }
   }

   /**
    * Overrides Object toString
    * @return String
    */
   public String toString () {
      return  "Initial Group Context for : " + getOwnerID() + " OwnerType: " + getOwnerType()
            + " Group ID: " + getGroupID() + " Position: " + getOrdinal() + " Expanded: "
            + isExpanded() + " Created : " + getDateCreated();
   }

   /**
    * Defers to the InitialGroupsContextStore class to perform the update
    * @exception ChainedException
    */
   public void update () throws ChainedException {
      try {
         getFactory().update(this);
      } catch (Exception ex) {
         throw  new ChainedException(ex.toString(), ex);
      }
   }
}



