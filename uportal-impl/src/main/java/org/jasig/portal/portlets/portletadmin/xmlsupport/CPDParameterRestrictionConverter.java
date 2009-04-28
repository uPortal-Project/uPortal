package org.jasig.portal.portlets.portletadmin.xmlsupport;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class CPDParameterRestrictionConverter extends AbstractCollectionConverter {

	public CPDParameterRestrictionConverter(Mapper mapper) {
		super(mapper);
	}

	public void marshal(Object arg0, HierarchicalStreamWriter arg1,
			MarshallingContext arg2) {
		// TODO Auto-generated method stub

	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		CPDParameterTypeRestriction restriction = new CPDParameterTypeRestriction();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if ("defaultValue".equals(reader.getNodeName())) {
				restriction.setDefaultValue(reader.getValue());
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
		return CPDParameterTypeRestriction.class == clazz;
	}

}
