package org.jasig.portal.utils;

public class LdapQueryUtils {

	/**
	 * 
	 * @param query
	 * @return
	 */
	public static String escapeSpecialCharacters(String query) {
        // We need to escape regex special characters that appear in the query string...
        final String[][] specials = new String[][] {
                            /* backslash must come first! */
                            new String[] { "\\", "\\\\"}, 
                            new String[] { "[", "\\[" }, 
                            /* closing ']' isn't needed b/c it's a normal character w/o a preceding '[' */
                            new String[] { "{", "\\{" }, 
                            /* closing '}' isn't needed b/c it's a normal character w/o a preceding '{' */
                            new String[] { "^", "\\^" },
                            new String[] { "$", "\\$" },
                            new String[] { ".", "\\." },
                            new String[] { "|", "\\|" },
                            new String[] { "?", "\\?" },
                            new String[] { "*", "\\*" },
                            new String[] { "+", "\\+" },
                            new String[] { "(", "\\(" },
                            new String[] { ")", "\\)" }
                        };
        for (String[] s : specials) {
            query = query.replace(s[0], s[1]);
        }
        return query;
	}
}
