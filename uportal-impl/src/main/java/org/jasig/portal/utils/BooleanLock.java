/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.utils;

/**
 * A simple object with a boolean property.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @version $Revision$
 */

public class BooleanLock {
    protected boolean flag;

    public BooleanLock() {
        this.flag=false;
    }
    public BooleanLock(boolean value) {
        this.flag=value;
    }

    public boolean getValue() { return flag; }
    public void setValue(boolean value) {
        this.flag=value;
    }

}
