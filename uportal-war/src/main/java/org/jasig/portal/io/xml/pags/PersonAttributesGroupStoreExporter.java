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
package org.jasig.portal.io.xml.pags;

import java.util.Set;

import org.dom4j.Element;
import org.jasig.portal.io.xml.crn.AbstractDom4jExporter;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupDefinitionDao;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Exports a PAGS Group definition data file
 * 
 * @author Shawn Connolly, sconnolly@unicon.net
 */
public class PersonAttributesGroupStoreExporter extends AbstractDom4jExporter {
    private IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao;
    
    @Autowired
    public void setpersonAttributesGroupDefinitionDao(IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao) {
        this.personAttributesGroupDefinitionDao = personAttributesGroupDefinitionDao;
    }

    @Override
    protected Element exportDataElement(String name) {
        final Set<IPersonAttributesGroupDefinition> personAttributesGroupDefinitions = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(name);
        
        if (personAttributesGroupDefinitions.isEmpty()) {
            return null;
        }
        
        final org.dom4j.Document pagsGroupDefDoc = new org.dom4j.DocumentFactory().createDocument();
        final Element pagsGroupDefElement = pagsGroupDefDoc.addElement("pags-group");
        pagsGroupDefElement.addAttribute("script", "classpath://org/jasig/portal/io/import-pags-group_v4-1.crn");
        personAttributesGroupDefinitions.iterator().next().toElement(pagsGroupDefElement);
        
        return pagsGroupDefElement;
    }
}
