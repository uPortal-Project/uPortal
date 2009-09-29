/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets;

import java.io.Serializable;

import org.apache.commons.collections.Factory;

public class BooleanAttributeFactory implements Factory, Serializable {
    private static final long serialVersionUID = 1L;

    public BooleanAttribute create() {
        return new BooleanAttribute();
    }
}