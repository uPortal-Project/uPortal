/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.dao.usertype;

import org.hibernate.Hibernate;

/**
 * Version of BaseEscapedStringType that stores its data in a VARCHAR.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EscapedTextType extends BaseEscapedStringType {
    public EscapedTextType() {
        super(Hibernate.TEXT);
    }
}
