/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.legacy;

import java.util.Arrays;

/**
 * Legacy PersonDirInfo bean.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
class PersonDirInfo {
    
    /**  protocol, server, and initial connection parameters */
    private String url; 
    
    /**  Resource Reference name for a J2EE style DataSource */
    private String ResRefName; 
    
    /**
     * Name of an LDAP resource configured in LdapServices.
     */
    private String ldapRefName;
    
    /**  JDBC java class to register */
    private String driver; 
    
    /**  database userid or LDAP user DN (if needed) */
    private String logonid; 
    
    /**  password */
    private String logonpassword; 
    
    /** where are users? "OU=people" or "CN=Users" */
    private String usercontext;
    
    /** SELECT or JNDI query for userid */
    private String uidquery; 
    
    /** timeout for LDAP in milliseconds. 0 means wait forever */
    private int ldaptimelimit = 0;
    
    /**
     * Names of attributes in the underlying attribute source.
     * The ith element of this array corresponds to the ith element of
     * attributenames to express a mapping from an attribute in the source to
     * an attribute in uPortal.
     */
    private String[] attributenames;
    
    /**
     * Names of attributes in uPortal.
     */
    private String[] attributealiases;
    
    /**
     * @return Returns the attributealiases.
     */
    public String[] getAttributealiases() {
        return this.attributealiases;
    }
    
    /**
     * @param attributealiases The attributealiases to set.
     */
    void setAttributealiases(String[] attributealiases) {
        this.attributealiases = attributealiases;
    }
    
    /**
     * @return Returns the attributenames.
     */
    String[] getAttributenames() {
        return this.attributenames;
    }
    
    /**
     * @param attributenames The attributenames to set.
     */
    void setAttributenames(String[] attributenames) {
        this.attributenames = attributenames;
    }
    
    /**
     * Get the fully qualified class name of the JDBC driver to use.
     * @return fully qualified class name of JDBC driver.
     */
    String getDriver() {
        return this.driver;
    }
    
    /**
     * Set the name of the class to use as the JDBC driver for a directly-configured
     * JDBC PersonDirInfo.
     * @param driver The driver to set.
     * @throws IllegalStateException if this is an LDAP or Res-Ref PDI.
     */
    void setDriver(String driver) {
        if (isLdap())
            throw new IllegalStateException("Cannot set driver for an LDAP source.");
        if (this.ResRefName != null)
            throw new IllegalStateException("Cannot set driver for a JDBC source " +
                    "using reference to an RDBMServices configured source.");
        this.driver = driver;
    }
    
    /**
     * Get the time limit for LDAP queriues, in milliseconds.
     * Zero has the special meaning of no time limit.
     * @return time limit for ldap queries, in milliseconds. Zero means no time limit.
     */
    int getLdaptimelimit() {
        return this.ldaptimelimit;
    }
    
    /**
     * Set the time limit, in milliseconds, for LDAP query.
     * Special value of zero means no time limit.
     * @param ldaptimelimit The ldaptimelimit to set.
     * @throws IllegalArgumentException if ldaptimelimt param < 0
     * @throws IllegalStateException if using JDBC.
     */
    void setLdaptimelimit(int ldaptimelimit) {
        if (ldaptimelimit < 0)
            throw new IllegalArgumentException("Cannot set an LDAP time limit " +
                    "of less than zero milliseconds: [" + ldaptimelimit + "]");
        if (isJdbc())
            throw new IllegalStateException("Cannot set an LDAP time limit on a " +
                    "PDI representing using JDBC.");
        this.ldaptimelimit = ldaptimelimit;
    }
    

    
    /**
     * Get the username to use to authenticate to the PersonDirInfo-configured
     * JDBC or LDAP source.
     * @return the username for authenticating to the soruce
     */
    String getLogonid() {
        return this.logonid;
    }
    
    /**
     * Set the username to use to authenticate to the PersonDirInfo-configured
     * JDBC or LDAP source.
     * Throws IllegalStateException if this PDI represents using an RDBMServices
     * or LdapServices-configured datasource.
     * @param logonid The logonid to set.
     * @throws IllegalStateException if this is a ResRef or Ldap-ref source.
     */
    void setLogonid(String logonid) {
        if (this.ldapRefName != null)
            throw new IllegalStateException("Cannot set logon id for a source " +
                    "configured to use LdapServices to obtain the LDAP connection.");
        if (this.ResRefName != null)
            throw new IllegalStateException("Cannot set logon id for a source " +
                    "configured to use RdbmServices to obtain the Jdbc DataSource.");
        this.logonid = logonid;
    }
    
    /**
     * Get the password to authenticate to the LDAP or JDBC source.
     * @return the password
     */
    String getLogonpassword() {
        return this.logonpassword;
    }
    
    /**
     * Set the password to use to authenticate to the LDAP or JDBC source.
     * @param logonpassword password to directly configured source
     */
    void setLogonpassword(String logonpassword) {
        if (this.ldapRefName != null)
            throw new IllegalStateException("Cannot set logon password for a source " +
                    "configured to use LdapServices to obtain the LDAP connection.");
        if (this.ResRefName != null)
            throw new IllegalStateException("Cannot set logon password for a source " +
                    "configured to use RdbmServices to obtain the Jdbc DataSource.");
        this.logonpassword = logonpassword;
    }
    
    /**
     * Get the name of the RDBMServices-configured DataSource this PersonDirInfo
     * indicates we should use.  Returns null if this PDI does not indicate we should
     * use an RDBMServices-configured DataSource.
     * @return the name of the RDBMServices-configured DataSource we should use.
     */
    String getResRefName() {
        return this.ResRefName;
    }
    
    /**
     * Set the name of an RDBMServices-configured DataSource against which
     * we should query for user attributes.
     * @param resRefName the name of an RDBMServices-configured DataSource.
     * @throws IllegalArgumentException if resRefName param is null
     * @throws IllegalStateException if url or ldapRefName already set
     */
    void setResRefName(String resRefName) {
        if (resRefName == null)
            throw new IllegalArgumentException("Cannot set resRefName to null.");
        if (this.url != null)
            throw new IllegalStateException("Cannot set resRefName when url already set.");
        if (this.ldapRefName != null)
            throw new IllegalStateException("Cannot set resRefName when ldapRefName already set.");
        this.ResRefName = resRefName;
    }
    
    /**
     * Get the parameterized JDBC or LDAP query - the single query parameter 
     * should be the user identifier.
     * @return LDAP or JDBC query parameterized by user identifier
     */
    String getUidquery() {
        return this.uidquery;
    }
    
    /**
     * Set the LDAP or JDBC uid query.
     * @param uidquery The uidquery to set.
     * @throws IllegalArgumentException if param uidquery is null.
     */
    void setUidquery(String uidquery) {
        if (uidquery == null)
            throw new IllegalArgumentException("You cannot set the uidquery to null.");
        this.uidquery = uidquery;
    }
    
    /**
     * Get the LDAP or JDBC url.
     * @return Returns the url.
     */
    String getUrl() {
        return this.url;
    }
    
    /**
     * Set the ldap or JDBC url.
     * @param url The url to set.
     * @throws IllegalArgumentException if the URL doesn't start with jdbc or ldap.
     * @throws IllegalStateException if ldapRefName or ResRefName is already set.
     */
    void setUrl(String url) {
        if (this.ldapRefName != null)
            throw new IllegalStateException("Cannot set the URL of a PDI " +
                    "configured to use an LdapServices-configured LDAP source.");
        if (this.ResRefName != null)
            throw new IllegalStateException("Cannot set the URL of a PDI " +
                    "configured to use an RDBMServices-configured DataSource.");
        if (!url.startsWith("ldap") && !url.startsWith("jdbc")) {
            throw new IllegalArgumentException("The url must start with 'ldap' " +
                    "or 'jdbc', this URL didn't: [" + url + "]");
        }
        this.url = url;
    }
    
    /**
     * Get the context in which users are to be found.
     * @return the context in which users are to be found.
     */
    String getUsercontext() {
        return this.usercontext;
    }
    
    /**
     * Set the LDAP context in which users are to be found.
     * @param usercontext LDAP context for users
     * @throws IllegalStateException if this is a JDBC PDI.
     */
    void setUsercontext(String usercontext) {
        if (isJdbc())
            throw new IllegalStateException("Cannot set usercontext of a JDBC PDI.");
        this.usercontext = usercontext;
    }
    
    /**
     * Does this PersonDirInfo instance represent information about a JDBC 
     * information source?
     * @return true if a JDBC source, false otherwise
     */
    boolean isJdbc() {
        if (this.ResRefName != null && this.ResRefName.length() > 0)
            return true;
        if (this.url != null && this.url.startsWith("jdbc:"))
            return true;
        return false;
    }
    
    /**
     * Does this PersonDirInfo instance represent information about an LDAP 
     * information source?
     * @return true if a LDAP source, false otherwise
     */
    boolean isLdap() {
        return (this.url != null && this.url.startsWith("ldap") 
                || (this.ldapRefName != null && this.ldapRefName.length() > 0));
    }
    
    /**
     * Get the name of the LDAP source from LdapServices to use.
     * @return Returns the ldapRefName, or null if not set.
     */
    String getLdapRefName() {
        return this.ldapRefName;
    }
    
    /**
     * Set the name of an LDAP resource to use from LdapServices.
     * @param ldapRefName The ldapRefName to set.
     * @throws IllegalStateException if ResRefName or url already set.
     */
    void setLdapRefName(String ldapRefName) {
        if (this.ResRefName != null)
            throw new IllegalStateException("Cannot set ldapRefName when ResRefName is already set.");
        if (this.url != null)
            throw new IllegalStateException("Cannot set ldap ref name when url is already set.");
        this.ldapRefName = ldapRefName;
    }
    
    public boolean equals(Object other) {
      if (other == null)
          return false;
      if (!(other instanceof PersonDirInfo))
          return false;
      PersonDirInfo otherPdi = (PersonDirInfo) other;
      
      if (this.attributealiases == null) {
          if (otherPdi.attributealiases != null)
              return false;
      } else {
          if (! Arrays.equals(this.attributealiases, otherPdi.attributealiases))
              return false;
      }

      if (this.attributenames == null) {
          if (otherPdi.attributenames != null)
              return false;
      } else {
          if (! Arrays.equals(this.attributenames, otherPdi.attributenames))
              return false;
      }
      
      if (this.driver == null) {
          if (otherPdi.driver != null)
              return false;
      } else {
          if (! this.driver.equals(otherPdi.driver))
              return false;
      }
      
      if (this.ldapRefName == null) {
          if (otherPdi.ldapRefName != null)
              return false;
      } else {
          if (! this.ldapRefName.equals(otherPdi.ldapRefName))
              return false;
      }
      
       if (! (this.ldaptimelimit == otherPdi.ldaptimelimit))
              return false;
       
       if (this.logonid == null) {
           if (otherPdi.logonid != null)
               return false;
       } else {
           if (! this.logonid.equals(otherPdi.logonid))
               return false;
       }
      
       if (this.logonpassword == null) {
           if (otherPdi.logonpassword != null)
               return false;
       } else {
           if (! this.logonpassword.equals(otherPdi.logonpassword))
               return false;
       }
       
       if (this.ResRefName == null) {
           if (otherPdi.ResRefName != null)
               return false;
       } else {
           if (! this.ResRefName.equals(otherPdi.ResRefName))
               return false;
       }
       
       if (this.uidquery == null) {
           if (otherPdi.uidquery != null)
               return false;
       } else {
           if (! this.uidquery.equals(otherPdi.uidquery))
               return false;
       }
       
       if (this.url == null) {
           if (otherPdi.url != null)
               return false;
       } else {
           if (! this.url.equals(otherPdi.url))
               return false;
       }
       
       if (this.usercontext == null) {
           if (otherPdi.usercontext != null)
               return false;
       } else {
           if (! this.usercontext.equals(otherPdi.usercontext))
               return false;
       }
       
      return true;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName()).append("\n");
        if (this.ldapRefName != null) {
            sb.append(" ldapRef=[").append(this.ldapRefName).append("]\n");
        }
        if (this.isLdap()) {
            sb.append(" ldaptimelim=").append(this.ldaptimelimit).append("\n");
            sb.append(" usercontext=[").append(this.usercontext).append("]\n");
        }
        if (this.isJdbc()) {
            sb.append(" driver=[").append(this.driver).append("]\n");
        }
        if(this.logonid != null) {
            sb.append(" loginId=[").append(this.logonid).append("]\n");
        }
        if (this.logonpassword != null) {
            sb.append(" logonpassword=[").append(this.logonpassword).append("]\n");
        }
        if (this.ResRefName != null) {
            sb.append(" ResRefName=[").append(this.ResRefName).append("]\n");
        }
        sb.append(" uidQuery=[").append(this.uidquery).append("]\n");
        if (this.url != null) {
            sb.append(" url=[").append(this.url).append("]\n");
        }
        
        if (this.attributenames != null) {
            sb.append(" attributeNames=\n");
            for (int i = 0; i < this.attributenames.length; i++) {
                sb.append("  ").append("'").append(this.attributenames[i]).append("'\n");
            }
        } else {
            sb.append(" attributeNames=null\n");
        }
        
        if (this.attributealiases != null) {
            sb.append(" attributeAliases=\n");
            for (int i = 0; i < this.attributealiases.length; i++) {
                sb.append("  ").append("'").append(this.attributealiases[i]).append("'\n");
            }
        } else {
            sb.append(" attributeAliases=null\n");
        }

        return sb.toString();
    }
    
    /**
     * Validate this object.
     * In the case where this object is insufficient to describe a source for
     * attributes, return a String describing the nature of the problem.
     * In the case where this object is valid, returns null.
     * Note that this method doesn't actually check that ResRefName or 
     * ldapRefName refers to an actually configured resource.
     * @return null if valid or a String message describing problem
     */
    String validate() {
        String problemMessage = "";
        if (this.url == null && this.ldapRefName == null && this.ResRefName == null)
            problemMessage += "The url for the LDAP or JDBC source " +
                    "or a name of an RDBMServices or LdapServices managed " +
                    "data source must be specified.  ";
        if (this.uidquery == null)
            problemMessage += "The uidquery must be specifed.  ";
        if (this.logonpassword != null && this.logonid == null)
            problemMessage += "There was a logon password specified but no logon id.  ";
        if (this.attributenames == null)
            problemMessage += "The names of the uPortal attributes to which to map are not specified.  ";
        if (this.attributealiases == null)
            problemMessage += "The names of the attributes in the LDAP or JDBC store from which to map are not specified.  ";
        if (this.attributenames != null && this.attributealiases != null
                && (this.attributenames.length != this.attributealiases.length))
            problemMessage += "the lengths of the attribute names and attribute aliases arrays are not equal.  ";
                
        if (this.url != null && isJdbc() && this.driver == null)
            problemMessage += "Using PDI-configured JDBC but no driver specified.  ";
        
        if ("".equals(problemMessage))
            problemMessage = null;
        return problemMessage;
    }
}
