package org.jasig.portal;

/**
 * Interface by which portal talks to the user layout database
 * @author Peter Kharchenko
 * @version $Revision$
 */

public interface IUserLayoutDB {
    public String getUserLayout(String userName,String media);
    public void setUserLayout(String userName,String media,String layoutXML);
}


