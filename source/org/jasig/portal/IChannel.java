package org.jasig.portal;

import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*; 

public interface IChannel
{
  public void initParams (Hashtable params);
  public String getName ();
  public boolean isMinimizable ();
  public boolean isDetachable ();
  public boolean isRemovable ();
  public boolean isEditable ();
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out);
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out);
}