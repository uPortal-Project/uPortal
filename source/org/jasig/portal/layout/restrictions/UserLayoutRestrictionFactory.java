/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions;

import org.jasig.portal.PortalException;

/**
 * UserLayoutRestrictionFactory summary description sentence goes here.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public class UserLayoutRestrictionFactory {

 public static IUserLayoutRestriction createRestriction( int restrictionType, String restrictionValue, String restrictionPath ) throws PortalException {
    IUserLayoutRestriction restriction = null;
    switch ( restrictionType ) {
      case RestrictionTypes.DEPTH_RESTRICTION:
        restriction = new DepthRestriction(restrictionPath);
        break;
      case RestrictionTypes.GROUP_RESTRICTION:
        restriction = new GroupRestriction(restrictionPath);
        break;
      case RestrictionTypes.HIDDEN_RESTRICTION:
        restriction = new HiddenRestriction(restrictionPath);
        break;
      case RestrictionTypes.IMMUTABLE_RESTRICTION:
        restriction = new ImmutableRestriction(restrictionPath);
        break;
      case RestrictionTypes.PRIORITY_RESTRICTION:
        restriction = new PriorityRestriction(restrictionPath);
        break;
      case RestrictionTypes.UNREMOVABLE_RESTRICTION:
        restriction = new UnremovableRestriction(restrictionPath);
        break;
      default:
        restriction = new UnremovableRestriction(restrictionPath);
    }
        restriction.setRestrictionExpression(restrictionValue);
        return restriction;
  }

}