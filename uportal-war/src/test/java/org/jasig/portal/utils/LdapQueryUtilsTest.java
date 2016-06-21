package org.jasig.portal.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LdapQueryUtilsTest {
	
	@Test
	public void testEscapeSpecialCharacters() {
		assertEquals("abcd", LdapQueryUtils.escapeLdapSearchFilterTerms("abcd"));
		assertEquals("abcd\\)\\(\\*\\)", LdapQueryUtils.escapeLdapSearchFilterTerms("abcd)(*)"));
		assertEquals("abcd\\)\\(name=\\$\\.\\?\\)", LdapQueryUtils.escapeLdapSearchFilterTerms("abcd)(name=$.?)"));
	}

}
