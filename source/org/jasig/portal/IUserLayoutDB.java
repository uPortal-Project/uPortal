package org.jasig.portal;

/**
 * Interface by which portal talks to the user layout database
 * @author Peter Kharchenko
 * @version $Revision$
 */

import org.w3c.dom.*;

public interface IUserLayoutDB {
    public Document getUserLayout(int userId,int profileId);
    public void setUserLayout(int userId,int profileId,Document layoutXML);
}


