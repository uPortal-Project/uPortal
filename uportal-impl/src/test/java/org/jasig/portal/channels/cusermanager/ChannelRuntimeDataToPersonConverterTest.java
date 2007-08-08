package org.jasig.portal.channels.cusermanager;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.security.IPerson;

import junit.framework.TestCase;

public class ChannelRuntimeDataToPersonConverterTest extends TestCase {

	private ChannelRuntimeDataToPersonConverter converter =  new ChannelRuntimeDataToPersonConverter();
	
	public void testMapsParametersToAttributes() {
		ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
		channelRuntimeData.setParameter("param_one", "value_one");
		channelRuntimeData.setParameter("param_two", "value_two");
		
		IPerson person = converter.channelRuntimeDataToPerson(channelRuntimeData);
		assertEquals("value_one", person.getAttribute("param_one"));
		assertEquals("value_two", person.getAttribute("param_two"));
		
	}
	
	public void testDoesNotMapFormActionParameterToAttribute() {
		ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
		channelRuntimeData.setParameter(Constants.FORMACTION, "some_value");
		
		IPerson person = converter.channelRuntimeDataToPerson(channelRuntimeData);
		
		assertNull(person.getAttribute(Constants.FORMACTION));
	}

}
