package org.jasig.portal;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

/**
 *  This class holds the information needed to perform a kick users command. It
 *  is placed in the user's session and used by the next step in the kick users
 *  command sequence.
 *
 *@author     zedshaw
 *@created    June 19, 2001
 */
public class KickCriteria {

    // verify all the form variables are correct
    // this first version doesn't have any access functions, sorry
    long creationTime = 0;
    long lastAccessedTime = 0;
    long inactiveTime = 0;
    String sessionType = "PORTAL";
    int kickType = RESET_KICK;
    long resetTime = 0;
    /**
     *  Indicates that the criteria was not set.
     *
     *@since
     */
    public final static int NOT_SET = 0;
    /**
     *  Indicates that a criteria is GREATER than the current user value.
     *
     *@since
     */
    public final static int GREATER = 1;
    /**
     *  Indicates that a criteria is LESSER than the current user value.
     *
     *@since
     */
    public final static int LESSER = 2;
    /**
     *  Indicates that the criteria is exactly EQUAL to the current user value.
     *
     *@since
     */
    public final static int EQUAL = 3;

    /**
     *  Indicates what type of kick to do ( a reset).
     *
     *@since
     */
    public final static int RESET_KICK = 1;

    /**
     *  Indicates what type of kick to do (a invalidate)
     *
     *@since
     */
    public final static int INVALIDATE_KICK = 2;

    int creationTimeCriteria = NOT_SET;
    int lastAccessedTimeCriteria = NOT_SET;
    int inactiveTimeCriteria = NOT_SET;


    /**
     *  Constructor, must give a request object to initialize this.
     *
     *@param  request  Description of Parameter
     *@since
     */
    public KickCriteria(javax.servlet.http.HttpServletRequest request) {

        // verify all the form variables are correct
        // we get everything after the criteria for thos fields that have it
        try {
            String ct = request.getParameter("creationTime").substring(1).trim();
            creationTime = Long.parseLong(ct);
            creationTimeCriteria = interpretCriteria(request.getParameter("creationTime"));
        }
        catch (Exception e) {
            creationTime = 0;
            creationTimeCriteria = NOT_SET;
        }

        try {
            String lt = request.getParameter("lastAccessedTime").substring(1).trim();
            lastAccessedTime = Long.parseLong(lt);
            lastAccessedTimeCriteria = interpretCriteria(request.getParameter("lastAccessedTime"));
        }
        catch (Exception e) {
            lastAccessedTime = 0;
            lastAccessedTimeCriteria = NOT_SET;
        }

        try {
            String it = request.getParameter("inactiveTime").substring(1).trim();
            inactiveTime = Long.parseLong(it);
            inactiveTimeCriteria = interpretCriteria(request.getParameter("inactiveTime"));
        }
        catch (Exception e) {
            inactiveTime = 0;
            inactiveTimeCriteria = NOT_SET;
        }

        sessionType = request.getParameter("sessionType");
        kickType = interpretKickType(request.getParameter("kickType"));

        try {
            resetTime = Long.parseLong(request.getParameter("resetTime").trim());
        }
        catch (Exception e) {
            resetTime = 200L;
        }
    }


    /**
     *  Gets the ResetTime attribute of the KickCriteria object
     *
     *@return    The ResetTime value
     *@since
     */
    public long getResetTime() {
        return resetTime;
    }


    /**
     *  Gets the CreationTime attribute of the KickCriteria object
     *
     *@return    The CreationTime value
     *@since
     */
    public long getCreationTime() {
        return creationTime;
    }


    /**
     *  Gets the LastAccessedTime attribute of the KickCriteria object
     *
     *@return    The LastAccessedTime value
     *@since
     */
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }


    /**
     *  Gets the InactiveTime attribute of the KickCriteria object
     *
     *@return    The InactiveTime value
     *@since
     */
    public long getInactiveTime() {
        return inactiveTime;
    }


    /**
     *  Gets the SessionType attribute of the KickCriteria object
     *
     *@return    The SessionType value
     *@since
     */
    public String getSessionType() {
        return sessionType;
    }


    /**
     *  Gets the KickType attribute of the KickCriteria object
     *
     *@return    The KickType value
     *@since
     */
    public int getKickType() {
        return kickType;
    }


    /**
     *  Gets the CreationTimeCriteria attribute of the KickCriteria object
     *
     *@return    The CreationTimeCriteria value
     *@since
     */
    public int getCreationTimeCriteria() {
        return creationTimeCriteria;
    }


    /**
     *  Gets the LastAccessedTimeCriteria attribute of the KickCriteria object
     *
     *@return    The LastAccessedTimeCriteria value
     *@since
     */
    public int getLastAccessedTimeCriteria() {
        return lastAccessedTimeCriteria;
    }


    /**
     *  Gets the InactiveTimeCriteria attribute of the KickCriteria object
     *
     *@return    The InactiveTimeCriteria value
     *@since
     */
    public int getInactiveTimeCriteria() {
        return inactiveTimeCriteria;
    }


    /**
     *  Takes an input string and parses it to determine what type of criteria
     *  it is (GREATER, LESSER, EQUAL, NOT_SET). It returns the int that matches
     *  one of the criteria types (GREATER, LESSER, EQUAL, NOT_SET).
     *
     *@param  criteriaString  Description of Parameter
     *@return                 Description of the Returned Value
     *@since
     */
    public static int interpretCriteria(String criteriaString) {
        // default to NOT_SET
        int criteriaValue = NOT_SET;

        // make sure it exists
        if (criteriaString != null && criteriaString.length() > 0) {
            // looks like it does, figure out what it is
            char criteriaValueInput = criteriaString.charAt(0);
            if ('>' == criteriaValueInput) {
                criteriaValue = GREATER;
            }
            else if ('<' == criteriaValueInput) {
                criteriaValue = LESSER;
            }
            else if ('=' == criteriaValueInput) {
                criteriaValue = EQUAL;
            }
        }

        // the criteriaValue should now be either NOT_SET or something else
        return criteriaValue;
    }


    /**
     *  Takes an integer representation of the criteri and returns a string for
     *  display purposes.
     *
     *@param  criteria  Description of Parameter
     *@param  verbose   Description of Parameter
     *@return           Description of the Returned Value
     *@since
     */
    public static String interpretCriteria(int criteria, boolean verbose) {
        String[] verboseCriteria = {"not set", "greater than", "less than", "equal to"};
        String[] shortCriteria = {"N", ">", "<", "="};
        if (verbose) {
            return verboseCriteria[criteria];
        }
        else {
            return shortCriteria[criteria];
        }
    }


    /**
     *  Takes a string indicating the kick type and returns the integer
     *  representation.
     *
     *@param  kickTypeValue  Description of Parameter
     *@return                Description of the Returned Value
     *@since
     */

    public static int interpretKickType(String kickTypeValue) {
        int kickType = RESET_KICK;

        if ("invalidate".equals(kickTypeValue)) {
            kickType = INVALIDATE_KICK;
        }

        return kickType;
    }


    /**
     *  Takes an integer representation of the kick type and returns a string
     *  for display purposes.
     *
     *@param  kickType  Description of Parameter
     *@return           Description of the Returned Value
     *@since
     */

    public static String interpretKickType(int kickType) {
        if (kickType == INVALIDATE_KICK) {
            return "invalidate";
        }
        else {
            return "reset";
        }
    }


    /**
     *  Determines if the given session matches this criteria object or not.
     *  True means that the session matches at least one criteria. False means
     *  that the session matches none of the criteria.
     *
     *@param  session  Description of Parameter
     *@return          Description of the Returned Value
     *@since
     */
    public boolean matches(HttpSession session) {

        // convert the values to be the same scale
        // minutes -> miliseconds
        long cTime = creationTime * 1000 * 60;
        // minutes -> miliseconds
        long aTime = lastAccessedTime * 1000 * 60;
        // seconds -> seconds
        long iTime = inactiveTime;

        // check creation time
        if (compare(new Date(),
                new Date(session.getCreationTime() + cTime),
                creationTimeCriteria)) {
            return true;
        }

        // last accessed time
        if (compare(new Date(),
                new Date(session.getLastAccessedTime() + aTime),
                lastAccessedTimeCriteria)) {
            return true;
        }

        // inactive time
        if (compare(session.getMaxInactiveInterval(), inactiveTime,
                inactiveTimeCriteria)) {
            return true;
        }

        return false;
    }


    /**
     *  Used to a basic comparison depending on what criteria is used.
     *
     *@param  x         Description of Parameter
     *@param  y         Description of Parameter
     *@param  criteria  Description of Parameter
     *@return           Description of the Returned Value
     *@since
     */
    private boolean compare(long x, long y, int criteria) {
        switch (criteria) {
            case EQUAL:
                return x == y;
            case LESSER:
                return x < y;
            case GREATER:
                return x > y;
            default:
                return false;
        }
    }


    /**
     *  Used to compare two dates using a given criteria.
     *
     *@param  x         Description of Parameter
     *@param  y         Description of Parameter
     *@param  criteria  Description of Parameter
     *@return           Description of the Returned Value
     *@since
     */
    private boolean compare(Date x, Date y, int criteria) {
        switch (criteria) {
            case EQUAL:
                return x.equals(y);
            case LESSER:
                return x.before(y);
            case GREATER:
                return x.after(y);
            default:
                return false;
        }
    }

}

