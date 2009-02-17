/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.providers;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Cascade;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.security.IPerson;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
@Entity
public class Paren extends Evaluator {
    
    // Static Members.
    public static final String RCS_ID = "@(#) $Header$";
    private static Log LOG = LogFactory.getLog(Paren.class);

    public static final ParenType OR  = new ParenType( "OR" );
    public static final ParenType AND = new ParenType( "AND" );
    public static final ParenType NOT = new ParenType( "NOT" );

    // Instance Members.
    private String type = null;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @Cascade({org.hibernate.annotations.CascadeType.DELETE_ORPHAN, org.hibernate.annotations.CascadeType.ALL })
    private List<Evaluator> evaluators = new LinkedList<Evaluator>();

    public Paren() {}

    public Paren(ParenType t) {
        type = t.toString();
    }

    public void addEvaluator(Evaluator e) {
        this.evaluators.add(e);
    }

    public boolean isApplicable( IPerson toPerson )
    {
        boolean rslt = false;
        if (LOG.isDebugEnabled())
            LOG.debug(" >>>> calling paren[" + this + ", op=" + type + 
                    "].isApplicable()");
        
        if ( type.equals(OR) )
        {
            rslt = false;   // presume false in this case...
            for(Evaluator v : this.evaluators)
                if ( v.isApplicable( toPerson ) )
                {
                    rslt = true;
                    break;
                }
        }
        else if ( type.equals(AND) )
        {
            rslt = true;   // presume true in this case...
            for(Evaluator v : this.evaluators)
                if ( v.isApplicable( toPerson ) == false )
                {
                    rslt = false;
                    break;
                }
        }
        else if ( type.equals(NOT) )
        {
            rslt = false;   // presume false in this case... until later...
            for(Evaluator v : this.evaluators)
                if ( v.isApplicable( toPerson ) )
                {
                    rslt = true;
                    break;
                }
            rslt = !rslt;
        }
        
        if (LOG.isDebugEnabled())
            LOG.debug(" ---- paren[" + this + ", op=" + type
                    + "].isApplicable()=" + rslt);
        return rslt;
    }

    @Override
    public void toElement(Element parent) {
        
        // Assertions.
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        // NB:  This method behaves vastly different depending on whether 
        // the parent of this Paren is an instance of FragmentDefinition.
        Element rslt = null;
        if (parent.getName().equals("fragment")) {
            
            // The parent is a fragment, so we render as a <dlm:audience> element...
            QName q = new QName("audience", FragmentDefinition.NAMESPACE);
            rslt = DocumentHelper.createElement(q);
            
            // Discover the EvaluatorFactory class...
            rslt.addAttribute("evaluatorFactory", this.getFactoryClass().getName());
                        
        } else {
            
            // The parent is *not* a fragment, so we render as a <paren> element...
            rslt = DocumentHelper.createElement("paren");            
            rslt.addAttribute("mode", this.type.toString());

        }
        
        // Serialize our children...
        for (Evaluator v : this.evaluators) {
            v.toElement(rslt);
        }
        
        // Append ourself...
        parent.add(rslt);

    }
    
    @Override
    public Class<? extends EvaluatorFactory> getFactoryClass() {
        return this.evaluators.get(0).getFactoryClass();
    }


}

class ParenType
{
    String type = null;
    
    public ParenType( String type )
    {
        this.type = type;
    }
    public String toString()
    {
        return type;
    }
}
