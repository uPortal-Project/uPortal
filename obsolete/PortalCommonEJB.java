package com.ibs.portal;

import java.util.* ;
import java.sql.*;
import com.ibs.Framework.*;
import com.ibs.Framework.EJB.*;

public class PortalCommonEJB extends GenericSession
{ 
  /**
   * The default constructor merely sets the name of the EJB.  The name is prepended to
   * informational texts sent to the EAS log.
   */
   
  public PortalCommonEJB ()
  {
    setName ("PortalCommonEJB");
  }

  public void ejbCreate ()
  {
  }
}

