package org.apereo.portal.groups.smartldap;

public class ChildKeyAsCnModifier implements IChildKeyModifier {

    @Override
    public String convertLdapKey(String ldapKey) {
        final int keyStart = ldapKey.indexOf('=');
        final int keyEnd = ldapKey.indexOf(',');
        if (keyStart >= 0 && keyEnd >= 0 && keyStart < keyEnd) {
            ldapKey = ldapKey.substring(keyStart + 1, keyEnd);
        }
        return ldapKey;
    }
}
