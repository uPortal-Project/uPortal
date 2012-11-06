package org.jasig.portal.jgroups.auth;

public interface AuthDao {
    String getAuthToken(String serviceName);
}
