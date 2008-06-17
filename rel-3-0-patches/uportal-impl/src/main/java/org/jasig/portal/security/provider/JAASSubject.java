/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
