package org.jasig.portal;


/**
 * Reference implementation of IUserLayoutDB
 * This implementation simply stores serialized XML string
 * @author Peter Kharchenko
 * @version $Revision$
 */

import org.w3c.dom.*;
import java.io.*;
import java.util.*;

public class UserLayoutDBImpl implements IUserLayoutDB {

    private static String DEFAULT_MEDIA="netscape";
    String sLayoutDtd = "userLayout.dtd";
    boolean bPropsLoaded = false;
    String sPathToLayoutDtd;

    public Document getUserLayout(int userId,int profileId) {
        try {
            IDBImpl dbImpl = new DBImpl();
            return dbImpl.getUserLayout(userId, profileId);
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
        return null;
    }


    public void setUserLayout(int userId,int profileId,Document layoutXML) {
        try {
            IDBImpl dbImpl = new DBImpl();
            dbImpl.setUserLayout(userId, profileId, layoutXML);
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
    }
}


