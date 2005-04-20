
package org.jasig.portal.layout.dlm.providers;

import org.w3c.dom.Node;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.security.IPerson;

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
