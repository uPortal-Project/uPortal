package org.jasig.portal;

import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*; 
import com.objectspace.xml.*;

public interface IAuthorizationBean
{
  public boolean authorize (String sUserName, String sPassword);
}