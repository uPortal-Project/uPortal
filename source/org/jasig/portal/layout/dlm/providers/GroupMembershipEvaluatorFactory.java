/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.providers;

import org.jasig.portal.layout.dlm.Evaluator;


/**
 * Creates a group membership evaluator. This class inherits from
 * IPersonEvaluatorFactory. Subclassing IPersonEvaluatorFactory gives us all
 * benefits of its configuration language structure. The side affect of course
 * is then that our group evaluator must be configured with that language
 * structure. The definitions of the specific, non-element-containing evaluators
 * must conform to the following element structure:
 * 
 * <pre>
 * 
 *   &lt; attribute mode=&quot;required&quot; 
 *     name=&quot;required&quot; 
 *     value=&quot;optional&quot;/ &gt;
 *  
 * </pre>
 * 
 * Therefore, the form selected for our group membership evaluator
 * configurations is:
 * 
 * <pre>
 * 
 *   &lt; attribute mode=&quot;memberOf&quot; name=&quot;Students&quot;/ &gt; 
 *  
 * </pre>
 * 
 * Case is important for both mode and name. The value attribute is not used in
 * configuration of these group membership evaluators.
 * 
 * @see org.jasig.portal.layout.dlm.providers.PersonEvaluatorFactory#getAttributeEvaluator(java.lang.String,
 *      java.lang.String, java.lang.String) *
 * @author mboyd@sungardsct.com
 */
public class GroupMembershipEvaluatorFactory extends PersonEvaluatorFactory
{
    /**
     * Returns an instance of an evaluator specific to this factory and the 
     * passed in values. Name should be a well known group name. Case is 
     * important. The mode should be "memberOf" for now. Other modes may be 
     * added in the future like, "deepMemberOf".
     */
    public Evaluator getAttributeEvaluator(String name, String mode,
            String value) throws Exception
    {
        return new GroupMembershipEvaluator(mode, name);
    }
}