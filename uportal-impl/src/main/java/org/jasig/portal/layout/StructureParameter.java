/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout;

/**
 * This class began its life as a public inner class of RDBMUserLayoutStore.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5 before 2.5 this was an inner class of org.jasig.portal.RDBMUserLayoutStore.
 */
public class StructureParameter {
    // TODO: provide an intelligent Type comment for this object.

    /**
     * The parameter name.
     */
    private final String name;

    /**
     * The parameter value.
     */
    private final String value;

    /**
     * Create a new StructureParameter instance representing the
     * given name, value pair.
     * @param name the name of the parameter
     * @param value the value for the parameter
     */
    public StructureParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get the name of this parameter.
     * @return the name of this parameter.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the value of this parameter.
     * @return the value of the parameter.
     */
    public String getValue() {
        return this.value;
    }
}

