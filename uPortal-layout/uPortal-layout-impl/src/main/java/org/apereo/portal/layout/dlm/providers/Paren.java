/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout.dlm.providers;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.layout.dlm.Evaluator;
import org.apereo.portal.layout.dlm.EvaluatorFactory;
import org.apereo.portal.layout.dlm.FragmentDefinition;
import org.apereo.portal.security.IPerson;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/** @since 2.5 */
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
    private static Log LOG = LogFactory.getLog(Paren.class);

    // Instance Members.
    @Column(name = "PAREN_TYPE")
    private Type type = null;

    public Paren() {}

    public Paren(Type t) {
        type = t;
    }

    @Override
    public boolean isApplicable(IPerson toPerson) {
        boolean result = false;
        if (LOG.isDebugEnabled())
            LOG.debug(" >>>> calling paren[" + this + ", op=" + type + "].isApplicable()");

        switch (this.type) {
            case OR:
                {
                    result = false; // presume false in this case...
                    for (Evaluator v : this.evaluators) {
                        if (v.isApplicable(toPerson)) {
                            result = true;
                            break;
                        }
                    }
                }
                break;

            case AND:
                {
                    result = true; // presume true in this case...
                    for (Evaluator v : this.evaluators) {
                        if (v.isApplicable(toPerson) == false) {
                            result = false;
                            break;
                        }
                    }
                }
                break;

            case NOT:
                {
                    result = false; // presume false in this case... until later...
                    for (Evaluator v : this.evaluators) {
                        if (v.isApplicable(toPerson)) {
                            result = true;
                            break;
                        }
                    }
                    result = !result;
                }
                break;
        }

        if (LOG.isDebugEnabled())
            LOG.debug(" ---- paren[" + this + ", op=" + type + "].isApplicable()=" + result);
        return result;
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
        Element result = null;
        if (parent.getName().equals("fragment")) {

            // The parent is a fragment, so we render as a <dlm:audience> element...
            QName q = new QName("audience", FragmentDefinition.NAMESPACE);
            result = DocumentHelper.createElement(q);

            // Discover the EvaluatorFactory class...
            result.addAttribute("evaluatorFactory", this.getFactoryClass().getName());

        } else {

            // The parent is *not* a fragment, so we render as a <paren> element...
            result = DocumentHelper.createElement("paren");
            result.addAttribute("mode", this.type.toString());
        }

        // Serialize our children...
        for (Evaluator v : this.evaluators) {
            v.toElement(result);
        }

        // Append ourself...
        parent.add(result);
    }

    @Override
    public Class<? extends EvaluatorFactory> getFactoryClass() {
        return this.evaluators.get(0).getFactoryClass();
    }

    @Override
    public String getSummary() {

        StringBuilder result = new StringBuilder();

        switch (type) {
            case AND:
            case OR:
                String operator = type.equals(Type.AND) ? " && " : " || ";
                for (int i = 0; i < evaluators.size(); i++) {
                    if (i > 0) {
                        result.append(operator);
                    }
                    Evaluator ev = evaluators.get(i);
                    result.append(ev.getSummary());
                }
                if (evaluators.size() > 1) {
                    result.insert(0, "(").append(")");
                }
                break;
            case NOT:
                result.append("!");
                if (!evaluators.isEmpty()) {
                    result.append(evaluators.get(0).getSummary());
                } else {
                    result.append("()");
                }
                break;
            default:
                throw new RuntimeException("Unrecognized Type: " + type);
        }

        return result.toString();
    }
}
