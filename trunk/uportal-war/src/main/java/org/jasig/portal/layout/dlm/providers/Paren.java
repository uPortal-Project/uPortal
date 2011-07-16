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
import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.security.IPerson;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Paren extends EvaluatorGroup {
    public enum Type {
        OR,
        AND,
        NOT;
    }
    
    // Static Members.
    public static final String RCS_ID = "@(#) $Header$";
    private static Log LOG = LogFactory.getLog(Paren.class);

    // Instance Members.
    @Column(name = "PAREN_TYPE")
    private Type type = null;

    public Paren() {}

    public Paren(Type t) {
        type = t;
    }

    @Override
    public boolean isApplicable( IPerson toPerson )
    {
        boolean rslt = false;
        if (LOG.isDebugEnabled())
            LOG.debug(" >>>> calling paren[" + this + ", op=" + type + 
                    "].isApplicable()");
        
        switch (this.type) {
            case OR: {
                rslt = false;   // presume false in this case...
                for(Evaluator v : this.evaluators) {
                    if ( v.isApplicable( toPerson ) )
                    {
                        rslt = true;
                        break;
                    }
                }
            } break;

            case AND: {
                rslt = true;   // presume true in this case...
                for(Evaluator v : this.evaluators) {
                    if ( v.isApplicable( toPerson ) == false )
                    {
                        rslt = false;
                        break;
                    }
                }
            } break;
            
            case NOT: {
                rslt = false;   // presume false in this case... until later...
                for(Evaluator v : this.evaluators) {
                    if ( v.isApplicable( toPerson ) )
                    {
                        rslt = true;
                        break;
                    }
                }
                rslt = !rslt;
            } break;
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

    @Override
    public String getSummary() {
        
        StringBuilder rslt = new StringBuilder();
        
        switch (type) {
            case AND:
            case OR:
                String operator = type.equals(Type.AND) ? " && " : " || ";
                for (int i=0; i < evaluators.size(); i++) {
                    if (i > 0) {
                        rslt.append(operator);
                    }
                    Evaluator ev = evaluators.get(i);
                    rslt.append(ev.getSummary());
                }
                if (evaluators.size() > 1) {
                    rslt.insert(0, "(").append(")");
                }
                break;
            case NOT:
                rslt.append("!");
                if (!evaluators.isEmpty()) {
                    rslt.append(evaluators.get(0).getSummary());
                } else {
                    rslt.append("()");
                }
                break;
            default:
                throw new RuntimeException("Unrecognized Type: " + type);
        }
        
        return rslt.toString();
    }


}