package org.jasig.portal;

import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*; 

public interface IChannel
{
  public void init (ChannelConfig chConfig);
  public String getName ();
  public boolean isMinimizable ();
  public boolean isDetachable ();
  public boolean isRemovable ();
  public boolean isEditable ();
  public boolean hasHelp ();
  
  public int getDefaultDetachWidth ();
  public int getDefaultDetachHeight ();

  /*a Vector of arrays [fieldLabel, fieldName, fieldSize, fieldMaxSize, desc] */
  public Vector getParameters();
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out);
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out);
  public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out);
}