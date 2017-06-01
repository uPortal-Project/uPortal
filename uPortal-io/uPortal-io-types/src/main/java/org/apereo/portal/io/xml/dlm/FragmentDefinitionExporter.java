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
package org.apereo.portal.io.xml.dlm;

import org.apereo.portal.io.xml.crn.AbstractDom4jExporter;
import org.apereo.portal.layout.dlm.Constants;
import org.apereo.portal.layout.dlm.FragmentDefinition;
import org.apereo.portal.layout.dlm.IFragmentDefinitionDao;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Exports a uPortal 3.1 style fragment definition data file
 *
 */
public class FragmentDefinitionExporter extends AbstractDom4jExporter {
    private IFragmentDefinitionDao fragmentDefinitionDao;

    @Autowired
    public void setFragmentDefinitionDao(IFragmentDefinitionDao fragmentDefinitionDao) {
        this.fragmentDefinitionDao = fragmentDefinitionDao;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.io.xml.crn.AbstractDom4jExporter#exportDataElement(java.lang.String)
     */
    @Override
    protected Element exportDataElement(String id) {
        final FragmentDefinition fragmentDefinition =
                fragmentDefinitionDao.getFragmentDefinition(id);
        if (fragmentDefinition == null) {
            return null;
        }

        final org.dom4j.Document fragmentDefDoc = new org.dom4j.DocumentFactory().createDocument();
        final Element fragmentDefElement = fragmentDefDoc.addElement("fragment-definition");
        fragmentDefElement.addNamespace("dlm", Constants.NS_URI);
        fragmentDefElement.addAttribute(
                "script", "classpath://org/jasig/portal/io/import-fragment-definition_v3-1.crn");
        fragmentDefinition.toElement(fragmentDefElement);

        return fragmentDefElement;
    }
}
