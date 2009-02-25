/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.dao.usertype;

import org.hibernate.Hibernate;

/**
 * Version of BaseEscapedStringType that stores its data in a CLOB.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EscapedStringType extends BaseEscapedStringType {
    public EscapedStringType() {
        super(Hibernate.STRING);
    }
}
