/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.jasig.portal.security.IPerson;

public interface Evaluator
{
    public static final String RCS_ID = "@(#) $Header$";

    public boolean isApplicable( IPerson person );
}
