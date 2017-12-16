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
package org.apereo.portal.rendering.predicates;

import java.util.function.Predicate;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Required;

/**
 * It's great that Java 8 provides native, language-level support for predicates, but they don't
 * make it easy to configure them in Spring EL (which is something we do for the Rendering
 * Pipeline). This class is essentially a hard-coded <code>Predicate</code>; you specify <code>true
 * </code> or <code>false</code>. The <code>HttpServletRequest</code> is ignored.
 *
 * @since 5.0
 */
public class SimplePredicate implements Predicate<HttpServletRequest> {

    private String valueString;
    private boolean value;

    @Required
    public void setValue(String value) {
        this.valueString = value;
    }

    @PostConstruct
    public void init() {
        value = Boolean.valueOf(valueString);
    }

    @Override
    public boolean test(HttpServletRequest httpServletRequest) {
        return value;
    }
}
