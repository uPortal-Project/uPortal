/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.w3c.dom.Node;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public interface EvaluatorFactory
{
    public static final String RCS_ID = "@(#) $Header$";

    public Evaluator getEvaluator( Node audience )
    throws Exception;
}
