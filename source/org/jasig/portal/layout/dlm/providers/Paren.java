/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.providers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.security.IPerson;

public class Paren
    implements Evaluator
{
    public static final String RCS_ID = "@(#) $Header$";
    private static Log LOG = LogFactory.getLog(Paren.class);

    public static final ParenType OR  = new ParenType( "OR" );
    public static final ParenType AND = new ParenType( "AND" );
    public static final ParenType NOT = new ParenType( "NOT" );

    private ParenType type = null;

    protected Evaluator[] evaluators = new Evaluator[] {};

    public Paren( ParenType t )
    {
        type = t;
    }

    public void addEvaluator( Evaluator e )
    {
        if ( evaluators == null )
            evaluators = new Evaluator[] { e };
        
        Evaluator[] arr = new Evaluator[ evaluators.length + 1 ];
        System.arraycopy( evaluators, 0, arr, 0, evaluators.length );
        arr[evaluators.length] = e;
        evaluators = arr;
    }

    public boolean isApplicable( IPerson toPerson )
    {
        boolean isApplicable = false;
        if (LOG.isDebugEnabled())
            LOG.debug(" >>>> calling paren[" + this + ", op=" + type + 
                    "].isApplicable()");
        if ( type == OR )
        {
            for( int i=0; i<evaluators.length; i++ )
                if ( evaluators[i].isApplicable( toPerson ) )
                {
                    isApplicable = true;
                    break;
                }
        }
        else if ( type == AND )
        {
            int i=0;
            for( ; i<evaluators.length; i++ )
                if ( evaluators[i].isApplicable( toPerson ) == false )
                {
                    isApplicable = false;
                    break;
                }
            if ( i == evaluators.length ) // ran to end without finding one
                isApplicable = true;
        }
        else if ( type == NOT )
        {
            for( int i=0; i<evaluators.length; i++ )
                if ( evaluators[i].isApplicable( toPerson ) )
                {
                    isApplicable = true;
                    break;
                }
            isApplicable = ! isApplicable;
        }
        if (LOG.isDebugEnabled())
            LOG.debug(" ---- paren[" + this + ", op=" + type
                    + "].isApplicable()=" + isApplicable);
        return isApplicable;
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
