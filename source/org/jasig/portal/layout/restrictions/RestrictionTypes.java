/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions;

/**
 * RestrictionTypes is an interface containing the restriction types.
 * 
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public interface RestrictionTypes {

  public static final int PRIORITY_RESTRICTION = 1;
  public static final int DEPTH_RESTRICTION = 2;
  public static final int GROUP_RESTRICTION = 4;
  public static final int IMMUTABLE_RESTRICTION = 8;
  public static final int UNREMOVABLE_RESTRICTION = 16;
  public static final int HIDDEN_RESTRICTION = 32;
  //public static final int REMOTE_RESTRICTION = 64;


}