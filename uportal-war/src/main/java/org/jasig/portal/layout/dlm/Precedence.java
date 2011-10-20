/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.layout.dlm;

import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.spring.locator.UserLayoutStoreLocator;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public final class Precedence
{
    public static final String RCS_ID = "@(#) $Header$";

    private double precedence = 0.0;
    private int index = -1;
    private static Precedence userPrecedence = new Precedence();

    private Precedence()
    {
    }

    static Precedence newInstance( String fragmentIdx )
    {
        if ( fragmentIdx == null ||
             fragmentIdx.equals( "" ) )
            return userPrecedence;
        return new Precedence( fragmentIdx );
    }

    public String toString()
    {
        return "p[" + precedence + ", " + index + "]";
    }
    
    private Precedence ( String fragmentIdx )
    {
        int fragmentIndex = 0;
        try
        {
            fragmentIndex = Integer.parseInt( fragmentIdx );
        }
        catch( Exception e )
        {
            // if unparsable default to lowest priority.
            return;
        }
                
        final IUserLayoutStore dls = UserLayoutStoreLocator.getUserLayoutStore();
                
        this.precedence = dls.getFragmentPrecedence( fragmentIndex );
        this.index = fragmentIndex;
    }

    /**
       Returns true of this complete precedence is less than the complete
       precedence of the passed in Precedence object. The complete
       precedence takes into account the location in the configuration
       file of the fragment definition. If the "precedence" value is
       equal then the precedence object with the lowest index has the
       higher complete precedence. And index of -1 indicates the highest
       index in the file.
    */
    public boolean isLessThan ( Precedence p )
    {
        if ( this.precedence < p.precedence ||
             ( this.precedence == p.precedence &&
               ( this.index == -1 && p.index > -1 ||
                 this.index > p.index ) ) )
            return true;
        return false;
    }

    public boolean isEqualTo ( Precedence p )
    {
        return this.precedence == p.precedence;
    }
    
    public static Precedence getUserPrecedence()
    {
        return userPrecedence;
    }
}
