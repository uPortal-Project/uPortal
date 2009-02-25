/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
/*
 * Created on Dec 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jasig.portal.channels.jsp.tree;


/**
 * @author Mark Boyd
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IDomainActionSet
{
    /**
     * Returns an array of keys representing the supported actions that will
     * appear in the tree.
     * 
     * @return
     */
    String[] getSupportedActions();

    /**
     * Provides information needed by a custom renderer to render the labeling
     * of an action for a given domain object. This information is made 
     * accessible to the custom renderer JSP via:
     * 
     * <pre>
     *   ${requestScope.model.actionLabelData}
     * </pre>
     *       
     * @param action
     * @param domainObject
     * @return
     */
    Object getLabelData(String action, Object domainObject);
    
}
