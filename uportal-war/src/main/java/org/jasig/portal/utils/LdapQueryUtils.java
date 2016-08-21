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
	 * be (cn=baseball\\29\\28cn=\\2a\\29".
	 * <p>See <a href=https://docs.ldap.com/specs/rfc4515.txt>https://docs.ldap.com/specs/rfc4515.txt</a> for
	 * additional information about LDAP special characters
	 * @param query Search term that needs to have special characters replaced
	 * @return Search term with special characters replaced with escaped versions.
	 */
    public static final String escapeLdapSearchFilterTerms(String filter) {
        StringBuffer sb = new StringBuffer(); 
        for (int i = 0; i < filter.length(); i++) {
        	char curChar = filter.charAt(i);
        	switch (curChar) {
                case '\\':
                    sb.append("\\5c");
                    break;
                case '*':
                    sb.append("\\2a");
                    break;
                case '(':
                    sb.append("\\28");
                    break;
                case ')':
                    sb.append("\\29");
                    break;
                case '\u0000': 
                    sb.append("\\00"); 
                    break;
                default:
                    sb.append(curChar);
            }
       }
       return sb.toString();
   }
}
