package org.jasig.portal.jndi;

import javax.naming.NamingException;

/**
 * Title:        uPortal 20
 * Description:  
 * Copyright:    Copyright (c) 2000
 * Company:      Interactive Business Solutions
 * @author 
 * @version 1.0
 */

public class PortalNamingException
  extends NamingException
{
  public PortalNamingException(String explanation)
  {
    super(explanation);
  }
}