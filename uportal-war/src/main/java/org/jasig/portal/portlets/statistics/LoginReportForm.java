package org.jasig.portal.portlets.statistics;


public class LoginReportForm extends BaseReportForm {
    
    private boolean totalLogins = true;
    private boolean uniqueLogins = false;
    
    public boolean isTotalLogins() {
        return totalLogins;
    }

    public void setTotalLogins(boolean totalLogins) {
        this.totalLogins = totalLogins;
    }

    public boolean isUniqueLogins() {
        return uniqueLogins;
    }

    public void setUniqueLogins(boolean uniqueLogins) {
        this.uniqueLogins = uniqueLogins;
    }
}
