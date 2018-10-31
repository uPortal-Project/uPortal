package org.apereo.portal.groups.smartldap;

/**
 * Strategy for modifying {@LdapRecord} children keys to group keys.
 */
public interface IChildKeyModifier {

    /**
     * Modify {@LdapRecord} child key to group key.
     *
     * @param ldapKey   Child key from the LDAP record
     * @return String   child key converted to group key
     */
    String convertLdapKey(String ldapKey);

}
