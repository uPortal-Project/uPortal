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

package org.jasig.portal.io.xml.dlm;

import java.util.List;

import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.SimpleStringPortalData;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.layout.dlm.IFragmentDefinitionDao;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Lists each fragment definition in the database
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class FragmentDefinitionDataFunction implements Function<IPortalDataType, Iterable<? extends IPortalData>> {
    private IFragmentDefinitionDao fragmentDefinitionDao;

    @Autowired
    public void setFragmentDefinitionDao(IFragmentDefinitionDao fragmentDefinitionDao) {
		this.fragmentDefinitionDao = fragmentDefinitionDao;
	}

	@Override
    public Iterable<? extends IPortalData> apply(IPortalDataType input) {
		final List<FragmentDefinition> fragmentDefinitions = this.fragmentDefinitionDao.getAllFragments();
        
        final List<IPortalData> portalData = Lists.transform(fragmentDefinitions, new Function<FragmentDefinition, IPortalData>() {
            @Override
            public IPortalData apply(FragmentDefinition fragmentDefinition) {
                return new SimpleStringPortalData(
                        fragmentDefinition.getName(),
                        null,
                        fragmentDefinition.getDescription());
            }
        });
        
        return portalData;
    }
}
