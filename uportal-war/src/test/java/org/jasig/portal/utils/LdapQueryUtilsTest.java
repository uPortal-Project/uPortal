package org.jasig.portal.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LdapQueryUtilsTest {
	
	@Test
	public void testEscapeSpecialCharacters() {
		assertEquals("abcd", LdapQueryUtils.escapeLdapSearchFilterTerms("abcd"));
		assertEquals("abcd\\29\\28\\2a\\29", LdapQueryUtils.escapeLdapSearchFilterTerms("abcd)(*)"));
		assertEquals("abcd\\29\\28name=$.?\\29", LdapQueryUtils.escapeLdapSearchFilterTerms("abcd)(name=$.?)"));
	}
}
