package org.jasig.portal.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class LdapQueryUtilsTest {
	
	@Test
	public void testEscapeSpecialCharacters() {
		assertEquals("abcd", LdapQueryUtils.escapeSpecialCharacters("abcd"));
		assertEquals("abcd\\)\\(\\*\\)", LdapQueryUtils.escapeSpecialCharacters("abcd)(*)"));
		assertEquals("abcd\\)\\(name=\\$\\.\\?\\)", LdapQueryUtils.escapeSpecialCharacters("abcd)(name=$.?)"));
	}

}
