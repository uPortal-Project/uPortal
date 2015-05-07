/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.groups.pags.dao;

import java.util.Set;

import org.dom4j.Element;
import org.jasig.portal.IBasicEntity;

/**
 * Defines an {@link IPersonTester} in PAGS.
 *
 * @author Shawn Connolly, sconnolly@unicon.net
 */
public interface IPersonAttributesGroupTestDefinition extends IBasicEntity {

    long getId();

    String getAttributeName();
    void setAttributeName(String attributeName);
    String getTesterClassName();
    void setTesterClassName(String className);
    String getTestValue();
    void setTestValue(String testValue);

    /**
     * @since 4.3
     */
    Set<String> getIncludes();

    /**
     * @since 4.3
     */
    void setIncludes(Set<String> includes);

    /**
     * @since 4.3
     */
    Set<String> getExcludes();

    /**
     * @since 4.3
     */
    void setExcludes(Set<String> excludes);

    IPersonAttributesGroupTestGroupDefinition getTestGroup();
    void setTestGroup(IPersonAttributesGroupTestGroupDefinition testGroup);
    void toElement(Element parent);

}
