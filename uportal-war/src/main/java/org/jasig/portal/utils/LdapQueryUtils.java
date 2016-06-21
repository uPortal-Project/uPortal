package org.jasig.portal.utils;

public class LdapQueryUtils {

	/**
	 * EscapeLdapSearchFilterTerms takes a string that represents a term in an
	 * LDAP query and escapes any LDAP special characters that could be used to
	 * perform an LDAP injection attack.  
	 * <p>For example, a valid term might be "baseball", and a valid LDAP query
	 * might be "(cn=baseball)".  We must prevent a malicious or accidental 
	 * attempt to send special characters that would subvert the intended query.  
	 * If, for example, the user sent a query term of "baseball)(cn=*)", we 
	 * don't want to process the LDAP query of (cn=baseball)(cn=*);
	 * this would potentially return additional information beyond what was 
	 * intended.
	 * <p>By escaping LDAP special characters, the query above would actually 
	 * be (cn=baseball\\)\\(cn=\\*)".
	 * <p>See <a href=https://docs.ldap.com/specs/rfc4515.txt>https://docs.ldap.com/specs/rfc4515.txt</a> for
	 * additional information about LDAP special characters
	 * @param query Search term that needs to have special characters replaced
	 * @return Search term with special characters replaced with escaped versions.
	 */
	public static String escapeLdapSearchFilterTerms(String query) {
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
