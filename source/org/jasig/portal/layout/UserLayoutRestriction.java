package org.jasig.portal.layout;

import org.jasig.portal.PortalException;
import java.util.StringTokenizer;

/**
 * <p>Title: UserLayoutRestriction class</p>
 * <p>Description: The base class for UserLayout restrictions</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic</p>
 * @author Michael Ivanov
 * @version 1.0
 */

public abstract class UserLayoutRestriction implements IUserLayoutRestriction {


  private String restrictionName;

  public UserLayoutRestriction() {

  }


  protected int parseInt(String str ) {
    try {
      return Integer.parseInt(str);
    } catch ( Exception e ) {
       return -1;
      }
  }

  /**
     * Sets the name of the restriction
     * @param restrictionName a <code>String</code> name of the restriction
     */
  public void setRestrictionName ( String restrictionName ) {
    this.restrictionName = restrictionName;
  }


  /**
     * Gets the name of the restriction
     * @return a <code>String</code> name of the restriction
     */
  public String getRestrictionName() {
    return restrictionName;
  }

  /**
     * Checks the UserLayout node restriction
     *
     * @param node a <code>UserLayoutNode</code> object to be checked
     * @exception PortalException
     */
  public boolean checkRestriction( UserLayoutNode node ) throws PortalException {
   String restrictionExp = node.getRestrictionExpression(getRestrictionName());
   int priority = node.getPriority();
   // Creating the priority parser
   StringTokenizer st = new StringTokenizer(restrictionExp,",");
   boolean result = false;
   while (st.hasMoreTokens()) {
         String token = st.nextToken();
         int index = token.indexOf('-');
         if ( index > 0 ) {
           int minLevel = parseInt(token.substring(0,index));
           int maxLevel = parseInt(token.substring(index+1));
           if ( minLevel < priority && priority < maxLevel )
             return true;
         } else if ( parseInt(token) == priority )
             return true;
   }
       return false;

  }



}