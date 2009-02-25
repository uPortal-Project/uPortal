/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security.provider;

import java.io.Serializable;

import org.jasig.portal.security.*;

import javax.security.auth.*;

/**
 * Simple container object for a JAAS Subject for the logged in user.
 * 
 * @author Al Wold
 * @version $Revision$
 *
 */
public class JAASSubject implements IAdditionalDescriptor, Serializable {
   private Subject subject;
   
   public JAASSubject(Subject subject) {
      this.subject = subject;
   }
   
   public Subject getSubject() {
      return subject;
   }
}
