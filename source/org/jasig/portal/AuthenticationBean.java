package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.objectspace.xml.*;
import org.jasig.portal.layout.*;
import org.jasig.portal.security.*;

/**
 * @author Ken Weiner
 */
public class AuthenticationBean extends GenericPortalBean
                               implements IAuthenticationBean
{
  public boolean authenticate (String sUserName, String sPassword)
  {    
    SecurityContext ic;
    Principal me;
    OpaqueCredentials op;
    
    ic = new InitialSecurityContext("root");
    me = ic.getPrincipalInstance();
    op = ic.getOpaqueCredentialsInstance();

    me.setUID(sUserName);
    op.setCredentials(sPassword);
    ic.authenticate();

    return (ic.isAuthenticated());
  }
}
