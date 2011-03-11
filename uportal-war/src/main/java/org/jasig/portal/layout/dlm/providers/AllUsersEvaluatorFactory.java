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

package org.jasig.portal.layout.dlm.providers;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Node;

/**
 * Used to target a fragment to all users of the system including guest users.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AllUsersEvaluatorFactory extends Evaluator implements EvaluatorFactory
{
    public static final String RCS_ID = "@(#) $Header$";

    @Override
    public Evaluator getEvaluator( Node audience )
    {
        return this;
    }
    
    @Override
    public boolean isApplicable( IPerson p )
    {
        return true;
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

    @Override
    public String getSummary() {
        return "(ANYONE)";
    }

}
