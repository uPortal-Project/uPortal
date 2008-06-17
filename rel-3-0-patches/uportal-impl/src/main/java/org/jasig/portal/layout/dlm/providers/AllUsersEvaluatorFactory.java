/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.providers;

import org.w3c.dom.Node;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.security.IPerson;

/**
 * Used to target a fragment to all users of the system including guest users.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class AllUsersEvaluatorFactory
    implements EvaluatorFactory, Evaluator
{
    public static final String RCS_ID = "@(#) $Header$";

    public Evaluator getEvaluator( Node audience )
    {
        return this;
    }
    public boolean isApplicable( IPerson p )
    {
        return true;
    }
}
