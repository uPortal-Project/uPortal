/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions.alm;

/**
 * RestrictionTypes is an interface containing the restriction types.
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.restrictions.
 * It was moved to its present package to reflect that it is part of Aggregated Layouts.
 * 
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public interface RestrictionTypes {

  public static final String PRIORITY_RESTRICTION = "priority";
  public static final String DEPTH_RESTRICTION = "depth";
  public static final String GROUP_RESTRICTION = "group";
  public static final String IMMUTABLE_RESTRICTION = "immutable";
  public static final String UNREMOVABLE_RESTRICTION = "unremovable";
  public static final String HIDDEN_RESTRICTION = "hidden";

}