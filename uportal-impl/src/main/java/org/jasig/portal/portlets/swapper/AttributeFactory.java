/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

import java.io.Serializable;

import org.apache.commons.collections.Factory;

/**
 * commons-collections Factory that creates new {@link Attribute}s
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class AttributeFactory implements Factory, Serializable {
    private static final long serialVersionUID = 1L;

    public Attribute create() {
        return new Attribute();
    }
}