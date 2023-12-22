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
import javax.persistence.Entity;
import org.apereo.portal.layout.dlm.Evaluator;
import org.apereo.portal.layout.dlm.EvaluatorFactory;
import org.apereo.portal.layout.dlm.FragmentDefinition;
import org.apereo.portal.security.IPerson;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.w3c.dom.Node;

/**
 * Used to target a fragment to all users of the system including guest users.
 *
 * @since 2.5
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AllUsersEvaluatorFactory extends Evaluator implements EvaluatorFactory {
    @Override
    public Evaluator getEvaluator(Node audience) {
        return this;
    }

    @Override
    public boolean isApplicable(IPerson p) {
        return true;
    }

    @Override
    public void toElement(Element parent) {

        // Assertions.
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Element result = null;
        QName q = new QName("audience", FragmentDefinition.NAMESPACE);
        result = DocumentHelper.createElement(q);
        result.addAttribute("evaluatorFactory", this.getFactoryClass().getName());
        parent.add(result);
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
