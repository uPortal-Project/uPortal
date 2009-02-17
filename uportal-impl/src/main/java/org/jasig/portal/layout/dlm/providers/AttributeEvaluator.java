/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.providers;

import javax.persistence.Entity;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.security.IPerson;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
@Entity
public class AttributeEvaluator
    extends Evaluator
{
    public static final String RCS_ID = "@(#) $Header$";

    public static final int CONTAINS = 0;
    public static final int EQUALS = 1;
    public static final int STARTS_WITH = 2;
    public static final int ENDS_WITH = 3;
    public static final int EXISTS = 4;

    protected int mode = -1;
    protected String name = null;
    protected String value = null;

    /**
     * Zero-arg constructor required by JPA.  Other Java code should not use it.
     */
    public AttributeEvaluator() {}
    
    public AttributeEvaluator( String name, String mode, String value )
    {
        if ( mode.equals( "equals" ) )
        {
            this.mode = EQUALS;
            if ( value == null )
                throw new RuntimeException("Missing value attribute"
                        + ". For mode of 'equals' value must be defined.");
        }
        else if ( mode.equals( "exists" ) )
            this.mode = EXISTS;
        else if ( mode.equals( "contains" ) )
        {
            this.mode = CONTAINS;
            if ( value == null || value.equals( "" ) )
                throw new RuntimeException("Missing or invalid value attribute"
                        + ". For mode of 'contains' value "
                        + "must be defined and not empty");
        }
        else if ( mode.equals( "startsWith" ) )
        {
            this.mode = STARTS_WITH;
            if ( value == null || value.equals( "" ) )
                throw new RuntimeException(
                        "Missing or invalid value attribute. "
                                + "For mode of 'startsWith' value must "
                                + "be defined and not empty");
        }
        else if ( mode.equals( "endsWith" ) )
        {
            this.mode = ENDS_WITH;
            if ( value == null || value.equals( "" ) )
                throw new RuntimeException(
                        "Missing or invalid value attribute. "
                                + "For mode of 'endsWith' value must be "
                                + "defined and not empty");
        }
        else
            throw new RuntimeException("Invalid mode attribute. Expected mode "
                    + "of 'contains', 'equals', 'startsWith', "
                    + "'exists', or 'endsWith'");
            
        this.name = name;
        this.value = value;
    }
    
    public boolean isApplicable( IPerson p )
    {
        String attrib = (String) p.getAttribute( name );

        // for tests other than 'exists' the attribute must be defined
        if ( attrib == null && mode != EXISTS )
            return false;

        if ( mode == EQUALS )
            return attrib.equals( value );
        if ( mode == EXISTS )
            return attrib != null;
        if ( mode == STARTS_WITH )
            return attrib.startsWith( value );
        if ( mode == ENDS_WITH )
            return attrib.endsWith( value );
        if ( mode == CONTAINS )
            return (attrib.indexOf( value ) != -1 );
        // will never get here
        return false;
    }
    
    @Override
    public void toElement(Element parent) {

        // Assertions.
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        String mde = null;
        switch (this.mode) {
            case AttributeEvaluator.CONTAINS:
                mde = "contains";
                break;
            case AttributeEvaluator.EQUALS:
                mde = "equals";
                break;
            case AttributeEvaluator.STARTS_WITH:
                mde = "startsWith";
                break;
            case AttributeEvaluator.ENDS_WITH:
                mde = "endsWith";
                break;
            case AttributeEvaluator.EXISTS:
                mde = "exists";
                break;
            default:
                throw new IllegalStateException("Unrecognized mode constant:  " + this.mode);
        }
        
        Element rslt = DocumentHelper.createElement("attribute");
        rslt.addAttribute("name", this.name);
        rslt.addAttribute("mode", mde);
        rslt.addAttribute("value", this.value);
        parent.add(rslt);
        
    }
    
    @Override
    public Class<? extends EvaluatorFactory> getFactoryClass() {
        return PersonEvaluatorFactory.class;
    }


}
