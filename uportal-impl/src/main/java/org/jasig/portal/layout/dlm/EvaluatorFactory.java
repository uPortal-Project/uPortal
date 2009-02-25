/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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

    public Evaluator getEvaluator( Node audience );
}
