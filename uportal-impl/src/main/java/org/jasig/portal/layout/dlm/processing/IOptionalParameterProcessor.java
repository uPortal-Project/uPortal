/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm.processing;

/**
 * An interface implemented along with IParameterProcessor to convey if a call
 * to IParameterProcessor's processParameters() method has resulted in a
 * processor completing its functionality and hence can be removed from being a
 * currently selected optional processor. This can alleviate a requirement of
 * sending a specific URL solely to remove a processor that has completed its
 * task. Once selected, an optional processor remains as the currently selected
 * processor for all future HTTP requests until it conveys that it is finished
 * via implementing this interface or it is replaced by a subsequent HTTP
 * request including the uP_dlmPrc parameter.
 *
 * @author Mark Boyd
 *
 */
public interface IOptionalParameterProcessor
{
    /**
     * Answers whether a currently selected optional parameter processor has
     * completed its work and can be removed from being the selected optional
     * processor.
     *
     * @return boolean
     */
    public boolean isFinished();
}
