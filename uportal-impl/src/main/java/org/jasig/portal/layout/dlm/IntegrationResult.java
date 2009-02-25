/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm;

/**
 * This class is used to keep track of changes when integrating PLF and ILF
 * components.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
 
public class IntegrationResult
{
    public static final String RCS_ID = "@(#) $Header$";

    public boolean changedPLF = false;
    public boolean changedILF = false;
}
