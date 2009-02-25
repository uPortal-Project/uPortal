/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets;

import java.io.Serializable;

import org.apache.commons.collections.Factory;

/**
 * commons-collections Factory that creates new {@link Attribute}s
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AttributeFactory implements Factory, Serializable {
    private static final long serialVersionUID = 1L;

    public Attribute create() {
        return new Attribute();
    }
}