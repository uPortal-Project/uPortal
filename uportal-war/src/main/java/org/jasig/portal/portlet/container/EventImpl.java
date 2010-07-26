/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.container;

import java.io.Serializable;

import javax.portlet.Event;
import javax.xml.namespace.QName;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EventImpl implements Event {

    private QName _qname;
    private Serializable _value;
    
    public EventImpl(QName qname){
        _qname = qname;
    }
    
    public EventImpl(QName qname, Serializable value){
        this(qname);
        _value = value;
    }

    public QName getQName() {
        return _qname;
    }

    public Serializable getValue() {
        return _value;
    }

    public String getName() {
        return _qname.getLocalPart();
    }
}
