/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.channels.cusermanager.provider;

import java.sql.Types;
import java.sql.ResultSet;
import java.sql.Connection;

import java.text.MessageFormat;

import java.util.Vector;
import java.util.Iterator;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.GroupService;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;

import org.jasig.portal.tools.DeleteUser;

import org.jasig.portal.IUserIdentityStore;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.RDBMUserIdentityStore;

import org.jasig.portal.channels.cusermanager.*;

/**
 * @author smb1@cornell.edu
 */
public class DefaultDataHandlerImpl implements IDataHandler {
  private static final Log LOG = LogFactory.getLog(DefaultDataHandlerImpl.class);

  protected static final String SINGLEQUOTE = "'";
  protected static final String WILDCARD = "%";

  protected static final String UPDMASK = "{0}={1}, ";

  protected static final String UPDCONDMASK
                                     = " where user_name={0}";

  protected static final String COUNTUSERS = "select count( user_name ) cnt"
                         + " from up_person_dir" + UPDCONDMASK;

  protected static final String ADDUSER = "insert into up_person_dir ({0}) "
                                                + "values ({1})";

  protected static final String UPDPWD = ("update up_person_dir set "
          + "encrptd_pswd={0}, lst_pswd_cgh_dt={1} where user_name={2} ")
            .toUpperCase();

  protected static final String USERSELECT = "select * from up_person_dir {0} "
                   + "order by user_name, first_name, last_name";

  protected static final String ALLUSERS
                    = MessageFormat.format( USERSELECT, new String[] { "" } );

  protected static final String GETTHISUSER = MessageFormat.format( USERSELECT,
                  new String[] { "where user_name = {0} " } );

  protected static final String SEARCHUSERS = MessageFormat.format( USERSELECT,
                   new String[] { "where user_name like {0} "
           + "or last_name like {0} or first_name like {0} " });

  private IUserIdentityStore rdbmuser = new RDBMUserIdentityStore();

  static {
      LOG.debug("USERSELECT: " + USERSELECT );
      LOG.debug("ALLUSERS: " + ALLUSERS );
      LOG.debug("GETTHISUSER: " + GETTHISUSER );
      LOG.debug("SEARCHUSERS: " + SEARCHUSERS );
      LOG.debug("ADDUSER: " + ADDUSER );
      LOG.debug("UPDMASK: " + UPDMASK );
      LOG.debug("UPDCONDMASK: " + UPDCONDMASK );
      LOG.debug("UPDPWD: " + UPDPWD );
  }// static

  public IPerson[] getAllUsers() throws Exception {
     return runQuery( ALLUSERS );
  }// getAllUsers

  public IPerson[] getAllUsersLike( String SearchString ) throws Exception {
     return runQuery( SEARCHUSERS, SearchString + WILDCARD );
  }// getAllUsersLike

  public IPerson getUser( String UID ) throws Exception {
    return runQuery( GETTHISUSER, UID )[ 0 ];
  }// getUser

  public void setUserInformation( IPerson AnIndividual ) throws Exception {

     // build sql and update table
     StringBuffer updsql = new StringBuffer( "update up_person_dir set ".toUpperCase() );
     String tmpcond = null;
     String worker = null;
     Enumeration E = AnIndividual.getAttributeNames();
     while( E.hasMoreElements()){
       worker = (String)E.nextElement();

       // Do not process attributs with "-" in them
//       if( worker.indexOf( "-" ) == -1 ) {
         if( worker.toLowerCase().indexOf( Constants.PWDFIELD ) == -1 ) { // don't process password fields
           if( !worker.equals( Constants.UNFIELD ))
             updsql.append( MessageFormat.format( UPDMASK, new String[] { worker.toUpperCase(),
                SINGLEQUOTE + (String)AnIndividual.getAttribute( worker )
                  + SINGLEQUOTE } ));
            else
             tmpcond = MessageFormat.format( UPDCONDMASK.toUpperCase(), new String[]
                 { SINGLEQUOTE + (String)AnIndividual.getAttribute( worker )
                     + SINGLEQUOTE } );
         }// if, password flds
//       }// if, -
     }// while

     // strip off trailing comma
     updsql.setLength( updsql.length() -2 );
     updsql.append( tmpcond );

     LOG.debug("Issuing: " + updsql.toString() );

     Connection C = getDBConn();
     C.createStatement().executeUpdate( updsql.toString());
     releaseConn( C );

  }// setUserInformation

  public void addUser( IPerson AnIndividual ) throws Exception {

    // first see if the username exists and throw if ot does.
    boolean preexisting = false;

    Connection C = getDBConn();
    ResultSet R =  C.createStatement().executeQuery(

      MessageFormat.format(
         COUNTUSERS, new String[] { SINGLEQUOTE
              + AnIndividual.getAttribute( Constants.UNFIELD ) + SINGLEQUOTE } )

      );

    R.next();
    if( R.getInt( "cnt" ) > 0 )
      preexisting = true;

    releaseConn( R, C );

    if( preexisting )
      throw new Exception(
          MessageFormat.format( Constants.USER_EXISTS, new String[]
            { (String)AnIndividual.getAttribute( Constants.UNFIELD ) } ));

    // clear to add user

    StringBuffer fields = new StringBuffer( "" );
    StringBuffer values = new StringBuffer( "" );

    String worker = null;
    Enumeration E = AnIndividual.getAttributeNames();
    while( E.hasMoreElements()) {
      worker = (String)E.nextElement();

      fields.append( worker + ", " );
      values.append( SINGLEQUOTE
                  + AnIndividual.getAttribute( worker ) + SINGLEQUOTE + ", " );
    }// while

    // Adjust len of str buffers
    fields.setLength( fields.length() -2 );
    values.setLength( values.length() -2 );

    C = getDBConn();
    C.createStatement().execute(

      MessageFormat.format(
         ADDUSER, new String[] { fields.toString(), values.toString() } )

      );

    releaseConn( C );

  }// addUser

  /** OriginalPassword is null if called in "UserManager" mode. */
  public void setUserPassword( IPerson AnIndividual, String OriginalPassword ) throws Exception {

    if( OriginalPassword != null )
      if( !Md5passwd.verifyPassword( (String)AnIndividual.getAttribute(
        Constants.UNFIELD ), OriginalPassword ))
          throw new Exception( Constants.ERRMSG_PWDNOTMATACHED );

      String newpwd = Constants.ACCOUNTLOCK;

      if( !((String)AnIndividual.getAttribute(
            Constants.ENCRYPTPWDFIELD )).equals( Constants.NULLIFYUSER ))
         newpwd = Md5passwd.encode(
            (String)AnIndividual.getAttribute( Constants.ENCRYPTPWDFIELD ));

      Connection C = getDBConn();

      C.createStatement().execute(

        MessageFormat.format(
           UPDPWD, new String[] {

             SINGLEQUOTE + newpwd + SINGLEQUOTE,

             SINGLEQUOTE
                 + Constants.SDF.format( new java.util.Date()) + SINGLEQUOTE,

             SINGLEQUOTE
               + (String)AnIndividual.getAttribute( Constants.UNFIELD )
                 + SINGLEQUOTE

         } ));

     releaseConn( C );

  }// setUserPassword

  public void removeUser( IPerson AnIndividual ) throws Exception {

       IPerson per= new PersonImpl();
       per.setAttribute(IPerson.USERNAME, AnIndividual.getAttribute( Constants.UNFIELD ));

       int portalUID = -1;
       try{
          portalUID = rdbmuser.getPortalUID(per, false);
       }catch(org.jasig.portal.AuthorizationException ae) { /* do nothing */ }

       if( portalUID > -1 ) {
           rdbmuser.removePortalUID(portalUID);

         String userName = (String)AnIndividual.getAttribute( Constants.UNFIELD );
         IGroupMember gm =
          GroupService.getGroupMember( userName, IPerson.class );

         for (Iterator itr = gm.getContainingGroups(); itr.hasNext(); ) {
             IEntityGroup group = (IEntityGroup) itr.next();

             if ( group.isEditable() ) {
               IEntityGroup lg =
                 GroupService.findLockableGroup( group.getKey(), this.getClass().getName() );

               lg.removeMember( gm );
               lg.update();

               LOG.info("Removed " + userName + " from " + group.getKey());
             }// if
          }// for

           DeleteUser.deleteBookmarks(portalUID);
       }// if

       AnIndividual.setAttribute( Constants.ENCRYPTPWDFIELD,
                                      Constants.NULLIFYUSER );

       setUserPassword( AnIndividual, null );
  }// removeUser

  private Connection getDBConn(){ return  RDBMServices.getConnection(); }

  private void releaseConn( ResultSet R, Connection C ) throws Exception {
     R.close();
     releaseConn( C );
  } // releaseConn

  private void releaseConn( Connection C ){ RDBMServices.releaseConnection( C ); }

  private IPerson[] runQuery( String Query ) throws Exception {
    return runQuery( Query, null );
  }// runQuery

  private IPerson[] runQuery( String Query, String Conditional ) throws Exception {

    Connection C = getDBConn();
    ResultSet R = C.createStatement().executeQuery(
      (Conditional == null? Query :
        MessageFormat.format( Query, new String[] {
                              SINGLEQUOTE + Conditional + SINGLEQUOTE } )));

    IPerson[] people = mkIPeople( R );

    // be good doobies
    releaseConn( R, C );

    return people;
  }// runQuery

  private IPerson[] mkIPeople( ResultSet R ) throws Exception {

     Vector v = new Vector();
     IPerson person = null;

     while( R.next() ){

       person = new PersonImpl();

       for( int i = 1; i <= R.getMetaData().getColumnCount(); i++ ) {

           if( R.getMetaData().getColumnType( i ) != Types.TIMESTAMP )
             person.setAttribute(
                   R.getMetaData().getColumnName( i ).toLowerCase(),
                      (R.getString( i )==null?"":R.getString( i )));
            else
             person.setAttribute(
                   R.getMetaData().getColumnName( i ).toLowerCase(),
                      (R.getString( i )==null?"": Constants.SDF.format(
                         new java.util.Date( R.getTimestamp( i ).getTime()) )));

       }// for

       v.addElement( person );
     }// while

     IPerson pwdtst = null;
     IPerson[] people = new IPerson[ v.size() ];
     for( int i = 0; i < people.length; i++ ) {
       people[ i ] = (IPerson)v.elementAt( i );

       // if the user exists but has no layout information, the account
       // is considered to be locked, this is technically inaccurate
       try{
         pwdtst = new PersonImpl();

         pwdtst.setAttribute( IPerson.USERNAME,
              people[ i ].getAttribute( Constants.UNFIELD ));

         rdbmuser.getPortalUID( pwdtst, false );
       }catch(org.jasig.portal.AuthorizationException ae) {

           people[ i ].setAttribute( Constants.ENCRYPTPWDFIELD,
                         Constants.ACCOUNTLOCKACKNOWLEDGE );
         }// catch

     }// for

     return people;
  }// mkIPerson

}// eoc
