/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm.providers;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Node;

/**
 * Used to target a fragment only to guest users.
 * 
 * @author mboyd@sungardsct.com
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
@Entity
public class GuestUserEvaluatorFactory extends Evaluator implements EvaluatorFactory {
    
    public static final String RCS_ID = "@(#) $Header$";

    @Transient
    private final Log log = LogFactory.getLog(getClass());

    public Evaluator getEvaluator( Node audience )
    {
        return this;
    }
    public boolean isApplicable( IPerson p )
    {
        if (log.isDebugEnabled()) {
            log.debug("Calling isApplicable():  username=" + p.getUserName() 
                                            + ",isGuest=" + p.isGuest());
        }
        return p.isGuest();
    }

    @Override
    public void toElement(Element parent) {
        
        // Assertions.
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Element rslt = null;
        QName q = new QName("audience", FragmentDefinition.NAMESPACE);
        rslt = DocumentHelper.createElement(q);
        rslt.addAttribute("evaluatorFactory", this.getFactoryClass().getName());
        parent.add(rslt);

    }
    
    @Override
    public Class<? extends EvaluatorFactory> getFactoryClass() {
        return this.getClass();
    }


}
