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

package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.util.ArrayList;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class CPDPreferenceRestrictionConverter extends AbstractCollectionConverter {

	public CPDPreferenceRestrictionConverter(Mapper mapper) {
		super(mapper);
	}

	public void marshal(Object arg0, HierarchicalStreamWriter arg1,
			MarshallingContext arg2) {
		// TODO Auto-generated method stub

	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		CPDPreferenceTypeRestriction restriction = new CPDPreferenceTypeRestriction();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if ("defaultValue".equals(reader.getNodeName())) {
				if (restriction.getDefaultValues() == null) {
					restriction.setDefaultValues(new ArrayList<String>());
				}
				restriction.getDefaultValues().add(reader.getValue());
			} else if ("max".equals(reader.getNodeName())) {
				restriction.setMax(reader.getValue());
			} else if ("min".equals(reader.getNodeName())) {
				restriction.setMin(reader.getValue());
			} else if ("type".equals(reader.getNodeName())) {
				restriction.setType(reader.getValue());
			} else if ("value".equals(reader.getNodeName())) {
				CPDParameterTypeRestrictionValue val = new CPDParameterTypeRestrictionValue();
				val.setDisplay(reader.getAttribute("display"));
				val.setValue(reader.getValue());
				restriction.addValue(val);
			}
			reader.moveUp();
		}
		return restriction;
	}

	public boolean canConvert(Class clazz) {
		return CPDPreferenceTypeRestriction.class == clazz;
	}

}
