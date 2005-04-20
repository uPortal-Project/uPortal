
package org.jasig.portal.layout.dlm;

import org.w3c.dom.Node;

public interface EvaluatorFactory
{
    public static final String RCS_ID = "@(#) $Header$";

    public Evaluator getEvaluator( Node audience )
    throws Exception;
}
