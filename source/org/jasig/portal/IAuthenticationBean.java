package org.jasig.portal;

import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*; 
import com.objectspace.xml.*;

public interface IAuthenticationBean
{
  public boolean authenticate (String sUserName, String sPassword);
}
