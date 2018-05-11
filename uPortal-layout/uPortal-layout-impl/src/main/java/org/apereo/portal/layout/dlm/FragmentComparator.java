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
package org.apereo.portal.layout.dlm;

import java.util.Comparator;

/**
 * A comparator of fragment objects that sorts first by precedence value with highest number being
 * higher and if equal it then sorts by fragment index with the lowest number having highest
 * precedence since it is an indication of the fragments location within the config file. For those
 * with equal precedence the one defined first when loading the file should take precedence.
 *
 * @since 2.5
 */
public class FragmentComparator implements Comparator<FragmentDefinition> {

    @Override
    public int compare(FragmentDefinition frag1, FragmentDefinition frag2) {
        if (frag1.getPrecedence() == frag2.getPrecedence()) {
            return (int) (frag1.getId() - frag2.getId());
        } else {
            return (int) (frag2.getPrecedence() - frag1.getPrecedence());
        }
    }
}
